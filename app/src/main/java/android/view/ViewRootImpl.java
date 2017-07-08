package android.view;

import android.animation.LayoutTransition;
import android.app.ActivityManagerNative;
import android.app.ResourcesManager;
import android.common.HwFrameworkFactory;
import android.content.ClipDescription;
import android.content.ComponentCallbacks;
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
import android.os.RemoteException;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.Trace;
import android.rog.AppRogInfo;
import android.rog.HwRogTranslater;
import android.scrollerboost.ScrollerBoostManager;
import android.util.AndroidRuntimeException;
import android.util.DisplayMetrics;
import android.util.Jlog;
import android.util.JlogConstants;
import android.util.Log;
import android.util.PtmLog;
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
import android.view.animation.Interpolator;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.view.inputmethod.InputMethodManager.FinishedInputEventCallback;
import android.webkit.WebViewClient;
import android.widget.Scroller;
import com.android.internal.R;
import com.android.internal.annotations.GuardedBy;
import com.android.internal.content.PackageHelper;
import com.android.internal.logging.MetricsProto.MetricsEvent;
import com.android.internal.os.HwBootFail;
import com.android.internal.os.IResultReceiver;
import com.android.internal.os.SomeArgs;
import com.android.internal.policy.PhoneFallbackEventHandler;
import com.android.internal.telephony.AbstractRILConstants;
import com.android.internal.telephony.RILConstants;
import com.android.internal.util.AsyncService;
import com.android.internal.util.Protocol;
import com.android.internal.view.BaseSurfaceHolder;
import com.android.internal.view.RootViewSurfaceTaker;
import com.huawei.android.statistical.StatisticalConstant;
import com.huawei.hwperformance.HwPerformance;
import com.huawei.indexsearch.IndexSearchConstants;
import com.huawei.pgmng.log.LogPower;
import com.huawei.pgmng.plug.PGSdk;
import huawei.cust.HwCfgFilePolicy;
import java.io.FileDescriptor;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.concurrent.CountDownLatch;
import javax.microedition.khronos.opengles.GL10;

public final class ViewRootImpl implements ViewParent, Callbacks, HardwareDrawCallbacks {
    static int CONTINUOUS_REF = 0;
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
    private static final boolean FRONT_FINGERPRINT_NAVIGATION = false;
    private static final boolean LOCAL_LOGV = false;
    private static final int MAX_QUEUED_INPUT_EVENT_POOL_SIZE = 10;
    static final int MAX_TRACKBALL_DELAY = 250;
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
    private static final int MSG_HUAWEI_CUSTOM_BASE = 100;
    private static final int MSG_INVALIDATE = 1;
    private static final int MSG_INVALIDATE_RECT = 2;
    private static final int MSG_INVALIDATE_WORLD = 22;
    private static final int MSG_PROCESS_INPUT_EVENTS = 19;
    private static final int MSG_REQUEST_KEYBOARD_SHORTCUTS = 26;
    private static final int MSG_RESIZED = 4;
    private static final int MSG_RESIZED_REPORT = 5;
    private static final int MSG_ROG_SWITCH_STATE_CHANGE = 101;
    private static final int MSG_SYNTHESIZE_INPUT_EVENT = 24;
    private static final int MSG_UPDATE_CONFIGURATION = 18;
    private static final int MSG_UPDATE_POINTER_ICON = 27;
    private static final int MSG_WINDOW_FOCUS_CHANGED = 6;
    private static final int MSG_WINDOW_MOVED = 23;
    static int ONTIME_REF = 0;
    public static final String PROPERTY_EMULATOR_WIN_OUTSET_BOTTOM_PX = "ro.emu.win_outset_bottom_px";
    private static final String PROPERTY_PROFILE_RENDERING = "viewroot.profile_rendering";
    private static final long REDUNDANT = 500000;
    private static final long SF_VSYNC_OFFSET = 5000000;
    private static final String TAG = "ViewRootImpl";
    private static final boolean USE_MT_RENDERER = true;
    private static final long VSYNC_OFFSET = 7500000;
    private static final long VSYNC_SPAN = 16666667;
    static final Interpolator mResizeInterpolator = null;
    private static final boolean mSupportAod = false;
    static final ArrayList<ComponentCallbacks> sConfigCallbacks = null;
    static boolean sFirstDrawComplete;
    static final ArrayList<Runnable> sFirstDrawHandlers = null;
    private static boolean sIsFirstFrame;
    static final ThreadLocal<HandlerActionQueue> sRunQueues = null;
    private int lastFrameDefer;
    View mAccessibilityFocusedHost;
    AccessibilityNodeInfo mAccessibilityFocusedVirtualView;
    AccessibilityInteractionConnectionManager mAccessibilityInteractionConnectionManager;
    AccessibilityInteractionController mAccessibilityInteractionController;
    final AccessibilityManager mAccessibilityManager;
    private boolean mActivityRelaunched;
    boolean mAdded;
    boolean mAddedTouchMode;
    boolean mAppVisible;
    boolean mApplyInsetsRequested;
    final AttachInfo mAttachInfo;
    AudioManager mAudioManager;
    final String mBasePackageName;
    private int mCanvasOffsetX;
    private int mCanvasOffsetY;
    View mCapturingView;
    Choreographer mChoreographer;
    int mClientWindowLayoutFlags;
    final ConsumeBatchedInputImmediatelyRunnable mConsumeBatchedInputImmediatelyRunnable;
    boolean mConsumeBatchedInputImmediatelyScheduled;
    boolean mConsumeBatchedInputScheduled;
    final ConsumeBatchedInputRunnable mConsumedBatchedInputRunnable;
    final Context mContext;
    int mCurScrollY;
    View mCurrentDragView;
    private PointerIcon mCustomPointerIcon;
    private boolean mDebugRefreshDirty;
    private long mDeliverInputTime;
    private final int mDensity;
    Rect mDirty;
    final Rect mDispatchContentInsets;
    final Rect mDispatchStableInsets;
    Display mDisplay;
    private final DisplayListener mDisplayListener;
    final DisplayManager mDisplayManager;
    ClipDescription mDragDescription;
    final PointF mDragPoint;
    private boolean mDragResizing;
    boolean mDrawingAllowed;
    FallbackEventHandler mFallbackEventHandler;
    boolean mFirst;
    InputStage mFirstInputStage;
    InputStage mFirstPostImeInputStage;
    private boolean mForceDecorViewVisibility;
    boolean mForceNextWindowRelayout;
    private int mFpsNumFrames;
    private long mFpsPrevTime;
    private long mFpsStartTime;
    boolean mFullRedrawNeeded;
    boolean mHadWindowFocus;
    final ViewRootHandler mHandler;
    boolean mHandlingLayoutInLayoutRequest;
    int mHardwareXOffset;
    int mHardwareYOffset;
    boolean mHasHadWindowFocus;
    int mHeight;
    HighContrastTextManager mHighContrastTextManager;
    private IHwPointEventFilter mHwPointEventFilter;
    private boolean mInLayout;
    InputChannel mInputChannel;
    protected final InputEventConsistencyVerifier mInputEventConsistencyVerifier;
    WindowInputEventReceiver mInputEventReceiver;
    InputQueue mInputQueue;
    Callback mInputQueueCallback;
    final InvalidateOnAnimationRunnable mInvalidateOnAnimationRunnable;
    private boolean mInvalidateRootRequested;
    boolean mIsAmbientMode;
    boolean mIsAnimating;
    boolean mIsCreating;
    boolean mIsDrawing;
    boolean mIsInTraversal;
    final Configuration mLastConfiguration;
    final InternalInsetsInfo mLastGivenInsets;
    boolean mLastInCompatMode;
    boolean mLastOverscanRequested;
    WeakReference<View> mLastScrolledFocus;
    int mLastSystemUiVisibility;
    final PointF mLastTouchPoint;
    int mLastTouchSource;
    boolean mLastWasImTarget;
    private WindowInsets mLastWindowInsets;
    boolean mLayoutRequested;
    ArrayList<View> mLayoutRequesters;
    volatile Object mLocalDragState;
    final WindowLeaked mLocation;
    boolean mLostWindowFocus;
    private boolean mNeedsHwRendererSetup;
    boolean mNewSurfaceNeeded;
    private final int mNoncompatDensity;
    int mOrigWindowType;
    boolean mPausedForTransition;
    boolean mPendingAlwaysConsumeNavBar;
    final Rect mPendingBackDropFrame;
    final Configuration mPendingConfiguration;
    final Rect mPendingContentInsets;
    int mPendingInputEventCount;
    QueuedInputEvent mPendingInputEventHead;
    String mPendingInputEventQueueLengthCounterName;
    QueuedInputEvent mPendingInputEventTail;
    final Rect mPendingOutsets;
    final Rect mPendingOverscanInsets;
    final Rect mPendingStableInsets;
    private ArrayList<LayoutTransition> mPendingTransitions;
    final Rect mPendingVisibleInsets;
    private String mPkgName;
    private int mPointerIconType;
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
    private AppRogInfo mRogInfo;
    private boolean mRogStateChanged;
    private boolean mRogSwitchState;
    HwRogTranslater mRogTranslater;
    boolean mScrollMayChange;
    int mScrollY;
    Scroller mScroller;
    SendWindowContentChangedAccessibilityEvent mSendWindowContentChangedAccessibilityEvent;
    int mSeq;
    private long mSoftDrawTime;
    int mSoftInputMode;
    boolean mStopped;
    final Surface mSurface;
    BaseSurfaceHolder mSurfaceHolder;
    Callback2 mSurfaceHolderCallback;
    InputStage mSyntheticInputStage;
    private String mTag;
    final int mTargetSdkVersion;
    HashSet<View> mTempHashSet;
    final Rect mTempRect;
    final Thread mThread;
    final int[] mTmpLocation;
    final TypedValue mTmpValue;
    Translator mTranslator;
    final Region mTransparentRegion;
    int mTraversalBarrier;
    final TraversalRunnable mTraversalRunnable;
    boolean mTraversalScheduled;
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
    final LayoutParams mWindowAttributes;
    boolean mWindowAttributesChanged;
    int mWindowAttributesChangesFlag;
    @GuardedBy("mWindowCallbacks")
    final ArrayList<WindowCallbacks> mWindowCallbacks;
    CountDownLatch mWindowDrawCountDown;
    final IWindowSession mWindowSession;

    /* renamed from: android.view.ViewRootImpl.2 */
    static class AnonymousClass2 implements Runnable {
        final /* synthetic */ boolean val$enable;

        AnonymousClass2(boolean val$enable) {
            this.val$enable = val$enable;
        }

        public void run() {
            if (this.val$enable) {
                SystemProperties.set("sys.refresh.dirty", "1");
            } else {
                SystemProperties.set("sys.refresh.dirty", "0");
            }
        }
    }

    /* renamed from: android.view.ViewRootImpl.3 */
    class AnonymousClass3 implements Runnable {
        final /* synthetic */ ArrayList val$finalRequesters;

        AnonymousClass3(ArrayList val$finalRequesters) {
            this.val$finalRequesters = val$finalRequesters;
        }

        public void run() {
            int numValidRequests = this.val$finalRequesters.size();
            for (int i = 0; i < numValidRequests; i += ViewRootImpl.MSG_INVALIDATE) {
                View view = (View) this.val$finalRequesters.get(i);
                Log.w("View", "requestLayout() improperly called by " + view + " during second layout pass: posting in next frame");
                view.requestLayout();
            }
        }
    }

    static final class AccessibilityInteractionConnection extends Stub {
        private final WeakReference<ViewRootImpl> mViewRootImpl;

        AccessibilityInteractionConnection(ViewRootImpl viewRootImpl) {
            this.mViewRootImpl = new WeakReference(viewRootImpl);
        }

        public void findAccessibilityNodeInfoByAccessibilityId(long accessibilityNodeId, Region interactiveRegion, int interactionId, IAccessibilityInteractionConnectionCallback callback, int flags, int interrogatingPid, long interrogatingTid, MagnificationSpec spec) {
            ViewRootImpl viewRootImpl = (ViewRootImpl) this.mViewRootImpl.get();
            if (viewRootImpl == null || viewRootImpl.mView == null) {
                try {
                    callback.setFindAccessibilityNodeInfosResult(null, interactionId);
                    return;
                } catch (RemoteException e) {
                    return;
                }
            }
            viewRootImpl.getAccessibilityInteractionController().findAccessibilityNodeInfoByAccessibilityIdClientThread(accessibilityNodeId, interactiveRegion, interactionId, callback, flags, interrogatingPid, interrogatingTid, spec);
        }

        public void performAccessibilityAction(long accessibilityNodeId, int action, Bundle arguments, int interactionId, IAccessibilityInteractionConnectionCallback callback, int flags, int interrogatingPid, long interrogatingTid) {
            ViewRootImpl viewRootImpl = (ViewRootImpl) this.mViewRootImpl.get();
            if (viewRootImpl == null || viewRootImpl.mView == null) {
                try {
                    callback.setPerformAccessibilityActionResult(ViewRootImpl.LOCAL_LOGV, interactionId);
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
                        focusedView.sendAccessibilityEvent(ViewRootImpl.MSG_DISPATCH_APP_VISIBILITY);
                        return;
                    }
                    return;
                }
                return;
            }
            ensureNoConnection();
            ViewRootImpl.this.mHandler.obtainMessage(ViewRootImpl.MSG_CLEAR_ACCESSIBILITY_FOCUS_HOST).sendToTarget();
        }

        public void ensureConnection() {
            if (!(ViewRootImpl.this.mAttachInfo.mAccessibilityWindowId != HwBootFail.STAGE_BOOT_SUCCESS ? ViewRootImpl.USE_MT_RENDERER : ViewRootImpl.LOCAL_LOGV)) {
                ViewRootImpl.this.mAttachInfo.mAccessibilityWindowId = ViewRootImpl.this.mAccessibilityManager.addAccessibilityInteractionConnection(ViewRootImpl.this.mWindow, new AccessibilityInteractionConnection(ViewRootImpl.this));
            }
        }

        public void ensureNoConnection() {
            if (ViewRootImpl.this.mAttachInfo != null) {
                if (ViewRootImpl.this.mAttachInfo.mAccessibilityWindowId != HwBootFail.STAGE_BOOT_SUCCESS ? ViewRootImpl.USE_MT_RENDERER : ViewRootImpl.LOCAL_LOGV) {
                    ViewRootImpl.this.mAttachInfo.mAccessibilityWindowId = HwBootFail.STAGE_BOOT_SUCCESS;
                    ViewRootImpl.this.mAccessibilityManager.removeAccessibilityInteractionConnection(ViewRootImpl.this.mWindow);
                }
            }
        }
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
            if ((q.mFlags & ViewRootImpl.MSG_RESIZED) != 0) {
                forward(q);
            } else if (shouldDropInputEvent(q)) {
                finish(q, ViewRootImpl.LOCAL_LOGV);
            } else {
                apply(q, onProcess(q));
            }
        }

        protected void finish(QueuedInputEvent q, boolean handled) {
            q.mFlags |= ViewRootImpl.MSG_RESIZED;
            if (handled) {
                q.mFlags |= ViewRootImpl.MSG_DISPATCH_APP_VISIBILITY;
            }
            forward(q);
        }

        protected void forward(QueuedInputEvent q) {
            onDeliverToNext(q);
        }

        protected void apply(QueuedInputEvent q, int result) {
            if (result == 0) {
                forward(q);
            } else if (result == FINISH_HANDLED) {
                finish(q, ViewRootImpl.USE_MT_RENDERER);
            } else if (result == FINISH_NOT_HANDLED) {
                finish(q, ViewRootImpl.LOCAL_LOGV);
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
            if (ViewRootImpl.this.mView == null || !ViewRootImpl.this.mAdded) {
                Slog.w(ViewRootImpl.this.mTag, "Dropping event due to root view being removed: " + q.mEvent);
                return ViewRootImpl.USE_MT_RENDERER;
            } else if ((ViewRootImpl.this.mAttachInfo.mHasWindowFocus || q.mEvent.isFromSource(FINISH_NOT_HANDLED)) && !ViewRootImpl.this.mStopped && ((!ViewRootImpl.this.mIsAmbientMode || q.mEvent.isFromSource(FINISH_HANDLED)) && (!ViewRootImpl.this.mPausedForTransition || isBack(q.mEvent)))) {
                return ViewRootImpl.LOCAL_LOGV;
            } else {
                if (ViewRootImpl.isTerminalInputEvent(q.mEvent)) {
                    q.mEvent.cancel();
                    Slog.w(ViewRootImpl.this.mTag, "Cancelling event due to no window focus: " + q.mEvent);
                    return ViewRootImpl.LOCAL_LOGV;
                }
                Slog.w(ViewRootImpl.this.mTag, "Dropping event due to no window focus: " + q.mEvent);
                return ViewRootImpl.USE_MT_RENDERER;
            }
        }

        void dump(String prefix, PrintWriter writer) {
            if (this.mNext != null) {
                this.mNext.dump(prefix, writer);
            }
        }

        private boolean isBack(InputEvent event) {
            boolean z = ViewRootImpl.LOCAL_LOGV;
            if (!(event instanceof KeyEvent)) {
                return ViewRootImpl.LOCAL_LOGV;
            }
            if (((KeyEvent) event).getKeyCode() == ViewRootImpl.MSG_RESIZED) {
                z = ViewRootImpl.USE_MT_RENDERER;
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
            q.mFlags |= ViewRootImpl.MSG_INVALIDATE_RECT;
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
            boolean blocked = ViewRootImpl.LOCAL_LOGV;
            while (curr != null && curr != q) {
                if (!blocked && deviceId == curr.mEvent.getDeviceId()) {
                    blocked = ViewRootImpl.USE_MT_RENDERER;
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
                } else if ((curr.mFlags & ViewRootImpl.MSG_INVALIDATE_RECT) != 0) {
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
            if (result == DEFER) {
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
            this.mQueueLength += ViewRootImpl.MSG_INVALIDATE;
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
            if ((q.mEvent.getSource() & ViewRootImpl.MSG_INVALIDATE_RECT) != 0) {
                return processPointerEvent(q);
            }
            return 0;
        }

        private int processKeyEvent(QueuedInputEvent q) {
            KeyEvent event = q.mEvent;
            if (ViewRootImpl.this.checkForLeavingTouchModeAndConsume(event)) {
                return ViewRootImpl.MSG_INVALIDATE;
            }
            ViewRootImpl.this.mFallbackEventHandler.preDispatchKeyEvent(event);
            return 0;
        }

        private int processPointerEvent(QueuedInputEvent q) {
            MotionEvent event = q.mEvent;
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
            if (!ViewRootImpl.FRONT_FINGERPRINT_NAVIGATION && HwFrameworkFactory.getHwViewRootImpl().filterDecorPointerEvent(ViewRootImpl.this.mContext, event, action, ViewRootImpl.this.mWindowAttributes, ViewRootImpl.this.mDisplay)) {
                return ViewRootImpl.MSG_INVALIDATE;
            }
            if (action == 0 || action == ViewRootImpl.MSG_DISPATCH_APP_VISIBILITY) {
                ViewRootImpl.this.ensureTouchMode(ViewRootImpl.USE_MT_RENDERER);
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
            if (ViewRootImpl.this.mLastWasImTarget && !ViewRootImpl.this.isInLocalFocusMode()) {
                InputMethodManager imm = InputMethodManager.peekInstance();
                if (imm != null) {
                    int result = imm.dispatchInputEvent(q.mEvent, q, this, ViewRootImpl.this.mHandler);
                    if (result == ViewRootImpl.MSG_INVALIDATE) {
                        return ViewRootImpl.MSG_INVALIDATE;
                    }
                    if (result == 0) {
                        return 0;
                    }
                    return ViewRootImpl.MSG_DIE;
                }
            }
            return 0;
        }

        public void onFinishedInputEvent(Object token, boolean handled) {
            QueuedInputEvent q = (QueuedInputEvent) token;
            if (handled) {
                finish(q, ViewRootImpl.USE_MT_RENDERER);
            } else {
                forward(q);
            }
        }
    }

    final class InvalidateOnAnimationRunnable implements Runnable {
        private boolean mPosted;
        private InvalidateInfo[] mTempViewRects;
        private View[] mTempViews;
        private final ArrayList<InvalidateInfo> mViewRects;
        private final ArrayList<View> mViews;

        InvalidateOnAnimationRunnable() {
            this.mViews = new ArrayList();
            this.mViewRects = new ArrayList();
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
                    ViewRootImpl.this.mChoreographer.removeCallbacks(ViewRootImpl.MSG_INVALIDATE, this, null);
                    this.mPosted = ViewRootImpl.LOCAL_LOGV;
                }
            }
        }

        public void run() {
            int i;
            synchronized (this) {
                this.mPosted = ViewRootImpl.LOCAL_LOGV;
                int viewCount = this.mViews.size();
                if (viewCount != 0) {
                    this.mTempViews = (View[]) this.mViews.toArray(this.mTempViews != null ? this.mTempViews : new View[viewCount]);
                    this.mViews.clear();
                }
                int viewRectCount = this.mViewRects.size();
                if (viewRectCount != 0) {
                    this.mTempViewRects = (InvalidateInfo[]) this.mViewRects.toArray(this.mTempViewRects != null ? this.mTempViewRects : new InvalidateInfo[viewRectCount]);
                    this.mViewRects.clear();
                }
            }
            for (i = 0; i < viewCount; i += ViewRootImpl.MSG_INVALIDATE) {
                this.mTempViews[i].invalidate();
                this.mTempViews[i] = null;
            }
            for (i = 0; i < viewRectCount; i += ViewRootImpl.MSG_INVALIDATE) {
                InvalidateInfo info = this.mTempViewRects[i];
                info.target.invalidate(info.left, info.top, info.right, info.bottom);
                info.recycle();
            }
        }

        private void postIfNeededLocked() {
            if (!this.mPosted) {
                ViewRootImpl.this.mChoreographer.postCallback(ViewRootImpl.MSG_INVALIDATE, this, null);
                this.mPosted = ViewRootImpl.USE_MT_RENDERER;
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
            ViewRootImpl.this.mInputQueue.sendInputEvent(q.mEvent, q, ViewRootImpl.LOCAL_LOGV, this);
            return ViewRootImpl.MSG_DIE;
        }

        public void onFinishedInputEvent(Object token, boolean handled) {
            QueuedInputEvent q = (QueuedInputEvent) token;
            if (handled) {
                finish(q, ViewRootImpl.USE_MT_RENDERER);
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
            ViewRootImpl.this.mInputQueue.sendInputEvent(q.mEvent, q, ViewRootImpl.USE_MT_RENDERER, this);
            return ViewRootImpl.MSG_DIE;
        }

        public void onFinishedInputEvent(Object token, boolean handled) {
            QueuedInputEvent q = (QueuedInputEvent) token;
            if (handled) {
                finish(q, ViewRootImpl.USE_MT_RENDERER);
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
            boolean z = ViewRootImpl.LOCAL_LOGV;
            if ((this.mFlags & FLAG_DELIVER_POST_IME) != 0) {
                return ViewRootImpl.USE_MT_RENDERER;
            }
            if (this.mEvent instanceof MotionEvent) {
                z = this.mEvent.isFromSource(FLAG_DEFERRED);
            }
            return z;
        }

        public boolean shouldSendToSynthesizer() {
            if ((this.mFlags & FLAG_UNHANDLED) != 0) {
                return ViewRootImpl.USE_MT_RENDERER;
            }
            return ViewRootImpl.LOCAL_LOGV;
        }

        public String toString() {
            StringBuilder sb = new StringBuilder("QueuedInputEvent{flags=");
            if (!flagToString("UNHANDLED", FLAG_UNHANDLED, flagToString("RESYNTHESIZED", FLAG_RESYNTHESIZED, flagToString("FINISHED_HANDLED", FLAG_FINISHED_HANDLED, flagToString("FINISHED", FLAG_FINISHED, flagToString("DEFERRED", FLAG_DEFERRED, flagToString("DELIVER_POST_IME", FLAG_DELIVER_POST_IME, ViewRootImpl.LOCAL_LOGV, sb), sb), sb), sb), sb), sb)) {
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
            return ViewRootImpl.USE_MT_RENDERER;
        }
    }

    private class SendWindowContentChangedAccessibilityEvent implements Runnable {
        private int mChangeTypes;
        public long mLastEventTimeMillis;
        public View mSource;

        private SendWindowContentChangedAccessibilityEvent() {
            this.mChangeTypes = 0;
        }

        public void run() {
            if (AccessibilityManager.getInstance(ViewRootImpl.this.mContext).isEnabled()) {
                this.mLastEventTimeMillis = SystemClock.uptimeMillis();
                AccessibilityEvent event = AccessibilityEvent.obtain();
                event.setEventType(GL10.GL_EXP);
                event.setContentChangeTypes(this.mChangeTypes);
                this.mSource.sendAccessibilityEventUnchecked(event);
            } else {
                this.mLastEventTimeMillis = 0;
            }
            this.mSource.resetSubtreeAccessibilityStateChanged();
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
        private final SyntheticJoystickHandler mJoystick;
        private final SyntheticKeyboardHandler mKeyboard;
        private final SyntheticTouchNavigationHandler mTouchNavigation;
        private final SyntheticTrackballHandler mTrackball;

        public SyntheticInputStage() {
            super(null);
            this.mTrackball = new SyntheticTrackballHandler();
            this.mJoystick = new SyntheticJoystickHandler();
            this.mTouchNavigation = new SyntheticTouchNavigationHandler();
            this.mKeyboard = new SyntheticKeyboardHandler();
        }

        protected int onProcess(QueuedInputEvent q) {
            q.mFlags |= ViewRootImpl.MSG_DISPATCH_DRAG_LOCATION_EVENT;
            if (q.mEvent instanceof MotionEvent) {
                MotionEvent event = q.mEvent;
                int source = event.getSource();
                if ((source & ViewRootImpl.MSG_RESIZED) != 0) {
                    this.mTrackball.process(event);
                    return ViewRootImpl.MSG_INVALIDATE;
                } else if ((source & ViewRootImpl.MSG_DISPATCH_DRAG_LOCATION_EVENT) != 0) {
                    this.mJoystick.process(event);
                    return ViewRootImpl.MSG_INVALIDATE;
                } else if ((source & AccessibilityNodeInfo.ACTION_SET_TEXT) == AccessibilityNodeInfo.ACTION_SET_TEXT) {
                    this.mTouchNavigation.process(event);
                    return ViewRootImpl.MSG_INVALIDATE;
                }
            } else if ((q.mFlags & 32) != 0) {
                this.mKeyboard.process((KeyEvent) q.mEvent);
                return ViewRootImpl.MSG_INVALIDATE;
            }
            return 0;
        }

        protected void onDeliverToNext(QueuedInputEvent q) {
            if ((q.mFlags & ViewRootImpl.MSG_DISPATCH_DRAG_LOCATION_EVENT) == 0 && (q.mEvent instanceof MotionEvent)) {
                MotionEvent event = q.mEvent;
                int source = event.getSource();
                if ((source & ViewRootImpl.MSG_RESIZED) != 0) {
                    this.mTrackball.cancel(event);
                } else if ((source & ViewRootImpl.MSG_DISPATCH_DRAG_LOCATION_EVENT) != 0) {
                    this.mJoystick.cancel(event);
                } else if ((source & AccessibilityNodeInfo.ACTION_SET_TEXT) == AccessibilityNodeInfo.ACTION_SET_TEXT) {
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
            super(ViewRootImpl.USE_MT_RENDERER);
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_ENQUEUE_X_AXIS_KEY_REPEAT /*1*/:
                case MSG_ENQUEUE_Y_AXIS_KEY_REPEAT /*2*/:
                    KeyEvent oldEvent = msg.obj;
                    KeyEvent e = KeyEvent.changeTimeRepeat(oldEvent, SystemClock.uptimeMillis(), oldEvent.getRepeatCount() + MSG_ENQUEUE_X_AXIS_KEY_REPEAT);
                    if (ViewRootImpl.this.mAttachInfo.mHasWindowFocus) {
                        ViewRootImpl.this.enqueueInputEvent(e);
                        Message m = obtainMessage(msg.what, e);
                        m.setAsynchronous(ViewRootImpl.USE_MT_RENDERER);
                        sendMessageDelayed(m, (long) ViewConfiguration.getKeyRepeatDelay());
                    }
                default:
            }
        }

        public void process(MotionEvent event) {
            switch (event.getActionMasked()) {
                case MSG_ENQUEUE_Y_AXIS_KEY_REPEAT /*2*/:
                    update(event, ViewRootImpl.USE_MT_RENDERER);
                case ViewRootImpl.MSG_DIE /*3*/:
                    cancel(event);
                default:
                    Log.w(ViewRootImpl.this.mTag, "Unexpected action: " + event.getActionMasked());
            }
        }

        private void cancel(MotionEvent event) {
            removeMessages(MSG_ENQUEUE_X_AXIS_KEY_REPEAT);
            removeMessages(MSG_ENQUEUE_Y_AXIS_KEY_REPEAT);
            update(event, ViewRootImpl.LOCAL_LOGV);
        }

        private void update(MotionEvent event, boolean synthesizeNewKeys) {
            long time = event.getEventTime();
            int metaState = event.getMetaState();
            int deviceId = event.getDeviceId();
            int source = event.getSource();
            int xDirection = joystickAxisValueToDirection(event.getAxisValue(ViewRootImpl.MSG_DISPATCH_DRAG_EVENT));
            if (xDirection == 0) {
                xDirection = joystickAxisValueToDirection(event.getX());
            }
            int yDirection = joystickAxisValueToDirection(event.getAxisValue(ViewRootImpl.MSG_DISPATCH_DRAG_LOCATION_EVENT));
            if (yDirection == 0) {
                yDirection = joystickAxisValueToDirection(event.getY());
            }
            if (xDirection != this.mLastXDirection) {
                if (this.mLastXKeyCode != 0) {
                    removeMessages(MSG_ENQUEUE_X_AXIS_KEY_REPEAT);
                    ViewRootImpl.this.enqueueInputEvent(new KeyEvent(time, time, MSG_ENQUEUE_X_AXIS_KEY_REPEAT, this.mLastXKeyCode, 0, metaState, deviceId, 0, GL10.GL_STENCIL_BUFFER_BIT, source));
                    this.mLastXKeyCode = 0;
                }
                this.mLastXDirection = xDirection;
                if (xDirection != 0 && synthesizeNewKeys) {
                    this.mLastXKeyCode = xDirection > 0 ? ViewRootImpl.MSG_INVALIDATE_WORLD : ViewRootImpl.MSG_CLEAR_ACCESSIBILITY_FOCUS_HOST;
                    KeyEvent e = new KeyEvent(time, time, 0, this.mLastXKeyCode, 0, metaState, deviceId, 0, GL10.GL_STENCIL_BUFFER_BIT, source);
                    ViewRootImpl.this.enqueueInputEvent(e);
                    Message m = obtainMessage(MSG_ENQUEUE_X_AXIS_KEY_REPEAT, e);
                    m.setAsynchronous(ViewRootImpl.USE_MT_RENDERER);
                    sendMessageDelayed(m, (long) ViewConfiguration.getKeyRepeatTimeout());
                }
            }
            if (yDirection != this.mLastYDirection) {
                if (this.mLastYKeyCode != 0) {
                    removeMessages(MSG_ENQUEUE_Y_AXIS_KEY_REPEAT);
                    ViewRootImpl.this.enqueueInputEvent(new KeyEvent(time, time, MSG_ENQUEUE_X_AXIS_KEY_REPEAT, this.mLastYKeyCode, 0, metaState, deviceId, 0, GL10.GL_STENCIL_BUFFER_BIT, source));
                    this.mLastYKeyCode = 0;
                }
                this.mLastYDirection = yDirection;
                if (yDirection != 0 && synthesizeNewKeys) {
                    this.mLastYKeyCode = yDirection > 0 ? 20 : ViewRootImpl.MSG_PROCESS_INPUT_EVENTS;
                    e = new KeyEvent(time, time, 0, this.mLastYKeyCode, 0, metaState, deviceId, 0, GL10.GL_STENCIL_BUFFER_BIT, source);
                    ViewRootImpl.this.enqueueInputEvent(e);
                    m = obtainMessage(MSG_ENQUEUE_Y_AXIS_KEY_REPEAT, e);
                    m.setAsynchronous(ViewRootImpl.USE_MT_RENDERER);
                    sendMessageDelayed(m, (long) ViewConfiguration.getKeyRepeatTimeout());
                }
            }
        }

        private int joystickAxisValueToDirection(float value) {
            if (value >= 0.5f) {
                return MSG_ENQUEUE_X_AXIS_KEY_REPEAT;
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
            if ((event.getFlags() & GL10.GL_STENCIL_BUFFER_BIT) == 0) {
                FallbackAction fallbackAction = event.getKeyCharacterMap().getFallbackAction(event.getKeyCode(), event.getMetaState());
                if (fallbackAction != null) {
                    InputEvent fallbackEvent = KeyEvent.obtain(event.getDownTime(), event.getEventTime(), event.getAction(), fallbackAction.keyCode, event.getRepeatCount(), fallbackAction.metaState, event.getDeviceId(), event.getScanCode(), event.getFlags() | GL10.GL_STENCIL_BUFFER_BIT, event.getSource(), null);
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
        private int mActivePointerId;
        private float mConfigMaxFlingVelocity;
        private float mConfigMinFlingVelocity;
        private float mConfigTickDistance;
        private boolean mConsumedMovement;
        private int mCurrentDeviceId;
        private boolean mCurrentDeviceSupported;
        private int mCurrentSource;
        private final Runnable mFlingRunnable;
        private float mFlingVelocity;
        private boolean mFlinging;
        private float mLastX;
        private float mLastY;
        private int mPendingKeyCode;
        private long mPendingKeyDownTime;
        private int mPendingKeyMetaState;
        private int mPendingKeyRepeatCount;
        private float mStartX;
        private float mStartY;
        private VelocityTracker mVelocityTracker;

        public SyntheticTouchNavigationHandler() {
            super(ViewRootImpl.USE_MT_RENDERER);
            this.mCurrentDeviceId = -1;
            this.mActivePointerId = -1;
            this.mPendingKeyCode = 0;
            this.mFlingRunnable = new Runnable() {
                public void run() {
                    long time = SystemClock.uptimeMillis();
                    SyntheticTouchNavigationHandler.this.sendKeyDownOrRepeat(time, SyntheticTouchNavigationHandler.this.mPendingKeyCode, SyntheticTouchNavigationHandler.this.mPendingKeyMetaState);
                    SyntheticTouchNavigationHandler syntheticTouchNavigationHandler = SyntheticTouchNavigationHandler.this;
                    syntheticTouchNavigationHandler.mFlingVelocity = syntheticTouchNavigationHandler.mFlingVelocity * SyntheticTouchNavigationHandler.FLING_TICK_DECAY;
                    if (!SyntheticTouchNavigationHandler.this.postFling(time)) {
                        SyntheticTouchNavigationHandler.this.mFlinging = SyntheticTouchNavigationHandler.LOCAL_DEBUG;
                        SyntheticTouchNavigationHandler.this.finishKeys(time);
                    }
                }
            };
        }

        /* JADX WARNING: inconsistent code. */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public void process(MotionEvent event) {
            long time = event.getEventTime();
            int deviceId = event.getDeviceId();
            int source = event.getSource();
            int i = this.mCurrentDeviceId;
            if (r0 == deviceId) {
                i = this.mCurrentSource;
            }
            finishKeys(time);
            finishTracking(time);
            this.mCurrentDeviceId = deviceId;
            this.mCurrentSource = source;
            this.mCurrentDeviceSupported = LOCAL_DEBUG;
            InputDevice device = event.getDevice();
            if (device != null) {
                MotionRange xRange = device.getMotionRange(0);
                MotionRange yRange = device.getMotionRange(ViewRootImpl.MSG_INVALIDATE);
                if (!(xRange == null || yRange == null)) {
                    this.mCurrentDeviceSupported = ViewRootImpl.USE_MT_RENDERER;
                    float xRes = xRange.getResolution();
                    if (xRes <= 0.0f) {
                        xRes = xRange.getRange() / DEFAULT_WIDTH_MILLIMETERS;
                    }
                    float yRes = yRange.getResolution();
                    if (yRes <= 0.0f) {
                        yRes = yRange.getRange() / DEFAULT_WIDTH_MILLIMETERS;
                    }
                    this.mConfigTickDistance = 12.0f * ((xRes + yRes) * 0.5f);
                    this.mConfigMinFlingVelocity = this.mConfigTickDistance * MIN_FLING_VELOCITY_TICKS_PER_SECOND;
                    this.mConfigMaxFlingVelocity = this.mConfigTickDistance * MAX_FLING_VELOCITY_TICKS_PER_SECOND;
                }
            }
            if (this.mCurrentDeviceSupported) {
                int action = event.getActionMasked();
                switch (action) {
                    case HwCfgFilePolicy.GLOBAL /*0*/:
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
                    case ViewRootImpl.MSG_INVALIDATE /*1*/:
                    case ViewRootImpl.MSG_INVALIDATE_RECT /*2*/:
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
                                if (action == ViewRootImpl.MSG_INVALIDATE) {
                                    if (this.mConsumedMovement && this.mPendingKeyCode != 0) {
                                        this.mVelocityTracker.computeCurrentVelocity(RILConstants.RIL_UNSOL_RESPONSE_RADIO_STATE_CHANGED, this.mConfigMaxFlingVelocity);
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
                    case ViewRootImpl.MSG_DIE /*3*/:
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
                    this.mAccumulatedX = consumeAccumulatedMovement(time, metaState, this.mAccumulatedX, ViewRootImpl.MSG_CLEAR_ACCESSIBILITY_FOCUS_HOST, ViewRootImpl.MSG_INVALIDATE_WORLD);
                    this.mAccumulatedY = 0.0f;
                    this.mConsumedMovement = ViewRootImpl.USE_MT_RENDERER;
                }
            } else if (absY >= this.mConfigTickDistance) {
                this.mAccumulatedY = consumeAccumulatedMovement(time, metaState, this.mAccumulatedY, ViewRootImpl.MSG_PROCESS_INPUT_EVENTS, 20);
                this.mAccumulatedX = 0.0f;
                this.mConsumedMovement = ViewRootImpl.USE_MT_RENDERER;
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
                this.mPendingKeyRepeatCount += ViewRootImpl.MSG_INVALIDATE;
            }
            this.mPendingKeyMetaState = metaState;
            ViewRootImpl.this.enqueueInputEvent(new KeyEvent(this.mPendingKeyDownTime, time, 0, this.mPendingKeyCode, this.mPendingKeyRepeatCount, this.mPendingKeyMetaState, this.mCurrentDeviceId, GL10.GL_STENCIL_BUFFER_BIT, this.mCurrentSource));
        }

        private void sendKeyUp(long time) {
            if (this.mPendingKeyCode != 0) {
                ViewRootImpl.this.enqueueInputEvent(new KeyEvent(this.mPendingKeyDownTime, time, ViewRootImpl.MSG_INVALIDATE, this.mPendingKeyCode, 0, this.mPendingKeyMetaState, this.mCurrentDeviceId, 0, GL10.GL_STENCIL_BUFFER_BIT, this.mCurrentSource));
                this.mPendingKeyCode = 0;
            }
        }

        private boolean startFling(long time, float vx, float vy) {
            switch (this.mPendingKeyCode) {
                case ViewRootImpl.MSG_PROCESS_INPUT_EVENTS /*19*/:
                    if ((-vy) >= this.mConfigMinFlingVelocity && Math.abs(vx) < this.mConfigMinFlingVelocity) {
                        this.mFlingVelocity = -vy;
                        break;
                    }
                    return LOCAL_DEBUG;
                case HwPerformance.PERF_TAG_TASK_FORK_ON_B_CLUSTER /*20*/:
                    if (vy >= this.mConfigMinFlingVelocity && Math.abs(vx) < this.mConfigMinFlingVelocity) {
                        this.mFlingVelocity = vy;
                        break;
                    }
                    return LOCAL_DEBUG;
                    break;
                case ViewRootImpl.MSG_CLEAR_ACCESSIBILITY_FOCUS_HOST /*21*/:
                    if ((-vx) >= this.mConfigMinFlingVelocity && Math.abs(vy) < this.mConfigMinFlingVelocity) {
                        this.mFlingVelocity = -vx;
                        break;
                    }
                    return LOCAL_DEBUG;
                case ViewRootImpl.MSG_INVALIDATE_WORLD /*22*/:
                    if (vx >= this.mConfigMinFlingVelocity && Math.abs(vy) < this.mConfigMinFlingVelocity) {
                        this.mFlingVelocity = vx;
                        break;
                    }
                    return LOCAL_DEBUG;
            }
            this.mFlinging = postFling(time);
            return this.mFlinging;
        }

        private boolean postFling(long time) {
            if (this.mFlingVelocity < this.mConfigMinFlingVelocity) {
                return LOCAL_DEBUG;
            }
            postAtTime(this.mFlingRunnable, time + ((long) ((this.mConfigTickDistance / this.mFlingVelocity) * 1000.0f)));
            return ViewRootImpl.USE_MT_RENDERER;
        }

        private void cancelFling() {
            if (this.mFlinging) {
                removeCallbacks(this.mFlingRunnable);
                this.mFlinging = LOCAL_DEBUG;
            }
        }
    }

    final class SyntheticTrackballHandler {
        private long mLastTime;
        private final TrackballAxis mX;
        private final TrackballAxis mY;

        SyntheticTrackballHandler() {
            this.mX = new TrackballAxis();
            this.mY = new TrackballAxis();
        }

        public void process(MotionEvent event) {
            ViewRootImpl viewRootImpl;
            long curTime = SystemClock.uptimeMillis();
            if (this.mLastTime + 250 < curTime) {
                this.mX.reset(0);
                this.mY.reset(0);
                this.mLastTime = curTime;
            }
            int action = event.getAction();
            int metaState = event.getMetaState();
            switch (action) {
                case HwCfgFilePolicy.GLOBAL /*0*/:
                    this.mX.reset(ViewRootImpl.MSG_INVALIDATE_RECT);
                    this.mY.reset(ViewRootImpl.MSG_INVALIDATE_RECT);
                    viewRootImpl = ViewRootImpl.this;
                    r16.enqueueInputEvent(new KeyEvent(curTime, curTime, 0, ViewRootImpl.MSG_WINDOW_MOVED, 0, metaState, -1, 0, GL10.GL_STENCIL_BUFFER_BIT, MetricsEvent.QS_WORKMODE));
                    break;
                case ViewRootImpl.MSG_INVALIDATE /*1*/:
                    this.mX.reset(ViewRootImpl.MSG_INVALIDATE_RECT);
                    this.mY.reset(ViewRootImpl.MSG_INVALIDATE_RECT);
                    viewRootImpl = ViewRootImpl.this;
                    r16.enqueueInputEvent(new KeyEvent(curTime, curTime, ViewRootImpl.MSG_INVALIDATE, ViewRootImpl.MSG_WINDOW_MOVED, 0, metaState, -1, 0, GL10.GL_STENCIL_BUFFER_BIT, MetricsEvent.QS_WORKMODE));
                    break;
            }
            float xOff = this.mX.collect(event.getX(), event.getEventTime(), "X");
            float yOff = this.mY.collect(event.getY(), event.getEventTime(), "Y");
            int keycode = 0;
            int movement = 0;
            float accel = LayoutParams.BRIGHTNESS_OVERRIDE_FULL;
            if (xOff > yOff) {
                movement = this.mX.generate();
                if (movement != 0) {
                    if (movement > 0) {
                        keycode = ViewRootImpl.MSG_INVALIDATE_WORLD;
                    } else {
                        keycode = ViewRootImpl.MSG_CLEAR_ACCESSIBILITY_FOCUS_HOST;
                    }
                    accel = this.mX.acceleration;
                    this.mY.reset(ViewRootImpl.MSG_INVALIDATE_RECT);
                }
            } else if (yOff > 0.0f) {
                movement = this.mY.generate();
                if (movement != 0) {
                    if (movement > 0) {
                        keycode = 20;
                    } else {
                        keycode = ViewRootImpl.MSG_PROCESS_INPUT_EVENTS;
                    }
                    accel = this.mY.acceleration;
                    this.mX.reset(ViewRootImpl.MSG_INVALIDATE_RECT);
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
                    viewRootImpl = ViewRootImpl.this;
                    r16.enqueueInputEvent(new KeyEvent(curTime, curTime, ViewRootImpl.MSG_INVALIDATE_RECT, keycode, repeatCount, metaState, -1, 0, GL10.GL_STENCIL_BUFFER_BIT, MetricsEvent.QS_WORKMODE));
                }
                while (movement > 0) {
                    movement--;
                    curTime = SystemClock.uptimeMillis();
                    ViewRootImpl.this.enqueueInputEvent(new KeyEvent(curTime, curTime, 0, keycode, 0, metaState, -1, 0, GL10.GL_STENCIL_BUFFER_BIT, MetricsEvent.QS_WORKMODE));
                    ViewRootImpl.this.enqueueInputEvent(new KeyEvent(curTime, curTime, ViewRootImpl.MSG_INVALIDATE, keycode, 0, metaState, -1, 0, GL10.GL_STENCIL_BUFFER_BIT, MetricsEvent.QS_WORKMODE));
                }
                this.mLastTime = curTime;
            }
        }

        public void cancel(MotionEvent event) {
            this.mLastTime = -2147483648L;
            if (ViewRootImpl.this.mView != null && ViewRootImpl.this.mAdded) {
                ViewRootImpl.this.ensureTouchMode(ViewRootImpl.LOCAL_LOGV);
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
        float acceleration;
        int dir;
        long lastMoveTime;
        int nonAccelMovement;
        float position;
        int step;

        TrackballAxis() {
            this.acceleration = SUBSEQUENT_INCREMENTAL_MOVEMENT_THRESHOLD;
            this.lastMoveTime = 0;
        }

        void reset(int _step) {
            this.position = 0.0f;
            this.acceleration = SUBSEQUENT_INCREMENTAL_MOVEMENT_THRESHOLD;
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
                    this.acceleration = SUBSEQUENT_INCREMENTAL_MOVEMENT_THRESHOLD;
                    this.lastMoveTime = 0;
                }
                this.dir = ViewRootImpl.MSG_INVALIDATE;
            } else if (off < 0.0f) {
                normTime = (long) ((-off) * 150.0f);
                if (this.dir > 0) {
                    this.position = 0.0f;
                    this.step = 0;
                    this.acceleration = SUBSEQUENT_INCREMENTAL_MOVEMENT_THRESHOLD;
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
                    if (scale > SUBSEQUENT_INCREMENTAL_MOVEMENT_THRESHOLD) {
                        acc *= scale;
                    }
                    if (acc >= MAX_ACCELERATION) {
                        acc = MAX_ACCELERATION;
                    }
                    this.acceleration = acc;
                } else {
                    scale = ((float) (delta - normTime)) * ACCEL_MOVE_SCALING_FACTOR;
                    if (scale > SUBSEQUENT_INCREMENTAL_MOVEMENT_THRESHOLD) {
                        acc /= scale;
                    }
                    if (acc <= SUBSEQUENT_INCREMENTAL_MOVEMENT_THRESHOLD) {
                        acc = SUBSEQUENT_INCREMENTAL_MOVEMENT_THRESHOLD;
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
                int dir = this.position >= 0.0f ? ViewRootImpl.MSG_INVALIDATE : -1;
                switch (this.step) {
                    case HwCfgFilePolicy.GLOBAL /*0*/:
                        if (Math.abs(this.position) >= FIRST_MOVEMENT_THRESHOLD) {
                            movement += dir;
                            this.nonAccelMovement += dir;
                            this.step = ViewRootImpl.MSG_INVALIDATE;
                            break;
                        }
                        return movement;
                    case ViewRootImpl.MSG_INVALIDATE /*1*/:
                        if (Math.abs(this.position) >= SECOND_CUMULATIVE_MOVEMENT_THRESHOLD) {
                            movement += dir;
                            this.nonAccelMovement += dir;
                            this.position -= ((float) dir) * SECOND_CUMULATIVE_MOVEMENT_THRESHOLD;
                            this.step = ViewRootImpl.MSG_INVALIDATE_RECT;
                            break;
                        }
                        return movement;
                    default:
                        if (Math.abs(this.position) >= SUBSEQUENT_INCREMENTAL_MOVEMENT_THRESHOLD) {
                            movement += dir;
                            this.position -= ((float) dir) * SUBSEQUENT_INCREMENTAL_MOVEMENT_THRESHOLD;
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
            boolean not_care_window;
            boolean z = ViewRootImpl.LOCAL_LOGV;
            boolean viewScrollChanged = ViewRootImpl.this.mAttachInfo.mViewScrollChanged;
            ViewRootImpl.this.doTraversal();
            if (ViewRootImpl.sIsFirstFrame) {
                ViewRootImpl.setIsFirstFrame(ViewRootImpl.LOCAL_LOGV);
                String pkg = ViewRootImpl.this.mContext == null ? "Unknown" : ViewRootImpl.this.mContext.getPackageName();
                Jlog.d((int) MetricsEvent.CONFIGURE_NOTIFICATION, pkg, "");
                if (Jlog.isPerfTest()) {
                    Jlog.i(JlogConstants.JLID_ACTIVITY_DISPLAY, "pid=" + Process.myPid() + "&pkg=" + pkg);
                }
            }
            int windowtype = ViewRootImpl.this.mWindowAttributes.type;
            if (windowtype <= LayoutParams.LAST_SUB_WINDOW || windowtype == LayoutParams.TYPE_KEYGUARD || windowtype == LayoutParams.TYPE_WALLPAPER) {
                not_care_window = ViewRootImpl.LOCAL_LOGV;
            } else {
                if (windowtype != AbstractRILConstants.RIL_REQUEST_HW_DATA_CONNECTION_DETACH) {
                    z = ViewRootImpl.USE_MT_RENDERER;
                }
                not_care_window = z;
            }
            if (!not_care_window) {
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
            if ((source & ViewRootImpl.MSG_INVALIDATE_RECT) != 0) {
                return processPointerEvent(q);
            }
            if ((source & ViewRootImpl.MSG_RESIZED) != 0) {
                return processTrackballEvent(q);
            }
            return processGenericMotionEvent(q);
        }

        protected void onDeliverToNext(QueuedInputEvent q) {
            if (ViewRootImpl.this.mUnbufferedInputDispatch && (q.mEvent instanceof MotionEvent) && ((MotionEvent) q.mEvent).isTouchEvent() && ViewRootImpl.isTerminalInputEvent(q.mEvent)) {
                ViewRootImpl.this.mUnbufferedInputDispatch = ViewRootImpl.LOCAL_LOGV;
                ViewRootImpl.this.scheduleConsumeBatchedInput();
            }
            super.onDeliverToNext(q);
        }

        private int processKeyEvent(QueuedInputEvent q) {
            KeyEvent event = q.mEvent;
            if (ViewRootImpl.this.mView.dispatchKeyEvent(event)) {
                return ViewRootImpl.MSG_INVALIDATE;
            }
            if (shouldDropInputEvent(q)) {
                return ViewRootImpl.MSG_INVALIDATE_RECT;
            }
            if (event.getAction() == 0 && event.isCtrlPressed() && event.getRepeatCount() == 0 && !KeyEvent.isModifierKey(event.getKeyCode())) {
                if (ViewRootImpl.this.mView.dispatchKeyShortcutEvent(event)) {
                    return ViewRootImpl.MSG_INVALIDATE;
                }
                if (shouldDropInputEvent(q)) {
                    return ViewRootImpl.MSG_INVALIDATE_RECT;
                }
            }
            if (ViewRootImpl.this.mFallbackEventHandler.dispatchKeyEvent(event)) {
                return ViewRootImpl.MSG_INVALIDATE;
            }
            if (shouldDropInputEvent(q)) {
                return ViewRootImpl.MSG_INVALIDATE_RECT;
            }
            if (event.getAction() == 0) {
                int direction = 0;
                switch (event.getKeyCode()) {
                    case ViewRootImpl.MSG_PROCESS_INPUT_EVENTS /*19*/:
                        if (event.hasNoModifiers()) {
                            direction = 33;
                            break;
                        }
                        break;
                    case HwPerformance.PERF_TAG_TASK_FORK_ON_B_CLUSTER /*20*/:
                        if (event.hasNoModifiers()) {
                            direction = LogPower.END_CHG_ROTATION;
                            break;
                        }
                        break;
                    case ViewRootImpl.MSG_CLEAR_ACCESSIBILITY_FOCUS_HOST /*21*/:
                        if (event.hasNoModifiers()) {
                            direction = ViewRootImpl.MSG_DISPATCH_SYSTEM_UI_VISIBILITY;
                            break;
                        }
                        break;
                    case ViewRootImpl.MSG_INVALIDATE_WORLD /*22*/:
                        if (event.hasNoModifiers()) {
                            direction = 66;
                            break;
                        }
                        break;
                    case StatisticalConstant.TYPE_WIFI_END /*61*/:
                        if (!event.hasNoModifiers()) {
                            if (event.hasModifiers(ViewRootImpl.MSG_INVALIDATE)) {
                                direction = ViewRootImpl.MSG_INVALIDATE;
                                break;
                            }
                        }
                        direction = ViewRootImpl.MSG_INVALIDATE_RECT;
                        break;
                        break;
                }
                if (direction != 0) {
                    View focused = ViewRootImpl.this.mView.findFocus();
                    View v;
                    if (focused != null) {
                        v = focused.focusSearch(direction);
                        if (!(v == null || v == focused)) {
                            focused.getFocusedRect(ViewRootImpl.this.mTempRect);
                            if (ViewRootImpl.this.mView instanceof ViewGroup) {
                                ((ViewGroup) ViewRootImpl.this.mView).offsetDescendantRectToMyCoords(focused, ViewRootImpl.this.mTempRect);
                                ((ViewGroup) ViewRootImpl.this.mView).offsetRectIntoDescendantCoords(v, ViewRootImpl.this.mTempRect);
                            }
                            if (v.requestFocus(direction, ViewRootImpl.this.mTempRect)) {
                                ViewRootImpl.this.playSoundEffect(SoundEffectConstants.getContantForFocusDirection(direction));
                                return ViewRootImpl.MSG_INVALIDATE;
                            }
                        }
                        if (ViewRootImpl.this.mView.dispatchUnhandledMove(focused, direction)) {
                            return ViewRootImpl.MSG_INVALIDATE;
                        }
                    }
                    v = ViewRootImpl.this.focusSearch(null, direction);
                    return (v == null || !v.requestFocus(direction)) ? 0 : ViewRootImpl.MSG_INVALIDATE;
                }
            }
        }

        private int processPointerEvent(QueuedInputEvent q) {
            boolean handled;
            int i = ViewRootImpl.MSG_INVALIDATE;
            MotionEvent event = q.mEvent;
            ViewRootImpl.this.mAttachInfo.mUnbufferedDispatchRequested = ViewRootImpl.LOCAL_LOGV;
            View eventTarget = (!event.isFromSource(InputDevice.SOURCE_MOUSE) || ViewRootImpl.this.mCapturingView == null) ? ViewRootImpl.this.mView : ViewRootImpl.this.mCapturingView;
            ViewRootImpl.this.mAttachInfo.mHandlingPointerEvent = ViewRootImpl.USE_MT_RENDERER;
            if (ViewRootImpl.this.mHwPointEventFilter != null) {
                MotionEvent convertEvent = ViewRootImpl.this.mHwPointEventFilter.convertPointEvent(event);
                if (convertEvent == null) {
                    return ViewRootImpl.MSG_INVALIDATE;
                }
                handled = eventTarget.dispatchPointerEvent(convertEvent);
                ViewRootImpl.this.mHwPointEventFilter.handleDownResult(convertEvent, handled);
                if (convertEvent != event) {
                    convertEvent.recycle();
                }
                MotionEvent additionalEvent = ViewRootImpl.this.mHwPointEventFilter.getAdditionalEvent();
                if (additionalEvent != null) {
                    handled = eventTarget.dispatchPointerEvent(additionalEvent);
                    additionalEvent.recycle();
                }
            } else {
                handled = eventTarget.dispatchPointerEvent(event);
            }
            maybeUpdatePointerIcon(event);
            ViewRootImpl.this.mAttachInfo.mHandlingPointerEvent = ViewRootImpl.LOCAL_LOGV;
            if (ViewRootImpl.this.mAttachInfo.mUnbufferedDispatchRequested && !ViewRootImpl.this.mUnbufferedInputDispatch) {
                ViewRootImpl.this.mUnbufferedInputDispatch = ViewRootImpl.USE_MT_RENDERER;
                if (ViewRootImpl.this.mConsumeBatchedInputScheduled) {
                    ViewRootImpl.this.scheduleConsumeBatchedInputImmediately();
                }
            }
            if (!handled) {
                i = 0;
            }
            return i;
        }

        private void maybeUpdatePointerIcon(MotionEvent event) {
            if (event.getPointerCount() == ViewRootImpl.MSG_INVALIDATE && event.isFromSource(InputDevice.SOURCE_MOUSE)) {
                if (event.getActionMasked() == ViewRootImpl.MSG_DISPATCH_GET_NEW_SURFACE || event.getActionMasked() == ViewRootImpl.MAX_QUEUED_INPUT_EVENT_POOL_SIZE) {
                    ViewRootImpl.this.mPointerIconType = ViewRootImpl.MSG_INVALIDATE;
                }
                if (event.getActionMasked() != ViewRootImpl.MAX_QUEUED_INPUT_EVENT_POOL_SIZE && !ViewRootImpl.this.updatePointerIcon(event) && event.getActionMasked() == ViewRootImpl.MSG_DISPATCH_INPUT_EVENT) {
                    ViewRootImpl.this.mPointerIconType = ViewRootImpl.MSG_INVALIDATE;
                }
            }
        }

        private int processTrackballEvent(QueuedInputEvent q) {
            if (ViewRootImpl.this.mView.dispatchTrackballEvent(q.mEvent)) {
                return ViewRootImpl.MSG_INVALIDATE;
            }
            return 0;
        }

        private int processGenericMotionEvent(QueuedInputEvent q) {
            if (ViewRootImpl.this.mView.dispatchGenericMotionEvent(q.mEvent)) {
                return ViewRootImpl.MSG_INVALIDATE;
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
                return ViewRootImpl.MSG_INVALIDATE;
            }
            return 0;
        }
    }

    final class ViewRootHandler extends Handler {
        ViewRootHandler() {
        }

        public String getMessageName(Message message) {
            switch (message.what) {
                case ViewRootImpl.MSG_INVALIDATE /*1*/:
                    return "MSG_INVALIDATE";
                case ViewRootImpl.MSG_INVALIDATE_RECT /*2*/:
                    return "MSG_INVALIDATE_RECT";
                case ViewRootImpl.MSG_DIE /*3*/:
                    return "MSG_DIE";
                case ViewRootImpl.MSG_RESIZED /*4*/:
                    return "MSG_RESIZED";
                case ViewRootImpl.MSG_RESIZED_REPORT /*5*/:
                    return "MSG_RESIZED_REPORT";
                case ViewRootImpl.MSG_WINDOW_FOCUS_CHANGED /*6*/:
                    return "MSG_WINDOW_FOCUS_CHANGED";
                case ViewRootImpl.MSG_DISPATCH_INPUT_EVENT /*7*/:
                    return "MSG_DISPATCH_INPUT_EVENT";
                case ViewRootImpl.MSG_DISPATCH_APP_VISIBILITY /*8*/:
                    return "MSG_DISPATCH_APP_VISIBILITY";
                case ViewRootImpl.MSG_DISPATCH_GET_NEW_SURFACE /*9*/:
                    return "MSG_DISPATCH_GET_NEW_SURFACE";
                case ViewRootImpl.MSG_DISPATCH_KEY_FROM_IME /*11*/:
                    return "MSG_DISPATCH_KEY_FROM_IME";
                case ViewRootImpl.MSG_CHECK_FOCUS /*13*/:
                    return "MSG_CHECK_FOCUS";
                case ViewRootImpl.MSG_CLOSE_SYSTEM_DIALOGS /*14*/:
                    return "MSG_CLOSE_SYSTEM_DIALOGS";
                case ViewRootImpl.MSG_DISPATCH_DRAG_EVENT /*15*/:
                    return "MSG_DISPATCH_DRAG_EVENT";
                case ViewRootImpl.MSG_DISPATCH_DRAG_LOCATION_EVENT /*16*/:
                    return "MSG_DISPATCH_DRAG_LOCATION_EVENT";
                case ViewRootImpl.MSG_DISPATCH_SYSTEM_UI_VISIBILITY /*17*/:
                    return "MSG_DISPATCH_SYSTEM_UI_VISIBILITY";
                case ViewRootImpl.MSG_UPDATE_CONFIGURATION /*18*/:
                    return "MSG_UPDATE_CONFIGURATION";
                case ViewRootImpl.MSG_PROCESS_INPUT_EVENTS /*19*/:
                    return "MSG_PROCESS_INPUT_EVENTS";
                case ViewRootImpl.MSG_CLEAR_ACCESSIBILITY_FOCUS_HOST /*21*/:
                    return "MSG_CLEAR_ACCESSIBILITY_FOCUS_HOST";
                case ViewRootImpl.MSG_WINDOW_MOVED /*23*/:
                    return "MSG_WINDOW_MOVED";
                case ViewRootImpl.MSG_SYNTHESIZE_INPUT_EVENT /*24*/:
                    return "MSG_SYNTHESIZE_INPUT_EVENT";
                case ViewRootImpl.MSG_DISPATCH_WINDOW_SHOWN /*25*/:
                    return "MSG_DISPATCH_WINDOW_SHOWN";
                case ViewRootImpl.MSG_UPDATE_POINTER_ICON /*27*/:
                    return "MSG_UPDATE_POINTER_ICON";
                default:
                    return super.getMessageName(message);
            }
        }

        public boolean sendMessageAtTime(Message msg, long uptimeMillis) {
            if (msg.what != ViewRootImpl.MSG_REQUEST_KEYBOARD_SHORTCUTS || msg.obj != null) {
                return super.sendMessageAtTime(msg, uptimeMillis);
            }
            throw new NullPointerException("Attempted to call MSG_REQUEST_KEYBOARD_SHORTCUTS with null receiver:");
        }

        /* JADX WARNING: inconsistent code. */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public void handleMessage(Message msg) {
            SomeArgs args;
            Configuration config;
            InputMethodManager imm;
            switch (msg.what) {
                case ViewRootImpl.MSG_INVALIDATE /*1*/:
                    ((View) msg.obj).invalidate();
                    break;
                case ViewRootImpl.MSG_INVALIDATE_RECT /*2*/:
                    InvalidateInfo info = msg.obj;
                    info.target.invalidate(info.left, info.top, info.right, info.bottom);
                    info.recycle();
                    break;
                case ViewRootImpl.MSG_DIE /*3*/:
                    ViewRootImpl.this.doDie();
                    break;
                case ViewRootImpl.MSG_RESIZED /*4*/:
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
                                                    break;
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                case ViewRootImpl.MSG_RESIZED_REPORT /*5*/:
                    HwFrameworkFactory.getHwViewRootImpl().clearDisplayPoint();
                    if (ViewRootImpl.this.mAdded) {
                        args = (SomeArgs) msg.obj;
                        config = args.arg4;
                        if (config != null) {
                            ViewRootImpl.this.updateConfiguration(config, ViewRootImpl.LOCAL_LOGV);
                        }
                        boolean framesChanged = (ViewRootImpl.this.mWinFrame.equals(args.arg1) && ViewRootImpl.this.mPendingOverscanInsets.equals(args.arg5) && ViewRootImpl.this.mPendingContentInsets.equals(args.arg2) && ViewRootImpl.this.mPendingStableInsets.equals(args.arg6) && ViewRootImpl.this.mPendingVisibleInsets.equals(args.arg3)) ? ViewRootImpl.this.mPendingOutsets.equals(args.arg7) ? ViewRootImpl.LOCAL_LOGV : ViewRootImpl.USE_MT_RENDERER : ViewRootImpl.USE_MT_RENDERER;
                        ViewRootImpl.this.mWinFrame.set((Rect) args.arg1);
                        ViewRootImpl.this.mPendingOverscanInsets.set((Rect) args.arg5);
                        ViewRootImpl.this.mPendingContentInsets.set((Rect) args.arg2);
                        ViewRootImpl.this.mPendingStableInsets.set((Rect) args.arg6);
                        ViewRootImpl.this.mPendingVisibleInsets.set((Rect) args.arg3);
                        ViewRootImpl.this.mPendingOutsets.set((Rect) args.arg7);
                        ViewRootImpl.this.mPendingBackDropFrame.set((Rect) args.arg8);
                        ViewRootImpl.this.mForceNextWindowRelayout = args.argi1 != 0 ? ViewRootImpl.USE_MT_RENDERER : ViewRootImpl.LOCAL_LOGV;
                        ViewRootImpl.this.mPendingAlwaysConsumeNavBar = args.argi2 != 0 ? ViewRootImpl.USE_MT_RENDERER : ViewRootImpl.LOCAL_LOGV;
                        args.recycle();
                        if (msg.what == ViewRootImpl.MSG_RESIZED_REPORT) {
                            ViewRootImpl.this.mReportNextDraw = ViewRootImpl.USE_MT_RENDERER;
                        }
                        if (ViewRootImpl.this.mView != null && framesChanged) {
                            ViewRootImpl.forceLayout(ViewRootImpl.this.mView);
                        }
                        ViewRootImpl.this.requestLayout();
                        break;
                    }
                    break;
                case ViewRootImpl.MSG_WINDOW_FOCUS_CHANGED /*6*/:
                    if (ViewRootImpl.this.mAdded) {
                        boolean hasWindowFocus = msg.arg1 != 0 ? ViewRootImpl.USE_MT_RENDERER : ViewRootImpl.LOCAL_LOGV;
                        ViewRootImpl.this.mAttachInfo.mHasWindowFocus = hasWindowFocus;
                        ViewRootImpl.this.profileRendering(hasWindowFocus);
                        if (hasWindowFocus) {
                            ViewRootImpl.this.ensureTouchModeLocally(msg.arg2 != 0 ? ViewRootImpl.USE_MT_RENDERER : ViewRootImpl.LOCAL_LOGV);
                            if (ViewRootImpl.this.mAttachInfo.mHardwareRenderer != null && ViewRootImpl.this.mSurface.isValid()) {
                                ViewRootImpl.this.mFullRedrawNeeded = ViewRootImpl.USE_MT_RENDERER;
                                try {
                                    LayoutParams lp = ViewRootImpl.this.mWindowAttributes;
                                    ViewRootImpl.this.mAttachInfo.mHardwareRenderer.initializeIfNeeded(ViewRootImpl.this.mWidth, ViewRootImpl.this.mHeight, ViewRootImpl.this.mAttachInfo, ViewRootImpl.this.mSurface, lp != null ? lp.surfaceInsets : null);
                                } catch (Throwable e) {
                                    Log.e(ViewRootImpl.this.mTag, "OutOfResourcesException locking surface", e);
                                    try {
                                        if (!ViewRootImpl.this.mWindowSession.outOfMemory(ViewRootImpl.this.mWindow)) {
                                            Slog.w(ViewRootImpl.this.mTag, "No processes killed for memory; killing self");
                                            Process.killProcess(Process.myPid());
                                        }
                                    } catch (RemoteException e2) {
                                    }
                                    sendMessageDelayed(obtainMessage(msg.what, msg.arg1, msg.arg2), 500);
                                    return;
                                }
                            }
                        }
                        ViewRootImpl.this.mLastWasImTarget = LayoutParams.mayUseInputMethod(ViewRootImpl.this.mWindowAttributes.flags);
                        imm = InputMethodManager.peekInstance();
                        if (!(imm == null || !ViewRootImpl.this.mLastWasImTarget || ViewRootImpl.this.isInLocalFocusMode())) {
                            imm.onPreWindowFocus(ViewRootImpl.this.mView, hasWindowFocus);
                        }
                        if (ViewRootImpl.this.mView != null) {
                            ViewRootImpl.this.mAttachInfo.mKeyDispatchState.reset();
                            ViewRootImpl.this.mView.dispatchWindowFocusChanged(hasWindowFocus);
                            ViewRootImpl.this.mAttachInfo.mTreeObserver.dispatchOnWindowFocusChange(hasWindowFocus);
                        }
                        if (hasWindowFocus) {
                            if (!(imm == null || !ViewRootImpl.this.mLastWasImTarget || ViewRootImpl.this.isInLocalFocusMode())) {
                                imm.onPostWindowFocus(ViewRootImpl.this.mView, ViewRootImpl.this.mView.findFocus(), ViewRootImpl.this.mWindowAttributes.softInputMode, ViewRootImpl.this.mHasHadWindowFocus ? ViewRootImpl.LOCAL_LOGV : ViewRootImpl.USE_MT_RENDERER, ViewRootImpl.this.mWindowAttributes.flags);
                            }
                            LayoutParams layoutParams = ViewRootImpl.this.mWindowAttributes;
                            layoutParams.softInputMode &= -257;
                            layoutParams = (LayoutParams) ViewRootImpl.this.mView.getLayoutParams();
                            layoutParams.softInputMode &= -257;
                            ViewRootImpl.this.mHasHadWindowFocus = ViewRootImpl.USE_MT_RENDERER;
                            if ("com.tencent.mm".equals(ViewRootImpl.this.mContext.getPackageName())) {
                                ViewRootImpl.this.mView.sendAccessibilityEvent(32);
                                break;
                            }
                        }
                    }
                    break;
                case ViewRootImpl.MSG_DISPATCH_INPUT_EVENT /*7*/:
                    args = (SomeArgs) msg.obj;
                    ViewRootImpl.this.enqueueInputEvent(args.arg1, args.arg2, 0, ViewRootImpl.USE_MT_RENDERER);
                    args.recycle();
                    break;
                case ViewRootImpl.MSG_DISPATCH_APP_VISIBILITY /*8*/:
                    ViewRootImpl.this.handleAppVisibility(msg.arg1 != 0 ? ViewRootImpl.USE_MT_RENDERER : ViewRootImpl.LOCAL_LOGV);
                    break;
                case ViewRootImpl.MSG_DISPATCH_GET_NEW_SURFACE /*9*/:
                    ViewRootImpl.this.handleGetNewSurface();
                    break;
                case ViewRootImpl.MSG_DISPATCH_KEY_FROM_IME /*11*/:
                    InputEvent event = msg.obj;
                    if ((event.getFlags() & ViewRootImpl.MSG_DISPATCH_APP_VISIBILITY) != 0) {
                        event = KeyEvent.changeFlags(event, event.getFlags() & -9);
                    }
                    ViewRootImpl.this.enqueueInputEvent(event, null, ViewRootImpl.MSG_INVALIDATE, ViewRootImpl.USE_MT_RENDERER);
                    break;
                case ViewRootImpl.MSG_CHECK_FOCUS /*13*/:
                    imm = InputMethodManager.peekInstance();
                    if (imm != null) {
                        imm.checkFocus();
                        break;
                    }
                    break;
                case ViewRootImpl.MSG_CLOSE_SYSTEM_DIALOGS /*14*/:
                    if (ViewRootImpl.this.mView != null) {
                        ViewRootImpl.this.mView.onCloseSystemDialogs((String) msg.obj);
                        break;
                    }
                    break;
                case ViewRootImpl.MSG_DISPATCH_DRAG_EVENT /*15*/:
                case ViewRootImpl.MSG_DISPATCH_DRAG_LOCATION_EVENT /*16*/:
                    DragEvent event2 = msg.obj;
                    event2.mLocalState = ViewRootImpl.this.mLocalDragState;
                    ViewRootImpl.this.handleDragEvent(event2);
                    break;
                case ViewRootImpl.MSG_DISPATCH_SYSTEM_UI_VISIBILITY /*17*/:
                    ViewRootImpl.this.handleDispatchSystemUiVisibilityChanged((SystemUiVisibilityInfo) msg.obj);
                    break;
                case ViewRootImpl.MSG_UPDATE_CONFIGURATION /*18*/:
                    config = (Configuration) msg.obj;
                    HwFrameworkFactory.getHwViewRootImpl().clearDisplayPoint();
                    if (config.isOtherSeqNewer(ViewRootImpl.this.mLastConfiguration)) {
                        config = ViewRootImpl.this.mLastConfiguration;
                    }
                    ViewRootImpl.this.updateConfiguration(config, ViewRootImpl.LOCAL_LOGV);
                    break;
                case ViewRootImpl.MSG_PROCESS_INPUT_EVENTS /*19*/:
                    ViewRootImpl.this.mProcessInputEventsScheduled = ViewRootImpl.LOCAL_LOGV;
                    ViewRootImpl.this.doProcessInputEvents();
                    break;
                case ViewRootImpl.MSG_CLEAR_ACCESSIBILITY_FOCUS_HOST /*21*/:
                    ViewRootImpl.this.setAccessibilityFocus(null, null);
                    break;
                case ViewRootImpl.MSG_INVALIDATE_WORLD /*22*/:
                    if (ViewRootImpl.this.mView != null) {
                        ViewRootImpl.this.invalidateWorld(ViewRootImpl.this.mView);
                        break;
                    }
                    break;
                case ViewRootImpl.MSG_WINDOW_MOVED /*23*/:
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
                        boolean isDockedDivider = ViewRootImpl.this.mWindowAttributes.type == LayoutParams.TYPE_DOCK_DIVIDER ? ViewRootImpl.USE_MT_RENDERER : ViewRootImpl.LOCAL_LOGV;
                        if (ViewRootImpl.this.mDragResizing && ViewRootImpl.this.mResizeMode == ViewRootImpl.MSG_INVALIDATE) {
                            suppress = ViewRootImpl.USE_MT_RENDERER;
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
                case ViewRootImpl.MSG_SYNTHESIZE_INPUT_EVENT /*24*/:
                    ViewRootImpl.this.enqueueInputEvent((InputEvent) msg.obj, null, 32, ViewRootImpl.USE_MT_RENDERER);
                    break;
                case ViewRootImpl.MSG_DISPATCH_WINDOW_SHOWN /*25*/:
                    ViewRootImpl.this.handleDispatchWindowShown();
                    break;
                case ViewRootImpl.MSG_REQUEST_KEYBOARD_SHORTCUTS /*26*/:
                    ViewRootImpl.this.handleRequestKeyboardShortcuts(msg.obj, msg.arg1);
                    break;
                case ViewRootImpl.MSG_UPDATE_POINTER_ICON /*27*/:
                    ViewRootImpl.this.resetPointerIcon(msg.obj);
                    break;
                case ViewRootImpl.MSG_ROG_SWITCH_STATE_CHANGE /*101*/:
                    ViewRootImpl.this.handleRogSwitchStateChange(msg.arg1 == ViewRootImpl.MSG_INVALIDATE ? ViewRootImpl.USE_MT_RENDERER : ViewRootImpl.LOCAL_LOGV);
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

        public void resized(Rect frame, Rect overscanInsets, Rect contentInsets, Rect visibleInsets, Rect stableInsets, Rect outsets, boolean reportDraw, Configuration newConfig, Rect backDropFrame, boolean forceLayout, boolean alwaysConsumeNavBar) {
            ViewRootImpl viewAncestor = (ViewRootImpl) this.mViewAncestor.get();
            if (viewAncestor != null) {
                viewAncestor.dispatchResized(frame, overscanInsets, contentInsets, visibleInsets, stableInsets, outsets, reportDraw, newConfig, backDropFrame, forceLayout, alwaysConsumeNavBar);
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

        public void windowFocusChanged(boolean hasFocus, boolean inTouchMode) {
            ViewRootImpl viewAncestor = (ViewRootImpl) this.mViewAncestor.get();
            if (viewAncestor != null) {
                viewAncestor.windowFocusChanged(hasFocus, inTouchMode);
            }
        }

        private static int checkCallingPermission(String permission) {
            try {
                return ActivityManagerNative.getDefault().checkPermission(permission, Binder.getCallingPid(), Binder.getCallingUid());
            } catch (RemoteException e) {
                return -1;
            }
        }

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
                            if (clientStream != null) {
                                try {
                                    clientStream.close();
                                } catch (IOException e22) {
                                    e22.printStackTrace();
                                }
                            }
                        } catch (Throwable th2) {
                            th = th2;
                            if (clientStream != null) {
                                try {
                                    clientStream.close();
                                } catch (IOException e222) {
                                    e222.printStackTrace();
                                }
                            }
                            throw th;
                        }
                    } catch (Throwable th3) {
                        th = th3;
                        clientStream = clientStream2;
                        if (clientStream != null) {
                            clientStream.close();
                        }
                        throw th;
                    }
                } catch (IOException e4) {
                    e222 = e4;
                    e222.printStackTrace();
                    if (clientStream != null) {
                        clientStream.close();
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
    }

    final class WindowInputEventReceiver extends InputEventReceiver {
        public WindowInputEventReceiver(InputChannel inputChannel, Looper looper) {
            super(inputChannel, looper);
        }

        public void onInputEvent(InputEvent event) {
            if (HwFrameworkFactory.getHwViewRootImpl().shouldQueueInputEvent(event, ViewRootImpl.this.mContext, ViewRootImpl.this.mView)) {
                ViewRootImpl.this.enqueueInputEvent(event, this, 0, ViewRootImpl.USE_MT_RENDERER);
            } else {
                finishInputEvent(event, ViewRootImpl.LOCAL_LOGV);
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

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.view.ViewRootImpl.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.view.ViewRootImpl.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: android.view.ViewRootImpl.<clinit>():void");
    }

    private boolean collectViewAttributes() {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.view.ViewRootImpl.collectViewAttributes():boolean
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
        throw new UnsupportedOperationException("Method not decompiled: android.view.ViewRootImpl.collectViewAttributes():boolean");
    }

    public static synchronized void setIsFirstFrame(boolean isFirstFrame) {
        synchronized (ViewRootImpl.class) {
            sIsFirstFrame = isFirstFrame;
        }
    }

    public static void setEnablePartialUpdate(boolean isEnable) {
        boolean enable = isEnable;
        new Thread(new AnonymousClass2(isEnable)).start();
    }

    public ViewRootImpl(Context context, Display display) {
        this.mWindowCallbacks = new ArrayList();
        this.mTmpLocation = new int[MSG_INVALIDATE_RECT];
        this.mTmpValue = new TypedValue();
        this.mWindowAttributes = new LayoutParams();
        this.mAppVisible = USE_MT_RENDERER;
        this.mForceDecorViewVisibility = LOCAL_LOGV;
        this.mOrigWindowType = -1;
        this.mStopped = LOCAL_LOGV;
        this.mIsAmbientMode = LOCAL_LOGV;
        this.mPausedForTransition = LOCAL_LOGV;
        this.mLastInCompatMode = LOCAL_LOGV;
        this.mPendingInputEventQueueLengthCounterName = "pq";
        this.mWindowAttributesChanged = LOCAL_LOGV;
        this.mWindowAttributesChangesFlag = 0;
        this.mSurface = new Surface();
        this.mPendingOverscanInsets = new Rect();
        this.mPendingVisibleInsets = new Rect();
        this.mPendingStableInsets = new Rect();
        this.mPendingContentInsets = new Rect();
        this.mPendingOutsets = new Rect();
        this.mPendingBackDropFrame = new Rect();
        this.mLastGivenInsets = new InternalInsetsInfo();
        this.mDispatchContentInsets = new Rect();
        this.mDispatchStableInsets = new Rect();
        this.mLastConfiguration = new Configuration();
        this.mPendingConfiguration = new Configuration();
        this.mDragPoint = new PointF();
        this.mLastTouchPoint = new PointF();
        this.mFpsStartTime = -1;
        this.mFpsPrevTime = -1;
        this.mPointerIconType = MSG_INVALIDATE;
        this.mCustomPointerIcon = null;
        this.mInLayout = LOCAL_LOGV;
        this.mLayoutRequesters = new ArrayList();
        this.mHandlingLayoutInLayoutRequest = LOCAL_LOGV;
        this.mRogStateChanged = LOCAL_LOGV;
        this.mDebugRefreshDirty = LOCAL_LOGV;
        this.mInputEventConsistencyVerifier = InputEventConsistencyVerifier.isInstrumentationEnabled() ? new InputEventConsistencyVerifier(this, 0) : null;
        this.mTag = TAG;
        this.mProfile = LOCAL_LOGV;
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
                            if (oldDisplayState == ViewRootImpl.MSG_INVALIDATE) {
                                ViewRootImpl.this.mFullRedrawNeeded = ViewRootImpl.USE_MT_RENDERER;
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
                if (displayState == ViewRootImpl.MSG_INVALIDATE) {
                    return 0;
                }
                return ViewRootImpl.MSG_INVALIDATE;
            }
        };
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
        this.mViewVisibility = MSG_DISPATCH_APP_VISIBILITY;
        this.mTransparentRegion = new Region();
        this.mPreviousTransparentRegion = new Region();
        this.mFirst = USE_MT_RENDERER;
        this.mAdded = LOCAL_LOGV;
        this.mAttachInfo = new AttachInfo(this.mWindowSession, this.mWindow, display, this, this.mHandler, this);
        this.mAccessibilityManager = AccessibilityManager.getInstance(context);
        this.mAccessibilityInteractionConnectionManager = new AccessibilityInteractionConnectionManager();
        this.mAccessibilityManager.addAccessibilityStateChangeListener(this.mAccessibilityInteractionConnectionManager);
        this.mHighContrastTextManager = new HighContrastTextManager();
        this.mAccessibilityManager.addHighTextContrastStateChangeListener(this.mHighContrastTextManager);
        this.mViewConfiguration = ViewConfiguration.get(context);
        this.mDensity = context.getResources().getDisplayMetrics().densityDpi;
        this.mNoncompatDensity = context.getResources().getDisplayMetrics().noncompatDensityDpi;
        this.mFallbackEventHandler = new PhoneFallbackEventHandler(context);
        this.mChoreographer = Choreographer.getInstance();
        this.mDisplayManager = (DisplayManager) context.getSystemService("display");
        loadSystemProperties();
        this.mPkgName = context.getPackageName();
        this.mDebugRefreshDirty = debugRefreshDirty();
        this.mHwPointEventFilter = HwFrameworkFactory.getHwPointEventFilter(this.mPkgName);
        HwFrameworkFactory.getHwNsdImpl().createEventAnalyzed();
    }

    public static void addFirstDrawHandler(Runnable callback) {
        synchronized (sFirstDrawHandlers) {
            if (!sFirstDrawComplete) {
                sFirstDrawHandlers.add(callback);
            }
        }
    }

    public static void addConfigCallback(ComponentCallbacks callback) {
        synchronized (sConfigCallbacks) {
            sConfigCallbacks.add(callback);
        }
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
        this.mProfile = USE_MT_RENDERER;
    }

    static boolean isInTouchMode() {
        IWindowSession windowSession = WindowManagerGlobal.peekWindowSession();
        if (windowSession != null) {
            try {
                return windowSession.getInTouchMode();
            } catch (RemoteException e) {
            }
        }
        return LOCAL_LOGV;
    }

    public void notifyChildRebuilt() {
        if (this.mView instanceof RootViewSurfaceTaker) {
            this.mSurfaceHolderCallback = ((RootViewSurfaceTaker) this.mView).willYouTakeTheSurface();
            if (this.mSurfaceHolderCallback != null) {
                this.mSurfaceHolder = new TakenSurfaceHolder();
                this.mSurfaceHolder.setFormat(0);
            } else {
                this.mSurfaceHolder = null;
            }
            this.mInputQueueCallback = ((RootViewSurfaceTaker) this.mView).willYouTakeTheInputQueue();
            if (this.mInputQueueCallback != null) {
                this.mInputQueueCallback.onInputQueueCreated(this.mInputQueue);
            }
        }
    }

    public void setView(View view, LayoutParams attrs, View panelParentView) {
        synchronized (this) {
            if (this.mView == null) {
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
                this.mClientWindowLayoutFlags = attrs.flags;
                setAccessibilityFocus(null, null);
                if (view instanceof RootViewSurfaceTaker) {
                    this.mSurfaceHolderCallback = ((RootViewSurfaceTaker) view).willYouTakeTheSurface();
                    if (this.mSurfaceHolderCallback != null) {
                        this.mSurfaceHolder = new TakenSurfaceHolder();
                        this.mSurfaceHolder.setFormat(0);
                    }
                }
                if (!attrs.hasManualSurfaceInsets) {
                    attrs.setSurfaceInsets(view, LOCAL_LOGV, USE_MT_RENDERER);
                }
                CompatibilityInfo compatibilityInfo = this.mDisplay.getDisplayAdjustments().getCompatibilityInfo();
                this.mTranslator = compatibilityInfo.getTranslator();
                this.mRogTranslater = getRogTranslator();
                if (getRogInfo() != null) {
                    this.mTranslator = null;
                }
                if (this.mSurfaceHolder == null) {
                    enableHardwareAcceleration(attrs);
                }
                boolean restore = LOCAL_LOGV;
                if (this.mRogTranslater != null) {
                    restore = USE_MT_RENDERER;
                    attrs.backup();
                    this.mRogTranslater.translateWindowLayout(attrs);
                    attrs.privateFlags |= AccessibilityNodeInfo.ACTION_DISMISS;
                } else if (this.mTranslator != null) {
                    this.mSurface.setCompatibilityTranslator(this.mTranslator);
                    restore = USE_MT_RENDERER;
                    attrs.backup();
                    this.mTranslator.translateWindowLayout(attrs);
                }
                if (!compatibilityInfo.supportsScreen()) {
                    attrs.privateFlags |= LogPower.START_CHG_ROTATION;
                    this.mLastInCompatMode = USE_MT_RENDERER;
                }
                this.mSoftInputMode = attrs.softInputMode;
                this.mWindowAttributesChanged = USE_MT_RENDERER;
                this.mWindowAttributesChangesFlag = -1;
                this.mAttachInfo.mRootView = view;
                if (this.mRogTranslater != null) {
                    this.mAttachInfo.mScalingRequired = USE_MT_RENDERER;
                    this.mAttachInfo.mApplicationScale = this.mRogTranslater.applicationScale;
                } else {
                    float f;
                    this.mAttachInfo.mScalingRequired = this.mTranslator != null ? USE_MT_RENDERER : LOCAL_LOGV;
                    AttachInfo attachInfo = this.mAttachInfo;
                    if (this.mTranslator == null) {
                        f = LayoutParams.BRIGHTNESS_OVERRIDE_FULL;
                    } else {
                        f = this.mTranslator.applicationScale;
                    }
                    attachInfo.mApplicationScale = f;
                }
                if (panelParentView != null) {
                    this.mAttachInfo.mPanelParentWindowToken = panelParentView.getApplicationWindowToken();
                }
                this.mAdded = USE_MT_RENDERER;
                requestLayout();
                if ((this.mWindowAttributes.inputFeatures & MSG_INVALIDATE_RECT) == 0) {
                    this.mInputChannel = new InputChannel();
                }
                this.mForceDecorViewVisibility = (this.mWindowAttributes.privateFlags & GL10.GL_LIGHT0) != 0 ? USE_MT_RENDERER : LOCAL_LOGV;
                try {
                    this.mOrigWindowType = this.mWindowAttributes.type;
                    this.mAttachInfo.mRecomputeGlobalAttributes = USE_MT_RENDERER;
                    collectViewAttributes();
                    int res = this.mWindowSession.addToDisplay(this.mWindow, this.mSeq, this.mWindowAttributes, getHostVisibility(), this.mDisplay.getDisplayId(), this.mAttachInfo.mContentInsets, this.mAttachInfo.mStableInsets, this.mAttachInfo.mOutsets, this.mInputChannel);
                    if (restore) {
                        attrs.restore();
                    }
                    if (this.mRogTranslater == null && this.mTranslator != null) {
                        this.mTranslator.translateRectInScreenToAppWindow(this.mAttachInfo.mContentInsets);
                    }
                    this.mPendingOverscanInsets.set(0, 0, 0, 0);
                    this.mPendingContentInsets.set(this.mAttachInfo.mContentInsets);
                    this.mPendingStableInsets.set(this.mAttachInfo.mStableInsets);
                    this.mPendingVisibleInsets.set(0, 0, 0, 0);
                    this.mAttachInfo.mAlwaysConsumeNavBar = (res & MSG_RESIZED) != 0 ? USE_MT_RENDERER : LOCAL_LOGV;
                    this.mPendingAlwaysConsumeNavBar = this.mAttachInfo.mAlwaysConsumeNavBar;
                    if (res < 0) {
                        this.mAttachInfo.mRootView = null;
                        this.mAdded = LOCAL_LOGV;
                        this.mFallbackEventHandler.setView(null);
                        unscheduleTraversals();
                        setAccessibilityFocus(null, null);
                        switch (res) {
                            case WebViewClient.ERROR_UNSUPPORTED_SCHEME /*-10*/:
                                throw new InvalidDisplayException("Unable to add window " + this.mWindow + " -- the specified window type " + this.mWindowAttributes.type + " is not valid");
                            case WebViewClient.ERROR_REDIRECT_LOOP /*-9*/:
                                throw new InvalidDisplayException("Unable to add window " + this.mWindow + " -- the specified display can not be found");
                            case WebViewClient.ERROR_TIMEOUT /*-8*/:
                                throw new BadTokenException("Unable to add window " + this.mWindow + " -- permission denied for window type " + this.mWindowAttributes.type);
                            case PackageHelper.RECOMMEND_FAILED_VERSION_DOWNGRADE /*-7*/:
                                throw new BadTokenException("Unable to add window " + this.mWindow + " -- another window of type " + this.mWindowAttributes.type + " already exists");
                            case PackageHelper.RECOMMEND_FAILED_INVALID_URI /*-6*/:
                                return;
                            case PackageHelper.RECOMMEND_MEDIA_UNAVAILABLE /*-5*/:
                                throw new BadTokenException("Unable to add window -- window " + this.mWindow + " has already been added");
                            case PackageHelper.RECOMMEND_FAILED_ALREADY_EXISTS /*-4*/:
                                throw new BadTokenException("Unable to add window -- app for token " + attrs.token + " is exiting");
                            case HwPerformance.REQUEST_INPUT_INVALID /*-3*/:
                                throw new BadTokenException("Unable to add window -- token " + attrs.token + " is not for an application");
                            case HwPerformance.REQUEST_PLATFORM_NOTSUPPORT /*-2*/:
                            case PGSdk.TYPE_UNKNOW /*-1*/:
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
                        this.mAddedTouchMode = (res & MSG_INVALIDATE) != 0 ? USE_MT_RENDERER : LOCAL_LOGV;
                        this.mAppVisible = (res & MSG_INVALIDATE_RECT) != 0 ? USE_MT_RENDERER : LOCAL_LOGV;
                        if (this.mAccessibilityManager.isEnabled()) {
                            this.mAccessibilityInteractionConnectionManager.ensureConnection();
                        }
                        if (view.getImportantForAccessibility() == 0) {
                            view.setImportantForAccessibility(MSG_INVALIDATE);
                        }
                        CharSequence counterSuffix = attrs.getTitle();
                        this.mSyntheticInputStage = new SyntheticInputStage();
                        InputStage earlyPostImeInputStage = new EarlyPostImeInputStage(new NativePostImeInputStage(new ViewPostImeInputStage(this.mSyntheticInputStage), "aq:native-post-ime:" + counterSuffix));
                        this.mFirstInputStage = new NativePreImeInputStage(new ViewPreImeInputStage(new ImeInputStage(earlyPostImeInputStage, "aq:ime:" + counterSuffix)), "aq:native-pre-ime:" + counterSuffix);
                        this.mFirstPostImeInputStage = earlyPostImeInputStage;
                        this.mPendingInputEventQueueLengthCounterName = "aq:pending:" + counterSuffix;
                    }
                } catch (RemoteException e) {
                    this.mAdded = LOCAL_LOGV;
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
        return (this.mWindowAttributes.flags & EditorInfo.IME_FLAG_NO_EXTRACT_UI) != 0 ? USE_MT_RENDERER : LOCAL_LOGV;
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
        if (this.mAttachInfo.mHardwareRenderer != null) {
            this.mAttachInfo.mHardwareRenderer.destroyHardwareResources(this.mView);
            this.mAttachInfo.mHardwareRenderer.destroy();
        }
    }

    public void detachFunctor(long functor) {
        if (this.mAttachInfo.mHardwareRenderer != null) {
            this.mAttachInfo.mHardwareRenderer.stopDrawing();
        }
    }

    public static void invokeFunctor(long functor, boolean waitForCompletion) {
        ThreadedRenderer.invokeFunctor(functor, waitForCompletion);
    }

    public void registerAnimatingRenderNode(RenderNode animator) {
        if (this.mAttachInfo.mHardwareRenderer != null) {
            this.mAttachInfo.mHardwareRenderer.registerAnimatingRenderNode(animator);
            return;
        }
        if (this.mAttachInfo.mPendingAnimatingRenderNodes == null) {
            this.mAttachInfo.mPendingAnimatingRenderNodes = new ArrayList();
        }
        this.mAttachInfo.mPendingAnimatingRenderNodes.add(animator);
    }

    private void enableHardwareAcceleration(LayoutParams attrs) {
        boolean hardwareAccelerated = LOCAL_LOGV;
        this.mAttachInfo.mHardwareAccelerated = LOCAL_LOGV;
        this.mAttachInfo.mHardwareAccelerationRequested = LOCAL_LOGV;
        if (this.mTranslator == null && this.mRogTranslater == null) {
            if ((attrs.flags & AsyncService.CMD_ASYNC_SERVICE_DESTROY) != 0) {
                hardwareAccelerated = USE_MT_RENDERER;
            }
            if (hardwareAccelerated && ThreadedRenderer.isAvailable()) {
                boolean fakeHwAccelerated = (attrs.privateFlags & MSG_INVALIDATE) != 0 ? USE_MT_RENDERER : LOCAL_LOGV;
                boolean forceHwAccelerated = (attrs.privateFlags & MSG_INVALIDATE_RECT) != 0 ? USE_MT_RENDERER : LOCAL_LOGV;
                if (fakeHwAccelerated) {
                    this.mAttachInfo.mHardwareAccelerationRequested = USE_MT_RENDERER;
                } else if (!ThreadedRenderer.sRendererDisabled || (ThreadedRenderer.sSystemRendererDisabled && forceHwAccelerated)) {
                    if (this.mAttachInfo.mHardwareRenderer != null) {
                        this.mAttachInfo.mHardwareRenderer.destroy();
                    }
                    Rect insets = attrs.surfaceInsets;
                    boolean hasSurfaceInsets = (insets.left == 0 && insets.right == 0 && insets.top == 0) ? insets.bottom != 0 ? USE_MT_RENDERER : LOCAL_LOGV : USE_MT_RENDERER;
                    this.mAttachInfo.mHardwareRenderer = ThreadedRenderer.create(this.mContext, attrs.format == -1 ? hasSurfaceInsets : USE_MT_RENDERER);
                    if (this.mAttachInfo.mHardwareRenderer != null) {
                        this.mAttachInfo.mHardwareRenderer.setName(attrs.getTitle().toString());
                        AttachInfo attachInfo = this.mAttachInfo;
                        this.mAttachInfo.mHardwareAccelerationRequested = USE_MT_RENDERER;
                        attachInfo.mHardwareAccelerated = USE_MT_RENDERER;
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

    void setLayoutParams(LayoutParams attrs, boolean newView) {
        synchronized (this) {
            int oldInsetLeft = this.mWindowAttributes.surfaceInsets.left;
            int oldInsetTop = this.mWindowAttributes.surfaceInsets.top;
            int oldInsetRight = this.mWindowAttributes.surfaceInsets.right;
            int oldInsetBottom = this.mWindowAttributes.surfaceInsets.bottom;
            int oldSoftInputMode = this.mWindowAttributes.softInputMode;
            boolean oldHasManualSurfaceInsets = this.mWindowAttributes.hasManualSurfaceInsets;
            this.mClientWindowLayoutFlags = attrs.flags;
            int compatibleWindowFlag = this.mWindowAttributes.privateFlags & LogPower.START_CHG_ROTATION;
            int rogModeFlag = this.mWindowAttributes.privateFlags & AccessibilityNodeInfo.ACTION_DISMISS;
            attrs.systemUiVisibility = this.mWindowAttributes.systemUiVisibility;
            attrs.subtreeSystemUiVisibility = this.mWindowAttributes.subtreeSystemUiVisibility;
            this.mWindowAttributesChangesFlag = this.mWindowAttributes.copyFrom(attrs);
            if ((this.mWindowAttributesChangesFlag & Protocol.BASE_CONNECTIVITY_MANAGER) != 0) {
                this.mAttachInfo.mRecomputeGlobalAttributes = USE_MT_RENDERER;
            }
            if ((this.mWindowAttributesChangesFlag & MSG_INVALIDATE) != 0) {
                this.mAttachInfo.mNeedsUpdateLightCenter = USE_MT_RENDERER;
            }
            if (this.mWindowAttributes.packageName == null) {
                this.mWindowAttributes.packageName = this.mBasePackageName;
            }
            LayoutParams layoutParams = this.mWindowAttributes;
            layoutParams.privateFlags |= compatibleWindowFlag;
            layoutParams = this.mWindowAttributes;
            layoutParams.privateFlags |= rogModeFlag;
            if (this.mWindowAttributes.preservePreviousSurfaceInsets) {
                this.mWindowAttributes.surfaceInsets.set(oldInsetLeft, oldInsetTop, oldInsetRight, oldInsetBottom);
                this.mWindowAttributes.hasManualSurfaceInsets = oldHasManualSurfaceInsets;
            } else {
                if (this.mWindowAttributes.surfaceInsets.left == oldInsetLeft && this.mWindowAttributes.surfaceInsets.top == oldInsetTop) {
                    if (this.mWindowAttributes.surfaceInsets.right == oldInsetRight) {
                        if (this.mWindowAttributes.surfaceInsets.bottom != oldInsetBottom) {
                        }
                    }
                }
                this.mNeedsHwRendererSetup = USE_MT_RENDERER;
            }
            applyKeepScreenOnFlag(this.mWindowAttributes);
            if (newView) {
                this.mSoftInputMode = attrs.softInputMode;
                requestLayout();
            }
            if ((attrs.softInputMode & IndexSearchConstants.INDEX_BUILD_FLAG_MASK) == 0) {
                this.mWindowAttributes.softInputMode = (this.mWindowAttributes.softInputMode & -241) | (oldSoftInputMode & IndexSearchConstants.INDEX_BUILD_FLAG_MASK);
            }
            this.mWindowAttributesChanged = USE_MT_RENDERER;
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
        this.mNewSurfaceNeeded = USE_MT_RENDERER;
        this.mFullRedrawNeeded = USE_MT_RENDERER;
        scheduleTraversals();
    }

    void pokeDrawLockIfNeeded() {
        int displayState = this.mAttachInfo.mDisplayState;
        if (this.mView == null || !this.mAdded || !this.mTraversalScheduled) {
            return;
        }
        if (displayState == MSG_DIE || (!mSupportAod && displayState == MSG_RESIZED)) {
            try {
                this.mWindowSession.pokeDrawLock(this.mWindow);
            } catch (RemoteException e) {
            }
        }
    }

    public void requestFitSystemWindows() {
        checkThread();
        this.mApplyInsetsRequested = USE_MT_RENDERER;
        scheduleTraversals();
    }

    public void requestLayout() {
        if (!this.mHandlingLayoutInLayoutRequest) {
            checkThread();
            this.mLayoutRequested = USE_MT_RENDERER;
            scheduleTraversals();
        }
    }

    public boolean isLayoutRequested() {
        return this.mLayoutRequested;
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
            for (int i = 0; i < parent.getChildCount(); i += MSG_INVALIDATE) {
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
            if (this.mCurScrollY == 0 && this.mTranslator == null) {
                if (this.mRogTranslater != null) {
                }
                invalidateRectOnScreen(dirty);
                return null;
            }
            this.mTempRect.set(dirty);
            dirty = this.mTempRect;
            if (this.mCurScrollY != 0) {
                dirty.offset(0, -this.mCurScrollY);
            }
            if (this.mRogTranslater == null && this.mTranslator != null) {
                this.mTranslator.translateRectInAppWindowToScreen(dirty);
            }
            if (this.mAttachInfo.mScalingRequired) {
                dirty.inset(-1, -1);
            }
            invalidateRectOnScreen(dirty);
            return null;
        }
    }

    private void invalidateRectOnScreen(Rect dirty) {
        Rect localDirty = this.mDirty;
        if (!(localDirty.isEmpty() || localDirty.contains(dirty))) {
            this.mAttachInfo.mSetIgnoreDirtyState = USE_MT_RENDERER;
            this.mAttachInfo.mIgnoreDirtyState = USE_MT_RENDERER;
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

    void setWindowStopped(boolean stopped) {
        if (this.mStopped != stopped) {
            this.mStopped = stopped;
            ThreadedRenderer renderer = this.mAttachInfo.mHardwareRenderer;
            if (renderer != null) {
                renderer.setStopped(this.mStopped);
            }
            if (!this.mStopped) {
                scheduleTraversals();
            } else if (renderer != null) {
                renderer.destroyHardwareResources(this.mView);
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
        return (this.mAppVisible || this.mForceDecorViewVisibility) ? this.mView.getVisibility() : MSG_DISPATCH_APP_VISIBILITY;
    }

    public void requestTransitionStart(LayoutTransition transition) {
        if (this.mPendingTransitions == null || !this.mPendingTransitions.contains(transition)) {
            if (this.mPendingTransitions == null) {
                this.mPendingTransitions = new ArrayList();
            }
            this.mPendingTransitions.add(transition);
        }
    }

    void notifyRendererOfFramePending() {
        if (this.mAttachInfo.mHardwareRenderer != null) {
            this.mAttachInfo.mHardwareRenderer.notifyFramePending();
        }
    }

    void scheduleTraversals() {
        if (!this.mTraversalScheduled) {
            this.mTraversalScheduled = USE_MT_RENDERER;
            this.mTraversalBarrier = this.mHandler.getLooper().getQueue().postSyncBarrier();
            this.mChoreographer.postCallback(MSG_INVALIDATE_RECT, this.mTraversalRunnable, null);
            if (!this.mUnbufferedInputDispatch) {
                scheduleConsumeBatchedInput();
            }
            notifyRendererOfFramePending();
            pokeDrawLockIfNeeded();
        }
    }

    void unscheduleTraversals() {
        if (this.mTraversalScheduled) {
            this.mTraversalScheduled = LOCAL_LOGV;
            this.mHandler.getLooper().getQueue().removeSyncBarrier(this.mTraversalBarrier);
            this.mChoreographer.removeCallbacks(MSG_INVALIDATE_RECT, this.mTraversalRunnable, null);
        }
    }

    void doTraversal() {
        if (this.mTraversalScheduled) {
            this.mTraversalScheduled = LOCAL_LOGV;
            this.mHandler.getLooper().getQueue().removeSyncBarrier(this.mTraversalBarrier);
            if (this.mProfile) {
                Debug.startMethodTracing("ViewAncestor");
            }
            performTraversals();
            if (this.mProfile) {
                Debug.stopMethodTracing();
                this.mProfile = LOCAL_LOGV;
            }
        }
    }

    private void applyKeepScreenOnFlag(LayoutParams params) {
        if (this.mAttachInfo.mKeepScreenOn) {
            params.flags |= LogPower.START_CHG_ROTATION;
        } else {
            params.flags = (params.flags & -129) | (this.mClientWindowLayoutFlags & LogPower.START_CHG_ROTATION);
        }
    }

    private int getImpliedSystemUiVisibility(LayoutParams params) {
        int vis = 0;
        if ((params.flags & EditorInfo.IME_FLAG_NAVIGATE_PREVIOUS) != 0) {
            vis = GL10.GL_INVALID_ENUM;
        }
        if ((params.flags & EditorInfo.IME_FLAG_NAVIGATE_NEXT) != 0) {
            return vis | GL10.GL_SRC_COLOR;
        }
        return vis;
    }

    private boolean measureHierarchy(View host, LayoutParams lp, Resources res, int desiredWindowWidth, int desiredWindowHeight) {
        boolean windowSizeMayChange = LOCAL_LOGV;
        long preMeasureTime = 0;
        if (Jlog.isPerfTest()) {
            preMeasureTime = System.nanoTime();
        }
        boolean goodMeasure = LOCAL_LOGV;
        if (lp.width == -2) {
            DisplayMetrics packageMetrics = res.getDisplayMetrics();
            res.getValue(R.dimen.config_prefDialogWidth, this.mTmpValue, USE_MT_RENDERER);
            int baseSize = 0;
            if (this.mTmpValue.type == MSG_RESIZED_REPORT) {
                baseSize = (int) this.mTmpValue.getDimension(packageMetrics);
            }
            if (baseSize != 0 && desiredWindowWidth > baseSize) {
                int childWidthMeasureSpec = getRootMeasureSpec(baseSize, lp.width);
                int childHeightMeasureSpec = getRootMeasureSpec(desiredWindowHeight, lp.height);
                performMeasure(childWidthMeasureSpec, childHeightMeasureSpec);
                if ((host.getMeasuredWidthAndState() & AsyncService.CMD_ASYNC_SERVICE_DESTROY) == 0) {
                    goodMeasure = USE_MT_RENDERER;
                } else {
                    performMeasure(getRootMeasureSpec((baseSize + desiredWindowWidth) / MSG_INVALIDATE_RECT, lp.width), childHeightMeasureSpec);
                    if ((host.getMeasuredWidthAndState() & AsyncService.CMD_ASYNC_SERVICE_DESTROY) == 0) {
                        goodMeasure = USE_MT_RENDERER;
                    }
                }
            }
        }
        if (!goodMeasure) {
            performMeasure(getRootMeasureSpec(desiredWindowWidth, lp.width), getRootMeasureSpec(desiredWindowHeight, lp.height));
            if (!(this.mWidth == host.getMeasuredWidth() && this.mHeight == host.getMeasuredHeight())) {
                windowSizeMayChange = USE_MT_RENDERER;
            }
        }
        if (Jlog.isPerfTest()) {
            Jlog.i(LayoutParams.TYPE_DREAM_ALBUM, "#ME:" + (((System.nanoTime() - preMeasureTime) + REDUNDANT) / TimeUtils.NANOS_PER_MS));
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
            if (!(forceConstruct || (this.mPendingContentInsets.equals(contentInsets) && this.mPendingStableInsets.equals(stableInsets)))) {
                contentInsets = this.mPendingContentInsets;
                stableInsets = this.mPendingStableInsets;
            }
            Rect outsets = this.mAttachInfo.mOutsets;
            if (outsets.left <= 0 && outsets.top <= 0 && outsets.right <= 0) {
                if (outsets.bottom > 0) {
                }
                this.mLastWindowInsets = new WindowInsets(contentInsets, null, stableInsets, this.mContext.getResources().getConfiguration().isScreenRound(), this.mAttachInfo.mAlwaysConsumeNavBar);
            }
            contentInsets = new Rect(contentInsets.left + outsets.left, contentInsets.top + outsets.top, contentInsets.right + outsets.right, contentInsets.bottom + outsets.bottom);
            this.mLastWindowInsets = new WindowInsets(contentInsets, null, stableInsets, this.mContext.getResources().getConfiguration().isScreenRound(), this.mAttachInfo.mAlwaysConsumeNavBar);
        }
        return this.mLastWindowInsets;
    }

    void dispatchApplyInsets(View host) {
        host.dispatchApplyWindowInsets(getWindowInsets(USE_MT_RENDERER));
    }

    private static boolean shouldUseDisplaySize(LayoutParams lp) {
        if (lp.type == LayoutParams.TYPE_STATUS_BAR_PANEL || lp.type == AbstractRILConstants.RIL_REQUEST_HW_DATA_CONNECTION_DETACH || lp.type == LayoutParams.TYPE_VOLUME_OVERLAY) {
            return USE_MT_RENDERER;
        }
        return LOCAL_LOGV;
    }

    private int dipToPx(int dip) {
        return (int) ((this.mContext.getResources().getDisplayMetrics().density * ((float) dip)) + 0.5f);
    }

    private void performTraversals() {
        View host = this.mView;
        if (host != null && this.mAdded) {
            Point size;
            int desiredWindowWidth;
            int desiredWindowHeight;
            Configuration config;
            int i;
            boolean computesInternalInsets;
            boolean triggerGlobalLayoutListener;
            this.mIsInTraversal = USE_MT_RENDERER;
            this.mWillDrawSoon = USE_MT_RENDERER;
            int windowSizeMayChange = LOCAL_LOGV;
            boolean newSurface = LOCAL_LOGV;
            boolean surfaceChanged = LOCAL_LOGV;
            LayoutParams lp = this.mWindowAttributes;
            int viewVisibility = getHostVisibility();
            boolean z = !this.mFirst ? this.mViewVisibility == viewVisibility ? this.mNewSurfaceNeeded : USE_MT_RENDERER : LOCAL_LOGV;
            LayoutParams params = null;
            if (this.mWindowAttributesChanged) {
                this.mWindowAttributesChanged = LOCAL_LOGV;
                surfaceChanged = USE_MT_RENDERER;
                params = lp;
            }
            if (this.mDisplay.getDisplayAdjustments().getCompatibilityInfo().supportsScreen() == this.mLastInCompatMode) {
                params = lp;
                this.mFullRedrawNeeded = USE_MT_RENDERER;
                this.mLayoutRequested = USE_MT_RENDERER;
                if (this.mLastInCompatMode) {
                    lp.privateFlags &= -129;
                    this.mLastInCompatMode = LOCAL_LOGV;
                } else {
                    lp.privateFlags |= LogPower.START_CHG_ROTATION;
                    this.mLastInCompatMode = USE_MT_RENDERER;
                }
            }
            if (this.mRogStateChanged) {
                params = lp;
                this.mFullRedrawNeeded = USE_MT_RENDERER;
                this.mLayoutRequested = USE_MT_RENDERER;
                this.mRogStateChanged = LOCAL_LOGV;
            }
            this.mWindowAttributesChangesFlag = 0;
            Rect frame = this.mWinFrame;
            if (this.mFirst) {
                this.mFullRedrawNeeded = USE_MT_RENDERER;
                this.mLayoutRequested = USE_MT_RENDERER;
                if (shouldUseDisplaySize(lp)) {
                    size = new Point();
                    this.mDisplay.getRealSize(size);
                    desiredWindowWidth = size.x;
                    desiredWindowHeight = size.y;
                } else {
                    config = this.mContext.getResources().getConfiguration();
                    desiredWindowWidth = dipToPx(config.screenWidthDp);
                    desiredWindowHeight = dipToPx(config.screenHeightDp);
                }
                this.mAttachInfo.mUse32BitDrawingCache = USE_MT_RENDERER;
                this.mAttachInfo.mHasWindowFocus = LOCAL_LOGV;
                this.mAttachInfo.mWindowVisibility = viewVisibility;
                this.mAttachInfo.mRecomputeGlobalAttributes = LOCAL_LOGV;
                this.mLastConfiguration.setTo(host.getResources().getConfiguration());
                this.mLastSystemUiVisibility = this.mAttachInfo.mSystemUiVisibility;
                if (this.mViewLayoutDirectionInitial == MSG_INVALIDATE_RECT) {
                    host.setLayoutDirection(this.mLastConfiguration.getLayoutDirection());
                }
                host.dispatchAttachedToWindow(this.mAttachInfo, 0);
                this.mAttachInfo.mTreeObserver.dispatchOnWindowAttachedChange(USE_MT_RENDERER);
                dispatchApplyInsets(host);
            } else {
                desiredWindowWidth = frame.width();
                desiredWindowHeight = frame.height();
                if (!(desiredWindowWidth == this.mWidth && desiredWindowHeight == this.mHeight)) {
                    this.mFullRedrawNeeded = USE_MT_RENDERER;
                    this.mLayoutRequested = USE_MT_RENDERER;
                    windowSizeMayChange = USE_MT_RENDERER;
                }
            }
            if (z) {
                this.mAttachInfo.mWindowVisibility = viewVisibility;
                host.dispatchWindowVisibilityChanged(viewVisibility);
                host.dispatchVisibilityAggregated(viewVisibility == 0 ? USE_MT_RENDERER : LOCAL_LOGV);
                if (viewVisibility != 0 || this.mNewSurfaceNeeded) {
                    endDragResizing();
                    destroyHardwareResources();
                }
                if (viewVisibility == MSG_DISPATCH_APP_VISIBILITY) {
                    this.mHasHadWindowFocus = LOCAL_LOGV;
                }
            }
            if (this.mAttachInfo.mWindowVisibility != 0) {
                host.clearAccessibilityFocus();
            }
            getRunQueue().executeActions(this.mAttachInfo.mHandler);
            boolean insetsChanged = LOCAL_LOGV;
            boolean layoutRequested = this.mLayoutRequested ? this.mStopped ? this.mReportNextDraw : USE_MT_RENDERER : LOCAL_LOGV;
            if (layoutRequested) {
                boolean windowSizeMayChange2;
                Resources res = this.mView.getContext().getResources();
                if (this.mFirst) {
                    this.mAttachInfo.mInTouchMode = this.mAddedTouchMode ? LOCAL_LOGV : USE_MT_RENDERER;
                    ensureTouchModeLocally(this.mAddedTouchMode);
                } else {
                    if (!this.mPendingOverscanInsets.equals(this.mAttachInfo.mOverscanInsets)) {
                        insetsChanged = USE_MT_RENDERER;
                    }
                    if (!this.mPendingContentInsets.equals(this.mAttachInfo.mContentInsets)) {
                        insetsChanged = USE_MT_RENDERER;
                    }
                    if (!this.mPendingStableInsets.equals(this.mAttachInfo.mStableInsets)) {
                        insetsChanged = USE_MT_RENDERER;
                    }
                    if (!this.mPendingVisibleInsets.equals(this.mAttachInfo.mVisibleInsets)) {
                        this.mAttachInfo.mVisibleInsets.set(this.mPendingVisibleInsets);
                    }
                    if (!this.mPendingOutsets.equals(this.mAttachInfo.mOutsets)) {
                        insetsChanged = USE_MT_RENDERER;
                    }
                    if (this.mPendingAlwaysConsumeNavBar != this.mAttachInfo.mAlwaysConsumeNavBar) {
                        insetsChanged = USE_MT_RENDERER;
                    }
                    if (lp.width == -2 || lp.height == -2) {
                        windowSizeMayChange2 = USE_MT_RENDERER;
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
                this.mAttachInfo.mForceReportNewAttributes = LOCAL_LOGV;
                params = lp;
            }
            if (this.mFirst || this.mAttachInfo.mViewVisibilityChanged) {
                this.mAttachInfo.mViewVisibilityChanged = LOCAL_LOGV;
                int resizeMode = this.mSoftInputMode & IndexSearchConstants.INDEX_BUILD_FLAG_MASK;
                if (resizeMode == 0) {
                    int N = this.mAttachInfo.mScrollContainers.size();
                    for (i = 0; i < N; i += MSG_INVALIDATE) {
                        if (((View) this.mAttachInfo.mScrollContainers.get(i)).isShown()) {
                            resizeMode = MSG_DISPATCH_DRAG_LOCATION_EVENT;
                        }
                    }
                    if (resizeMode == 0) {
                        resizeMode = 32;
                    }
                    if ((lp.softInputMode & IndexSearchConstants.INDEX_BUILD_FLAG_MASK) != resizeMode) {
                        lp.softInputMode = (lp.softInputMode & -241) | resizeMode;
                        params = lp;
                    }
                }
            }
            if (params != null) {
                if (!((host.mPrivateFlags & GL10.GL_NEVER) == 0 || PixelFormat.formatHasAlpha(params.format))) {
                    params.format = -3;
                }
                this.mAttachInfo.mOverscanRequested = (params.flags & EditorInfo.IME_FLAG_NO_FULLSCREEN) != 0 ? USE_MT_RENDERER : LOCAL_LOGV;
            }
            if (this.mApplyInsetsRequested) {
                this.mApplyInsetsRequested = LOCAL_LOGV;
                this.mLastOverscanRequested = this.mAttachInfo.mOverscanRequested;
                dispatchApplyInsets(host);
                if (this.mLayoutRequested) {
                    windowSizeMayChange |= measureHierarchy(host, lp, this.mView.getContext().getResources(), desiredWindowWidth, desiredWindowHeight);
                }
            }
            if (layoutRequested) {
                this.mLayoutRequested = LOCAL_LOGV;
            }
            boolean windowShouldResize = (!layoutRequested || windowSizeMayChange == 0) ? LOCAL_LOGV : (this.mWidth == host.getMeasuredWidth() && this.mHeight == host.getMeasuredHeight() && (lp.width != -2 || frame.width() >= desiredWindowWidth || frame.width() == this.mWidth)) ? (lp.height != -2 || frame.height() >= desiredWindowHeight) ? LOCAL_LOGV : frame.height() != this.mHeight ? USE_MT_RENDERER : LOCAL_LOGV : USE_MT_RENDERER;
            int i2 = (this.mDragResizing && this.mResizeMode == 0) ? MSG_INVALIDATE : 0;
            windowShouldResize = (windowShouldResize | i2) | this.mActivityRelaunched;
            if (this.mAttachInfo.mTreeObserver.hasComputeInternalInsetsListeners()) {
                computesInternalInsets = USE_MT_RENDERER;
            } else {
                computesInternalInsets = this.mAttachInfo.mHasNonEmptyGivenInternalInsets;
            }
            boolean insetsPending = LOCAL_LOGV;
            int relayoutResult = 0;
            boolean z2 = LOCAL_LOGV;
            int surfaceGenerationId = this.mSurface.getGenerationId();
            boolean isViewVisible = viewVisibility == 0 ? USE_MT_RENDERER : LOCAL_LOGV;
            if (this.mFirst || windowShouldResize || insetsChanged || z || params != null || this.mForceNextWindowRelayout) {
                boolean freeformResizing;
                boolean z3;
                SurfaceHolder.Callback[] callbacks;
                int length;
                ThreadedRenderer hardwareRenderer;
                int childWidthMeasureSpec;
                int childHeightMeasureSpec;
                int width;
                int height;
                boolean measureAgain;
                this.mForceNextWindowRelayout = LOCAL_LOGV;
                if (isViewVisible) {
                    insetsPending = computesInternalInsets ? !this.mFirst ? z : USE_MT_RENDERER : LOCAL_LOGV;
                }
                if (this.mSurfaceHolder != null) {
                    this.mSurfaceHolder.mSurfaceLock.lock();
                    this.mDrawingAllowed = USE_MT_RENDERER;
                }
                boolean z4 = LOCAL_LOGV;
                boolean z5 = LOCAL_LOGV;
                boolean hadSurface = this.mSurface.isValid();
                int fl = 0;
                if (params != null) {
                    try {
                        fl = params.flags;
                        if (this.mAttachInfo.mKeepScreenOn) {
                            params.flags |= LogPower.START_CHG_ROTATION;
                        }
                        params.subtreeSystemUiVisibility = this.mAttachInfo.mSystemUiVisibility;
                        params.hasSystemUiListeners = this.mAttachInfo.mHasSystemUiListeners;
                    } catch (RemoteException e) {
                    }
                }
                if (this.mAttachInfo.mHardwareRenderer != null) {
                    if (this.mAttachInfo.mHardwareRenderer.pauseSurface(this.mSurface)) {
                        this.mDirty.set(0, 0, this.mWidth, this.mHeight);
                    }
                    this.mChoreographer.mFrameInfo.addFlags(1);
                }
                relayoutResult = relayoutWindow(params, viewVisibility, insetsPending);
                if (params != null) {
                    params.flags = fl;
                }
                if (this.mPendingConfiguration.seq != 0) {
                    updateConfiguration(new Configuration(this.mPendingConfiguration), this.mFirst ? LOCAL_LOGV : USE_MT_RENDERER);
                    this.mPendingConfiguration.seq = 0;
                    z2 = USE_MT_RENDERER;
                }
                boolean overscanInsetsChanged = this.mPendingOverscanInsets.equals(this.mAttachInfo.mOverscanInsets) ? LOCAL_LOGV : USE_MT_RENDERER;
                boolean contentInsetsChanged = this.mPendingContentInsets.equals(this.mAttachInfo.mContentInsets) ? LOCAL_LOGV : USE_MT_RENDERER;
                boolean visibleInsetsChanged = this.mPendingVisibleInsets.equals(this.mAttachInfo.mVisibleInsets) ? LOCAL_LOGV : USE_MT_RENDERER;
                boolean stableInsetsChanged = this.mPendingStableInsets.equals(this.mAttachInfo.mStableInsets) ? LOCAL_LOGV : USE_MT_RENDERER;
                boolean outsetsChanged = this.mPendingOutsets.equals(this.mAttachInfo.mOutsets) ? LOCAL_LOGV : USE_MT_RENDERER;
                boolean surfaceSizeChanged = (relayoutResult & 32) != 0 ? USE_MT_RENDERER : LOCAL_LOGV;
                boolean alwaysConsumeNavBarChanged = this.mPendingAlwaysConsumeNavBar != this.mAttachInfo.mAlwaysConsumeNavBar ? USE_MT_RENDERER : LOCAL_LOGV;
                if (contentInsetsChanged) {
                    this.mAttachInfo.mContentInsets.set(this.mPendingContentInsets);
                }
                if (overscanInsetsChanged) {
                    this.mAttachInfo.mOverscanInsets.set(this.mPendingOverscanInsets);
                    contentInsetsChanged = USE_MT_RENDERER;
                }
                if (stableInsetsChanged) {
                    this.mAttachInfo.mStableInsets.set(this.mPendingStableInsets);
                    contentInsetsChanged = USE_MT_RENDERER;
                }
                if (alwaysConsumeNavBarChanged) {
                    this.mAttachInfo.mAlwaysConsumeNavBar = this.mPendingAlwaysConsumeNavBar;
                    contentInsetsChanged = USE_MT_RENDERER;
                }
                if (!contentInsetsChanged && this.mLastSystemUiVisibility == this.mAttachInfo.mSystemUiVisibility) {
                    if (!this.mApplyInsetsRequested && this.mLastOverscanRequested == this.mAttachInfo.mOverscanRequested) {
                        if (outsetsChanged) {
                        }
                        if (visibleInsetsChanged) {
                            this.mAttachInfo.mVisibleInsets.set(this.mPendingVisibleInsets);
                        }
                        if (!overscanInsetsChanged || contentInsetsChanged || stableInsetsChanged || visibleInsetsChanged) {
                            z5 = USE_MT_RENDERER;
                        } else {
                            z5 = outsetsChanged;
                        }
                        if (this.mAdded && this.mView != null && r42) {
                            forceLayout(this.mView);
                        }
                        if (hadSurface) {
                            if (this.mSurface.isValid()) {
                                newSurface = USE_MT_RENDERER;
                                this.mFullRedrawNeeded = USE_MT_RENDERER;
                                this.mPreviousTransparentRegion.setEmpty();
                                if (this.mAttachInfo.mHardwareRenderer != null) {
                                    try {
                                        z4 = this.mAttachInfo.mHardwareRenderer.initialize(this.mSurface);
                                        if (z4 && (host.mPrivateFlags & GL10.GL_NEVER) == 0) {
                                            this.mAttachInfo.mHardwareRenderer.allocateBuffers(this.mSurface);
                                        }
                                    } catch (OutOfResourcesException e2) {
                                        handleOutOfResourcesException(e2);
                                        return;
                                    }
                                }
                            }
                        } else if (this.mSurface.isValid()) {
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
                            if (this.mAttachInfo.mHardwareRenderer != null && this.mAttachInfo.mHardwareRenderer.isEnabled()) {
                                if (this.mView != null) {
                                    this.mAttachInfo.mHardwareRenderer.destroyHardwareResources(this.mView);
                                }
                                this.mAttachInfo.mHardwareRenderer.destroy();
                            }
                        } else if ((surfaceGenerationId != this.mSurface.getGenerationId() || surfaceSizeChanged) && this.mSurfaceHolder == null && this.mAttachInfo.mHardwareRenderer != null) {
                            this.mFullRedrawNeeded = USE_MT_RENDERER;
                            try {
                                this.mAttachInfo.mHardwareRenderer.updateSurface(this.mSurface);
                            } catch (OutOfResourcesException e22) {
                                handleOutOfResourcesException(e22);
                                return;
                            }
                        }
                        freeformResizing = (relayoutResult & MSG_DISPATCH_DRAG_LOCATION_EVENT) == 0 ? USE_MT_RENDERER : LOCAL_LOGV;
                        z3 = freeformResizing ? (relayoutResult & MSG_DISPATCH_APP_VISIBILITY) == 0 ? USE_MT_RENDERER : LOCAL_LOGV : USE_MT_RENDERER;
                        if (this.mDragResizing != z3) {
                            if (z3) {
                                endDragResizing();
                            } else {
                                if (freeformResizing) {
                                    i2 = MSG_INVALIDATE;
                                } else {
                                    i2 = 0;
                                }
                                this.mResizeMode = i2;
                                startDragResizing(this.mPendingBackDropFrame, this.mWinFrame.equals(this.mPendingBackDropFrame), this.mPendingVisibleInsets, this.mPendingStableInsets, this.mResizeMode);
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
                            if (this.mSurface.isValid()) {
                                if (!hadSurface) {
                                    this.mSurfaceHolder.ungetCallbacks();
                                    this.mIsCreating = USE_MT_RENDERER;
                                    this.mSurfaceHolderCallback.surfaceCreated(this.mSurfaceHolder);
                                    callbacks = this.mSurfaceHolder.getCallbacks();
                                    if (callbacks != null) {
                                        length = callbacks.length;
                                        for (i2 = 0; i2 < length; i2 += MSG_INVALIDATE) {
                                            callbacks[i2].surfaceCreated(this.mSurfaceHolder);
                                        }
                                    }
                                    surfaceChanged = USE_MT_RENDERER;
                                }
                                if (surfaceChanged || surfaceGenerationId != this.mSurface.getGenerationId()) {
                                    this.mSurfaceHolderCallback.surfaceChanged(this.mSurfaceHolder, lp.format, this.mWidth, this.mHeight);
                                    callbacks = this.mSurfaceHolder.getCallbacks();
                                    if (callbacks != null) {
                                        length = callbacks.length;
                                        for (i2 = 0; i2 < length; i2 += MSG_INVALIDATE) {
                                            callbacks[i2].surfaceChanged(this.mSurfaceHolder, lp.format, this.mWidth, this.mHeight);
                                        }
                                    }
                                }
                                this.mIsCreating = LOCAL_LOGV;
                            } else if (hadSurface) {
                                this.mSurfaceHolder.ungetCallbacks();
                                callbacks = this.mSurfaceHolder.getCallbacks();
                                this.mSurfaceHolderCallback.surfaceDestroyed(this.mSurfaceHolder);
                                if (callbacks != null) {
                                    length = callbacks.length;
                                    for (i2 = 0; i2 < length; i2 += MSG_INVALIDATE) {
                                        callbacks[i2].surfaceDestroyed(this.mSurfaceHolder);
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
                        hardwareRenderer = this.mAttachInfo.mHardwareRenderer;
                        if (hardwareRenderer != null && hardwareRenderer.isEnabled()) {
                            if (!z4 && this.mWidth == hardwareRenderer.getWidth() && this.mHeight == hardwareRenderer.getHeight()) {
                                if (this.mNeedsHwRendererSetup) {
                                }
                            }
                            hardwareRenderer.setup(this.mWidth, this.mHeight, this.mAttachInfo, this.mWindowAttributes.surfaceInsets);
                            this.mNeedsHwRendererSetup = LOCAL_LOGV;
                        }
                        if (!this.mStopped || this.mReportNextDraw) {
                            if (!ensureTouchModeLocally((relayoutResult & MSG_INVALIDATE) == 0 ? USE_MT_RENDERER : LOCAL_LOGV) && this.mWidth == host.getMeasuredWidth() && this.mHeight == host.getMeasuredHeight() && !r42) {
                                if (z2) {
                                }
                            }
                            childWidthMeasureSpec = getRootMeasureSpec(this.mWidth, lp.width);
                            childHeightMeasureSpec = getRootMeasureSpec(this.mHeight, lp.height);
                            performMeasure(childWidthMeasureSpec, childHeightMeasureSpec);
                            width = host.getMeasuredWidth();
                            height = host.getMeasuredHeight();
                            measureAgain = LOCAL_LOGV;
                            if (lp.horizontalWeight > 0.0f) {
                                childWidthMeasureSpec = MeasureSpec.makeMeasureSpec(width + ((int) (((float) (this.mWidth - width)) * lp.horizontalWeight)), EditorInfo.IME_FLAG_NO_ENTER_ACTION);
                                measureAgain = USE_MT_RENDERER;
                            }
                            if (lp.verticalWeight > 0.0f) {
                                childHeightMeasureSpec = MeasureSpec.makeMeasureSpec(height + ((int) (((float) (this.mHeight - height)) * lp.verticalWeight)), EditorInfo.IME_FLAG_NO_ENTER_ACTION);
                                measureAgain = USE_MT_RENDERER;
                            }
                            if (measureAgain) {
                                performMeasure(childWidthMeasureSpec, childHeightMeasureSpec);
                            }
                            layoutRequested = USE_MT_RENDERER;
                        }
                    }
                }
                this.mLastSystemUiVisibility = this.mAttachInfo.mSystemUiVisibility;
                this.mLastOverscanRequested = this.mAttachInfo.mOverscanRequested;
                this.mAttachInfo.mOutsets.set(this.mPendingOutsets);
                this.mApplyInsetsRequested = LOCAL_LOGV;
                dispatchApplyInsets(host);
                if (visibleInsetsChanged) {
                    this.mAttachInfo.mVisibleInsets.set(this.mPendingVisibleInsets);
                }
                if (overscanInsetsChanged) {
                }
                z5 = USE_MT_RENDERER;
                forceLayout(this.mView);
                if (hadSurface) {
                    if (this.mSurface.isValid()) {
                        this.mFullRedrawNeeded = USE_MT_RENDERER;
                        this.mAttachInfo.mHardwareRenderer.updateSurface(this.mSurface);
                    } else {
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
                        if (this.mView != null) {
                            this.mAttachInfo.mHardwareRenderer.destroyHardwareResources(this.mView);
                        }
                        this.mAttachInfo.mHardwareRenderer.destroy();
                    }
                } else if (this.mSurface.isValid()) {
                    newSurface = USE_MT_RENDERER;
                    this.mFullRedrawNeeded = USE_MT_RENDERER;
                    this.mPreviousTransparentRegion.setEmpty();
                    if (this.mAttachInfo.mHardwareRenderer != null) {
                        z4 = this.mAttachInfo.mHardwareRenderer.initialize(this.mSurface);
                        this.mAttachInfo.mHardwareRenderer.allocateBuffers(this.mSurface);
                    }
                }
                if ((relayoutResult & MSG_DISPATCH_DRAG_LOCATION_EVENT) == 0) {
                }
                if ((relayoutResult & MSG_DISPATCH_APP_VISIBILITY) == 0) {
                }
                if (freeformResizing) {
                }
                if (this.mDragResizing != z3) {
                    if (z3) {
                        endDragResizing();
                    } else {
                        if (freeformResizing) {
                            i2 = MSG_INVALIDATE;
                        } else {
                            i2 = 0;
                        }
                        this.mResizeMode = i2;
                        startDragResizing(this.mPendingBackDropFrame, this.mWinFrame.equals(this.mPendingBackDropFrame), this.mPendingVisibleInsets, this.mPendingStableInsets, this.mResizeMode);
                    }
                }
                this.mAttachInfo.mWindowLeft = frame.left;
                this.mAttachInfo.mWindowTop = frame.top;
                this.mWidth = frame.width();
                this.mHeight = frame.height();
                if (this.mSurfaceHolder != null) {
                    if (this.mSurface.isValid()) {
                        this.mSurfaceHolder.mSurface = this.mSurface;
                    }
                    this.mSurfaceHolder.setSurfaceFrameSize(this.mWidth, this.mHeight);
                    this.mSurfaceHolder.mSurfaceLock.unlock();
                    if (this.mSurface.isValid()) {
                        if (hadSurface) {
                            this.mSurfaceHolder.ungetCallbacks();
                            this.mIsCreating = USE_MT_RENDERER;
                            this.mSurfaceHolderCallback.surfaceCreated(this.mSurfaceHolder);
                            callbacks = this.mSurfaceHolder.getCallbacks();
                            if (callbacks != null) {
                                length = callbacks.length;
                                for (i2 = 0; i2 < length; i2 += MSG_INVALIDATE) {
                                    callbacks[i2].surfaceCreated(this.mSurfaceHolder);
                                }
                            }
                            surfaceChanged = USE_MT_RENDERER;
                        }
                        this.mSurfaceHolderCallback.surfaceChanged(this.mSurfaceHolder, lp.format, this.mWidth, this.mHeight);
                        callbacks = this.mSurfaceHolder.getCallbacks();
                        if (callbacks != null) {
                            length = callbacks.length;
                            for (i2 = 0; i2 < length; i2 += MSG_INVALIDATE) {
                                callbacks[i2].surfaceChanged(this.mSurfaceHolder, lp.format, this.mWidth, this.mHeight);
                            }
                        }
                        this.mIsCreating = LOCAL_LOGV;
                    } else if (hadSurface) {
                        this.mSurfaceHolder.ungetCallbacks();
                        callbacks = this.mSurfaceHolder.getCallbacks();
                        this.mSurfaceHolderCallback.surfaceDestroyed(this.mSurfaceHolder);
                        if (callbacks != null) {
                            length = callbacks.length;
                            for (i2 = 0; i2 < length; i2 += MSG_INVALIDATE) {
                                callbacks[i2].surfaceDestroyed(this.mSurfaceHolder);
                            }
                        }
                        this.mSurfaceHolder.mSurfaceLock.lock();
                        this.mSurfaceHolder.mSurface = new Surface();
                    }
                }
                hardwareRenderer = this.mAttachInfo.mHardwareRenderer;
                if (this.mNeedsHwRendererSetup) {
                    hardwareRenderer.setup(this.mWidth, this.mHeight, this.mAttachInfo, this.mWindowAttributes.surfaceInsets);
                    this.mNeedsHwRendererSetup = LOCAL_LOGV;
                }
                if ((relayoutResult & MSG_INVALIDATE) == 0) {
                }
                if (z2) {
                    childWidthMeasureSpec = getRootMeasureSpec(this.mWidth, lp.width);
                    childHeightMeasureSpec = getRootMeasureSpec(this.mHeight, lp.height);
                    performMeasure(childWidthMeasureSpec, childHeightMeasureSpec);
                    width = host.getMeasuredWidth();
                    height = host.getMeasuredHeight();
                    measureAgain = LOCAL_LOGV;
                    if (lp.horizontalWeight > 0.0f) {
                        childWidthMeasureSpec = MeasureSpec.makeMeasureSpec(width + ((int) (((float) (this.mWidth - width)) * lp.horizontalWeight)), EditorInfo.IME_FLAG_NO_ENTER_ACTION);
                        measureAgain = USE_MT_RENDERER;
                    }
                    if (lp.verticalWeight > 0.0f) {
                        childHeightMeasureSpec = MeasureSpec.makeMeasureSpec(height + ((int) (((float) (this.mHeight - height)) * lp.verticalWeight)), EditorInfo.IME_FLAG_NO_ENTER_ACTION);
                        measureAgain = USE_MT_RENDERER;
                    }
                    if (measureAgain) {
                        performMeasure(childWidthMeasureSpec, childHeightMeasureSpec);
                    }
                    layoutRequested = USE_MT_RENDERER;
                }
            } else {
                maybeHandleWindowMove(frame);
            }
            boolean didLayout = layoutRequested ? this.mStopped ? this.mReportNextDraw : USE_MT_RENDERER : LOCAL_LOGV;
            if (didLayout) {
                triggerGlobalLayoutListener = USE_MT_RENDERER;
            } else {
                triggerGlobalLayoutListener = this.mAttachInfo.mRecomputeGlobalAttributes;
            }
            if (didLayout) {
                doRelayoutAsyncly(performLayout(lp, this.mWidth, this.mHeight));
                if ((host.mPrivateFlags & GL10.GL_NEVER) != 0) {
                    host.getLocationInWindow(this.mTmpLocation);
                    this.mTransparentRegion.set(this.mTmpLocation[0], this.mTmpLocation[MSG_INVALIDATE], (this.mTmpLocation[0] + host.mRight) - host.mLeft, (this.mTmpLocation[MSG_INVALIDATE] + host.mBottom) - host.mTop);
                    host.gatherTransparentRegion(this.mTransparentRegion);
                    if (this.mRogTranslater == null && this.mTranslator != null) {
                        this.mTranslator.translateRegionInWindowToScreen(this.mTransparentRegion);
                    }
                    if (!this.mTransparentRegion.equals(this.mPreviousTransparentRegion)) {
                        this.mPreviousTransparentRegion.set(this.mTransparentRegion);
                        this.mFullRedrawNeeded = USE_MT_RENDERER;
                        try {
                            this.mWindowSession.setTransparentRegion(this.mWindow, this.mTransparentRegion);
                        } catch (RemoteException e3) {
                        }
                    }
                }
            }
            if (triggerGlobalLayoutListener) {
                this.mAttachInfo.mRecomputeGlobalAttributes = LOCAL_LOGV;
                this.mAttachInfo.mTreeObserver.dispatchOnGlobalLayout();
            }
            if (computesInternalInsets) {
                InternalInsetsInfo insets = this.mAttachInfo.mGivenInternalInsets;
                insets.reset();
                this.mAttachInfo.mTreeObserver.dispatchOnComputeInternalInsets(insets);
                this.mAttachInfo.mHasNonEmptyGivenInternalInsets = insets.isEmpty() ? LOCAL_LOGV : USE_MT_RENDERER;
                if (insetsPending || !this.mLastGivenInsets.equals(insets)) {
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
            if (!(!this.mFirst || this.mView == null || this.mView.hasFocus())) {
                this.mView.requestFocus(MSG_INVALIDATE_RECT);
            }
            boolean z6 = (z || this.mFirst) ? isViewVisible : LOCAL_LOGV;
            boolean hasWindowFocus = this.mAttachInfo.mHasWindowFocus ? isViewVisible : LOCAL_LOGV;
            boolean regainedFocus = hasWindowFocus ? this.mLostWindowFocus : LOCAL_LOGV;
            if (regainedFocus) {
                this.mLostWindowFocus = LOCAL_LOGV;
            } else if (!hasWindowFocus && this.mHadWindowFocus) {
                this.mLostWindowFocus = USE_MT_RENDERER;
            }
            if (z6 || regainedFocus) {
                host.sendAccessibilityEvent(32);
            }
            this.mFirst = LOCAL_LOGV;
            this.mWillDrawSoon = LOCAL_LOGV;
            this.mNewSurfaceNeeded = LOCAL_LOGV;
            this.mActivityRelaunched = LOCAL_LOGV;
            this.mViewVisibility = viewVisibility;
            this.mHadWindowFocus = hasWindowFocus;
            if (hasWindowFocus && !isInLocalFocusMode()) {
                boolean imTarget = LayoutParams.mayUseInputMethod(this.mWindowAttributes.flags);
                if (imTarget != this.mLastWasImTarget) {
                    this.mLastWasImTarget = imTarget;
                    InputMethodManager imm = InputMethodManager.peekInstance();
                    if (imm != null && imTarget) {
                        imm.onPreWindowFocus(this.mView, hasWindowFocus);
                        imm.onPostWindowFocus(this.mView, this.mView.findFocus(), this.mWindowAttributes.softInputMode, this.mHasHadWindowFocus ? LOCAL_LOGV : USE_MT_RENDERER, this.mWindowAttributes.flags);
                    }
                }
            }
            if ((relayoutResult & MSG_INVALIDATE_RECT) != 0) {
                this.mReportNextDraw = USE_MT_RENDERER;
            }
            boolean cancelDraw = (this.mAttachInfo.mTreeObserver.dispatchOnPreDraw() || !isViewVisible) ? USE_MT_RENDERER : LOCAL_LOGV;
            if (!cancelDraw && !newSurface) {
                if (this.mPendingTransitions != null && this.mPendingTransitions.size() > 0) {
                    for (i = 0; i < this.mPendingTransitions.size(); i += MSG_INVALIDATE) {
                        ((LayoutTransition) this.mPendingTransitions.get(i)).startChangingAnimations();
                    }
                    this.mPendingTransitions.clear();
                }
                performDraw();
            } else if (isViewVisible) {
                scheduleTraversals();
            } else if (this.mPendingTransitions != null && this.mPendingTransitions.size() > 0) {
                for (i = 0; i < this.mPendingTransitions.size(); i += MSG_INVALIDATE) {
                    ((LayoutTransition) this.mPendingTransitions.get(i)).endChangingAnimations();
                }
                this.mPendingTransitions.clear();
            }
            this.mIsInTraversal = LOCAL_LOGV;
        }
    }

    private void maybeHandleWindowMove(Rect frame) {
        boolean windowMoved = USE_MT_RENDERER;
        if (this.mAttachInfo.mWindowLeft == frame.left && this.mAttachInfo.mWindowTop == frame.top) {
            windowMoved = LOCAL_LOGV;
        }
        if (windowMoved) {
            if (this.mTranslator != null) {
                this.mTranslator.translateRectInScreenToAppWinFrame(frame);
            }
            this.mAttachInfo.mWindowLeft = frame.left;
            this.mAttachInfo.mWindowTop = frame.top;
        }
        if (windowMoved || this.mAttachInfo.mNeedsUpdateLightCenter) {
            if (this.mAttachInfo.mHardwareRenderer != null) {
                this.mAttachInfo.mHardwareRenderer.setLightCenter(this.mAttachInfo);
            }
            this.mAttachInfo.mNeedsUpdateLightCenter = LOCAL_LOGV;
        }
    }

    private void handleOutOfResourcesException(OutOfResourcesException e) {
        Log.e(this.mTag, "OutOfResourcesException initializing HW surface", e);
        try {
            if (!(this.mWindowSession.outOfMemory(this.mWindow) || Process.myUid() == RILConstants.RIL_UNSOL_RESPONSE_RADIO_STATE_CHANGED)) {
                Slog.w(this.mTag, "No processes killed for memory; killing self");
                Process.killProcess(Process.myPid());
            }
        } catch (RemoteException e2) {
        }
        this.mLayoutRequested = USE_MT_RENDERER;
    }

    private void performMeasure(int childWidthMeasureSpec, int childHeightMeasureSpec) {
        Trace.traceBegin(8, "measure");
        try {
            this.mView.measure(childWidthMeasureSpec, childHeightMeasureSpec);
        } finally {
            Trace.traceEnd(8);
        }
    }

    boolean isInLayout() {
        return this.mInLayout;
    }

    boolean requestLayoutDuringLayout(View view) {
        if (view.mParent == null || view.mAttachInfo == null) {
            return USE_MT_RENDERER;
        }
        if (!this.mLayoutRequesters.contains(view)) {
            this.mLayoutRequesters.add(view);
        }
        if (this.mHandlingLayoutInLayoutRequest) {
            return LOCAL_LOGV;
        }
        return USE_MT_RENDERER;
    }

    private boolean performLayout(LayoutParams lp, int desiredWindowWidth, int desiredWindowHeight) {
        this.mLayoutRequested = LOCAL_LOGV;
        this.mScrollMayChange = USE_MT_RENDERER;
        this.mInLayout = USE_MT_RENDERER;
        boolean z = LOCAL_LOGV;
        View host = this.mView;
        long preLayoutTime = 0;
        if (Jlog.isPerfTest()) {
            preLayoutTime = System.nanoTime();
        }
        Trace.traceBegin(8, "layout");
        try {
            host.layout(0, 0, host.getMeasuredWidth(), host.getMeasuredHeight());
            this.mInLayout = LOCAL_LOGV;
            if (this.mLayoutRequesters.size() > 0) {
                ArrayList<View> validLayoutRequesters = getValidLayoutRequesters(this.mLayoutRequesters, LOCAL_LOGV);
                if (validLayoutRequesters != null) {
                    this.mHandlingLayoutInLayoutRequest = USE_MT_RENDERER;
                    int numValidRequests = validLayoutRequesters.size();
                    for (int i = 0; i < numValidRequests; i += MSG_INVALIDATE) {
                        View view = (View) validLayoutRequesters.get(i);
                        Log.w("View", "requestLayout() improperly called by " + view + " during layout: running second layout pass");
                        view.requestLayout();
                    }
                    z = measureHierarchy(host, lp, this.mView.getContext().getResources(), desiredWindowWidth, desiredWindowHeight);
                    this.mInLayout = USE_MT_RENDERER;
                    host.layout(0, 0, host.getMeasuredWidth(), host.getMeasuredHeight());
                    this.mHandlingLayoutInLayoutRequest = LOCAL_LOGV;
                    validLayoutRequesters = getValidLayoutRequesters(this.mLayoutRequesters, USE_MT_RENDERER);
                    if (validLayoutRequesters != null) {
                        ArrayList<View> finalRequesters = validLayoutRequesters;
                        getRunQueue().post(new AnonymousClass3(validLayoutRequesters));
                    }
                }
            }
            Trace.traceEnd(8);
            if (Jlog.isPerfTest()) {
                Jlog.i(JlogConstants.JLID_DISPLAY_VIEW_LAYOUT, "#LA:" + (((System.nanoTime() - preLayoutTime) + REDUNDANT) / TimeUtils.NANOS_PER_MS));
            }
            this.mInLayout = LOCAL_LOGV;
            return z;
        } catch (Throwable th) {
            Trace.traceEnd(8);
            if (Jlog.isPerfTest()) {
                Jlog.i(JlogConstants.JLID_DISPLAY_VIEW_LAYOUT, "#LA:" + (((System.nanoTime() - preLayoutTime) + REDUNDANT) / TimeUtils.NANOS_PER_MS));
            }
        }
    }

    private ArrayList<View> getValidLayoutRequesters(ArrayList<View> layoutRequesters, boolean secondLayoutRequests) {
        int i;
        int numViewsRequestingLayout = layoutRequesters.size();
        ArrayList<View> validLayoutRequesters = null;
        for (i = 0; i < numViewsRequestingLayout; i += MSG_INVALIDATE) {
            View view = (View) layoutRequesters.get(i);
            if (!(view == null || view.mAttachInfo == null || view.mParent == null || (!secondLayoutRequests && (view.mPrivateFlags & HwPerformance.PERF_EVENT_RAW_REQ) != HwPerformance.PERF_EVENT_RAW_REQ))) {
                boolean gone = LOCAL_LOGV;
                View view2 = view;
                while (view2 != null) {
                    if ((view2.mViewFlags & 12) == MSG_DISPATCH_APP_VISIBILITY) {
                        gone = USE_MT_RENDERER;
                        break;
                    } else if (view2.mParent instanceof View) {
                        view2 = view2.mParent;
                    } else {
                        view2 = null;
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
            for (i = 0; i < numViewsRequestingLayout; i += MSG_INVALIDATE) {
                view = (View) layoutRequesters.get(i);
                while (view != null && (view.mPrivateFlags & HwPerformance.PERF_EVENT_RAW_REQ) != 0) {
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
            view.mPrivateFlags |= GL10.GL_NEVER;
            this.mWindowAttributesChanged = USE_MT_RENDERER;
            this.mWindowAttributesChangesFlag = 0;
            requestLayout();
        }
    }

    private static int getRootMeasureSpec(int windowSize, int rootDimension) {
        switch (rootDimension) {
            case HwPerformance.REQUEST_PLATFORM_NOTSUPPORT /*-2*/:
                return MeasureSpec.makeMeasureSpec(windowSize, RtlSpacingHelper.UNDEFINED);
            case PGSdk.TYPE_UNKNOW /*-1*/:
                return MeasureSpec.makeMeasureSpec(windowSize, EditorInfo.IME_FLAG_NO_ENTER_ACTION);
            default:
                return MeasureSpec.makeMeasureSpec(rootDimension, EditorInfo.IME_FLAG_NO_ENTER_ACTION);
        }
    }

    public void onHardwarePreDraw(DisplayListCanvas canvas) {
        canvas.translate((float) (-this.mHardwareXOffset), (float) (-this.mHardwareYOffset));
    }

    public void onHardwarePostDraw(DisplayListCanvas canvas) {
        drawAccessibilityFocusedDrawableIfNeeded(canvas);
        for (int i = this.mWindowCallbacks.size() - 1; i >= 0; i--) {
            ((WindowCallbacks) this.mWindowCallbacks.get(i)).onPostDraw(canvas);
        }
    }

    void outputDisplayList(View view) {
        view.mRenderNode.output();
        if (this.mAttachInfo.mHardwareRenderer != null) {
            this.mAttachInfo.mHardwareRenderer.serializeDisplayListTree();
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
        this.mFpsNumFrames += MSG_INVALIDATE;
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

    private void performDraw() {
        boolean fullRedrawNeeded;
        long preDrawTime;
        int count;
        int i;
        SurfaceHolder.Callback[] callbacks;
        int length;
        int i2;
        SurfaceHolder.Callback c;
        if (!mSupportAod) {
            if (this.mAttachInfo.mDisplayState == MSG_INVALIDATE) {
            }
            fullRedrawNeeded = this.mFullRedrawNeeded;
            this.mFullRedrawNeeded = LOCAL_LOGV;
            this.mIsDrawing = USE_MT_RENDERER;
            preDrawTime = System.nanoTime();
            Trace.traceBegin(8, "draw");
            if (fullRedrawNeeded) {
            }
            Trace.traceBegin(8, "draw " + (fullRedrawNeeded ? "Rect(full)" : this.mDirty));
            draw(fullRedrawNeeded);
            this.mIsDrawing = LOCAL_LOGV;
            if (Jlog.isPerfTest()) {
                this.mSoftDrawTime = System.nanoTime() - preDrawTime;
            }
            Trace.traceEnd(8);
            Trace.traceEnd(8);
            if (this.mAttachInfo.mPendingAnimatingRenderNodes != null) {
                count = this.mAttachInfo.mPendingAnimatingRenderNodes.size();
                for (i = 0; i < count; i += MSG_INVALIDATE) {
                    ((RenderNode) this.mAttachInfo.mPendingAnimatingRenderNodes.get(i)).endAllAnimators();
                }
                this.mAttachInfo.mPendingAnimatingRenderNodes.clear();
            }
            if (this.mReportNextDraw) {
                this.mReportNextDraw = LOCAL_LOGV;
                if (this.mWindowDrawCountDown != null) {
                    this.mWindowDrawCountDown.await();
                    this.mWindowDrawCountDown = null;
                }
                if (this.mAttachInfo.mHardwareRenderer != null) {
                    this.mAttachInfo.mHardwareRenderer.fence();
                    this.mAttachInfo.mHardwareRenderer.setStopped(this.mStopped);
                }
                this.mSurfaceHolderCallback.surfaceRedrawNeeded(this.mSurfaceHolder);
                callbacks = this.mSurfaceHolder.getCallbacks();
                if (callbacks != null) {
                    length = callbacks.length;
                    for (i2 = 0; i2 < length; i2 += MSG_INVALIDATE) {
                        c = callbacks[i2];
                        if (!(c instanceof Callback2)) {
                            ((Callback2) c).surfaceRedrawNeeded(this.mSurfaceHolder);
                        }
                    }
                }
                this.mWindowSession.finishDrawing(this.mWindow);
            }
            Jlog.i(JlogConstants.JLID_DISPLAY_VIEW_DRAW, "Soft,#DR:" + ((this.mSoftDrawTime + REDUNDANT) / TimeUtils.NANOS_PER_MS));
        } else if (!(this.mAttachInfo.mDisplayState == MSG_INVALIDATE || this.mAttachInfo.mDisplayState == MSG_RESIZED)) {
            if (this.mStopped) {
            }
            fullRedrawNeeded = this.mFullRedrawNeeded;
            this.mFullRedrawNeeded = LOCAL_LOGV;
            this.mIsDrawing = USE_MT_RENDERER;
            preDrawTime = System.nanoTime();
            Trace.traceBegin(8, "draw");
            Trace.traceBegin(8, "draw " + (fullRedrawNeeded ? "Rect(full)" : this.mDirty));
            draw(fullRedrawNeeded);
            this.mIsDrawing = LOCAL_LOGV;
            if (Jlog.isPerfTest()) {
                this.mSoftDrawTime = System.nanoTime() - preDrawTime;
            }
            Trace.traceEnd(8);
            Trace.traceEnd(8);
            if (this.mAttachInfo.mPendingAnimatingRenderNodes != null) {
                count = this.mAttachInfo.mPendingAnimatingRenderNodes.size();
                for (i = 0; i < count; i += MSG_INVALIDATE) {
                    ((RenderNode) this.mAttachInfo.mPendingAnimatingRenderNodes.get(i)).endAllAnimators();
                }
                this.mAttachInfo.mPendingAnimatingRenderNodes.clear();
            }
            if (this.mReportNextDraw) {
                this.mReportNextDraw = LOCAL_LOGV;
                if (this.mWindowDrawCountDown != null) {
                    try {
                        this.mWindowDrawCountDown.await();
                    } catch (InterruptedException e) {
                        Log.e(this.mTag, "Window redraw count down interruped!");
                    }
                    this.mWindowDrawCountDown = null;
                }
                if (this.mAttachInfo.mHardwareRenderer != null) {
                    this.mAttachInfo.mHardwareRenderer.fence();
                    this.mAttachInfo.mHardwareRenderer.setStopped(this.mStopped);
                }
                if (this.mSurfaceHolder != null && this.mSurface.isValid()) {
                    this.mSurfaceHolderCallback.surfaceRedrawNeeded(this.mSurfaceHolder);
                    callbacks = this.mSurfaceHolder.getCallbacks();
                    if (callbacks != null) {
                        length = callbacks.length;
                        for (i2 = 0; i2 < length; i2 += MSG_INVALIDATE) {
                            c = callbacks[i2];
                            if (!(c instanceof Callback2)) {
                                ((Callback2) c).surfaceRedrawNeeded(this.mSurfaceHolder);
                            }
                        }
                    }
                }
                try {
                    this.mWindowSession.finishDrawing(this.mWindow);
                } catch (RemoteException e2) {
                }
            }
            if (Jlog.isPerfTest() && this.mAttachInfo.mHardwareRenderer == null) {
                Jlog.i(JlogConstants.JLID_DISPLAY_VIEW_DRAW, "Soft,#DR:" + ((this.mSoftDrawTime + REDUNDANT) / TimeUtils.NANOS_PER_MS));
            }
        }
        if (!this.mReportNextDraw) {
            return;
        }
        fullRedrawNeeded = this.mFullRedrawNeeded;
        this.mFullRedrawNeeded = LOCAL_LOGV;
        this.mIsDrawing = USE_MT_RENDERER;
        preDrawTime = System.nanoTime();
        Trace.traceBegin(8, "draw");
        if (fullRedrawNeeded) {
        }
        Trace.traceBegin(8, "draw " + (fullRedrawNeeded ? "Rect(full)" : this.mDirty));
        try {
            draw(fullRedrawNeeded);
            this.mIsDrawing = LOCAL_LOGV;
            if (Jlog.isPerfTest()) {
                this.mSoftDrawTime = System.nanoTime() - preDrawTime;
            }
            Trace.traceEnd(8);
            Trace.traceEnd(8);
            if (this.mAttachInfo.mPendingAnimatingRenderNodes != null) {
                count = this.mAttachInfo.mPendingAnimatingRenderNodes.size();
                for (i = 0; i < count; i += MSG_INVALIDATE) {
                    ((RenderNode) this.mAttachInfo.mPendingAnimatingRenderNodes.get(i)).endAllAnimators();
                }
                this.mAttachInfo.mPendingAnimatingRenderNodes.clear();
            }
            if (this.mReportNextDraw) {
                this.mReportNextDraw = LOCAL_LOGV;
                if (this.mWindowDrawCountDown != null) {
                    this.mWindowDrawCountDown.await();
                    this.mWindowDrawCountDown = null;
                }
                if (this.mAttachInfo.mHardwareRenderer != null) {
                    this.mAttachInfo.mHardwareRenderer.fence();
                    this.mAttachInfo.mHardwareRenderer.setStopped(this.mStopped);
                }
                this.mSurfaceHolderCallback.surfaceRedrawNeeded(this.mSurfaceHolder);
                callbacks = this.mSurfaceHolder.getCallbacks();
                if (callbacks != null) {
                    length = callbacks.length;
                    for (i2 = 0; i2 < length; i2 += MSG_INVALIDATE) {
                        c = callbacks[i2];
                        if (!(c instanceof Callback2)) {
                            ((Callback2) c).surfaceRedrawNeeded(this.mSurfaceHolder);
                        }
                    }
                }
                this.mWindowSession.finishDrawing(this.mWindow);
            }
            Jlog.i(JlogConstants.JLID_DISPLAY_VIEW_DRAW, "Soft,#DR:" + ((this.mSoftDrawTime + REDUNDANT) / TimeUtils.NANOS_PER_MS));
        } catch (Throwable th) {
            this.mIsDrawing = LOCAL_LOGV;
            if (Jlog.isPerfTest()) {
                this.mSoftDrawTime = System.nanoTime() - preDrawTime;
            }
            Trace.traceEnd(8);
            Trace.traceEnd(8);
        }
    }

    private void draw(boolean fullRedrawNeeded) {
        Surface surface = this.mSurface;
        if (surface.isValid()) {
            int curScrollY;
            if (!sFirstDrawComplete) {
                synchronized (sFirstDrawHandlers) {
                    sFirstDrawComplete = USE_MT_RENDERER;
                    int count = sFirstDrawHandlers.size();
                    for (int i = 0; i < count; i += MSG_INVALIDATE) {
                        this.mHandler.post((Runnable) sFirstDrawHandlers.get(i));
                    }
                }
            }
            scrollToRectOrFocus(null, LOCAL_LOGV);
            if (this.mAttachInfo.mViewScrollChanged) {
                this.mAttachInfo.mViewScrollChanged = LOCAL_LOGV;
                this.mAttachInfo.mTreeObserver.dispatchOnScrollChanged();
            }
            boolean computeScrollOffset = this.mScroller != null ? this.mScroller.computeScrollOffset() : LOCAL_LOGV;
            if (computeScrollOffset) {
                curScrollY = this.mScroller.getCurrY();
            } else {
                curScrollY = this.mScrollY;
            }
            if (this.mCurScrollY != curScrollY) {
                this.mCurScrollY = curScrollY;
                fullRedrawNeeded = USE_MT_RENDERER;
                if (this.mView instanceof RootViewSurfaceTaker) {
                    ((RootViewSurfaceTaker) this.mView).onRootViewScrollYChanged(this.mCurScrollY);
                }
            }
            float appScale = this.mAttachInfo.mApplicationScale;
            boolean scalingRequired = this.mAttachInfo.mScalingRequired;
            Rect dirty = this.mDirty;
            if (this.mSurfaceHolder != null) {
                dirty.setEmpty();
                if (computeScrollOffset && this.mScroller != null) {
                    this.mScroller.abortAnimation();
                }
                return;
            }
            if (fullRedrawNeeded) {
                this.mAttachInfo.mIgnoreDirtyState = USE_MT_RENDERER;
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
            boolean accessibilityFocusDirty = LOCAL_LOGV;
            Drawable drawable = this.mAttachInfo.mAccessibilityFocusDrawable;
            if (drawable != null) {
                Rect bounds = this.mAttachInfo.mTmpInvalRect;
                if (!getAccessibilityFocusedRect(bounds)) {
                    bounds.setEmpty();
                }
                if (!bounds.equals(drawable.getBounds())) {
                    accessibilityFocusDirty = USE_MT_RENDERER;
                }
            }
            this.mAttachInfo.mDrawingTime = this.mChoreographer.getFrameTimeNanos() / TimeUtils.NANOS_PER_MS;
            if (!dirty.isEmpty() || this.mIsAnimating || accessibilityFocusDirty) {
                if (enableRefreshDirty()) {
                    setRefreshDirty(dirty);
                }
                if (this.mAttachInfo.mHardwareRenderer != null && this.mAttachInfo.mHardwareRenderer.isEnabled()) {
                    boolean z = !accessibilityFocusDirty ? this.mInvalidateRootRequested : USE_MT_RENDERER;
                    this.mInvalidateRootRequested = LOCAL_LOGV;
                    this.mIsAnimating = LOCAL_LOGV;
                    if (!(this.mHardwareYOffset == yOffset && this.mHardwareXOffset == xOffset)) {
                        this.mHardwareYOffset = yOffset;
                        this.mHardwareXOffset = xOffset;
                        z = USE_MT_RENDERER;
                    }
                    if (z) {
                        this.mAttachInfo.mHardwareRenderer.invalidateRoot();
                    }
                    dirty.setEmpty();
                    boolean updated = updateContentDrawBounds();
                    if (this.mReportNextDraw) {
                        this.mAttachInfo.mHardwareRenderer.setStopped(LOCAL_LOGV);
                    }
                    if (updated) {
                        requestDrawWindow();
                    }
                    this.mAttachInfo.mHardwareRenderer.draw(this.mView, this.mAttachInfo, this);
                } else if (this.mAttachInfo.mHardwareRenderer != null && !this.mAttachInfo.mHardwareRenderer.isEnabled() && this.mAttachInfo.mHardwareRenderer.isRequested()) {
                    try {
                        this.mAttachInfo.mHardwareRenderer.initializeIfNeeded(this.mWidth, this.mHeight, this.mAttachInfo, this.mSurface, surfaceInsets);
                        this.mFullRedrawNeeded = USE_MT_RENDERER;
                        scheduleTraversals();
                        return;
                    } catch (OutOfResourcesException e) {
                        handleOutOfResourcesException(e);
                        return;
                    }
                } else if (!drawSoftware(surface, this.mAttachInfo, xOffset, yOffset, scalingRequired, dirty)) {
                    return;
                }
            }
            if (computeScrollOffset) {
                this.mFullRedrawNeeded = USE_MT_RENDERER;
                scheduleTraversals();
            }
        }
    }

    private boolean enableRefreshDirty() {
        return SystemProperties.getInt("sys.refresh.dirty", 0) > 0 ? USE_MT_RENDERER : LOCAL_LOGV;
    }

    private boolean debugRefreshDirty() {
        return SystemProperties.getInt("debug.refresh.dirty", 0) > 0 ? USE_MT_RENDERER : LOCAL_LOGV;
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
                Log.i(TAG, "@@@setRefreshDirty-dirty=[" + dirty.left + PtmLog.PAIRE_DELIMETER + dirty.top + PtmLog.PAIRE_DELIMETER + dirty.right + PtmLog.PAIRE_DELIMETER + dirty.bottom + "]-w:" + dirty.width() + ", h:" + dirty.height());
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

    private boolean drawSoftware(Surface surface, AttachInfo attachInfo, int xoff, int yoff, boolean scalingRequired, Rect dirty) {
        try {
            View view;
            int left = dirty.left;
            int top = dirty.top;
            int right = dirty.right;
            int bottom = dirty.bottom;
            Canvas canvas = this.mSurface.lockCanvas(dirty);
            if (left == dirty.left && top == dirty.top) {
                if (right == dirty.right) {
                    if (bottom != dirty.bottom) {
                    }
                    canvas.setDensity(this.mDensity);
                    if (canvas.isOpaque() && yoff == 0) {
                        if (xoff != 0) {
                        }
                        dirty.setEmpty();
                        this.mIsAnimating = LOCAL_LOGV;
                        view = this.mView;
                        view.mPrivateFlags |= 32;
                        canvas.translate((float) (-xoff), (float) (-yoff));
                        if (this.mRogTranslater == null) {
                            if (this.mTranslator != null) {
                                this.mTranslator.translateCanvas(canvas);
                            }
                        }
                        canvas.setScreenDensity(scalingRequired ? this.mNoncompatDensity : 0);
                        attachInfo.mSetIgnoreDirtyState = LOCAL_LOGV;
                        this.mView.draw(canvas);
                        drawAccessibilityFocusedDrawableIfNeeded(canvas);
                        if (!attachInfo.mSetIgnoreDirtyState) {
                            attachInfo.mIgnoreDirtyState = LOCAL_LOGV;
                        }
                        surface.unlockCanvasAndPost(canvas);
                        return USE_MT_RENDERER;
                    }
                    canvas.drawColor(0, Mode.CLEAR);
                    dirty.setEmpty();
                    this.mIsAnimating = LOCAL_LOGV;
                    view = this.mView;
                    view.mPrivateFlags |= 32;
                    canvas.translate((float) (-xoff), (float) (-yoff));
                    if (this.mRogTranslater == null) {
                        if (this.mTranslator != null) {
                            this.mTranslator.translateCanvas(canvas);
                        }
                    }
                    if (scalingRequired) {
                    }
                    canvas.setScreenDensity(scalingRequired ? this.mNoncompatDensity : 0);
                    attachInfo.mSetIgnoreDirtyState = LOCAL_LOGV;
                    this.mView.draw(canvas);
                    drawAccessibilityFocusedDrawableIfNeeded(canvas);
                    if (attachInfo.mSetIgnoreDirtyState) {
                        attachInfo.mIgnoreDirtyState = LOCAL_LOGV;
                    }
                    surface.unlockCanvasAndPost(canvas);
                    return USE_MT_RENDERER;
                }
            }
            attachInfo.mIgnoreDirtyState = USE_MT_RENDERER;
            canvas.setDensity(this.mDensity);
            try {
                if (xoff != 0) {
                    canvas.drawColor(0, Mode.CLEAR);
                }
                dirty.setEmpty();
                this.mIsAnimating = LOCAL_LOGV;
                view = this.mView;
                view.mPrivateFlags |= 32;
                canvas.translate((float) (-xoff), (float) (-yoff));
                if (this.mRogTranslater == null) {
                    if (this.mTranslator != null) {
                        this.mTranslator.translateCanvas(canvas);
                    }
                }
                if (scalingRequired) {
                }
                canvas.setScreenDensity(scalingRequired ? this.mNoncompatDensity : 0);
                attachInfo.mSetIgnoreDirtyState = LOCAL_LOGV;
                this.mView.draw(canvas);
                drawAccessibilityFocusedDrawableIfNeeded(canvas);
                if (attachInfo.mSetIgnoreDirtyState) {
                    attachInfo.mIgnoreDirtyState = LOCAL_LOGV;
                }
                try {
                    surface.unlockCanvasAndPost(canvas);
                    return USE_MT_RENDERER;
                } catch (IllegalArgumentException e) {
                    Log.e(this.mTag, "Could not unlock surface", e);
                    this.mLayoutRequested = USE_MT_RENDERER;
                    return LOCAL_LOGV;
                }
            } catch (Throwable th) {
                try {
                    surface.unlockCanvasAndPost(canvas);
                } catch (IllegalArgumentException e2) {
                    Log.e(this.mTag, "Could not unlock surface", e2);
                    this.mLayoutRequested = USE_MT_RENDERER;
                    return LOCAL_LOGV;
                }
            }
        } catch (OutOfResourcesException e3) {
            handleOutOfResourcesException(e3);
            return LOCAL_LOGV;
        } catch (IllegalArgumentException e22) {
            Log.e(this.mTag, "Could not lock surface", e22);
            this.mLayoutRequested = USE_MT_RENDERER;
            return LOCAL_LOGV;
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
        boolean z = LOCAL_LOGV;
        AccessibilityManager manager = AccessibilityManager.getInstance(this.mView.mContext);
        if (!manager.isEnabled() || !manager.isTouchExplorationEnabled()) {
            return LOCAL_LOGV;
        }
        View host = this.mAccessibilityFocusedHost;
        if (host == null || host.mAttachInfo == null) {
            return LOCAL_LOGV;
        }
        if (host.getAccessibilityNodeProvider() == null) {
            host.getBoundsOnScreen(bounds, USE_MT_RENDERER);
        } else if (this.mAccessibilityFocusedVirtualView == null) {
            return LOCAL_LOGV;
        } else {
            this.mAccessibilityFocusedVirtualView.getBoundsInScreen(bounds);
        }
        AttachInfo attachInfo = this.mAttachInfo;
        bounds.offset(0, attachInfo.mViewRootImpl.mScrollY);
        bounds.offset(-attachInfo.mWindowLeft, -attachInfo.mWindowTop);
        if (!bounds.intersect(0, 0, attachInfo.mViewRootImpl.mWidth, attachInfo.mViewRootImpl.mHeight)) {
            bounds.setEmpty();
        }
        if (!bounds.isEmpty()) {
            z = USE_MT_RENDERER;
        }
        return z;
    }

    private Drawable getAccessibilityFocusedDrawable() {
        if (this.mAttachInfo.mAccessibilityFocusDrawable == null) {
            TypedValue value = new TypedValue();
            if (this.mView.mContext.getTheme().resolveAttribute(R.attr.accessibilityFocusedDrawable, value, USE_MT_RENDERER)) {
                this.mAttachInfo.mAccessibilityFocusDrawable = this.mView.mContext.getDrawable(value.resourceId);
            }
        }
        return this.mAttachInfo.mAccessibilityFocusDrawable;
    }

    public void requestInvalidateRootRenderNode() {
        this.mInvalidateRootRequested = USE_MT_RENDERER;
    }

    boolean scrollToRectOrFocus(Rect rectangle, boolean immediate) {
        Rect ci = this.mAttachInfo.mContentInsets;
        Rect vi = this.mAttachInfo.mVisibleInsets;
        int scrollY = 0;
        boolean handled = LOCAL_LOGV;
        if (vi.left > ci.left || vi.top > ci.top || vi.right > ci.right || vi.bottom > ci.bottom) {
            scrollY = this.mScrollY;
            View focus = this.mView.findFocus();
            if (focus == null) {
                return LOCAL_LOGV;
            }
            View lastScrolledFocus;
            if (this.mLastScrolledFocus != null) {
                lastScrolledFocus = (View) this.mLastScrolledFocus.get();
            } else {
                lastScrolledFocus = null;
            }
            if (focus != lastScrolledFocus) {
                rectangle = null;
            }
            if (focus != lastScrolledFocus || this.mScrollMayChange || rectangle != null) {
                this.mLastScrolledFocus = new WeakReference(focus);
                this.mScrollMayChange = LOCAL_LOGV;
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
                        handled = USE_MT_RENDERER;
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
                provider.performAction(AccessibilityNodeInfo.getVirtualDescendantId(focusNode.getSourceNodeId()), LogPower.START_CHG_ROTATION, null);
            }
            focusNode.recycle();
        }
        if (this.mAccessibilityFocusedHost != null) {
            this.mAccessibilityFocusedHost.clearAccessibilityFocusNoCallbacks(64);
        }
        this.mAccessibilityFocusedHost = view;
        this.mAccessibilityFocusedVirtualView = node;
        if (this.mAttachInfo.mHardwareRenderer != null) {
            this.mAttachInfo.mHardwareRenderer.invalidateRoot();
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
            if ((focused instanceof ViewGroup) && ((ViewGroup) focused).getDescendantFocusability() == Protocol.BASE_DATA_CONNECTION && isViewDescendantOf(v, focused)) {
                v.requestFocus();
                return;
            }
            return;
        }
        v.requestFocus();
    }

    public void recomputeViewAttributes(View child) {
        checkThread();
        if (this.mView == child) {
            this.mAttachInfo.mRecomputeGlobalAttributes = USE_MT_RENDERER;
            if (!this.mWillDrawSoon) {
                scheduleTraversals();
            }
        }
    }

    void dispatchDetachedFromWindow() {
        if (!(this.mView == null || this.mView.mAttachInfo == null)) {
            this.mAttachInfo.mTreeObserver.dispatchOnWindowAttachedChange(LOCAL_LOGV);
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

    void updateConfiguration(Configuration config, boolean force) {
        CompatibilityInfo ci = this.mDisplay.getDisplayAdjustments().getCompatibilityInfo();
        if (!ci.equals(CompatibilityInfo.DEFAULT_COMPATIBILITY_INFO)) {
            Configuration config2 = new Configuration(config);
            ci.applyToConfiguration(this.mNoncompatDensity, config2);
            ci.applyToConfigurationExt(null, this.mNoncompatDensity, config2);
            config = config2;
        }
        synchronized (sConfigCallbacks) {
            for (int i = sConfigCallbacks.size() - 1; i >= 0; i--) {
                ((ComponentCallbacks) sConfigCallbacks.get(i)).onConfigurationChanged(config);
            }
        }
        if (this.mView != null) {
            Resources localResources = this.mView.getResources();
            config = localResources.getConfiguration();
            if (force || this.mLastConfiguration.diff(config) != 0) {
                this.mDisplay = ResourcesManager.getInstance().getAdjustedDisplay(this.mDisplay.getDisplayId(), localResources.getDisplayAdjustments());
                int lastLayoutDirection = this.mLastConfiguration.getLayoutDirection();
                int currentLayoutDirection = config.getLayoutDirection();
                this.mLastConfiguration.setTo(config);
                if (lastLayoutDirection != currentLayoutDirection && this.mViewLayoutDirectionInitial == MSG_INVALIDATE_RECT) {
                    this.mView.setLayoutDirection(currentLayoutDirection);
                }
                this.mView.dispatchConfigurationChanged(config);
            }
        }
    }

    public static boolean isViewDescendantOf(View child, View parent) {
        if (child == parent) {
            return USE_MT_RENDERER;
        }
        ViewParent theParent = child.getParent();
        return theParent instanceof ViewGroup ? isViewDescendantOf((View) theParent, parent) : LOCAL_LOGV;
    }

    private static void forceLayout(View view) {
        view.forceLayout();
        if (view instanceof ViewGroup) {
            ViewGroup group = (ViewGroup) view;
            int count = group.getChildCount();
            for (int i = 0; i < count; i += MSG_INVALIDATE) {
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
        boolean bOntime = delaytime < ((long) ONTIME_REF) ? USE_MT_RENDERER : LOCAL_LOGV;
        jank_newcheckSkippedFrame(now, frameTime);
        boolean bContinuousUpdate = bOntime ? frameTime - lastFrameDoneTime < VSYNC_SPAN ? USE_MT_RENDERER : LOCAL_LOGV : lastFrameDoneTime > frameTime ? USE_MT_RENDERER : LOCAL_LOGV;
        if (!bOntime || now - frameTime >= 14166667) {
            if (scroll) {
                if (frameTime - lastFrameDoneTime < ((long) CONTINUOUS_REF)) {
                    bContinuousUpdate = USE_MT_RENDERER;
                }
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
                    this.lastFrameDefer = MSG_INVALIDATE;
                    return;
                } else if (skip != MSG_INVALIDATE) {
                    if (skip > MSG_INVALIDATE) {
                        skip = (skip - 1) - this.lastFrameDefer;
                        this.lastFrameDefer = 0;
                    }
                    if (skip >= MSG_RESIZED_REPORT) {
                        String msg = "#P:" + this.mWindowAttributes.getTitle() + "#SK:" + skip;
                        if (this.mAttachInfo.mHardwareRenderer != null) {
                            msg = msg + "#IP:" + (this.mDeliverInputTime / 10000) + "#DR:" + (this.mAttachInfo.mHardwareRenderer.getJankDrawData(0) / 10000) + "#PRO:" + (this.mAttachInfo.mHardwareRenderer.getJankDrawData(MSG_INVALIDATE) / 10000) + "#EX:" + (this.mAttachInfo.mHardwareRenderer.getJankDrawData(MSG_INVALIDATE_RECT) / 10000) + "#TRA:" + (this.mAttachInfo.mHardwareRenderer.getJankDrawData(MSG_DIE) / 10000);
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
            return LOCAL_LOGV;
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
            return LOCAL_LOGV;
        }
        this.mAttachInfo.mInTouchMode = inTouchMode;
        this.mAttachInfo.mTreeObserver.dispatchOnTouchModeChanged(inTouchMode);
        return inTouchMode ? enterTouchMode() : leaveTouchMode();
    }

    private boolean enterTouchMode() {
        if (this.mView != null && this.mView.hasFocus()) {
            View focused = this.mView.findFocus();
            if (!(focused == null || focused.isFocusableInTouchMode())) {
                ViewGroup ancestorToTakeFocus = findAncestorToTakeFocusInTouchMode(focused);
                if (ancestorToTakeFocus != null) {
                    return ancestorToTakeFocus.requestFocus();
                }
                focused.clearFocusInternal(null, USE_MT_RENDERER, LOCAL_LOGV);
                return USE_MT_RENDERER;
            }
        }
        return LOCAL_LOGV;
    }

    private static ViewGroup findAncestorToTakeFocusInTouchMode(View focused) {
        ViewParent parent = focused.getParent();
        while (parent instanceof ViewGroup) {
            ViewGroup vgParent = (ViewGroup) parent;
            if (vgParent.getDescendantFocusability() == Protocol.BASE_DATA_CONNECTION && vgParent.isFocusableInTouchMode()) {
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
                if (!((focusedView instanceof ViewGroup) && ((ViewGroup) focusedView).getDescendantFocusability() == Protocol.BASE_DATA_CONNECTION)) {
                    return LOCAL_LOGV;
                }
            }
            View focused = focusSearch(null, LogPower.END_CHG_ROTATION);
            if (focused != null) {
                return focused.requestFocus(LogPower.END_CHG_ROTATION);
            }
        }
        return LOCAL_LOGV;
    }

    private void resetPointerIcon(MotionEvent event) {
        this.mPointerIconType = MSG_INVALIDATE;
        updatePointerIcon(event);
    }

    private boolean updatePointerIcon(MotionEvent event) {
        float x = event.getX(0);
        float y = event.getY(0);
        if (this.mView == null) {
            Slog.d(this.mTag, "updatePointerIcon called after view was removed");
            return LOCAL_LOGV;
        } else if (x < 0.0f || x >= ((float) this.mView.getWidth()) || y < 0.0f || y >= ((float) this.mView.getHeight())) {
            Slog.d(this.mTag, "updatePointerIcon called with position out of bounds");
            return LOCAL_LOGV;
        } else {
            PointerIcon pointerIcon = this.mView.onResolvePointerIcon(event, 0);
            int pointerType = pointerIcon != null ? pointerIcon.getType() : RILConstants.RIL_UNSOL_RESPONSE_RADIO_STATE_CHANGED;
            if (this.mPointerIconType != pointerType) {
                this.mPointerIconType = pointerType;
                if (this.mPointerIconType != -1) {
                    this.mCustomPointerIcon = null;
                    InputManager.getInstance().setPointerIconType(pointerType);
                    return USE_MT_RENDERER;
                }
            }
            if (this.mPointerIconType == -1 && !pointerIcon.equals(this.mCustomPointerIcon)) {
                this.mCustomPointerIcon = pointerIcon;
                InputManager.getInstance().setCustomPointerIcon(this.mCustomPointerIcon);
            }
            return USE_MT_RENDERER;
        }
    }

    private static boolean isNavigationKey(KeyEvent keyEvent) {
        switch (keyEvent.getKeyCode()) {
            case MSG_PROCESS_INPUT_EVENTS /*19*/:
            case HwPerformance.PERF_TAG_TASK_FORK_ON_B_CLUSTER /*20*/:
            case MSG_CLEAR_ACCESSIBILITY_FOCUS_HOST /*21*/:
            case MSG_INVALIDATE_WORLD /*22*/:
            case MSG_WINDOW_MOVED /*23*/:
            case StatisticalConstant.TYPE_WIFI_END /*61*/:
            case RILConstants.RIL_REQUEST_SET_SUPP_SVC_NOTIFICATION /*62*/:
            case RILConstants.RIL_REQUEST_QUERY_AVAILABLE_BAND_MODE /*66*/:
            case RILConstants.RIL_REQUEST_CDMA_GET_BROADCAST_CONFIG /*92*/:
            case RILConstants.RIL_REQUEST_CDMA_SET_BROADCAST_CONFIG /*93*/:
            case LogPower.NOTIFICATION_ENQUEUE /*122*/:
            case LogPower.NOTIFICATION_CANCEL /*123*/:
                return USE_MT_RENDERER;
            default:
                return LOCAL_LOGV;
        }
    }

    private static boolean isTypingKey(KeyEvent keyEvent) {
        return keyEvent.getUnicodeChar() > 0 ? USE_MT_RENDERER : LOCAL_LOGV;
    }

    private boolean checkForLeavingTouchModeAndConsume(KeyEvent event) {
        if (!this.mAttachInfo.mInTouchMode) {
            return LOCAL_LOGV;
        }
        int action = event.getAction();
        if ((action != 0 && action != MSG_INVALIDATE_RECT) || (event.getFlags() & MSG_RESIZED) != 0) {
            return LOCAL_LOGV;
        }
        if (isNavigationKey(event)) {
            return ensureTouchMode(LOCAL_LOGV);
        }
        if (!isTypingKey(event)) {
            return LOCAL_LOGV;
        }
        ensureTouchMode(LOCAL_LOGV);
        return LOCAL_LOGV;
    }

    void setLocalDragState(Object obj) {
        this.mLocalDragState = obj;
    }

    private void handleDragEvent(DragEvent event) {
        if (this.mView != null && this.mAdded) {
            int what = event.mAction;
            if (what == MSG_WINDOW_FOCUS_CHANGED) {
                this.mView.dispatchDragEvent(event);
            } else {
                if (what == MSG_INVALIDATE) {
                    this.mCurrentDragView = null;
                    this.mDragDescription = event.mClipDescription;
                } else {
                    event.mClipDescription = this.mDragDescription;
                }
                if (what == MSG_INVALIDATE_RECT || what == MSG_DIE) {
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
                boolean result = this.mView.dispatchDragEvent(event);
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
                if (what == MSG_DIE) {
                    this.mDragDescription = null;
                    try {
                        Log.i(this.mTag, "Reporting drop result: " + result);
                        this.mWindowSession.reportDropResult(this.mWindow, result);
                    } catch (RemoteException e2) {
                        Log.e(this.mTag, "Unable to report drop result");
                    }
                }
                if (what == MSG_RESIZED) {
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
            this.mAttachInfo.mForceReportNewAttributes = USE_MT_RENDERER;
            scheduleTraversals();
        }
        if (this.mView != null) {
            if (args.localChanges != 0) {
                this.mView.updateLocalSystemUiVisibility(args.localValue, args.localChanges);
            }
            int visibility = args.globalVisibility & MSG_DISPATCH_INPUT_EVENT;
            if (visibility != this.mAttachInfo.mGlobalSystemUiVisibility) {
                this.mAttachInfo.mGlobalSystemUiVisibility = visibility;
                this.mView.dispatchSystemUiVisibilityChanged(visibility);
            }
        }
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

    public void setDragFocus(View newDragTarget) {
        if (this.mCurrentDragView != newDragTarget) {
            this.mCurrentDragView = newDragTarget;
        }
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
        boolean restore = LOCAL_LOGV;
        if (params != null && this.mRogTranslater != null) {
            restore = USE_MT_RENDERER;
            params.backup();
            this.mRogTranslater.translateWindowLayout(params);
        } else if (!(params == null || this.mTranslator == null)) {
            restore = USE_MT_RENDERER;
            params.backup();
            this.mTranslator.translateWindowLayout(params);
        }
        if (params != null) {
            this.mPendingConfiguration.seq = 0;
        } else {
            this.mPendingConfiguration.seq = 0;
        }
        if (!(params == null || this.mOrigWindowType == params.type || this.mTargetSdkVersion >= MSG_CLOSE_SYSTEM_DIALOGS)) {
            Slog.w(this.mTag, "Window type can not be changed after the window is added; ignoring change of " + this.mView);
            params.type = this.mOrigWindowType;
        }
        int relayoutResult = this.mWindowSession.relayout(this.mWindow, this.mSeq, params, (int) ((((float) this.mView.getMeasuredWidth()) * appScale) + 0.5f), (int) ((((float) this.mView.getMeasuredHeight()) * appScale) + 0.5f), viewVisibility, insetsPending ? MSG_INVALIDATE : 0, this.mWinFrame, this.mPendingOverscanInsets, this.mPendingContentInsets, this.mPendingVisibleInsets, this.mPendingStableInsets, this.mPendingOutsets, this.mPendingBackDropFrame, this.mPendingConfiguration, this.mSurface);
        this.mPendingAlwaysConsumeNavBar = (relayoutResult & 64) != 0 ? USE_MT_RENDERER : LOCAL_LOGV;
        if (restore) {
            params.restore();
        }
        if (this.mRogTranslater == null && this.mTranslator != null) {
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
                case HwCfgFilePolicy.GLOBAL /*0*/:
                    audioManager.playSoundEffect(0);
                    return;
                case MSG_INVALIDATE /*1*/:
                    audioManager.playSoundEffect(MSG_DIE);
                    return;
                case MSG_INVALIDATE_RECT /*2*/:
                    audioManager.playSoundEffect(MSG_INVALIDATE);
                    return;
                case MSG_DIE /*3*/:
                    audioManager.playSoundEffect(MSG_RESIZED);
                    return;
                case MSG_RESIZED /*4*/:
                    audioManager.playSoundEffect(MSG_INVALIDATE_RECT);
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
            return LOCAL_LOGV;
        }
    }

    public View focusSearch(View focused, int direction) {
        checkThread();
        if (this.mView instanceof ViewGroup) {
            return FocusFinder.getInstance().findNextFocus((ViewGroup) this.mView, focused, direction);
        }
        return null;
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
                for (int i = 0; i < N; i += MSG_INVALIDATE) {
                    dumpViewHierarchy(prefix, writer, grp.getChildAt(i));
                }
            }
        }
    }

    public void dumpGfxInfo(int[] info) {
        info[MSG_INVALIDATE] = 0;
        info[0] = 0;
        if (this.mView != null) {
            getGfxInfo(this.mView, info);
        }
    }

    private static void getGfxInfo(View view, int[] info) {
        RenderNode renderNode = view.mRenderNode;
        info[0] = info[0] + MSG_INVALIDATE;
        if (renderNode != null) {
            info[MSG_INVALIDATE] = info[MSG_INVALIDATE] + renderNode.getDebugSize();
        }
        if (view instanceof ViewGroup) {
            ViewGroup group = (ViewGroup) view;
            int count = group.getChildCount();
            for (int i = 0; i < count; i += MSG_INVALIDATE) {
                getGfxInfo(group.getChildAt(i), info);
            }
        }
    }

    boolean die(boolean immediate) {
        if (!immediate || this.mIsInTraversal) {
            if (this.mIsDrawing) {
                Log.e(this.mTag, "Attempting to destroy the window while drawing!\n  window=" + this + ", title=" + this.mWindowAttributes.getTitle());
            } else {
                destroyHardwareRenderer();
            }
            this.mHandler.sendEmptyMessage(MSG_DIE);
            return USE_MT_RENDERER;
        }
        doDie();
        return LOCAL_LOGV;
    }

    void doDie() {
        boolean ignoreCheckThread = LOCAL_LOGV;
        if (this.mView != null) {
            ignoreCheckThread = this.mView.isTouchableInOtherThread();
        }
        if (ignoreCheckThread) {
            Log.w(this.mTag, "doDie in " + this + ",CREATE IN " + this.mThread + ",DIE IN " + Thread.currentThread());
        } else {
            checkThread();
        }
        synchronized (this) {
            if (this.mRemoved) {
                return;
            }
            this.mRemoved = USE_MT_RENDERER;
            if (this.mAdded) {
                dispatchDetachedFromWindow();
            }
            if (this.mAdded && !this.mFirst) {
                destroyHardwareRenderer();
                if (this.mView != null) {
                    int viewVisibility = this.mView.getVisibility();
                    boolean viewVisibilityChanged = this.mViewVisibility != viewVisibility ? USE_MT_RENDERER : LOCAL_LOGV;
                    if (this.mWindowAttributesChanged || viewVisibilityChanged) {
                        try {
                            if ((relayoutWindow(this.mWindowAttributes, viewVisibility, LOCAL_LOGV) & MSG_INVALIDATE_RECT) != 0) {
                                this.mWindowSession.finishDrawing(this.mWindow);
                            }
                        } catch (RemoteException e) {
                        }
                    }
                    this.mSurface.release();
                }
            }
            this.mAdded = LOCAL_LOGV;
            WindowManagerGlobal.getInstance().doRemoveView(this);
        }
    }

    public void requestUpdateConfiguration(Configuration config) {
        this.mHandler.sendMessage(this.mHandler.obtainMessage(MSG_UPDATE_CONFIGURATION, config));
    }

    public void loadSystemProperties() {
        this.mHandler.post(new Runnable() {
            public void run() {
                ViewRootImpl.this.mProfileRendering = SystemProperties.getBoolean(ViewRootImpl.PROPERTY_PROFILE_RENDERING, ViewRootImpl.LOCAL_LOGV);
                ViewRootImpl.this.profileRendering(ViewRootImpl.this.mAttachInfo.mHasWindowFocus);
                if (ViewRootImpl.this.mAttachInfo.mHardwareRenderer != null && ViewRootImpl.this.mAttachInfo.mHardwareRenderer.loadSystemProperties()) {
                    ViewRootImpl.this.invalidate();
                }
                boolean layout = SystemProperties.getBoolean(View.DEBUG_LAYOUT_PROPERTY, ViewRootImpl.LOCAL_LOGV);
                if (layout != ViewRootImpl.this.mAttachInfo.mDebugLayout) {
                    ViewRootImpl.this.mAttachInfo.mDebugLayout = layout;
                    if (!ViewRootImpl.this.mHandler.hasMessages(ViewRootImpl.MSG_INVALIDATE_WORLD)) {
                        ViewRootImpl.this.mHandler.sendEmptyMessageDelayed(ViewRootImpl.MSG_INVALIDATE_WORLD, 200);
                    }
                }
            }
        });
    }

    private void destroyHardwareRenderer() {
        ThreadedRenderer hardwareRenderer = this.mAttachInfo.mHardwareRenderer;
        if (hardwareRenderer != null) {
            if (this.mView != null) {
                hardwareRenderer.destroyHardwareResources(this.mView);
            }
            hardwareRenderer.destroy();
            hardwareRenderer.setRequested(LOCAL_LOGV);
            this.mAttachInfo.mHardwareRenderer = null;
            this.mAttachInfo.mHardwareAccelerated = LOCAL_LOGV;
        }
    }

    public void dispatchResized(Rect frame, Rect overscanInsets, Rect contentInsets, Rect visibleInsets, Rect stableInsets, Rect outsets, boolean reportDraw, Configuration newConfig, Rect backDropFrame, boolean forceLayout, boolean alwaysConsumeNavBar) {
        if (this.mDragResizing) {
            boolean fullscreen = frame.equals(backDropFrame);
            synchronized (this.mWindowCallbacks) {
                for (int i = this.mWindowCallbacks.size() - 1; i >= 0; i--) {
                    ((WindowCallbacks) this.mWindowCallbacks.get(i)).onWindowSizeIsChanging(backDropFrame, fullscreen, visibleInsets, stableInsets);
                }
            }
        }
        Message msg = this.mHandler.obtainMessage(reportDraw ? MSG_RESIZED_REPORT : MSG_RESIZED);
        if (this.mTranslator != null) {
            this.mTranslator.translateRectInScreenToAppWindow(frame);
            this.mTranslator.translateRectInScreenToAppWindow(overscanInsets);
            this.mTranslator.translateRectInScreenToAppWindow(contentInsets);
            this.mTranslator.translateRectInScreenToAppWindow(visibleInsets);
        }
        SomeArgs args = SomeArgs.obtain();
        boolean sameProcessCall = Binder.getCallingPid() == Process.myPid() ? USE_MT_RENDERER : LOCAL_LOGV;
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
        if (sameProcessCall && newConfig != null) {
            newConfig = new Configuration(newConfig);
        }
        args.arg4 = newConfig;
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
        args.argi1 = forceLayout ? MSG_INVALIDATE : 0;
        args.argi2 = alwaysConsumeNavBar ? MSG_INVALIDATE : 0;
        msg.obj = args;
        this.mHandler.sendMessage(msg);
    }

    public void dispatchMoved(int newX, int newY) {
        PointF point;
        if (this.mRogTranslater != null) {
            point = new PointF((float) newX, (float) newY);
            newX = (int) (((double) point.x) + 0.5d);
            newY = (int) (((double) point.y) + 0.5d);
        } else if (this.mTranslator != null) {
            point = new PointF((float) newX, (float) newY);
            this.mTranslator.translatePointInScreenToAppWindow(point);
            newX = (int) (((double) point.x) + 0.5d);
            newY = (int) (((double) point.y) + 0.5d);
        }
        this.mHandler.sendMessage(this.mHandler.obtainMessage(MSG_WINDOW_MOVED, newX, newY));
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
        if (this.mQueuedInputEventPoolSize < MAX_QUEUED_INPUT_EVENT_POOL_SIZE) {
            this.mQueuedInputEventPoolSize += MSG_INVALIDATE;
            q.mNext = this.mQueuedInputEventPool;
            this.mQueuedInputEventPool = q;
        }
    }

    void enqueueInputEvent(InputEvent event) {
        enqueueInputEvent(event, null, 0, LOCAL_LOGV);
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
        this.mPendingInputEventCount += MSG_INVALIDATE;
        Trace.traceCounter(4, this.mPendingInputEventQueueLengthCounterName, this.mPendingInputEventCount);
        if (processImmediately) {
            doProcessInputEvents();
        } else {
            scheduleProcessInputEvents();
        }
    }

    private void scheduleProcessInputEvents() {
        if (!this.mProcessInputEventsScheduled) {
            this.mProcessInputEventsScheduled = USE_MT_RENDERER;
            Message msg = this.mHandler.obtainMessage(MSG_PROCESS_INPUT_EVENTS);
            msg.setAsynchronous(USE_MT_RENDERER);
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
            this.mProcessInputEventsScheduled = LOCAL_LOGV;
            this.mHandler.removeMessages(MSG_PROCESS_INPUT_EVENTS);
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
        if (q.mReceiver != null) {
            q.mReceiver.finishInputEvent(q.mEvent, (q.mFlags & MSG_DISPATCH_APP_VISIBILITY) != 0 ? USE_MT_RENDERER : LOCAL_LOGV);
        } else {
            q.mEvent.recycleIfNeededAfterDispatch();
        }
        recycleQueuedInputEvent(q);
    }

    private void adjustInputEventForCompatibility(InputEvent e) {
        if (this.mTargetSdkVersion < MSG_WINDOW_MOVED && (e instanceof MotionEvent)) {
            MotionEvent motion = (MotionEvent) e;
            int buttonState = motion.getButtonState();
            int compatButtonState = (buttonState & 96) >> MSG_RESIZED;
            if (compatButtonState != 0) {
                motion.setButtonState(buttonState | compatButtonState);
            }
        }
    }

    static boolean isTerminalInputEvent(InputEvent event) {
        boolean z = USE_MT_RENDERER;
        if (event instanceof KeyEvent) {
            if (((KeyEvent) event).getAction() != MSG_INVALIDATE) {
                z = LOCAL_LOGV;
            }
            return z;
        }
        int action = ((MotionEvent) event).getAction();
        if (!(action == MSG_INVALIDATE || action == MSG_DIE || action == MAX_QUEUED_INPUT_EVENT_POOL_SIZE)) {
            z = LOCAL_LOGV;
        }
        return z;
    }

    void scheduleConsumeBatchedInput() {
        if (!this.mConsumeBatchedInputScheduled) {
            this.mConsumeBatchedInputScheduled = USE_MT_RENDERER;
            this.mChoreographer.postCallback(0, this.mConsumedBatchedInputRunnable, null);
        }
    }

    void unscheduleConsumeBatchedInput() {
        if (this.mConsumeBatchedInputScheduled) {
            this.mConsumeBatchedInputScheduled = LOCAL_LOGV;
            this.mChoreographer.removeCallbacks(0, this.mConsumedBatchedInputRunnable, null);
        }
    }

    void scheduleConsumeBatchedInputImmediately() {
        if (!this.mConsumeBatchedInputImmediatelyScheduled) {
            unscheduleConsumeBatchedInput();
            this.mConsumeBatchedInputImmediatelyScheduled = USE_MT_RENDERER;
            this.mHandler.post(this.mConsumeBatchedInputImmediatelyRunnable);
        }
    }

    void doConsumeBatchedInput(long frameTimeNanos) {
        if (this.mConsumeBatchedInputScheduled) {
            this.mConsumeBatchedInputScheduled = LOCAL_LOGV;
            if (!(this.mInputEventReceiver == null || !this.mInputEventReceiver.consumeBatchedInputEvents(frameTimeNanos) || frameTimeNanos == -1)) {
                scheduleConsumeBatchedInput();
            }
            doProcessInputEvents();
        }
    }

    public void dispatchInvalidateDelayed(View view, long delayMilliseconds) {
        this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(MSG_INVALIDATE, view), delayMilliseconds);
    }

    public void dispatchInvalidateRectDelayed(InvalidateInfo info, long delayMilliseconds) {
        this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(MSG_INVALIDATE_RECT, info), delayMilliseconds);
    }

    public void dispatchInvalidateOnAnimation(View view) {
        this.mInvalidateOnAnimationRunnable.addView(view);
    }

    public void dispatchInvalidateRectOnAnimation(InvalidateInfo info) {
        this.mInvalidateOnAnimationRunnable.addViewRect(info);
    }

    public void cancelInvalidate(View view) {
        this.mHandler.removeMessages(MSG_INVALIDATE, view);
        this.mHandler.removeMessages(MSG_INVALIDATE_RECT, view);
        this.mInvalidateOnAnimationRunnable.removeView(view);
    }

    public void dispatchInputEvent(InputEvent event) {
        dispatchInputEvent(event, null);
    }

    public void dispatchInputEvent(InputEvent event, InputEventReceiver receiver) {
        SomeArgs args = SomeArgs.obtain();
        args.arg1 = event;
        args.arg2 = receiver;
        Message msg = this.mHandler.obtainMessage(MSG_DISPATCH_INPUT_EVENT, args);
        msg.setAsynchronous(USE_MT_RENDERER);
        this.mHandler.sendMessage(msg);
    }

    public void synthesizeInputEvent(InputEvent event) {
        Message msg = this.mHandler.obtainMessage(MSG_SYNTHESIZE_INPUT_EVENT, event);
        msg.setAsynchronous(USE_MT_RENDERER);
        this.mHandler.sendMessage(msg);
    }

    public void dispatchKeyFromIme(KeyEvent event) {
        Message msg = this.mHandler.obtainMessage(MSG_DISPATCH_KEY_FROM_IME, event);
        msg.setAsynchronous(USE_MT_RENDERER);
        this.mHandler.sendMessage(msg);
    }

    public void dispatchUnhandledInputEvent(InputEvent event) {
        if (event instanceof MotionEvent) {
            event = MotionEvent.obtain((MotionEvent) event);
        }
        synthesizeInputEvent(event);
    }

    public void dispatchAppVisibility(boolean visible) {
        Message msg = this.mHandler.obtainMessage(MSG_DISPATCH_APP_VISIBILITY);
        msg.arg1 = visible ? MSG_INVALIDATE : 0;
        this.mHandler.sendMessage(msg);
    }

    public void dispatchGetNewSurface() {
        this.mHandler.sendMessage(this.mHandler.obtainMessage(MSG_DISPATCH_GET_NEW_SURFACE));
    }

    public void windowFocusChanged(boolean hasFocus, boolean inTouchMode) {
        int i;
        int i2 = MSG_INVALIDATE;
        Message msg = Message.obtain();
        msg.what = MSG_WINDOW_FOCUS_CHANGED;
        if (hasFocus) {
            i = MSG_INVALIDATE;
        } else {
            i = 0;
        }
        msg.arg1 = i;
        if (!inTouchMode) {
            i2 = 0;
        }
        msg.arg2 = i2;
        this.mHandler.sendMessage(msg);
    }

    public void dispatchWindowShown() {
        this.mHandler.sendEmptyMessage(MSG_DISPATCH_WINDOW_SHOWN);
    }

    public void dispatchCloseSystemDialogs(String reason) {
        Message msg = Message.obtain();
        msg.what = MSG_CLOSE_SYSTEM_DIALOGS;
        msg.obj = reason;
        this.mHandler.sendMessage(msg);
    }

    public void dispatchDragEvent(DragEvent event) {
        int what;
        if (event.getAction() == MSG_INVALIDATE_RECT) {
            what = MSG_DISPATCH_DRAG_LOCATION_EVENT;
            this.mHandler.removeMessages(MSG_DISPATCH_DRAG_LOCATION_EVENT);
        } else {
            what = MSG_DISPATCH_DRAG_EVENT;
        }
        this.mHandler.sendMessage(this.mHandler.obtainMessage(what, event));
    }

    public void updatePointerIcon(float x, float y) {
        this.mHandler.removeMessages(MSG_UPDATE_POINTER_ICON);
        this.mHandler.sendMessage(this.mHandler.obtainMessage(MSG_UPDATE_POINTER_ICON, MotionEvent.obtain(0, SystemClock.uptimeMillis(), MSG_DISPATCH_INPUT_EVENT, x, y, 0)));
    }

    public void dispatchSystemUiVisibilityChanged(int seq, int globalVisibility, int localValue, int localChanges) {
        SystemUiVisibilityInfo args = new SystemUiVisibilityInfo();
        args.seq = seq;
        args.globalVisibility = globalVisibility;
        args.localValue = localValue;
        args.localChanges = localChanges;
        this.mHandler.sendMessage(this.mHandler.obtainMessage(MSG_DISPATCH_SYSTEM_UI_VISIBILITY, args));
    }

    public void dispatchCheckFocus() {
        if (!this.mHandler.hasMessages(MSG_CHECK_FOCUS)) {
            this.mHandler.sendEmptyMessage(MSG_CHECK_FOCUS);
        }
    }

    public void dispatchRequestKeyboardShortcuts(IResultReceiver receiver, int deviceId) {
        this.mHandler.obtainMessage(MSG_REQUEST_KEYBOARD_SHORTCUTS, deviceId, 0, receiver).sendToTarget();
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
        return LOCAL_LOGV;
    }

    public boolean showContextMenuForChild(View originalView, float x, float y) {
        return LOCAL_LOGV;
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
            return LOCAL_LOGV;
        }
        View source;
        switch (event.getEventType()) {
            case GL10.GL_EXP /*2048*/:
                handleWindowContentChangedEvent(event);
                break;
            case AccessibilityNodeInfo.ACTION_PASTE /*32768*/:
                long sourceNodeId = event.getSourceNodeId();
                source = this.mView.findViewByAccessibilityId(AccessibilityNodeInfo.getAccessibilityViewId(sourceNodeId));
                if (source != null) {
                    AccessibilityNodeProvider provider = source.getAccessibilityNodeProvider();
                    if (provider != null) {
                        AccessibilityNodeInfo node;
                        int virtualNodeId = AccessibilityNodeInfo.getVirtualDescendantId(sourceNodeId);
                        if (virtualNodeId == HwBootFail.STAGE_BOOT_SUCCESS) {
                            node = provider.createAccessibilityNodeInfo(-1);
                        } else {
                            node = provider.createAccessibilityNodeInfo(virtualNodeId);
                        }
                        setAccessibilityFocus(source, node);
                        break;
                    }
                }
                break;
            case Protocol.BASE_SYSTEM_RESERVED /*65536*/:
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
        return USE_MT_RENDERER;
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
            if ((changes & MSG_INVALIDATE) != 0 || changes == 0) {
                int changedViewId = AccessibilityNodeInfo.getAccessibilityViewId(event.getSourceNodeId());
                boolean hostInSubtree = LOCAL_LOGV;
                View view = this.mAccessibilityFocusedHost;
                while (view != null && !hostInSubtree) {
                    if (changedViewId == view.getAccessibilityViewId()) {
                        hostInSubtree = USE_MT_RENDERER;
                    } else {
                        ViewParent parent = view.getParent();
                        if (parent instanceof View) {
                            view = (View) parent;
                        } else {
                            view = null;
                        }
                    }
                }
                if (hostInSubtree) {
                    int focusedChildId = AccessibilityNodeInfo.getVirtualDescendantId(this.mAccessibilityFocusedVirtualView.getSourceNodeId());
                    if (focusedChildId == Integer.MAX_VALUE) {
                        focusedChildId = -1;
                    }
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
        postSendWindowContentChangedCallback(source, changeType);
    }

    public boolean canResolveLayoutDirection() {
        return USE_MT_RENDERER;
    }

    public boolean isLayoutDirectionResolved() {
        return USE_MT_RENDERER;
    }

    public int getLayoutDirection() {
        return 0;
    }

    public boolean canResolveTextDirection() {
        return USE_MT_RENDERER;
    }

    public boolean isTextDirectionResolved() {
        return USE_MT_RENDERER;
    }

    public int getTextDirection() {
        return MSG_INVALIDATE;
    }

    public boolean canResolveTextAlignment() {
        return USE_MT_RENDERER;
    }

    public boolean isTextAlignmentResolved() {
        return USE_MT_RENDERER;
    }

    public int getTextAlignment() {
        return MSG_INVALIDATE;
    }

    private View getCommonPredecessor(View first, View second) {
        if (this.mTempHashSet == null) {
            this.mTempHashSet = new HashSet();
        }
        HashSet<View> seen = this.mTempHashSet;
        seen.clear();
        View view = first;
        while (view != null) {
            seen.add(view);
            ViewParent firstCurrentParent = view.mParent;
            if (firstCurrentParent instanceof View) {
                view = (View) firstCurrentParent;
            } else {
                view = null;
            }
        }
        View view2 = second;
        while (view2 != null) {
            if (seen.contains(view2)) {
                seen.clear();
                return view2;
            }
            ViewParent secondCurrentParent = view2.mParent;
            if (secondCurrentParent instanceof View) {
                view2 = (View) secondCurrentParent;
            } else {
                view2 = null;
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
        return LOCAL_LOGV;
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
        return LOCAL_LOGV;
    }

    public boolean onNestedPreFling(View target, float velocityX, float velocityY) {
        return LOCAL_LOGV;
    }

    public boolean onNestedPrePerformAccessibilityAction(View target, int action, Bundle args) {
        return LOCAL_LOGV;
    }

    public void setReportNextDraw() {
        this.mReportNextDraw = USE_MT_RENDERER;
        invalidate();
    }

    void changeCanvasOpacity(boolean opaque) {
        Log.d(this.mTag, "changeCanvasOpacity: opaque=" + opaque);
        if (this.mAttachInfo.mHardwareRenderer != null) {
            this.mAttachInfo.mHardwareRenderer.setOpaque(opaque);
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
            this.mDragResizing = USE_MT_RENDERER;
            for (int i = this.mWindowCallbacks.size() - 1; i >= 0; i--) {
                ((WindowCallbacks) this.mWindowCallbacks.get(i)).onWindowDragResizeStart(initialBounds, fullscreen, systemInsets, stableInsets, resizeMode);
            }
            this.mFullRedrawNeeded = USE_MT_RENDERER;
        }
    }

    private void endDragResizing() {
        if (this.mDragResizing) {
            this.mDragResizing = LOCAL_LOGV;
            for (int i = this.mWindowCallbacks.size() - 1; i >= 0; i--) {
                ((WindowCallbacks) this.mWindowCallbacks.get(i)).onWindowDragResizeEnd();
            }
            this.mFullRedrawNeeded = USE_MT_RENDERER;
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
        this.mActivityRelaunched = USE_MT_RENDERER;
    }

    private void doRelayoutAsyncly(boolean shouldDo) {
        if (shouldDo) {
            this.mFullRedrawNeeded = USE_MT_RENDERER;
            this.mLayoutRequested = USE_MT_RENDERER;
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

    private HwRogTranslater getRogTranslator() {
        if (!this.mRogSwitchState || this.mRogInfo == null || !this.mRogInfo.isRogEnable()) {
            return null;
        }
        float rogAppSclae = this.mRogInfo.getRogAppSclae();
        if (Float.compare(rogAppSclae, LayoutParams.BRIGHTNESS_OVERRIDE_FULL) > 0) {
            return new HwRogTranslater(rogAppSclae, LayoutParams.BRIGHTNESS_OVERRIDE_FULL / rogAppSclae);
        }
        Slog.i(TAG, "getRogTranslator->no need to enable rog");
        return null;
    }

    private void handleRogSwitchStateChange(boolean state) {
        boolean z = USE_MT_RENDERER;
        unscheduleTraversals();
        destroyHardwareRenderer();
        this.mRogSwitchState = state;
        this.mRogStateChanged = USE_MT_RENDERER;
        if (!(this.mTranslator == null || this.mRogInfo == null)) {
            this.mTranslator = null;
            LayoutParams layoutParams = this.mWindowAttributes;
            layoutParams.privateFlags &= -129;
        }
        if (state) {
            this.mRogTranslater = getRogTranslator();
            layoutParams = this.mWindowAttributes;
            layoutParams.privateFlags |= AccessibilityNodeInfo.ACTION_DISMISS;
        } else {
            this.mRogTranslater = null;
            layoutParams = this.mWindowAttributes;
            layoutParams.privateFlags &= -1048577;
        }
        AttachInfo attachInfo = this.mAttachInfo;
        if (this.mRogTranslater == null) {
            z = LOCAL_LOGV;
        }
        attachInfo.mScalingRequired = z;
        this.mAttachInfo.mApplicationScale = this.mRogTranslater == null ? LayoutParams.BRIGHTNESS_OVERRIDE_FULL : this.mRogTranslater.applicationScale;
        if (this.mView != null) {
            forceLayout(this.mView);
        }
        requestLayout();
    }

    void dispatchRogSwitchStateChange(boolean state) {
        Message msg = this.mHandler.obtainMessage(MSG_ROG_SWITCH_STATE_CHANGE);
        msg.arg1 = state ? MSG_INVALIDATE : 0;
        this.mHandler.sendMessage(msg);
    }

    void setRogInfo(AppRogInfo info, boolean state) {
        this.mRogInfo = info;
        this.mRogSwitchState = state;
    }

    private AppRogInfo getRogInfo() {
        return this.mRogInfo;
    }
}
