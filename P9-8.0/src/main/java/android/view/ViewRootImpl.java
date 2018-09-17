package android.view;

import android.animation.LayoutTransition;
import android.app.ActivityManager;
import android.app.ResourcesManager;
import android.common.HwFrameworkFactory;
import android.content.ClipData;
import android.content.ClipDescription;
import android.content.Context;
import android.content.res.CompatibilityInfo;
import android.content.res.CompatibilityInfo.Translator;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.PorterDuff.Mode;
import android.graphics.Rect;
import android.graphics.Region;
import android.graphics.drawable.AnimatedVectorDrawable.VectorDrawableAnimatorRT;
import android.graphics.drawable.Drawable;
import android.hardware.display.DisplayManager;
import android.hardware.display.DisplayManager.DisplayListener;
import android.hardware.input.InputManager;
import android.media.AudioManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.Debug;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.ParcelFileDescriptor;
import android.os.ParcelFileDescriptor.AutoCloseOutputStream;
import android.os.Process;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.Trace;
import android.scrollerboost.ScrollerBoostManager;
import android.util.AndroidRuntimeException;
import android.util.DisplayMetrics;
import android.util.HwPCUtils;
import android.util.Jlog;
import android.util.JlogConstants;
import android.util.Log;
import android.util.LogException;
import android.util.MergedConfiguration;
import android.util.Slog;
import android.util.TimeUtils;
import android.util.TypedValue;
import android.view.Choreographer.FrameCallback;
import android.view.InputDevice.MotionRange;
import android.view.InputQueue.Callback;
import android.view.KeyCharacterMap.FallbackAction;
import android.view.Surface.OutOfResourcesException;
import android.view.SurfaceHolder.Callback2;
import android.view.View.MeasureSpec;
import android.view.ViewTreeObserver.InternalInsetsInfo;
import android.view.WindowManager.BadTokenException;
import android.view.WindowManager.InvalidDisplayException;
import android.view.WindowManager.LayoutParams;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityManager;
import android.view.accessibility.AccessibilityManager.AccessibilityStateChangeListener;
import android.view.accessibility.AccessibilityManager.HighTextContrastChangeListener;
import android.view.accessibility.AccessibilityNodeInfo;
import android.view.accessibility.AccessibilityNodeInfo.AccessibilityAction;
import android.view.accessibility.AccessibilityNodeProvider;
import android.view.accessibility.IAccessibilityInteractionConnection.Stub;
import android.view.accessibility.IAccessibilityInteractionConnectionCallback;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Interpolator;
import android.view.inputmethod.InputMethodManager;
import android.view.inputmethod.InputMethodManager.FinishedInputEventCallback;
import android.widget.Scroller;
import com.android.internal.R;
import com.android.internal.annotations.GuardedBy;
import com.android.internal.content.NativeLibraryHelper;
import com.android.internal.os.IResultReceiver;
import com.android.internal.os.SomeArgs;
import com.android.internal.policy.PhoneFallbackEventHandler;
import com.android.internal.util.Preconditions;
import com.android.internal.view.BaseSurfaceHolder;
import com.android.internal.view.RootViewSurfaceTaker;
import com.android.internal.view.SurfaceCallbackHelper;
import java.io.FileDescriptor;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.concurrent.CountDownLatch;
import javax.microedition.khronos.opengles.GL10;

public final class ViewRootImpl implements ViewParent, Callbacks, DrawCallbacks {
    static int CONTINUOUS_REF = SystemProperties.getInt("ro.config.hw_jank_continuousref", 1000);
    private static final boolean DBG = false;
    private static final boolean DEBUG_CONFIGURATION = false;
    private static final boolean DEBUG_DIALOG = false;
    private static final boolean DEBUG_DRAW = false;
    private static final boolean DEBUG_FPS = false;
    private static final boolean DEBUG_IMF = false;
    private static final boolean DEBUG_INPUT_RESIZE = false;
    private static final boolean DEBUG_INPUT_STAGES = false;
    private static final boolean DEBUG_KEEP_SCREEN_ON = false;
    private static final boolean DEBUG_LAYOUT = false;
    private static final boolean DEBUG_ORIENTATION = false;
    private static final boolean DEBUG_TRACKBALL = false;
    private static final boolean FRONT_FINGERPRINT_NAVIGATION = SystemProperties.getBoolean("ro.config.hw_front_fp_navi", false);
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
    static int ONTIME_REF = SystemProperties.getInt("ro.config.hw_jank_ontimeref", 1);
    private static final String PROPERTY_ALLOC_BUFFER_SYNC = "persist.alloc_buffers_sync";
    public static final String PROPERTY_EMULATOR_WIN_OUTSET_BOTTOM_PX = "ro.emu.win_outset_bottom_px";
    private static final String PROPERTY_PROFILE_RENDERING = "viewroot.profile_rendering";
    private static final long REDUNDANT = 500000;
    private static final long SF_VSYNC_OFFSET = 5000000;
    private static final String TAG = "ViewRootImpl";
    private static final boolean USE_MT_RENDERER = true;
    private static final long VSYNC_OFFSET = 7500000;
    private static final long VSYNC_SPAN = 16666667;
    static final Interpolator mResizeInterpolator = new AccelerateDecelerateInterpolator();
    private static final boolean mSupportAod = "1".equals(SystemProperties.get("ro.config.support_aod", null));
    public static boolean sAlwaysAssignFocus;
    private static boolean sCompatibilityDone = false;
    private static final ArrayList<ConfigChangedCallback> sConfigCallbacks = new ArrayList();
    static boolean sFirstDrawComplete = false;
    static final ArrayList<Runnable> sFirstDrawHandlers = new ArrayList();
    private static boolean sIsFirstFrame = false;
    protected static long sLastRelayoutNotifyTime = 0;
    protected static long sRelayoutNotifyPeriod = 0;
    static final ThreadLocal<HandlerActionQueue> sRunQueues = new ThreadLocal();
    public static boolean sSLBSwitch = false;
    static final RemoteCallbackList<IWindowLayoutObserver> sWindowLayoutObservers = new RemoteCallbackList();
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
    private boolean mAllocBufferAsync = false;
    private boolean mAllocBufferSync = SystemProperties.getBoolean(PROPERTY_ALLOC_BUFFER_SYNC, false);
    boolean mAppVisible = true;
    boolean mApplyInsetsRequested;
    final AttachInfo mAttachInfo;
    AudioManager mAudioManager;
    final String mBasePackageName;
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
    private boolean mDebugRefreshDirty = false;
    private long mDeliverInputTime;
    private final int mDensity;
    Rect mDirty;
    final Rect mDispatchContentInsets = new Rect();
    final Rect mDispatchStableInsets = new Rect();
    Display mDisplay;
    private final DisplayListener mDisplayListener;
    final DisplayManager mDisplayManager;
    ClipDescription mDragDescription;
    final PointF mDragPoint = new PointF();
    private boolean mDragResizing;
    boolean mDrawingAllowed;
    int mDrawsNeededToReport;
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
    Callback mInputQueueCallback;
    final InvalidateOnAnimationRunnable mInvalidateOnAnimationRunnable;
    private boolean mInvalidateRootRequested;
    public View mInvalidatedChildInPU = null;
    boolean mIsAmbientMode = false;
    public boolean mIsAnimating;
    boolean mIsCreating;
    boolean mIsDrawing;
    boolean mIsInTraversal;
    private final Configuration mLastConfigurationFromResources = new Configuration();
    final InternalInsetsInfo mLastGivenInsets = new InternalInsetsInfo();
    boolean mLastInCompatMode = false;
    boolean mLastOverscanRequested;
    private final MergedConfiguration mLastReportedMergedConfiguration = new MergedConfiguration();
    WeakReference<View> mLastScrolledFocus;
    int mLastSystemUiVisibility;
    final PointF mLastTouchPoint = new PointF();
    int mLastTouchSource;
    boolean mLastWasImTarget;
    private WindowInsets mLastWindowInsets;
    boolean mLayoutRequested;
    ArrayList<View> mLayoutRequesters = new ArrayList();
    volatile Object mLocalDragState;
    final WindowLeaked mLocation;
    boolean mLostWindowFocus;
    private boolean mNeedsRendererSetup;
    boolean mNewSurfaceNeeded;
    private final int mNoncompatDensity;
    public Point mOffset = null;
    int mOrigWindowType = -1;
    private boolean mPartialUpdateSwitch = false;
    boolean mPausedForTransition = false;
    boolean mPendingAlwaysConsumeNavBar;
    final Rect mPendingBackDropFrame = new Rect();
    final Rect mPendingContentInsets = new Rect();
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
    private String mPkgName;
    boolean mPointerCapture;
    private int mPointerIconType = 1;
    final Region mPreviousTransparentRegion;
    boolean mProcessInputEventsScheduled;
    private boolean mProfile;
    private boolean mProfileRendering;
    private QueuedInputEvent mQueuedInputEventPool;
    private int mQueuedInputEventPoolSize;
    private boolean mRemoved;
    private FrameCallback mRenderProfiler;
    private boolean mRenderProfilingEnabled;
    boolean mReportNextDraw;
    private int mResizeMode;
    boolean mScrollMayChange;
    int mScrollY;
    Scroller mScroller;
    SendWindowContentChangedAccessibilityEvent mSendWindowContentChangedAccessibilityEvent;
    int mSeq;
    private long mSoftDrawTime;
    int mSoftInputMode;
    boolean mStopped = false;
    final Surface mSurface = new Surface();
    BaseSurfaceHolder mSurfaceHolder;
    Callback2 mSurfaceHolderCallback;
    InputStage mSyntheticInputStage;
    private String mTag;
    final int mTargetSdkVersion;
    HashSet<View> mTempHashSet;
    final Rect mTempRect;
    final Thread mThread;
    final int[] mTmpLocation = new int[2];
    final TypedValue mTmpValue = new TypedValue();
    Translator mTranslator;
    final Region mTransparentRegion;
    int mTraversalBarrier;
    final TraversalRunnable mTraversalRunnable;
    public boolean mTraversalScheduled;
    boolean mUnbufferedInputDispatch;
    View mView;
    final ViewConfiguration mViewConfiguration;
    private int mViewLayoutDirectionInitial;
    int mViewVisibility;
    final Rect mVisRect;
    int mWidth;
    boolean mWillDrawSoon;
    final Rect mWinFrame;
    final W mWindow;
    final LayoutParams mWindowAttributes = new LayoutParams();
    boolean mWindowAttributesChanged = false;
    int mWindowAttributesChangesFlag = 0;
    @GuardedBy("mWindowCallbacks")
    final ArrayList<WindowCallbacks> mWindowCallbacks = new ArrayList();
    CountDownLatch mWindowDrawCountDown;
    final IWindowSession mWindowSession;
    private final ArrayList<WindowStoppedCallback> mWindowStoppedCallbacks;

    interface WindowStoppedCallback {
        void windowStopped(boolean z);
    }

    public interface ConfigChangedCallback {
        void onConfigurationChanged(Configuration configuration);
    }

    static final class AccessibilityInteractionConnection extends Stub {
        private final WeakReference<ViewRootImpl> mViewRootImpl;

        AccessibilityInteractionConnection(ViewRootImpl viewRootImpl) {
            this.mViewRootImpl = new WeakReference(viewRootImpl);
        }

        public void findAccessibilityNodeInfoByAccessibilityId(long accessibilityNodeId, Region interactiveRegion, int interactionId, IAccessibilityInteractionConnectionCallback callback, int flags, int interrogatingPid, long interrogatingTid, MagnificationSpec spec, Bundle args) {
            ViewRootImpl viewRootImpl = (ViewRootImpl) this.mViewRootImpl.get();
            if (viewRootImpl == null || viewRootImpl.mView == null) {
                try {
                    callback.setFindAccessibilityNodeInfosResult(null, interactionId);
                    return;
                } catch (RemoteException e) {
                    return;
                }
            }
            viewRootImpl.getAccessibilityInteractionController().findAccessibilityNodeInfoByAccessibilityIdClientThread(accessibilityNodeId, interactiveRegion, interactionId, callback, flags, interrogatingPid, interrogatingTid, spec, args);
        }

        public void performAccessibilityAction(long accessibilityNodeId, int action, Bundle arguments, int interactionId, IAccessibilityInteractionConnectionCallback callback, int flags, int interrogatingPid, long interrogatingTid) {
            ViewRootImpl viewRootImpl = (ViewRootImpl) this.mViewRootImpl.get();
            if (viewRootImpl == null || viewRootImpl.mView == null) {
                try {
                    callback.setPerformAccessibilityActionResult(false, interactionId);
                    return;
                } catch (RemoteException e) {
                    return;
                }
            }
            viewRootImpl.getAccessibilityInteractionController().performAccessibilityActionClientThread(accessibilityNodeId, action, arguments, interactionId, callback, flags, interrogatingPid, interrogatingTid);
        }

        public void findAccessibilityNodeInfosByViewId(long accessibilityNodeId, String viewId, Region interactiveRegion, int interactionId, IAccessibilityInteractionConnectionCallback callback, int flags, int interrogatingPid, long interrogatingTid, MagnificationSpec spec) {
            ViewRootImpl viewRootImpl = (ViewRootImpl) this.mViewRootImpl.get();
            if (viewRootImpl == null || viewRootImpl.mView == null) {
                try {
                    callback.setFindAccessibilityNodeInfoResult(null, interactionId);
                    return;
                } catch (RemoteException e) {
                    return;
                }
            }
            viewRootImpl.getAccessibilityInteractionController().findAccessibilityNodeInfosByViewIdClientThread(accessibilityNodeId, viewId, interactiveRegion, interactionId, callback, flags, interrogatingPid, interrogatingTid, spec);
        }

        public void findAccessibilityNodeInfosByText(long accessibilityNodeId, String text, Region interactiveRegion, int interactionId, IAccessibilityInteractionConnectionCallback callback, int flags, int interrogatingPid, long interrogatingTid, MagnificationSpec spec) {
            ViewRootImpl viewRootImpl = (ViewRootImpl) this.mViewRootImpl.get();
            if (viewRootImpl == null || viewRootImpl.mView == null) {
                try {
                    callback.setFindAccessibilityNodeInfosResult(null, interactionId);
                    return;
                } catch (RemoteException e) {
                    return;
                }
            }
            viewRootImpl.getAccessibilityInteractionController().findAccessibilityNodeInfosByTextClientThread(accessibilityNodeId, text, interactiveRegion, interactionId, callback, flags, interrogatingPid, interrogatingTid, spec);
        }

        public void findFocus(long accessibilityNodeId, int focusType, Region interactiveRegion, int interactionId, IAccessibilityInteractionConnectionCallback callback, int flags, int interrogatingPid, long interrogatingTid, MagnificationSpec spec) {
            ViewRootImpl viewRootImpl = (ViewRootImpl) this.mViewRootImpl.get();
            if (viewRootImpl == null || viewRootImpl.mView == null) {
                try {
                    callback.setFindAccessibilityNodeInfoResult(null, interactionId);
                    return;
                } catch (RemoteException e) {
                    return;
                }
            }
            viewRootImpl.getAccessibilityInteractionController().findFocusClientThread(accessibilityNodeId, focusType, interactiveRegion, interactionId, callback, flags, interrogatingPid, interrogatingTid, spec);
        }

        public void focusSearch(long accessibilityNodeId, int direction, Region interactiveRegion, int interactionId, IAccessibilityInteractionConnectionCallback callback, int flags, int interrogatingPid, long interrogatingTid, MagnificationSpec spec) {
            ViewRootImpl viewRootImpl = (ViewRootImpl) this.mViewRootImpl.get();
            if (viewRootImpl == null || viewRootImpl.mView == null) {
                try {
                    callback.setFindAccessibilityNodeInfoResult(null, interactionId);
                    return;
                } catch (RemoteException e) {
                    return;
                }
            }
            viewRootImpl.getAccessibilityInteractionController().focusSearchClientThread(accessibilityNodeId, direction, interactiveRegion, interactionId, callback, flags, interrogatingPid, interrogatingTid, spec);
        }
    }

    final class AccessibilityInteractionConnectionManager implements AccessibilityStateChangeListener {
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
                ViewRootImpl.this.mAttachInfo.mAccessibilityWindowId = ViewRootImpl.this.mAccessibilityManager.addAccessibilityInteractionConnection(ViewRootImpl.this.mWindow, new AccessibilityInteractionConnection(ViewRootImpl.this));
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

        protected void finish(QueuedInputEvent q, boolean handled) {
            q.mFlags |= 4;
            if (handled) {
                q.mFlags |= 8;
            }
            forward(q);
        }

        protected void forward(QueuedInputEvent q) {
            onDeliverToNext(q);
        }

        protected void apply(QueuedInputEvent q, int result) {
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

        protected int onProcess(QueuedInputEvent q) {
            return 0;
        }

        protected void onDeliverToNext(QueuedInputEvent q) {
            if (this.mNext != null) {
                this.mNext.deliver(q);
            } else {
                ViewRootImpl.this.finishInputEvent(q);
            }
        }

        protected boolean shouldDropInputEvent(QueuedInputEvent q) {
            if (ViewRootImpl.this.mView == null || (ViewRootImpl.this.mAdded ^ 1) != 0) {
                Slog.w(ViewRootImpl.this.mTag, "Dropping event due to root view being removed: " + q.mEvent);
                return true;
            } else if ((ViewRootImpl.this.mAttachInfo.mHasWindowFocus || (q.mEvent.isFromSource(2) ^ 1) == 0) && !ViewRootImpl.this.mStopped && ((!ViewRootImpl.this.mIsAmbientMode || (q.mEvent.isFromSource(1) ^ 1) == 0) && (!ViewRootImpl.this.mPausedForTransition || (isBack(q.mEvent) ^ 1) == 0))) {
                return false;
            } else {
                if (ViewRootImpl.isTerminalInputEvent(q.mEvent)) {
                    q.mEvent.cancel();
                    Slog.w(ViewRootImpl.this.mTag, "Cancelling event due to no window focus: " + q.mEvent);
                    return false;
                }
                Slog.w(ViewRootImpl.this.mTag, "Dropping event due to no window focus: " + q.mEvent);
                return true;
            }
        }

        void dump(String prefix, PrintWriter writer) {
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

        protected void defer(QueuedInputEvent q) {
            q.mFlags |= 2;
            enqueue(q);
        }

        protected void forward(QueuedInputEvent q) {
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
            if (blocked) {
                if (curr == null) {
                    enqueue(q);
                }
                return;
            }
            if (curr != null) {
                curr = curr.mNext;
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

        protected void apply(QueuedInputEvent q, int result) {
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

        void dump(String prefix, PrintWriter writer) {
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
            ViewRootImpl.this.mDeliverInputTime = System.nanoTime() - runstart;
        }
    }

    final class EarlyPostImeInputStage extends InputStage {
        public EarlyPostImeInputStage(InputStage next) {
            super(next);
        }

        protected int onProcess(QueuedInputEvent q) {
            if (q.mEvent instanceof KeyEvent) {
                return processKeyEvent(q);
            }
            if ((q.mEvent.getSource() & 2) != 0) {
                return processPointerEvent(q);
            }
            return 0;
        }

        private int processKeyEvent(QueuedInputEvent q) {
            KeyEvent event = q.mEvent;
            if (ViewRootImpl.this.mAttachInfo.mTooltipHost != null) {
                ViewRootImpl.this.mAttachInfo.mTooltipHost.handleTooltipKey(event);
            }
            if (ViewRootImpl.this.checkForLeavingTouchModeAndConsume(event)) {
                return 1;
            }
            ViewRootImpl.this.mFallbackEventHandler.preDispatchKeyEvent(event);
            return 0;
        }

        private int processPointerEvent(QueuedInputEvent q) {
            MotionEvent event = q.mEvent;
            if (ViewRootImpl.this.mDisplay != null && HwPCUtils.isValidExtDisplayId(ViewRootImpl.this.mDisplay.getDisplayId())) {
                event.setOffset(ViewRootImpl.this.mOffset);
            }
            if (ViewRootImpl.this.mTranslator != null) {
                ViewRootImpl.this.mTranslator.translateEventInScreenToAppWindow(event);
            }
            if (HwFrameworkFactory.getHwNsdImpl().isSupportAps()) {
                HwFrameworkFactory.getHwNsdImpl().adaptPowerSave(ViewRootImpl.this.mView.getContext(), event);
                if (HwFrameworkFactory.getHwNsdImpl().isGameProcess(ViewRootImpl.this.mContext.getPackageName())) {
                    HwFrameworkFactory.getHwNsdImpl().initAPS(ViewRootImpl.this.mView.getContext(), ViewRootImpl.this.mView.getResources().getDisplayMetrics().widthPixels, Process.myPid());
                }
            }
            int action = event.getAction();
            if (action == 0 || action == 8) {
                if (ViewRootImpl.this.mDisplay == null || !HwPCUtils.isValidExtDisplayId(ViewRootImpl.this.mDisplay.getDisplayId())) {
                    ViewRootImpl.this.ensureTouchMode(event.isFromSource(4098));
                } else {
                    HwPCUtils.log(ViewRootImpl.TAG, "ensureTouchMode true");
                    ViewRootImpl.this.ensureTouchMode(true);
                }
            }
            if (action == 0 && ViewRootImpl.this.mAttachInfo.mTooltipHost != null) {
                ViewRootImpl.this.mAttachInfo.mTooltipHost.-android_view_View-mthref-1();
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

    final class HighContrastTextManager implements HighTextContrastChangeListener {
        HighContrastTextManager() {
            ViewRootImpl.this.mAttachInfo.mHighContrastText = ViewRootImpl.this.mAccessibilityManager.isHighTextContrastEnabled();
        }

        public void onHighTextContrastStateChanged(boolean enabled) {
            ViewRootImpl.this.mAttachInfo.mHighContrastText = enabled;
            ViewRootImpl.this.destroyHardwareResources();
            ViewRootImpl.this.invalidate();
        }
    }

    final class ImeInputStage extends AsyncInputStage implements FinishedInputEventCallback {
        public ImeInputStage(InputStage next, String traceCounter) {
            super(next, traceCounter);
        }

        protected int onProcess(QueuedInputEvent q) {
            if (ViewRootImpl.this.mLastWasImTarget && (ViewRootImpl.this.isInLocalFocusMode() ^ 1) != 0) {
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

    final class InvalidateOnAnimationRunnable implements Runnable {
        private boolean mPosted;
        private InvalidateInfo[] mTempViewRects;
        private View[] mTempViews;
        private final ArrayList<InvalidateInfo> mViewRects = new ArrayList();
        private final ArrayList<View> mViews = new ArrayList();

        InvalidateOnAnimationRunnable() {
        }

        public void addView(View view) {
            synchronized (this) {
                this.mViews.add(view);
                postIfNeededLocked();
            }
        }

        public void addViewRect(InvalidateInfo info) {
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
                    InvalidateInfo info = (InvalidateInfo) this.mViewRects.get(i2);
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
            int viewCount;
            int viewRectCount;
            int i;
            synchronized (this) {
                this.mPosted = false;
                viewCount = this.mViews.size();
                if (viewCount != 0) {
                    this.mTempViews = (View[]) this.mViews.toArray(this.mTempViews != null ? this.mTempViews : new View[viewCount]);
                    this.mViews.clear();
                }
                viewRectCount = this.mViewRects.size();
                if (viewRectCount != 0) {
                    this.mTempViewRects = (InvalidateInfo[]) this.mViewRects.toArray(this.mTempViewRects != null ? this.mTempViewRects : new InvalidateInfo[viewRectCount]);
                    this.mViewRects.clear();
                }
            }
            for (i = 0; i < viewCount; i++) {
                this.mTempViews[i].invalidate();
                this.mTempViews[i] = null;
            }
            for (i = 0; i < viewRectCount; i++) {
                InvalidateInfo info = this.mTempViewRects[i];
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

    final class NativePostImeInputStage extends AsyncInputStage implements InputQueue.FinishedInputEventCallback {
        public NativePostImeInputStage(InputStage next, String traceCounter) {
            super(next, traceCounter);
        }

        protected int onProcess(QueuedInputEvent q) {
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

    final class NativePreImeInputStage extends AsyncInputStage implements InputQueue.FinishedInputEventCallback {
        public NativePreImeInputStage(InputStage next, String traceCounter) {
            super(next, traceCounter);
        }

        protected int onProcess(QueuedInputEvent q) {
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

        /* synthetic */ QueuedInputEvent(QueuedInputEvent -this0) {
            this();
        }

        private QueuedInputEvent() {
        }

        public boolean shouldSkipIme() {
            boolean z = true;
            if ((this.mFlags & 1) != 0) {
                return true;
            }
            if (!(this.mEvent instanceof MotionEvent)) {
                z = false;
            } else if (!this.mEvent.isFromSource(2)) {
                z = this.mEvent.isFromSource(4194304);
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
                sb.append("0");
            }
            sb.append(", hasNextQueuedEvent=").append(this.mEvent != null ? "true" : "false");
            sb.append(", hasInputEventReceiver=").append(this.mReceiver != null ? "true" : "false");
            sb.append(", mEvent=").append(this.mEvent).append("}");
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

    private class SendWindowContentChangedAccessibilityEvent implements Runnable {
        private int mChangeTypes;
        public long mLastEventTimeMillis;
        public View mSource;

        /* synthetic */ SendWindowContentChangedAccessibilityEvent(ViewRootImpl this$0, SendWindowContentChangedAccessibilityEvent -this1) {
            this();
        }

        private SendWindowContentChangedAccessibilityEvent() {
            this.mChangeTypes = 0;
        }

        public void run() {
            if (AccessibilityManager.getInstance(ViewRootImpl.this.mContext).isEnabled()) {
                this.mLastEventTimeMillis = SystemClock.uptimeMillis();
                AccessibilityEvent event = AccessibilityEvent.obtain();
                event.setEventType(2048);
                event.setContentChangeTypes(this.mChangeTypes);
                if (this.mSource != null) {
                    this.mSource.sendAccessibilityEventUnchecked(event);
                }
            } else {
                this.mLastEventTimeMillis = 0;
            }
            if (this.mSource != null) {
                this.mSource.resetSubtreeAccessibilityStateChanged();
            }
            this.mSource = null;
            this.mChangeTypes = 0;
        }

        public void runOrPost(View source, int changeType) {
            if (this.mSource != null) {
                View predecessor = ViewRootImpl.this.getCommonPredecessor(this.mSource, source);
                if (predecessor == null) {
                    predecessor = source;
                }
                this.mSource = predecessor;
                this.mChangeTypes |= changeType;
                return;
            }
            this.mSource = source;
            this.mChangeTypes = changeType;
            long timeSinceLastMillis = SystemClock.uptimeMillis() - this.mLastEventTimeMillis;
            long minEventIntevalMillis = ViewConfiguration.getSendRecurringAccessibilityEventsInterval();
            if (timeSinceLastMillis >= minEventIntevalMillis) {
                this.mSource.removeCallbacks(this);
                run();
            } else {
                this.mSource.postDelayed(this, minEventIntevalMillis - timeSinceLastMillis);
            }
        }
    }

    final class SyntheticInputStage extends InputStage {
        private final SyntheticJoystickHandler mJoystick = new SyntheticJoystickHandler();
        private final SyntheticKeyboardHandler mKeyboard = new SyntheticKeyboardHandler();
        private final SyntheticTouchNavigationHandler mTouchNavigation = new SyntheticTouchNavigationHandler();
        private final SyntheticTrackballHandler mTrackball = new SyntheticTrackballHandler();

        public SyntheticInputStage() {
            super(null);
        }

        protected int onProcess(QueuedInputEvent q) {
            q.mFlags |= 16;
            if (q.mEvent instanceof MotionEvent) {
                MotionEvent event = q.mEvent;
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

        protected void onDeliverToNext(QueuedInputEvent q) {
            if ((q.mFlags & 16) == 0 && (q.mEvent instanceof MotionEvent)) {
                MotionEvent event = q.mEvent;
                int source = event.getSource();
                if ((source & 4) != 0) {
                    this.mTrackball.cancel(event);
                } else if ((source & 16) != 0) {
                    this.mJoystick.cancel(event);
                } else if ((source & 2097152) == 2097152) {
                    this.mTouchNavigation.cancel(event);
                }
            }
            super.onDeliverToNext(q);
        }
    }

    final class SyntheticJoystickHandler extends Handler {
        private static final int MSG_ENQUEUE_X_AXIS_KEY_REPEAT = 1;
        private static final int MSG_ENQUEUE_Y_AXIS_KEY_REPEAT = 2;
        private static final String TAG = "SyntheticJoystickHandler";
        private int mLastXDirection;
        private int mLastXKeyCode;
        private int mLastYDirection;
        private int mLastYKeyCode;

        public SyntheticJoystickHandler() {
            super(true);
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                case 2:
                    KeyEvent oldEvent = msg.obj;
                    KeyEvent e = KeyEvent.changeTimeRepeat(oldEvent, SystemClock.uptimeMillis(), oldEvent.getRepeatCount() + 1);
                    if (ViewRootImpl.this.mAttachInfo.mHasWindowFocus) {
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
                    update(event, true);
                    return;
                case 3:
                    cancel(event);
                    return;
                default:
                    Log.w(ViewRootImpl.this.mTag, "Unexpected action: " + event.getActionMasked());
                    return;
            }
        }

        private void cancel(MotionEvent event) {
            removeMessages(1);
            removeMessages(2);
            update(event, false);
        }

        private void update(MotionEvent event, boolean synthesizeNewKeys) {
            KeyEvent e;
            Message m;
            long time = event.getEventTime();
            int metaState = event.getMetaState();
            int deviceId = event.getDeviceId();
            int source = event.getSource();
            int xDirection = joystickAxisValueToDirection(event.getAxisValue(15));
            if (xDirection == 0) {
                xDirection = joystickAxisValueToDirection(event.getX());
            }
            int yDirection = joystickAxisValueToDirection(event.getAxisValue(16));
            if (yDirection == 0) {
                yDirection = joystickAxisValueToDirection(event.getY());
            }
            if (xDirection != this.mLastXDirection) {
                if (this.mLastXKeyCode != 0) {
                    removeMessages(1);
                    ViewRootImpl.this.enqueueInputEvent(new KeyEvent(time, time, 1, this.mLastXKeyCode, 0, metaState, deviceId, 0, 1024, source));
                    this.mLastXKeyCode = 0;
                }
                this.mLastXDirection = xDirection;
                if (xDirection != 0 && synthesizeNewKeys) {
                    this.mLastXKeyCode = xDirection > 0 ? 22 : 21;
                    e = new KeyEvent(time, time, 0, this.mLastXKeyCode, 0, metaState, deviceId, 0, 1024, source);
                    ViewRootImpl.this.enqueueInputEvent(e);
                    m = obtainMessage(1, e);
                    m.setAsynchronous(true);
                    sendMessageDelayed(m, (long) ViewConfiguration.getKeyRepeatTimeout());
                }
            }
            if (yDirection != this.mLastYDirection) {
                if (this.mLastYKeyCode != 0) {
                    removeMessages(2);
                    ViewRootImpl.this.enqueueInputEvent(new KeyEvent(time, time, 1, this.mLastYKeyCode, 0, metaState, deviceId, 0, 1024, source));
                    this.mLastYKeyCode = 0;
                }
                this.mLastYDirection = yDirection;
                if (yDirection != 0 && synthesizeNewKeys) {
                    this.mLastYKeyCode = yDirection > 0 ? 20 : 19;
                    e = new KeyEvent(time, time, 0, this.mLastYKeyCode, 0, metaState, deviceId, 0, 1024, source);
                    ViewRootImpl.this.enqueueInputEvent(e);
                    m = obtainMessage(2, e);
                    m.setAsynchronous(true);
                    sendMessageDelayed(m, (long) ViewConfiguration.getKeyRepeatTimeout());
                }
            }
        }

        private int joystickAxisValueToDirection(float value) {
            if (value >= 0.5f) {
                return 1;
            }
            if (value <= -0.5f) {
                return -1;
            }
            return 0;
        }
    }

    final class SyntheticKeyboardHandler {
        SyntheticKeyboardHandler() {
        }

        public void process(KeyEvent event) {
            if ((event.getFlags() & 1024) == 0) {
                FallbackAction fallbackAction = event.getKeyCharacterMap().getFallbackAction(event.getKeyCode(), event.getMetaState());
                if (fallbackAction != null) {
                    InputEvent fallbackEvent = KeyEvent.obtain(event.getDownTime(), event.getEventTime(), event.getAction(), fallbackAction.keyCode, event.getRepeatCount(), fallbackAction.metaState, event.getDeviceId(), event.getScanCode(), event.getFlags() | 1024, event.getSource(), null);
                    fallbackAction.recycle();
                    ViewRootImpl.this.enqueueInputEvent(fallbackEvent);
                }
            }
        }
    }

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
            public void run() {
                long time = SystemClock.uptimeMillis();
                SyntheticTouchNavigationHandler.this.sendKeyDownOrRepeat(time, SyntheticTouchNavigationHandler.this.mPendingKeyCode, SyntheticTouchNavigationHandler.this.mPendingKeyMetaState);
                SyntheticTouchNavigationHandler syntheticTouchNavigationHandler = SyntheticTouchNavigationHandler.this;
                syntheticTouchNavigationHandler.mFlingVelocity = syntheticTouchNavigationHandler.mFlingVelocity * SyntheticTouchNavigationHandler.FLING_TICK_DECAY;
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

        public SyntheticTouchNavigationHandler() {
            super(true);
        }

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
                    MotionRange xRange = device.getMotionRange(0);
                    MotionRange yRange = device.getMotionRange(1);
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
                        this.mConfigTickDistance = 12.0f * ((xRes + yRes) * 0.5f);
                        this.mConfigMinFlingVelocity = this.mConfigTickDistance * MIN_FLING_VELOCITY_TICKS_PER_SECOND;
                        this.mConfigMaxFlingVelocity = this.mConfigTickDistance * MAX_FLING_VELOCITY_TICKS_PER_SECOND;
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
                        break;
                    case 1:
                    case 2:
                        if (this.mActivePointerId >= 0) {
                            int index = event.findPointerIndex(this.mActivePointerId);
                            if (index >= 0) {
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
                                    break;
                                }
                            }
                            finishKeys(time);
                            finishTracking(time);
                            break;
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
            if (this.mPendingKeyCode != 0) {
                ViewRootImpl.this.enqueueInputEvent(new KeyEvent(this.mPendingKeyDownTime, time, 1, this.mPendingKeyCode, 0, this.mPendingKeyMetaState, this.mCurrentDeviceId, 0, 1024, this.mCurrentSource));
                this.mPendingKeyCode = 0;
            }
        }

        private boolean startFling(long time, float vx, float vy) {
            switch (this.mPendingKeyCode) {
                case 19:
                    if ((-vy) >= this.mConfigMinFlingVelocity && Math.abs(vx) < this.mConfigMinFlingVelocity) {
                        this.mFlingVelocity = -vy;
                        break;
                    }
                    return false;
                case 20:
                    if (vy >= this.mConfigMinFlingVelocity && Math.abs(vx) < this.mConfigMinFlingVelocity) {
                        this.mFlingVelocity = vy;
                        break;
                    }
                    return false;
                    break;
                case 21:
                    if ((-vx) >= this.mConfigMinFlingVelocity && Math.abs(vy) < this.mConfigMinFlingVelocity) {
                        this.mFlingVelocity = -vx;
                        break;
                    }
                    return false;
                case 22:
                    if (vx >= this.mConfigMinFlingVelocity && Math.abs(vy) < this.mConfigMinFlingVelocity) {
                        this.mFlingVelocity = vx;
                        break;
                    }
                    return false;
            }
            this.mFlinging = postFling(time);
            return this.mFlinging;
        }

        private boolean postFling(long time) {
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

    final class SyntheticTrackballHandler {
        private long mLastTime;
        private final TrackballAxis mX = new TrackballAxis();
        private final TrackballAxis mY = new TrackballAxis();

        SyntheticTrackballHandler() {
        }

        public void process(MotionEvent event) {
            long curTime = SystemClock.uptimeMillis();
            if (this.mLastTime + 250 < curTime) {
                this.mX.reset(0);
                this.mY.reset(0);
                this.mLastTime = curTime;
            }
            int action = event.getAction();
            int metaState = event.getMetaState();
            switch (action) {
                case 0:
                    this.mX.reset(2);
                    this.mY.reset(2);
                    ViewRootImpl.this.enqueueInputEvent(new KeyEvent(curTime, curTime, 0, 23, 0, metaState, -1, 0, 1024, 257));
                    break;
                case 1:
                    this.mX.reset(2);
                    this.mY.reset(2);
                    ViewRootImpl.this.enqueueInputEvent(new KeyEvent(curTime, curTime, 1, 23, 0, metaState, -1, 0, 1024, 257));
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
                        keycode = 22;
                    } else {
                        keycode = 21;
                    }
                    accel = this.mX.acceleration;
                    this.mY.reset(2);
                }
            } else if (yOff > 0.0f) {
                movement = this.mY.generate();
                if (movement != 0) {
                    if (movement > 0) {
                        keycode = 20;
                    } else {
                        keycode = 19;
                    }
                    accel = this.mY.acceleration;
                    this.mX.reset(2);
                }
            }
            if (keycode != 0) {
                if (movement < 0) {
                    movement = -movement;
                }
                int accelMovement = (int) (((float) movement) * accel);
                if (accelMovement > movement) {
                    movement--;
                    int repeatCount = accelMovement - movement;
                    ViewRootImpl.this.enqueueInputEvent(new KeyEvent(curTime, curTime, 2, keycode, repeatCount, metaState, -1, 0, 1024, 257));
                }
                while (movement > 0) {
                    movement--;
                    curTime = SystemClock.uptimeMillis();
                    ViewRootImpl.this.enqueueInputEvent(new KeyEvent(curTime, curTime, 0, keycode, 0, metaState, -1, 0, 1024, 257));
                    ViewRootImpl.this.enqueueInputEvent(new KeyEvent(curTime, curTime, 1, keycode, 0, metaState, -1, 0, 1024, 257));
                }
                this.mLastTime = curTime;
            }
        }

        public void cancel(MotionEvent event) {
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
            ((RootViewSurfaceTaker) ViewRootImpl.this.mView).setSurfaceFormat(format);
        }

        public void setType(int type) {
            ((RootViewSurfaceTaker) ViewRootImpl.this.mView).setSurfaceType(type);
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
            ((RootViewSurfaceTaker) ViewRootImpl.this.mView).setSurfaceKeepScreenOn(screenOn);
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

        void reset(int _step) {
            this.position = 0.0f;
            this.acceleration = 1.0f;
            this.lastMoveTime = 0;
            this.step = _step;
            this.dir = 0;
        }

        float collect(float off, long time, String axis) {
            long normTime;
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
                float scale;
                if (delta < normTime) {
                    scale = ((float) (normTime - delta)) * ACCEL_MOVE_SCALING_FACTOR;
                    if (scale > 1.0f) {
                        acc *= scale;
                    }
                    if (acc >= MAX_ACCELERATION) {
                        acc = MAX_ACCELERATION;
                    }
                    this.acceleration = acc;
                } else {
                    scale = ((float) (delta - normTime)) * ACCEL_MOVE_SCALING_FACTOR;
                    if (scale > 1.0f) {
                        acc /= scale;
                    }
                    if (acc <= 1.0f) {
                        acc = 1.0f;
                    }
                    this.acceleration = acc;
                }
            }
            this.position += off;
            return Math.abs(this.position);
        }

        int generate() {
            int movement = 0;
            this.nonAccelMovement = 0;
            while (true) {
                int dir = this.position >= 0.0f ? 1 : -1;
                switch (this.step) {
                    case 0:
                        if (Math.abs(this.position) >= FIRST_MOVEMENT_THRESHOLD) {
                            movement += dir;
                            this.nonAccelMovement += dir;
                            this.step = 1;
                            break;
                        }
                        return movement;
                    case 1:
                        if (Math.abs(this.position) >= SECOND_CUMULATIVE_MOVEMENT_THRESHOLD) {
                            movement += dir;
                            this.nonAccelMovement += dir;
                            this.position -= ((float) dir) * SECOND_CUMULATIVE_MOVEMENT_THRESHOLD;
                            this.step = 2;
                            break;
                        }
                        return movement;
                    default:
                        if (Math.abs(this.position) >= 1.0f) {
                            movement += dir;
                            this.position -= ((float) dir) * 1.0f;
                            float acc = this.acceleration * 1.1f;
                            if (acc >= MAX_ACCELERATION) {
                                acc = this.acceleration;
                            }
                            this.acceleration = acc;
                            break;
                        }
                        return movement;
                }
            }
        }
    }

    final class TraversalRunnable implements Runnable {
        TraversalRunnable() {
        }

        public void run() {
            boolean viewScrollChanged = ViewRootImpl.this.mAttachInfo.mViewScrollChanged;
            ViewRootImpl.this.doTraversal();
            if (ViewRootImpl.sIsFirstFrame) {
                ViewRootImpl.setIsFirstFrame(false);
                String pkg = ViewRootImpl.this.mContext == null ? "Unknown" : ViewRootImpl.this.mContext.getPackageName();
                Jlog.d(337, pkg, LogException.NO_VALUE);
                if (Jlog.isPerfTest()) {
                    Jlog.i(JlogConstants.JLID_ACTIVITY_DISPLAY, "pid=" + Process.myPid() + "&pkg=" + pkg);
                }
            }
            int windowtype = ViewRootImpl.this.mWindowAttributes.type;
            boolean not_care_window = (windowtype <= LayoutParams.LAST_SUB_WINDOW || windowtype == 2004 || windowtype == 2013) ? false : windowtype != 2011;
            if (!not_care_window && ViewRootImpl.this.mChoreographer.isNeedDraw) {
                if (ViewRootImpl.sLastRelayoutNotifyTime != 0 || Math.abs(System.currentTimeMillis() - ViewRootImpl.sLastRelayoutNotifyTime) > ViewRootImpl.sRelayoutNotifyPeriod) {
                    ViewRootImpl.dispatchWindowLayoutChange();
                    ViewRootImpl.sLastRelayoutNotifyTime = System.currentTimeMillis();
                }
                ViewRootImpl.this.jank_processAfterTraversal(viewScrollChanged);
            }
        }
    }

    final class ViewPostImeInputStage extends InputStage {
        public ViewPostImeInputStage(InputStage next) {
            super(next);
        }

        protected int onProcess(QueuedInputEvent q) {
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

        protected void onDeliverToNext(QueuedInputEvent q) {
            if (ViewRootImpl.this.mUnbufferedInputDispatch && (q.mEvent instanceof MotionEvent) && ((MotionEvent) q.mEvent).isTouchEvent() && ViewRootImpl.isTerminalInputEvent(q.mEvent)) {
                ViewRootImpl.this.mUnbufferedInputDispatch = false;
                ViewRootImpl.this.scheduleConsumeBatchedInput();
            }
            super.onDeliverToNext(q);
        }

        private boolean performFocusNavigation(KeyEvent event) {
            int direction = 0;
            switch (event.getKeyCode()) {
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
                case 61:
                    if (!event.hasNoModifiers()) {
                        if (event.hasModifiers(1)) {
                            direction = 1;
                            break;
                        }
                    }
                    direction = 2;
                    break;
                    break;
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
            View focused = ViewRootImpl.this.mView.findFocus();
            if (focused == null && ViewRootImpl.this.mView.restoreDefaultFocus()) {
                return true;
            }
            View cluster;
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
            KeyEvent event = q.mEvent;
            if (ViewRootImpl.this.mView.dispatchKeyEvent(event)) {
                return 1;
            }
            if (shouldDropInputEvent(q)) {
                return 2;
            }
            int groupNavigationDirection = 0;
            if (event.getAction() == 0 && event.getKeyCode() == 61) {
                if (KeyEvent.metaStateHasModifiers(event.getMetaState(), 65536)) {
                    groupNavigationDirection = 2;
                } else if (KeyEvent.metaStateHasModifiers(event.getMetaState(), 65537)) {
                    groupNavigationDirection = 1;
                }
            }
            if (event.getAction() == 0 && (KeyEvent.metaStateHasNoModifiers(event.getMetaState()) ^ 1) != 0 && event.getRepeatCount() == 0 && (KeyEvent.isModifierKey(event.getKeyCode()) ^ 1) != 0 && groupNavigationDirection == 0) {
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
            MotionEvent event = q.mEvent;
            ViewRootImpl.this.mAttachInfo.mUnbufferedDispatchRequested = false;
            ViewRootImpl.this.mAttachInfo.mHandlingPointerEvent = true;
            int action = event.getAction();
            if (HwFrameworkFactory.getHwViewRootImpl().filterDecorPointerEvent(ViewRootImpl.this.mContext, event, action, ViewRootImpl.this.mWindowAttributes, ViewRootImpl.this.mDisplay)) {
                return 1;
            }
            boolean handled;
            if (action == 1) {
                MotionEvent mv = HwFrameworkFactory.getHwViewRootImpl().getRedispatchEvent();
                if (mv != null) {
                    if (!ViewRootImpl.this.mView.dispatchPointerEvent(mv)) {
                        Log.w(ViewRootImpl.TAG, "redispatch downevent failed!");
                    }
                    mv.recycle();
                }
            }
            if (ViewRootImpl.this.mDisplay == null || !HwPCUtils.isValidExtDisplayId(ViewRootImpl.this.mDisplay.getDisplayId())) {
                handled = false;
            } else {
                handled = HwFrameworkFactory.getHwViewRootImpl().interceptMotionEvent(ViewRootImpl.this.mView, event);
            }
            if (!(handled || ViewRootImpl.this.mView == null)) {
                handled = ViewRootImpl.this.mView.dispatchPointerEvent(event);
            }
            maybeUpdatePointerIcon(event);
            ViewRootImpl.this.maybeUpdateTooltip(event);
            ViewRootImpl.this.mAttachInfo.mHandlingPointerEvent = false;
            if (ViewRootImpl.this.mAttachInfo.mUnbufferedDispatchRequested && (ViewRootImpl.this.mUnbufferedInputDispatch ^ 1) != 0) {
                ViewRootImpl.this.mUnbufferedInputDispatch = true;
                if (ViewRootImpl.this.mConsumeBatchedInputScheduled) {
                    ViewRootImpl.this.scheduleConsumeBatchedInputImmediately();
                }
            }
            return handled ? 1 : 0;
        }

        private void maybeUpdatePointerIcon(MotionEvent event) {
            if (event.getPointerCount() == 1 && event.isFromSource(InputDevice.SOURCE_MOUSE)) {
                if (event.getActionMasked() == 9 || event.getActionMasked() == 10) {
                    ViewRootImpl.this.mPointerIconType = 1;
                }
                if (event.getActionMasked() != 10 && !ViewRootImpl.this.updatePointerIcon(event) && event.getActionMasked() == 7) {
                    ViewRootImpl.this.mPointerIconType = 1;
                }
            }
        }

        /* JADX WARNING: Missing block: B:6:0x0020, code:
            return 1;
     */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        private int processTrackballEvent(QueuedInputEvent q) {
            MotionEvent event = q.mEvent;
            if ((!event.isFromSource(InputDevice.SOURCE_MOUSE_RELATIVE) || (ViewRootImpl.this.hasPointerCapture() && !ViewRootImpl.this.mView.dispatchCapturedPointerEvent(event))) && !ViewRootImpl.this.mView.dispatchTrackballEvent(event)) {
                return 0;
            }
            return 1;
        }

        private int processGenericMotionEvent(QueuedInputEvent q) {
            if (ViewRootImpl.this.mView.dispatchGenericMotionEvent(q.mEvent)) {
                return 1;
            }
            return 0;
        }
    }

    final class ViewPreImeInputStage extends InputStage {
        public ViewPreImeInputStage(InputStage next) {
            super(next);
        }

        protected int onProcess(QueuedInputEvent q) {
            if (q.mEvent instanceof KeyEvent) {
                return processKeyEvent(q);
            }
            return 0;
        }

        private int processKeyEvent(QueuedInputEvent q) {
            if (ViewRootImpl.this.mView.dispatchKeyEventPreIme(q.mEvent)) {
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

        /* JADX WARNING: Missing block: B:31:0x00e0, code:
            if (r40.this$0.mDisplay.getDisplayId() == r14.argi3) goto L_0x0007;
     */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public void handleMessage(Message msg) {
            SomeArgs args;
            InputMethodManager imm;
            switch (msg.what) {
                case 1:
                    ((View) msg.obj).invalidate();
                    break;
                case 2:
                    InvalidateInfo info = msg.obj;
                    info.target.invalidate(info.left, info.top, info.right, info.bottom);
                    info.recycle();
                    break;
                case 3:
                    ViewRootImpl.this.doDie();
                    break;
                case 4:
                    args = msg.obj;
                    HwFrameworkFactory.getHwViewRootImpl().clearDisplayPoint();
                    if (ViewRootImpl.this.mWinFrame.equals(args.arg1)) {
                        if (ViewRootImpl.this.mPendingOverscanInsets.equals(args.arg5)) {
                            if (ViewRootImpl.this.mPendingContentInsets.equals(args.arg2)) {
                                if (ViewRootImpl.this.mPendingStableInsets.equals(args.arg6)) {
                                    if (ViewRootImpl.this.mPendingVisibleInsets.equals(args.arg3)) {
                                        if (ViewRootImpl.this.mPendingOutsets.equals(args.arg7)) {
                                            if (ViewRootImpl.this.mPendingBackDropFrame.equals(args.arg8)) {
                                                if (args.arg4 == null) {
                                                    if (args.argi1 == 0) {
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
                case 5:
                    HwFrameworkFactory.getHwViewRootImpl().clearDisplayPoint();
                    if (ViewRootImpl.this.mAdded) {
                        args = (SomeArgs) msg.obj;
                        MergedConfiguration mergedConfiguration = args.arg4;
                        int displayId = args.argi3;
                        boolean displayChanged = ViewRootImpl.this.mDisplay.getDisplayId() != displayId;
                        if (!ViewRootImpl.this.mLastReportedMergedConfiguration.equals(mergedConfiguration)) {
                            ViewRootImpl viewRootImpl = ViewRootImpl.this;
                            if (!displayChanged) {
                                displayId = -1;
                            }
                            viewRootImpl.performConfigurationChange(mergedConfiguration, false, displayId);
                        } else if (displayChanged) {
                            ViewRootImpl.this.onMovedToDisplay(displayId, ViewRootImpl.this.mLastConfigurationFromResources);
                        }
                        int framesChanged;
                        if (ViewRootImpl.this.mWinFrame.equals(args.arg1) && (ViewRootImpl.this.mPendingOverscanInsets.equals(args.arg5) ^ 1) == 0 && (ViewRootImpl.this.mPendingContentInsets.equals(args.arg2) ^ 1) == 0 && (ViewRootImpl.this.mPendingStableInsets.equals(args.arg6) ^ 1) == 0 && (ViewRootImpl.this.mPendingVisibleInsets.equals(args.arg3) ^ 1) == 0) {
                            framesChanged = ViewRootImpl.this.mPendingOutsets.equals(args.arg7) ^ 1;
                        } else {
                            framesChanged = 1;
                        }
                        ViewRootImpl.this.mWinFrame.set((Rect) args.arg1);
                        ViewRootImpl.this.mPendingOverscanInsets.set((Rect) args.arg5);
                        ViewRootImpl.this.mPendingContentInsets.set((Rect) args.arg2);
                        ViewRootImpl.this.mPendingStableInsets.set((Rect) args.arg6);
                        ViewRootImpl.this.mPendingVisibleInsets.set((Rect) args.arg3);
                        ViewRootImpl.this.mPendingOutsets.set((Rect) args.arg7);
                        ViewRootImpl.this.mPendingBackDropFrame.set((Rect) args.arg8);
                        ViewRootImpl.this.mForceNextWindowRelayout = args.argi1 != 0;
                        ViewRootImpl.this.mPendingAlwaysConsumeNavBar = args.argi2 != 0;
                        args.recycle();
                        if (msg.what == 5) {
                            ViewRootImpl.this.reportNextDraw();
                        }
                        if (!(ViewRootImpl.this.mView == null || framesChanged == 0)) {
                            ViewRootImpl.forceLayout(ViewRootImpl.this.mView);
                        }
                        ViewRootImpl.this.requestLayout();
                        break;
                    }
                    break;
                case 6:
                    if (ViewRootImpl.this.mAdded) {
                        boolean hasWindowFocus = msg.arg1 != 0;
                        ViewRootImpl.this.mAttachInfo.mHasWindowFocus = hasWindowFocus;
                        ViewRootImpl.this.profileRendering(hasWindowFocus);
                        if (hasWindowFocus) {
                            ViewRootImpl.this.ensureTouchModeLocally(msg.arg2 != 0);
                            if (ViewRootImpl.this.mAttachInfo.mThreadedRenderer != null && ViewRootImpl.this.mSurface.isValid()) {
                                ViewRootImpl.this.mFullRedrawNeeded = true;
                                if (ViewRootImpl.this.mTag.contains(Surface.TOAST)) {
                                    Log.w(ViewRootImpl.this.mTag, "EGLdebug  focusChanged surface " + ViewRootImpl.this.mSurface);
                                }
                                try {
                                    LayoutParams lp = ViewRootImpl.this.mWindowAttributes;
                                    ViewRootImpl.this.mAttachInfo.mThreadedRenderer.initializeIfNeeded(ViewRootImpl.this.mWidth, ViewRootImpl.this.mHeight, ViewRootImpl.this.mAttachInfo, ViewRootImpl.this.mSurface, lp != null ? lp.surfaceInsets : null);
                                } catch (Throwable e) {
                                    Log.e(ViewRootImpl.this.mTag, "OutOfResourcesException locking surface", e);
                                    try {
                                        if (!ViewRootImpl.this.mWindowSession.outOfMemory(ViewRootImpl.this.mWindow)) {
                                            Slog.w(ViewRootImpl.this.mTag, "No processes killed for memory; killing self");
                                            Process.killProcess(Process.myPid());
                                        }
                                    } catch (RemoteException e2) {
                                    }
                                    sendMessageDelayed(obtainMessage(msg.what, msg.arg1, msg.arg2), ViewRootImpl.MIN_PERIOD);
                                    return;
                                }
                            }
                        }
                        ViewRootImpl.this.mLastWasImTarget = LayoutParams.mayUseInputMethod(ViewRootImpl.this.mWindowAttributes.flags);
                        imm = InputMethodManager.peekInstance();
                        if (!(imm == null || !ViewRootImpl.this.mLastWasImTarget || (ViewRootImpl.this.isInLocalFocusMode() ^ 1) == 0)) {
                            imm.onPreWindowFocus(ViewRootImpl.this.mView, hasWindowFocus);
                        }
                        if (ViewRootImpl.this.mView != null) {
                            ViewRootImpl.this.mAttachInfo.mKeyDispatchState.reset();
                            ViewRootImpl.this.mView.dispatchWindowFocusChanged(hasWindowFocus);
                            ViewRootImpl.this.mAttachInfo.mTreeObserver.dispatchOnWindowFocusChange(hasWindowFocus);
                            if (ViewRootImpl.this.mAttachInfo.mTooltipHost != null) {
                                ViewRootImpl.this.mAttachInfo.mTooltipHost.-android_view_View-mthref-1();
                            }
                        }
                        if (!hasWindowFocus) {
                            if (ViewRootImpl.this.mPointerCapture) {
                                ViewRootImpl.this.handlePointerCaptureChanged(false);
                                break;
                            }
                        }
                        if (!(imm == null || !ViewRootImpl.this.mLastWasImTarget || (ViewRootImpl.this.isInLocalFocusMode() ^ 1) == 0)) {
                            imm.onPostWindowFocus(ViewRootImpl.this.mView, ViewRootImpl.this.mView.findFocus(), ViewRootImpl.this.mWindowAttributes.softInputMode, ViewRootImpl.this.mHasHadWindowFocus ^ 1, ViewRootImpl.this.mWindowAttributes.flags);
                        }
                        LayoutParams layoutParams = ViewRootImpl.this.mWindowAttributes;
                        layoutParams.softInputMode &= -257;
                        layoutParams = (LayoutParams) ViewRootImpl.this.mView.getLayoutParams();
                        layoutParams.softInputMode &= -257;
                        ViewRootImpl.this.mHasHadWindowFocus = true;
                        if ("com.tencent.mm".equals(ViewRootImpl.this.mContext.getPackageName())) {
                            ViewRootImpl.this.mView.sendAccessibilityEvent(32);
                            break;
                        }
                    }
                    break;
                case 7:
                    args = (SomeArgs) msg.obj;
                    ViewRootImpl.this.enqueueInputEvent(args.arg1, args.arg2, 0, true);
                    args.recycle();
                    break;
                case 8:
                    ViewRootImpl.this.handleAppVisibility(msg.arg1 != 0);
                    break;
                case 9:
                    ViewRootImpl.this.handleGetNewSurface();
                    break;
                case 11:
                    InputEvent event = msg.obj;
                    if ((event.getFlags() & 8) != 0) {
                        event = KeyEvent.changeFlags(event, event.getFlags() & -9);
                    }
                    ViewRootImpl.this.enqueueInputEvent(event, null, 1, true);
                    break;
                case 13:
                    imm = InputMethodManager.peekInstance();
                    if (imm != null) {
                        imm.checkFocus();
                        break;
                    }
                    break;
                case 14:
                    if (ViewRootImpl.this.mView != null) {
                        ViewRootImpl.this.mView.onCloseSystemDialogs((String) msg.obj);
                        break;
                    }
                    break;
                case 15:
                case 16:
                    DragEvent event2 = msg.obj;
                    event2.mLocalState = ViewRootImpl.this.mLocalDragState;
                    ViewRootImpl.this.handleDragEvent(event2);
                    break;
                case 17:
                    ViewRootImpl.this.handleDispatchSystemUiVisibilityChanged((SystemUiVisibilityInfo) msg.obj);
                    break;
                case 18:
                    Configuration config = msg.obj;
                    HwFrameworkFactory.getHwViewRootImpl().clearDisplayPoint();
                    if (config.isOtherSeqNewer(ViewRootImpl.this.mLastReportedMergedConfiguration.getMergedConfiguration())) {
                        config = ViewRootImpl.this.mLastReportedMergedConfiguration.getGlobalConfiguration();
                    }
                    ViewRootImpl.this.mPendingMergedConfiguration.setConfiguration(config, ViewRootImpl.this.mLastReportedMergedConfiguration.getOverrideConfiguration());
                    ViewRootImpl.this.performConfigurationChange(ViewRootImpl.this.mPendingMergedConfiguration, false, -1);
                    break;
                case 19:
                    ViewRootImpl.this.mProcessInputEventsScheduled = false;
                    ViewRootImpl.this.doProcessInputEvents();
                    break;
                case 21:
                    ViewRootImpl.this.setAccessibilityFocus(null, null);
                    break;
                case 22:
                    if (ViewRootImpl.this.mView != null) {
                        ViewRootImpl.this.invalidateWorld(ViewRootImpl.this.mView);
                        break;
                    }
                    break;
                case 23:
                    HwFrameworkFactory.getHwViewRootImpl().clearDisplayPoint();
                    if (ViewRootImpl.this.mAdded) {
                        boolean suppress;
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
                        if (ViewRootImpl.this.mDragResizing && ViewRootImpl.this.mResizeMode == 1) {
                            suppress = true;
                        } else {
                            suppress = isDockedDivider;
                        }
                        if (!suppress) {
                            if (ViewRootImpl.this.mView != null) {
                                ViewRootImpl.forceLayout(ViewRootImpl.this.mView);
                            }
                            ViewRootImpl.this.requestLayout();
                            break;
                        }
                        ViewRootImpl.this.maybeHandleWindowMove(ViewRootImpl.this.mWinFrame);
                        break;
                    }
                    break;
                case 24:
                    ViewRootImpl.this.enqueueInputEvent((InputEvent) msg.obj, null, 32, true);
                    break;
                case 25:
                    ViewRootImpl.this.handleDispatchWindowShown();
                    break;
                case 26:
                    ViewRootImpl.this.handleRequestKeyboardShortcuts(msg.obj, msg.arg1);
                    break;
                case 27:
                    ViewRootImpl.this.resetPointerIcon(msg.obj);
                    break;
                case 28:
                    ViewRootImpl.this.handlePointerCaptureChanged(msg.arg1 != 0);
                    break;
                case 29:
                    ViewRootImpl.this.pendingDrawFinished();
                    break;
            }
        }
    }

    static class W extends IWindow.Stub {
        private final WeakReference<ViewRootImpl> mViewAncestor;
        private final IWindowSession mWindowSession;

        W(ViewRootImpl viewAncestor) {
            this.mViewAncestor = new WeakReference(viewAncestor);
            this.mWindowSession = viewAncestor.mWindowSession;
        }

        public void resized(Rect frame, Rect overscanInsets, Rect contentInsets, Rect visibleInsets, Rect stableInsets, Rect outsets, boolean reportDraw, MergedConfiguration mergedConfiguration, Rect backDropFrame, boolean forceLayout, boolean alwaysConsumeNavBar, int displayId) {
            ViewRootImpl viewAncestor = (ViewRootImpl) this.mViewAncestor.get();
            if (viewAncestor != null) {
                viewAncestor.dispatchResized(frame, overscanInsets, contentInsets, visibleInsets, stableInsets, outsets, reportDraw, mergedConfiguration, backDropFrame, forceLayout, alwaysConsumeNavBar, displayId);
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

        /* JADX WARNING: Removed duplicated region for block: B:42:? A:{SYNTHETIC, RETURN} */
        /* JADX WARNING: Removed duplicated region for block: B:22:0x005e A:{SYNTHETIC, Splitter: B:22:0x005e} */
        /* JADX WARNING: Removed duplicated region for block: B:28:0x006a A:{SYNTHETIC, Splitter: B:28:0x006a} */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public void executeCommand(String command, String parameters, ParcelFileDescriptor out) {
            IOException e;
            Throwable th;
            ViewRootImpl viewAncestor = (ViewRootImpl) this.mViewAncestor.get();
            if (viewAncestor != null) {
                View view = viewAncestor.mView;
                if (view == null) {
                    return;
                }
                if (checkCallingPermission("android.permission.DUMP") != 0) {
                    throw new SecurityException("Insufficient permissions to invoke executeCommand() from pid=" + Binder.getCallingPid() + ", uid=" + Binder.getCallingUid());
                }
                OutputStream clientStream = null;
                try {
                    OutputStream clientStream2 = new AutoCloseOutputStream(out);
                    try {
                        ViewDebug.dispatchCommand(view, command, parameters, clientStream2);
                        if (clientStream2 != null) {
                            try {
                                clientStream2.close();
                            } catch (IOException e2) {
                                e2.printStackTrace();
                            }
                        }
                    } catch (IOException e3) {
                        e2 = e3;
                        clientStream = clientStream2;
                        try {
                            e2.printStackTrace();
                            if (clientStream == null) {
                            }
                        } catch (Throwable th2) {
                            th = th2;
                            if (clientStream != null) {
                                try {
                                    clientStream.close();
                                } catch (IOException e22) {
                                    e22.printStackTrace();
                                }
                            }
                            throw th;
                        }
                    } catch (Throwable th3) {
                        th = th3;
                        clientStream = clientStream2;
                        if (clientStream != null) {
                        }
                        throw th;
                    }
                } catch (IOException e4) {
                    e22 = e4;
                    e22.printStackTrace();
                    if (clientStream == null) {
                        try {
                            clientStream.close();
                        } catch (IOException e222) {
                            e222.printStackTrace();
                        }
                    }
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

        public void unRegisterWindowObserver(IWindowLayoutObserver observer) {
            synchronized (ViewRootImpl.sWindowLayoutObservers) {
                ViewRootImpl.sWindowLayoutObservers.unregister(observer);
            }
        }
    }

    final class WindowInputEventReceiver extends InputEventReceiver {
        public WindowInputEventReceiver(InputChannel inputChannel, Looper looper) {
            super(inputChannel, looper);
        }

        public void onInputEvent(InputEvent event) {
            if (Log.HWINFO) {
                if (event instanceof MotionEvent) {
                    MotionEvent myEvent = (MotionEvent) event;
                    if (((double) myEvent.getX()) > 0.0d || ((double) myEvent.getY()) > 0.0d) {
                        Log.v("InputEvent", "L3:" + myEvent.mSeq + NativeLibraryHelper.CLEAR_ABI_OVERRIDE + ((int) Math.rint((double) myEvent.getX())) + NativeLibraryHelper.CLEAR_ABI_OVERRIDE + ((int) Math.rint((double) myEvent.getY())));
                    }
                } else if (event instanceof KeyEvent) {
                    KeyEvent myEvent2 = (KeyEvent) event;
                    Log.v("InputEvent", "L3:" + myEvent2.mSeq + NativeLibraryHelper.CLEAR_ABI_OVERRIDE + myEvent2.getKeyCode() + NativeLibraryHelper.CLEAR_ABI_OVERRIDE + myEvent2.getAction());
                }
            }
            if (HwFrameworkFactory.getHwViewRootImpl().shouldQueueInputEvent(event, ViewRootImpl.this.mContext, ViewRootImpl.this.mView, ViewRootImpl.this.mWindowAttributes)) {
                ViewRootImpl.this.enqueueInputEvent(event, this, 0, true);
            } else {
                finishInputEvent(event, false);
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

    public static synchronized void setIsFirstFrame(boolean isFirstFrame) {
        synchronized (ViewRootImpl.class) {
            sIsFirstFrame = isFirstFrame;
        }
    }

    public static void setEnablePartialUpdate(final boolean isEnable) {
        boolean enable = isEnable;
        new Thread(new Runnable() {
            public void run() {
                if (isEnable) {
                    SystemProperties.set("sys.refresh.dirty", "1");
                } else {
                    SystemProperties.set("sys.refresh.dirty", "0");
                }
            }
        }).start();
    }

    public ViewRootImpl(Context context, Display display) {
        InputEventConsistencyVerifier inputEventConsistencyVerifier;
        if (InputEventConsistencyVerifier.isInstrumentationEnabled()) {
            inputEventConsistencyVerifier = new InputEventConsistencyVerifier(this, 0);
        } else {
            inputEventConsistencyVerifier = null;
        }
        this.mInputEventConsistencyVerifier = inputEventConsistencyVerifier;
        this.mTag = TAG;
        this.mProfile = false;
        this.mDisplayListener = new DisplayListener() {
            public void onDisplayChanged(int displayId) {
                if (ViewRootImpl.this.mView != null && ViewRootImpl.this.mDisplay.getDisplayId() == displayId) {
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
        this.mWindowStoppedCallbacks = new ArrayList();
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
        this.mAttachInfo = new AttachInfo(this.mWindowSession, this.mWindow, display, this, this.mHandler, this, context);
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
            sAlwaysAssignFocus = true;
            sCompatibilityDone = true;
        }
        loadSystemProperties();
        this.mPkgName = context.getPackageName();
        this.mDebugRefreshDirty = debugRefreshDirty();
        HwFrameworkFactory.getHwNsdImpl().createEventAnalyzed();
        sSLBSwitch = HwFrameworkFactory.getHwNsdImpl().isSLBSwitchOn(this.mPkgName);
        this.mPartialUpdateSwitch = SystemProperties.getBoolean("sys.refresh.switch", true);
        Point pt = new Point();
        this.mDisplay.getRealSize(pt);
        HwFrameworkFactory.getHwViewRootImpl().setRealSize(pt);
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
            this.mSurfaceHolderCallback = ((RootViewSurfaceTaker) this.mView).willYouTakeTheSurface();
            if (this.mSurfaceHolderCallback != null) {
                this.mSurfaceHolder = new TakenSurfaceHolder();
                this.mSurfaceHolder.setFormat(0);
                this.mSurfaceHolder.addCallback(this.mSurfaceHolderCallback);
            } else {
                this.mSurfaceHolder = null;
            }
            this.mInputQueueCallback = ((RootViewSurfaceTaker) this.mView).willYouTakeTheInputQueue();
            if (this.mInputQueueCallback != null) {
                this.mInputQueueCallback.onInputQueueCreated(this.mInputQueue);
            }
        }
    }

    /* JADX WARNING: Missing block: B:119:0x0546, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void setView(View view, LayoutParams attrs, View panelParentView) {
        synchronized (this) {
            if (this.mView == null) {
                float f;
                int res;
                this.mView = view;
                this.mAttachInfo.mDisplayState = this.mDisplay.getState();
                this.mDisplayManager.registerDisplayListener(this.mDisplayListener, this.mHandler);
                this.mViewLayoutDirectionInitial = this.mView.getRawLayoutDirection();
                this.mFallbackEventHandler.setView(view);
                this.mWindowAttributes.copyFrom(attrs);
                if (this.mWindowAttributes.packageName == null) {
                    this.mWindowAttributes.packageName = this.mBasePackageName;
                }
                attrs = this.mWindowAttributes;
                setTag();
                this.mSurface.mAppName = this.mBasePackageName;
                this.mClientWindowLayoutFlags = attrs.flags;
                setAccessibilityFocus(null, null);
                if (view instanceof RootViewSurfaceTaker) {
                    this.mSurfaceHolderCallback = ((RootViewSurfaceTaker) view).willYouTakeTheSurface();
                    if (this.mSurfaceHolderCallback != null) {
                        this.mSurfaceHolder = new TakenSurfaceHolder();
                        this.mSurfaceHolder.setFormat(0);
                        this.mSurfaceHolder.addCallback(this.mSurfaceHolderCallback);
                    }
                }
                if (!attrs.hasManualSurfaceInsets) {
                    attrs.setSurfaceInsets(view, false, true);
                }
                CompatibilityInfo compatibilityInfo = this.mDisplay.getDisplayAdjustments().getCompatibilityInfo();
                this.mTranslator = compatibilityInfo.getTranslator();
                if (this.mSurfaceHolder == null) {
                    enableHardwareAcceleration(attrs);
                }
                boolean restore = false;
                if (this.mTranslator != null && (attrs.hwFlags & 1048576) == 0) {
                    this.mSurface.setCompatibilityTranslator(this.mTranslator);
                    restore = true;
                    attrs.backup();
                    this.mTranslator.translateWindowLayout(attrs);
                }
                if (!compatibilityInfo.supportsScreen()) {
                    attrs.privateFlags |= 128;
                    this.mLastInCompatMode = true;
                }
                this.mSoftInputMode = attrs.softInputMode;
                this.mWindowAttributesChanged = true;
                this.mWindowAttributesChangesFlag = -1;
                this.mAttachInfo.mRootView = view;
                this.mAttachInfo.mScalingRequired = this.mTranslator != null;
                AttachInfo attachInfo = this.mAttachInfo;
                if (this.mTranslator == null) {
                    f = 1.0f;
                } else {
                    f = this.mTranslator.applicationScale;
                }
                attachInfo.mApplicationScale = f;
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
                    res = this.mWindowSession.addToDisplay(this.mWindow, this.mSeq, this.mWindowAttributes, getHostVisibility(), this.mDisplay.getDisplayId(), this.mAttachInfo.mContentInsets, this.mAttachInfo.mStableInsets, this.mAttachInfo.mOutsets, this.mInputChannel);
                    if (restore) {
                        attrs.restore();
                    }
                    if (this.mTranslator != null) {
                        this.mTranslator.translateRectInScreenToAppWindow(this.mAttachInfo.mContentInsets);
                    }
                    this.mPendingOverscanInsets.set(0, 0, 0, 0);
                    this.mPendingContentInsets.set(this.mAttachInfo.mContentInsets);
                    this.mPendingStableInsets.set(this.mAttachInfo.mStableInsets);
                    this.mPendingVisibleInsets.set(0, 0, 0, 0);
                    this.mAttachInfo.mAlwaysConsumeNavBar = (res & 4) != 0;
                    this.mPendingAlwaysConsumeNavBar = this.mAttachInfo.mAlwaysConsumeNavBar;
                } catch (RemoteException e) {
                    this.mAdded = false;
                    this.mView = null;
                    this.mAttachInfo.mRootView = null;
                    this.mInputChannel = null;
                    this.mFallbackEventHandler.setView(null);
                    unscheduleTraversals();
                    setAccessibilityFocus(null, null);
                    throw new RuntimeException("Adding window failed", e);
                } catch (Throwable th) {
                    if (restore) {
                        attrs.restore();
                    }
                }
                if (res < 0) {
                    this.mAttachInfo.mRootView = null;
                    this.mAdded = false;
                    this.mFallbackEventHandler.setView(null);
                    unscheduleTraversals();
                    setAccessibilityFocus(null, null);
                    switch (res) {
                        case -10:
                            throw new InvalidDisplayException("Unable to add window " + this.mWindow + " -- the specified window type " + this.mWindowAttributes.type + " is not valid");
                        case -9:
                            throw new InvalidDisplayException("Unable to add window " + this.mWindow + " -- the specified display can not be found");
                        case -8:
                            throw new BadTokenException("Unable to add window " + this.mWindow + " -- permission denied for window type " + this.mWindowAttributes.type);
                        case -7:
                            throw new BadTokenException("Unable to add window " + this.mWindow + " -- another window of type " + this.mWindowAttributes.type + " already exists");
                        case -6:
                            return;
                        case -5:
                            throw new BadTokenException("Unable to add window -- window " + this.mWindow + " has already been added");
                        case -4:
                            throw new BadTokenException("Unable to add window -- app for token " + attrs.token + " is exiting");
                        case -3:
                            throw new BadTokenException("Unable to add window -- token " + attrs.token + " is not for an application");
                        case -2:
                        case -1:
                            throw new BadTokenException("Unable to add window -- token " + attrs.token + " is not valid; is your activity running?");
                        default:
                            throw new RuntimeException("Unable to add window -- unknown error code " + res);
                    }
                } else {
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
                    CharSequence counterSuffix = attrs.getTitle();
                    this.mSyntheticInputStage = new SyntheticInputStage();
                    InputStage earlyPostImeInputStage = new EarlyPostImeInputStage(new NativePostImeInputStage(new ViewPostImeInputStage(this.mSyntheticInputStage), "aq:native-post-ime:" + counterSuffix));
                    this.mFirstInputStage = new NativePreImeInputStage(new ViewPreImeInputStage(new ImeInputStage(earlyPostImeInputStage, "aq:ime:" + counterSuffix)), "aq:native-pre-ime:" + counterSuffix);
                    this.mFirstPostImeInputStage = earlyPostImeInputStage;
                    this.mPendingInputEventQueueLengthCounterName = "aq:pending:" + counterSuffix;
                }
            }
        }
    }

    private void setTag() {
        String[] split = this.mWindowAttributes.getTitle().toString().split("\\.");
        if (split.length > 0) {
            this.mTag = "ViewRootImpl[" + split[split.length - 1] + "]";
        }
    }

    private boolean isInLocalFocusMode() {
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

    void destroyHardwareResources() {
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

    public void registerVectorDrawableAnimator(VectorDrawableAnimatorRT animator) {
        if (this.mAttachInfo.mThreadedRenderer != null) {
            this.mAttachInfo.mThreadedRenderer.registerVectorDrawableAnimator(animator);
        }
    }

    private void enableHardwareAcceleration(LayoutParams attrs) {
        this.mAttachInfo.mHardwareAccelerated = false;
        this.mAttachInfo.mHardwareAccelerationRequested = false;
        if (this.mTranslator == null) {
            if (((attrs.flags & 16777216) != 0) && ThreadedRenderer.isAvailable()) {
                boolean fakeHwAccelerated = (attrs.privateFlags & 1) != 0;
                boolean forceHwAccelerated = (attrs.privateFlags & 2) != 0;
                if (fakeHwAccelerated) {
                    this.mAttachInfo.mHardwareAccelerationRequested = true;
                } else if (!ThreadedRenderer.sRendererDisabled || (ThreadedRenderer.sSystemRendererDisabled && forceHwAccelerated)) {
                    if (this.mAttachInfo.mThreadedRenderer != null) {
                        this.mAttachInfo.mThreadedRenderer.destroy();
                    }
                    Rect insets = attrs.surfaceInsets;
                    boolean hasSurfaceInsets = (insets.left == 0 && insets.right == 0 && insets.top == 0) ? insets.bottom != 0 : true;
                    this.mAttachInfo.mThreadedRenderer = ThreadedRenderer.create(this.mContext, attrs.format == -1 ? hasSurfaceInsets : true, attrs.getTitle().toString());
                    if (this.mAttachInfo.mThreadedRenderer != null) {
                        AttachInfo attachInfo = this.mAttachInfo;
                        this.mAttachInfo.mHardwareAccelerationRequested = true;
                        attachInfo.mHardwareAccelerated = true;
                    }
                }
            }
        }
    }

    public View getView() {
        return this.mView;
    }

    final WindowLeaked getLocation() {
        return this.mLocation;
    }

    /* JADX WARNING: Missing block: B:36:0x00c8, code:
            if (r10.mWindowAttributes.surfaceInsets.bottom == r2) goto L_0x007a;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    void setLayoutParams(LayoutParams attrs, boolean newView) {
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
            LayoutParams layoutParams = this.mWindowAttributes;
            layoutParams.privateFlags |= compatibleWindowFlag;
            if (this.mWindowAttributes.preservePreviousSurfaceInsets) {
                this.mWindowAttributes.surfaceInsets.set(oldInsetLeft, oldInsetTop, oldInsetRight, oldInsetBottom);
                this.mWindowAttributes.hasManualSurfaceInsets = oldHasManualSurfaceInsets;
            } else {
                if (this.mWindowAttributes.surfaceInsets.left == oldInsetLeft && this.mWindowAttributes.surfaceInsets.top == oldInsetTop) {
                    if (this.mWindowAttributes.surfaceInsets.right == oldInsetRight) {
                    }
                }
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

    void handleAppVisibility(boolean visible) {
        if (this.mAppVisible != visible) {
            this.mAppVisible = visible;
            scheduleTraversals();
            if (!this.mAppVisible) {
                WindowManagerGlobal.trimForeground();
            }
        }
    }

    void handleGetNewSurface() {
        this.mNewSurfaceNeeded = true;
        this.mFullRedrawNeeded = true;
        scheduleTraversals();
    }

    public void onMovedToDisplay(int displayId, Configuration config) {
        if (this.mDisplay.getDisplayId() != displayId) {
            this.mDisplay = ResourcesManager.getInstance().getAdjustedDisplay(displayId, this.mView.getResources());
            this.mAttachInfo.mDisplayState = this.mDisplay.getState();
            this.mView.dispatchMovedToDisplay(this.mDisplay, config);
        }
    }

    void pokeDrawLockIfNeeded() {
        int displayState = this.mAttachInfo.mDisplayState;
        if (this.mView == null || !this.mAdded || !this.mTraversalScheduled) {
            return;
        }
        if (displayState == 3 || (!mSupportAod && displayState == 4)) {
            try {
                this.mWindowSession.pokeDrawLock(this.mWindow);
            } catch (RemoteException e) {
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
        if (!enableRefreshDirty() || this.mInvalidatedChildInPU == null) {
            if ((descendant.mPrivateFlags & 64) != 0) {
                this.mIsAnimating = true;
            }
            invalidate();
        }
    }

    void invalidate() {
        this.mDirty.set(0, 0, this.mWidth, this.mHeight);
        if (!this.mWillDrawSoon) {
            scheduleTraversals();
        }
    }

    void invalidateWorld(View view) {
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
        } else if (dirty.isEmpty() && (this.mIsAnimating ^ 1) != 0) {
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
        if (!(localDirty.isEmpty() || (localDirty.contains(dirty) ^ 1) == 0)) {
            this.mAttachInfo.mSetIgnoreDirtyState = true;
            this.mAttachInfo.mIgnoreDirtyState = true;
        }
        localDirty.union(dirty.left, dirty.top, dirty.right, dirty.bottom);
        float appScale = this.mAttachInfo.mApplicationScale;
        boolean intersected = localDirty.intersect(0, 0, (int) ((((float) this.mWidth) * appScale) + 0.5f), (int) ((((float) this.mHeight) * appScale) + 0.5f));
        if (!intersected) {
            localDirty.setEmpty();
        }
        if (!this.mWillDrawSoon) {
            if (intersected || this.mIsAnimating) {
                scheduleTraversals();
            }
        }
    }

    public void setIsAmbientMode(boolean ambient) {
        this.mIsAmbientMode = ambient;
    }

    void addWindowStoppedCallback(WindowStoppedCallback c) {
        this.mWindowStoppedCallbacks.add(c);
    }

    void removeWindowStoppedCallback(WindowStoppedCallback c) {
        this.mWindowStoppedCallbacks.remove(c);
    }

    void setWindowStopped(boolean stopped) {
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
                ((WindowStoppedCallback) this.mWindowStoppedCallbacks.get(i)).windowStopped(stopped);
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

    int getHostVisibility() {
        return (this.mAppVisible || this.mForceDecorViewVisibility) ? this.mView.getVisibility() : 8;
    }

    public void requestTransitionStart(LayoutTransition transition) {
        if (this.mPendingTransitions == null || (this.mPendingTransitions.contains(transition) ^ 1) != 0) {
            if (this.mPendingTransitions == null) {
                this.mPendingTransitions = new ArrayList();
            }
            this.mPendingTransitions.add(transition);
        }
    }

    void notifyRendererOfFramePending() {
        if (this.mAttachInfo.mThreadedRenderer != null) {
            this.mAttachInfo.mThreadedRenderer.notifyFramePending();
        }
    }

    void scheduleTraversals() {
        if (!this.mTraversalScheduled) {
            this.mTraversalScheduled = true;
            this.mTraversalBarrier = this.mHandler.getLooper().getQueue().postSyncBarrier();
            this.mChoreographer.postCallback(2, this.mTraversalRunnable, null);
            if (!this.mUnbufferedInputDispatch) {
                scheduleConsumeBatchedInput();
            }
            notifyRendererOfFramePending();
            pokeDrawLockIfNeeded();
        }
    }

    void unscheduleTraversals() {
        if (this.mTraversalScheduled) {
            this.mTraversalScheduled = false;
            this.mHandler.getLooper().getQueue().removeSyncBarrier(this.mTraversalBarrier);
            this.mChoreographer.removeCallbacks(2, this.mTraversalRunnable, null);
        }
    }

    void doTraversal() {
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

    private void applyKeepScreenOnFlag(LayoutParams params) {
        if (this.mAttachInfo.mKeepScreenOn) {
            params.flags |= 128;
        } else {
            params.flags = (params.flags & -129) | (this.mClientWindowLayoutFlags & 128);
        }
    }

    private boolean collectViewAttributes() {
        if (this.mAttachInfo.mRecomputeGlobalAttributes) {
            this.mAttachInfo.mRecomputeGlobalAttributes = false;
            boolean oldScreenOn = this.mAttachInfo.mKeepScreenOn;
            this.mAttachInfo.mKeepScreenOn = false;
            this.mAttachInfo.mSystemUiVisibility = 0;
            this.mAttachInfo.mHasSystemUiListeners = false;
            this.mView.dispatchCollectViewAttributes(this.mAttachInfo, 0);
            AttachInfo attachInfo = this.mAttachInfo;
            attachInfo.mSystemUiVisibility &= ~this.mAttachInfo.mDisabledSystemUiVisibility;
            LayoutParams params = this.mWindowAttributes;
            attachInfo = this.mAttachInfo;
            attachInfo.mSystemUiVisibility |= getImpliedSystemUiVisibility(params);
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

    private int getImpliedSystemUiVisibility(LayoutParams params) {
        int vis = 0;
        if ((params.flags & 67108864) != 0) {
            vis = GL10.GL_INVALID_ENUM;
        }
        if ((params.flags & 134217728) != 0) {
            return vis | 768;
        }
        return vis;
    }

    private boolean measureHierarchy(View host, LayoutParams lp, Resources res, int desiredWindowWidth, int desiredWindowHeight) {
        boolean windowSizeMayChange = false;
        long preMeasureTime = 0;
        if (Jlog.isPerfTest()) {
            preMeasureTime = System.nanoTime();
        }
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
        if (!goodMeasure) {
            performMeasure(getRootMeasureSpec(desiredWindowWidth, lp.width), getRootMeasureSpec(desiredWindowHeight, lp.height));
            if (!(this.mWidth == host.getMeasuredWidth() && this.mHeight == host.getMeasuredHeight())) {
                windowSizeMayChange = true;
            }
        }
        if (Jlog.isPerfTest()) {
            Jlog.i(2102, "#ME:" + (((System.nanoTime() - preMeasureTime) + REDUNDANT) / TimeUtils.NANOS_PER_MS));
        }
        return windowSizeMayChange;
    }

    void transformMatrixToGlobal(Matrix m) {
        m.preTranslate((float) this.mAttachInfo.mWindowLeft, (float) this.mAttachInfo.mWindowTop);
    }

    void transformMatrixToLocal(Matrix m) {
        m.postTranslate((float) (-this.mAttachInfo.mWindowLeft), (float) (-this.mAttachInfo.mWindowTop));
    }

    WindowInsets getWindowInsets(boolean forceConstruct) {
        if (this.mLastWindowInsets == null || forceConstruct) {
            this.mDispatchContentInsets.set(this.mAttachInfo.mContentInsets);
            this.mDispatchStableInsets.set(this.mAttachInfo.mStableInsets);
            Rect contentInsets = this.mDispatchContentInsets;
            Rect stableInsets = this.mDispatchStableInsets;
            if (!(forceConstruct || (this.mPendingContentInsets.equals(contentInsets) && (this.mPendingStableInsets.equals(stableInsets) ^ 1) == 0))) {
                contentInsets = this.mPendingContentInsets;
                stableInsets = this.mPendingStableInsets;
            }
            Rect outsets = this.mAttachInfo.mOutsets;
            if (outsets.left > 0 || outsets.top > 0 || outsets.right > 0 || outsets.bottom > 0) {
                contentInsets = new Rect(contentInsets.left + outsets.left, contentInsets.top + outsets.top, contentInsets.right + outsets.right, contentInsets.bottom + outsets.bottom);
            }
            this.mLastWindowInsets = new WindowInsets(contentInsets, null, stableInsets, this.mContext.getResources().getConfiguration().isScreenRound(), this.mAttachInfo.mAlwaysConsumeNavBar);
        }
        return this.mLastWindowInsets;
    }

    void dispatchApplyInsets(View host) {
        host.dispatchApplyWindowInsets(getWindowInsets(true));
    }

    private static boolean shouldUseDisplaySize(LayoutParams lp) {
        if (lp.type == LayoutParams.TYPE_STATUS_BAR_PANEL || lp.type == 2011 || lp.type == 2020) {
            return true;
        }
        return false;
    }

    private int dipToPx(int dip) {
        return (int) ((this.mContext.getResources().getDisplayMetrics().density * ((float) dip)) + 0.5f);
    }

    /* JADX WARNING: Missing block: B:383:0x0a14, code:
            if (r57 == false) goto L_0x05c7;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void performTraversals() {
        View host = this.mView;
        if (host != null && (this.mAdded ^ 1) == 0) {
            boolean viewUserVisibilityChanged;
            Configuration config;
            Point size;
            int desiredWindowWidth;
            int desiredWindowHeight;
            int i;
            boolean computesInternalInsets;
            boolean triggerGlobalLayoutListener;
            this.mIsInTraversal = true;
            this.mWillDrawSoon = true;
            int windowSizeMayChange = false;
            boolean newSurface = false;
            boolean surfaceChanged = false;
            LayoutParams lp = this.mWindowAttributes;
            int viewVisibility = getHostVisibility();
            boolean viewVisibilityChanged = !this.mFirst ? this.mViewVisibility == viewVisibility ? this.mNewSurfaceNeeded : true : false;
            if (this.mFirst) {
                viewUserVisibilityChanged = false;
            } else {
                viewUserVisibilityChanged = (this.mViewVisibility == 0 ? 1 : null) != (viewVisibility == 0 ? 1 : null);
            }
            LayoutParams params = null;
            if (this.mWindowAttributesChanged) {
                this.mWindowAttributesChanged = false;
                surfaceChanged = true;
                params = lp;
            }
            if (this.mDisplay.getDisplayAdjustments().getCompatibilityInfo().supportsScreen() == this.mLastInCompatMode) {
                params = lp;
                this.mFullRedrawNeeded = true;
                this.mLayoutRequested = true;
                if (this.mLastInCompatMode) {
                    lp.privateFlags &= -129;
                    this.mLastInCompatMode = false;
                } else {
                    lp.privateFlags |= 128;
                    this.mLastInCompatMode = true;
                }
            }
            this.mWindowAttributesChangesFlag = 0;
            Rect frame = this.mWinFrame;
            if (this.mFirst) {
                this.mFullRedrawNeeded = true;
                this.mLayoutRequested = true;
                config = this.mContext.getResources().getConfiguration();
                if (shouldUseDisplaySize(lp)) {
                    size = new Point();
                    this.mDisplay.getRealSize(size);
                    desiredWindowWidth = size.x;
                    desiredWindowHeight = size.y;
                } else {
                    desiredWindowWidth = dipToPx(config.screenWidthDp);
                    desiredWindowHeight = dipToPx(config.screenHeightDp);
                }
                this.mAttachInfo.mUse32BitDrawingCache = true;
                this.mAttachInfo.mHasWindowFocus = false;
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
                desiredWindowWidth = frame.width();
                desiredWindowHeight = frame.height();
                if (!(desiredWindowWidth == this.mWidth && desiredWindowHeight == this.mHeight)) {
                    this.mFullRedrawNeeded = true;
                    this.mLayoutRequested = true;
                    windowSizeMayChange = true;
                }
            }
            if (viewVisibilityChanged) {
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
            boolean insetsChanged = false;
            boolean layoutRequested = this.mLayoutRequested ? this.mStopped ? this.mReportNextDraw : true : false;
            if (layoutRequested) {
                boolean windowSizeMayChange2;
                Resources res = this.mView.getContext().getResources();
                if (this.mFirst) {
                    this.mAttachInfo.mInTouchMode = this.mAddedTouchMode ^ 1;
                    ensureTouchModeLocally(this.mAddedTouchMode);
                } else {
                    if (!this.mPendingOverscanInsets.equals(this.mAttachInfo.mOverscanInsets)) {
                        insetsChanged = true;
                    }
                    if (!this.mPendingContentInsets.equals(this.mAttachInfo.mContentInsets)) {
                        insetsChanged = true;
                    }
                    if (!this.mPendingStableInsets.equals(this.mAttachInfo.mStableInsets)) {
                        insetsChanged = true;
                    }
                    if (!this.mPendingVisibleInsets.equals(this.mAttachInfo.mVisibleInsets)) {
                        this.mAttachInfo.mVisibleInsets.set(this.mPendingVisibleInsets);
                    }
                    if (!this.mPendingOutsets.equals(this.mAttachInfo.mOutsets)) {
                        insetsChanged = true;
                    }
                    if (this.mPendingAlwaysConsumeNavBar != this.mAttachInfo.mAlwaysConsumeNavBar) {
                        insetsChanged = true;
                    }
                    if (lp.width == -2 || lp.height == -2) {
                        windowSizeMayChange2 = true;
                        if (shouldUseDisplaySize(lp)) {
                            size = new Point();
                            this.mDisplay.getRealSize(size);
                            desiredWindowWidth = size.x;
                            desiredWindowHeight = size.y;
                        } else {
                            config = res.getConfiguration();
                            desiredWindowWidth = dipToPx(config.screenWidthDp);
                            desiredWindowHeight = dipToPx(config.screenHeightDp);
                        }
                    }
                }
                windowSizeMayChange = windowSizeMayChange2 | measureHierarchy(host, lp, res, desiredWindowWidth, desiredWindowHeight);
            }
            if (collectViewAttributes()) {
                params = lp;
            }
            if (this.mAttachInfo.mForceReportNewAttributes) {
                this.mAttachInfo.mForceReportNewAttributes = false;
                params = lp;
            }
            if (this.mFirst || this.mAttachInfo.mViewVisibilityChanged) {
                this.mAttachInfo.mViewVisibilityChanged = false;
                int resizeMode = this.mSoftInputMode & 240;
                if (resizeMode == 0) {
                    int N = this.mAttachInfo.mScrollContainers.size();
                    for (i = 0; i < N; i++) {
                        if (((View) this.mAttachInfo.mScrollContainers.get(i)).isShown()) {
                            resizeMode = 16;
                        }
                    }
                    if (resizeMode == 0) {
                        resizeMode = 32;
                    }
                    if ((lp.softInputMode & 240) != resizeMode) {
                        lp.softInputMode = (lp.softInputMode & -241) | resizeMode;
                        params = lp;
                    }
                }
            }
            if (params != null) {
                if (!((host.mPrivateFlags & 512) == 0 || PixelFormat.formatHasAlpha(params.format))) {
                    params.format = -3;
                }
                this.mAttachInfo.mOverscanRequested = (params.flags & 33554432) != 0;
            }
            if (this.mApplyInsetsRequested) {
                this.mApplyInsetsRequested = false;
                this.mLastOverscanRequested = this.mAttachInfo.mOverscanRequested;
                dispatchApplyInsets(host);
                if (this.mLayoutRequested) {
                    windowSizeMayChange |= measureHierarchy(host, lp, this.mView.getContext().getResources(), desiredWindowWidth, desiredWindowHeight);
                }
            }
            if (layoutRequested) {
                this.mLayoutRequested = false;
            }
            boolean windowShouldResize = (!layoutRequested || windowSizeMayChange == 0) ? false : (this.mWidth == host.getMeasuredWidth() && this.mHeight == host.getMeasuredHeight() && (lp.width != -2 || frame.width() >= desiredWindowWidth || frame.width() == this.mWidth)) ? (lp.height != -2 || frame.height() >= desiredWindowHeight) ? false : frame.height() != this.mHeight : true;
            int i2 = (this.mDragResizing && this.mResizeMode == 0) ? 1 : 0;
            windowShouldResize = (windowShouldResize | i2) | this.mActivityRelaunched;
            if (this.mAttachInfo.mTreeObserver.hasComputeInternalInsetsListeners()) {
                computesInternalInsets = true;
            } else {
                computesInternalInsets = this.mAttachInfo.mHasNonEmptyGivenInternalInsets;
            }
            boolean insetsPending = false;
            int relayoutResult = 0;
            boolean updatedConfiguration = false;
            int surfaceGenerationId = this.mSurface.getGenerationId();
            boolean isViewVisible = viewVisibility == 0;
            boolean windowRelayoutWasForced = this.mForceNextWindowRelayout;
            if (this.mFirst || windowShouldResize || insetsChanged || viewVisibilityChanged || params != null || this.mForceNextWindowRelayout) {
                this.mForceNextWindowRelayout = false;
                if (isViewVisible) {
                    insetsPending = computesInternalInsets ? !this.mFirst ? viewVisibilityChanged : true : false;
                }
                if (this.mSurfaceHolder != null) {
                    this.mSurfaceHolder.mSurfaceLock.lock();
                    this.mDrawingAllowed = true;
                }
                boolean hwInitialized = false;
                boolean contentInsetsChanged = false;
                boolean hadSurface = this.mSurface.isValid();
                int fl = 0;
                if (params != null) {
                    try {
                        fl = params.flags;
                        if (this.mAttachInfo.mKeepScreenOn) {
                            params.flags |= 128;
                        }
                        params.subtreeSystemUiVisibility = this.mAttachInfo.mSystemUiVisibility;
                        params.hasSystemUiListeners = this.mAttachInfo.mHasSystemUiListeners;
                    } catch (RemoteException e) {
                    }
                }
                if (this.mAttachInfo.mThreadedRenderer != null) {
                    if (this.mAttachInfo.mThreadedRenderer.pauseSurface(this.mSurface)) {
                        this.mDirty.set(0, 0, this.mWidth, this.mHeight);
                    }
                    this.mChoreographer.mFrameInfo.addFlags(1);
                }
                relayoutResult = relayoutWindow(params, viewVisibility, insetsPending);
                if (params != null) {
                    params.flags = fl;
                }
                if (!this.mPendingMergedConfiguration.equals(this.mLastReportedMergedConfiguration)) {
                    performConfigurationChange(this.mPendingMergedConfiguration, this.mFirst ^ 1, -1);
                    updatedConfiguration = true;
                }
                boolean overscanInsetsChanged = this.mPendingOverscanInsets.equals(this.mAttachInfo.mOverscanInsets) ^ 1;
                contentInsetsChanged = this.mPendingContentInsets.equals(this.mAttachInfo.mContentInsets) ^ 1;
                boolean visibleInsetsChanged = this.mPendingVisibleInsets.equals(this.mAttachInfo.mVisibleInsets) ^ 1;
                boolean stableInsetsChanged = this.mPendingStableInsets.equals(this.mAttachInfo.mStableInsets) ^ 1;
                boolean outsetsChanged = this.mPendingOutsets.equals(this.mAttachInfo.mOutsets) ^ 1;
                boolean surfaceSizeChanged = (relayoutResult & 32) != 0;
                boolean alwaysConsumeNavBarChanged = this.mPendingAlwaysConsumeNavBar != this.mAttachInfo.mAlwaysConsumeNavBar;
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
                if (alwaysConsumeNavBarChanged) {
                    this.mAttachInfo.mAlwaysConsumeNavBar = this.mPendingAlwaysConsumeNavBar;
                    contentInsetsChanged = true;
                }
                if (!contentInsetsChanged && this.mLastSystemUiVisibility == this.mAttachInfo.mSystemUiVisibility) {
                    if (!this.mApplyInsetsRequested) {
                        if (this.mLastOverscanRequested == this.mAttachInfo.mOverscanRequested) {
                        }
                    }
                }
                this.mLastSystemUiVisibility = this.mAttachInfo.mSystemUiVisibility;
                this.mLastOverscanRequested = this.mAttachInfo.mOverscanRequested;
                this.mAttachInfo.mOutsets.set(this.mPendingOutsets);
                this.mApplyInsetsRequested = false;
                dispatchApplyInsets(host);
                if (visibleInsetsChanged) {
                    this.mAttachInfo.mVisibleInsets.set(this.mPendingVisibleInsets);
                }
                if (hadSurface) {
                    if (!this.mSurface.isValid()) {
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
                    } else if ((surfaceGenerationId != this.mSurface.getGenerationId() || surfaceSizeChanged || windowRelayoutWasForced) && this.mSurfaceHolder == null && this.mAttachInfo.mThreadedRenderer != null) {
                        this.mFullRedrawNeeded = true;
                        try {
                            if (surfaceControlllerIsValid()) {
                                this.mAttachInfo.mThreadedRenderer.updateSurface(this.mSurface);
                            } else {
                                Log.w(TAG, "##### surfaceControlller is not Valid #### ");
                            }
                        } catch (OutOfResourcesException e2) {
                            handleOutOfResourcesException(e2);
                            return;
                        }
                    }
                } else if (this.mSurface.isValid()) {
                    newSurface = true;
                    this.mFullRedrawNeeded = true;
                    this.mPreviousTransparentRegion.setEmpty();
                    if (this.mAttachInfo.mThreadedRenderer != null) {
                        try {
                            if (this.mTag.contains(Surface.TOAST)) {
                                Log.w(this.mTag, "EGLdebug Surface is" + this.mSurface);
                            }
                            if (surfaceControlllerIsValid()) {
                                hwInitialized = this.mAttachInfo.mThreadedRenderer.initialize(this.mSurface);
                            } else {
                                Log.w(TAG, "##### surfaceControlller is not Valid can not initialize#### ");
                            }
                            if (hwInitialized && (host.mPrivateFlags & 512) == 0) {
                                if (this.mAllocBufferAsync) {
                                    this.mAttachInfo.mThreadedRenderer.allocateBuffers(this.mSurface);
                                } else {
                                    this.mSurface.allocateBuffers();
                                }
                            }
                        } catch (OutOfResourcesException e22) {
                            handleOutOfResourcesException(e22);
                            return;
                        }
                    }
                }
                boolean freeformResizing = (relayoutResult & 16) != 0;
                boolean dragResizing = !freeformResizing ? (relayoutResult & 8) != 0 : true;
                if (this.mDragResizing != dragResizing) {
                    if (dragResizing) {
                        if (freeformResizing) {
                            i2 = 0;
                        } else {
                            i2 = 1;
                        }
                        this.mResizeMode = i2;
                        startDragResizing(this.mPendingBackDropFrame, this.mWinFrame.equals(this.mPendingBackDropFrame), this.mPendingVisibleInsets, this.mPendingStableInsets, this.mResizeMode);
                    } else {
                        endDragResizing();
                    }
                }
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
                    SurfaceHolder.Callback[] callbacks;
                    if (this.mSurface.isValid()) {
                        if (!hadSurface) {
                            this.mSurfaceHolder.ungetCallbacks();
                            this.mIsCreating = true;
                            callbacks = this.mSurfaceHolder.getCallbacks();
                            if (callbacks != null) {
                                for (SurfaceHolder.Callback c : callbacks) {
                                    c.surfaceCreated(this.mSurfaceHolder);
                                }
                            }
                            surfaceChanged = true;
                        }
                        if (surfaceChanged || surfaceGenerationId != this.mSurface.getGenerationId()) {
                            callbacks = this.mSurfaceHolder.getCallbacks();
                            if (callbacks != null) {
                                for (SurfaceHolder.Callback c2 : callbacks) {
                                    c2.surfaceChanged(this.mSurfaceHolder, lp.format, this.mWidth, this.mHeight);
                                }
                            }
                        }
                        this.mIsCreating = false;
                    } else if (hadSurface) {
                        this.mSurfaceHolder.ungetCallbacks();
                        callbacks = this.mSurfaceHolder.getCallbacks();
                        if (callbacks != null) {
                            for (SurfaceHolder.Callback c22 : callbacks) {
                                c22.surfaceDestroyed(this.mSurfaceHolder);
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
                ThreadedRenderer threadedRenderer = this.mAttachInfo.mThreadedRenderer;
                if (threadedRenderer != null && threadedRenderer.isEnabled() && (hwInitialized || this.mWidth != threadedRenderer.getWidth() || this.mHeight != threadedRenderer.getHeight() || this.mNeedsRendererSetup)) {
                    threadedRenderer.setup(this.mWidth, this.mHeight, this.mAttachInfo, this.mWindowAttributes.surfaceInsets);
                    this.mNeedsRendererSetup = false;
                }
                if (!this.mStopped || this.mReportNextDraw) {
                    if (ensureTouchModeLocally((relayoutResult & 1) != 0) || this.mWidth != host.getMeasuredWidth() || this.mHeight != host.getMeasuredHeight() || contentInsetsChanged || updatedConfiguration) {
                        int childWidthMeasureSpec = getRootMeasureSpec(this.mWidth, lp.width);
                        int childHeightMeasureSpec = getRootMeasureSpec(this.mHeight, lp.height);
                        performMeasure(childWidthMeasureSpec, childHeightMeasureSpec);
                        int width = host.getMeasuredWidth();
                        int height = host.getMeasuredHeight();
                        boolean measureAgain = false;
                        if (lp.horizontalWeight > 0.0f) {
                            childWidthMeasureSpec = MeasureSpec.makeMeasureSpec(width + ((int) (((float) (this.mWidth - width)) * lp.horizontalWeight)), 1073741824);
                            measureAgain = true;
                        }
                        if (lp.verticalWeight > 0.0f) {
                            childHeightMeasureSpec = MeasureSpec.makeMeasureSpec(height + ((int) (((float) (this.mHeight - height)) * lp.verticalWeight)), 1073741824);
                            measureAgain = true;
                        }
                        if (measureAgain) {
                            performMeasure(childWidthMeasureSpec, childHeightMeasureSpec);
                        }
                        layoutRequested = true;
                    }
                }
            } else {
                maybeHandleWindowMove(frame);
            }
            boolean didLayout = layoutRequested ? this.mStopped ? this.mReportNextDraw : true : false;
            if (didLayout) {
                triggerGlobalLayoutListener = true;
            } else {
                triggerGlobalLayoutListener = this.mAttachInfo.mRecomputeGlobalAttributes;
            }
            if (didLayout) {
                doRelayoutAsyncly(performLayout(lp, this.mWidth, this.mHeight));
                if ((host.mPrivateFlags & 512) != 0) {
                    host.getLocationInWindow(this.mTmpLocation);
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
                        } catch (RemoteException e3) {
                        }
                    }
                }
            }
            if (triggerGlobalLayoutListener) {
                this.mAttachInfo.mRecomputeGlobalAttributes = false;
                this.mAttachInfo.mTreeObserver.dispatchOnGlobalLayout();
            }
            if (computesInternalInsets) {
                InternalInsetsInfo insets = this.mAttachInfo.mGivenInternalInsets;
                insets.reset();
                this.mAttachInfo.mTreeObserver.dispatchOnComputeInternalInsets(insets);
                this.mAttachInfo.mHasNonEmptyGivenInternalInsets = insets.isEmpty() ^ 1;
                if (insetsPending || (this.mLastGivenInsets.equals(insets) ^ 1) != 0) {
                    Rect contentInsets;
                    Rect visibleInsets;
                    Region touchableRegion;
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
                    } catch (RemoteException e4) {
                    }
                }
            }
            if (this.mFirst && sAlwaysAssignFocus && this.mView != null && !this.mView.hasFocus()) {
                this.mView.restoreDefaultFocus();
            }
            boolean changedVisibility = (viewVisibilityChanged || this.mFirst) ? isViewVisible : false;
            boolean hasWindowFocus = this.mAttachInfo.mHasWindowFocus ? isViewVisible : false;
            boolean regainedFocus = hasWindowFocus ? this.mLostWindowFocus : false;
            if (regainedFocus) {
                this.mLostWindowFocus = false;
            } else if (!hasWindowFocus && this.mHadWindowFocus) {
                this.mLostWindowFocus = true;
            }
            if (changedVisibility || regainedFocus) {
                boolean isToast = this.mWindowAttributes == null ? false : this.mWindowAttributes.type == 2005;
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
            if (hasWindowFocus && (isInLocalFocusMode() ^ 1) != 0) {
                boolean imTarget = LayoutParams.mayUseInputMethod(this.mWindowAttributes.flags);
                if (imTarget != this.mLastWasImTarget) {
                    this.mLastWasImTarget = imTarget;
                    InputMethodManager imm = InputMethodManager.peekInstance();
                    if (imm != null && imTarget) {
                        imm.onPreWindowFocus(this.mView, hasWindowFocus);
                        imm.onPostWindowFocus(this.mView, this.mView.findFocus(), this.mWindowAttributes.softInputMode, this.mHasHadWindowFocus ^ 1, this.mWindowAttributes.flags);
                    }
                }
            }
            if ((relayoutResult & 2) != 0) {
                reportNextDraw();
            }
            if ((!this.mAttachInfo.mTreeObserver.dispatchOnPreDraw() ? isViewVisible ^ 1 : 1) == 0 && (newSurface ^ 1) != 0) {
                if (this.mPendingTransitions != null && this.mPendingTransitions.size() > 0) {
                    for (i = 0; i < this.mPendingTransitions.size(); i++) {
                        ((LayoutTransition) this.mPendingTransitions.get(i)).startChangingAnimations();
                    }
                    this.mPendingTransitions.clear();
                }
                performDraw();
            } else if (isViewVisible) {
                scheduleTraversals();
            } else if (this.mPendingTransitions != null && this.mPendingTransitions.size() > 0) {
                for (i = 0; i < this.mPendingTransitions.size(); i++) {
                    ((LayoutTransition) this.mPendingTransitions.get(i)).endChangingAnimations();
                }
                this.mPendingTransitions.clear();
            }
            this.mIsInTraversal = false;
        }
    }

    private void maybeHandleWindowMove(Rect frame) {
        boolean windowMoved = this.mAttachInfo.mWindowLeft == frame.left ? this.mAttachInfo.mWindowTop != frame.top : true;
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

    private void handleOutOfResourcesException(OutOfResourcesException e) {
        Log.e(this.mTag, "OutOfResourcesException initializing HW surface", e);
        try {
            if (!(this.mWindowSession.outOfMemory(this.mWindow) || Process.myUid() == 1000)) {
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

    boolean isInLayout() {
        return this.mInLayout;
    }

    boolean requestLayoutDuringLayout(View view) {
        if (view.mParent == null || view.mAttachInfo == null) {
            return true;
        }
        if (!this.mLayoutRequesters.contains(view)) {
            this.mLayoutRequesters.add(view);
        }
        if (this.mHandlingLayoutInLayoutRequest) {
            return false;
        }
        return true;
    }

    private boolean performLayout(LayoutParams lp, int desiredWindowWidth, int desiredWindowHeight) {
        this.mLayoutRequested = false;
        this.mScrollMayChange = true;
        this.mInLayout = true;
        boolean windowSizeMayChange = false;
        if (this.mView == null) {
            return false;
        }
        View host = this.mView;
        if (host == null) {
            return false;
        }
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
                    for (int i = 0; i < numValidRequests; i++) {
                        View view = (View) validLayoutRequesters.get(i);
                        Log.w("View", "requestLayout() improperly called by " + view + " during layout: running second layout pass");
                        view.requestLayout();
                    }
                    windowSizeMayChange = measureHierarchy(host, lp, this.mView.getContext().getResources(), desiredWindowWidth, desiredWindowHeight);
                    this.mInLayout = true;
                    host.layout(0, 0, host.getMeasuredWidth(), host.getMeasuredHeight());
                    this.mHandlingLayoutInLayoutRequest = false;
                    validLayoutRequesters = getValidLayoutRequesters(this.mLayoutRequesters, true);
                    if (validLayoutRequesters != null) {
                        ArrayList<View> finalRequesters = validLayoutRequesters;
                        getRunQueue().post(new Runnable() {
                            public void run() {
                                int numValidRequests = validLayoutRequesters.size();
                                for (int i = 0; i < numValidRequests; i++) {
                                    View view = (View) validLayoutRequesters.get(i);
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
            return windowSizeMayChange;
        } catch (Throwable th) {
            Trace.traceEnd(8);
            if (Jlog.isPerfTest()) {
                Jlog.i(2103, "#LA:" + (((System.nanoTime() - preLayoutTime) + REDUNDANT) / TimeUtils.NANOS_PER_MS));
            }
        }
    }

    private ArrayList<View> getValidLayoutRequesters(ArrayList<View> layoutRequesters, boolean secondLayoutRequests) {
        int i;
        View view;
        int numViewsRequestingLayout = layoutRequesters.size();
        ArrayList<View> validLayoutRequesters = null;
        for (i = 0; i < numViewsRequestingLayout; i++) {
            view = (View) layoutRequesters.get(i);
            if (!(view == null || view.mAttachInfo == null || view.mParent == null || (!secondLayoutRequests && (view.mPrivateFlags & 4096) != 4096))) {
                boolean gone = false;
                View parent = view;
                while (parent != null) {
                    if ((parent.mViewFlags & 12) == 8) {
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
                        validLayoutRequesters = new ArrayList();
                    }
                    validLayoutRequesters.add(view);
                }
            }
        }
        if (!secondLayoutRequests) {
            for (i = 0; i < numViewsRequestingLayout; i++) {
                view = (View) layoutRequesters.get(i);
                while (view != null && (view.mPrivateFlags & 4096) != 0) {
                    view.mPrivateFlags &= -4097;
                    if (view.mParent instanceof View) {
                        view = (View) view.mParent;
                    } else {
                        view = null;
                    }
                }
            }
        }
        layoutRequesters.clear();
        return validLayoutRequesters;
    }

    public void requestTransparentRegion(View child) {
        checkThread();
        if (this.mView == child) {
            View view = this.mView;
            view.mPrivateFlags |= 512;
            this.mWindowAttributesChanged = true;
            this.mWindowAttributesChangesFlag = 0;
            requestLayout();
        }
    }

    private static int getRootMeasureSpec(int windowSize, int rootDimension) {
        switch (rootDimension) {
            case -2:
                return MeasureSpec.makeMeasureSpec(windowSize, Integer.MIN_VALUE);
            case -1:
                return MeasureSpec.makeMeasureSpec(windowSize, 1073741824);
            default:
                return MeasureSpec.makeMeasureSpec(rootDimension, 1073741824);
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
        for (int i = this.mWindowCallbacks.size() - 1; i >= 0; i--) {
            ((WindowCallbacks) this.mWindowCallbacks.get(i)).onPostDraw(canvas);
        }
    }

    void outputDisplayList(View view) {
        view.mRenderNode.output();
        if (this.mAttachInfo.mThreadedRenderer != null) {
            this.mAttachInfo.mThreadedRenderer.serializeDisplayListTree();
        }
    }

    private void profileRendering(boolean enabled) {
        if (this.mProfileRendering) {
            this.mRenderProfilingEnabled = enabled;
            if (this.mRenderProfiler != null) {
                this.mChoreographer.removeFrameCallback(this.mRenderProfiler);
            }
            if (this.mRenderProfilingEnabled) {
                if (this.mRenderProfiler == null) {
                    this.mRenderProfiler = new FrameCallback() {
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
            Log.v(this.mTag, "0x" + thisHash + "\tFPS:\t" + ((((float) this.mFpsNumFrames) * 1000.0f) / ((float) totalTime)));
            this.mFpsStartTime = nowTime;
            this.mFpsNumFrames = 0;
        }
    }

    void drawPending() {
        this.mDrawsNeededToReport++;
    }

    void pendingDrawFinished() {
        if (this.mDrawsNeededToReport == 0) {
            throw new RuntimeException("Unbalanced drawPending/pendingDrawFinished calls");
        }
        this.mDrawsNeededToReport--;
        if (this.mDrawsNeededToReport == 0) {
            reportDrawFinished();
        }
    }

    private void postDrawFinished() {
        this.mHandler.sendEmptyMessage(29);
    }

    private void reportDrawFinished() {
        try {
            this.mDrawsNeededToReport = 0;
            this.mWindowSession.finishDrawing(this.mWindow);
        } catch (RemoteException e) {
        }
    }

    private void performDraw() {
        if (mSupportAod ? !(this.mAttachInfo.mDisplayState == 1 || this.mAttachInfo.mDisplayState == 4 || this.mStopped) : this.mAttachInfo.mDisplayState != 1) {
            if ((this.mReportNextDraw ^ 1) != 0) {
                return;
            }
        }
        if (this.mView != null) {
            boolean fullRedrawNeeded = this.mFullRedrawNeeded;
            this.mFullRedrawNeeded = false;
            this.mIsDrawing = true;
            long preDrawTime = System.nanoTime();
            this.mChoreographer.isNeedDraw = true;
            Trace.traceBegin(8, "draw");
            Trace.traceBegin(8, "draw " + (fullRedrawNeeded ? "Rect(full)" : this.mDirty));
            try {
                draw(fullRedrawNeeded);
                this.mIsDrawing = false;
                if (Jlog.isPerfTest()) {
                    this.mSoftDrawTime = System.nanoTime() - preDrawTime;
                }
                Trace.traceEnd(8);
                Trace.traceEnd(8);
                if (this.mAttachInfo.mPendingAnimatingRenderNodes != null) {
                    int count = this.mAttachInfo.mPendingAnimatingRenderNodes.size();
                    for (int i = 0; i < count; i++) {
                        ((RenderNode) this.mAttachInfo.mPendingAnimatingRenderNodes.get(i)).endAllAnimators();
                    }
                    this.mAttachInfo.mPendingAnimatingRenderNodes.clear();
                }
                if (this.mReportNextDraw) {
                    this.mReportNextDraw = false;
                    if (this.mWindowDrawCountDown != null) {
                        try {
                            this.mWindowDrawCountDown.await();
                        } catch (InterruptedException e) {
                            Log.e(this.mTag, "Window redraw count down interruped!");
                        }
                        this.mWindowDrawCountDown = null;
                    }
                    if (this.mAttachInfo.mThreadedRenderer != null) {
                        this.mAttachInfo.mThreadedRenderer.fence();
                        this.mAttachInfo.mThreadedRenderer.setStopped(this.mStopped);
                    }
                    if (this.mSurfaceHolder == null || !this.mSurface.isValid()) {
                        pendingDrawFinished();
                    } else {
                        new SurfaceCallbackHelper(new -$Lambda$0h_uZnZRHKL2x08-ZPzgSojgn6M(this)).dispatchSurfaceRedrawNeededAsync(this.mSurfaceHolder, this.mSurfaceHolder.getCallbacks());
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
                Trace.traceEnd(8);
            }
        }
    }

    private void draw(boolean fullRedrawNeeded) {
        Surface surface = this.mSurface;
        if (surface.isValid()) {
            int curScrollY;
            if (!sFirstDrawComplete) {
                synchronized (sFirstDrawHandlers) {
                    sFirstDrawComplete = true;
                    int count = sFirstDrawHandlers.size();
                    for (int i = 0; i < count; i++) {
                        this.mHandler.post((Runnable) sFirstDrawHandlers.get(i));
                    }
                }
            }
            scrollToRectOrFocus(null, false);
            if (this.mAttachInfo.mViewScrollChanged) {
                this.mAttachInfo.mViewScrollChanged = false;
                this.mAttachInfo.mTreeObserver.dispatchOnScrollChanged();
            }
            boolean animating = this.mScroller != null ? this.mScroller.computeScrollOffset() : false;
            if (animating) {
                curScrollY = this.mScroller.getCurrY();
            } else {
                curScrollY = this.mScrollY;
            }
            if (this.mCurScrollY != curScrollY) {
                this.mCurScrollY = curScrollY;
                fullRedrawNeeded = true;
                if (this.mView instanceof RootViewSurfaceTaker) {
                    ((RootViewSurfaceTaker) this.mView).onRootViewScrollYChanged(this.mCurScrollY);
                }
            }
            float appScale = this.mAttachInfo.mApplicationScale;
            boolean scalingRequired = this.mAttachInfo.mScalingRequired;
            Rect dirty = this.mDirty;
            if (this.mSurfaceHolder != null) {
                dirty.setEmpty();
                if (animating && this.mScroller != null) {
                    this.mScroller.abortAnimation();
                }
                return;
            }
            if (fullRedrawNeeded) {
                this.mAttachInfo.mIgnoreDirtyState = true;
                dirty.set(0, 0, (int) ((((float) this.mWidth) * appScale) + 0.5f), (int) ((((float) this.mHeight) * appScale) + 0.5f));
            }
            this.mAttachInfo.mTreeObserver.dispatchOnDraw();
            int xOffset = -this.mCanvasOffsetX;
            int yOffset = (-this.mCanvasOffsetY) + curScrollY;
            LayoutParams params = this.mWindowAttributes;
            Rect surfaceInsets = params != null ? params.surfaceInsets : null;
            if (surfaceInsets != null) {
                xOffset -= surfaceInsets.left;
                yOffset -= surfaceInsets.top;
                dirty.offset(surfaceInsets.left, surfaceInsets.right);
            }
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
            this.mAttachInfo.mDrawingTime = this.mChoreographer.getFrameTimeNanos() / TimeUtils.NANOS_PER_MS;
            if (!dirty.isEmpty() || this.mIsAnimating || accessibilityFocusDirty) {
                if (enableRefreshDirty()) {
                    setRefreshDirty(dirty);
                }
                if (this.mAttachInfo.mThreadedRenderer != null && this.mAttachInfo.mThreadedRenderer.isEnabled()) {
                    boolean invalidateRoot = !accessibilityFocusDirty ? this.mInvalidateRootRequested : true;
                    this.mInvalidateRootRequested = false;
                    this.mIsAnimating = false;
                    if (!(this.mHardwareYOffset == yOffset && this.mHardwareXOffset == xOffset)) {
                        this.mHardwareYOffset = yOffset;
                        this.mHardwareXOffset = xOffset;
                        invalidateRoot = true;
                    }
                    if (invalidateRoot) {
                        this.mAttachInfo.mThreadedRenderer.invalidateRoot();
                    }
                    dirty.setEmpty();
                    boolean updated = updateContentDrawBounds();
                    if (this.mReportNextDraw) {
                        this.mAttachInfo.mThreadedRenderer.setStopped(false);
                    }
                    if (updated) {
                        requestDrawWindow();
                    }
                    this.mAttachInfo.mThreadedRenderer.draw(this.mView, this.mAttachInfo, this);
                } else if (this.mAttachInfo.mThreadedRenderer == null || (this.mAttachInfo.mThreadedRenderer.isEnabled() ^ 1) == 0 || !this.mAttachInfo.mThreadedRenderer.isRequested()) {
                    if (!drawSoftware(surface, this.mAttachInfo, xOffset, yOffset, scalingRequired, dirty)) {
                        return;
                    }
                } else {
                    try {
                        if (!this.mTag.contains(Surface.TOAST) || this.mSurface.mNativeObject == 0 || (this.mSurface.mSurfaceControllerIsValid ^ 1) == 0) {
                            this.mAttachInfo.mThreadedRenderer.initializeIfNeeded(this.mWidth, this.mHeight, this.mAttachInfo, this.mSurface, surfaceInsets);
                            this.mFullRedrawNeeded = true;
                            scheduleTraversals();
                            return;
                        }
                        Log.w(this.mTag, "EGLdebug### draw Surface is" + this.mSurface + ",visibility " + getHostVisibility());
                        this.mSurface.release();
                        return;
                    } catch (OutOfResourcesException e) {
                        handleOutOfResourcesException(e);
                        return;
                    }
                }
            }
            if (animating) {
                this.mFullRedrawNeeded = true;
                scheduleTraversals();
            }
        }
    }

    public boolean enableRefreshDirty() {
        return this.mPartialUpdateSwitch && SystemProperties.getInt("sys.refresh.dirty", 0) > 0;
    }

    private boolean debugRefreshDirty() {
        return SystemProperties.getInt("debug.refresh.dirty", 0) > 0;
    }

    private void setRefreshDirty(Rect dirty) {
        if (dirty == null) {
            try {
                Log.w(TAG, "setRefreshDirty-dirty is null");
            } catch (RuntimeException e) {
                Log.e(TAG, "setRefreshDirty-fail, Run Time exception");
            } catch (Exception e2) {
                Log.e(TAG, "setRefreshDirty-fail");
            }
        } else {
            if (this.mDebugRefreshDirty) {
                Log.i(TAG, "@@@setRefreshDirty-dirty=[" + dirty.left + "," + dirty.top + "," + dirty.right + "," + dirty.bottom + "]-w:" + dirty.width() + ", h:" + dirty.height());
            }
            this.mSurface.setRefreshDirty(dirty);
        }
    }

    public void setSDRRatio(float ratio) {
        this.mSurface.setSDRRatio(ratio);
    }

    public boolean getDisplayInfo(DisplayInfo outDisplayInfo) {
        return this.mDisplay.getDisplayInfo(outDisplayInfo);
    }

    /* JADX WARNING: Missing block: B:33:0x0073, code:
            if (r0 == r15.bottom) goto L_0x0019;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private boolean drawSoftware(Surface surface, AttachInfo attachInfo, int xoff, int yoff, boolean scalingRequired, Rect dirty) {
        try {
            int left = dirty.left;
            int top = dirty.top;
            int right = dirty.right;
            int bottom = dirty.bottom;
            Canvas canvas = this.mSurface.lockCanvas(dirty);
            if (left == dirty.left && top == dirty.top) {
                if (right == dirty.right) {
                }
            }
            attachInfo.mIgnoreDirtyState = true;
            canvas.setDensity(this.mDensity);
            try {
                if (!(canvas.isOpaque() && yoff == 0 && xoff == 0)) {
                    canvas.drawColor(0, Mode.CLEAR);
                }
                dirty.setEmpty();
                this.mIsAnimating = false;
                View view = this.mView;
                view.mPrivateFlags |= 32;
                canvas.translate((float) (-xoff), (float) (-yoff));
                if (this.mTranslator != null) {
                    this.mTranslator.translateCanvas(canvas);
                }
                canvas.setScreenDensity(scalingRequired ? this.mNoncompatDensity : 0);
                attachInfo.mSetIgnoreDirtyState = false;
                this.mView.draw(canvas);
                drawAccessibilityFocusedDrawableIfNeeded(canvas);
                if (!attachInfo.mSetIgnoreDirtyState) {
                    attachInfo.mIgnoreDirtyState = false;
                }
                try {
                    surface.syncFrameInfo(this.mChoreographer);
                    surface.unlockCanvasAndPost(canvas);
                    return true;
                } catch (IllegalArgumentException e) {
                    Log.e(this.mTag, "Could not unlock surface", e);
                    this.mLayoutRequested = true;
                    return false;
                }
            } catch (Throwable th) {
                try {
                    surface.syncFrameInfo(this.mChoreographer);
                    surface.unlockCanvasAndPost(canvas);
                } catch (IllegalArgumentException e2) {
                    Log.e(this.mTag, "Could not unlock surface", e2);
                    this.mLayoutRequested = true;
                    return false;
                }
            }
        } catch (OutOfResourcesException e3) {
            handleOutOfResourcesException(e3);
            return false;
        } catch (IllegalArgumentException e22) {
            Log.e(this.mTag, "Could not lock surface", e22);
            this.mLayoutRequested = true;
            return false;
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
        if (!manager.isEnabled() || (manager.isTouchExplorationEnabled() ^ 1) != 0) {
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
        AttachInfo attachInfo = this.mAttachInfo;
        bounds.offset(0, attachInfo.mViewRootImpl.mScrollY);
        bounds.offset(-attachInfo.mWindowLeft, -attachInfo.mWindowTop);
        if (!bounds.intersect(0, 0, attachInfo.mViewRootImpl.mWidth, attachInfo.mViewRootImpl.mHeight)) {
            bounds.setEmpty();
        }
        return bounds.isEmpty() ^ 1;
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

    public void requestInvalidateRootRenderNode() {
        this.mInvalidateRootRequested = true;
    }

    boolean scrollToRectOrFocus(Rect rectangle, boolean immediate) {
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
            View lastScrolledFocus = this.mLastScrolledFocus != null ? (View) this.mLastScrolledFocus.get() : null;
            if (focus != lastScrolledFocus) {
                rectangle = null;
            }
            if (!(focus == lastScrolledFocus && (this.mScrollMayChange ^ 1) != 0 && rectangle == null)) {
                this.mLastScrolledFocus = new WeakReference(focus);
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
                this.mScroller.startScroll(0, this.mScrollY, 0, scrollY - this.mScrollY);
            } else if (this.mScroller != null) {
                this.mScroller.abortAnimation();
            }
            this.mScrollY = scrollY;
        }
        return handled;
    }

    public View getAccessibilityFocusedHost() {
        return this.mAccessibilityFocusedHost;
    }

    public AccessibilityNodeInfo getAccessibilityFocusedVirtualView() {
        return this.mAccessibilityFocusedVirtualView;
    }

    void setAccessibilityFocus(View view, AccessibilityNodeInfo node) {
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

    boolean hasPointerCapture() {
        return this.mPointerCapture;
    }

    void requestPointerCapture(boolean enabled) {
        if (this.mPointerCapture != enabled) {
            InputManager.getInstance().requestPointerCapture(this.mAttachInfo.mWindowToken, enabled);
        }
    }

    private void handlePointerCaptureChanged(boolean hasCapture) {
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
        } else if (sAlwaysAssignFocus) {
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

    void dispatchDetachedFromWindow() {
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

    private void performConfigurationChange(MergedConfiguration mergedConfiguration, boolean force, int newDisplayId) {
        if (mergedConfiguration == null) {
            throw new IllegalArgumentException("No merged config provided.");
        }
        Configuration globalConfig = mergedConfiguration.getGlobalConfiguration();
        Configuration overrideConfig = mergedConfiguration.getOverrideConfiguration();
        synchronized (sConfigCallbacks) {
            for (int i = sConfigCallbacks.size() - 1; i >= 0; i--) {
                ((ConfigChangedCallback) sConfigCallbacks.get(i)).onConfigurationChanged(globalConfig);
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
    }

    public void updateConfiguration(int newDisplayId) {
        if (this.mView != null) {
            Resources localResources = this.mView.getResources();
            Configuration config = localResources.getConfiguration();
            if (newDisplayId != -1) {
                onMovedToDisplay(newDisplayId, config);
            }
            if (this.mForceNextConfigUpdate || this.mLastConfigurationFromResources.diff(config) != 0) {
                this.mDisplay = ResourcesManager.getInstance().getAdjustedDisplay(this.mDisplay.getDisplayId(), localResources);
                if (this.mDisplay == null && this.mView.getContext() != null && HwPCUtils.isValidExtDisplayId(this.mView.getContext())) {
                    HwPCUtils.log(TAG, "mDisplay is null...");
                    doDie();
                    return;
                }
                int lastLayoutDirection = this.mLastConfigurationFromResources.getLayoutDirection();
                int currentLayoutDirection = config.getLayoutDirection();
                this.mLastConfigurationFromResources.setTo(config);
                if (lastLayoutDirection != currentLayoutDirection && this.mViewLayoutDirectionInitial == 2) {
                    this.mView.setLayoutDirection(currentLayoutDirection);
                }
                this.mView.dispatchConfigurationChanged(config);
                this.mForceNextWindowRelayout = true;
                requestLayout();
            }
        }
    }

    public static boolean isViewDescendantOf(View child, View parent) {
        if (child == parent) {
            return true;
        }
        ViewParent theParent = child.getParent();
        return theParent instanceof ViewGroup ? isViewDescendantOf((View) theParent, parent) : false;
    }

    private static void forceLayout(View view) {
        view.forceLayout();
        if (view instanceof ViewGroup) {
            ViewGroup group = (ViewGroup) view;
            int count = group.getChildCount();
            for (int i = 0; i < count; i++) {
                forceLayout(group.getChildAt(i));
            }
        }
    }

    void jank_newcheckSkippedFrame(long nowtime, long frameVsynctime) {
        boolean bLastTraversal = this.mChoreographer.isLastTraversal();
        long LastSkippedFrameEnd = this.mChoreographer.getLastSkippedFrameEndTime();
        if (bLastTraversal) {
            long skippedFrames;
            if (LastSkippedFrameEnd > frameVsynctime) {
                skippedFrames = (nowtime - LastSkippedFrameEnd) / VSYNC_SPAN;
            } else {
                skippedFrames = (nowtime - frameVsynctime) / VSYNC_SPAN;
            }
            if (skippedFrames >= 5) {
                Trace.traceBegin(8, "jank_event_sync: start_ts=" + frameVsynctime + ",end_ts=" + nowtime + ", appid=" + Process.myPid());
                Trace.traceEnd(8);
                Jlog.d(37, "#P:" + this.mWindowAttributes.getTitle() + "#SK:" + skippedFrames + "#FRT:" + (frameVsynctime / 10000) + "#DNT:" + (nowtime / 10000));
                this.mChoreographer.setLastSkippedFrameEndTime(nowtime);
            }
            ScrollerBoostManager.getInstance().updateFrameJankInfo(skippedFrames);
        }
    }

    void jank_processAfterTraversal(boolean scroll) {
        long now = System.nanoTime();
        long delaytime = this.mChoreographer.getDoFrameDelayTime();
        long lastFrameDoneTime = this.mChoreographer.getLastFrameDoneTime();
        long frameTime = this.mChoreographer.getRealFrameTime();
        boolean bOntime = delaytime < ((long) ONTIME_REF);
        jank_newcheckSkippedFrame(now, frameTime);
        boolean bContinuousUpdate = bOntime ? frameTime - lastFrameDoneTime < VSYNC_SPAN : lastFrameDoneTime > frameTime;
        if (!bOntime || now - frameTime >= 14166667) {
            if (scroll && frameTime - lastFrameDoneTime < ((long) CONTINUOUS_REF)) {
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
                    lastSFSyncTime = (lastFrameDoneTime - ((lastFrameDoneTime - frameTime) - (((long) ((int) ((lastFrameDoneTime - frameTime) / VSYNC_SPAN))) * VSYNC_SPAN))) - 2500000;
                } else if (scroll) {
                    lastSFSyncTime = (lastFrameDoneTime - ((frameTime - lastFrameDoneTime) - (((long) ((int) ((frameTime - lastFrameDoneTime) / VSYNC_SPAN))) * VSYNC_SPAN))) - 2500000;
                }
                int skip = (int) (((now - lastSFSyncTime) - REDUNDANT) / VSYNC_SPAN);
                if (skip == 0) {
                    this.lastFrameDefer = 1;
                    return;
                } else if (skip != 1) {
                    if (skip > 1) {
                        skip = (skip - 1) - this.lastFrameDefer;
                        this.lastFrameDefer = 0;
                    }
                    if (skip >= 5) {
                        String msg = "#P:" + this.mWindowAttributes.getTitle() + "#SK:" + skip;
                        if (this.mAttachInfo.mThreadedRenderer != null) {
                            msg = msg + "#IP:" + (this.mDeliverInputTime / 10000) + "#DR:" + (this.mAttachInfo.mThreadedRenderer.getJankDrawData(0) / 10000) + "#PRO:" + (this.mAttachInfo.mThreadedRenderer.getJankDrawData(1) / 10000) + "#EX:" + (this.mAttachInfo.mThreadedRenderer.getJankDrawData(2) / 10000) + "#TRA:" + (this.mAttachInfo.mThreadedRenderer.getJankDrawData(3) / 10000);
                        } else {
                            msg = msg + "#IP:" + (this.mDeliverInputTime / 10000) + "#DR:" + 0 + "#PRO:" + 0 + "#EX:" + 0 + "#TRA:" + (this.mSoftDrawTime / 10000);
                        }
                        Jlog.d(67, msg + "#FRT:" + (frameTime / 10000) + "#DNT:" + (now / 10000) + "#LFT:" + (lastFrameDoneTime / 10000));
                    }
                } else {
                    return;
                }
            }
            return;
        }
        this.lastFrameDefer = 0;
    }

    boolean ensureTouchMode(boolean inTouchMode) {
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
            if (!(focused == null || (focused.isFocusableInTouchMode() ^ 1) == 0)) {
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
        if (this.mView != null) {
            if (this.mView.hasFocus()) {
                View focusedView = this.mView.findFocus();
                if (!((focusedView instanceof ViewGroup) && ((ViewGroup) focusedView).getDescendantFocusability() == 262144)) {
                    return false;
                }
            }
            View focused = focusSearch(null, 130);
            if (focused != null) {
                return focused.requestFocus(130);
            }
        }
        return false;
    }

    private void resetPointerIcon(MotionEvent event) {
        this.mPointerIconType = 1;
        updatePointerIcon(event);
    }

    private boolean updatePointerIcon(MotionEvent event) {
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
            if (this.mPointerIconType == -1 && (pointerIcon.equals(this.mCustomPointerIcon) ^ 1) != 0) {
                this.mCustomPointerIcon = pointerIcon;
                InputManager.getInstance().setCustomPointerIcon(this.mCustomPointerIcon);
            }
            return true;
        }
    }

    private void maybeUpdateTooltip(MotionEvent event) {
        if (event.getPointerCount() == 1) {
            int action = event.getActionMasked();
            if (action == 9 || action == 7 || action == 10) {
                AccessibilityManager manager = AccessibilityManager.getInstance(this.mContext);
                if (!manager.isEnabled() || !manager.isTouchExplorationEnabled()) {
                    if (this.mView == null) {
                        Slog.d(this.mTag, "maybeUpdateTooltip called after view was removed");
                    } else {
                        this.mView.dispatchTooltipHoverEvent(event);
                    }
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

    void setLocalDragState(Object obj) {
        this.mLocalDragState = obj;
    }

    private void handleDragEvent(DragEvent event) {
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
                if (what == 2 && (event.mEventHandlerWasCalled ^ 1) != 0) {
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
                        Log.i(this.mTag, "Reporting drop result: " + result);
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
        ArrayList<KeyboardShortcutGroup> list = new ArrayList();
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
        if (!(this.mCurrentDragView == newDragTarget || (View.sCascadedDragDrop ^ 1) == 0)) {
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
        if (this.mView == null) {
            throw new IllegalStateException("getAudioManager called when there is no mView");
        }
        if (this.mAudioManager == null) {
            this.mAudioManager = (AudioManager) this.mView.getContext().getSystemService("audio");
        }
        return this.mAudioManager;
    }

    public AccessibilityInteractionController getAccessibilityInteractionController() {
        if (this.mView == null) {
            throw new IllegalStateException("getAccessibilityInteractionController called when there is no mView");
        }
        if (this.mAccessibilityInteractionController == null) {
            this.mAccessibilityInteractionController = new AccessibilityInteractionController(this);
        }
        return this.mAccessibilityInteractionController;
    }

    private int relayoutWindow(LayoutParams params, int viewVisibility, boolean insetsPending) throws RemoteException {
        float appScale = this.mAttachInfo.mApplicationScale;
        boolean restore = false;
        if (!(params == null || this.mTranslator == null || (params.hwFlags & 1048576) != 0)) {
            restore = true;
            params.backup();
            this.mTranslator.translateWindowLayout(params);
        }
        if (!(params == null || this.mOrigWindowType == params.type || this.mTargetSdkVersion >= 14)) {
            Slog.w(this.mTag, "Window type can not be changed after the window is added; ignoring change of " + this.mView);
            params.type = this.mOrigWindowType;
        }
        if (this.mTag.contains(Surface.TOAST)) {
            Slog.w(this.mTag, "EGLdebug relayoutWindow Surface is" + this.mSurface + ",viewVisibility is" + viewVisibility);
        }
        int relayoutResult = this.mWindowSession.relayout(this.mWindow, this.mSeq, params, (int) ((((float) this.mView.getMeasuredWidth()) * appScale) + 0.5f), (int) ((((float) this.mView.getMeasuredHeight()) * appScale) + 0.5f), viewVisibility, insetsPending ? 1 : 0, this.mWinFrame, this.mPendingOverscanInsets, this.mPendingContentInsets, this.mPendingVisibleInsets, this.mPendingStableInsets, this.mPendingOutsets, this.mPendingBackDropFrame, this.mPendingMergedConfiguration, this.mSurface);
        if (this.mTag.contains(Surface.TOAST)) {
            Slog.w(this.mTag, "EGLdebug relayoutWindow Surface is" + this.mSurface + ",relayoutResult is" + relayoutResult);
        }
        this.mPendingAlwaysConsumeNavBar = (relayoutResult & 64) != 0;
        if (restore) {
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
            Log.e(this.mTag, "FATAL EXCEPTION when attempting to play sound effect: " + e);
            e.printStackTrace();
        }
        Log.e(this.mTag, "FATAL EXCEPTION when attempting to play sound effect: " + e);
        e.printStackTrace();
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
        if (this.mView instanceof ViewGroup) {
            return FocusFinder.getInstance().findNextFocus((ViewGroup) this.mView, focused, direction);
        }
        return null;
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
                prefix = prefix + "  ";
                for (int i = 0; i < N; i++) {
                    dumpViewHierarchy(prefix, writer, grp.getChildAt(i));
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

    boolean die(boolean immediate) {
        if (!immediate || (this.mIsInTraversal ^ 1) == 0) {
            if (this.mIsDrawing) {
                Log.e(this.mTag, "Attempting to destroy the window while drawing!\n  window=" + this + ", title=" + this.mWindowAttributes.getTitle());
            } else {
                destroyHardwareRenderer();
            }
            this.mHandler.sendEmptyMessage(3);
            return true;
        }
        doDie();
        return false;
    }

    void doDie() {
        if (this.mView != null ? this.mView.isTouchableInOtherThread() : false) {
            Log.w(this.mTag, "doDie in " + this + ",CREATE IN " + this.mThread + ",DIE IN " + Thread.currentThread());
        } else {
            checkThread();
        }
        synchronized (this) {
            if (this.mRemoved) {
                return;
            }
            this.mRemoved = true;
            if (this.mAdded) {
                dispatchDetachedFromWindow();
            }
            if (this.mAdded && (this.mFirst ^ 1) != 0) {
                destroyHardwareRenderer();
                if (this.mView != null) {
                    int viewVisibility = this.mView.getVisibility();
                    boolean viewVisibilityChanged = this.mViewVisibility != viewVisibility;
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

    public void requestUpdateConfiguration(Configuration config) {
        this.mHandler.sendMessage(this.mHandler.obtainMessage(18, config));
    }

    public void loadSystemProperties() {
        this.mHandler.post(new Runnable() {
            public void run() {
                ViewRootImpl.this.mAllocBufferAsync = SystemProperties.getBoolean(ViewRootImpl.PROPERTY_ALLOC_BUFFER_SYNC, false);
                ViewRootImpl.this.mProfileRendering = SystemProperties.getBoolean(ViewRootImpl.PROPERTY_PROFILE_RENDERING, false);
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

    private void dispatchResized(Rect frame, Rect overscanInsets, Rect contentInsets, Rect visibleInsets, Rect stableInsets, Rect outsets, boolean reportDraw, MergedConfiguration mergedConfiguration, Rect backDropFrame, boolean forceLayout, boolean alwaysConsumeNavBar, int displayId) {
        if (this.mDragResizing) {
            boolean fullscreen = frame.equals(backDropFrame);
            synchronized (this.mWindowCallbacks) {
                for (int i = this.mWindowCallbacks.size() - 1; i >= 0; i--) {
                    ((WindowCallbacks) this.mWindowCallbacks.get(i)).onWindowSizeIsChanging(backDropFrame, fullscreen, visibleInsets, stableInsets);
                }
            }
        }
        Message msg = this.mHandler.obtainMessage(reportDraw ? 5 : 4);
        if (this.mTranslator != null) {
            this.mTranslator.translateRectInScreenToAppWindow(frame);
            this.mTranslator.translateRectInScreenToAppWindow(overscanInsets);
            this.mTranslator.translateRectInScreenToAppWindow(contentInsets);
            this.mTranslator.translateRectInScreenToAppWindow(visibleInsets);
        }
        SomeArgs args = SomeArgs.obtain();
        boolean sameProcessCall = Binder.getCallingPid() == Process.myPid();
        if (sameProcessCall) {
            frame = new Rect(frame);
        }
        args.arg1 = frame;
        if (sameProcessCall) {
            contentInsets = new Rect(contentInsets);
        }
        args.arg2 = contentInsets;
        if (sameProcessCall) {
            visibleInsets = new Rect(visibleInsets);
        }
        args.arg3 = visibleInsets;
        if (sameProcessCall && mergedConfiguration != null) {
            mergedConfiguration = new MergedConfiguration(mergedConfiguration);
        }
        args.arg4 = mergedConfiguration;
        if (sameProcessCall) {
            overscanInsets = new Rect(overscanInsets);
        }
        args.arg5 = overscanInsets;
        if (sameProcessCall) {
            stableInsets = new Rect(stableInsets);
        }
        args.arg6 = stableInsets;
        if (sameProcessCall) {
            outsets = new Rect(outsets);
        }
        args.arg7 = outsets;
        if (sameProcessCall) {
            backDropFrame = new Rect(backDropFrame);
        }
        args.arg8 = backDropFrame;
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

    void enqueueInputEvent(InputEvent event) {
        enqueueInputEvent(event, null, 0, false);
    }

    void enqueueInputEvent(InputEvent event, InputEventReceiver receiver, int flags, boolean processImmediately) {
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

    void doProcessInputEvents() {
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
                MotionEvent me = q.mEvent;
                if (me.getHistorySize() > 0) {
                    oldestEventTime = me.getHistoricalEventTimeNano(0);
                }
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
        Trace.asyncTraceBegin(8, "deliverInputEvent", q.mEvent.getSequenceNumber());
        if (this.mInputEventConsistencyVerifier != null) {
            this.mInputEventConsistencyVerifier.onInputEvent(q.mEvent, 0);
        }
        InputStage stage = q.shouldSendToSynthesizer() ? this.mSyntheticInputStage : q.shouldSkipIme() ? this.mFirstPostImeInputStage : this.mFirstInputStage;
        if (stage != null) {
            stage.deliver(q);
        } else {
            finishInputEvent(q);
        }
    }

    private void finishInputEvent(QueuedInputEvent q) {
        Trace.asyncTraceEnd(8, "deliverInputEvent", q.mEvent.getSequenceNumber());
        if (this.mDisplay != null && HwPCUtils.isValidExtDisplayId(this.mDisplay.getDisplayId()) && (q.mEvent instanceof MotionEvent)) {
            q.mEvent.setOffset(null);
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
        boolean z = true;
        if (event instanceof KeyEvent) {
            if (((KeyEvent) event).getAction() != 1) {
                z = false;
            }
            return z;
        }
        int action = ((MotionEvent) event).getAction();
        if (!(action == 1 || action == 3 || action == 10)) {
            z = false;
        }
        return z;
    }

    void scheduleConsumeBatchedInput() {
        if (!this.mConsumeBatchedInputScheduled) {
            this.mConsumeBatchedInputScheduled = true;
            this.mChoreographer.postCallback(0, this.mConsumedBatchedInputRunnable, null);
        }
    }

    void unscheduleConsumeBatchedInput() {
        if (this.mConsumeBatchedInputScheduled) {
            this.mConsumeBatchedInputScheduled = false;
            this.mChoreographer.removeCallbacks(0, this.mConsumedBatchedInputRunnable, null);
        }
    }

    void scheduleConsumeBatchedInputImmediately() {
        if (!this.mConsumeBatchedInputImmediatelyScheduled) {
            unscheduleConsumeBatchedInput();
            this.mConsumeBatchedInputImmediatelyScheduled = true;
            this.mHandler.post(this.mConsumeBatchedInputImmediatelyRunnable);
        }
    }

    void doConsumeBatchedInput(long frameTimeNanos) {
        if (this.mConsumeBatchedInputScheduled) {
            this.mConsumeBatchedInputScheduled = false;
            if (!(this.mInputEventReceiver == null || !this.mInputEventReceiver.consumeBatchedInputEvents(frameTimeNanos) || frameTimeNanos == -1)) {
                scheduleConsumeBatchedInput();
            }
            doProcessInputEvents();
        }
    }

    public void dispatchInvalidateDelayed(View view, long delayMilliseconds) {
        this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(1, view), delayMilliseconds);
    }

    public void dispatchInvalidateRectDelayed(InvalidateInfo info, long delayMilliseconds) {
        this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(2, info), delayMilliseconds);
    }

    public void dispatchInvalidateOnAnimation(View view) {
        this.mInvalidateOnAnimationRunnable.addView(view);
    }

    public void dispatchInvalidateRectOnAnimation(InvalidateInfo info) {
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

    public void updateSurfaceStatus(boolean status) {
        this.mSurface.setSurfaceControllerState(status);
    }

    public boolean surfaceControlllerIsValid() {
        if (this.mSurface.mNativeObject == 0) {
            return false;
        }
        if (this.mSurface.mNativeObject == 0 || this.mSurface.mSurfaceControllerIsValid || this.mSurface.mAppName == null || (!this.mSurface.mAppName.equals(Surface.APP_INTELLIGENT) && !this.mSurface.mAppName.equals(Surface.APP_LAUNCHER))) {
            return true;
        }
        return false;
    }

    public void windowFocusChanged(boolean hasFocus, boolean inTouchMode) {
        int i = 1;
        Message msg = Message.obtain();
        msg.what = 6;
        msg.arg1 = hasFocus ? 1 : 0;
        if (!inTouchMode) {
            i = 0;
        }
        msg.arg2 = i;
        if (this.mTag.contains(Surface.TOAST)) {
            Log.w(this.mTag, "EGLdebug  callers=" + Debug.getCallers(5));
        }
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
        msg.arg1 = on ? 1 : 0;
        this.mHandler.sendMessage(msg);
    }

    private void postSendWindowContentChangedCallback(View source, int changeType) {
        if (this.mSendWindowContentChangedAccessibilityEvent == null) {
            this.mSendWindowContentChangedAccessibilityEvent = new SendWindowContentChangedAccessibilityEvent(this, null);
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
        View source;
        switch (event.getEventType()) {
            case 2048:
                handleWindowContentChangedEvent(event);
                break;
            case 32768:
                long sourceNodeId = event.getSourceNodeId();
                source = this.mView.findViewByAccessibilityId(AccessibilityNodeInfo.getAccessibilityViewId(sourceNodeId));
                if (source != null) {
                    AccessibilityNodeProvider provider = source.getAccessibilityNodeProvider();
                    if (provider != null) {
                        setAccessibilityFocus(source, provider.createAccessibilityNodeInfo(AccessibilityNodeInfo.getVirtualDescendantId(sourceNodeId)));
                        break;
                    }
                }
                break;
            case 65536:
                source = this.mView.findViewByAccessibilityId(AccessibilityNodeInfo.getAccessibilityViewId(event.getSourceNodeId()));
                if (!(source == null || source.getAccessibilityNodeProvider() == null)) {
                    setAccessibilityFocus(null, null);
                    break;
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
                while (root != null && (hostInSubtree ^ 1) != 0) {
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
                    if (this.mAccessibilityFocusedVirtualView == null) {
                        this.mAccessibilityFocusedHost = null;
                        focusedHost.clearAccessibilityFocusNoCallbacks(0);
                        provider.performAction(focusedChildId, AccessibilityAction.ACTION_CLEAR_ACCESSIBILITY_FOCUS.getId(), null);
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

    private View getCommonPredecessor(View first, View second) {
        if (this.mTempHashSet == null) {
            this.mTempHashSet = new HashSet();
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

    void checkThread() {
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

    void changeCanvasOpacity(boolean opaque) {
        Log.d(this.mTag, "changeCanvasOpacity: opaque=" + opaque);
        if (this.mAttachInfo.mThreadedRenderer != null) {
            this.mAttachInfo.mThreadedRenderer.setOpaque(opaque);
        }
    }

    public static void dispatchWindowLayoutChange() {
        synchronized (sWindowLayoutObservers) {
            int i = sWindowLayoutObservers.beginBroadcast();
            while (i > 0) {
                i--;
                IWindowLayoutObserver observer = (IWindowLayoutObserver) sWindowLayoutObservers.getBroadcastItem(i);
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
        HandlerActionQueue rq = (HandlerActionQueue) sRunQueues.get();
        if (rq != null) {
            return rq;
        }
        rq = new HandlerActionQueue();
        sRunQueues.set(rq);
        return rq;
    }

    private void startDragResizing(Rect initialBounds, boolean fullscreen, Rect systemInsets, Rect stableInsets, int resizeMode) {
        if (!this.mDragResizing) {
            this.mDragResizing = true;
            for (int i = this.mWindowCallbacks.size() - 1; i >= 0; i--) {
                ((WindowCallbacks) this.mWindowCallbacks.get(i)).onWindowDragResizeStart(initialBounds, fullscreen, systemInsets, stableInsets, resizeMode);
            }
            this.mFullRedrawNeeded = true;
        }
    }

    private void endDragResizing() {
        if (this.mDragResizing) {
            this.mDragResizing = false;
            for (int i = this.mWindowCallbacks.size() - 1; i >= 0; i--) {
                ((WindowCallbacks) this.mWindowCallbacks.get(i)).onWindowDragResizeEnd();
            }
            this.mFullRedrawNeeded = true;
        }
    }

    private boolean updateContentDrawBounds() {
        int updated = 0;
        for (int i = this.mWindowCallbacks.size() - 1; i >= 0; i--) {
            updated |= ((WindowCallbacks) this.mWindowCallbacks.get(i)).onContentDrawn(this.mWindowAttributes.surfaceInsets.left, this.mWindowAttributes.surfaceInsets.top, this.mWidth, this.mHeight);
        }
        return (this.mDragResizing ? this.mReportNextDraw : 0) | updated;
    }

    private void requestDrawWindow() {
        if (this.mReportNextDraw) {
            this.mWindowDrawCountDown = new CountDownLatch(this.mWindowCallbacks.size());
        }
        for (int i = this.mWindowCallbacks.size() - 1; i >= 0; i--) {
            ((WindowCallbacks) this.mWindowCallbacks.get(i)).onRequestDraw(this.mReportNextDraw);
        }
    }

    public void reportActivityRelaunched() {
        this.mActivityRelaunched = true;
    }

    private void doRelayoutAsyncly(boolean shouldDo) {
        if (shouldDo) {
            this.mFullRedrawNeeded = true;
            this.mLayoutRequested = true;
            getRunQueue().post(new Runnable() {
                public void run() {
                    if (ViewRootImpl.this.mView != null) {
                        Log.v(ViewRootImpl.TAG, "doRelayoutAsyncly requestLayout");
                        ViewRootImpl.this.mView.requestLayout();
                    }
                }
            });
        }
    }
}
