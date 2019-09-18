package android.view;

import android.animation.LayoutTransition;
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
import android.contentsensor.ContentSensorManagerFactory;
import android.contentsensor.IContentSensorManager;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.Region;
import android.graphics.drawable.AnimatedVectorDrawable;
import android.graphics.drawable.Drawable;
import android.hardware.display.DisplayManager;
import android.hardware.input.InputManager;
import android.media.AudioManager;
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
import android.os.ServiceManager;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.Trace;
import android.scrollerboost.ScrollerBoostManager;
import android.util.AndroidRuntimeException;
import android.util.DisplayMetrics;
import android.util.Flog;
import android.util.HwPCUtils;
import android.util.Jlog;
import android.util.JlogConstants;
import android.util.Log;
import android.util.LongArray;
import android.util.MergedConfiguration;
import android.util.Pair;
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
import android.view.SurfaceHolder;
import android.view.ThreadedRenderer;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityManager;
import android.view.accessibility.AccessibilityNodeInfo;
import android.view.accessibility.AccessibilityNodeProvider;
import android.view.accessibility.IAccessibilityInteractionConnection;
import android.view.accessibility.IAccessibilityInteractionConnectionCallback;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Interpolator;
import android.view.autofill.AutofillManager;
import android.view.inputmethod.InputMethodManager;
import android.vrsystem.IVRSystemServiceManager;
import android.widget.Scroller;
import com.android.internal.annotations.GuardedBy;
import com.android.internal.os.IResultReceiver;
import com.android.internal.os.SomeArgs;
import com.android.internal.policy.PhoneFallbackEventHandler;
import com.android.internal.util.Preconditions;
import com.android.internal.view.BaseSurfaceHolder;
import com.android.internal.view.IInputMethodManager;
import com.android.internal.view.RootViewSurfaceTaker;
import com.android.internal.view.SurfaceCallbackHelper;
import com.huawei.pgmng.log.LogPower;
import java.io.FileDescriptor;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.lang.annotation.RCUnownedThisRef;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.CountDownLatch;

public final class ViewRootImpl implements ViewParent, View.AttachInfo.Callbacks, ThreadedRenderer.DrawCallbacks {
    private static final String APS_DROP_EMPTY_FRAME = "sys.aps.drop_empty_frame";
    private static final boolean DBG = false;
    private static final boolean DEBUG_CONFIGURATION = false;
    private static final boolean DEBUG_DIALOG = false;
    private static final boolean DEBUG_DRAW = false;
    private static final boolean DEBUG_FPS = false;
    /* access modifiers changed from: private */
    public static final boolean DEBUG_HWFLOW = (SystemProperties.getBoolean("ro.debuggable", false) || SystemProperties.getInt("ro.logsystem.usertype", 1) == 3 || SystemProperties.getInt("ro.logsystem.usertype", 1) == 5);
    private static final boolean DEBUG_IMF = false;
    private static final boolean DEBUG_INPUT_RESIZE = false;
    private static final boolean DEBUG_INPUT_STAGES = false;
    private static final boolean DEBUG_KEEP_SCREEN_ON = false;
    private static final boolean DEBUG_LAYOUT = false;
    /* access modifiers changed from: private */
    public static final boolean DEBUG_MOVING = SystemProperties.getBoolean("ro.config.hw_log", false);
    private static final boolean DEBUG_ORIENTATION = false;
    private static final boolean DEBUG_TRACKBALL = false;
    private static final boolean FRONT_FINGERPRINT_NAVIGATION = SystemProperties.getBoolean("ro.config.hw_front_fp_navi", false);
    /* access modifiers changed from: private */
    public static final boolean IS_TABLET = "tablet".equals(SystemProperties.get("ro.build.characteristics", ""));
    /* access modifiers changed from: private */
    public static final boolean IS_USER_DOMESTIC_BETA;
    private static final boolean LOCAL_LOGV = false;
    private static final int MAX_QUEUED_INPUT_EVENT_POOL_SIZE = 10;
    static final int MAX_TRACKBALL_DELAY = 250;
    protected static final long MIN_PERIOD = 500;
    private static final int MSG_CHECK_FOCUS = 13;
    private static final int MSG_CLEAR_ACCESSIBILITY_FOCUS_HOST = 21;
    private static final int MSG_CLOSE_SYSTEM_DIALOGS = 14;
    private static final int MSG_DIE = 3;
    private static final int MSG_DISPATCH_APP_VISIBILITY = 8;
    private static final int MSG_DISPATCH_DRAG_EVENT = 15;
    private static final int MSG_DISPATCH_DRAG_LOCATION_EVENT = 16;
    private static final int MSG_DISPATCH_GET_NEW_SURFACE = 9;
    private static final int MSG_DISPATCH_INPUT_EVENT = 7;
    private static final int MSG_DISPATCH_KEY_FROM_AUTOFILL = 12;
    private static final int MSG_DISPATCH_KEY_FROM_IME = 11;
    private static final int MSG_DISPATCH_SYSTEM_UI_VISIBILITY = 17;
    private static final int MSG_DISPATCH_WINDOW_SHOWN = 25;
    private static final int MSG_DRAW_FINISHED = 29;
    private static final int MSG_INVALIDATE = 1;
    private static final int MSG_INVALIDATE_RECT = 2;
    private static final int MSG_INVALIDATE_WORLD = 22;
    private static final int MSG_POINTER_CAPTURE_CHANGED = 28;
    private static final int MSG_PROCESS_INPUT_EVENTS = 19;
    private static final int MSG_REQUEST_KEYBOARD_SHORTCUTS = 26;
    private static final int MSG_RESIZED = 4;
    private static final int MSG_RESIZED_REPORT = 5;
    private static final int MSG_SYNTHESIZE_INPUT_EVENT = 24;
    private static final int MSG_UPDATE_CONFIGURATION = 18;
    private static final int MSG_UPDATE_POINTER_ICON = 27;
    private static final int MSG_WINDOW_FOCUS_CHANGED = 6;
    private static final int MSG_WINDOW_MOVED = 23;
    private static final boolean MT_RENDERER_AVAILABLE = true;
    private static final String PROPERTY_ALLOC_BUFFER_SYNC = "persist.kirin.alloc_buffers_sync";
    public static final String PROPERTY_EMULATOR_WIN_OUTSET_BOTTOM_PX = "ro.emu.win_outset_bottom_px";
    private static final String PROPERTY_PROFILE_RENDERING = "viewroot.profile_rendering";
    private static final long REDUNDANT = 500000;
    private static final long SF_VSYNC_OFFSET = 5000000;
    private static final String TAG = "ViewRootImpl";
    private static final long UNLOCK_VIEW_DRAW_TIME = 16;
    private static final long VSYNC_OFFSET = 7500000;
    private static final long VSYNC_SPAN = 16666667;
    /* access modifiers changed from: private */
    public static boolean mDebugUnlockKeyguard = false;
    static final Interpolator mResizeInterpolator = new AccelerateDecelerateInterpolator();
    private static final boolean mSupportAod = "1".equals(SystemProperties.get("ro.config.support_aod", null));
    private static boolean sAlwaysAssignFocus;
    private static boolean sCompatibilityDone = false;
    private static final ArrayList<ConfigChangedCallback> sConfigCallbacks = new ArrayList<>();
    /* access modifiers changed from: private */
    public static long sDebugKeyguardViewDrawTime = 0;
    static boolean sFirstDrawComplete = false;
    static final ArrayList<Runnable> sFirstDrawHandlers = new ArrayList<>();
    /* access modifiers changed from: private */
    public static boolean sIsFirstFrame = false;
    static final ArrayList<Pair<Long, Long>> sJankList = new ArrayList<>();
    protected static long sLastRelayoutNotifyTime = 0;
    protected static long sRelayoutNotifyPeriod = 0;
    static final ThreadLocal<HandlerActionQueue> sRunQueues = new ThreadLocal<>();
    static final RemoteCallbackList<IWindowLayoutObserver> sWindowLayoutObservers = new RemoteCallbackList<>();
    private int lastFrameDefer;
    View mAccessibilityFocusedHost;
    AccessibilityNodeInfo mAccessibilityFocusedVirtualView;
    final AccessibilityInteractionConnectionManager mAccessibilityInteractionConnectionManager = new AccessibilityInteractionConnectionManager();
    AccessibilityInteractionController mAccessibilityInteractionController;
    final AccessibilityManager mAccessibilityManager;
    private ActivityConfigCallback mActivityConfigCallback;
    private boolean mActivityRelaunched;
    boolean mAdded;
    boolean mAddedTouchMode;
    /* access modifiers changed from: private */
    public boolean mAllocBufferAsync = false;
    private boolean mAllocBufferSync = SystemProperties.getBoolean(PROPERTY_ALLOC_BUFFER_SYNC, false);
    private boolean mAppVisibilityChanged;
    boolean mAppVisible = true;
    boolean mApplyInsetsRequested;
    final View.AttachInfo mAttachInfo;
    AudioManager mAudioManager;
    final String mBasePackageName;
    public View mBeingInvalidatedChild = null;
    private int mCanvasOffsetX;
    private int mCanvasOffsetY;
    Choreographer mChoreographer;
    int mClientWindowLayoutFlags;
    final ConsumeBatchedInputImmediatelyRunnable mConsumeBatchedInputImmediatelyRunnable;
    boolean mConsumeBatchedInputImmediatelyScheduled;
    boolean mConsumeBatchedInputScheduled;
    final ConsumeBatchedInputRunnable mConsumedBatchedInputRunnable;
    final Context mContext;
    int mCurScrollY;
    View mCurrentDragView;
    private PointerIcon mCustomPointerIcon = null;
    /* access modifiers changed from: private */
    public long mDeliverInputTime;
    private final int mDensity;
    Rect mDirty;
    final Rect mDispatchContentInsets = new Rect();
    DisplayCutout mDispatchDisplayCutout = DisplayCutout.NO_CUTOUT;
    final Rect mDispatchStableInsets = new Rect();
    Display mDisplay;
    private final DisplayManager.DisplayListener mDisplayListener;
    final DisplayManager mDisplayManager;
    ClipDescription mDragDescription;
    final PointF mDragPoint = new PointF();
    /* access modifiers changed from: private */
    public boolean mDragResizing;
    boolean mDrawingAllowed;
    int mDrawsNeededToReport;
    private boolean mEventChanged = false;
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
    boolean mHadWindowFocus;
    final ViewRootHandler mHandler;
    boolean mHandlingLayoutInLayoutRequest = false;
    int mHardwareXOffset;
    int mHardwareYOffset;
    boolean mHasHadWindowFocus;
    int mHeight;
    final HighContrastTextManager mHighContrastTextManager;
    private boolean mInLayout = false;
    InputChannel mInputChannel;
    protected final InputEventConsistencyVerifier mInputEventConsistencyVerifier;
    WindowInputEventReceiver mInputEventReceiver;
    InputQueue mInputQueue;
    InputQueue.Callback mInputQueueCallback;
    final InvalidateOnAnimationRunnable mInvalidateOnAnimationRunnable;
    private boolean mInvalidateRootRequested;
    boolean mIsAmbientMode = false;
    public boolean mIsAnimating;
    boolean mIsCreating;
    boolean mIsDrawing;
    public boolean mIsDropEmptyFrame = true;
    boolean mIsInTraversal;
    /* access modifiers changed from: private */
    public final Configuration mLastConfigurationFromResources = new Configuration();
    final ViewTreeObserver.InternalInsetsInfo mLastGivenInsets = new ViewTreeObserver.InternalInsetsInfo();
    boolean mLastInCompatMode = false;
    boolean mLastOverscanRequested;
    /* access modifiers changed from: private */
    public final MergedConfiguration mLastReportedMergedConfiguration = new MergedConfiguration();
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
    private ThreadedRenderer.FrameDrawingCallback mNextRtFrameCallback;
    private final int mNoncompatDensity;
    public Point mOffset = null;
    int mOrigWindowType = -1;
    boolean mPausedForTransition = false;
    boolean mPendingAlwaysConsumeNavBar;
    final Rect mPendingBackDropFrame = new Rect();
    final Rect mPendingContentInsets = new Rect();
    final DisplayCutout.ParcelableWrapper mPendingDisplayCutout = new DisplayCutout.ParcelableWrapper(DisplayCutout.NO_CUTOUT);
    int mPendingInputEventCount;
    QueuedInputEvent mPendingInputEventHead;
    String mPendingInputEventQueueLengthCounterName = "pq";
    QueuedInputEvent mPendingInputEventTail;
    /* access modifiers changed from: private */
    public final MergedConfiguration mPendingMergedConfiguration = new MergedConfiguration();
    final Rect mPendingOutsets = new Rect();
    final Rect mPendingOverscanInsets = new Rect();
    final Rect mPendingStableInsets = new Rect();
    private ArrayList<LayoutTransition> mPendingTransitions;
    final Rect mPendingVisibleInsets = new Rect();
    private String mPkgName;
    boolean mPointerCapture;
    /* access modifiers changed from: private */
    public int mPointerIconType = 1;
    final Region mPreviousTransparentRegion;
    boolean mProcessInputEventsScheduled;
    private boolean mProfile;
    /* access modifiers changed from: private */
    public boolean mProfileRendering;
    private QueuedInputEvent mQueuedInputEventPool;
    private int mQueuedInputEventPoolSize;
    private boolean mRemoved;
    /* access modifiers changed from: private */
    public Choreographer.FrameCallback mRenderProfiler;
    /* access modifiers changed from: private */
    public boolean mRenderProfilingEnabled;
    boolean mReportNextDraw;
    /* access modifiers changed from: private */
    public int mResizeMode;
    boolean mScrollMayChange;
    int mScrollY;
    Scroller mScroller;
    SendWindowContentChangedAccessibilityEvent mSendWindowContentChangedAccessibilityEvent;
    int mSeq;
    private long mSoftDrawTime;
    private long mSoftDrawTimeUse;
    int mSoftInputMode;
    boolean mStopped = false;
    public final Surface mSurface = new Surface();
    BaseSurfaceHolder mSurfaceHolder;
    SurfaceHolder.Callback2 mSurfaceHolderCallback;
    InputStage mSyntheticInputStage;
    /* access modifiers changed from: private */
    public String mTag;
    final int mTargetSdkVersion;
    HashSet<View> mTempHashSet;
    final Rect mTempRect;
    final Thread mThread;
    final int[] mTmpLocation = new int[2];
    final TypedValue mTmpValue = new TypedValue();
    CompatibilityInfo.Translator mTranslator;
    final Region mTransparentRegion;
    int mTraversalBarrier;
    final TraversalRunnable mTraversalRunnable;
    public boolean mTraversalScheduled;
    boolean mUnbufferedInputDispatch;
    /* access modifiers changed from: private */
    public final UnhandledKeyManager mUnhandledKeyManager = new UnhandledKeyManager();
    @GuardedBy("this")
    boolean mUpcomingInTouchMode;
    @GuardedBy("this")
    boolean mUpcomingWindowFocus;
    private boolean mUseMTRenderer;
    View mView;
    final ViewConfiguration mViewConfiguration;
    private int mViewLayoutDirectionInitial;
    int mViewVisibility;
    final Rect mVisRect;
    private IVRSystemServiceManager mVrMananger;
    int mWidth;
    boolean mWillDrawSoon;
    final Rect mWinFrame;
    final W mWindow;
    public final WindowManager.LayoutParams mWindowAttributes = new WindowManager.LayoutParams();
    boolean mWindowAttributesChanged = false;
    int mWindowAttributesChangesFlag = 0;
    @GuardedBy("mWindowCallbacks")
    final ArrayList<WindowCallbacks> mWindowCallbacks = new ArrayList<>();
    CountDownLatch mWindowDrawCountDown;
    @GuardedBy("this")
    boolean mWindowFocusChanged;
    final IWindowSession mWindowSession;
    private final ArrayList<WindowStoppedCallback> mWindowStoppedCallbacks;

    static final class AccessibilityInteractionConnection extends IAccessibilityInteractionConnection.Stub {
        private final WeakReference<ViewRootImpl> mViewRootImpl;

        AccessibilityInteractionConnection(ViewRootImpl viewRootImpl) {
            this.mViewRootImpl = new WeakReference<>(viewRootImpl);
        }

        public void findAccessibilityNodeInfoByAccessibilityId(long accessibilityNodeId, Region interactiveRegion, int interactionId, IAccessibilityInteractionConnectionCallback callback, int flags, int interrogatingPid, long interrogatingTid, MagnificationSpec spec, Bundle args) {
            ViewRootImpl viewRootImpl = (ViewRootImpl) this.mViewRootImpl.get();
            if (viewRootImpl == null || viewRootImpl.mView == null) {
                try {
                    callback.setFindAccessibilityNodeInfosResult(null, interactionId);
                } catch (RemoteException e) {
                }
            } else {
                viewRootImpl.getAccessibilityInteractionController().findAccessibilityNodeInfoByAccessibilityIdClientThread(accessibilityNodeId, interactiveRegion, interactionId, callback, flags, interrogatingPid, interrogatingTid, spec, args);
                int i = interactionId;
                IAccessibilityInteractionConnectionCallback iAccessibilityInteractionConnectionCallback = callback;
            }
        }

        public void performAccessibilityAction(long accessibilityNodeId, int action, Bundle arguments, int interactionId, IAccessibilityInteractionConnectionCallback callback, int flags, int interrogatingPid, long interrogatingTid) {
            ViewRootImpl viewRootImpl = (ViewRootImpl) this.mViewRootImpl.get();
            if (viewRootImpl == null || viewRootImpl.mView == null) {
                try {
                    callback.setPerformAccessibilityActionResult(false, interactionId);
                } catch (RemoteException e) {
                }
            } else {
                viewRootImpl.getAccessibilityInteractionController().performAccessibilityActionClientThread(accessibilityNodeId, action, arguments, interactionId, callback, flags, interrogatingPid, interrogatingTid);
                int i = interactionId;
                IAccessibilityInteractionConnectionCallback iAccessibilityInteractionConnectionCallback = callback;
            }
        }

        public void findAccessibilityNodeInfosByViewId(long accessibilityNodeId, String viewId, Region interactiveRegion, int interactionId, IAccessibilityInteractionConnectionCallback callback, int flags, int interrogatingPid, long interrogatingTid, MagnificationSpec spec) {
            ViewRootImpl viewRootImpl = (ViewRootImpl) this.mViewRootImpl.get();
            if (viewRootImpl == null || viewRootImpl.mView == null) {
                try {
                    callback.setFindAccessibilityNodeInfoResult(null, interactionId);
                } catch (RemoteException e) {
                }
            } else {
                viewRootImpl.getAccessibilityInteractionController().findAccessibilityNodeInfosByViewIdClientThread(accessibilityNodeId, viewId, interactiveRegion, interactionId, callback, flags, interrogatingPid, interrogatingTid, spec);
                int i = interactionId;
                IAccessibilityInteractionConnectionCallback iAccessibilityInteractionConnectionCallback = callback;
            }
        }

        public void findAccessibilityNodeInfosByText(long accessibilityNodeId, String text, Region interactiveRegion, int interactionId, IAccessibilityInteractionConnectionCallback callback, int flags, int interrogatingPid, long interrogatingTid, MagnificationSpec spec) {
            ViewRootImpl viewRootImpl = (ViewRootImpl) this.mViewRootImpl.get();
            if (viewRootImpl == null || viewRootImpl.mView == null) {
                try {
                    callback.setFindAccessibilityNodeInfosResult(null, interactionId);
                } catch (RemoteException e) {
                }
            } else {
                viewRootImpl.getAccessibilityInteractionController().findAccessibilityNodeInfosByTextClientThread(accessibilityNodeId, text, interactiveRegion, interactionId, callback, flags, interrogatingPid, interrogatingTid, spec);
                int i = interactionId;
                IAccessibilityInteractionConnectionCallback iAccessibilityInteractionConnectionCallback = callback;
            }
        }

        public void findFocus(long accessibilityNodeId, int focusType, Region interactiveRegion, int interactionId, IAccessibilityInteractionConnectionCallback callback, int flags, int interrogatingPid, long interrogatingTid, MagnificationSpec spec) {
            ViewRootImpl viewRootImpl = (ViewRootImpl) this.mViewRootImpl.get();
            if (viewRootImpl == null || viewRootImpl.mView == null) {
                try {
                    callback.setFindAccessibilityNodeInfoResult(null, interactionId);
                } catch (RemoteException e) {
                }
            } else {
                viewRootImpl.getAccessibilityInteractionController().findFocusClientThread(accessibilityNodeId, focusType, interactiveRegion, interactionId, callback, flags, interrogatingPid, interrogatingTid, spec);
                int i = interactionId;
                IAccessibilityInteractionConnectionCallback iAccessibilityInteractionConnectionCallback = callback;
            }
        }

        public void focusSearch(long accessibilityNodeId, int direction, Region interactiveRegion, int interactionId, IAccessibilityInteractionConnectionCallback callback, int flags, int interrogatingPid, long interrogatingTid, MagnificationSpec spec) {
            ViewRootImpl viewRootImpl = (ViewRootImpl) this.mViewRootImpl.get();
            if (viewRootImpl == null || viewRootImpl.mView == null) {
                try {
                    callback.setFindAccessibilityNodeInfoResult(null, interactionId);
                } catch (RemoteException e) {
                }
            } else {
                viewRootImpl.getAccessibilityInteractionController().focusSearchClientThread(accessibilityNodeId, direction, interactiveRegion, interactionId, callback, flags, interrogatingPid, interrogatingTid, spec);
                int i = interactionId;
                IAccessibilityInteractionConnectionCallback iAccessibilityInteractionConnectionCallback = callback;
            }
        }
    }

    final class AccessibilityInteractionConnectionManager implements AccessibilityManager.AccessibilityStateChangeListener {
        AccessibilityInteractionConnectionManager() {
        }

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

    public interface ActivityConfigCallback {
        void onConfigurationChanged(Configuration configuration, int i);
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
        public void forward(QueuedInputEvent q) {
            QueuedInputEvent curr;
            q.mFlags &= -3;
            QueuedInputEvent curr2 = this.mQueueHead;
            if (curr2 == null) {
                super.forward(q);
                return;
            }
            int deviceId = q.mEvent.getDeviceId();
            QueuedInputEvent prev = null;
            boolean blocked = false;
            while (curr2 != null && curr2 != q) {
                if (!blocked && deviceId == curr2.mEvent.getDeviceId()) {
                    blocked = true;
                }
                prev = curr2;
                curr2 = curr2.mNext;
            }
            if (blocked) {
                if (curr2 == null) {
                    enqueue(q);
                }
                return;
            }
            if (curr2 != null) {
                curr2 = curr2.mNext;
                dequeue(q, prev);
            }
            super.forward(q);
            while (curr != null) {
                if (deviceId != curr.mEvent.getDeviceId()) {
                    prev = curr;
                    curr = curr.mNext;
                } else if ((curr.mFlags & 2) != 0) {
                    break;
                } else {
                    QueuedInputEvent next = curr.mNext;
                    dequeue(curr, prev);
                    super.forward(curr);
                    curr = next;
                }
            }
        }

        /* access modifiers changed from: protected */
        public void apply(QueuedInputEvent q, int result) {
            if (result == 3) {
                defer(q);
            } else {
                super.apply(q, result);
            }
        }

        private void enqueue(QueuedInputEvent q) {
            if (this.mQueueTail == null) {
                this.mQueueHead = q;
                this.mQueueTail = q;
            } else {
                this.mQueueTail.mNext = q;
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
        public void dump(String prefix, PrintWriter writer) {
            writer.print(prefix);
            writer.print(getClass().getName());
            writer.print(": mQueueLength=");
            writer.println(this.mQueueLength);
            super.dump(prefix, writer);
        }
    }

    public static final class CalledFromWrongThreadException extends AndroidRuntimeException {
        public CalledFromWrongThreadException(String msg) {
            super(msg);
        }
    }

    public interface ConfigChangedCallback {
        void onConfigurationChanged(Configuration configuration);
    }

    @RCUnownedThisRef
    final class ConsumeBatchedInputImmediatelyRunnable implements Runnable {
        ConsumeBatchedInputImmediatelyRunnable() {
        }

        public void run() {
            ViewRootImpl.this.doConsumeBatchedInput(-1);
        }
    }

    final class ConsumeBatchedInputRunnable implements Runnable {
        ConsumeBatchedInputRunnable() {
        }

        public void run() {
            long runstart = System.nanoTime();
            ViewRootImpl.this.doConsumeBatchedInput(ViewRootImpl.this.mChoreographer.getFrameTimeNanos());
            long unused = ViewRootImpl.this.mDeliverInputTime = System.nanoTime() - runstart;
        }
    }

    @RCUnownedThisRef
    final class EarlyPostImeInputStage extends InputStage {
        public EarlyPostImeInputStage(InputStage next) {
            super(next);
        }

        /* access modifiers changed from: protected */
        public int onProcess(QueuedInputEvent q) {
            if (q.mEvent instanceof KeyEvent) {
                return processKeyEvent(q);
            }
            if ((q.mEvent.getSource() & 2) != 0) {
                return processPointerEvent(q);
            }
            return 0;
        }

        private int processKeyEvent(QueuedInputEvent q) {
            KeyEvent event = (KeyEvent) q.mEvent;
            if (ViewRootImpl.this.mAttachInfo.mTooltipHost != null) {
                ViewRootImpl.this.mAttachInfo.mTooltipHost.handleTooltipKey(event);
            }
            if (ViewRootImpl.IS_TABLET && event.getAction() == 0 && event.getKeyCode() == 62 && event.isShiftPressed() && event.getRepeatCount() == 0) {
                IInputMethodManager inputMethodManager = IInputMethodManager.Stub.asInterface(ServiceManager.getService("input_method"));
                if (inputMethodManager != null) {
                    try {
                        inputMethodManager.showInputMethodPickerFromClient(null, 2);
                    } catch (RemoteException e) {
                        Log.e(ViewRootImpl.TAG, "showInputMethodPicker failed");
                    }
                }
                return 1;
            } else if (ViewRootImpl.this.checkForLeavingTouchModeAndConsume(event)) {
                return 1;
            } else {
                ViewRootImpl.this.mFallbackEventHandler.preDispatchKeyEvent(event);
                return 0;
            }
        }

        private int processPointerEvent(QueuedInputEvent q) {
            MotionEvent event = (MotionEvent) q.mEvent;
            if (ViewRootImpl.this.mDisplay != null && ViewRootImpl.this.isValidExtDisplayId(ViewRootImpl.this.mDisplay.getDisplayId())) {
                event.setOffset(ViewRootImpl.this.mOffset);
            }
            if (ViewRootImpl.this.mTranslator != null) {
                ViewRootImpl.this.mTranslator.translateEventInScreenToAppWindow(event);
            }
            IHwApsImpl hwApsImpl = HwFrameworkFactory.getHwApsImpl();
            if (hwApsImpl.isSupportAps() && hwApsImpl.isGameProcess(ViewRootImpl.this.mContext.getPackageName())) {
                hwApsImpl.initAPS(ViewRootImpl.this.mView.getContext(), ViewRootImpl.this.mView.getResources().getDisplayMetrics().widthPixels, Process.myPid());
                hwApsImpl.adaptPowerSave(ViewRootImpl.this.mView.getContext(), event);
            }
            int action = event.getAction();
            if (action == 0 || action == 8) {
                if (ViewRootImpl.this.mDisplay == null || !ViewRootImpl.this.isValidExtDisplayId(ViewRootImpl.this.mDisplay.getDisplayId())) {
                    ViewRootImpl.this.ensureTouchMode(event.isFromSource(InputDevice.SOURCE_TOUCHSCREEN));
                } else {
                    HwPCUtils.log(ViewRootImpl.TAG, "ensureTouchMode true");
                    ViewRootImpl.this.ensureTouchMode(true);
                }
            }
            if (action == 0) {
                AutofillManager afm = ViewRootImpl.this.getAutofillManager();
                if (afm != null) {
                    afm.requestHideFillUi();
                }
            }
            if (action == 0 && ViewRootImpl.this.mAttachInfo.mTooltipHost != null) {
                ViewRootImpl.this.mAttachInfo.mTooltipHost.hideTooltip();
            }
            if (ViewRootImpl.this.mCurScrollY != 0) {
                event.offsetLocation(0.0f, (float) ViewRootImpl.this.mCurScrollY);
            }
            if (event.isTouchEvent()) {
                ViewRootImpl.this.mLastTouchPoint.x = event.getRawX();
                ViewRootImpl.this.mLastTouchPoint.y = event.getRawY();
                ViewRootImpl.this.mLastTouchSource = event.getSource();
            }
            return 0;
        }
    }

    @RCUnownedThisRef
    final class HighContrastTextManager implements AccessibilityManager.HighTextContrastChangeListener {
        HighContrastTextManager() {
            ThreadedRenderer.setHighContrastText(ViewRootImpl.this.mAccessibilityManager.isHighTextContrastEnabled());
        }

        public void onHighTextContrastStateChanged(boolean enabled) {
            ThreadedRenderer.setHighContrastText(enabled);
            ViewRootImpl.this.destroyHardwareResources();
            ViewRootImpl.this.invalidate();
        }
    }

    @RCUnownedThisRef
    final class ImeInputStage extends AsyncInputStage implements InputMethodManager.FinishedInputEventCallback {
        public ImeInputStage(InputStage next, String traceCounter) {
            super(next, traceCounter);
        }

        /* access modifiers changed from: protected */
        public int onProcess(QueuedInputEvent q) {
            if (ViewRootImpl.this.mLastWasImTarget && !ViewRootImpl.this.isInLocalFocusMode()) {
                InputMethodManager imm = InputMethodManager.peekInstance();
                if (imm != null) {
                    int result = imm.dispatchInputEvent(q.mEvent, q, this, ViewRootImpl.this.mHandler);
                    if (result == 1) {
                        return 1;
                    }
                    if (result == 0) {
                        return 0;
                    }
                    return 3;
                }
            }
            return 0;
        }

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
    abstract class InputStage {
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
            if (this.mNext != null) {
                this.mNext.deliver(q);
            } else {
                ViewRootImpl.this.finishInputEvent(q);
            }
        }

        /* access modifiers changed from: protected */
        public void onWindowFocusChanged(boolean hasWindowFocus) {
            if (this.mNext != null) {
                this.mNext.onWindowFocusChanged(hasWindowFocus);
            }
        }

        /* access modifiers changed from: protected */
        public void onDetachedFromWindow() {
            if (this.mNext != null) {
                this.mNext.onDetachedFromWindow();
            }
        }

        /* access modifiers changed from: protected */
        public boolean shouldDropInputEvent(QueuedInputEvent q) {
            if (ViewRootImpl.this.mView == null || !ViewRootImpl.this.mAdded) {
                String access$1700 = ViewRootImpl.this.mTag;
                Slog.w(access$1700, "Dropping event due to root view being removed: " + q.mEvent);
                return true;
            } else if ((ViewRootImpl.this.mAttachInfo.mHasWindowFocus || q.mEvent.isFromSource(2) || ViewRootImpl.this.isAutofillUiShowing()) && !ViewRootImpl.this.mStopped && ((!ViewRootImpl.this.mIsAmbientMode || q.mEvent.isFromSource(1)) && (!ViewRootImpl.this.mPausedForTransition || isBack(q.mEvent)))) {
                return false;
            } else {
                String access$17002 = ViewRootImpl.this.mTag;
                Slog.w(access$17002, "hasFocus:" + ViewRootImpl.this.mAttachInfo.mHasWindowFocus + ", stopped:" + ViewRootImpl.this.mStopped + ", isAmbientMode:" + ViewRootImpl.this.mIsAmbientMode + ", paused:" + ViewRootImpl.this.mPausedForTransition + ", flags:" + q.mFlags);
                if (ViewRootImpl.this.mDisplay != null && HwPCUtils.isValidExtDisplayId(ViewRootImpl.this.mDisplay.getDisplayId()) && (q.mEvent instanceof KeyEvent) && (((KeyEvent) q.mEvent).getFlags() & 4096) != 0) {
                    HwPCUtils.log(ViewRootImpl.this.mTag, "shouldDropInputEvent: no, ext display key event");
                    return false;
                } else if (ViewRootImpl.isTerminalInputEvent(q.mEvent)) {
                    q.mEvent.cancel();
                    String access$17003 = ViewRootImpl.this.mTag;
                    Slog.w(access$17003, "Cancelling event due to no window focus: " + q.mEvent);
                    return false;
                } else {
                    String access$17004 = ViewRootImpl.this.mTag;
                    Slog.w(access$17004, "Dropping event due to no window focus: " + q.mEvent);
                    return true;
                }
            }
        }

        /* access modifiers changed from: package-private */
        public void dump(String prefix, PrintWriter writer) {
            if (this.mNext != null) {
                this.mNext.dump(prefix, writer);
            }
        }

        private boolean isBack(InputEvent event) {
            boolean z = false;
            if (!(event instanceof KeyEvent)) {
                return false;
            }
            if (((KeyEvent) event).getKeyCode() == 4) {
                z = true;
            }
            return z;
        }
    }

    @RCUnownedThisRef
    final class InvalidateOnAnimationRunnable implements Runnable {
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

        public void run() {
            int i;
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
            for (int i2 = 0; i2 < viewCount; i2++) {
                this.mTempViews[i2].invalidate();
                this.mTempViews[i2] = null;
            }
            for (i = 0; i < viewRectCount; i++) {
                View.AttachInfo.InvalidateInfo info = this.mTempViewRects[i];
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

    @RCUnownedThisRef
    final class NativePostImeInputStage extends AsyncInputStage implements InputQueue.FinishedInputEventCallback {
        public NativePostImeInputStage(InputStage next, String traceCounter) {
            super(next, traceCounter);
        }

        /* access modifiers changed from: protected */
        public int onProcess(QueuedInputEvent q) {
            if (ViewRootImpl.this.mInputQueue == null) {
                return 0;
            }
            ViewRootImpl.this.mInputQueue.sendInputEvent(q.mEvent, q, false, this);
            return 3;
        }

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
    final class NativePreImeInputStage extends AsyncInputStage implements InputQueue.FinishedInputEventCallback {
        public NativePreImeInputStage(InputStage next, String traceCounter) {
            super(next, traceCounter);
        }

        /* access modifiers changed from: protected */
        public int onProcess(QueuedInputEvent q) {
            if (ViewRootImpl.this.mInputQueue == null || !(q.mEvent instanceof KeyEvent)) {
                return 0;
            }
            ViewRootImpl.this.mInputQueue.sendInputEvent(q.mEvent, q, true, this);
            return 3;
        }

        public void onFinishedInputEvent(Object token, boolean handled) {
            QueuedInputEvent q = (QueuedInputEvent) token;
            if (handled) {
                finish(q, true);
            } else {
                forward(q);
            }
        }
    }

    private static final class QueuedInputEvent {
        public static final int FLAG_DEFERRED = 2;
        public static final int FLAG_DELIVER_POST_IME = 1;
        public static final int FLAG_FINISHED = 4;
        public static final int FLAG_FINISHED_HANDLED = 8;
        public static final int FLAG_RESYNTHESIZED = 16;
        public static final int FLAG_UNHANDLED = 32;
        public InputEvent mEvent;
        public int mFlags;
        public QueuedInputEvent mNext;
        public InputEventReceiver mReceiver;

        private QueuedInputEvent() {
        }

        public boolean shouldSkipIme() {
            boolean z = true;
            if ((this.mFlags & 1) != 0) {
                return true;
            }
            if (!(this.mEvent instanceof MotionEvent) || (!this.mEvent.isFromSource(2) && !this.mEvent.isFromSource(4194304))) {
                z = false;
            }
            return z;
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
            sb2.append(this.mEvent != null ? "true" : "false");
            sb.append(sb2.toString());
            StringBuilder sb3 = new StringBuilder();
            sb3.append(", hasInputEventReceiver=");
            sb3.append(this.mReceiver != null ? "true" : "false");
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

    @RCUnownedThisRef
    private class SendWindowContentChangedAccessibilityEvent implements Runnable {
        private int mChangeTypes;
        public long mLastEventTimeMillis;
        public StackTraceElement[] mOrigin;
        public View mSource;

        private SendWindowContentChangedAccessibilityEvent() {
            this.mChangeTypes = 0;
        }

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
            if (this.mSource != null) {
                View predecessor = ViewRootImpl.this.getCommonPredecessor(this.mSource, source);
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
                } else if ((source & 2097152) == 2097152) {
                    this.mTouchNavigation.process(event);
                    return 1;
                }
            } else if ((q.mFlags & 32) != 0) {
                this.mKeyboard.process((KeyEvent) q.mEvent);
                return 1;
            }
            return 0;
        }

        /* access modifiers changed from: protected */
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
        public void onWindowFocusChanged(boolean hasWindowFocus) {
            if (!hasWindowFocus) {
                this.mJoystick.cancel();
            }
        }

        /* access modifiers changed from: protected */
        public void onDetachedFromWindow() {
            this.mJoystick.cancel();
        }
    }

    @RCUnownedThisRef
    final class SyntheticJoystickHandler extends Handler {
        private static final int MSG_ENQUEUE_X_AXIS_KEY_REPEAT = 1;
        private static final int MSG_ENQUEUE_Y_AXIS_KEY_REPEAT = 2;
        /* access modifiers changed from: private */
        public final SparseArray<KeyEvent> mDeviceKeyEvents = new SparseArray<>();
        private final JoystickAxesState mJoystickAxesState = new JoystickAxesState();

        @RCUnownedThisRef
        final class JoystickAxesState {
            private static final int STATE_DOWN_OR_RIGHT = 1;
            private static final int STATE_NEUTRAL = 0;
            private static final int STATE_UP_OR_LEFT = -1;
            final int[] mAxisStatesHat = {0, 0};
            final int[] mAxisStatesStick = {0, 0};

            JoystickAxesState() {
            }

            /* access modifiers changed from: package-private */
            public void resetState() {
                this.mAxisStatesHat[0] = 0;
                this.mAxisStatesHat[1] = 0;
                this.mAxisStatesStick[0] = 0;
                this.mAxisStatesStick[1] = 0;
            }

            /* access modifiers changed from: package-private */
            public void updateStateForAxis(MotionEvent event, long time, int axis, float value) {
                int repeatMessage;
                int axisStateIndex;
                int currentState;
                int i = axis;
                if (isXAxis(i)) {
                    axisStateIndex = 0;
                    repeatMessage = 1;
                } else if (isYAxis(i) != 0) {
                    axisStateIndex = 1;
                    repeatMessage = 2;
                } else {
                    float f = value;
                    String access$1700 = ViewRootImpl.this.mTag;
                    Log.e(access$1700, "Unexpected axis " + i + " in updateStateForAxis!");
                    return;
                }
                int newState = joystickAxisValueToState(value);
                if (i == 0 || i == 1) {
                    currentState = this.mAxisStatesStick[axisStateIndex];
                } else {
                    currentState = this.mAxisStatesHat[axisStateIndex];
                }
                if (currentState != newState) {
                    int metaState = event.getMetaState();
                    int deviceId = event.getDeviceId();
                    int source = event.getSource();
                    if (currentState == 1 || currentState == -1) {
                        int keyCode = joystickAxisAndStateToKeycode(i, currentState);
                        if (keyCode != 0) {
                            KeyEvent keyEvent = r8;
                            int deviceId2 = deviceId;
                            KeyEvent keyEvent2 = new KeyEvent(time, time, 1, keyCode, 0, metaState, deviceId2, 0, 1024, source);
                            ViewRootImpl.this.enqueueInputEvent(keyEvent);
                            deviceId = deviceId2;
                            SyntheticJoystickHandler.this.mDeviceKeyEvents.put(deviceId, null);
                        }
                        SyntheticJoystickHandler.this.removeMessages(repeatMessage);
                    }
                    if (newState == 1 || newState == -1) {
                        int keyCode2 = joystickAxisAndStateToKeycode(i, newState);
                        if (keyCode2 != 0) {
                            int deviceId3 = deviceId;
                            int i2 = source;
                            KeyEvent keyEvent3 = new KeyEvent(time, time, 0, keyCode2, 0, metaState, deviceId3, 0, 1024, i2);
                            KeyEvent keyEvent4 = keyEvent3;
                            ViewRootImpl.this.enqueueInputEvent(keyEvent4);
                            Message m = SyntheticJoystickHandler.this.obtainMessage(repeatMessage, keyEvent4);
                            m.setAsynchronous(true);
                            SyntheticJoystickHandler.this.sendMessageDelayed(m, (long) ViewConfiguration.getKeyRepeatTimeout());
                            KeyEvent keyEvent5 = r8;
                            Message message = m;
                            KeyEvent keyEvent6 = keyEvent4;
                            KeyEvent keyEvent7 = new KeyEvent(time, time, 1, keyCode2, 0, metaState, deviceId3, 0, 1056, i2);
                            SyntheticJoystickHandler.this.mDeviceKeyEvents.put(deviceId3, keyEvent5);
                        }
                    } else {
                        int i3 = deviceId;
                    }
                    if (i == 0 || i == 1) {
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
                String access$1700 = ViewRootImpl.this.mTag;
                Log.e(access$1700, "Unknown axis " + axis + " or direction " + state);
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

        public SyntheticJoystickHandler() {
            super(true);
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                case 2:
                    if (ViewRootImpl.this.mAttachInfo.mHasWindowFocus) {
                        KeyEvent oldEvent = (KeyEvent) msg.obj;
                        KeyEvent e = KeyEvent.changeTimeRepeat(oldEvent, SystemClock.uptimeMillis(), oldEvent.getRepeatCount() + 1);
                        ViewRootImpl.this.enqueueInputEvent(e);
                        Message m = obtainMessage(msg.what, e);
                        m.setAsynchronous(true);
                        sendMessageDelayed(m, (long) ViewConfiguration.getKeyRepeatDelay());
                        return;
                    }
                    return;
                default:
                    return;
            }
        }

        public void process(MotionEvent event) {
            switch (event.getActionMasked()) {
                case 2:
                    update(event);
                    return;
                case 3:
                    cancel();
                    return;
                default:
                    String access$1700 = ViewRootImpl.this.mTag;
                    Log.w(access$1700, "Unexpected action: " + event.getActionMasked());
                    return;
            }
        }

        /* access modifiers changed from: private */
        public void cancel() {
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
                MotionEvent motionEvent = event;
                long historicalEventTime = event.getHistoricalEventTime(h);
                this.mJoystickAxesState.updateStateForAxis(motionEvent, historicalEventTime, 0, event.getHistoricalAxisValue(0, 0, h));
                this.mJoystickAxesState.updateStateForAxis(motionEvent, historicalEventTime, 1, event.getHistoricalAxisValue(1, 0, h));
                this.mJoystickAxesState.updateStateForAxis(motionEvent, historicalEventTime, 15, event.getHistoricalAxisValue(15, 0, h));
                this.mJoystickAxesState.updateStateForAxis(motionEvent, historicalEventTime, 16, event.getHistoricalAxisValue(16, 0, h));
            }
            long time = event.getEventTime();
            this.mJoystickAxesState.updateStateForAxis(event, time, 0, event.getAxisValue(0));
            this.mJoystickAxesState.updateStateForAxis(event, time, 1, event.getAxisValue(1));
            this.mJoystickAxesState.updateStateForAxis(event, time, 15, event.getAxisValue(15));
            this.mJoystickAxesState.updateStateForAxis(event, time, 16, event.getAxisValue(16));
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
            @RCUnownedThisRef
            public void run() {
                long time = SystemClock.uptimeMillis();
                SyntheticTouchNavigationHandler.this.sendKeyDownOrRepeat(time, SyntheticTouchNavigationHandler.this.mPendingKeyCode, SyntheticTouchNavigationHandler.this.mPendingKeyMetaState);
                SyntheticTouchNavigationHandler.access$3432(SyntheticTouchNavigationHandler.this, SyntheticTouchNavigationHandler.FLING_TICK_DECAY);
                if (!SyntheticTouchNavigationHandler.this.postFling(time)) {
                    boolean unused = SyntheticTouchNavigationHandler.this.mFlinging = false;
                    SyntheticTouchNavigationHandler.this.finishKeys(time);
                }
            }
        };
        private float mFlingVelocity;
        /* access modifiers changed from: private */
        public boolean mFlinging;
        private float mLastX;
        private float mLastY;
        /* access modifiers changed from: private */
        public int mPendingKeyCode = 0;
        private long mPendingKeyDownTime;
        /* access modifiers changed from: private */
        public int mPendingKeyMetaState;
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

        public void process(MotionEvent event) {
            MotionEvent motionEvent = event;
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
                        this.mConfigMinFlingVelocity = MIN_FLING_VELOCITY_TICKS_PER_SECOND * this.mConfigTickDistance;
                        this.mConfigMaxFlingVelocity = MAX_FLING_VELOCITY_TICKS_PER_SECOND * this.mConfigTickDistance;
                    }
                }
            }
            if (this.mCurrentDeviceSupported) {
                int action = event.getActionMasked();
                switch (action) {
                    case 0:
                        boolean caughtFling = this.mFlinging;
                        finishKeys(time);
                        finishTracking(time);
                        this.mActivePointerId = motionEvent.getPointerId(0);
                        this.mVelocityTracker = VelocityTracker.obtain();
                        this.mVelocityTracker.addMovement(motionEvent);
                        this.mStartX = event.getX();
                        this.mStartY = event.getY();
                        this.mLastX = this.mStartX;
                        this.mLastY = this.mStartY;
                        this.mAccumulatedX = 0.0f;
                        this.mAccumulatedY = 0.0f;
                        this.mConsumedMovement = caughtFling;
                        break;
                    case 1:
                    case 2:
                        if (this.mActivePointerId >= 0) {
                            int index = motionEvent.findPointerIndex(this.mActivePointerId);
                            if (index >= 0) {
                                this.mVelocityTracker.addMovement(motionEvent);
                                float x = motionEvent.getX(index);
                                float y = motionEvent.getY(index);
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
                                    break;
                                }
                            } else {
                                finishKeys(time);
                                finishTracking(time);
                                break;
                            }
                        }
                        break;
                    case 3:
                        finishKeys(time);
                        finishTracking(time);
                        break;
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
        public void finishKeys(long time) {
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
        public void sendKeyDownOrRepeat(long time, int keyCode, int metaState) {
            long j;
            int i = keyCode;
            if (this.mPendingKeyCode != i) {
                sendKeyUp(time);
                j = time;
                this.mPendingKeyDownTime = j;
                this.mPendingKeyCode = i;
                this.mPendingKeyRepeatCount = 0;
            } else {
                j = time;
                this.mPendingKeyRepeatCount++;
            }
            this.mPendingKeyMetaState = metaState;
            ViewRootImpl viewRootImpl = ViewRootImpl.this;
            KeyEvent keyEvent = r3;
            KeyEvent keyEvent2 = new KeyEvent(this.mPendingKeyDownTime, j, 0, this.mPendingKeyCode, this.mPendingKeyRepeatCount, this.mPendingKeyMetaState, this.mCurrentDeviceId, 1024, this.mCurrentSource);
            viewRootImpl.enqueueInputEvent(keyEvent);
        }

        private void sendKeyUp(long time) {
            if (this.mPendingKeyCode != 0) {
                ViewRootImpl viewRootImpl = ViewRootImpl.this;
                KeyEvent keyEvent = new KeyEvent(this.mPendingKeyDownTime, time, 1, this.mPendingKeyCode, 0, this.mPendingKeyMetaState, this.mCurrentDeviceId, 0, 1024, this.mCurrentSource);
                viewRootImpl.enqueueInputEvent(keyEvent);
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
        public boolean postFling(long time) {
            if (this.mFlingVelocity < this.mConfigMinFlingVelocity) {
                return false;
            }
            postAtTime(this.mFlingRunnable, time + ((long) ((this.mConfigTickDistance / this.mFlingVelocity) * 1000.0f)));
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
    final class SyntheticTrackballHandler {
        private long mLastTime;
        private final TrackballAxis mX = new TrackballAxis();
        private final TrackballAxis mY = new TrackballAxis();

        SyntheticTrackballHandler() {
        }

        public void process(MotionEvent event) {
            long curTime;
            int i;
            long curTime2;
            int i2;
            long curTime3 = SystemClock.uptimeMillis();
            if (this.mLastTime + 250 < curTime3) {
                this.mX.reset(0);
                this.mY.reset(0);
                this.mLastTime = curTime3;
            }
            int action = event.getAction();
            int metaState = event.getMetaState();
            switch (action) {
                case 0:
                    curTime = curTime3;
                    this.mX.reset(2);
                    this.mY.reset(2);
                    ViewRootImpl viewRootImpl = ViewRootImpl.this;
                    KeyEvent keyEvent = r1;
                    KeyEvent keyEvent2 = new KeyEvent(curTime, curTime, 0, 23, 0, metaState, -1, 0, 1024, 257);
                    viewRootImpl.enqueueInputEvent(keyEvent);
                    break;
                case 1:
                    this.mX.reset(2);
                    this.mY.reset(2);
                    KeyEvent keyEvent3 = r1;
                    curTime = curTime3;
                    KeyEvent keyEvent4 = new KeyEvent(curTime3, curTime3, 1, 23, 0, metaState, -1, 0, 1024, 257);
                    ViewRootImpl.this.enqueueInputEvent(keyEvent3);
                    break;
                default:
                    curTime = curTime3;
                    break;
            }
            float xOff = this.mX.collect(event.getX(), event.getEventTime(), "X");
            float yOff = this.mY.collect(event.getY(), event.getEventTime(), "Y");
            int keycode = 0;
            int movement = 0;
            float accel = 1.0f;
            if (xOff > yOff) {
                movement = this.mX.generate();
                if (movement != 0) {
                    if (movement > 0) {
                        i2 = 22;
                    } else {
                        i2 = 21;
                    }
                    keycode = i2;
                    accel = this.mX.acceleration;
                    this.mY.reset(2);
                }
            } else if (yOff > 0.0f) {
                movement = this.mY.generate();
                if (movement != 0) {
                    if (movement > 0) {
                        i = 20;
                    } else {
                        i = 19;
                    }
                    keycode = i;
                    accel = this.mY.acceleration;
                    this.mX.reset(2);
                }
            }
            int keycode2 = keycode;
            float accel2 = accel;
            if (keycode2 != 0) {
                if (movement < 0) {
                    movement = -movement;
                }
                int accelMovement = (int) (((float) movement) * accel2);
                if (accelMovement > movement) {
                    int movement2 = movement - 1;
                    KeyEvent keyEvent5 = r1;
                    int i3 = accelMovement;
                    KeyEvent keyEvent6 = new KeyEvent(curTime, curTime, 2, keycode2, accelMovement - movement2, metaState, -1, 0, 1024, 257);
                    ViewRootImpl.this.enqueueInputEvent(keyEvent5);
                    movement = movement2;
                    curTime2 = curTime;
                } else {
                    curTime2 = curTime;
                }
                while (movement > 0) {
                    int movement3 = movement - 1;
                    long curTime4 = SystemClock.uptimeMillis();
                    long j = curTime4;
                    long j2 = curTime4;
                    int i4 = keycode2;
                    int i5 = metaState;
                    KeyEvent keyEvent7 = r1;
                    float xOff2 = xOff;
                    KeyEvent keyEvent8 = new KeyEvent(j, j2, 0, i4, 0, i5, -1, 0, 1024, 257);
                    ViewRootImpl.this.enqueueInputEvent(keyEvent7);
                    ViewRootImpl viewRootImpl2 = ViewRootImpl.this;
                    float yOff2 = yOff;
                    KeyEvent keyEvent9 = r1;
                    KeyEvent keyEvent10 = new KeyEvent(j, j2, 1, i4, 0, i5, -1, 0, 1024, 257);
                    viewRootImpl2.enqueueInputEvent(keyEvent9);
                    movement = movement3;
                    curTime2 = curTime4;
                    xOff = xOff2;
                    yOff = yOff2;
                }
                float f = yOff;
                this.mLastTime = curTime2;
                return;
            }
            float f2 = yOff;
            long j3 = curTime;
        }

        public void cancel() {
            this.mLastTime = -2147483648L;
            if (ViewRootImpl.this.mView != null && ViewRootImpl.this.mAdded) {
                ViewRootImpl.this.ensureTouchMode(false);
            }
        }
    }

    static final class SystemUiVisibilityInfo {
        int globalVisibility;
        int localChanges;
        int localValue;
        int seq;

        SystemUiVisibilityInfo() {
        }
    }

    class TakenSurfaceHolder extends BaseSurfaceHolder {
        TakenSurfaceHolder() {
        }

        public boolean onAllowLockCanvas() {
            return ViewRootImpl.this.mDrawingAllowed;
        }

        public void onRelayoutContainer() {
        }

        public void setFormat(int format) {
            ViewRootImpl.this.mView.setSurfaceFormat(format);
        }

        public void setType(int type) {
            ViewRootImpl.this.mView.setSurfaceType(type);
        }

        public void onUpdateSurface() {
            throw new IllegalStateException("Shouldn't be here");
        }

        public boolean isCreating() {
            return ViewRootImpl.this.mIsCreating;
        }

        public void setFixedSize(int width, int height) {
            throw new UnsupportedOperationException("Currently only support sizing from layout");
        }

        public void setKeepScreenOn(boolean screenOn) {
            ViewRootImpl.this.mView.setSurfaceKeepScreenOn(screenOn);
        }
    }

    static final class TrackballAxis {
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
            long normTime2 = normTime;
            if (normTime2 > 0) {
                long delta = time - this.lastMoveTime;
                this.lastMoveTime = time;
                float acc = this.acceleration;
                if (delta < normTime2) {
                    float scale = ((float) (normTime2 - delta)) * ACCEL_MOVE_SCALING_FACTOR;
                    if (scale > 1.0f) {
                        acc *= scale;
                    }
                    float f2 = MAX_ACCELERATION;
                    if (acc < MAX_ACCELERATION) {
                        f2 = acc;
                    }
                    this.acceleration = f2;
                } else {
                    float scale2 = ((float) (delta - normTime2)) * ACCEL_MOVE_SCALING_FACTOR;
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
                switch (this.step) {
                    case 0:
                        if (Math.abs(this.position) >= FIRST_MOVEMENT_THRESHOLD) {
                            movement += dir2;
                            this.nonAccelMovement += dir2;
                            this.step = 1;
                            break;
                        } else {
                            return movement;
                        }
                    case 1:
                        if (Math.abs(this.position) >= SECOND_CUMULATIVE_MOVEMENT_THRESHOLD) {
                            movement += dir2;
                            this.nonAccelMovement += dir2;
                            this.position -= SECOND_CUMULATIVE_MOVEMENT_THRESHOLD * ((float) dir2);
                            this.step = 2;
                            break;
                        } else {
                            return movement;
                        }
                    default:
                        if (Math.abs(this.position) >= 1.0f) {
                            movement += dir2;
                            this.position -= ((float) dir2) * 1.0f;
                            float acc = this.acceleration * 1.1f;
                            this.acceleration = acc < MAX_ACCELERATION ? acc : this.acceleration;
                            break;
                        } else {
                            return movement;
                        }
                }
            }
        }
    }

    final class TraversalRunnable implements Runnable {
        TraversalRunnable() {
        }

        public void run() {
            boolean viewScrollChanged = ViewRootImpl.this.mAttachInfo.mViewScrollChanged;
            if (ViewRootImpl.mDebugUnlockKeyguard && ViewRootImpl.DEBUG_HWFLOW && ViewRootImpl.this.mTag.contains("StatusBar")) {
                long mKeyguardViewTime = System.currentTimeMillis() - ViewRootImpl.sDebugKeyguardViewDrawTime;
                if (mKeyguardViewTime > 16) {
                    String access$1700 = ViewRootImpl.this.mTag;
                    Slog.d(access$1700, "unlock keyguard view draw time " + mKeyguardViewTime);
                }
                long unused = ViewRootImpl.sDebugKeyguardViewDrawTime = 0;
            }
            ViewRootImpl.this.doTraversal();
            boolean not_care_window = false;
            if (ViewRootImpl.sIsFirstFrame) {
                ViewRootImpl.setIsFirstFrame(false);
                String pkg = ViewRootImpl.this.mContext == null ? "Unknown" : ViewRootImpl.this.mContext.getPackageName();
                Jlog.d(337, ViewRootImpl.this.mWindowAttributes.getTitle() + "", "");
                if (Jlog.isPerfTest()) {
                    Jlog.i(JlogConstants.JLID_ACTIVITY_DISPLAY, Jlog.getMessage(ViewRootImpl.TAG, "TraversalRunnable.run", "pid=" + Process.myPid() + "&pkg=" + pkg));
                }
            }
            int windowtype = ViewRootImpl.this.mWindowAttributes.type;
            if (!(windowtype <= 1999 || windowtype == 2004 || windowtype == 2013 || windowtype == 2011)) {
                not_care_window = true;
            }
            if (!not_care_window && ViewRootImpl.this.mChoreographer.isNeedDraw) {
                if (ViewRootImpl.sLastRelayoutNotifyTime != 0 || Math.abs(System.currentTimeMillis() - ViewRootImpl.sLastRelayoutNotifyTime) > ViewRootImpl.sRelayoutNotifyPeriod) {
                    ViewRootImpl.dispatchWindowLayoutChange();
                    ViewRootImpl.sLastRelayoutNotifyTime = System.currentTimeMillis();
                }
                ViewRootImpl.this.jank_processAfterTraversal(viewScrollChanged);
            }
        }
    }

    private static class UnhandledKeyManager {
        private final SparseArray<WeakReference<View>> mCapturedKeys;
        private WeakReference<View> mCurrentReceiver;
        private boolean mDispatched;

        private UnhandledKeyManager() {
            this.mDispatched = true;
            this.mCapturedKeys = new SparseArray<>();
            this.mCurrentReceiver = null;
        }

        /* JADX INFO: finally extract failed */
        /* access modifiers changed from: package-private */
        public boolean dispatch(View root, KeyEvent event) {
            if (this.mDispatched) {
                return false;
            }
            try {
                Trace.traceBegin(8, "UnhandledKeyEvent dispatch");
                boolean z = true;
                this.mDispatched = true;
                View consumer = root.dispatchUnhandledKeyEvent(event);
                if (event.getAction() == 0) {
                    int keycode = event.getKeyCode();
                    if (consumer != null && !KeyEvent.isModifierKey(keycode)) {
                        this.mCapturedKeys.put(keycode, new WeakReference(consumer));
                    }
                }
                Trace.traceEnd(8);
                if (consumer == null) {
                    z = false;
                }
                return z;
            } catch (Throwable th) {
                Trace.traceEnd(8);
                throw th;
            }
        }

        /* access modifiers changed from: package-private */
        public void preDispatch(KeyEvent event) {
            this.mCurrentReceiver = null;
            if (event.getAction() == 1) {
                int idx = this.mCapturedKeys.indexOfKey(event.getKeyCode());
                if (idx >= 0) {
                    this.mCurrentReceiver = this.mCapturedKeys.valueAt(idx);
                    this.mCapturedKeys.removeAt(idx);
                }
            }
        }

        /* access modifiers changed from: package-private */
        public boolean preViewDispatch(KeyEvent event) {
            this.mDispatched = false;
            if (this.mCurrentReceiver == null) {
                this.mCurrentReceiver = this.mCapturedKeys.get(event.getKeyCode());
            }
            if (this.mCurrentReceiver == null) {
                return false;
            }
            View target = (View) this.mCurrentReceiver.get();
            if (event.getAction() == 1) {
                this.mCurrentReceiver = null;
            }
            if (target != null && target.isAttachedToWindow()) {
                target.onUnhandledKeyEvent(event);
            }
            return true;
        }
    }

    @RCUnownedThisRef
    final class ViewPostImeInputStage extends InputStage {
        public ViewPostImeInputStage(InputStage next) {
            super(next);
        }

        /* access modifiers changed from: protected */
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
        public void onDeliverToNext(QueuedInputEvent q) {
            if (ViewRootImpl.this.mUnbufferedInputDispatch && (q.mEvent instanceof MotionEvent) && ((MotionEvent) q.mEvent).isTouchEvent() && ViewRootImpl.isTerminalInputEvent(q.mEvent)) {
                ViewRootImpl.this.mUnbufferedInputDispatch = false;
                ViewRootImpl.this.scheduleConsumeBatchedInput();
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
            if (direction != 0) {
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
                } else if (ViewRootImpl.this.mView.restoreDefaultFocus()) {
                    return true;
                }
            }
            return false;
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
            if (event.getAction() == 0) {
                if (groupNavigationDirection != 0) {
                    if (performKeyboardGroupNavigation(groupNavigationDirection)) {
                        return 1;
                    }
                } else if (performFocusNavigation(event)) {
                    return 1;
                }
            }
            return 0;
        }

        private int processPointerEvent(QueuedInputEvent q) {
            MotionEvent event = (MotionEvent) q.mEvent;
            if (Jlog.isMicroTest()) {
                Jlog.i(JlogConstants.JL_VIEW_PROCESS_BEGIN, Jlog.getMessage(ViewRootImpl.TAG, "processPointerEvent", String.valueOf(event.getSequenceNumber()) + "&oper=" + event.getActionMasked()));
            }
            int i = 0;
            ViewRootImpl.this.mAttachInfo.mUnbufferedDispatchRequested = false;
            ViewRootImpl.this.mAttachInfo.mHandlingPointerEvent = true;
            int action = event.getAction();
            if (HwFrameworkFactory.getHwViewRootImpl().filterDecorPointerEvent(ViewRootImpl.this.mContext, event, action, ViewRootImpl.this.mWindowAttributes, ViewRootImpl.this.mDisplay)) {
                if (ViewRootImpl.DEBUG_HWFLOW) {
                    Log.i(ViewRootImpl.this.mTag, "Event consumed by filterDecor!");
                }
                return 1;
            }
            if (action == 1) {
                MotionEvent mv = HwFrameworkFactory.getHwViewRootImpl().getRedispatchEvent();
                if (mv != null) {
                    boolean handledRedispatch = ViewRootImpl.this.mView.dispatchPointerEvent(mv);
                    if (ViewRootImpl.DEBUG_HWFLOW) {
                        Log.i(ViewRootImpl.this.mTag, "reDispatchEvent:" + handledRedispatch);
                    }
                    mv.recycle();
                }
            }
            boolean handled = ViewRootImpl.this.mDisplay != null && ViewRootImpl.this.isValidExtDisplayId(ViewRootImpl.this.mDisplay.getDisplayId()) && HwFrameworkFactory.getHwViewRootImpl().interceptMotionEvent(ViewRootImpl.this.mView, event);
            if (!handled && ViewRootImpl.this.mView != null) {
                handled = ViewRootImpl.this.mView.dispatchPointerEvent(event);
            }
            maybeUpdatePointerIcon(event);
            ViewRootImpl.this.maybeUpdateTooltip(event);
            ViewRootImpl.this.mAttachInfo.mHandlingPointerEvent = false;
            if (ViewRootImpl.this.mAttachInfo.mUnbufferedDispatchRequested && !ViewRootImpl.this.mUnbufferedInputDispatch) {
                ViewRootImpl.this.mUnbufferedInputDispatch = true;
                if (ViewRootImpl.this.mConsumeBatchedInputScheduled) {
                    ViewRootImpl.this.scheduleConsumeBatchedInputImmediately();
                }
            }
            if (Jlog.isMicroTest()) {
                Jlog.i(JlogConstants.JL_VIEW_PROCESS_END, Jlog.getMessage(ViewRootImpl.TAG, "processPointerEvent", String.valueOf(event.getSequenceNumber()) + "&oper=" + event.getActionMasked()));
            }
            if (handled) {
                i = 1;
            }
            return i;
        }

        private void maybeUpdatePointerIcon(MotionEvent event) {
            if (event.getPointerCount() == 1 && event.isFromSource(InputDevice.SOURCE_MOUSE)) {
                if (event.getActionMasked() == 9 || event.getActionMasked() == 10) {
                    int unused = ViewRootImpl.this.mPointerIconType = 1;
                }
                if (event.getActionMasked() != 10 && !ViewRootImpl.this.updatePointerIcon(event) && event.getActionMasked() == 7) {
                    int unused2 = ViewRootImpl.this.mPointerIconType = 1;
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
            if (ViewRootImpl.this.mView.dispatchGenericMotionEvent((MotionEvent) q.mEvent)) {
                return 1;
            }
            return 0;
        }
    }

    @RCUnownedThisRef
    final class ViewPreImeInputStage extends InputStage {
        public ViewPreImeInputStage(InputStage next) {
            super(next);
        }

        /* access modifiers changed from: protected */
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

    final class ViewRootHandler extends Handler {
        ViewRootHandler() {
        }

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
                default:
                    return super.getMessageName(message);
            }
        }

        public boolean sendMessageAtTime(Message msg, long uptimeMillis) {
            if (msg.what != 26 || msg.obj != null) {
                return super.sendMessageAtTime(msg, uptimeMillis);
            }
            throw new NullPointerException("Attempted to call MSG_REQUEST_KEYBOARD_SHORTCUTS with null receiver:");
        }

        public void handleMessage(Message msg) {
            int i = -1;
            boolean suppress = false;
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
                    if (ViewRootImpl.this.mWindowAttributes != null && ViewRootImpl.this.mWindowAttributes.getTitle().toString().contains("Emui:ProximityWnd")) {
                        Log.d(ViewRootImpl.TAG, "MSG_DIE received");
                    }
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
                    if (msg.arg1 != 0) {
                        suppress = true;
                    }
                    viewRootImpl.handleAppVisibility(suppress);
                    return;
                case 9:
                    ViewRootImpl.this.handleGetNewSurface();
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
                    InputMethodManager imm = InputMethodManager.peekInstance();
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
                    ViewRootImpl.this.performConfigurationChange(ViewRootImpl.this.mPendingMergedConfiguration, false, -1);
                    return;
                case 19:
                    ViewRootImpl.this.mProcessInputEventsScheduled = false;
                    ViewRootImpl.this.doProcessInputEvents();
                    return;
                case 21:
                    ViewRootImpl.this.setAccessibilityFocus(null, null);
                    return;
                case 22:
                    if (ViewRootImpl.this.mView != null) {
                        ViewRootImpl.this.invalidateWorld(ViewRootImpl.this.mView);
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
                        ViewRootImpl.this.mWinFrame.left = l;
                        ViewRootImpl.this.mWinFrame.right = l + w;
                        ViewRootImpl.this.mWinFrame.top = t;
                        ViewRootImpl.this.mWinFrame.bottom = t + h;
                        ViewRootImpl.this.mPendingBackDropFrame.set(ViewRootImpl.this.mWinFrame);
                        boolean isDockedDivider = ViewRootImpl.this.mWindowAttributes.type == 2034;
                        if ((ViewRootImpl.this.mDragResizing && ViewRootImpl.this.mResizeMode == 1) || isDockedDivider) {
                            suppress = true;
                        }
                        if (!suppress) {
                            if (ViewRootImpl.this.mView != null) {
                                ViewRootImpl.forceLayout(ViewRootImpl.this.mView);
                            }
                            ViewRootImpl.this.requestLayout();
                            return;
                        }
                        ViewRootImpl.this.maybeHandleWindowMove(ViewRootImpl.this.mWinFrame);
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
                    if (msg.arg1 != 0) {
                        suppress = true;
                    }
                    ViewRootImpl.this.handlePointerCaptureChanged(suppress);
                    return;
                case 29:
                    ViewRootImpl.this.pendingDrawFinished();
                    return;
                default:
                    return;
            }
            HwFrameworkFactory.getHwViewRootImpl().clearDisplayPoint();
            if (ViewRootImpl.this.mAdded) {
                SomeArgs args3 = (SomeArgs) msg.obj;
                int displayId = args3.argi3;
                MergedConfiguration mergedConfiguration = (MergedConfiguration) args3.arg4;
                if (ViewRootImpl.this.mDisplay != null) {
                    boolean displayChanged = ViewRootImpl.this.mDisplay.getDisplayId() != displayId;
                    if (!ViewRootImpl.this.mLastReportedMergedConfiguration.equals(mergedConfiguration)) {
                        ViewRootImpl viewRootImpl2 = ViewRootImpl.this;
                        if (displayChanged) {
                            i = displayId;
                        }
                        viewRootImpl2.performConfigurationChange(mergedConfiguration, false, i);
                    } else if (displayChanged) {
                        ViewRootImpl.this.onMovedToDisplay(displayId, ViewRootImpl.this.mLastConfigurationFromResources);
                    }
                    boolean framesChanged = !ViewRootImpl.this.mWinFrame.equals(args3.arg1) || !ViewRootImpl.this.mPendingOverscanInsets.equals(args3.arg5) || !ViewRootImpl.this.mPendingContentInsets.equals(args3.arg2) || !ViewRootImpl.this.mPendingStableInsets.equals(args3.arg6) || !ViewRootImpl.this.mPendingDisplayCutout.get().equals(args3.arg9) || !ViewRootImpl.this.mPendingVisibleInsets.equals(args3.arg3) || !ViewRootImpl.this.mPendingOutsets.equals(args3.arg7);
                    ViewRootImpl.this.mWinFrame.set((Rect) args3.arg1);
                    ViewRootImpl.this.mPendingOverscanInsets.set((Rect) args3.arg5);
                    ViewRootImpl.this.mPendingContentInsets.set((Rect) args3.arg2);
                    ViewRootImpl.this.mPendingStableInsets.set((Rect) args3.arg6);
                    ViewRootImpl.this.mPendingDisplayCutout.set((DisplayCutout) args3.arg9);
                    ViewRootImpl.this.mPendingVisibleInsets.set((Rect) args3.arg3);
                    ViewRootImpl.this.mPendingOutsets.set((Rect) args3.arg7);
                    ViewRootImpl.this.mPendingBackDropFrame.set((Rect) args3.arg8);
                    ViewRootImpl.this.mForceNextWindowRelayout = args3.argi1 != 0;
                    ViewRootImpl viewRootImpl3 = ViewRootImpl.this;
                    if (args3.argi2 != 0) {
                        suppress = true;
                    }
                    viewRootImpl3.mPendingAlwaysConsumeNavBar = suppress;
                    args3.recycle();
                    if (msg.what == 5) {
                        ViewRootImpl.this.reportNextDraw();
                    }
                    if (ViewRootImpl.this.mView != null && framesChanged) {
                        ViewRootImpl.forceLayout(ViewRootImpl.this.mView);
                    }
                    ViewRootImpl.this.requestLayout();
                }
            }
        }
    }

    static class W extends IWindow.Stub {
        private final WeakReference<ViewRootImpl> mViewAncestor;
        private final IWindowSession mWindowSession;

        W(ViewRootImpl viewAncestor) {
            this.mViewAncestor = new WeakReference<>(viewAncestor);
            this.mWindowSession = viewAncestor.mWindowSession;
        }

        public void resized(Rect frame, Rect overscanInsets, Rect contentInsets, Rect visibleInsets, Rect stableInsets, Rect outsets, boolean reportDraw, MergedConfiguration mergedConfiguration, Rect backDropFrame, boolean forceLayout, boolean alwaysConsumeNavBar, int displayId, DisplayCutout.ParcelableWrapper displayCutout) {
            ViewRootImpl viewAncestor = (ViewRootImpl) this.mViewAncestor.get();
            if (viewAncestor != null) {
                viewAncestor.dispatchResized(frame, overscanInsets, contentInsets, visibleInsets, stableInsets, outsets, reportDraw, mergedConfiguration, backDropFrame, forceLayout, alwaysConsumeNavBar, displayId, displayCutout);
            }
        }

        public void moved(int newX, int newY) {
            ViewRootImpl viewAncestor = (ViewRootImpl) this.mViewAncestor.get();
            if (viewAncestor != null) {
                viewAncestor.dispatchMoved(newX, newY);
            }
        }

        public void dispatchAppVisibility(boolean visible) {
            ViewRootImpl viewAncestor = (ViewRootImpl) this.mViewAncestor.get();
            if (viewAncestor != null) {
                viewAncestor.dispatchAppVisibility(visible);
            }
        }

        public void dispatchGetNewSurface() {
            ViewRootImpl viewAncestor = (ViewRootImpl) this.mViewAncestor.get();
            if (viewAncestor != null) {
                viewAncestor.dispatchGetNewSurface();
            }
        }

        public void updateSurfaceStatus(boolean status) {
            ViewRootImpl viewAncestor = (ViewRootImpl) this.mViewAncestor.get();
            if (viewAncestor != null) {
                viewAncestor.updateSurfaceStatus(status);
            }
        }

        public void windowFocusChanged(boolean hasFocus, boolean inTouchMode) {
            ViewRootImpl viewAncestor = (ViewRootImpl) this.mViewAncestor.get();
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

        public void executeCommand(String command, String parameters, ParcelFileDescriptor out) {
            ViewRootImpl viewAncestor = (ViewRootImpl) this.mViewAncestor.get();
            if (viewAncestor != null) {
                View view = viewAncestor.mView;
                if (view == null) {
                    return;
                }
                if (checkCallingPermission("android.permission.DUMP") == 0) {
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

        public void closeSystemDialogs(String reason) {
            ViewRootImpl viewAncestor = (ViewRootImpl) this.mViewAncestor.get();
            if (viewAncestor != null) {
                viewAncestor.dispatchCloseSystemDialogs(reason);
            }
        }

        public void dispatchWallpaperOffsets(float x, float y, float xStep, float yStep, boolean sync) {
            if (sync) {
                try {
                    this.mWindowSession.wallpaperOffsetsComplete(asBinder());
                } catch (RemoteException e) {
                }
            }
        }

        public void dispatchWallpaperCommand(String action, int x, int y, int z, Bundle extras, boolean sync) {
            if (sync) {
                try {
                    this.mWindowSession.wallpaperCommandComplete(asBinder(), null);
                } catch (RemoteException e) {
                }
            }
        }

        public void dispatchDragEvent(DragEvent event) {
            ViewRootImpl viewAncestor = (ViewRootImpl) this.mViewAncestor.get();
            if (viewAncestor != null) {
                viewAncestor.dispatchDragEvent(event);
            }
        }

        public void updatePointerIcon(float x, float y) {
            ViewRootImpl viewAncestor = (ViewRootImpl) this.mViewAncestor.get();
            if (viewAncestor != null) {
                viewAncestor.updatePointerIcon(x, y);
            }
        }

        public void dispatchSystemUiVisibilityChanged(int seq, int globalVisibility, int localValue, int localChanges) {
            ViewRootImpl viewAncestor = (ViewRootImpl) this.mViewAncestor.get();
            if (viewAncestor != null) {
                viewAncestor.dispatchSystemUiVisibilityChanged(seq, globalVisibility, localValue, localChanges);
            }
        }

        public void dispatchWindowShown() {
            ViewRootImpl viewAncestor = (ViewRootImpl) this.mViewAncestor.get();
            if (viewAncestor != null) {
                viewAncestor.dispatchWindowShown();
            }
        }

        public void requestAppKeyboardShortcuts(IResultReceiver receiver, int deviceId) {
            ViewRootImpl viewAncestor = (ViewRootImpl) this.mViewAncestor.get();
            if (viewAncestor != null) {
                viewAncestor.dispatchRequestKeyboardShortcuts(receiver, deviceId);
            }
        }

        public void dispatchPointerCaptureChanged(boolean hasCapture) {
            ViewRootImpl viewAncestor = (ViewRootImpl) this.mViewAncestor.get();
            if (viewAncestor != null) {
                viewAncestor.dispatchPointerCaptureChanged(hasCapture);
            }
        }

        public void registerWindowObserver(IWindowLayoutObserver observer, long period) {
            if (period <= 0) {
                Slog.e(ViewRootImpl.TAG, "registerWindowObserver with wrong period " + period);
                return;
            }
            ViewRootImpl.sRelayoutNotifyPeriod = period;
            if (ViewRootImpl.sRelayoutNotifyPeriod < 500) {
                ViewRootImpl.sRelayoutNotifyPeriod = 500;
            }
            ViewRootImpl.sLastRelayoutNotifyTime = 0;
            synchronized (ViewRootImpl.sWindowLayoutObservers) {
                ViewRootImpl.sWindowLayoutObservers.register(observer);
            }
            ViewRootImpl.dispatchWindowLayoutChange();
            ViewRootImpl.sLastRelayoutNotifyTime = System.currentTimeMillis();
        }

        public void unRegisterWindowObserver(IWindowLayoutObserver observer) {
            synchronized (ViewRootImpl.sWindowLayoutObservers) {
                ViewRootImpl.sWindowLayoutObservers.unregister(observer);
            }
        }

        public void notifyFocusChanged() {
            if (ActivityThread.getContentSensorManager() == null) {
                ActivityThread.setContentSensorManager(ContentSensorManagerFactory.createContentSensorManager(0, null));
            }
            IContentSensorManager csm = ActivityThread.getContentSensorManager();
            ViewRootImpl viewAncestor = (ViewRootImpl) this.mViewAncestor.get();
            if (viewAncestor != null && csm != null) {
                csm.notifyFocusChanged(viewAncestor.mView);
            }
        }
    }

    final class WindowInputEventReceiver extends InputEventReceiver {
        public WindowInputEventReceiver(InputChannel inputChannel, Looper looper) {
            super(inputChannel, looper);
        }

        public void onInputEvent(InputEvent event, int displayId) {
            if (Jlog.isMicroTest()) {
                Jlog.i(JlogConstants.JL_VIEW_EVENT_BEGIN, Jlog.getMessage(ViewRootImpl.TAG, "onInputEvent", String.valueOf(event.getSequenceNumber())));
            }
            boolean isMove = false;
            if (ViewRootImpl.DEBUG_HWFLOW) {
                isMove = ViewRootImpl.this.printEventIfNeed(event);
            }
            if (HwFrameworkFactory.getHwViewRootImpl().shouldQueueInputEvent(event, ViewRootImpl.this.mContext, ViewRootImpl.this.mView, ViewRootImpl.this.mWindowAttributes)) {
                ViewRootImpl.this.enqueueInputEvent(event, this, 0, true);
            } else {
                if (ViewRootImpl.DEBUG_HWFLOW && (!isMove || ViewRootImpl.DEBUG_MOVING)) {
                    Log.i(ViewRootImpl.this.mTag, "Event not queue and finish!");
                }
                finishInputEvent(event, false);
            }
            if (ViewRootImpl.IS_USER_DOMESTIC_BETA) {
                ViewRootImpl.this.sendKeyEventToPG(event);
            }
            if (Jlog.isMicroTest()) {
                Jlog.i(JlogConstants.JL_VIEW_EVENT_END, Jlog.getMessage(ViewRootImpl.TAG, "onInputEvent", String.valueOf(event.getSequenceNumber())));
            }
        }

        public void onBatchedInputEventPending() {
            if (ViewRootImpl.this.mUnbufferedInputDispatch) {
                super.onBatchedInputEventPending();
            } else {
                ViewRootImpl.this.scheduleConsumeBatchedInput();
            }
        }

        public void dispose() {
            ViewRootImpl.this.unscheduleConsumeBatchedInput();
            super.dispose();
        }
    }

    interface WindowStoppedCallback {
        void windowStopped(boolean z);
    }

    static {
        boolean z = true;
        if (SystemProperties.getInt("ro.logsystem.usertype", 1) != 3) {
            z = false;
        }
        IS_USER_DOMESTIC_BETA = z;
    }

    public static void setDebugUnlockKeyguard(boolean flag) {
        mDebugUnlockKeyguard = flag;
    }

    public static synchronized void setIsFirstFrame(boolean isFirstFrame) {
        synchronized (ViewRootImpl.class) {
            sIsFirstFrame = isFirstFrame;
        }
    }

    public ViewRootImpl(Context context, Display display) {
        boolean z = false;
        this.mInputEventConsistencyVerifier = InputEventConsistencyVerifier.isInstrumentationEnabled() ? new InputEventConsistencyVerifier(this, 0) : null;
        this.mTag = TAG;
        this.mProfile = false;
        this.mDisplayListener = new DisplayManager.DisplayListener() {
            @RCUnownedThisRef
            public void onDisplayChanged(int displayId) {
                if (ViewRootImpl.this.mView != null && ViewRootImpl.this.mDisplay != null && ViewRootImpl.this.mDisplay.getDisplayId() == displayId) {
                    int oldDisplayState = ViewRootImpl.this.mAttachInfo.mDisplayState;
                    int newDisplayState = ViewRootImpl.this.mDisplay.getState();
                    if (oldDisplayState != newDisplayState) {
                        ViewRootImpl.this.mAttachInfo.mDisplayState = newDisplayState;
                        ViewRootImpl.this.pokeDrawLockIfNeeded();
                        if (oldDisplayState != 0) {
                            int oldScreenState = toViewScreenState(oldDisplayState);
                            int newScreenState = toViewScreenState(newDisplayState);
                            if (oldScreenState != newScreenState) {
                                ViewRootImpl.this.mView.dispatchScreenStateChanged(newScreenState);
                            }
                            if (oldDisplayState == 1) {
                                ViewRootImpl.this.mFullRedrawNeeded = true;
                                ViewRootImpl.this.scheduleTraversals();
                            }
                        }
                    }
                }
            }

            public void onDisplayRemoved(int displayId) {
            }

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
        this.lastFrameDefer = 0;
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
        View.AttachInfo attachInfo = new View.AttachInfo(this.mWindowSession, this.mWindow, display, this, this.mHandler, this, context);
        this.mAttachInfo = attachInfo;
        this.mAccessibilityManager = AccessibilityManager.getInstance(context);
        this.mAccessibilityManager.addAccessibilityStateChangeListener(this.mAccessibilityInteractionConnectionManager, this.mHandler);
        this.mHighContrastTextManager = new HighContrastTextManager();
        this.mAccessibilityManager.addHighTextContrastStateChangeListener(this.mHighContrastTextManager, this.mHandler);
        this.mViewConfiguration = ViewConfiguration.get(context);
        this.mDensity = context.getResources().getDisplayMetrics().densityDpi;
        this.mNoncompatDensity = context.getResources().getDisplayMetrics().noncompatDensityDpi;
        this.mFallbackEventHandler = new PhoneFallbackEventHandler(context);
        this.mChoreographer = Choreographer.getInstance();
        this.mDisplayManager = (DisplayManager) context.getSystemService("display");
        if (!sCompatibilityDone) {
            sAlwaysAssignFocus = this.mTargetSdkVersion < 28;
            sCompatibilityDone = true;
        }
        loadSystemProperties();
        this.mPkgName = context.getPackageName();
        Point pt = new Point();
        this.mDisplay.getRealSize(pt);
        HwFrameworkFactory.getHwViewRootImpl().setRealSize(pt);
        HwFrameworkFactory.getHwAppInnerBoostImpl().initialize(this.mPkgName);
        this.mIsDropEmptyFrame = SystemProperties.getInt(APS_DROP_EMPTY_FRAME, 1) != 0 ? true : z;
        this.mVrMananger = HwFrameworkFactory.getVRSystemServiceManager();
    }

    public static void addFirstDrawHandler(Runnable callback) {
        synchronized (sFirstDrawHandlers) {
            if (!sFirstDrawComplete) {
                sFirstDrawHandlers.add(callback);
            }
        }
    }

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
        if (this.mWindowDrawCountDown != null) {
            this.mWindowDrawCountDown.countDown();
        }
    }

    public void profile() {
        this.mProfile = true;
    }

    static boolean isInTouchMode() {
        IWindowSession windowSession = WindowManagerGlobal.peekWindowSession();
        if (windowSession != null) {
            try {
                return windowSession.getInTouchMode();
            } catch (RemoteException e) {
            }
        }
        return false;
    }

    public void notifyChildRebuilt() {
        if (this.mView instanceof RootViewSurfaceTaker) {
            if (this.mSurfaceHolderCallback != null) {
                this.mSurfaceHolder.removeCallback(this.mSurfaceHolderCallback);
            }
            this.mSurfaceHolderCallback = this.mView.willYouTakeTheSurface();
            if (this.mSurfaceHolderCallback != null) {
                this.mSurfaceHolder = new TakenSurfaceHolder();
                this.mSurfaceHolder.setFormat(0);
                this.mSurfaceHolder.addCallback(this.mSurfaceHolderCallback);
            } else {
                this.mSurfaceHolder = null;
            }
            this.mInputQueueCallback = this.mView.willYouTakeTheInputQueue();
            if (this.mInputQueueCallback != null) {
                this.mInputQueueCallback.onInputQueueCreated(this.mInputQueue);
            }
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:133:0x03fc, code lost:
        return;
     */
    /* JADX WARNING: Removed duplicated region for block: B:128:0x03f5 A[SYNTHETIC, Splitter:B:128:0x03f5] */
    public void setView(View view, WindowManager.LayoutParams attrs, View panelParentView) {
        View view2 = view;
        synchronized (this) {
            try {
                if (this.mView == null) {
                    this.mView = view2;
                    this.mAttachInfo.mDisplayState = this.mDisplay.getState();
                    this.mDisplayManager.registerDisplayListener(this.mDisplayListener, this.mHandler);
                    this.mViewLayoutDirectionInitial = this.mView.getRawLayoutDirection();
                    this.mFallbackEventHandler.setView(view2);
                    this.mWindowAttributes.copyFrom(attrs);
                    if (this.mWindowAttributes.packageName == null) {
                        this.mWindowAttributes.packageName = this.mBasePackageName;
                    }
                    WindowManager.LayoutParams attrs2 = this.mWindowAttributes;
                    setTag();
                    this.mClientWindowLayoutFlags = attrs2.flags;
                    setAccessibilityFocus(null, null);
                    if (view2 instanceof RootViewSurfaceTaker) {
                        this.mSurfaceHolderCallback = ((RootViewSurfaceTaker) view2).willYouTakeTheSurface();
                        if (this.mSurfaceHolderCallback != null) {
                            this.mSurfaceHolder = new TakenSurfaceHolder();
                            this.mSurfaceHolder.setFormat(0);
                            this.mSurfaceHolder.addCallback(this.mSurfaceHolderCallback);
                        }
                    }
                    if (!attrs2.hasManualSurfaceInsets) {
                        attrs2.setSurfaceInsets(view2, false, true);
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
                    boolean restore = false;
                    if (this.mTranslator != null) {
                        this.mSurface.setCompatibilityTranslator(this.mTranslator);
                        restore = true;
                        attrs2.backup();
                        this.mTranslator.translateWindowLayout(attrs2);
                    }
                    boolean restore2 = restore;
                    if (!compatibilityInfo.supportsScreen()) {
                        attrs2.privateFlags |= 128;
                        this.mLastInCompatMode = true;
                    }
                    this.mSoftInputMode = attrs2.softInputMode;
                    this.mWindowAttributesChanged = true;
                    this.mWindowAttributesChangesFlag = -1;
                    this.mAttachInfo.mRootView = view2;
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
                        CompatibilityInfo compatibilityInfo2 = compatibilityInfo;
                        try {
                            int res = this.mWindowSession.addToDisplay(this.mWindow, this.mSeq, this.mWindowAttributes, getHostVisibility(), this.mDisplay.getDisplayId(), this.mWinFrame, this.mAttachInfo.mContentInsets, this.mAttachInfo.mStableInsets, this.mAttachInfo.mOutsets, this.mAttachInfo.mDisplayCutout, this.mInputChannel);
                            if (restore2) {
                                attrs2.restore();
                            }
                            if (this.mTranslator != null) {
                                this.mTranslator.translateRectInScreenToAppWindow(this.mAttachInfo.mContentInsets);
                            }
                            this.mPendingOverscanInsets.set(0, 0, 0, 0);
                            this.mPendingContentInsets.set(this.mAttachInfo.mContentInsets);
                            this.mPendingStableInsets.set(this.mAttachInfo.mStableInsets);
                            this.mPendingDisplayCutout.set(this.mAttachInfo.mDisplayCutout);
                            this.mPendingVisibleInsets.set(0, 0, 0, 0);
                            this.mAttachInfo.mAlwaysConsumeNavBar = (res & 4) != 0;
                            this.mPendingAlwaysConsumeNavBar = this.mAttachInfo.mAlwaysConsumeNavBar;
                            if (res < 0) {
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
                                        throw new WindowManager.BadTokenException("Unable to add window -- token " + attrs2.token + " for displayid = " + this.mDisplay.getDisplayId() + " is not valid; is your activity running?");
                                    default:
                                        throw new RuntimeException("Unable to add window -- unknown error code " + res);
                                }
                            } else {
                                if (view2 instanceof RootViewSurfaceTaker) {
                                    this.mInputQueueCallback = ((RootViewSurfaceTaker) view2).willYouTakeTheInputQueue();
                                }
                                if (this.mInputChannel != null) {
                                    if (this.mInputQueueCallback != null) {
                                        this.mInputQueue = new InputQueue();
                                        this.mInputQueueCallback.onInputQueueCreated(this.mInputQueue);
                                    }
                                    this.mInputEventReceiver = new WindowInputEventReceiver(this.mInputChannel, Looper.myLooper());
                                }
                                view2.assignParent(this);
                                this.mAddedTouchMode = (res & 1) != 0;
                                this.mAppVisible = (res & 2) != 0;
                                if (this.mAccessibilityManager.isEnabled()) {
                                    this.mAccessibilityInteractionConnectionManager.ensureConnection();
                                }
                                if (view.getImportantForAccessibility() == 0) {
                                    view2.setImportantForAccessibility(1);
                                }
                                CharSequence counterSuffix = attrs2.getTitle();
                                this.mSyntheticInputStage = new SyntheticInputStage();
                                InputStage earlyPostImeStage = new EarlyPostImeInputStage(new NativePostImeInputStage(new ViewPostImeInputStage(this.mSyntheticInputStage), "aq:native-post-ime:" + counterSuffix));
                                this.mFirstInputStage = new NativePreImeInputStage(new ViewPreImeInputStage(new ImeInputStage(earlyPostImeStage, "aq:ime:" + counterSuffix)), "aq:native-pre-ime:" + counterSuffix);
                                this.mFirstPostImeInputStage = earlyPostImeStage;
                                this.mPendingInputEventQueueLengthCounterName = "aq:pending:" + counterSuffix;
                            }
                        } catch (RemoteException e) {
                            e = e;
                            try {
                                this.mAdded = false;
                                this.mView = null;
                                this.mAttachInfo.mRootView = null;
                                this.mInputChannel = null;
                                this.mFallbackEventHandler.setView(null);
                                unscheduleTraversals();
                                setAccessibilityFocus(null, null);
                                throw new RuntimeException("Adding window failed", e);
                            } catch (Throwable th) {
                                e = th;
                                if (restore2) {
                                    attrs2.restore();
                                }
                                throw e;
                            }
                        }
                    } catch (RemoteException e2) {
                        e = e2;
                        CompatibilityInfo compatibilityInfo3 = compatibilityInfo;
                        this.mAdded = false;
                        this.mView = null;
                        this.mAttachInfo.mRootView = null;
                        this.mInputChannel = null;
                        this.mFallbackEventHandler.setView(null);
                        unscheduleTraversals();
                        setAccessibilityFocus(null, null);
                        throw new RuntimeException("Adding window failed", e);
                    } catch (Throwable th2) {
                        e = th2;
                        CompatibilityInfo compatibilityInfo4 = compatibilityInfo;
                        if (restore2) {
                        }
                        throw e;
                    }
                }
            } catch (Throwable th3) {
                th = th3;
                throw th;
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
    public boolean isInLocalFocusMode() {
        return (this.mWindowAttributes.flags & 268435456) != 0;
    }

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
        if (this.mAttachInfo.mThreadedRenderer != null) {
            this.mAttachInfo.mThreadedRenderer.destroyHardwareResources(this.mView);
            this.mAttachInfo.mThreadedRenderer.destroy();
        }
    }

    public void detachFunctor(long functor) {
        if (this.mAttachInfo.mThreadedRenderer != null) {
            this.mAttachInfo.mThreadedRenderer.stopDrawing();
        }
    }

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

    public void registerVectorDrawableAnimator(AnimatedVectorDrawable.VectorDrawableAnimatorRT animator) {
        if (this.mAttachInfo.mThreadedRenderer != null) {
            this.mAttachInfo.mThreadedRenderer.registerVectorDrawableAnimator(animator);
        }
    }

    public void registerRtFrameCallback(ThreadedRenderer.FrameDrawingCallback callback) {
        this.mNextRtFrameCallback = callback;
    }

    private void enableHardwareAcceleration(WindowManager.LayoutParams attrs) {
        boolean wideGamut = false;
        this.mAttachInfo.mHardwareAccelerated = false;
        this.mAttachInfo.mHardwareAccelerationRequested = false;
        if (this.mTranslator == null) {
            if ((attrs.flags & 16777216) != 0) {
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
                        if (this.mAttachInfo.mThreadedRenderer != null) {
                            View.AttachInfo attachInfo = this.mAttachInfo;
                            this.mAttachInfo.mHardwareAccelerationRequested = true;
                            attachInfo.mHardwareAccelerated = true;
                        }
                    }
                } else {
                    return;
                }
            }
            if (this.mAttachInfo.mThreadedRenderer == null) {
                Jlog.d(JlogConstants.JLID_SOFTWARE_DRAW, "#ARG1:<" + attrs.getTitle().toString() + ">");
            }
        }
    }

    public View getView() {
        return this.mView;
    }

    /* access modifiers changed from: package-private */
    public final WindowLeaked getLocation() {
        return this.mLocation;
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
                this.mWindowAttributes.softInputMode = (this.mWindowAttributes.softInputMode & -241) | (oldSoftInputMode & 240);
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
            this.mDisplay = ResourcesManager.getInstance().getAdjustedDisplay(displayId, this.mView.getResources());
            if (this.mDisplay != null || !this.mVrMananger.isValidVRDisplayId(displayId)) {
                this.mAttachInfo.mDisplayState = this.mDisplay.getState();
                this.mView.dispatchMovedToDisplay(this.mDisplay, config);
                return;
            }
            Log.w(TAG, "onMovedToDisplay mDisplay is null...");
            this.mDisplay = ResourcesManager.getInstance().getAdjustedDisplay(0, this.mView.getResources());
        }
    }

    /* access modifiers changed from: package-private */
    public void pokeDrawLockIfNeeded() {
        int displayState = this.mAttachInfo.mDisplayState;
        if (this.mView != null && this.mAdded && this.mTraversalScheduled) {
            if (displayState == 3 || (!mSupportAod && displayState == 4)) {
                try {
                    this.mWindowSession.pokeDrawLock(this.mWindow);
                } catch (RemoteException e) {
                }
            }
        }
    }

    public void requestFitSystemWindows() {
        checkThread();
        this.mApplyInsetsRequested = true;
        scheduleTraversals();
    }

    public void requestLayout() {
        if (!this.mHandlingLayoutInLayoutRequest) {
            checkThread();
            this.mLayoutRequested = true;
            scheduleTraversals();
        }
    }

    public boolean isLayoutRequested() {
        return this.mLayoutRequested;
    }

    public void onDescendantInvalidated(View child, View descendant) {
        if (!this.mIsDropEmptyFrame || this.mBeingInvalidatedChild != descendant) {
            if ((descendant.mPrivateFlags & 64) != 0) {
                this.mIsAnimating = true;
            }
            invalidate();
        }
    }

    /* access modifiers changed from: package-private */
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

    public void invalidateChild(View child, Rect dirty) {
        invalidateChildInParent(null, dirty);
    }

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
                if (this.mCurScrollY != 0) {
                    dirty.offset(0, -this.mCurScrollY);
                }
                if (this.mTranslator != null) {
                    this.mTranslator.translateRectInAppWindowToScreen(dirty);
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
        if (!localDirty.isEmpty() && !localDirty.contains(dirty)) {
            this.mAttachInfo.mSetIgnoreDirtyState = true;
            this.mAttachInfo.mIgnoreDirtyState = true;
        }
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
                scheduleTraversals();
            } else if (renderer != null) {
                renderer.destroyHardwareResources(this.mView);
            }
            for (int i = 0; i < this.mWindowStoppedCallbacks.size(); i++) {
                this.mWindowStoppedCallbacks.get(i).windowStopped(stopped);
            }
            if (this.mStopped != 0) {
                Log.d(this.mTag, "surface should not be released");
            }
        }
    }

    public void setPausedForTransition(boolean paused) {
        this.mPausedForTransition = paused;
    }

    public ViewParent getParent() {
        return null;
    }

    public boolean getChildVisibleRect(View child, Rect r, Point offset) {
        if (child == this.mView) {
            return r.intersect(0, 0, this.mWidth, this.mHeight);
        }
        throw new RuntimeException("child is not mine, honest!");
    }

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
        if (this.mPendingTransitions == null || !this.mPendingTransitions.contains(transition)) {
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
    public void scheduleTraversals() {
        if (!this.mTraversalScheduled) {
            this.mTraversalScheduled = true;
            this.mTraversalBarrier = this.mHandler.getLooper().getQueue().postSyncBarrier();
            if (mDebugUnlockKeyguard && DEBUG_HWFLOW && this.mTag.contains("StatusBar")) {
                sDebugKeyguardViewDrawTime = System.currentTimeMillis();
            }
            this.mChoreographer.postCallback(2, this.mTraversalRunnable, null);
            if (!this.mUnbufferedInputDispatch) {
                scheduleConsumeBatchedInput();
            }
            notifyRendererOfFramePending();
            pokeDrawLockIfNeeded();
            HwFrameworkFactory.getHwAppInnerBoostImpl().onTraversal();
        }
    }

    /* access modifiers changed from: package-private */
    public void unscheduleTraversals() {
        if (this.mTraversalScheduled) {
            this.mTraversalScheduled = false;
            this.mHandler.getLooper().getQueue().removeSyncBarrier(this.mTraversalBarrier);
            this.mChoreographer.removeCallbacks(2, this.mTraversalRunnable, null);
        }
    }

    /* access modifiers changed from: package-private */
    public void doTraversal() {
        if (this.mTraversalScheduled) {
            this.mTraversalScheduled = false;
            this.mHandler.getLooper().getQueue().removeSyncBarrier(this.mTraversalBarrier);
            if (this.mProfile) {
                Debug.startMethodTracing("ViewAncestor");
            }
            performTraversals();
            if (this.mProfile) {
                Debug.stopMethodTracing();
                this.mProfile = false;
            }
        }
    }

    private void applyKeepScreenOnFlag(WindowManager.LayoutParams params) {
        if (this.mAttachInfo.mKeepScreenOn) {
            params.flags |= 128;
            Log.i(this.mTag, "view add KeepScreenOnFlag");
            return;
        }
        params.flags = (params.flags & -129) | (this.mClientWindowLayoutFlags & 128);
    }

    private boolean collectViewAttributes() {
        if (this.mAttachInfo.mRecomputeGlobalAttributes) {
            this.mAttachInfo.mRecomputeGlobalAttributes = false;
            boolean oldScreenOn = this.mAttachInfo.mKeepScreenOn;
            this.mAttachInfo.mKeepScreenOn = false;
            this.mAttachInfo.mSystemUiVisibility = 0;
            this.mAttachInfo.mHasSystemUiListeners = false;
            this.mView.dispatchCollectViewAttributes(this.mAttachInfo, 0);
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
        boolean windowSizeMayChange = false;
        long preMeasureTime = 0;
        if (Jlog.isPerfTest()) {
            preMeasureTime = System.nanoTime();
        }
        boolean goodMeasure = false;
        if (lp.width == -2) {
            DisplayMetrics packageMetrics = res.getDisplayMetrics();
            res.getValue(17104972, this.mTmpValue, true);
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
        if (!goodMeasure) {
            performMeasure(getRootMeasureSpec(desiredWindowWidth, lp.width), getRootMeasureSpec(desiredWindowHeight, lp.height));
            if (!(this.mWidth == host.getMeasuredWidth() && this.mHeight == host.getMeasuredHeight())) {
                windowSizeMayChange = true;
            }
        }
        if (Jlog.isPerfTest() != 0) {
            Jlog.i(2102, "#ME:" + (((System.nanoTime() - preMeasureTime) + REDUNDANT) / TimeUtils.NANOS_PER_MS));
        }
        return windowSizeMayChange;
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
        if (this.mLastWindowInsets == null || forceConstruct) {
            this.mDispatchContentInsets.set(this.mAttachInfo.mContentInsets);
            this.mDispatchStableInsets.set(this.mAttachInfo.mStableInsets);
            this.mDispatchDisplayCutout = this.mAttachInfo.mDisplayCutout.get();
            Rect contentInsets = this.mDispatchContentInsets;
            Rect stableInsets = this.mDispatchStableInsets;
            DisplayCutout displayCutout = this.mDispatchDisplayCutout;
            if (!forceConstruct && (!this.mPendingContentInsets.equals(contentInsets) || !this.mPendingStableInsets.equals(stableInsets) || !this.mPendingDisplayCutout.get().equals(displayCutout))) {
                contentInsets = this.mPendingContentInsets;
                stableInsets = this.mPendingStableInsets;
                displayCutout = this.mPendingDisplayCutout.get();
            }
            DisplayCutout displayCutout2 = displayCutout;
            Rect outsets = this.mAttachInfo.mOutsets;
            if (outsets.left > 0 || outsets.top > 0 || outsets.right > 0 || outsets.bottom > 0) {
                contentInsets = new Rect(contentInsets.left + outsets.left, contentInsets.top + outsets.top, contentInsets.right + outsets.right, contentInsets.bottom + outsets.bottom);
            }
            Rect contentInsets2 = ensureInsetsNonNegative(contentInsets, "content");
            WindowInsets windowInsets = new WindowInsets(contentInsets2, null, ensureInsetsNonNegative(stableInsets, "stable"), this.mContext.getResources().getConfiguration().isScreenRound(), this.mAttachInfo.mAlwaysConsumeNavBar, displayCutout2);
            this.mLastWindowInsets = windowInsets;
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
        boolean dispatchCutout = true;
        WindowInsets insets = getWindowInsets(true);
        if (this.mWindowAttributes.layoutInDisplayCutoutMode != 1 && (this.mWindowAttributes.hwFlags & 65536) == 0) {
            dispatchCutout = false;
        }
        if (!dispatchCutout) {
            insets = insets.consumeDisplayCutout();
        }
        host.dispatchApplyWindowInsets(insets);
    }

    private static boolean shouldUseDisplaySize(WindowManager.LayoutParams lp) {
        return lp.type == 2014 || lp.type == 2011 || lp.type == 2020;
    }

    private int dipToPx(int dip) {
        return (int) ((this.mContext.getResources().getDisplayMetrics().density * ((float) dip)) + 0.5f);
    }

    /* JADX WARNING: Code restructure failed: missing block: B:178:0x0337, code lost:
        if (r6.height() != r7.mHeight) goto L_0x0341;
     */
    /* JADX WARNING: Removed duplicated region for block: B:115:0x0233  */
    /* JADX WARNING: Removed duplicated region for block: B:118:0x0246  */
    /* JADX WARNING: Removed duplicated region for block: B:121:0x024e  */
    /* JADX WARNING: Removed duplicated region for block: B:128:0x026a  */
    /* JADX WARNING: Removed duplicated region for block: B:141:0x02a2  */
    /* JADX WARNING: Removed duplicated region for block: B:153:0x02c5  */
    /* JADX WARNING: Removed duplicated region for block: B:158:0x02f4  */
    /* JADX WARNING: Removed duplicated region for block: B:160:0x02f9 A[ADDED_TO_REGION] */
    /* JADX WARNING: Removed duplicated region for block: B:186:0x034c  */
    /* JADX WARNING: Removed duplicated region for block: B:192:0x0362  */
    /* JADX WARNING: Removed duplicated region for block: B:198:0x0379  */
    /* JADX WARNING: Removed duplicated region for block: B:199:0x037b  */
    /* JADX WARNING: Removed duplicated region for block: B:202:0x0384 A[ADDED_TO_REGION] */
    /* JADX WARNING: Removed duplicated region for block: B:214:0x03b9  */
    /* JADX WARNING: Removed duplicated region for block: B:221:0x03c6  */
    /* JADX WARNING: Removed duplicated region for block: B:224:0x03cc  */
    /* JADX WARNING: Removed duplicated region for block: B:225:0x03d9  */
    /* JADX WARNING: Removed duplicated region for block: B:228:0x03e9  */
    /* JADX WARNING: Removed duplicated region for block: B:237:0x0423  */
    /* JADX WARNING: Removed duplicated region for block: B:241:0x042f A[SYNTHETIC, Splitter:B:241:0x042f] */
    /* JADX WARNING: Removed duplicated region for block: B:276:0x04d9  */
    /* JADX WARNING: Removed duplicated region for block: B:281:0x04ea A[SYNTHETIC, Splitter:B:281:0x04ea] */
    /* JADX WARNING: Removed duplicated region for block: B:28:0x004c  */
    /* JADX WARNING: Removed duplicated region for block: B:290:0x0505 A[SYNTHETIC, Splitter:B:290:0x0505] */
    /* JADX WARNING: Removed duplicated region for block: B:296:0x0568 A[Catch:{ RemoteException -> 0x07de }] */
    /* JADX WARNING: Removed duplicated region for block: B:297:0x056a A[Catch:{ RemoteException -> 0x07de }] */
    /* JADX WARNING: Removed duplicated region for block: B:300:0x0577  */
    /* JADX WARNING: Removed duplicated region for block: B:301:0x0579  */
    /* JADX WARNING: Removed duplicated region for block: B:304:0x057e A[SYNTHETIC, Splitter:B:304:0x057e] */
    /* JADX WARNING: Removed duplicated region for block: B:307:0x0589 A[Catch:{ RemoteException -> 0x04ed }] */
    /* JADX WARNING: Removed duplicated region for block: B:309:0x0597 A[Catch:{ RemoteException -> 0x04ed }] */
    /* JADX WARNING: Removed duplicated region for block: B:311:0x05a5 A[Catch:{ RemoteException -> 0x04ed }] */
    /* JADX WARNING: Removed duplicated region for block: B:313:0x05b3 A[Catch:{ RemoteException -> 0x04ed }] */
    /* JADX WARNING: Removed duplicated region for block: B:31:0x0064  */
    /* JADX WARNING: Removed duplicated region for block: B:325:0x05f1 A[SYNTHETIC, Splitter:B:325:0x05f1] */
    /* JADX WARNING: Removed duplicated region for block: B:328:0x05fc A[SYNTHETIC, Splitter:B:328:0x05fc] */
    /* JADX WARNING: Removed duplicated region for block: B:373:0x0697  */
    /* JADX WARNING: Removed duplicated region for block: B:37:0x0088  */
    /* JADX WARNING: Removed duplicated region for block: B:418:0x073a  */
    /* JADX WARNING: Removed duplicated region for block: B:419:0x073c  */
    /* JADX WARNING: Removed duplicated region for block: B:422:0x0741  */
    /* JADX WARNING: Removed duplicated region for block: B:423:0x0743  */
    /* JADX WARNING: Removed duplicated region for block: B:426:0x0748 A[ADDED_TO_REGION] */
    /* JADX WARNING: Removed duplicated region for block: B:433:0x0753 A[Catch:{ RemoteException -> 0x07d3 }] */
    /* JADX WARNING: Removed duplicated region for block: B:447:0x07aa A[Catch:{ RemoteException -> 0x07d1 }] */
    /* JADX WARNING: Removed duplicated region for block: B:450:0x07bc A[Catch:{ RemoteException -> 0x07d1 }] */
    /* JADX WARNING: Removed duplicated region for block: B:469:0x083b  */
    /* JADX WARNING: Removed duplicated region for block: B:48:0x00f4  */
    /* JADX WARNING: Removed duplicated region for block: B:521:0x092d  */
    /* JADX WARNING: Removed duplicated region for block: B:522:0x092f  */
    /* JADX WARNING: Removed duplicated region for block: B:534:0x0978  */
    /* JADX WARNING: Removed duplicated region for block: B:537:0x098e  */
    /* JADX WARNING: Removed duplicated region for block: B:539:0x09a0  */
    /* JADX WARNING: Removed duplicated region for block: B:543:0x09a8  */
    /* JADX WARNING: Removed duplicated region for block: B:54:0x010c  */
    /* JADX WARNING: Removed duplicated region for block: B:551:0x09b6  */
    /* JADX WARNING: Removed duplicated region for block: B:557:0x09c3  */
    /* JADX WARNING: Removed duplicated region for block: B:570:0x0a30  */
    /* JADX WARNING: Removed duplicated region for block: B:572:0x0a3e  */
    /* JADX WARNING: Removed duplicated region for block: B:586:0x0aa6  */
    /* JADX WARNING: Removed duplicated region for block: B:603:0x0ae1  */
    /* JADX WARNING: Removed duplicated region for block: B:610:0x0af0 A[ADDED_TO_REGION] */
    /* JADX WARNING: Removed duplicated region for block: B:614:0x0af7  */
    /* JADX WARNING: Removed duplicated region for block: B:619:0x0b00  */
    /* JADX WARNING: Removed duplicated region for block: B:620:0x0b04 A[ADDED_TO_REGION] */
    /* JADX WARNING: Removed duplicated region for block: B:629:0x0b17  */
    /* JADX WARNING: Removed duplicated region for block: B:633:0x0b24  */
    /* JADX WARNING: Removed duplicated region for block: B:640:0x0b4a  */
    /* JADX WARNING: Removed duplicated region for block: B:647:0x0b89  */
    /* JADX WARNING: Removed duplicated region for block: B:654:0x0ba0 A[ADDED_TO_REGION] */
    /* JADX WARNING: Removed duplicated region for block: B:666:0x0bd0  */
    /* JADX WARNING: Removed duplicated region for block: B:667:0x0bd4  */
    /* JADX WARNING: Removed duplicated region for block: B:69:0x0135  */
    /* JADX WARNING: Removed duplicated region for block: B:80:0x0156  */
    private void performTraversals() {
        WindowManager.LayoutParams lp;
        boolean z;
        boolean surfaceChanged;
        CompatibilityInfo compatibilityInfo;
        int desiredWindowWidth;
        int desiredWindowHeight;
        boolean layoutRequested;
        Rect frame;
        boolean newSurface;
        boolean insetsChanged;
        boolean windowSizeMayChange;
        int desiredWindowWidth2;
        int i;
        int desiredWindowHeight2;
        WindowManager.LayoutParams params;
        int desiredWindowHeight3;
        WindowManager.LayoutParams params2;
        int desiredWindowHeight4;
        Rect frame2;
        boolean windowShouldResize;
        boolean computesInternalInsets;
        int surfaceGenerationId;
        boolean isViewVisible;
        boolean windowRelayoutWasForced;
        int relayoutResult;
        boolean insetsPending;
        boolean viewVisibilityChanged;
        boolean layoutRequested2;
        boolean didLayout;
        boolean triggerGlobalLayoutListener;
        boolean regainedFocus;
        boolean cancelDraw;
        boolean imTarget;
        Rect contentInsets;
        Rect visibleInsets;
        Region touchableRegion;
        boolean insetsPending2;
        WindowManager.LayoutParams params3;
        boolean insetsPending3;
        boolean updatedConfiguration;
        boolean contentInsetsChanged;
        boolean hadSurface;
        int relayoutResult2;
        boolean hwInitialized;
        boolean layoutRequested3;
        Rect frame3;
        int surfaceGenerationId2;
        int relayoutResult3;
        ThreadedRenderer threadedRenderer;
        boolean layoutRequested4;
        boolean measureAgain;
        Rect frame4;
        int relayoutResult4;
        boolean overscanInsetsChanged;
        boolean visibleInsetsChanged;
        boolean stableInsetsChanged;
        boolean cutoutChanged;
        boolean alwaysConsumeNavBarChanged;
        boolean dragResizing;
        boolean freeformResizing;
        boolean insetsPending4;
        int surfaceGenerationId3;
        int resizeMode;
        boolean windowSizeMayChange2;
        int desiredWindowHeight5;
        int desiredWindowWidth3;
        boolean windowSizeMayChange3;
        View host = this.mView;
        if (host != null && this.mAdded) {
            this.mIsInTraversal = true;
            this.mWillDrawSoon = true;
            boolean windowSizeMayChange4 = false;
            boolean surfaceChanged2 = false;
            lp = this.mWindowAttributes;
            int viewVisibility = getHostVisibility();
            boolean viewVisibilityChanged2 = !this.mFirst && (this.mViewVisibility != viewVisibility || this.mNewSurfaceNeeded || this.mAppVisibilityChanged);
            this.mAppVisibilityChanged = false;
            if (!this.mFirst) {
                if ((this.mViewVisibility == 0) != (viewVisibility == 0)) {
                    z = true;
                    boolean viewUserVisibilityChanged = z;
                    WindowManager.LayoutParams params4 = null;
                    if (this.mWindowAttributesChanged) {
                        this.mWindowAttributesChanged = false;
                        surfaceChanged2 = true;
                        params4 = lp;
                    }
                    surfaceChanged = surfaceChanged2;
                    compatibilityInfo = this.mDisplay.getDisplayAdjustments().getCompatibilityInfo();
                    if (compatibilityInfo.supportsScreen() == this.mLastInCompatMode) {
                        params4 = lp;
                        this.mFullRedrawNeeded = true;
                        this.mLayoutRequested = true;
                        if (this.mLastInCompatMode) {
                            params4.privateFlags &= -129;
                            this.mLastInCompatMode = false;
                        } else {
                            params4.privateFlags |= 128;
                            this.mLastInCompatMode = true;
                        }
                    }
                    WindowManager.LayoutParams params5 = params4;
                    this.mWindowAttributesChangesFlag = 0;
                    Rect frame5 = this.mWinFrame;
                    if (!this.mFirst) {
                        this.mFullRedrawNeeded = true;
                        this.mLayoutRequested = true;
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
                        this.mAttachInfo.mUse32BitDrawingCache = true;
                        this.mAttachInfo.mHasWindowFocus = false;
                        if (this.mView != null) {
                            this.mAttachInfo.mWindowVisibility = viewVisibility;
                            this.mAttachInfo.mRecomputeGlobalAttributes = false;
                            this.mLastConfigurationFromResources.setTo(config);
                            this.mLastSystemUiVisibility = this.mAttachInfo.mSystemUiVisibility;
                            if (this.mViewLayoutDirectionInitial == 2) {
                                host.setLayoutDirection(config.getLayoutDirection());
                            }
                            host.dispatchAttachedToWindow(this.mAttachInfo, 0);
                            this.mAttachInfo.mTreeObserver.dispatchOnWindowAttachedChange(true);
                            dispatchApplyInsets(host);
                        } else {
                            return;
                        }
                    } else {
                        desiredWindowWidth = frame5.width();
                        desiredWindowHeight = frame5.height();
                        if (!(desiredWindowWidth == this.mWidth && desiredWindowHeight == this.mHeight)) {
                            this.mFullRedrawNeeded = true;
                            this.mLayoutRequested = true;
                            windowSizeMayChange4 = true;
                        }
                    }
                    if (viewVisibilityChanged2) {
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
                            windowSizeMayChange2 = windowSizeMayChange4;
                            this.mAttachInfo.mInTouchMode = !this.mAddedTouchMode;
                            ensureTouchModeLocally(this.mAddedTouchMode);
                        } else {
                            windowSizeMayChange2 = windowSizeMayChange4;
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
                            if (this.mPendingAlwaysConsumeNavBar != this.mAttachInfo.mAlwaysConsumeNavBar) {
                                insetsChanged2 = true;
                            }
                            if (lp.width == -2 || lp.height == -2) {
                                if (shouldUseDisplaySize(lp)) {
                                    Point size2 = new Point();
                                    windowSizeMayChange3 = true;
                                    this.mDisplay.getRealSize(size2);
                                    desiredWindowWidth3 = size2.x;
                                    desiredWindowHeight5 = size2.y;
                                } else {
                                    windowSizeMayChange3 = true;
                                    Configuration config2 = res.getConfiguration();
                                    int desiredWindowWidth4 = dipToPx(config2.screenWidthDp);
                                    desiredWindowHeight5 = dipToPx(config2.screenHeightDp);
                                    desiredWindowWidth3 = desiredWindowWidth4;
                                }
                                windowSizeMayChange2 = windowSizeMayChange3;
                                insetsChanged = insetsChanged2;
                                newSurface = false;
                                i = -2;
                                frame = frame5;
                                CompatibilityInfo compatibilityInfo2 = compatibilityInfo;
                                desiredWindowHeight2 = desiredWindowHeight5;
                                desiredWindowWidth2 = desiredWindowWidth3;
                                windowSizeMayChange = windowSizeMayChange2 | measureHierarchy(host, lp, res, desiredWindowWidth3, desiredWindowHeight2);
                            }
                        }
                        insetsChanged = insetsChanged2;
                        desiredWindowHeight5 = desiredWindowHeight;
                        desiredWindowWidth3 = desiredWindowWidth;
                        newSurface = false;
                        i = -2;
                        frame = frame5;
                        CompatibilityInfo compatibilityInfo22 = compatibilityInfo;
                        desiredWindowHeight2 = desiredWindowHeight5;
                        desiredWindowWidth2 = desiredWindowWidth3;
                        windowSizeMayChange = windowSizeMayChange2 | measureHierarchy(host, lp, res, desiredWindowWidth3, desiredWindowHeight2);
                    } else {
                        windowSizeMayChange = windowSizeMayChange4;
                        frame = frame5;
                        CompatibilityInfo compatibilityInfo3 = compatibilityInfo;
                        newSurface = false;
                        i = -2;
                        insetsChanged = false;
                        desiredWindowHeight2 = desiredWindowHeight;
                        desiredWindowWidth2 = desiredWindowWidth;
                    }
                    if (collectViewAttributes()) {
                        params5 = lp;
                    }
                    if (this.mAttachInfo.mForceReportNewAttributes) {
                        this.mAttachInfo.mForceReportNewAttributes = false;
                        params5 = lp;
                    }
                    if (this.mFirst || this.mAttachInfo.mViewVisibilityChanged) {
                        this.mAttachInfo.mViewVisibilityChanged = false;
                        resizeMode = this.mSoftInputMode & 240;
                        if (resizeMode == 0) {
                            int N = this.mAttachInfo.mScrollContainers.size();
                            int resizeMode2 = resizeMode;
                            for (int i2 = 0; i2 < N; i2++) {
                                if (this.mAttachInfo.mScrollContainers.get(i2).isShown()) {
                                    resizeMode2 = 16;
                                }
                            }
                            if (resizeMode2 == 0) {
                                resizeMode2 = 32;
                            }
                            if ((lp.softInputMode & 240) != resizeMode2) {
                                lp.softInputMode = (lp.softInputMode & -241) | resizeMode2;
                                params5 = lp;
                            }
                        }
                    }
                    params = params5;
                    if (params != null) {
                        if ((host.mPrivateFlags & 512) != 0 && !PixelFormat.formatHasAlpha(params.format)) {
                            params.format = -3;
                        }
                        this.mAttachInfo.mOverscanRequested = (params.flags & 33554432) != 0;
                    }
                    if (this.mApplyInsetsRequested) {
                        this.mApplyInsetsRequested = false;
                        this.mLastOverscanRequested = this.mAttachInfo.mOverscanRequested;
                        dispatchApplyInsets(host);
                        if (this.mLayoutRequested) {
                            params2 = params;
                            desiredWindowHeight3 = desiredWindowHeight2;
                            windowSizeMayChange |= measureHierarchy(host, lp, this.mView.getContext().getResources(), desiredWindowWidth2, desiredWindowHeight2);
                            if (layoutRequested) {
                                this.mLayoutRequested = false;
                            }
                            if (layoutRequested || !windowSizeMayChange) {
                                frame2 = frame;
                                desiredWindowHeight4 = desiredWindowHeight3;
                            } else {
                                if (this.mWidth == host.getMeasuredWidth() && this.mHeight == host.getMeasuredHeight()) {
                                    if (lp.width == i) {
                                        frame2 = frame;
                                        if (frame2.width() < desiredWindowWidth2 && frame2.width() != this.mWidth) {
                                            desiredWindowHeight4 = desiredWindowHeight3;
                                        }
                                    } else {
                                        frame2 = frame;
                                    }
                                    if (lp.height == i) {
                                        desiredWindowHeight4 = desiredWindowHeight3;
                                        if (frame2.height() < desiredWindowHeight4) {
                                        }
                                    } else {
                                        desiredWindowHeight4 = desiredWindowHeight3;
                                    }
                                } else {
                                    frame2 = frame;
                                    desiredWindowHeight4 = desiredWindowHeight3;
                                }
                                windowShouldResize = true;
                                boolean windowShouldResize2 = windowShouldResize | (!this.mDragResizing && this.mResizeMode == 0) | this.mActivityRelaunched;
                                computesInternalInsets = !this.mAttachInfo.mTreeObserver.hasComputeInternalInsetsListeners() || this.mAttachInfo.mHasNonEmptyGivenInternalInsets;
                                surfaceGenerationId = this.mSurface.getGenerationId();
                                isViewVisible = viewVisibility != 0;
                                windowRelayoutWasForced = this.mForceNextWindowRelayout;
                                if (!this.mFirst || windowShouldResize2 || insetsChanged || viewVisibilityChanged2) {
                                    insetsPending2 = false;
                                    params3 = params2;
                                } else {
                                    params3 = params2;
                                    if (params3 == null) {
                                        insetsPending2 = false;
                                        if (!this.mForceNextWindowRelayout) {
                                            maybeHandleWindowMove(frame2);
                                            relayoutResult = false;
                                            WindowManager.LayoutParams layoutParams = params3;
                                            boolean z2 = windowRelayoutWasForced;
                                            layoutRequested2 = layoutRequested;
                                            int i3 = desiredWindowHeight4;
                                            int i4 = desiredWindowWidth2;
                                            viewVisibilityChanged = viewVisibilityChanged2;
                                            insetsPending = false;
                                            int i5 = surfaceGenerationId;
                                            Rect rect = frame2;
                                            didLayout = !layoutRequested2 && (!this.mStopped || this.mReportNextDraw);
                                            triggerGlobalLayoutListener = !didLayout || this.mAttachInfo.mRecomputeGlobalAttributes;
                                            if (didLayout) {
                                                performLayout(lp, this.mWidth, this.mHeight);
                                                if ((host.mPrivateFlags & 512) != 0) {
                                                    host.getLocationInWindow(this.mTmpLocation);
                                                    boolean z3 = layoutRequested2;
                                                    this.mTransparentRegion.set(this.mTmpLocation[0], this.mTmpLocation[1], (this.mTmpLocation[0] + host.mRight) - host.mLeft, (this.mTmpLocation[1] + host.mBottom) - host.mTop);
                                                    host.gatherTransparentRegion(this.mTransparentRegion);
                                                    if (this.mTranslator != null) {
                                                        this.mTranslator.translateRegionInWindowToScreen(this.mTransparentRegion);
                                                    }
                                                    if (!this.mTransparentRegion.equals(this.mPreviousTransparentRegion)) {
                                                        this.mPreviousTransparentRegion.set(this.mTransparentRegion);
                                                        this.mFullRedrawNeeded = true;
                                                        try {
                                                            this.mWindowSession.setTransparentRegion(this.mWindow, this.mTransparentRegion);
                                                        } catch (RemoteException e) {
                                                        }
                                                    }
                                                    if (triggerGlobalLayoutListener) {
                                                        this.mAttachInfo.mRecomputeGlobalAttributes = false;
                                                        this.mAttachInfo.mTreeObserver.dispatchOnGlobalLayout();
                                                    }
                                                    if (computesInternalInsets) {
                                                        ViewTreeObserver.InternalInsetsInfo insets = this.mAttachInfo.mGivenInternalInsets;
                                                        insets.reset();
                                                        this.mAttachInfo.mTreeObserver.dispatchOnComputeInternalInsets(insets);
                                                        this.mAttachInfo.mHasNonEmptyGivenInternalInsets = !insets.isEmpty();
                                                        if (insetsPending || !this.mLastGivenInsets.equals(insets)) {
                                                            this.mLastGivenInsets.set(insets);
                                                            if (this.mTranslator != null) {
                                                                contentInsets = this.mTranslator.getTranslatedContentInsets(insets.contentInsets);
                                                                visibleInsets = this.mTranslator.getTranslatedVisibleInsets(insets.visibleInsets);
                                                                touchableRegion = this.mTranslator.getTranslatedTouchableArea(insets.touchableRegion);
                                                            } else {
                                                                contentInsets = insets.contentInsets;
                                                                visibleInsets = insets.visibleInsets;
                                                                touchableRegion = insets.touchableRegion;
                                                            }
                                                            try {
                                                                this.mWindowSession.setInsets(this.mWindow, insets.mTouchableInsets, contentInsets, visibleInsets, touchableRegion);
                                                            } catch (RemoteException e2) {
                                                            }
                                                        }
                                                    }
                                                    if (this.mFirst) {
                                                        if (sAlwaysAssignFocus || !isInTouchMode()) {
                                                            if (this.mView != null && !this.mView.hasFocus()) {
                                                                this.mView.restoreDefaultFocus();
                                                            }
                                                        } else if (this.mView != null) {
                                                            View focused = this.mView.findFocus();
                                                            if ((focused instanceof ViewGroup) && ((ViewGroup) focused).getDescendantFocusability() == 262144) {
                                                                focused.restoreDefaultFocus();
                                                            }
                                                        }
                                                    }
                                                    boolean changedVisibility = (!viewVisibilityChanged || this.mFirst) && isViewVisible;
                                                    boolean hasWindowFocus = !this.mAttachInfo.mHasWindowFocus && isViewVisible;
                                                    regainedFocus = !hasWindowFocus && this.mLostWindowFocus;
                                                    if (regainedFocus) {
                                                        this.mLostWindowFocus = false;
                                                    } else if (!hasWindowFocus && this.mHadWindowFocus) {
                                                        this.mLostWindowFocus = true;
                                                    }
                                                    if (changedVisibility || regainedFocus) {
                                                        if (!(this.mWindowAttributes != null && this.mWindowAttributes.type == 2005)) {
                                                            host.sendAccessibilityEvent(32);
                                                        }
                                                    }
                                                    this.mFirst = false;
                                                    this.mWillDrawSoon = false;
                                                    this.mNewSurfaceNeeded = false;
                                                    this.mActivityRelaunched = false;
                                                    this.mViewVisibility = viewVisibility;
                                                    this.mHadWindowFocus = hasWindowFocus;
                                                    if (hasWindowFocus && !isInLocalFocusMode()) {
                                                        imTarget = WindowManager.LayoutParams.mayUseInputMethod(this.mWindowAttributes.flags);
                                                        if (imTarget != this.mLastWasImTarget) {
                                                            this.mLastWasImTarget = imTarget;
                                                            InputMethodManager imm = InputMethodManager.peekInstance();
                                                            if (imm != null && imTarget) {
                                                                imm.onPreWindowFocus(this.mView, hasWindowFocus);
                                                                boolean z4 = changedVisibility;
                                                                boolean z5 = hasWindowFocus;
                                                                cancelDraw = true;
                                                                imm.onPostWindowFocus(this.mView, this.mView.findFocus(), this.mWindowAttributes.softInputMode, !this.mHasHadWindowFocus, this.mWindowAttributes.flags);
                                                                if ((relayoutResult & 2) != 0) {
                                                                    reportNextDraw();
                                                                }
                                                                if (!this.mAttachInfo.mTreeObserver.dispatchOnPreDraw() && isViewVisible) {
                                                                    cancelDraw = false;
                                                                }
                                                                if (cancelDraw && !newSurface) {
                                                                    if (this.mPendingTransitions != null && this.mPendingTransitions.size() > 0) {
                                                                        for (int i6 = 0; i6 < this.mPendingTransitions.size(); i6++) {
                                                                            this.mPendingTransitions.get(i6).startChangingAnimations();
                                                                        }
                                                                        this.mPendingTransitions.clear();
                                                                    }
                                                                    performDraw();
                                                                } else if (isViewVisible) {
                                                                    scheduleTraversals();
                                                                } else if (this.mPendingTransitions != null && this.mPendingTransitions.size() > 0) {
                                                                    for (int i7 = 0; i7 < this.mPendingTransitions.size(); i7++) {
                                                                        this.mPendingTransitions.get(i7).endChangingAnimations();
                                                                    }
                                                                    this.mPendingTransitions.clear();
                                                                }
                                                                this.mIsInTraversal = false;
                                                            }
                                                        }
                                                    }
                                                    boolean z6 = hasWindowFocus;
                                                    cancelDraw = true;
                                                    if ((relayoutResult & 2) != 0) {
                                                    }
                                                    cancelDraw = false;
                                                    if (cancelDraw) {
                                                    }
                                                    if (isViewVisible) {
                                                    }
                                                    this.mIsInTraversal = false;
                                                }
                                            }
                                            if (triggerGlobalLayoutListener) {
                                            }
                                            if (computesInternalInsets) {
                                            }
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
                                            if (this.mWindowAttributes != null) {
                                                if (!(this.mWindowAttributes != null && this.mWindowAttributes.type == 2005)) {
                                                }
                                                this.mFirst = false;
                                                this.mWillDrawSoon = false;
                                                this.mNewSurfaceNeeded = false;
                                                this.mActivityRelaunched = false;
                                                this.mViewVisibility = viewVisibility;
                                                this.mHadWindowFocus = hasWindowFocus;
                                                imTarget = WindowManager.LayoutParams.mayUseInputMethod(this.mWindowAttributes.flags);
                                                if (imTarget != this.mLastWasImTarget) {
                                                }
                                                boolean z62 = hasWindowFocus;
                                                cancelDraw = true;
                                                if ((relayoutResult & 2) != 0) {
                                                }
                                                cancelDraw = false;
                                                if (cancelDraw) {
                                                }
                                                if (isViewVisible) {
                                                }
                                                this.mIsInTraversal = false;
                                            }
                                            if (!(this.mWindowAttributes != null && this.mWindowAttributes.type == 2005)) {
                                            }
                                            this.mFirst = false;
                                            this.mWillDrawSoon = false;
                                            this.mNewSurfaceNeeded = false;
                                            this.mActivityRelaunched = false;
                                            this.mViewVisibility = viewVisibility;
                                            this.mHadWindowFocus = hasWindowFocus;
                                            imTarget = WindowManager.LayoutParams.mayUseInputMethod(this.mWindowAttributes.flags);
                                            if (imTarget != this.mLastWasImTarget) {
                                            }
                                            boolean z622 = hasWindowFocus;
                                            cancelDraw = true;
                                            if ((relayoutResult & 2) != 0) {
                                            }
                                            cancelDraw = false;
                                            if (cancelDraw) {
                                            }
                                            if (isViewVisible) {
                                            }
                                            this.mIsInTraversal = false;
                                        }
                                    } else {
                                        insetsPending2 = false;
                                    }
                                }
                                this.mForceNextWindowRelayout = false;
                                if (!isViewVisible) {
                                    insetsPending3 = computesInternalInsets && (this.mFirst || viewVisibilityChanged2);
                                } else {
                                    insetsPending3 = insetsPending2;
                                }
                                if (this.mSurfaceHolder == null) {
                                    this.mSurfaceHolder.mSurfaceLock.lock();
                                    updatedConfiguration = false;
                                    this.mDrawingAllowed = true;
                                } else {
                                    updatedConfiguration = false;
                                }
                                contentInsetsChanged = false;
                                hadSurface = this.mSurface.isValid();
                                if (params3 == null) {
                                    try {
                                        int fl = params3.flags;
                                        if (this.mAttachInfo.mKeepScreenOn != 0) {
                                            params3.flags |= 128;
                                        }
                                        params3.subtreeSystemUiVisibility = this.mAttachInfo.mSystemUiVisibility;
                                        params3.hasSystemUiListeners = this.mAttachInfo.mHasSystemUiListeners;
                                        layoutRequested3 = layoutRequested;
                                        surfaceGenerationId2 = fl;
                                    } catch (RemoteException e3) {
                                        insetsPending = insetsPending3;
                                        hwInitialized = false;
                                        WindowManager.LayoutParams layoutParams2 = params3;
                                        boolean z7 = windowRelayoutWasForced;
                                        layoutRequested3 = layoutRequested;
                                        int i8 = desiredWindowHeight4;
                                        int i9 = desiredWindowWidth2;
                                        viewVisibilityChanged = viewVisibilityChanged2;
                                        relayoutResult2 = false;
                                        surfaceGenerationId2 = surfaceGenerationId;
                                        frame3 = frame2;
                                    }
                                } else {
                                    layoutRequested3 = layoutRequested;
                                    surfaceGenerationId2 = 0;
                                }
                                if (this.mAttachInfo.mThreadedRenderer == null) {
                                    try {
                                        hwInitialized = false;
                                        try {
                                            if (this.mAttachInfo.mThreadedRenderer.pauseSurface(this.mSurface)) {
                                                try {
                                                    frame4 = frame2;
                                                    try {
                                                        int i10 = desiredWindowHeight4;
                                                        try {
                                                            this.mDirty.set(0, 0, this.mWidth, this.mHeight);
                                                        } catch (RemoteException e4) {
                                                            insetsPending = insetsPending3;
                                                            WindowManager.LayoutParams layoutParams3 = params3;
                                                            boolean z8 = windowRelayoutWasForced;
                                                            surfaceGenerationId3 = surfaceGenerationId;
                                                            int i11 = desiredWindowWidth2;
                                                            viewVisibilityChanged = viewVisibilityChanged2;
                                                        }
                                                    } catch (RemoteException e5) {
                                                        int i12 = desiredWindowHeight4;
                                                        insetsPending = insetsPending3;
                                                        WindowManager.LayoutParams layoutParams4 = params3;
                                                        boolean z9 = windowRelayoutWasForced;
                                                        surfaceGenerationId2 = surfaceGenerationId;
                                                        int i13 = desiredWindowWidth2;
                                                        viewVisibilityChanged = viewVisibilityChanged2;
                                                        relayoutResult2 = false;
                                                        frame3 = frame4;
                                                    }
                                                } catch (RemoteException e6) {
                                                    int i14 = desiredWindowHeight4;
                                                    insetsPending = insetsPending3;
                                                    WindowManager.LayoutParams layoutParams5 = params3;
                                                    boolean z10 = windowRelayoutWasForced;
                                                    surfaceGenerationId2 = surfaceGenerationId;
                                                    frame3 = frame2;
                                                    int i15 = desiredWindowWidth2;
                                                    viewVisibilityChanged = viewVisibilityChanged2;
                                                    relayoutResult2 = false;
                                                }
                                            } else {
                                                frame4 = frame2;
                                                int i16 = desiredWindowHeight4;
                                            }
                                            try {
                                                int i17 = desiredWindowWidth2;
                                                viewVisibilityChanged = viewVisibilityChanged2;
                                                try {
                                                    this.mChoreographer.mFrameInfo.addFlags(1);
                                                } catch (RemoteException e7) {
                                                    insetsPending = insetsPending3;
                                                    WindowManager.LayoutParams layoutParams6 = params3;
                                                    boolean z11 = windowRelayoutWasForced;
                                                    surfaceGenerationId3 = surfaceGenerationId;
                                                }
                                            } catch (RemoteException e8) {
                                                int i18 = desiredWindowWidth2;
                                                viewVisibilityChanged = viewVisibilityChanged2;
                                                insetsPending = insetsPending3;
                                                WindowManager.LayoutParams layoutParams7 = params3;
                                                boolean z12 = windowRelayoutWasForced;
                                                surfaceGenerationId2 = surfaceGenerationId;
                                                relayoutResult2 = false;
                                                frame3 = frame4;
                                            }
                                        } catch (RemoteException e9) {
                                            int i19 = desiredWindowHeight4;
                                            int i20 = desiredWindowWidth2;
                                            viewVisibilityChanged = viewVisibilityChanged2;
                                            insetsPending = insetsPending3;
                                            WindowManager.LayoutParams layoutParams8 = params3;
                                            boolean z13 = windowRelayoutWasForced;
                                            surfaceGenerationId2 = surfaceGenerationId;
                                            frame3 = frame2;
                                            relayoutResult2 = false;
                                        }
                                    } catch (RemoteException e10) {
                                        hwInitialized = false;
                                        int i21 = desiredWindowHeight4;
                                        int i22 = desiredWindowWidth2;
                                        viewVisibilityChanged = viewVisibilityChanged2;
                                        insetsPending = insetsPending3;
                                        WindowManager.LayoutParams layoutParams9 = params3;
                                        boolean z14 = windowRelayoutWasForced;
                                        surfaceGenerationId2 = surfaceGenerationId;
                                        frame3 = frame2;
                                        relayoutResult2 = false;
                                    }
                                } else {
                                    hwInitialized = false;
                                    frame4 = frame2;
                                    int i23 = desiredWindowHeight4;
                                    int i24 = desiredWindowWidth2;
                                    viewVisibilityChanged = viewVisibilityChanged2;
                                }
                                relayoutResult4 = relayoutWindow(params3, viewVisibility, insetsPending3);
                                if (params3 != null) {
                                    try {
                                        params3.flags = surfaceGenerationId2;
                                    } catch (RemoteException e11) {
                                        insetsPending = insetsPending3;
                                    }
                                }
                                if (!this.mPendingMergedConfiguration.equals(this.mLastReportedMergedConfiguration)) {
                                    performConfigurationChange(this.mPendingMergedConfiguration, !this.mFirst, -1);
                                    updatedConfiguration = true;
                                }
                                overscanInsetsChanged = !this.mPendingOverscanInsets.equals(this.mAttachInfo.mOverscanInsets);
                                contentInsetsChanged = !this.mPendingContentInsets.equals(this.mAttachInfo.mContentInsets);
                                visibleInsetsChanged = !this.mPendingVisibleInsets.equals(this.mAttachInfo.mVisibleInsets);
                                stableInsetsChanged = !this.mPendingStableInsets.equals(this.mAttachInfo.mStableInsets);
                                cutoutChanged = !this.mPendingDisplayCutout.equals(this.mAttachInfo.mDisplayCutout);
                                boolean outsetsChanged = !this.mPendingOutsets.equals(this.mAttachInfo.mOutsets);
                                boolean surfaceSizeChanged = (relayoutResult4 & true) == false;
                                surfaceChanged |= surfaceSizeChanged;
                                alwaysConsumeNavBarChanged = this.mPendingAlwaysConsumeNavBar == this.mAttachInfo.mAlwaysConsumeNavBar;
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
                                if (alwaysConsumeNavBarChanged) {
                                    this.mAttachInfo.mAlwaysConsumeNavBar = this.mPendingAlwaysConsumeNavBar;
                                    contentInsetsChanged = true;
                                }
                                if (contentInsetsChanged || this.mLastSystemUiVisibility != this.mAttachInfo.mSystemUiVisibility || this.mApplyInsetsRequested || this.mLastOverscanRequested != this.mAttachInfo.mOverscanRequested || outsetsChanged) {
                                    this.mLastSystemUiVisibility = this.mAttachInfo.mSystemUiVisibility;
                                    this.mLastOverscanRequested = this.mAttachInfo.mOverscanRequested;
                                    this.mAttachInfo.mOutsets.set(this.mPendingOutsets);
                                    this.mApplyInsetsRequested = false;
                                    dispatchApplyInsets(host);
                                }
                                if (visibleInsetsChanged) {
                                    this.mAttachInfo.mVisibleInsets.set(this.mPendingVisibleInsets);
                                }
                                if (hadSurface) {
                                    try {
                                        if (this.mSurface.isValid()) {
                                            try {
                                                this.mFullRedrawNeeded = true;
                                                this.mPreviousTransparentRegion.setEmpty();
                                                if (this.mAttachInfo.mThreadedRenderer != null) {
                                                    try {
                                                        if (!surfaceControlllerIsValid()) {
                                                            StringBuilder sb = new StringBuilder();
                                                            insetsPending = insetsPending3;
                                                            try {
                                                                sb.append("##### surfaceControlller is not Valid can not initialize#### ");
                                                                sb.append(this.mSurface);
                                                                Log.w(TAG, sb.toString());
                                                            } catch (Surface.OutOfResourcesException e12) {
                                                                e = e12;
                                                                try {
                                                                    handleOutOfResourcesException(e);
                                                                    return;
                                                                } catch (RemoteException e13) {
                                                                    newSurface = true;
                                                                    surfaceGenerationId3 = surfaceGenerationId;
                                                                    relayoutResult2 = relayoutResult4;
                                                                    frame3 = frame4;
                                                                    boolean updatedConfiguration2 = updatedConfiguration;
                                                                    relayoutResult3 = relayoutResult2;
                                                                    this.mAttachInfo.mWindowLeft = frame3.left;
                                                                    this.mAttachInfo.mWindowTop = frame3.top;
                                                                    this.mWidth = frame3.width();
                                                                    this.mHeight = frame3.height();
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
                                                                    ThreadedRenderer threadedRenderer2 = threadedRenderer;
                                                                    relayoutResult = relayoutResult3;
                                                                    if (lp.horizontalWeight > 0.0f) {
                                                                    }
                                                                    if (lp.verticalWeight > 0.0f) {
                                                                    }
                                                                    if (measureAgain) {
                                                                    }
                                                                    layoutRequested4 = true;
                                                                    layoutRequested2 = layoutRequested4;
                                                                    didLayout = !layoutRequested2 && (!this.mStopped || this.mReportNextDraw);
                                                                    triggerGlobalLayoutListener = !didLayout || this.mAttachInfo.mRecomputeGlobalAttributes;
                                                                    if (didLayout) {
                                                                    }
                                                                    if (triggerGlobalLayoutListener) {
                                                                    }
                                                                    if (computesInternalInsets) {
                                                                    }
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
                                                                    if (!(this.mWindowAttributes != null && this.mWindowAttributes.type == 2005)) {
                                                                    }
                                                                    this.mFirst = false;
                                                                    this.mWillDrawSoon = false;
                                                                    this.mNewSurfaceNeeded = false;
                                                                    this.mActivityRelaunched = false;
                                                                    this.mViewVisibility = viewVisibility;
                                                                    this.mHadWindowFocus = hasWindowFocus;
                                                                    imTarget = WindowManager.LayoutParams.mayUseInputMethod(this.mWindowAttributes.flags);
                                                                    if (imTarget != this.mLastWasImTarget) {
                                                                    }
                                                                    boolean z6222 = hasWindowFocus;
                                                                    cancelDraw = true;
                                                                    if ((relayoutResult & 2) != 0) {
                                                                    }
                                                                    cancelDraw = false;
                                                                    if (cancelDraw) {
                                                                    }
                                                                    if (isViewVisible) {
                                                                    }
                                                                    this.mIsInTraversal = false;
                                                                }
                                                            }
                                                        } else {
                                                            insetsPending = insetsPending3;
                                                        }
                                                        boolean hwInitialized2 = this.mAttachInfo.mThreadedRenderer.initialize(this.mSurface);
                                                        if (hwInitialized2) {
                                                            try {
                                                                if ((host.mPrivateFlags & 512) == 0) {
                                                                    if (this.mAllocBufferAsync) {
                                                                        this.mAttachInfo.mThreadedRenderer.allocateBuffers(this.mSurface);
                                                                    } else {
                                                                        this.mSurface.allocateBuffers();
                                                                    }
                                                                }
                                                            } catch (Surface.OutOfResourcesException e14) {
                                                                e = e14;
                                                                hwInitialized = hwInitialized2;
                                                                handleOutOfResourcesException(e);
                                                                return;
                                                            } catch (RemoteException e15) {
                                                                hwInitialized = hwInitialized2;
                                                                newSurface = true;
                                                                surfaceGenerationId3 = surfaceGenerationId;
                                                                relayoutResult2 = relayoutResult4;
                                                                frame3 = frame4;
                                                                boolean updatedConfiguration22 = updatedConfiguration;
                                                                relayoutResult3 = relayoutResult2;
                                                                this.mAttachInfo.mWindowLeft = frame3.left;
                                                                this.mAttachInfo.mWindowTop = frame3.top;
                                                                this.mWidth = frame3.width();
                                                                this.mHeight = frame3.height();
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
                                                                ThreadedRenderer threadedRenderer22 = threadedRenderer;
                                                                relayoutResult = relayoutResult3;
                                                                if (lp.horizontalWeight > 0.0f) {
                                                                }
                                                                if (lp.verticalWeight > 0.0f) {
                                                                }
                                                                if (measureAgain) {
                                                                }
                                                                layoutRequested4 = true;
                                                                layoutRequested2 = layoutRequested4;
                                                                didLayout = !layoutRequested2 && (!this.mStopped || this.mReportNextDraw);
                                                                triggerGlobalLayoutListener = !didLayout || this.mAttachInfo.mRecomputeGlobalAttributes;
                                                                if (didLayout) {
                                                                }
                                                                if (triggerGlobalLayoutListener) {
                                                                }
                                                                if (computesInternalInsets) {
                                                                }
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
                                                                if (!(this.mWindowAttributes != null && this.mWindowAttributes.type == 2005)) {
                                                                }
                                                                this.mFirst = false;
                                                                this.mWillDrawSoon = false;
                                                                this.mNewSurfaceNeeded = false;
                                                                this.mActivityRelaunched = false;
                                                                this.mViewVisibility = viewVisibility;
                                                                this.mHadWindowFocus = hasWindowFocus;
                                                                imTarget = WindowManager.LayoutParams.mayUseInputMethod(this.mWindowAttributes.flags);
                                                                if (imTarget != this.mLastWasImTarget) {
                                                                }
                                                                boolean z62222 = hasWindowFocus;
                                                                cancelDraw = true;
                                                                if ((relayoutResult & 2) != 0) {
                                                                }
                                                                cancelDraw = false;
                                                                if (cancelDraw) {
                                                                }
                                                                if (isViewVisible) {
                                                                }
                                                                this.mIsInTraversal = false;
                                                            }
                                                        }
                                                        hwInitialized = hwInitialized2;
                                                        newSurface = true;
                                                    } catch (Surface.OutOfResourcesException e16) {
                                                        e = e16;
                                                        insetsPending = insetsPending3;
                                                        handleOutOfResourcesException(e);
                                                        return;
                                                    }
                                                } else {
                                                    insetsPending = insetsPending3;
                                                    newSurface = true;
                                                }
                                            } catch (RemoteException e17) {
                                                insetsPending4 = insetsPending3;
                                                newSurface = true;
                                                surfaceGenerationId2 = surfaceGenerationId;
                                                relayoutResult2 = relayoutResult4;
                                                frame3 = frame4;
                                                boolean updatedConfiguration222 = updatedConfiguration;
                                                relayoutResult3 = relayoutResult2;
                                                this.mAttachInfo.mWindowLeft = frame3.left;
                                                this.mAttachInfo.mWindowTop = frame3.top;
                                                this.mWidth = frame3.width();
                                                this.mHeight = frame3.height();
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
                                                ThreadedRenderer threadedRenderer222 = threadedRenderer;
                                                relayoutResult = relayoutResult3;
                                                if (lp.horizontalWeight > 0.0f) {
                                                }
                                                if (lp.verticalWeight > 0.0f) {
                                                }
                                                if (measureAgain) {
                                                }
                                                layoutRequested4 = true;
                                                layoutRequested2 = layoutRequested4;
                                                didLayout = !layoutRequested2 && (!this.mStopped || this.mReportNextDraw);
                                                triggerGlobalLayoutListener = !didLayout || this.mAttachInfo.mRecomputeGlobalAttributes;
                                                if (didLayout) {
                                                }
                                                if (triggerGlobalLayoutListener) {
                                                }
                                                if (computesInternalInsets) {
                                                }
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
                                                if (!(this.mWindowAttributes != null && this.mWindowAttributes.type == 2005)) {
                                                }
                                                this.mFirst = false;
                                                this.mWillDrawSoon = false;
                                                this.mNewSurfaceNeeded = false;
                                                this.mActivityRelaunched = false;
                                                this.mViewVisibility = viewVisibility;
                                                this.mHadWindowFocus = hasWindowFocus;
                                                imTarget = WindowManager.LayoutParams.mayUseInputMethod(this.mWindowAttributes.flags);
                                                if (imTarget != this.mLastWasImTarget) {
                                                }
                                                boolean z622222 = hasWindowFocus;
                                                cancelDraw = true;
                                                if ((relayoutResult & 2) != 0) {
                                                }
                                                cancelDraw = false;
                                                if (cancelDraw) {
                                                }
                                                if (isViewVisible) {
                                                }
                                                this.mIsInTraversal = false;
                                            }
                                        } else {
                                            insetsPending = insetsPending3;
                                        }
                                    } catch (RemoteException e18) {
                                        insetsPending4 = insetsPending3;
                                        surfaceGenerationId2 = surfaceGenerationId;
                                        relayoutResult2 = relayoutResult4;
                                        frame3 = frame4;
                                        boolean updatedConfiguration2222 = updatedConfiguration;
                                        relayoutResult3 = relayoutResult2;
                                        this.mAttachInfo.mWindowLeft = frame3.left;
                                        this.mAttachInfo.mWindowTop = frame3.top;
                                        this.mWidth = frame3.width();
                                        this.mHeight = frame3.height();
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
                                        ThreadedRenderer threadedRenderer2222 = threadedRenderer;
                                        relayoutResult = relayoutResult3;
                                        if (lp.horizontalWeight > 0.0f) {
                                        }
                                        if (lp.verticalWeight > 0.0f) {
                                        }
                                        if (measureAgain) {
                                        }
                                        layoutRequested4 = true;
                                        layoutRequested2 = layoutRequested4;
                                        didLayout = !layoutRequested2 && (!this.mStopped || this.mReportNextDraw);
                                        triggerGlobalLayoutListener = !didLayout || this.mAttachInfo.mRecomputeGlobalAttributes;
                                        if (didLayout) {
                                        }
                                        if (triggerGlobalLayoutListener) {
                                        }
                                        if (computesInternalInsets) {
                                        }
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
                                        if (!(this.mWindowAttributes != null && this.mWindowAttributes.type == 2005)) {
                                        }
                                        this.mFirst = false;
                                        this.mWillDrawSoon = false;
                                        this.mNewSurfaceNeeded = false;
                                        this.mActivityRelaunched = false;
                                        this.mViewVisibility = viewVisibility;
                                        this.mHadWindowFocus = hasWindowFocus;
                                        imTarget = WindowManager.LayoutParams.mayUseInputMethod(this.mWindowAttributes.flags);
                                        if (imTarget != this.mLastWasImTarget) {
                                        }
                                        boolean z6222222 = hasWindowFocus;
                                        cancelDraw = true;
                                        if ((relayoutResult & 2) != 0) {
                                        }
                                        cancelDraw = false;
                                        if (cancelDraw) {
                                        }
                                        if (isViewVisible) {
                                        }
                                        this.mIsInTraversal = false;
                                    }
                                } else {
                                    insetsPending = insetsPending3;
                                    try {
                                        if (!this.mSurface.isValid()) {
                                            try {
                                                if (this.mLastScrolledFocus != null) {
                                                    this.mLastScrolledFocus.clear();
                                                }
                                                this.mCurScrollY = 0;
                                                this.mScrollY = 0;
                                                if (this.mView instanceof RootViewSurfaceTaker) {
                                                    this.mView.onRootViewScrollYChanged(this.mCurScrollY);
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
                                            } catch (RemoteException e19) {
                                                surfaceGenerationId3 = surfaceGenerationId;
                                                relayoutResult2 = relayoutResult4;
                                                frame3 = frame4;
                                                boolean updatedConfiguration22222 = updatedConfiguration;
                                                relayoutResult3 = relayoutResult2;
                                                this.mAttachInfo.mWindowLeft = frame3.left;
                                                this.mAttachInfo.mWindowTop = frame3.top;
                                                this.mWidth = frame3.width();
                                                this.mHeight = frame3.height();
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
                                                ThreadedRenderer threadedRenderer22222 = threadedRenderer;
                                                relayoutResult = relayoutResult3;
                                                if (lp.horizontalWeight > 0.0f) {
                                                }
                                                if (lp.verticalWeight > 0.0f) {
                                                }
                                                if (measureAgain) {
                                                }
                                                layoutRequested4 = true;
                                                layoutRequested2 = layoutRequested4;
                                                didLayout = !layoutRequested2 && (!this.mStopped || this.mReportNextDraw);
                                                triggerGlobalLayoutListener = !didLayout || this.mAttachInfo.mRecomputeGlobalAttributes;
                                                if (didLayout) {
                                                }
                                                if (triggerGlobalLayoutListener) {
                                                }
                                                if (computesInternalInsets) {
                                                }
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
                                                if (!(this.mWindowAttributes != null && this.mWindowAttributes.type == 2005)) {
                                                }
                                                this.mFirst = false;
                                                this.mWillDrawSoon = false;
                                                this.mNewSurfaceNeeded = false;
                                                this.mActivityRelaunched = false;
                                                this.mViewVisibility = viewVisibility;
                                                this.mHadWindowFocus = hasWindowFocus;
                                                imTarget = WindowManager.LayoutParams.mayUseInputMethod(this.mWindowAttributes.flags);
                                                if (imTarget != this.mLastWasImTarget) {
                                                }
                                                boolean z62222222 = hasWindowFocus;
                                                cancelDraw = true;
                                                if ((relayoutResult & 2) != 0) {
                                                }
                                                cancelDraw = false;
                                                if (cancelDraw) {
                                                }
                                                if (isViewVisible) {
                                                }
                                                this.mIsInTraversal = false;
                                            }
                                        } else if ((surfaceGenerationId != this.mSurface.getGenerationId() || surfaceSizeChanged || windowRelayoutWasForced) && this.mSurfaceHolder == null) {
                                            if (this.mAttachInfo.mThreadedRenderer != null) {
                                                this.mFullRedrawNeeded = true;
                                                try {
                                                    if (surfaceControlllerIsValid()) {
                                                        this.mAttachInfo.mThreadedRenderer.updateSurface(this.mSurface);
                                                    } else {
                                                        Log.w(TAG, "##### surfaceControlller is not Valid #### " + this.mSurface);
                                                    }
                                                } catch (Surface.OutOfResourcesException e20) {
                                                    handleOutOfResourcesException(e20);
                                                    return;
                                                }
                                            }
                                        }
                                    } catch (RemoteException e21) {
                                        WindowManager.LayoutParams layoutParams10 = params3;
                                        boolean z15 = windowRelayoutWasForced;
                                        surfaceGenerationId2 = surfaceGenerationId;
                                        relayoutResult2 = relayoutResult4;
                                        frame3 = frame4;
                                    }
                                }
                                boolean freeformResizing2 = (relayoutResult4 & true) == false;
                                dragResizing = !freeformResizing2 || ((relayoutResult4 & true) == false);
                                if (this.mDragResizing != dragResizing) {
                                    WindowManager.LayoutParams layoutParams11 = params3;
                                    boolean z16 = windowRelayoutWasForced;
                                    freeformResizing = dragResizing;
                                    int i25 = surfaceGenerationId2;
                                    relayoutResult2 = relayoutResult4;
                                    frame3 = frame4;
                                    surfaceGenerationId2 = surfaceGenerationId;
                                } else if (dragResizing) {
                                    this.mResizeMode = freeformResizing2 ? 0 : 1;
                                    boolean z17 = freeformResizing2;
                                    int surfaceGenerationId4 = surfaceGenerationId;
                                    try {
                                        int frame6 = relayoutResult4;
                                        WindowManager.LayoutParams layoutParams12 = params3;
                                        boolean z18 = windowRelayoutWasForced;
                                        int i26 = surfaceGenerationId2;
                                        surfaceGenerationId2 = surfaceGenerationId4;
                                        relayoutResult2 = relayoutResult4;
                                        Rect rect2 = frame4;
                                        freeformResizing = dragResizing;
                                        try {
                                            frame6 = rect2;
                                            startDragResizing(this.mPendingBackDropFrame, this.mWinFrame.equals(this.mPendingBackDropFrame), this.mPendingVisibleInsets, this.mPendingStableInsets, this.mResizeMode);
                                            frame3 = rect2;
                                        } catch (RemoteException e22) {
                                            frame3 = frame6;
                                        }
                                    } catch (RemoteException e23) {
                                        WindowManager.LayoutParams layoutParams13 = params3;
                                        boolean z19 = windowRelayoutWasForced;
                                        relayoutResult2 = relayoutResult4;
                                        frame3 = frame4;
                                        surfaceGenerationId2 = surfaceGenerationId4;
                                    }
                                } else {
                                    boolean z20 = freeformResizing2;
                                    WindowManager.LayoutParams layoutParams14 = params3;
                                    boolean z21 = windowRelayoutWasForced;
                                    freeformResizing = dragResizing;
                                    int i27 = surfaceGenerationId2;
                                    relayoutResult2 = relayoutResult4;
                                    frame3 = frame4;
                                    surfaceGenerationId2 = surfaceGenerationId;
                                    endDragResizing();
                                }
                                if (!this.mUseMTRenderer) {
                                    if (freeformResizing) {
                                        this.mCanvasOffsetX = this.mWinFrame.left;
                                        this.mCanvasOffsetY = this.mWinFrame.top;
                                    } else {
                                        this.mCanvasOffsetY = 0;
                                        this.mCanvasOffsetX = 0;
                                    }
                                }
                                boolean updatedConfiguration222222 = updatedConfiguration;
                                relayoutResult3 = relayoutResult2;
                                this.mAttachInfo.mWindowLeft = frame3.left;
                                this.mAttachInfo.mWindowTop = frame3.top;
                                if (!(this.mWidth == frame3.width() && this.mHeight == frame3.height())) {
                                    this.mWidth = frame3.width();
                                    this.mHeight = frame3.height();
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
                                                for (SurfaceHolder.Callback c : callbacks) {
                                                    c.surfaceCreated(this.mSurfaceHolder);
                                                }
                                            }
                                            surfaceChanged = true;
                                        }
                                        if (surfaceChanged || surfaceGenerationId2 != this.mSurface.getGenerationId()) {
                                            SurfaceHolder.Callback[] callbacks2 = this.mSurfaceHolder.getCallbacks();
                                            if (callbacks2 != null) {
                                                int length = callbacks2.length;
                                                int i28 = 0;
                                                while (i28 < length) {
                                                    callbacks2[i28].surfaceChanged(this.mSurfaceHolder, lp.format, this.mWidth, this.mHeight);
                                                    i28++;
                                                    callbacks2 = callbacks2;
                                                }
                                            }
                                        }
                                        this.mIsCreating = false;
                                    } else if (hadSurface) {
                                        this.mSurfaceHolder.ungetCallbacks();
                                        SurfaceHolder.Callback[] callbacks3 = this.mSurfaceHolder.getCallbacks();
                                        if (callbacks3 != null) {
                                            for (SurfaceHolder.Callback c2 : callbacks3) {
                                                c2.surfaceDestroyed(this.mSurfaceHolder);
                                            }
                                        }
                                        this.mSurfaceHolder.mSurfaceLock.lock();
                                        try {
                                            this.mSurfaceHolder.mSurface = new Surface();
                                        } finally {
                                            this.mSurfaceHolder.mSurfaceLock.unlock();
                                        }
                                    }
                                }
                                threadedRenderer = this.mAttachInfo.mThreadedRenderer;
                                if (threadedRenderer != null && threadedRenderer.isEnabled() && (hwInitialized || this.mWidth != threadedRenderer.getWidth() || this.mHeight != threadedRenderer.getHeight() || this.mNeedsRendererSetup)) {
                                    threadedRenderer.setup(this.mWidth, this.mHeight, this.mAttachInfo, this.mWindowAttributes.surfaceInsets);
                                    this.mNeedsRendererSetup = false;
                                }
                                if (!this.mStopped || this.mReportNextDraw) {
                                    if (ensureTouchModeLocally((relayoutResult3 & 1) == 0) || this.mWidth != host.getMeasuredWidth() || this.mHeight != host.getMeasuredHeight() || contentInsetsChanged || updatedConfiguration222222) {
                                        int childWidthMeasureSpec22222 = getRootMeasureSpec(this.mWidth, lp.width);
                                        int childHeightMeasureSpec22222 = getRootMeasureSpec(this.mHeight, lp.height);
                                        performMeasure(childWidthMeasureSpec22222, childHeightMeasureSpec22222);
                                        int width22222 = host.getMeasuredWidth();
                                        int height22222 = host.getMeasuredHeight();
                                        measureAgain = false;
                                        ThreadedRenderer threadedRenderer222222 = threadedRenderer;
                                        relayoutResult = relayoutResult3;
                                        if (lp.horizontalWeight > 0.0f) {
                                            childWidthMeasureSpec22222 = View.MeasureSpec.makeMeasureSpec(width22222 + ((int) (((float) (this.mWidth - width22222)) * lp.horizontalWeight)), 1073741824);
                                            measureAgain = true;
                                        }
                                        if (lp.verticalWeight > 0.0f) {
                                            childHeightMeasureSpec22222 = View.MeasureSpec.makeMeasureSpec(height22222 + ((int) (((float) (this.mHeight - height22222)) * lp.verticalWeight)), 1073741824);
                                            measureAgain = true;
                                        }
                                        if (measureAgain) {
                                            performMeasure(childWidthMeasureSpec22222, childHeightMeasureSpec22222);
                                        }
                                        layoutRequested4 = true;
                                        layoutRequested2 = layoutRequested4;
                                        didLayout = !layoutRequested2 && (!this.mStopped || this.mReportNextDraw);
                                        triggerGlobalLayoutListener = !didLayout || this.mAttachInfo.mRecomputeGlobalAttributes;
                                        if (didLayout) {
                                        }
                                        if (triggerGlobalLayoutListener) {
                                        }
                                        if (computesInternalInsets) {
                                        }
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
                                        if (!(this.mWindowAttributes != null && this.mWindowAttributes.type == 2005)) {
                                        }
                                        this.mFirst = false;
                                        this.mWillDrawSoon = false;
                                        this.mNewSurfaceNeeded = false;
                                        this.mActivityRelaunched = false;
                                        this.mViewVisibility = viewVisibility;
                                        this.mHadWindowFocus = hasWindowFocus;
                                        imTarget = WindowManager.LayoutParams.mayUseInputMethod(this.mWindowAttributes.flags);
                                        if (imTarget != this.mLastWasImTarget) {
                                        }
                                        boolean z622222222 = hasWindowFocus;
                                        cancelDraw = true;
                                        if ((relayoutResult & 2) != 0) {
                                        }
                                        cancelDraw = false;
                                        if (cancelDraw) {
                                        }
                                        if (isViewVisible) {
                                        }
                                        this.mIsInTraversal = false;
                                    }
                                }
                                relayoutResult = relayoutResult3;
                                layoutRequested4 = layoutRequested3;
                                layoutRequested2 = layoutRequested4;
                                didLayout = !layoutRequested2 && (!this.mStopped || this.mReportNextDraw);
                                triggerGlobalLayoutListener = !didLayout || this.mAttachInfo.mRecomputeGlobalAttributes;
                                if (didLayout) {
                                }
                                if (triggerGlobalLayoutListener) {
                                }
                                if (computesInternalInsets) {
                                }
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
                                if (!(this.mWindowAttributes != null && this.mWindowAttributes.type == 2005)) {
                                }
                                this.mFirst = false;
                                this.mWillDrawSoon = false;
                                this.mNewSurfaceNeeded = false;
                                this.mActivityRelaunched = false;
                                this.mViewVisibility = viewVisibility;
                                this.mHadWindowFocus = hasWindowFocus;
                                imTarget = WindowManager.LayoutParams.mayUseInputMethod(this.mWindowAttributes.flags);
                                if (imTarget != this.mLastWasImTarget) {
                                }
                                boolean z6222222222 = hasWindowFocus;
                                cancelDraw = true;
                                if ((relayoutResult & 2) != 0) {
                                }
                                cancelDraw = false;
                                if (cancelDraw) {
                                }
                                if (isViewVisible) {
                                }
                                this.mIsInTraversal = false;
                            }
                            windowShouldResize = false;
                            boolean windowShouldResize22 = windowShouldResize | (!this.mDragResizing && this.mResizeMode == 0) | this.mActivityRelaunched;
                            computesInternalInsets = !this.mAttachInfo.mTreeObserver.hasComputeInternalInsetsListeners() || this.mAttachInfo.mHasNonEmptyGivenInternalInsets;
                            surfaceGenerationId = this.mSurface.getGenerationId();
                            isViewVisible = viewVisibility != 0;
                            windowRelayoutWasForced = this.mForceNextWindowRelayout;
                            if (!this.mFirst) {
                            }
                            insetsPending2 = false;
                            params3 = params2;
                            this.mForceNextWindowRelayout = false;
                            if (!isViewVisible) {
                            }
                            if (this.mSurfaceHolder == null) {
                            }
                            contentInsetsChanged = false;
                            hadSurface = this.mSurface.isValid();
                            if (params3 == null) {
                            }
                            if (this.mAttachInfo.mThreadedRenderer == null) {
                            }
                            relayoutResult4 = relayoutWindow(params3, viewVisibility, insetsPending3);
                            if (params3 != null) {
                            }
                            if (!this.mPendingMergedConfiguration.equals(this.mLastReportedMergedConfiguration)) {
                            }
                            overscanInsetsChanged = !this.mPendingOverscanInsets.equals(this.mAttachInfo.mOverscanInsets);
                            contentInsetsChanged = !this.mPendingContentInsets.equals(this.mAttachInfo.mContentInsets);
                            visibleInsetsChanged = !this.mPendingVisibleInsets.equals(this.mAttachInfo.mVisibleInsets);
                            stableInsetsChanged = !this.mPendingStableInsets.equals(this.mAttachInfo.mStableInsets);
                            cutoutChanged = !this.mPendingDisplayCutout.equals(this.mAttachInfo.mDisplayCutout);
                            boolean outsetsChanged2 = !this.mPendingOutsets.equals(this.mAttachInfo.mOutsets);
                            boolean surfaceSizeChanged2 = (relayoutResult4 & true) == false;
                            surfaceChanged |= surfaceSizeChanged2;
                            alwaysConsumeNavBarChanged = this.mPendingAlwaysConsumeNavBar == this.mAttachInfo.mAlwaysConsumeNavBar;
                            if (contentInsetsChanged) {
                            }
                            if (overscanInsetsChanged) {
                            }
                            if (stableInsetsChanged) {
                            }
                            if (cutoutChanged) {
                            }
                            if (alwaysConsumeNavBarChanged) {
                            }
                            this.mLastSystemUiVisibility = this.mAttachInfo.mSystemUiVisibility;
                            this.mLastOverscanRequested = this.mAttachInfo.mOverscanRequested;
                            this.mAttachInfo.mOutsets.set(this.mPendingOutsets);
                            this.mApplyInsetsRequested = false;
                            dispatchApplyInsets(host);
                            if (visibleInsetsChanged) {
                            }
                            if (hadSurface) {
                            }
                            if (relayoutResult4 == false || !true) {
                            }
                            dragResizing = !freeformResizing2 || ((relayoutResult4 & true) == false);
                            if (this.mDragResizing != dragResizing) {
                            }
                            if (!this.mUseMTRenderer) {
                            }
                            boolean updatedConfiguration2222222 = updatedConfiguration;
                            relayoutResult3 = relayoutResult2;
                            this.mAttachInfo.mWindowLeft = frame3.left;
                            this.mAttachInfo.mWindowTop = frame3.top;
                            this.mWidth = frame3.width();
                            this.mHeight = frame3.height();
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
                            ThreadedRenderer threadedRenderer2222222 = threadedRenderer;
                            relayoutResult = relayoutResult3;
                            if (lp.horizontalWeight > 0.0f) {
                            }
                            if (lp.verticalWeight > 0.0f) {
                            }
                            if (measureAgain) {
                            }
                            layoutRequested4 = true;
                            layoutRequested2 = layoutRequested4;
                            didLayout = !layoutRequested2 && (!this.mStopped || this.mReportNextDraw);
                            triggerGlobalLayoutListener = !didLayout || this.mAttachInfo.mRecomputeGlobalAttributes;
                            if (didLayout) {
                            }
                            if (triggerGlobalLayoutListener) {
                            }
                            if (computesInternalInsets) {
                            }
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
                            if (!(this.mWindowAttributes != null && this.mWindowAttributes.type == 2005)) {
                            }
                            this.mFirst = false;
                            this.mWillDrawSoon = false;
                            this.mNewSurfaceNeeded = false;
                            this.mActivityRelaunched = false;
                            this.mViewVisibility = viewVisibility;
                            this.mHadWindowFocus = hasWindowFocus;
                            imTarget = WindowManager.LayoutParams.mayUseInputMethod(this.mWindowAttributes.flags);
                            if (imTarget != this.mLastWasImTarget) {
                            }
                            boolean z62222222222 = hasWindowFocus;
                            cancelDraw = true;
                            if ((relayoutResult & 2) != 0) {
                            }
                            cancelDraw = false;
                            if (cancelDraw) {
                            }
                            if (isViewVisible) {
                            }
                            this.mIsInTraversal = false;
                        }
                    }
                    params2 = params;
                    desiredWindowHeight3 = desiredWindowHeight2;
                    if (layoutRequested) {
                    }
                    if (layoutRequested) {
                    }
                    frame2 = frame;
                    desiredWindowHeight4 = desiredWindowHeight3;
                    windowShouldResize = false;
                    boolean windowShouldResize222 = windowShouldResize | (!this.mDragResizing && this.mResizeMode == 0) | this.mActivityRelaunched;
                    computesInternalInsets = !this.mAttachInfo.mTreeObserver.hasComputeInternalInsetsListeners() || this.mAttachInfo.mHasNonEmptyGivenInternalInsets;
                    surfaceGenerationId = this.mSurface.getGenerationId();
                    isViewVisible = viewVisibility != 0;
                    windowRelayoutWasForced = this.mForceNextWindowRelayout;
                    if (!this.mFirst) {
                    }
                    insetsPending2 = false;
                    params3 = params2;
                    this.mForceNextWindowRelayout = false;
                    if (!isViewVisible) {
                    }
                    if (this.mSurfaceHolder == null) {
                    }
                    contentInsetsChanged = false;
                    hadSurface = this.mSurface.isValid();
                    if (params3 == null) {
                    }
                    if (this.mAttachInfo.mThreadedRenderer == null) {
                    }
                    relayoutResult4 = relayoutWindow(params3, viewVisibility, insetsPending3);
                    if (params3 != null) {
                    }
                    try {
                        if (!this.mPendingMergedConfiguration.equals(this.mLastReportedMergedConfiguration)) {
                        }
                        overscanInsetsChanged = !this.mPendingOverscanInsets.equals(this.mAttachInfo.mOverscanInsets);
                        contentInsetsChanged = !this.mPendingContentInsets.equals(this.mAttachInfo.mContentInsets);
                        visibleInsetsChanged = !this.mPendingVisibleInsets.equals(this.mAttachInfo.mVisibleInsets);
                        stableInsetsChanged = !this.mPendingStableInsets.equals(this.mAttachInfo.mStableInsets);
                        cutoutChanged = !this.mPendingDisplayCutout.equals(this.mAttachInfo.mDisplayCutout);
                        boolean outsetsChanged22 = !this.mPendingOutsets.equals(this.mAttachInfo.mOutsets);
                        boolean surfaceSizeChanged22 = (relayoutResult4 & true) == false;
                        surfaceChanged |= surfaceSizeChanged22;
                        alwaysConsumeNavBarChanged = this.mPendingAlwaysConsumeNavBar == this.mAttachInfo.mAlwaysConsumeNavBar;
                        if (contentInsetsChanged) {
                        }
                        if (overscanInsetsChanged) {
                        }
                        if (stableInsetsChanged) {
                        }
                        if (cutoutChanged) {
                        }
                        if (alwaysConsumeNavBarChanged) {
                        }
                        this.mLastSystemUiVisibility = this.mAttachInfo.mSystemUiVisibility;
                        this.mLastOverscanRequested = this.mAttachInfo.mOverscanRequested;
                        this.mAttachInfo.mOutsets.set(this.mPendingOutsets);
                        this.mApplyInsetsRequested = false;
                        dispatchApplyInsets(host);
                        if (visibleInsetsChanged) {
                        }
                        if (hadSurface) {
                        }
                        if (relayoutResult4 == false || !true) {
                        }
                        dragResizing = !freeformResizing2 || ((relayoutResult4 & true) == false);
                        if (this.mDragResizing != dragResizing) {
                        }
                        if (!this.mUseMTRenderer) {
                        }
                    } catch (RemoteException e24) {
                        insetsPending = insetsPending3;
                        WindowManager.LayoutParams layoutParams15 = params3;
                        boolean z22 = windowRelayoutWasForced;
                        surfaceGenerationId2 = surfaceGenerationId;
                        relayoutResult2 = relayoutResult4;
                        frame3 = frame4;
                    }
                    boolean updatedConfiguration22222222 = updatedConfiguration;
                    relayoutResult3 = relayoutResult2;
                    this.mAttachInfo.mWindowLeft = frame3.left;
                    this.mAttachInfo.mWindowTop = frame3.top;
                    this.mWidth = frame3.width();
                    this.mHeight = frame3.height();
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
                    ThreadedRenderer threadedRenderer22222222 = threadedRenderer;
                    relayoutResult = relayoutResult3;
                    if (lp.horizontalWeight > 0.0f) {
                    }
                    if (lp.verticalWeight > 0.0f) {
                    }
                    if (measureAgain) {
                    }
                    layoutRequested4 = true;
                    layoutRequested2 = layoutRequested4;
                    didLayout = !layoutRequested2 && (!this.mStopped || this.mReportNextDraw);
                    triggerGlobalLayoutListener = !didLayout || this.mAttachInfo.mRecomputeGlobalAttributes;
                    if (didLayout) {
                    }
                    if (triggerGlobalLayoutListener) {
                    }
                    if (computesInternalInsets) {
                    }
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
                    if (!(this.mWindowAttributes != null && this.mWindowAttributes.type == 2005)) {
                    }
                    this.mFirst = false;
                    this.mWillDrawSoon = false;
                    this.mNewSurfaceNeeded = false;
                    this.mActivityRelaunched = false;
                    this.mViewVisibility = viewVisibility;
                    this.mHadWindowFocus = hasWindowFocus;
                    imTarget = WindowManager.LayoutParams.mayUseInputMethod(this.mWindowAttributes.flags);
                    if (imTarget != this.mLastWasImTarget) {
                    }
                    boolean z622222222222 = hasWindowFocus;
                    cancelDraw = true;
                    if ((relayoutResult & 2) != 0) {
                    }
                    cancelDraw = false;
                    if (cancelDraw) {
                    }
                    if (isViewVisible) {
                    }
                    this.mIsInTraversal = false;
                }
            }
            z = false;
            boolean viewUserVisibilityChanged2 = z;
            WindowManager.LayoutParams params42 = null;
            if (this.mWindowAttributesChanged) {
            }
            surfaceChanged = surfaceChanged2;
            compatibilityInfo = this.mDisplay.getDisplayAdjustments().getCompatibilityInfo();
            if (compatibilityInfo.supportsScreen() == this.mLastInCompatMode) {
            }
            WindowManager.LayoutParams params52 = params42;
            this.mWindowAttributesChangesFlag = 0;
            Rect frame52 = this.mWinFrame;
            if (!this.mFirst) {
            }
            if (viewVisibilityChanged2) {
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
            this.mAttachInfo.mViewVisibilityChanged = false;
            resizeMode = this.mSoftInputMode & 240;
            if (resizeMode == 0) {
            }
            params = params52;
            if (params != null) {
            }
            if (this.mApplyInsetsRequested) {
            }
            params2 = params;
            desiredWindowHeight3 = desiredWindowHeight2;
            if (layoutRequested) {
            }
            if (layoutRequested) {
            }
            frame2 = frame;
            desiredWindowHeight4 = desiredWindowHeight3;
            windowShouldResize = false;
            boolean windowShouldResize2222 = windowShouldResize | (!this.mDragResizing && this.mResizeMode == 0) | this.mActivityRelaunched;
            computesInternalInsets = !this.mAttachInfo.mTreeObserver.hasComputeInternalInsetsListeners() || this.mAttachInfo.mHasNonEmptyGivenInternalInsets;
            surfaceGenerationId = this.mSurface.getGenerationId();
            isViewVisible = viewVisibility != 0;
            windowRelayoutWasForced = this.mForceNextWindowRelayout;
            if (!this.mFirst) {
            }
            insetsPending2 = false;
            params3 = params2;
            this.mForceNextWindowRelayout = false;
            if (!isViewVisible) {
            }
            if (this.mSurfaceHolder == null) {
            }
            contentInsetsChanged = false;
            hadSurface = this.mSurface.isValid();
            if (params3 == null) {
            }
            try {
                if (this.mAttachInfo.mThreadedRenderer == null) {
                }
                try {
                    relayoutResult4 = relayoutWindow(params3, viewVisibility, insetsPending3);
                    if (params3 != null) {
                    }
                    if (!this.mPendingMergedConfiguration.equals(this.mLastReportedMergedConfiguration)) {
                    }
                    overscanInsetsChanged = !this.mPendingOverscanInsets.equals(this.mAttachInfo.mOverscanInsets);
                    contentInsetsChanged = !this.mPendingContentInsets.equals(this.mAttachInfo.mContentInsets);
                    visibleInsetsChanged = !this.mPendingVisibleInsets.equals(this.mAttachInfo.mVisibleInsets);
                    stableInsetsChanged = !this.mPendingStableInsets.equals(this.mAttachInfo.mStableInsets);
                    cutoutChanged = !this.mPendingDisplayCutout.equals(this.mAttachInfo.mDisplayCutout);
                    boolean outsetsChanged222 = !this.mPendingOutsets.equals(this.mAttachInfo.mOutsets);
                    boolean surfaceSizeChanged222 = (relayoutResult4 & true) == false;
                    surfaceChanged |= surfaceSizeChanged222;
                    alwaysConsumeNavBarChanged = this.mPendingAlwaysConsumeNavBar == this.mAttachInfo.mAlwaysConsumeNavBar;
                    if (contentInsetsChanged) {
                    }
                    if (overscanInsetsChanged) {
                    }
                    if (stableInsetsChanged) {
                    }
                    if (cutoutChanged) {
                    }
                    if (alwaysConsumeNavBarChanged) {
                    }
                    this.mLastSystemUiVisibility = this.mAttachInfo.mSystemUiVisibility;
                    this.mLastOverscanRequested = this.mAttachInfo.mOverscanRequested;
                    this.mAttachInfo.mOutsets.set(this.mPendingOutsets);
                    this.mApplyInsetsRequested = false;
                    dispatchApplyInsets(host);
                    if (visibleInsetsChanged) {
                    }
                    if (hadSurface) {
                    }
                    if (relayoutResult4 == false || !true) {
                    }
                    dragResizing = !freeformResizing2 || ((relayoutResult4 & true) == false);
                    if (this.mDragResizing != dragResizing) {
                    }
                    if (!this.mUseMTRenderer) {
                    }
                } catch (RemoteException e25) {
                    insetsPending = insetsPending3;
                    WindowManager.LayoutParams layoutParams16 = params3;
                    boolean z23 = windowRelayoutWasForced;
                    surfaceGenerationId2 = surfaceGenerationId;
                    frame3 = frame4;
                    relayoutResult2 = false;
                }
            } catch (RemoteException e26) {
                insetsPending = insetsPending3;
                hwInitialized = false;
                WindowManager.LayoutParams layoutParams17 = params3;
                boolean z24 = windowRelayoutWasForced;
                surfaceGenerationId2 = surfaceGenerationId;
                int i29 = desiredWindowHeight4;
                int i30 = desiredWindowWidth2;
                viewVisibilityChanged = viewVisibilityChanged2;
                frame3 = frame2;
                relayoutResult2 = 0;
            }
            boolean updatedConfiguration222222222 = updatedConfiguration;
            relayoutResult3 = relayoutResult2;
            this.mAttachInfo.mWindowLeft = frame3.left;
            this.mAttachInfo.mWindowTop = frame3.top;
            this.mWidth = frame3.width();
            this.mHeight = frame3.height();
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
            ThreadedRenderer threadedRenderer222222222 = threadedRenderer;
            relayoutResult = relayoutResult3;
            if (lp.horizontalWeight > 0.0f) {
            }
            if (lp.verticalWeight > 0.0f) {
            }
            if (measureAgain) {
            }
            layoutRequested4 = true;
            layoutRequested2 = layoutRequested4;
            didLayout = !layoutRequested2 && (!this.mStopped || this.mReportNextDraw);
            triggerGlobalLayoutListener = !didLayout || this.mAttachInfo.mRecomputeGlobalAttributes;
            if (didLayout) {
            }
            if (triggerGlobalLayoutListener) {
            }
            if (computesInternalInsets) {
            }
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
            if (!(this.mWindowAttributes != null && this.mWindowAttributes.type == 2005)) {
            }
            this.mFirst = false;
            this.mWillDrawSoon = false;
            this.mNewSurfaceNeeded = false;
            this.mActivityRelaunched = false;
            this.mViewVisibility = viewVisibility;
            this.mHadWindowFocus = hasWindowFocus;
            imTarget = WindowManager.LayoutParams.mayUseInputMethod(this.mWindowAttributes.flags);
            if (imTarget != this.mLastWasImTarget) {
            }
            boolean z6222222222222 = hasWindowFocus;
            cancelDraw = true;
            if ((relayoutResult & 2) != 0) {
            }
            cancelDraw = false;
            if (cancelDraw) {
            }
            if (isViewVisible) {
            }
            this.mIsInTraversal = false;
        }
        return;
        relayoutResult2 = false;
        frame3 = frame4;
        boolean updatedConfiguration2222222222 = updatedConfiguration;
        relayoutResult3 = relayoutResult2;
        this.mAttachInfo.mWindowLeft = frame3.left;
        this.mAttachInfo.mWindowTop = frame3.top;
        this.mWidth = frame3.width();
        this.mHeight = frame3.height();
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
        ThreadedRenderer threadedRenderer2222222222 = threadedRenderer;
        relayoutResult = relayoutResult3;
        if (lp.horizontalWeight > 0.0f) {
        }
        if (lp.verticalWeight > 0.0f) {
        }
        if (measureAgain) {
        }
        layoutRequested4 = true;
        layoutRequested2 = layoutRequested4;
        didLayout = !layoutRequested2 && (!this.mStopped || this.mReportNextDraw);
        triggerGlobalLayoutListener = !didLayout || this.mAttachInfo.mRecomputeGlobalAttributes;
        if (didLayout) {
        }
        if (triggerGlobalLayoutListener) {
        }
        if (computesInternalInsets) {
        }
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
        if (!(this.mWindowAttributes != null && this.mWindowAttributes.type == 2005)) {
        }
        this.mFirst = false;
        this.mWillDrawSoon = false;
        this.mNewSurfaceNeeded = false;
        this.mActivityRelaunched = false;
        this.mViewVisibility = viewVisibility;
        this.mHadWindowFocus = hasWindowFocus;
        imTarget = WindowManager.LayoutParams.mayUseInputMethod(this.mWindowAttributes.flags);
        if (imTarget != this.mLastWasImTarget) {
        }
        boolean z62222222222222 = hasWindowFocus;
        cancelDraw = true;
        if ((relayoutResult & 2) != 0) {
        }
        cancelDraw = false;
        if (cancelDraw) {
        }
        if (isViewVisible) {
        }
        this.mIsInTraversal = false;
    }

    /* access modifiers changed from: private */
    public void maybeHandleWindowMove(Rect frame) {
        boolean windowMoved = (this.mAttachInfo.mWindowLeft == frame.left && this.mAttachInfo.mWindowTop == frame.top) ? false : true;
        if (windowMoved) {
            if (this.mTranslator != null) {
                this.mTranslator.translateRectInScreenToAppWinFrame(frame);
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
    /* JADX WARNING: Code restructure failed: missing block: B:10:0x0013, code lost:
        profileRendering(r1);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:11:0x0017, code lost:
        if (r1 == false) goto L_0x0076;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:12:0x0019, code lost:
        ensureTouchModeLocally(r2);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:13:0x0020, code lost:
        if (r12.mAttachInfo.mThreadedRenderer == null) goto L_0x0076;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:15:0x0028, code lost:
        if (r12.mSurface.isValid() == false) goto L_0x0076;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:16:0x002a, code lost:
        r12.mFullRedrawNeeded = true;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:18:?, code lost:
        r4 = r12.mWindowAttributes;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:19:0x002e, code lost:
        if (r4 == null) goto L_0x0033;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:20:0x0030, code lost:
        r5 = r4.surfaceInsets;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:21:0x0033, code lost:
        r5 = null;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:22:0x0034, code lost:
        r12.mAttachInfo.mThreadedRenderer.initializeIfNeeded(r12.mWidth, r12.mHeight, r12.mAttachInfo, r12.mSurface, r5);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:23:0x0045, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:24:0x0046, code lost:
        android.util.Log.e(r12.mTag, "OutOfResourcesException locking surface", r0);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:27:0x0055, code lost:
        if (r12.mWindowSession.outOfMemory(r12.mWindow) == false) goto L_0x0057;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:28:0x0057, code lost:
        android.util.Slog.w(r12.mTag, "No processes killed for memory; killing self");
        android.os.Process.killProcess(android.os.Process.myPid());
     */
    /* JADX WARNING: Code restructure failed: missing block: B:30:0x0067, code lost:
        r12.mHandler.sendMessageDelayed(r12.mHandler.obtainMessage(6), 500);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:31:0x0075, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:32:0x0076, code lost:
        r12.mAttachInfo.mHasWindowFocus = r1;
        r12.mLastWasImTarget = android.view.WindowManager.LayoutParams.mayUseInputMethod(r12.mWindowAttributes.flags);
        r4 = android.view.inputmethod.InputMethodManager.peekInstance();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:33:0x0088, code lost:
        if (r4 == null) goto L_0x0099;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:35:0x008c, code lost:
        if (r12.mLastWasImTarget == false) goto L_0x0099;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:37:0x0092, code lost:
        if (isInLocalFocusMode() != false) goto L_0x0099;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:38:0x0094, code lost:
        r4.onPreWindowFocus(r12.mView, r1);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:40:0x009b, code lost:
        if (r12.mView == null) goto L_0x00bd;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:41:0x009d, code lost:
        r12.mAttachInfo.mKeyDispatchState.reset();
        r12.mView.dispatchWindowFocusChanged(r1);
        r12.mAttachInfo.mTreeObserver.dispatchOnWindowFocusChange(r1);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:42:0x00b4, code lost:
        if (r12.mAttachInfo.mTooltipHost == null) goto L_0x00bd;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:43:0x00b6, code lost:
        r12.mAttachInfo.mTooltipHost.hideTooltip();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:44:0x00bd, code lost:
        if (r1 == false) goto L_0x0114;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:45:0x00bf, code lost:
        if (r4 == null) goto L_0x00e3;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:47:0x00c3, code lost:
        if (r12.mLastWasImTarget == false) goto L_0x00e3;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:49:0x00c9, code lost:
        if (isInLocalFocusMode() != false) goto L_0x00e3;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:50:0x00cb, code lost:
        r4.onPostWindowFocus(r12.mView, r12.mView.findFocus(), r12.mWindowAttributes.softInputMode, !r12.mHasHadWindowFocus, r12.mWindowAttributes.flags);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:51:0x00e3, code lost:
        r12.mWindowAttributes.softInputMode &= -257;
        ((android.view.WindowManager.LayoutParams) r12.mView.getLayoutParams()).softInputMode &= -257;
        r12.mHasHadWindowFocus = true;
        fireAccessibilityFocusEventIfHasFocusedNode();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:52:0x010a, code lost:
        if ("com.tencent.mm".equals(r12.mContext.getPackageName()) == false) goto L_0x011b;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:53:0x010c, code lost:
        r12.mView.sendAccessibilityEvent(32);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:55:0x0116, code lost:
        if (r12.mPointerCapture == false) goto L_0x011b;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:56:0x0118, code lost:
        handlePointerCaptureChanged(false);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:57:0x011b, code lost:
        r12.mFirstInputStage.onWindowFocusChanged(r1);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:58:0x0120, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:9:0x0011, code lost:
        if (r12.mAdded == false) goto L_0x011b;
     */
    public void handleWindowFocusChanged() {
        synchronized (this) {
            if (this.mWindowFocusChanged) {
                this.mWindowFocusChanged = false;
                boolean hasWindowFocus = this.mUpcomingWindowFocus;
                boolean inTouchMode = this.mUpcomingInTouchMode;
            }
        }
    }

    private void fireAccessibilityFocusEventIfHasFocusedNode() {
        if (AccessibilityManager.getInstance(this.mContext).isEnabled()) {
            View focusedView = this.mView.findFocus();
            if (focusedView != null) {
                AccessibilityNodeProvider provider = focusedView.getAccessibilityNodeProvider();
                if (provider == null) {
                    focusedView.sendAccessibilityEvent(8);
                } else {
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
            try {
                this.mView.measure(childWidthMeasureSpec, childHeightMeasureSpec);
            } finally {
                Trace.traceEnd(8);
            }
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

    private void performLayout(WindowManager.LayoutParams lp, int desiredWindowWidth, int desiredWindowHeight) {
        View view;
        this.mLayoutRequested = false;
        this.mScrollMayChange = true;
        this.mInLayout = true;
        if (this.mView != null) {
            View host = this.mView;
            if (host != null) {
                long preLayoutTime = 0;
                if (Jlog.isPerfTest()) {
                    preLayoutTime = System.nanoTime();
                }
                Trace.traceBegin(8, "layout");
                try {
                    host.layout(0, 0, host.getMeasuredWidth(), host.getMeasuredHeight());
                    this.mInLayout = false;
                    if (this.mLayoutRequesters.size() > 0) {
                        ArrayList<View> validLayoutRequesters = getValidLayoutRequesters(this.mLayoutRequesters, false);
                        if (validLayoutRequesters != null) {
                            this.mHandlingLayoutInLayoutRequest = true;
                            int numValidRequests = validLayoutRequesters.size();
                            int i = 0;
                            while (i < numValidRequests) {
                                Log.w("View", "requestLayout() improperly called by " + view + " during layout: running second layout pass");
                                view.requestLayout();
                                i++;
                            }
                            int i2 = numValidRequests;
                            ArrayList<View> arrayList = validLayoutRequesters;
                            measureHierarchy(host, lp, this.mView.getContext().getResources(), desiredWindowWidth, desiredWindowHeight);
                            this.mInLayout = true;
                            host.layout(0, 0, host.getMeasuredWidth(), host.getMeasuredHeight());
                            this.mHandlingLayoutInLayoutRequest = false;
                            ArrayList<View> validLayoutRequesters2 = getValidLayoutRequesters(this.mLayoutRequesters, true);
                            if (validLayoutRequesters2 != null) {
                                final ArrayList<View> finalRequesters = validLayoutRequesters2;
                                getRunQueue().post(new Runnable() {
                                    public void run() {
                                        int numValidRequests = finalRequesters.size();
                                        for (int i = 0; i < numValidRequests; i++) {
                                            View view = (View) finalRequesters.get(i);
                                            Log.w("View", "requestLayout() improperly called by " + view + " during second layout pass: posting in next frame");
                                            view.requestLayout();
                                        }
                                    }
                                });
                            }
                        }
                    }
                    Trace.traceEnd(8);
                    if (Jlog.isPerfTest()) {
                        Jlog.i(2103, "#LA:" + (((System.nanoTime() - preLayoutTime) + REDUNDANT) / TimeUtils.NANOS_PER_MS));
                    }
                    this.mInLayout = false;
                } catch (Throwable th) {
                    Trace.traceEnd(8);
                    if (Jlog.isPerfTest()) {
                        Jlog.i(2103, "#LA:" + (((System.nanoTime() - preLayoutTime) + REDUNDANT) / TimeUtils.NANOS_PER_MS));
                    }
                    throw th;
                }
            }
        }
    }

    /* JADX WARNING: type inference failed for: r5v6, types: [android.view.ViewParent] */
    /* JADX WARNING: type inference failed for: r7v5, types: [android.view.ViewParent] */
    /* JADX WARNING: Multi-variable type inference failed */
    private ArrayList<View> getValidLayoutRequesters(ArrayList<View> layoutRequesters, boolean secondLayoutRequests) {
        int numViewsRequestingLayout = layoutRequesters.size();
        int i = 0;
        ArrayList<View> validLayoutRequesters = null;
        for (int i2 = 0; i2 < numViewsRequestingLayout; i2++) {
            View view = layoutRequesters.get(i2);
            if (!(view == null || view.mAttachInfo == null || view.mParent == null || (!secondLayoutRequests && (view.mPrivateFlags & 4096) != 4096))) {
                boolean gone = false;
                View parent = view;
                while (true) {
                    if (parent == null) {
                        break;
                    } else if ((parent.mViewFlags & 12) == 8) {
                        gone = true;
                        break;
                    } else if (parent.mParent instanceof View) {
                        parent = parent.mParent;
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
            while (true) {
                int i3 = i;
                if (i3 >= numViewsRequestingLayout) {
                    break;
                }
                View view2 = layoutRequesters.get(i3);
                while (view2 != null && (view2.mPrivateFlags & 4096) != 0) {
                    view2.mPrivateFlags &= -4097;
                    if (view2.mParent instanceof View) {
                        view2 = view2.mParent;
                    } else {
                        view2 = null;
                    }
                }
                i = i3 + 1;
            }
        }
        layoutRequesters.clear();
        return validLayoutRequesters;
    }

    public void requestTransparentRegion(View child) {
        checkThread();
        if (this.mView == child) {
            this.mView.mPrivateFlags |= 512;
            this.mWindowAttributesChanged = true;
            this.mWindowAttributesChangesFlag = 0;
            requestLayout();
        }
    }

    private static int getRootMeasureSpec(int windowSize, int rootDimension) {
        switch (rootDimension) {
            case -2:
                return View.MeasureSpec.makeMeasureSpec(windowSize, Integer.MIN_VALUE);
            case -1:
                return View.MeasureSpec.makeMeasureSpec(windowSize, 1073741824);
            default:
                return View.MeasureSpec.makeMeasureSpec(rootDimension, 1073741824);
        }
    }

    public void onPreDraw(DisplayListCanvas canvas) {
        if (!(this.mCurScrollY == 0 || this.mHardwareYOffset == 0 || !this.mAttachInfo.mThreadedRenderer.isOpaque())) {
            canvas.drawColor(-16777216);
        }
        canvas.translate((float) (-this.mHardwareXOffset), (float) (-this.mHardwareYOffset));
    }

    public void onPostDraw(DisplayListCanvas canvas) {
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
        if (this.mAttachInfo.mThreadedRenderer != null) {
            this.mAttachInfo.mThreadedRenderer.serializeDisplayListTree();
        }
    }

    /* access modifiers changed from: private */
    public void profileRendering(boolean enabled) {
        if (this.mProfileRendering) {
            this.mRenderProfilingEnabled = enabled;
            if (this.mRenderProfiler != null) {
                this.mChoreographer.removeFrameCallback(this.mRenderProfiler);
            }
            if (this.mRenderProfilingEnabled) {
                if (this.mRenderProfiler == null) {
                    this.mRenderProfiler = new Choreographer.FrameCallback() {
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
        if (this.mDrawsNeededToReport != 0) {
            this.mDrawsNeededToReport--;
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
            if (DEBUG_HWFLOW && this.mTag.contains("StatusBar")) {
                String str = this.mTag;
                Slog.d(str, "reportDrawFinished use:" + (System.nanoTime() - this.mSoftDrawTimeUse) + WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER + this.mBasePackageName);
            }
            this.mWindowSession.finishDrawing(this.mWindow);
        } catch (RemoteException e) {
        }
    }

    /* JADX INFO: finally extract failed */
    private void performDraw() {
        if (!mSupportAod ? this.mAttachInfo.mDisplayState == 1 : this.mAttachInfo.mDisplayState == 1 || this.mAttachInfo.mDisplayState == 4 || this.mStopped) {
            if (!this.mReportNextDraw) {
                if (mSupportAod && ((this.mAttachInfo.mDisplayState == 1 || this.mAttachInfo.mDisplayState == 4 || this.mStopped) && this.mTag != null && this.mTag.contains("[AOD]"))) {
                    Slog.d(this.mTag, "performDraw return,DisplayState:" + this.mAttachInfo.mDisplayState + ",mStopped:" + this.mStopped + ",mReportNextDraw:" + this.mReportNextDraw);
                }
                return;
            }
        }
        if (this.mView != null) {
            boolean fullRedrawNeeded = this.mFullRedrawNeeded || this.mReportNextDraw;
            this.mFullRedrawNeeded = false;
            this.mIsDrawing = true;
            long preDrawTime = System.nanoTime();
            this.mSoftDrawTimeUse = preDrawTime;
            this.mChoreographer.isNeedDraw = true;
            Trace.traceBegin(8, "draw");
            boolean usingAsyncReport = false;
            if (this.mReportNextDraw && this.mAttachInfo.mThreadedRenderer != null && this.mAttachInfo.mThreadedRenderer.isEnabled()) {
                usingAsyncReport = true;
                this.mAttachInfo.mThreadedRenderer.setFrameCompleteCallback(new ThreadedRenderer.FrameCompleteCallback() {
                    public final void onFrameComplete(long j) {
                        ViewRootImpl.this.pendingDrawFinished();
                    }
                });
            }
            try {
                boolean canUseAsync = draw(fullRedrawNeeded);
                if (usingAsyncReport && !canUseAsync) {
                    this.mAttachInfo.mThreadedRenderer.setFrameCompleteCallback(null);
                    usingAsyncReport = false;
                }
                this.mIsDrawing = false;
                if (Jlog.isPerfTest()) {
                    this.mSoftDrawTime = System.nanoTime() - preDrawTime;
                }
                Trace.traceEnd(8);
                if (this.mAttachInfo.mPendingAnimatingRenderNodes != null) {
                    int count = this.mAttachInfo.mPendingAnimatingRenderNodes.size();
                    for (int i = 0; i < count; i++) {
                        this.mAttachInfo.mPendingAnimatingRenderNodes.get(i).endAllAnimators();
                    }
                    this.mAttachInfo.mPendingAnimatingRenderNodes.clear();
                }
                if (this.mReportNextDraw != 0) {
                    this.mReportNextDraw = false;
                    if (this.mWindowDrawCountDown != null) {
                        try {
                            this.mWindowDrawCountDown.await();
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
                if (Jlog.isPerfTest() && this.mAttachInfo.mThreadedRenderer == null) {
                    Jlog.i(2104, "Soft,#DR:" + ((this.mSoftDrawTime + REDUNDANT) / TimeUtils.NANOS_PER_MS));
                }
            } catch (Throwable th) {
                this.mIsDrawing = false;
                if (Jlog.isPerfTest()) {
                    this.mSoftDrawTime = System.nanoTime() - preDrawTime;
                }
                Trace.traceEnd(8);
                throw th;
            }
        }
    }

    private boolean draw(boolean fullRedrawNeeded) {
        int curScrollY;
        boolean fullRedrawNeeded2;
        int xOffset;
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
        scrollToRectOrFocus(null, false);
        if (this.mAttachInfo.mViewScrollChanged) {
            this.mAttachInfo.mViewScrollChanged = false;
            this.mAttachInfo.mTreeObserver.dispatchOnScrollChanged();
        }
        boolean animating = this.mScroller != null && this.mScroller.computeScrollOffset();
        if (animating) {
            curScrollY = this.mScroller.getCurrY();
        } else {
            curScrollY = this.mScrollY;
        }
        int curScrollY2 = curScrollY;
        if (this.mCurScrollY != curScrollY2) {
            this.mCurScrollY = curScrollY2;
            if (this.mView instanceof RootViewSurfaceTaker) {
                this.mView.onRootViewScrollYChanged(this.mCurScrollY);
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
            if (animating && this.mScroller != null) {
                this.mScroller.abortAnimation();
            }
            return false;
        }
        if (fullRedrawNeeded2) {
            this.mAttachInfo.mIgnoreDirtyState = true;
            dirty.set(0, 0, (int) ((((float) this.mWidth) * appScale) + 0.5f), (int) ((((float) this.mHeight) * appScale) + 0.5f));
        }
        this.mAttachInfo.mTreeObserver.dispatchOnDraw();
        int xOffset2 = -this.mCanvasOffsetX;
        int yOffset = (-this.mCanvasOffsetY) + curScrollY2;
        WindowManager.LayoutParams params = this.mWindowAttributes;
        Rect surfaceInsets = params != null ? params.surfaceInsets : null;
        if (surfaceInsets != null) {
            xOffset2 -= surfaceInsets.left;
            yOffset -= surfaceInsets.top;
            dirty.offset(surfaceInsets.left, surfaceInsets.right);
        }
        int xOffset3 = xOffset2;
        int yOffset2 = yOffset;
        boolean accessibilityFocusDirty = false;
        Drawable drawable = this.mAttachInfo.mAccessibilityFocusDrawable;
        if (drawable != null) {
            Rect bounds = this.mAttachInfo.mTmpInvalRect;
            if (!getAccessibilityFocusedRect(bounds)) {
                bounds.setEmpty();
            }
            if (!bounds.equals(drawable.getBounds())) {
                accessibilityFocusDirty = true;
            }
        }
        boolean accessibilityFocusDirty2 = accessibilityFocusDirty;
        Drawable drawable2 = drawable;
        this.mAttachInfo.mDrawingTime = this.mChoreographer.getFrameTimeNanos() / TimeUtils.NANOS_PER_MS;
        boolean useAsyncReport = false;
        if (dirty.isEmpty() && !this.mIsAnimating && !accessibilityFocusDirty2) {
            int i2 = xOffset3;
            Rect rect = surfaceInsets;
            WindowManager.LayoutParams layoutParams = params;
            Rect rect2 = dirty;
            boolean z = scalingRequired;
            float f = appScale;
            Drawable drawable3 = drawable2;
        } else if (this.mAttachInfo.mThreadedRenderer == null || !this.mAttachInfo.mThreadedRenderer.isEnabled()) {
            int xOffset4 = xOffset3;
            if (this.mAttachInfo.mThreadedRenderer == null || this.mAttachInfo.mThreadedRenderer.isEnabled() || !this.mAttachInfo.mThreadedRenderer.isRequested() || !this.mSurface.isValid()) {
                Drawable drawable4 = drawable2;
                WindowManager.LayoutParams layoutParams2 = params;
                boolean z2 = scalingRequired;
                float f2 = appScale;
                if (!drawSoftware(surface, this.mAttachInfo, xOffset4, yOffset2, scalingRequired, dirty, surfaceInsets)) {
                    return false;
                }
            } else {
                try {
                    if (this.mSurface.mNativeObject != 0) {
                        try {
                            if (!this.mSurface.mSurfaceControllerIsValid) {
                                Log.w(this.mTag, "EGLdebug### draw Surface is" + this.mSurface + ",visibility " + getHostVisibility());
                            }
                        } catch (Surface.OutOfResourcesException e) {
                            e = e;
                            WindowManager.LayoutParams layoutParams3 = params;
                            handleOutOfResourcesException(e);
                            return false;
                        }
                    }
                    WindowManager.LayoutParams layoutParams4 = params;
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
                    WindowManager.LayoutParams layoutParams5 = params;
                    handleOutOfResourcesException(e);
                    return false;
                }
            }
        } else {
            boolean invalidateRoot = accessibilityFocusDirty2 || this.mInvalidateRootRequested;
            this.mInvalidateRootRequested = false;
            this.mIsAnimating = false;
            if (!(this.mHardwareYOffset == yOffset2 && this.mHardwareXOffset == xOffset3)) {
                this.mHardwareYOffset = yOffset2;
                this.mHardwareXOffset = xOffset3;
                invalidateRoot = true;
            }
            if (invalidateRoot) {
                this.mAttachInfo.mThreadedRenderer.invalidateRoot();
            }
            dirty.setEmpty();
            boolean updated = updateContentDrawBounds();
            if (this.mReportNextDraw) {
                xOffset = xOffset3;
                this.mAttachInfo.mThreadedRenderer.setStopped(false);
            } else {
                xOffset = xOffset3;
            }
            if (updated) {
                requestDrawWindow();
            }
            useAsyncReport = true;
            ThreadedRenderer.FrameDrawingCallback callback = this.mNextRtFrameCallback;
            this.mNextRtFrameCallback = null;
            boolean z3 = invalidateRoot;
            boolean z4 = updated;
            this.mAttachInfo.mThreadedRenderer.draw(this.mView, this.mAttachInfo, this, callback);
            Rect rect3 = surfaceInsets;
            WindowManager.LayoutParams layoutParams6 = params;
            Rect rect4 = dirty;
            boolean z5 = scalingRequired;
            float f3 = appScale;
            Drawable drawable5 = drawable2;
            int i3 = xOffset;
        }
        if (animating) {
            this.mFullRedrawNeeded = true;
            scheduleTraversals();
        }
        return useAsyncReport;
    }

    public boolean getDisplayInfo(DisplayInfo outDisplayInfo) {
        return this.mDisplay.getDisplayInfo(outDisplayInfo);
    }

    /* Debug info: failed to restart local var, previous not found, register: 17 */
    /* JADX WARNING: Removed duplicated region for block: B:29:0x0075 A[Catch:{ all -> 0x00ab, all -> 0x00b4 }] */
    /* JADX WARNING: Removed duplicated region for block: B:31:0x007c A[Catch:{ all -> 0x00ab, all -> 0x00b4 }] */
    /* JADX WARNING: Removed duplicated region for block: B:32:0x007f A[Catch:{ all -> 0x00ab, all -> 0x00b4 }] */
    /* JADX WARNING: Removed duplicated region for block: B:37:0x0092 A[Catch:{ all -> 0x00ab, all -> 0x00b4 }] */
    private boolean drawSoftware(Surface surface, View.AttachInfo attachInfo, int xoff, int yoff, boolean scalingRequired, Rect dirty, Rect surfaceInsets) {
        boolean z;
        Surface surface2 = surface;
        View.AttachInfo attachInfo2 = attachInfo;
        int i = xoff;
        int i2 = yoff;
        Rect rect = dirty;
        Rect rect2 = surfaceInsets;
        int dirtyXOffset = i;
        int dirtyYOffset = i2;
        if (rect2 != null) {
            dirtyXOffset += rect2.left;
            dirtyYOffset += rect2.top;
        }
        int dirtyYOffset2 = dirtyYOffset;
        int dirtyXOffset2 = dirtyXOffset;
        try {
            rect.offset(-dirtyXOffset2, -dirtyYOffset2);
            int left = rect.left;
            int top = rect.top;
            int right = rect.right;
            int bottom = rect.bottom;
            Canvas canvas = this.mSurface.lockCanvas(rect);
            if (!(left == rect.left && top == rect.top && right == rect.right && bottom == rect.bottom)) {
                attachInfo2.mIgnoreDirtyState = true;
            }
            canvas.setDensity(this.mDensity);
            rect.offset(dirtyXOffset2, dirtyYOffset2);
            Canvas canvas2 = canvas;
            try {
                if (canvas2.isOpaque() && i2 == 0) {
                    if (i == 0) {
                        z = false;
                        dirty.setEmpty();
                        this.mIsAnimating = z;
                        this.mView.mPrivateFlags |= 32;
                        canvas2.translate((float) (-i), (float) (-i2));
                        if (this.mTranslator != null) {
                            this.mTranslator.translateCanvas(canvas2);
                        }
                        canvas2.setScreenDensity(!scalingRequired ? this.mNoncompatDensity : 0);
                        attachInfo2.mSetIgnoreDirtyState = false;
                        this.mView.draw(canvas2);
                        drawAccessibilityFocusedDrawableIfNeeded(canvas2);
                        if (!attachInfo2.mSetIgnoreDirtyState) {
                            attachInfo2.mIgnoreDirtyState = false;
                        }
                        surface2.syncFrameInfo(this.mChoreographer);
                        surface2.unlockCanvasAndPost(canvas2);
                        return true;
                    }
                }
                z = false;
                canvas2.drawColor(0, PorterDuff.Mode.CLEAR);
                dirty.setEmpty();
                this.mIsAnimating = z;
                this.mView.mPrivateFlags |= 32;
                canvas2.translate((float) (-i), (float) (-i2));
                if (this.mTranslator != null) {
                }
                canvas2.setScreenDensity(!scalingRequired ? this.mNoncompatDensity : 0);
                attachInfo2.mSetIgnoreDirtyState = false;
                this.mView.draw(canvas2);
                drawAccessibilityFocusedDrawableIfNeeded(canvas2);
                if (!attachInfo2.mSetIgnoreDirtyState) {
                }
                try {
                    surface2.syncFrameInfo(this.mChoreographer);
                    surface2.unlockCanvasAndPost(canvas2);
                    return true;
                } catch (IllegalArgumentException e) {
                    Log.e(this.mTag, "Could not unlock surface", e);
                    this.mLayoutRequested = true;
                    return false;
                }
            } catch (Throwable th) {
                surface2.syncFrameInfo(this.mChoreographer);
                surface2.unlockCanvasAndPost(canvas2);
                throw th;
            }
        } catch (Surface.OutOfResourcesException e2) {
            handleOutOfResourcesException(e2);
            rect.offset(dirtyXOffset2, dirtyYOffset2);
            return false;
        } catch (IllegalArgumentException e3) {
            Log.e(this.mTag, "Could not lock surface", e3);
            this.mLayoutRequested = true;
            rect.offset(dirtyXOffset2, dirtyYOffset2);
            return false;
        } catch (Throwable th2) {
            rect.offset(dirtyXOffset2, dirtyYOffset2);
            throw th2;
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
        AccessibilityManager manager = AccessibilityManager.getInstance(this.mView.mContext);
        if (!manager.isEnabled() || !manager.isTouchExplorationEnabled()) {
            return false;
        }
        View host = this.mAccessibilityFocusedHost;
        if (host == null || host.mAttachInfo == null) {
            return false;
        }
        if (host.getAccessibilityNodeProvider() == null) {
            host.getBoundsOnScreen(bounds, true);
        } else if (this.mAccessibilityFocusedVirtualView == null) {
            return false;
        } else {
            this.mAccessibilityFocusedVirtualView.getBoundsInScreen(bounds);
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
            if (this.mView.mContext.getTheme().resolveAttribute(17891332, value, true)) {
                this.mAttachInfo.mAccessibilityFocusDrawable = this.mView.mContext.getDrawable(value.resourceId);
            }
        }
        return this.mAttachInfo.mAccessibilityFocusDrawable;
    }

    public void requestInvalidateRootRenderNode() {
        this.mInvalidateRootRequested = true;
    }

    /* access modifiers changed from: package-private */
    public boolean scrollToRectOrFocus(Rect rectangle, boolean immediate) {
        int scrollY;
        Rect ci = this.mAttachInfo.mContentInsets;
        Rect vi = this.mAttachInfo.mVisibleInsets;
        int scrollY2 = 0;
        boolean handled = false;
        if (vi.left > ci.left || vi.top > ci.top || vi.right > ci.right || vi.bottom > ci.bottom) {
            scrollY2 = this.mScrollY;
            View focus = this.mView.findFocus();
            if (focus == null) {
                return false;
            }
            View lastScrolledFocus = this.mLastScrolledFocus != null ? (View) this.mLastScrolledFocus.get() : null;
            if (focus != lastScrolledFocus) {
                rectangle = null;
            }
            if (!(focus == lastScrolledFocus && !this.mScrollMayChange && rectangle == null)) {
                this.mLastScrolledFocus = new WeakReference<>(focus);
                this.mScrollMayChange = false;
                if (focus.getGlobalVisibleRect(this.mVisRect, null)) {
                    if (rectangle == null) {
                        focus.getFocusedRect(this.mTempRect);
                        if (this.mView instanceof ViewGroup) {
                            ((ViewGroup) this.mView).offsetDescendantRectToMyCoords(focus, this.mTempRect);
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
                                scrollY2 = 0;
                            }
                            scrollY2 = scrollY;
                        }
                        handled = true;
                    }
                }
            }
        }
        if (scrollY2 != this.mScrollY) {
            if (!immediate) {
                if (this.mScroller == null) {
                    this.mScroller = new Scroller(this.mView.getContext());
                }
                this.mScroller.startScroll(0, this.mScrollY, 0, scrollY2 - this.mScrollY);
            } else if (this.mScroller != null) {
                this.mScroller.abortAnimation();
            }
            this.mScrollY = scrollY2;
        }
        return handled;
    }

    public View getAccessibilityFocusedHost() {
        return this.mAccessibilityFocusedHost;
    }

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
        if (!(this.mAccessibilityFocusedHost == null || this.mAccessibilityFocusedHost == view)) {
            this.mAccessibilityFocusedHost.clearAccessibilityFocusNoCallbacks(64);
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
    public void handlePointerCaptureChanged(boolean hasCapture) {
        if (this.mPointerCapture != hasCapture) {
            this.mPointerCapture = hasCapture;
            if (this.mView != null) {
                this.mView.dispatchPointerCaptureChanged(hasCapture);
            }
        }
    }

    public void requestChildFocus(View child, View focused) {
        checkThread();
        scheduleTraversals();
    }

    public void clearChildFocus(View child) {
        checkThread();
        scheduleTraversals();
    }

    public ViewParent getParentForAccessibility() {
        return null;
    }

    public void focusableViewAvailable(View v) {
        checkThread();
        if (this.mView == null) {
            return;
        }
        if (this.mView.hasFocus()) {
            View focused = this.mView.findFocus();
            if ((focused instanceof ViewGroup) && ((ViewGroup) focused).getDescendantFocusability() == 262144 && isViewDescendantOf(v, focused)) {
                v.requestFocus();
            }
        } else if (sAlwaysAssignFocus || !this.mAttachInfo.mInTouchMode) {
            v.requestFocus();
        }
    }

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
        this.mFirstInputStage.onDetachedFromWindow();
        if (!(this.mView == null || this.mView.mAttachInfo == null)) {
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
        this.mSurface.release();
        if (!(this.mInputQueueCallback == null || this.mInputQueue == null)) {
            this.mInputQueueCallback.onInputQueueDestroyed(this.mInputQueue);
            this.mInputQueue.dispose();
            this.mInputQueueCallback = null;
            this.mInputQueue = null;
        }
        if (this.mInputEventReceiver != null) {
            this.mInputEventReceiver.dispose();
            this.mInputEventReceiver = null;
        }
        try {
            this.mWindowSession.remove(this.mWindow);
        } catch (RemoteException e) {
        }
        if (this.mInputChannel != null) {
            this.mInputChannel.dispose();
            this.mInputChannel = null;
        }
        this.mDisplayManager.unregisterDisplayListener(this.mDisplayListener);
        unscheduleTraversals();
    }

    /* access modifiers changed from: private */
    public void performConfigurationChange(MergedConfiguration mergedConfiguration, boolean force, int newDisplayId) {
        if (mergedConfiguration != null) {
            Configuration globalConfig = mergedConfiguration.getGlobalConfiguration();
            Configuration overrideConfig = mergedConfiguration.getOverrideConfiguration();
            synchronized (sConfigCallbacks) {
                for (int i = sConfigCallbacks.size() - 1; i >= 0; i--) {
                    sConfigCallbacks.get(i).onConfigurationChanged(globalConfig);
                }
            }
            this.mLastReportedMergedConfiguration.setConfiguration(globalConfig, overrideConfig);
            this.mForceNextConfigUpdate = force;
            if (this.mActivityConfigCallback != null) {
                this.mActivityConfigCallback.onConfigurationChanged(overrideConfig, newDisplayId);
            } else {
                updateConfiguration(newDisplayId);
            }
            this.mForceNextConfigUpdate = false;
            return;
        }
        throw new IllegalArgumentException("No merged config provided.");
    }

    public void updateConfiguration(int newDisplayId) {
        if (this.mView != null) {
            Resources localResources = this.mView.getResources();
            Configuration config = localResources.getConfiguration();
            if (newDisplayId != -1) {
                onMovedToDisplay(newDisplayId, config);
            }
            if (this.mForceNextConfigUpdate || this.mLastConfigurationFromResources.diff(config) != 0) {
                int displayID = this.mDisplay.getDisplayId();
                this.mDisplay = ResourcesManager.getInstance().getAdjustedDisplay(this.mDisplay.getDisplayId(), localResources);
                if (this.mDisplay == null && this.mVrMananger.isValidVRDisplayId(displayID)) {
                    Log.w(TAG, "mDisplay is null...");
                    this.mDisplay = ResourcesManager.getInstance().getAdjustedDisplay(0, this.mView.getResources());
                } else if (this.mDisplay != null || this.mView.getContext() == null || !isValidExtDisplayId(this.mView.getContext())) {
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
                }
            }
        }
    }

    public static boolean isViewDescendantOf(View child, View parent) {
        boolean z = true;
        if (child == parent) {
            return true;
        }
        ViewParent theParent = child.getParent();
        if (!(theParent instanceof ViewGroup) || !isViewDescendantOf((View) theParent, parent)) {
            z = false;
        }
        return z;
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
    public void jank_removeInvalidNode(long time) {
        int end = 0;
        int index = -1;
        int start = sJankList.size();
        if (start == 0) {
            Log.i(TAG, "jank_removeInvalidNode jank list is null");
        } else if (((Long) sJankList.get(start - 1).first).longValue() < time) {
            Log.i(TAG, "jank_removeInvalidNode all the node in jank list is out of time");
            sJankList.clear();
        } else {
            while (true) {
                if (start <= end) {
                    break;
                }
                int middle = (start + end) / 2;
                if (((Long) sJankList.get(middle).first).longValue() >= time) {
                    start = middle;
                } else if (middle == start - 1) {
                    index = middle;
                    break;
                } else if (((Long) sJankList.get(middle + 1).first).longValue() >= time) {
                    index = middle;
                    break;
                } else {
                    end = middle;
                }
            }
            if (index == 0) {
                sJankList.remove(0);
            } else if (index > 0) {
                sJankList.subList(0, index + 1).clear();
            }
        }
    }

    /* access modifiers changed from: package-private */
    public int jank_getTotalJankNum() {
        int totalCount = 0;
        int size = sJankList.size();
        for (int i = 0; i < size; i++) {
            totalCount = (int) (((long) totalCount) + ((Long) sJankList.get(i).second).longValue());
        }
        return totalCount;
    }

    /* access modifiers changed from: package-private */
    public void jank_newcheckSkippedFrame(long nowtime, long frameVsynctime) {
        long skippedFrames;
        long j = nowtime;
        long j2 = frameVsynctime;
        boolean bLastTraversal = this.mChoreographer.isLastTraversal();
        long LastSkippedFrameEnd = this.mChoreographer.getLastSkippedFrameEndTime();
        if (bLastTraversal) {
            if (LastSkippedFrameEnd > j2) {
                skippedFrames = (j - LastSkippedFrameEnd) / VSYNC_SPAN;
            } else {
                skippedFrames = (j - j2) / VSYNC_SPAN;
            }
            long skippedFrames2 = skippedFrames;
            if (skippedFrames2 >= 5) {
                Trace.traceBegin(8, "jank_event_sync: start_ts=" + j2 + ",end_ts=" + j + ", appid=" + Process.myPid());
                Trace.traceEnd(8);
                StringBuilder sb = new StringBuilder();
                sb.append("#P:");
                sb.append(this.mWindowAttributes.getTitle());
                sb.append("#SK:");
                sb.append(skippedFrames2);
                sb.append("#FRT:");
                boolean z = bLastTraversal;
                sb.append(j2 / 10000);
                sb.append("#DNT:");
                sb.append(j / 10000);
                Jlog.d(37, sb.toString());
                this.mChoreographer.setLastSkippedFrameEndTime(j);
            } else {
                boolean z2 = bLastTraversal;
            }
            HwFrameworkFactory.getHwAppInnerBoostImpl().onJitter(skippedFrames2);
            if (skippedFrames2 >= 15) {
                sJankList.clear();
            } else if (skippedFrames2 >= 1 && skippedFrames2 < 15) {
                long time = j - Jlog.CONSECU_JANK_WINDOW;
                Pair<Long, Long> node = new Pair<>(Long.valueOf(nowtime), Long.valueOf(skippedFrames2));
                jank_removeInvalidNode(time);
                sJankList.add(node);
                long totalJankNum = (long) jank_getTotalJankNum();
                if (totalJankNum >= 90) {
                    StringBuilder sb2 = new StringBuilder();
                    sb2.append("#P:");
                    sb2.append(this.mWindowAttributes.getTitle());
                    sb2.append("#SK:");
                    sb2.append(totalJankNum);
                    sb2.append("#FRT:");
                    long j3 = time;
                    sb2.append(j2 / 10000);
                    sb2.append("#DNT:");
                    sb2.append(j / 10000);
                    Jlog.d(362, sb2.toString());
                    sJankList.clear();
                }
            }
            this.mChoreographer.checkTounchResponseTime(this.mWindowAttributes.getTitle(), j);
            ScrollerBoostManager.getInstance().updateFrameJankInfo(skippedFrames2);
        }
    }

    /* access modifiers changed from: package-private */
    /* JADX WARNING: Removed duplicated region for block: B:43:0x00bc  */
    /* JADX WARNING: Removed duplicated region for block: B:45:0x00c0  */
    public void jank_processAfterTraversal(boolean scroll) {
        boolean bContinuousUpdate;
        int skip;
        int skip2;
        String msg;
        long now = System.nanoTime();
        long delaytime = this.mChoreographer.getDoFrameDelayTime();
        long lastFrameDoneTime = this.mChoreographer.getLastFrameDoneTime();
        long frameTime = this.mChoreographer.getRealFrameTime();
        boolean bOntime = delaytime < 1;
        jank_newcheckSkippedFrame(now, frameTime);
        if (bOntime) {
            bContinuousUpdate = frameTime - lastFrameDoneTime < VSYNC_SPAN;
        } else {
            bContinuousUpdate = lastFrameDoneTime > frameTime;
        }
        if (!bOntime || now - frameTime >= VSYNC_SPAN - 2500000) {
            if (scroll && frameTime - lastFrameDoneTime < 1000) {
                bContinuousUpdate = true;
            }
            if (bContinuousUpdate) {
                long lastSFSyncTime = 0;
                if (bOntime) {
                    if (frameTime - lastFrameDoneTime > 2500000) {
                        lastSFSyncTime = (frameTime - 2500000) - VSYNC_SPAN;
                    } else {
                        lastSFSyncTime = frameTime - 2500000;
                    }
                } else if (frameTime < lastFrameDoneTime) {
                    long j = delaytime;
                    lastSFSyncTime = (lastFrameDoneTime - ((lastFrameDoneTime - frameTime) - (((long) ((int) ((lastFrameDoneTime - frameTime) / VSYNC_SPAN))) * VSYNC_SPAN))) - 2500000;
                } else {
                    if (scroll) {
                        boolean z = bOntime;
                        lastSFSyncTime = (lastFrameDoneTime - ((frameTime - lastFrameDoneTime) - (((long) ((int) ((frameTime - lastFrameDoneTime) / VSYNC_SPAN))) * VSYNC_SPAN))) - 2500000;
                    } else {
                        boolean z2 = bOntime;
                    }
                    skip = (int) (((now - lastSFSyncTime) - REDUNDANT) / VSYNC_SPAN);
                    if (skip != 0) {
                        this.lastFrameDefer = 1;
                        return;
                    } else if (skip != 1) {
                        if (skip > 1) {
                            this.lastFrameDefer = 0;
                            skip2 = (skip - 1) - this.lastFrameDefer;
                        } else {
                            skip2 = skip;
                        }
                        if (skip2 >= 5) {
                            String msg2 = "#P:" + this.mWindowAttributes.getTitle() + "#SK:" + skip2;
                            if (this.mAttachInfo.mThreadedRenderer != null) {
                                msg = msg2 + "#IP:" + (this.mDeliverInputTime / 10000) + "#DR:" + (this.mAttachInfo.mThreadedRenderer.getJankDrawData(0) / 10000) + "#PRO:" + (this.mAttachInfo.mThreadedRenderer.getJankDrawData(1) / 10000) + "#EX:" + (this.mAttachInfo.mThreadedRenderer.getJankDrawData(2) / 10000) + "#TRA:" + (this.mAttachInfo.mThreadedRenderer.getJankDrawData(3) / 10000);
                            } else {
                                msg = msg2 + "#IP:" + (this.mDeliverInputTime / 10000) + "#DR:" + 0 + "#PRO:" + 0 + "#EX:" + 0 + "#TRA:" + (this.mSoftDrawTime / 10000);
                            }
                            Jlog.d(67, msg + "#FRT:" + (frameTime / 10000) + "#DNT:" + (now / 10000) + "#LFT:" + (lastFrameDoneTime / 10000));
                        }
                    } else {
                        return;
                    }
                }
                skip = (int) (((now - lastSFSyncTime) - REDUNDANT) / VSYNC_SPAN);
                if (skip != 0) {
                }
            } else {
                boolean z3 = bOntime;
            }
            return;
        }
        this.lastFrameDefer = 0;
    }

    /* access modifiers changed from: package-private */
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
        this.mAttachInfo.mInTouchMode = inTouchMode;
        this.mAttachInfo.mTreeObserver.dispatchOnTouchModeChanged(inTouchMode);
        return inTouchMode ? enterTouchMode() : leaveTouchMode();
    }

    private boolean enterTouchMode() {
        if (this.mView != null && this.mView.hasFocus()) {
            View focused = this.mView.findFocus();
            if (focused != null && !focused.isFocusableInTouchMode()) {
                ViewGroup ancestorToTakeFocus = findAncestorToTakeFocusInTouchMode(focused);
                if (ancestorToTakeFocus != null) {
                    return ancestorToTakeFocus.requestFocus();
                }
                focused.clearFocusInternal(null, true, false);
                return true;
            }
        }
        return false;
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
        if (this.mView == null) {
            return false;
        }
        if (this.mView.hasFocus()) {
            View focusedView = this.mView.findFocus();
            if (!(focusedView instanceof ViewGroup) || ((ViewGroup) focusedView).getDescendantFocusability() != 262144) {
                return false;
            }
        }
        return this.mView.restoreDefaultFocus();
    }

    /* access modifiers changed from: private */
    public void resetPointerIcon(MotionEvent event) {
        this.mPointerIconType = 1;
        updatePointerIcon(event);
    }

    /* access modifiers changed from: private */
    public boolean updatePointerIcon(MotionEvent event) {
        float x = event.getX(0);
        float y = event.getY(0);
        if (this.mView == null) {
            Slog.d(this.mTag, "updatePointerIcon called after view was removed");
            return false;
        } else if (x < 0.0f || x >= ((float) this.mView.getWidth()) || y < 0.0f || y >= ((float) this.mView.getHeight())) {
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
    public void maybeUpdateTooltip(MotionEvent event) {
        if (event.getPointerCount() == 1) {
            int action = event.getActionMasked();
            if (action == 9 || action == 7 || action == 10) {
                AccessibilityManager manager = AccessibilityManager.getInstance(this.mContext);
                if (manager.isEnabled() && manager.isTouchExplorationEnabled()) {
                    return;
                }
                if (this.mView == null) {
                    Slog.d(this.mTag, "maybeUpdateTooltip called after view was removed");
                } else {
                    this.mView.dispatchTooltipHoverEvent(event);
                }
            }
        }
    }

    private static boolean isNavigationKey(KeyEvent keyEvent) {
        switch (keyEvent.getKeyCode()) {
            case 19:
            case 20:
            case 21:
            case 22:
            case 23:
            case 61:
            case 62:
            case 66:
            case 92:
            case 93:
            case 122:
            case 123:
                return true;
            default:
                return false;
        }
    }

    private static boolean isTypingKey(KeyEvent keyEvent) {
        return keyEvent.getUnicodeChar() > 0;
    }

    /* access modifiers changed from: private */
    public boolean checkForLeavingTouchModeAndConsume(KeyEvent event) {
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
    public void setLocalDragState(Object obj) {
        this.mLocalDragState = obj;
    }

    /* access modifiers changed from: private */
    public void handleDragEvent(DragEvent event) {
        if (this.mView != null && this.mAdded) {
            int what = event.mAction;
            if (what == 1) {
                this.mCurrentDragView = null;
                this.mDragDescription = event.mClipDescription;
            } else {
                if (what == 4) {
                    this.mDragDescription = null;
                }
                event.mClipDescription = this.mDragDescription;
            }
            if (what == 6) {
                if (View.sCascadedDragDrop) {
                    this.mView.dispatchDragEnterExitInPreN(event);
                }
                setDragFocus(null, event);
            } else {
                if (what == 2 || what == 3) {
                    this.mDragPoint.set(event.mX, event.mY);
                    if (this.mTranslator != null) {
                        this.mTranslator.translatePointInScreenToAppWindow(this.mDragPoint);
                    }
                    if (this.mCurScrollY != 0) {
                        this.mDragPoint.offset(0.0f, (float) this.mCurScrollY);
                    }
                    event.mX = this.mDragPoint.x;
                    event.mY = this.mDragPoint.y;
                }
                View prevDragView = this.mCurrentDragView;
                if (what == 3 && event.mClipData != null) {
                    event.mClipData.prepareToEnterProcess();
                }
                boolean result = this.mView.dispatchDragEvent(event);
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
                    this.mAttachInfo.mDragToken = null;
                    if (this.mAttachInfo.mDragSurface != null) {
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
        if (this.mView != null) {
            this.mView.requestKeyboardShortcuts(list, deviceId);
        }
        data.putParcelableArrayList(WindowManager.PARCEL_KEY_SHORTCUTS_ARRAY, list);
        try {
            receiver.send(0, data);
        } catch (RemoteException e) {
        }
    }

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
            if (this.mCurrentDragView != null) {
                event.mAction = 6;
                this.mCurrentDragView.callDragEventHandler(event);
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
        if (this.mView != null) {
            if (this.mAudioManager == null) {
                this.mAudioManager = (AudioManager) this.mView.getContext().getSystemService("audio");
            }
            return this.mAudioManager;
        }
        throw new IllegalStateException("getAudioManager called when there is no mView");
    }

    /* access modifiers changed from: private */
    public AutofillManager getAutofillManager() {
        if (this.mView instanceof ViewGroup) {
            ViewGroup decorView = (ViewGroup) this.mView;
            if (decorView.getChildCount() > 0) {
                return (AutofillManager) decorView.getChildAt(0).getContext().getSystemService(AutofillManager.class);
            }
        }
        return null;
    }

    /* access modifiers changed from: private */
    public boolean isAutofillUiShowing() {
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
        WindowManager.LayoutParams layoutParams = params;
        float appScale = this.mAttachInfo.mApplicationScale;
        boolean restore = false;
        if (!(layoutParams == null || this.mTranslator == null)) {
            restore = true;
            params.backup();
            this.mTranslator.translateWindowLayout(layoutParams);
        }
        boolean restore2 = restore;
        if (!(layoutParams == null || this.mOrigWindowType == layoutParams.type || this.mTargetSdkVersion >= 14)) {
            String str = this.mTag;
            Slog.w(str, "Window type can not be changed after the window is added; ignoring change of " + this.mView);
            layoutParams.type = this.mOrigWindowType;
        }
        long frameNumber = -1;
        if (this.mSurface.isValid()) {
            frameNumber = this.mSurface.getNextFrameNumber();
        }
        long frameNumber2 = frameNumber;
        IWindowSession iWindowSession = this.mWindowSession;
        W w = this.mWindow;
        int i = this.mSeq;
        int measuredWidth = (int) ((((float) this.mView.getMeasuredWidth()) * appScale) + 0.5f);
        int measuredHeight = (int) ((((float) this.mView.getMeasuredHeight()) * appScale) + 0.5f);
        Rect rect = this.mWinFrame;
        Rect rect2 = this.mPendingOverscanInsets;
        Rect rect3 = this.mPendingContentInsets;
        Rect rect4 = this.mPendingVisibleInsets;
        Rect rect5 = this.mPendingStableInsets;
        Rect rect6 = this.mPendingOutsets;
        Rect rect7 = this.mPendingBackDropFrame;
        DisplayCutout.ParcelableWrapper parcelableWrapper = this.mPendingDisplayCutout;
        float f = appScale;
        MergedConfiguration mergedConfiguration = this.mPendingMergedConfiguration;
        Rect rect8 = rect6;
        Rect rect9 = rect4;
        Surface surface = this.mSurface;
        int i2 = insetsPending ? 1 : 0;
        int relayoutResult = iWindowSession.relayout(w, i, layoutParams, measuredWidth, measuredHeight, viewVisibility, i2, frameNumber2, rect, rect2, rect3, rect9, rect5, rect8, rect7, parcelableWrapper, mergedConfiguration, surface);
        this.mPendingAlwaysConsumeNavBar = (relayoutResult & 64) != 0;
        if (restore2) {
            params.restore();
        }
        if (this.mTranslator != null) {
            this.mTranslator.translateRectInScreenToAppWinFrame(this.mWinFrame);
            this.mTranslator.translateRectInScreenToAppWindow(this.mPendingOverscanInsets);
            this.mTranslator.translateRectInScreenToAppWindow(this.mPendingContentInsets);
            this.mTranslator.translateRectInScreenToAppWindow(this.mPendingVisibleInsets);
            this.mTranslator.translateRectInScreenToAppWindow(this.mPendingStableInsets);
        }
        return relayoutResult;
    }

    public void playSoundEffect(int effectId) {
        checkThread();
        try {
            AudioManager audioManager = getAudioManager();
            switch (effectId) {
                case 0:
                    audioManager.playSoundEffect(0);
                    return;
                case 1:
                    audioManager.playSoundEffect(3);
                    return;
                case 2:
                    audioManager.playSoundEffect(1);
                    return;
                case 3:
                    audioManager.playSoundEffect(4);
                    return;
                case 4:
                    audioManager.playSoundEffect(2);
                    return;
                default:
                    throw new IllegalArgumentException("unknown effect id " + effectId + " not defined in " + SoundEffectConstants.class.getCanonicalName());
            }
        } catch (IllegalStateException e) {
            String str = this.mTag;
            Log.e(str, "FATAL EXCEPTION when attempting to play sound effect: " + e);
            e.printStackTrace();
        }
    }

    public boolean performHapticFeedback(int effectId, boolean always) {
        try {
            return this.mWindowSession.performHapticFeedback(this.mWindow, effectId, always);
        } catch (RemoteException e) {
            return false;
        }
    }

    public View focusSearch(View focused, int direction) {
        checkThread();
        if (!(this.mView instanceof ViewGroup)) {
            return null;
        }
        return FocusFinder.getInstance().findNextFocus((ViewGroup) this.mView, focused, direction);
    }

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
        writer.print(prefix);
        writer.println("View Hierarchy:");
        dumpViewHierarchy(innerPrefix, writer, this.mView);
    }

    private void dumpViewHierarchy(String prefix, PrintWriter writer, View view) {
        writer.print(prefix);
        if (view == null) {
            writer.println("null");
            return;
        }
        writer.println(view.toString());
        if (view instanceof ViewGroup) {
            ViewGroup grp = (ViewGroup) view;
            int N = grp.getChildCount();
            if (N > 0) {
                String prefix2 = prefix + "  ";
                for (int i = 0; i < N; i++) {
                    dumpViewHierarchy(prefix2, writer, grp.getChildAt(i));
                }
            }
        }
    }

    public void dumpGfxInfo(int[] info) {
        info[1] = 0;
        info[0] = 0;
        if (this.mView != null) {
            getGfxInfo(this.mView, info);
        }
    }

    private static void getGfxInfo(View view, int[] info) {
        RenderNode renderNode = view.mRenderNode;
        info[0] = info[0] + 1;
        if (renderNode != null) {
            info[1] = info[1] + renderNode.getDebugSize();
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
            if (!this.mIsDrawing) {
                destroyHardwareRenderer();
            } else {
                String str = this.mTag;
                Log.e(str, "Attempting to destroy the window while drawing!\n  window=" + this + ", title=" + this.mWindowAttributes.getTitle());
            }
            if (this.mWindowAttributes != null && this.mWindowAttributes.getTitle().toString().contains("Emui:ProximityWnd")) {
                Log.d(TAG, "MSG_DIE send");
            }
            this.mHandler.sendEmptyMessage(3);
            return true;
        }
        doDie();
        return false;
    }

    /* access modifiers changed from: package-private */
    public void doDie() {
        boolean viewVisibilityChanged = true;
        if (this.mView != null && this.mView.isTouchableInOtherThread()) {
            Log.w(this.mTag, "doDie in " + this + ",CREATE IN " + this.mThread + ",DIE IN " + Thread.currentThread());
        } else {
            checkThread();
        }
        if (this.mWindowAttributes != null && this.mWindowAttributes.getTitle().toString().contains("Emui:ProximityWnd")) {
            Log.d(TAG, "mRemoved = " + this.mRemoved + " mAdded = " + this.mAdded);
        }
        synchronized (this) {
            if (!this.mRemoved) {
                this.mRemoved = true;
                if (this.mAdded) {
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
                        this.mSurface.release();
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
            public void run() {
                boolean unused = ViewRootImpl.this.mAllocBufferAsync = SystemProperties.getBoolean(ViewRootImpl.PROPERTY_ALLOC_BUFFER_SYNC, false);
                boolean unused2 = ViewRootImpl.this.mProfileRendering = SystemProperties.getBoolean(ViewRootImpl.PROPERTY_PROFILE_RENDERING, false);
                ViewRootImpl.this.profileRendering(ViewRootImpl.this.mAttachInfo.mHasWindowFocus);
                if (ViewRootImpl.this.mAttachInfo.mThreadedRenderer != null && ViewRootImpl.this.mAttachInfo.mThreadedRenderer.loadSystemProperties()) {
                    ViewRootImpl.this.invalidate();
                }
                boolean layout = SystemProperties.getBoolean(View.DEBUG_LAYOUT_PROPERTY, false);
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
            if (this.mView != null) {
                hardwareRenderer.destroyHardwareResources(this.mView);
            }
            hardwareRenderer.destroy();
            hardwareRenderer.setRequested(false);
            this.mAttachInfo.mThreadedRenderer = null;
            this.mAttachInfo.mHardwareAccelerated = false;
        }
    }

    /* access modifiers changed from: private */
    public void dispatchResized(Rect frame, Rect overscanInsets, Rect contentInsets, Rect visibleInsets, Rect stableInsets, Rect outsets, boolean reportDraw, MergedConfiguration mergedConfiguration, Rect backDropFrame, boolean forceLayout, boolean alwaysConsumeNavBar, int displayId, DisplayCutout.ParcelableWrapper displayCutout) {
        Rect rect = frame;
        Rect rect2 = overscanInsets;
        Rect rect3 = contentInsets;
        Rect rect4 = visibleInsets;
        Rect rect5 = stableInsets;
        MergedConfiguration mergedConfiguration2 = mergedConfiguration;
        Rect rect6 = backDropFrame;
        boolean sameProcessCall = true;
        if (this.mDragResizing && this.mUseMTRenderer) {
            boolean fullscreen = rect.equals(rect6);
            synchronized (this.mWindowCallbacks) {
                for (int i = this.mWindowCallbacks.size() - 1; i >= 0; i--) {
                    this.mWindowCallbacks.get(i).onWindowSizeIsChanging(rect6, fullscreen, rect4, rect5);
                }
            }
        }
        Message msg = this.mHandler.obtainMessage(reportDraw ? 5 : 4);
        if (DEBUG_HWFLOW && reportDraw && this.mTag.contains("StatusBar")) {
            Slog.d(this.mTag, "dispatchResized " + msg);
        }
        if (this.mTranslator != null) {
            this.mTranslator.translateRectInScreenToAppWindow(rect);
            this.mTranslator.translateRectInScreenToAppWindow(rect2);
            this.mTranslator.translateRectInScreenToAppWindow(rect3);
            this.mTranslator.translateRectInScreenToAppWindow(rect4);
        }
        SomeArgs args = SomeArgs.obtain();
        if (Binder.getCallingPid() != Process.myPid()) {
            sameProcessCall = false;
        }
        args.arg1 = sameProcessCall ? new Rect(rect) : rect;
        args.arg2 = sameProcessCall ? new Rect(rect3) : rect3;
        args.arg3 = sameProcessCall ? new Rect(rect4) : rect4;
        args.arg4 = (!sameProcessCall || mergedConfiguration2 == null) ? mergedConfiguration2 : new MergedConfiguration(mergedConfiguration2);
        args.arg5 = sameProcessCall ? new Rect(rect2) : rect2;
        args.arg6 = sameProcessCall ? new Rect(rect5) : rect5;
        args.arg7 = sameProcessCall ? new Rect(outsets) : outsets;
        args.arg8 = sameProcessCall ? new Rect(rect6) : rect6;
        args.arg9 = displayCutout.get();
        args.argi1 = forceLayout ? 1 : 0;
        args.argi2 = alwaysConsumeNavBar ? 1 : 0;
        args.argi3 = displayId;
        msg.obj = args;
        this.mHandler.sendMessage(msg);
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
        if (this.mQueuedInputEventPoolSize < 10) {
            this.mQueuedInputEventPoolSize++;
            q.mNext = this.mQueuedInputEventPool;
            this.mQueuedInputEventPool = q;
        }
    }

    /* access modifiers changed from: package-private */
    public void enqueueInputEvent(InputEvent event) {
        enqueueInputEvent(event, null, 0, false);
    }

    /* access modifiers changed from: package-private */
    public void enqueueInputEvent(InputEvent event, InputEventReceiver receiver, int flags, boolean processImmediately) {
        adjustInputEventForCompatibility(event);
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
                this.mChoreographer.updateOldestInputTime(oldestEventTime);
            }
            this.mChoreographer.mFrameInfo.updateInputEventTime(eventTime, oldestEventTime);
            deliverInputEvent(q);
        }
        if (this.mProcessInputEventsScheduled) {
            this.mProcessInputEventsScheduled = false;
            this.mHandler.removeMessages(19);
        }
    }

    private void deliverInputEvent(QueuedInputEvent q) {
        InputStage stage;
        Trace.asyncTraceBegin(8, "deliverInputEvent", q.mEvent.getSequenceNumber());
        if (this.mInputEventConsistencyVerifier != null) {
            this.mInputEventConsistencyVerifier.onInputEvent(q.mEvent, 0);
        }
        HwFrameworkFactory.getHwAppInnerBoostImpl().onInputEvent(q.mEvent);
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
        this.mChoreographer.checkOldestInputTime();
    }

    /* access modifiers changed from: private */
    public boolean isValidExtDisplayId(int displayId) {
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
    public void finishInputEvent(QueuedInputEvent q) {
        Trace.asyncTraceEnd(8, "deliverInputEvent", q.mEvent.getSequenceNumber());
        if (this.mDisplay != null && isValidExtDisplayId(this.mDisplay.getDisplayId()) && (q.mEvent instanceof MotionEvent)) {
            ((MotionEvent) q.mEvent).setOffset(null);
        }
        if (q.mReceiver != null) {
            q.mReceiver.finishInputEvent(q.mEvent, (q.mFlags & 8) != 0);
        } else {
            q.mEvent.recycleIfNeededAfterDispatch();
        }
        recycleQueuedInputEvent(q);
    }

    private void adjustInputEventForCompatibility(InputEvent e) {
        if (this.mTargetSdkVersion < 23 && (e instanceof MotionEvent)) {
            MotionEvent motion = (MotionEvent) e;
            int buttonState = motion.getButtonState();
            int compatButtonState = (buttonState & 96) >> 4;
            if (compatButtonState != 0) {
                motion.setButtonState(buttonState | compatButtonState);
            }
        }
    }

    static boolean isTerminalInputEvent(InputEvent event) {
        boolean z = false;
        if (event instanceof KeyEvent) {
            if (((KeyEvent) event).getAction() == 1) {
                z = true;
            }
            return z;
        }
        int action = ((MotionEvent) event).getAction();
        if (action == 1 || action == 3 || action == 10) {
            z = true;
        }
        return z;
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
            if (!(this.mInputEventReceiver == null || !this.mInputEventReceiver.consumeBatchedInputEvents(frameTimeNanos) || frameTimeNanos == -1)) {
                scheduleConsumeBatchedInput();
            }
            doProcessInputEvents();
        }
    }

    public boolean peekEvent() {
        if (this.mInputEventReceiver != null) {
            return this.mInputEventReceiver.peekEvent();
        }
        return false;
    }

    public void dispatchInvalidateDelayed(View view, long delayMilliseconds) {
        Message msg = this.mHandler.obtainMessage(1, view);
        msg.setAsynchronous(true);
        this.mHandler.sendMessageDelayed(msg, delayMilliseconds);
    }

    public void dispatchInvalidateRectDelayed(View.AttachInfo.InvalidateInfo info, long delayMilliseconds) {
        Message msg = this.mHandler.obtainMessage(2, info);
        msg.setAsynchronous(true);
        this.mHandler.sendMessageDelayed(msg, delayMilliseconds);
    }

    public void dispatchInvalidateOnAnimation(View view) {
        this.mInvalidateOnAnimationRunnable.addView(view);
    }

    public void dispatchInvalidateRectOnAnimation(View.AttachInfo.InvalidateInfo info) {
        this.mInvalidateOnAnimationRunnable.addViewRect(info);
    }

    public void cancelInvalidate(View view) {
        this.mHandler.removeMessages(1, view);
        this.mHandler.removeMessages(2, view);
        this.mInvalidateOnAnimationRunnable.removeView(view);
    }

    public void dispatchInputEvent(InputEvent event) {
        dispatchInputEvent(event, null);
    }

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

    public void dispatchUnhandledInputEvent(InputEvent event) {
        if (event instanceof MotionEvent) {
            event = MotionEvent.obtain((MotionEvent) event);
        }
        synthesizeInputEvent(event);
    }

    public void dispatchAppVisibility(boolean visible) {
        Message msg = this.mHandler.obtainMessage(8);
        msg.arg1 = visible;
        this.mHandler.sendMessage(msg);
    }

    public void dispatchGetNewSurface() {
        this.mHandler.sendMessage(this.mHandler.obtainMessage(9));
    }

    public void updateSurfaceStatus(boolean status) {
        this.mSurface.setSurfaceControllerState(status);
    }

    public boolean surfaceControlllerIsValid() {
        if (this.mSurface.mNativeObject == 0) {
            return false;
        }
        if (this.mSurface.mNativeObject == 0 || this.mSurface.mSurfaceControllerIsValid) {
            return true;
        }
        return false;
    }

    public void windowFocusChanged(boolean hasFocus, boolean inTouchMode) {
        synchronized (this) {
            this.mWindowFocusChanged = true;
            this.mUpcomingWindowFocus = hasFocus;
            this.mUpcomingInTouchMode = inTouchMode;
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

    public void dispatchDragEvent(DragEvent event) {
        int what;
        if (event.getAction() == 2) {
            what = 16;
            this.mHandler.removeMessages(16);
        } else {
            what = 15;
        }
        this.mHandler.sendMessage(this.mHandler.obtainMessage(what, event));
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
        this.mHandler.sendMessage(this.mHandler.obtainMessage(17, args));
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
        msg.arg1 = on;
        this.mHandler.sendMessage(msg);
    }

    private void postSendWindowContentChangedCallback(View source, int changeType) {
        if (this.mSendWindowContentChangedAccessibilityEvent == null) {
            this.mSendWindowContentChangedAccessibilityEvent = new SendWindowContentChangedAccessibilityEvent();
        }
        this.mSendWindowContentChangedAccessibilityEvent.runOrPost(source, changeType);
    }

    private void removeSendWindowContentChangedCallback() {
        if (this.mSendWindowContentChangedAccessibilityEvent != null) {
            this.mHandler.removeCallbacks(this.mSendWindowContentChangedAccessibilityEvent);
        }
    }

    public boolean showContextMenuForChild(View originalView) {
        return false;
    }

    public boolean showContextMenuForChild(View originalView, float x, float y) {
        return false;
    }

    public ActionMode startActionModeForChild(View originalView, ActionMode.Callback callback) {
        return null;
    }

    public ActionMode startActionModeForChild(View originalView, ActionMode.Callback callback, int type) {
        return null;
    }

    public void createContextMenu(ContextMenu menu) {
    }

    public void childDrawableStateChanged(View child) {
    }

    public boolean requestSendAccessibilityEvent(View child, AccessibilityEvent event) {
        if (this.mView == null || this.mStopped || this.mPausedForTransition) {
            return false;
        }
        if (!(event.getEventType() == 2048 || this.mSendWindowContentChangedAccessibilityEvent == null || this.mSendWindowContentChangedAccessibilityEvent.mSource == null)) {
            this.mSendWindowContentChangedAccessibilityEvent.removeCallbacksAndRun();
        }
        int eventType = event.getEventType();
        if (eventType == 2048) {
            handleWindowContentChangedEvent(event);
        } else if (eventType == 32768) {
            long sourceNodeId = event.getSourceNodeId();
            View source = this.mView.findViewByAccessibilityId(AccessibilityNodeInfo.getAccessibilityViewId(sourceNodeId));
            if (source != null) {
                AccessibilityNodeProvider provider = source.getAccessibilityNodeProvider();
                if (provider != null) {
                    setAccessibilityFocus(source, provider.createAccessibilityNodeInfo(AccessibilityNodeInfo.getVirtualDescendantId(sourceNodeId)));
                }
            }
        } else if (eventType == 65536) {
            View source2 = this.mView.findViewByAccessibilityId(AccessibilityNodeInfo.getAccessibilityViewId(event.getSourceNodeId()));
            if (!(source2 == null || source2.getAccessibilityNodeProvider() == null)) {
                setAccessibilityFocus(null, null);
            }
        }
        if (this.mAccessibilityManager.isEnabled()) {
            try {
                this.mAccessibilityManager.sendAccessibilityEvent(event);
            } catch (IllegalStateException e) {
                Log.e(TAG, e.toString());
            }
        }
        return true;
    }

    /* JADX WARNING: type inference failed for: r11v3, types: [android.view.ViewParent] */
    /* JADX WARNING: Multi-variable type inference failed */
    /* JADX WARNING: Unknown variable types count: 1 */
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
                        ? root2 = root.getParent();
                        if (root2 instanceof View) {
                            root = root2;
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
                    if (this.mAccessibilityFocusedVirtualView == null) {
                        this.mAccessibilityFocusedHost = null;
                        focusedHost.clearAccessibilityFocusNoCallbacks(0);
                        provider.performAction(focusedChildId, AccessibilityNodeInfo.AccessibilityAction.ACTION_CLEAR_ACCESSIBILITY_FOCUS.getId(), null);
                        invalidateRectOnScreen(oldBounds);
                    } else {
                        Rect newBounds = this.mAccessibilityFocusedVirtualView.getBoundsInScreen();
                        if (!oldBounds.equals(newBounds)) {
                            oldBounds.union(newBounds);
                            invalidateRectOnScreen(oldBounds);
                        }
                    }
                }
            }
        }
    }

    public void notifySubtreeAccessibilityStateChanged(View child, View source, int changeType) {
        postSendWindowContentChangedCallback((View) Preconditions.checkNotNull(source), changeType);
    }

    public boolean canResolveLayoutDirection() {
        return true;
    }

    public boolean isLayoutDirectionResolved() {
        return true;
    }

    public int getLayoutDirection() {
        return 0;
    }

    public boolean canResolveTextDirection() {
        return true;
    }

    public boolean isTextDirectionResolved() {
        return true;
    }

    public int getTextDirection() {
        return 1;
    }

    public boolean canResolveTextAlignment() {
        return true;
    }

    public boolean isTextAlignmentResolved() {
        return true;
    }

    public int getTextAlignment() {
        return 1;
    }

    /* JADX WARNING: type inference failed for: r3v2, types: [android.view.ViewParent] */
    /* JADX WARNING: type inference failed for: r2v6, types: [android.view.ViewParent] */
    /* access modifiers changed from: private */
    /* JADX WARNING: Multi-variable type inference failed */
    /* JADX WARNING: Unknown variable types count: 2 */
    public View getCommonPredecessor(View first, View second) {
        if (this.mTempHashSet == null) {
            this.mTempHashSet = new HashSet<>();
        }
        HashSet<View> seen = this.mTempHashSet;
        seen.clear();
        View firstCurrent = first;
        while (firstCurrent != null) {
            seen.add(firstCurrent);
            ? firstCurrent2 = firstCurrent.mParent;
            if (firstCurrent2 instanceof View) {
                firstCurrent = firstCurrent2;
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
            ? secondCurrent2 = secondCurrent.mParent;
            if (secondCurrent2 instanceof View) {
                secondCurrent = secondCurrent2;
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

    public void requestDisallowInterceptTouchEvent(boolean disallowIntercept) {
    }

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

    public void childHasTransientStateChanged(View child, boolean hasTransientState) {
    }

    public boolean onStartNestedScroll(View child, View target, int nestedScrollAxes) {
        return false;
    }

    public void onStopNestedScroll(View target) {
    }

    public void onNestedScrollAccepted(View child, View target, int nestedScrollAxes) {
    }

    public void onNestedScroll(View target, int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed) {
    }

    public void onNestedPreScroll(View target, int dx, int dy, int[] consumed) {
    }

    public boolean onNestedFling(View target, float velocityX, float velocityY, boolean consumed) {
        return false;
    }

    public boolean onNestedPreFling(View target, float velocityX, float velocityY) {
        return false;
    }

    public boolean onNestedPrePerformAccessibilityAction(View target, int action, Bundle args) {
        return false;
    }

    /* access modifiers changed from: private */
    public void reportNextDraw() {
        if (!this.mReportNextDraw) {
            drawPending();
        }
        if (DEBUG_HWFLOW && this.mTag.contains("StatusBar")) {
            Slog.d(this.mTag, "reportNextDraw...");
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
        if (this.mAttachInfo.mThreadedRenderer != null) {
            this.mAttachInfo.mThreadedRenderer.setOpaque(opaque);
        }
    }

    public boolean dispatchUnhandledKeyEvent(KeyEvent event) {
        return this.mUnhandledKeyManager.dispatch(this.mView, event);
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
        if (this.mDragResizing == 0 || !this.mReportNextDraw) {
            z = false;
        }
        return updated | z;
    }

    private void requestDrawWindow() {
        if (this.mUseMTRenderer) {
            this.mWindowDrawCountDown = new CountDownLatch(this.mWindowCallbacks.size());
            for (int i = this.mWindowCallbacks.size() - 1; i >= 0; i--) {
                this.mWindowCallbacks.get(i).onRequestDraw(this.mReportNextDraw);
            }
        }
    }

    public void reportActivityRelaunched() {
        this.mActivityRelaunched = true;
    }

    /* access modifiers changed from: private */
    public boolean printEventIfNeed(InputEvent event) {
        boolean isMove = false;
        if (event instanceof MotionEvent) {
            MotionEvent e = (MotionEvent) event;
            boolean z = false;
            if (!DEBUG_MOVING) {
                if (e.getActionMasked() == 2) {
                    if (this.mEventChanged) {
                        Flog.i(1507, "Moving, pointerCount=" + e.getPointerCount());
                        this.mEventChanged = false;
                    }
                    return true;
                }
                this.mEventChanged = true;
            }
            if (e.getX() > 0.0f || e.getY() > 0.0f) {
                StringBuilder sb = new StringBuilder();
                int count = e.getPointerCount();
                for (int i = 0; i < count; i++) {
                    sb.append("(");
                    sb.append(Math.round(e.getX(i)));
                    sb.append(",");
                    sb.append(Math.round(e.getY(i)));
                    sb.append(")");
                }
                int i2 = e.getAction();
                sb.append(i2);
                Flog.i(1507, sb.toString());
                if (i2 == 2) {
                    z = true;
                }
                isMove = z;
            }
        } else if (event instanceof KeyEvent) {
            KeyEvent e2 = (KeyEvent) event;
            Flog.i(1507, e2.getKeyCode() + "-" + e2.getAction());
        }
        return isMove;
    }

    /* access modifiers changed from: private */
    public void sendKeyEventToPG(InputEvent event) {
        if (event instanceof KeyEvent) {
            KeyEvent e = (KeyEvent) event;
            if (1 == e.getAction() && 4 == e.getKeyCode()) {
                LogPower.push(213, this.mPkgName);
            }
        }
    }
}
