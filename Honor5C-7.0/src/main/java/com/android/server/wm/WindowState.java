package com.android.server.wm;

import android.app.ActivityManager;
import android.app.ActivityManager.StackId;
import android.app.ActivityManagerInternal;
import android.app.AppOpsManager;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.Region;
import android.graphics.Region.Op;
import android.os.PowerManager.WakeLock;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.Trace;
import android.os.UserHandle;
import android.os.WorkSource;
import android.rog.AppRogInfo;
import android.rog.IRogManager;
import android.util.DisplayMetrics;
import android.util.Slog;
import android.util.TimeUtils;
import android.view.DisplayInfo;
import android.view.Gravity;
import android.view.IApplicationToken;
import android.view.IWindow;
import android.view.IWindowFocusObserver;
import android.view.IWindowId;
import android.view.IWindowId.Stub;
import android.view.InputChannel;
import android.view.InputEvent;
import android.view.InputEventReceiver;
import android.view.WindowManager.LayoutParams;
import android.view.WindowManagerPolicy;
import com.android.server.HwServiceFactory;
import com.android.server.HwServiceFactory.IHwWindowStateAnimator;
import com.android.server.LocalServices;
import com.android.server.am.ProcessList;
import com.android.server.input.InputWindowHandle;
import com.android.server.power.AbsPowerManagerService;
import com.android.server.power.IHwShutdownThread;
import com.android.server.usb.UsbAudioDevice;
import com.android.server.wm.WindowManagerService.H;
import java.io.PrintWriter;

public final class WindowState implements android.view.WindowManagerPolicy.WindowState {
    static final boolean DEBUG_DISABLE_SAVING_SURFACES = false;
    public static final int LOW_RESOLUTION_COMPOSITION_OFF = 1;
    public static final int LOW_RESOLUTION_COMPOSITION_ON = 2;
    public static final int LOW_RESOLUTION_FEATURE_OFF = 0;
    static final int MINIMUM_VISIBLE_HEIGHT_IN_DP = 36;
    static final int MINIMUM_VISIBLE_WIDTH_IN_DP = 42;
    static final int RESIZE_HANDLE_WIDTH_IN_DP = 30;
    static final boolean SHOW_TRANSACTIONS = false;
    static final String TAG = null;
    static final Region sEmptyRegion = null;
    private static final Rect sTmpRect = null;
    boolean mAnimateReplacingWindow;
    boolean mAnimatingExit;
    private boolean mAnimatingWithSavedSurface;
    boolean mAppDied;
    boolean mAppFreezing;
    final int mAppOp;
    public boolean mAppOpVisibility;
    AppWindowToken mAppToken;
    boolean mAttachedHidden;
    public final WindowState mAttachedWindow;
    public final LayoutParams mAttrs;
    final int mBaseLayer;
    private boolean mCanCarryColors;
    public final WindowList mChildWindows;
    final IWindow mClient;
    InputChannel mClientChannel;
    final Rect mCompatFrame;
    private boolean mConfigHasChanged;
    final Rect mContainingFrame;
    boolean mContentChanged;
    final Rect mContentFrame;
    final Rect mContentInsets;
    boolean mContentInsetsChanged;
    final Context mContext;
    private DeadWindowEventReceiver mDeadWindowEventReceiver;
    final DeathRecipient mDeathRecipient;
    final Rect mDecorFrame;
    boolean mDestroying;
    DisplayContent mDisplayContent;
    final Rect mDisplayFrame;
    boolean mDragResizing;
    boolean mDragResizingChangeReported;
    WakeLock mDrawLock;
    boolean mEnforceSizeCompat;
    RemoteCallbackList<IWindowFocusObserver> mFocusCallbacks;
    int mForceCompatMode;
    final Rect mFrame;
    final Rect mGivenContentInsets;
    boolean mGivenInsetsPending;
    final Region mGivenTouchableRegion;
    final Rect mGivenVisibleInsets;
    float mGlobalScale;
    float mHScale;
    boolean mHasSurface;
    boolean mHaveFrame;
    boolean mInRelayout;
    InputChannel mInputChannel;
    final InputWindowHandle mInputWindowHandle;
    final Rect mInsetFrame;
    float mInvGlobalScale;
    final boolean mIsFloatingLayer;
    final boolean mIsImWindow;
    final boolean mIsWallpaper;
    private boolean mJustMovedInStack;
    final Rect mLastContentInsets;
    final Rect mLastFrame;
    int mLastFreezeDuration;
    float mLastHScale;
    final Rect mLastOutsets;
    final Rect mLastOverscanInsets;
    int mLastRequestedHeight;
    int mLastRequestedWidth;
    final Rect mLastStableInsets;
    CharSequence mLastTitle;
    float mLastVScale;
    final Rect mLastVisibleInsets;
    int mLayer;
    final boolean mLayoutAttached;
    boolean mLayoutNeeded;
    int mLayoutSeq;
    private Configuration mMergedConfiguration;
    boolean mMovedByResize;
    boolean mNotOnAppsDisplay;
    boolean mObscured;
    boolean mOrientationChanging;
    final Rect mOutsetFrame;
    final Rect mOutsets;
    boolean mOutsetsChanged;
    final Rect mOverscanFrame;
    final Rect mOverscanInsets;
    boolean mOverscanInsetsChanged;
    final int mOwnerUid;
    final Rect mParentFrame;
    final WindowManagerPolicy mPolicy;
    boolean mPolicyVisibility;
    boolean mPolicyVisibilityAfterAnim;
    boolean mRebuilding;
    boolean mRelayoutCalled;
    boolean mRemoveOnExit;
    boolean mRemoved;
    boolean mReplacingRemoveRequested;
    WindowState mReplacingWindow;
    int mRequestedHeight;
    int mRequestedWidth;
    int mResizeMode;
    boolean mResizedWhileGone;
    private boolean mResizedWhileNotDragResizing;
    private boolean mResizedWhileNotDragResizingReported;
    WindowToken mRootToken;
    int mSeq;
    final WindowManagerService mService;
    final Session mSession;
    private boolean mShowToOwnerOnly;
    final Point mShownPosition;
    boolean mSkipEnterAnimationForSeamlessReplacement;
    final Rect mStableFrame;
    final Rect mStableInsets;
    boolean mStableInsetsChanged;
    String mStringNameCache;
    final int mSubLayer;
    private boolean mSurfaceSaved;
    int mSystemUiVisibility;
    AppWindowToken mTargetAppToken;
    private final Configuration mTmpConfig;
    final Matrix mTmpMatrix;
    private final Rect mTmpRect;
    WindowToken mToken;
    int mTouchableInsets;
    boolean mTurnOnScreen;
    float mVScale;
    int mViewVisibility;
    final Rect mVisibleFrame;
    final Rect mVisibleInsets;
    boolean mVisibleInsetsChanged;
    int mWallpaperDisplayOffsetX;
    int mWallpaperDisplayOffsetY;
    boolean mWallpaperVisible;
    float mWallpaperX;
    float mWallpaperXStep;
    float mWallpaperY;
    float mWallpaperYStep;
    boolean mWasExiting;
    boolean mWasVisibleBeforeClientHidden;
    boolean mWillReplaceWindow;
    final WindowStateAnimator mWinAnimator;
    final IWindowId mWindowId;
    boolean mWindowRemovalAllowed;
    int mXOffset;
    int mYOffset;

    /* renamed from: com.android.server.wm.WindowState.2 */
    class AnonymousClass2 implements Runnable {
        final /* synthetic */ Rect val$contentInsets;
        final /* synthetic */ Rect val$frame;
        final /* synthetic */ Configuration val$newConfig;
        final /* synthetic */ Rect val$outsets;
        final /* synthetic */ Rect val$overscanInsets;
        final /* synthetic */ boolean val$reportDraw;
        final /* synthetic */ Rect val$stableInsets;
        final /* synthetic */ Rect val$visibleInsets;

        AnonymousClass2(Rect val$frame, Rect val$overscanInsets, Rect val$contentInsets, Rect val$visibleInsets, Rect val$stableInsets, Rect val$outsets, boolean val$reportDraw, Configuration val$newConfig) {
            this.val$frame = val$frame;
            this.val$overscanInsets = val$overscanInsets;
            this.val$contentInsets = val$contentInsets;
            this.val$visibleInsets = val$visibleInsets;
            this.val$stableInsets = val$stableInsets;
            this.val$outsets = val$outsets;
            this.val$reportDraw = val$reportDraw;
            this.val$newConfig = val$newConfig;
        }

        public void run() {
            try {
                WindowState.this.dispatchResized(this.val$frame, this.val$overscanInsets, this.val$contentInsets, this.val$visibleInsets, this.val$stableInsets, this.val$outsets, this.val$reportDraw, this.val$newConfig);
            } catch (RemoteException e) {
            }
        }
    }

    private final class DeadWindowEventReceiver extends InputEventReceiver {
        DeadWindowEventReceiver(InputChannel inputChannel) {
            super(inputChannel, WindowState.this.mService.mH.getLooper());
        }

        public void onInputEvent(InputEvent event) {
            finishInputEvent(event, true);
        }
    }

    private class DeathRecipient implements android.os.IBinder.DeathRecipient {
        private DeathRecipient() {
        }

        public void binderDied() {
            try {
                synchronized (WindowState.this.mService.mWindowMap) {
                    WindowState win = WindowState.this.mService.windowForClientLocked(WindowState.this.mSession, WindowState.this.mClient, (boolean) WindowState.DEBUG_DISABLE_SAVING_SURFACES);
                    Slog.i(WindowState.TAG, "WIN DEATH: " + win);
                    if (win != null) {
                        WindowState.this.mService.removeWindowLocked(win, WindowState.this.shouldKeepVisibleDeadAppWindow());
                        if (win.mAttrs.type == 2034) {
                            TaskStack stack = (TaskStack) WindowState.this.mService.mStackIdToStack.get(3);
                            if (stack != null) {
                                stack.resetDockedStackToMiddle();
                            }
                            WindowState.this.mService.setDockedStackResizing(WindowState.DEBUG_DISABLE_SAVING_SURFACES);
                        }
                    } else if (WindowState.this.mHasSurface) {
                        Slog.e(WindowState.TAG, "!!! LEAK !!! Window removed but surface still valid.");
                        WindowState.this.mService.removeWindowLocked(WindowState.this);
                    }
                }
            } catch (IllegalArgumentException e) {
            }
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.wm.WindowState.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.wm.WindowState.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.wm.WindowState.<clinit>():void");
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    WindowState(WindowManagerService service, Session s, IWindow c, WindowToken token, WindowState attachedWindow, int appOp, int seq, LayoutParams a, int viewVisibility, DisplayContent displayContent, int forceCompatFlag) {
        boolean isRogEnable;
        this.mAttrs = new LayoutParams();
        this.mChildWindows = new WindowList();
        this.mPolicyVisibility = true;
        this.mPolicyVisibilityAfterAnim = true;
        this.mAppOpVisibility = true;
        this.mLayoutSeq = -1;
        this.mTmpConfig = new Configuration();
        this.mMergedConfiguration = new Configuration();
        this.mShownPosition = new Point();
        this.mVisibleInsets = new Rect();
        this.mLastVisibleInsets = new Rect();
        this.mContentInsets = new Rect();
        this.mLastContentInsets = new Rect();
        this.mOverscanInsets = new Rect();
        this.mLastOverscanInsets = new Rect();
        this.mStableInsets = new Rect();
        this.mLastStableInsets = new Rect();
        this.mOutsets = new Rect();
        this.mLastOutsets = new Rect();
        this.mOutsetsChanged = DEBUG_DISABLE_SAVING_SURFACES;
        this.mGivenContentInsets = new Rect();
        this.mGivenVisibleInsets = new Rect();
        this.mGivenTouchableRegion = new Region();
        this.mTouchableInsets = LOW_RESOLUTION_FEATURE_OFF;
        this.mGlobalScale = 1.0f;
        this.mInvGlobalScale = 1.0f;
        this.mHScale = 1.0f;
        this.mVScale = 1.0f;
        this.mLastHScale = 1.0f;
        this.mLastVScale = 1.0f;
        this.mTmpMatrix = new Matrix();
        this.mFrame = new Rect();
        this.mLastFrame = new Rect();
        this.mCompatFrame = new Rect();
        this.mContainingFrame = new Rect();
        this.mParentFrame = new Rect();
        this.mDisplayFrame = new Rect();
        this.mOverscanFrame = new Rect();
        this.mStableFrame = new Rect();
        this.mDecorFrame = new Rect();
        this.mContentFrame = new Rect();
        this.mVisibleFrame = new Rect();
        this.mOutsetFrame = new Rect();
        this.mInsetFrame = new Rect();
        this.mWallpaperX = -1.0f;
        this.mWallpaperY = -1.0f;
        this.mWallpaperXStep = -1.0f;
        this.mWallpaperYStep = -1.0f;
        this.mWallpaperDisplayOffsetX = UsbAudioDevice.kAudioDeviceMeta_Alsa;
        this.mWallpaperDisplayOffsetY = UsbAudioDevice.kAudioDeviceMeta_Alsa;
        this.mHasSurface = DEBUG_DISABLE_SAVING_SURFACES;
        this.mNotOnAppsDisplay = DEBUG_DISABLE_SAVING_SURFACES;
        this.mSurfaceSaved = DEBUG_DISABLE_SAVING_SURFACES;
        this.mWillReplaceWindow = DEBUG_DISABLE_SAVING_SURFACES;
        this.mReplacingRemoveRequested = DEBUG_DISABLE_SAVING_SURFACES;
        this.mAnimateReplacingWindow = DEBUG_DISABLE_SAVING_SURFACES;
        this.mReplacingWindow = null;
        this.mSkipEnterAnimationForSeamlessReplacement = DEBUG_DISABLE_SAVING_SURFACES;
        this.mForceCompatMode = LOW_RESOLUTION_FEATURE_OFF;
        this.mTmpRect = new Rect();
        this.mResizedWhileGone = DEBUG_DISABLE_SAVING_SURFACES;
        this.mService = service;
        this.mSession = s;
        this.mClient = c;
        this.mAppOp = appOp;
        this.mToken = token;
        this.mOwnerUid = s.mUid;
        this.mWindowId = new Stub() {
            public void registerFocusObserver(IWindowFocusObserver observer) {
                WindowState.this.registerFocusObserver(observer);
            }

            public void unregisterFocusObserver(IWindowFocusObserver observer) {
                WindowState.this.unregisterFocusObserver(observer);
            }

            public boolean isFocused() {
                return WindowState.this.isFocused();
            }
        };
        this.mAttrs.copyFrom(a);
        this.mViewVisibility = viewVisibility;
        this.mDisplayContent = displayContent;
        this.mPolicy = this.mService.mPolicy;
        this.mContext = this.mService.mContext;
        WindowState windowState = this;
        DeathRecipient deathRecipient = new DeathRecipient();
        this.mSeq = seq;
        if ((this.mAttrs.privateFlags & DumpState.DUMP_PACKAGES) == 0) {
            isRogEnable = isRogEnable();
        } else {
            isRogEnable = true;
        }
        this.mEnforceSizeCompat = isRogEnable;
        try {
            c.asBinder().linkToDeath(deathRecipient, LOW_RESOLUTION_FEATURE_OFF);
            this.mDeathRecipient = deathRecipient;
            if (this.mAttrs.type < ProcessList.PSS_SAFE_TIME_FROM_STATE_CHANGE || this.mAttrs.type > 1999) {
                this.mBaseLayer = (this.mPolicy.windowTypeToLayerLw(a.type) * AbsPowerManagerService.MIN_COVER_SCREEN_OFF_TIMEOUT) + ProcessList.PSS_SAFE_TIME_FROM_STATE_CHANGE;
                this.mSubLayer = LOW_RESOLUTION_FEATURE_OFF;
                this.mAttachedWindow = null;
                this.mLayoutAttached = DEBUG_DISABLE_SAVING_SURFACES;
                isRogEnable = this.mAttrs.type != 2011 ? this.mAttrs.type == 2012 ? true : DEBUG_DISABLE_SAVING_SURFACES : true;
                this.mIsImWindow = isRogEnable;
                this.mIsWallpaper = this.mAttrs.type == 2013 ? true : DEBUG_DISABLE_SAVING_SURFACES;
                this.mIsFloatingLayer = !this.mIsImWindow ? this.mIsWallpaper : true;
            } else {
                this.mBaseLayer = (this.mPolicy.windowTypeToLayerLw(attachedWindow.mAttrs.type) * AbsPowerManagerService.MIN_COVER_SCREEN_OFF_TIMEOUT) + ProcessList.PSS_SAFE_TIME_FROM_STATE_CHANGE;
                this.mSubLayer = this.mPolicy.subWindowTypeToLayerLw(a.type);
                this.mAttachedWindow = attachedWindow;
                WindowList childWindows = this.mAttachedWindow.mChildWindows;
                int numChildWindows = childWindows.size();
                if (numChildWindows == 0) {
                    childWindows.add(this);
                } else {
                    boolean added = DEBUG_DISABLE_SAVING_SURFACES;
                    for (int i = LOW_RESOLUTION_FEATURE_OFF; i < numChildWindows; i += LOW_RESOLUTION_COMPOSITION_OFF) {
                        int childSubLayer = ((WindowState) childWindows.get(i)).mSubLayer;
                        if (this.mSubLayer < childSubLayer || (this.mSubLayer == childSubLayer && childSubLayer < 0)) {
                            childWindows.add(i, this);
                            added = true;
                            break;
                        }
                    }
                    if (!added) {
                        childWindows.add(this);
                    }
                }
                this.mLayoutAttached = this.mAttrs.type != 1003 ? true : DEBUG_DISABLE_SAVING_SURFACES;
                isRogEnable = attachedWindow.mAttrs.type != 2011 ? attachedWindow.mAttrs.type == 2012 ? true : DEBUG_DISABLE_SAVING_SURFACES : true;
                this.mIsImWindow = isRogEnable;
                this.mIsWallpaper = attachedWindow.mAttrs.type == 2013 ? true : DEBUG_DISABLE_SAVING_SURFACES;
                if (this.mIsImWindow) {
                    isRogEnable = true;
                } else {
                    isRogEnable = this.mIsWallpaper;
                }
                this.mIsFloatingLayer = isRogEnable;
            }
            WindowState appWin = this;
            while (appWin.isChildWindow()) {
                appWin = appWin.mAttachedWindow;
            }
            if (forceCompatFlag == -3) {
                this.mForceCompatMode = appWin.mForceCompatMode;
            } else {
                this.mForceCompatMode = forceCompatFlag;
            }
            WindowToken appToken = appWin.mToken;
            while (appToken.appWindowToken == null) {
                WindowToken parent = (WindowToken) this.mService.mTokenMap.get(appToken.token);
                if (!(parent == null || appToken == parent)) {
                    appToken = parent;
                }
            }
            this.mRootToken = appToken;
            this.mAppToken = appToken.appWindowToken;
            if (this.mAppToken != null) {
                this.mNotOnAppsDisplay = displayContent != getDisplayContent() ? true : DEBUG_DISABLE_SAVING_SURFACES;
                if (this.mAppToken.showForAllUsers) {
                    LayoutParams layoutParams = this.mAttrs;
                    layoutParams.flags |= DumpState.DUMP_FROZEN;
                }
            }
            IHwWindowStateAnimator iwsa = HwServiceFactory.getHuaweiWindowStateAnimator();
            if (iwsa != null) {
                this.mWinAnimator = iwsa.getInstance(this);
            } else {
                this.mWinAnimator = new WindowStateAnimator(this);
            }
            this.mWinAnimator.mAlpha = a.alpha;
            this.mRequestedWidth = LOW_RESOLUTION_FEATURE_OFF;
            this.mRequestedHeight = LOW_RESOLUTION_FEATURE_OFF;
            this.mLastRequestedWidth = LOW_RESOLUTION_FEATURE_OFF;
            this.mLastRequestedHeight = LOW_RESOLUTION_FEATURE_OFF;
            this.mXOffset = LOW_RESOLUTION_FEATURE_OFF;
            this.mYOffset = LOW_RESOLUTION_FEATURE_OFF;
            this.mLayer = LOW_RESOLUTION_FEATURE_OFF;
            this.mInputWindowHandle = new InputWindowHandle(this.mAppToken != null ? this.mAppToken.mInputApplicationHandle : null, this, displayContent.getDisplayId());
        } catch (RemoteException e) {
            this.mDeathRecipient = null;
            this.mAttachedWindow = null;
            this.mLayoutAttached = DEBUG_DISABLE_SAVING_SURFACES;
            this.mIsImWindow = DEBUG_DISABLE_SAVING_SURFACES;
            this.mIsWallpaper = DEBUG_DISABLE_SAVING_SURFACES;
            this.mIsFloatingLayer = DEBUG_DISABLE_SAVING_SURFACES;
            this.mBaseLayer = LOW_RESOLUTION_FEATURE_OFF;
            this.mSubLayer = LOW_RESOLUTION_FEATURE_OFF;
            this.mInputWindowHandle = null;
            this.mWinAnimator = null;
        }
    }

    void attach() {
        this.mSession.windowAddedLocked();
    }

    public int getOwningUid() {
        return this.mOwnerUid;
    }

    public String getOwningPackage() {
        return this.mAttrs.packageName;
    }

    private void subtractInsets(Rect frame, Rect layoutFrame, Rect insetFrame, Rect displayFrame) {
        frame.inset(Math.max(LOW_RESOLUTION_FEATURE_OFF, insetFrame.left - Math.max(layoutFrame.left, displayFrame.left)), Math.max(LOW_RESOLUTION_FEATURE_OFF, insetFrame.top - Math.max(layoutFrame.top, displayFrame.top)), Math.max(LOW_RESOLUTION_FEATURE_OFF, Math.min(layoutFrame.right, displayFrame.right) - insetFrame.right), Math.max(LOW_RESOLUTION_FEATURE_OFF, Math.min(layoutFrame.bottom, displayFrame.bottom) - insetFrame.bottom));
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void computeFrameLw(Rect pf, Rect df, Rect of, Rect cf, Rect vf, Rect dcf, Rect sf, Rect osf) {
        if (!this.mWillReplaceWindow || (!this.mAnimatingExit && this.mReplacingRemoveRequested)) {
            Rect layoutDisplayFrame;
            Rect layoutContainingFrame;
            int layoutXDiff;
            int layoutYDiff;
            int i;
            boolean overrideRightInset;
            boolean overrideBottomInset;
            Rect rect;
            int i2;
            int i3;
            int i4;
            DisplayContent displayContent;
            DisplayInfo displayInfo;
            this.mHaveFrame = true;
            Task task = getTask();
            boolean fullscreenTask = isInMultiWindowMode() ? DEBUG_DISABLE_SAVING_SURFACES : true;
            boolean isFloating = task != null ? task.isFloating() : DEBUG_DISABLE_SAVING_SURFACES;
            if (fullscreenTask) {
                this.mInsetFrame.setEmpty();
            } else {
                task.getTempInsetBounds(this.mInsetFrame);
            }
            if (fullscreenTask || layoutInParentFrame()) {
                this.mContainingFrame.set(pf);
                this.mDisplayFrame.set(df);
                layoutDisplayFrame = df;
                layoutContainingFrame = pf;
                layoutXDiff = LOW_RESOLUTION_FEATURE_OFF;
                layoutYDiff = LOW_RESOLUTION_FEATURE_OFF;
            } else {
                task.getBounds(this.mContainingFrame);
                if (this.mAppToken != null) {
                    if (!this.mAppToken.mFrozenBounds.isEmpty()) {
                        Rect frozen = (Rect) this.mAppToken.mFrozenBounds.peek();
                        this.mContainingFrame.right = this.mContainingFrame.left + frozen.width();
                        this.mContainingFrame.bottom = this.mContainingFrame.top + frozen.height();
                    }
                }
                WindowState imeWin = this.mService.mInputMethodWindow;
                if (imeWin != null && imeWin.isVisibleNow()) {
                    WindowState windowState = this.mService.mInputMethodTarget;
                    if (r0 == this) {
                        if (!isFloating || this.mContainingFrame.bottom <= cf.bottom) {
                            if (this.mContainingFrame.bottom > pf.bottom) {
                                this.mContainingFrame.bottom = pf.bottom;
                            }
                        } else {
                            Rect rect2 = this.mContainingFrame;
                            rect2.top -= this.mContainingFrame.bottom - cf.bottom;
                        }
                    }
                }
                if (isFloating) {
                    if (this.mContainingFrame.isEmpty()) {
                        this.mContainingFrame.set(cf);
                    }
                }
                this.mDisplayFrame.set(this.mContainingFrame);
                layoutXDiff = !this.mInsetFrame.isEmpty() ? this.mInsetFrame.left - this.mContainingFrame.left : LOW_RESOLUTION_FEATURE_OFF;
                layoutYDiff = !this.mInsetFrame.isEmpty() ? this.mInsetFrame.top - this.mContainingFrame.top : LOW_RESOLUTION_FEATURE_OFF;
                layoutContainingFrame = !this.mInsetFrame.isEmpty() ? this.mInsetFrame : this.mContainingFrame;
                this.mTmpRect.set(LOW_RESOLUTION_FEATURE_OFF, LOW_RESOLUTION_FEATURE_OFF, this.mDisplayContent.getDisplayInfo().logicalWidth, this.mDisplayContent.getDisplayInfo().logicalHeight);
                subtractInsets(this.mDisplayFrame, layoutContainingFrame, df, this.mTmpRect);
                if (!layoutInParentFrame()) {
                    subtractInsets(this.mContainingFrame, layoutContainingFrame, pf, this.mTmpRect);
                    subtractInsets(this.mInsetFrame, layoutContainingFrame, pf, this.mTmpRect);
                }
                layoutDisplayFrame = df;
                df.intersect(layoutContainingFrame);
            }
            int pw = this.mContainingFrame.width();
            int ph = this.mContainingFrame.height();
            if (!this.mParentFrame.equals(pf)) {
                this.mParentFrame.set(pf);
                this.mContentChanged = true;
            }
            if (!(this.mRequestedWidth == this.mLastRequestedWidth && this.mRequestedHeight == this.mLastRequestedHeight)) {
                this.mLastRequestedWidth = this.mRequestedWidth;
                this.mLastRequestedHeight = this.mRequestedHeight;
                this.mContentChanged = true;
            }
            this.mOverscanFrame.set(of);
            this.mContentFrame.set(cf);
            this.mVisibleFrame.set(vf);
            this.mDecorFrame.set(dcf);
            this.mStableFrame.set(sf);
            boolean hasOutsets = osf != null ? true : DEBUG_DISABLE_SAVING_SURFACES;
            if (hasOutsets) {
                this.mOutsetFrame.set(osf);
            }
            int fw = this.mFrame.width();
            int fh = this.mFrame.height();
            applyGravityAndUpdateFrame(layoutContainingFrame, layoutDisplayFrame);
            if (hasOutsets) {
                this.mOutsets.set(Math.max(this.mContentFrame.left - this.mOutsetFrame.left, LOW_RESOLUTION_FEATURE_OFF), Math.max(this.mContentFrame.top - this.mOutsetFrame.top, LOW_RESOLUTION_FEATURE_OFF), Math.max(this.mOutsetFrame.right - this.mContentFrame.right, LOW_RESOLUTION_FEATURE_OFF), Math.max(this.mOutsetFrame.bottom - this.mContentFrame.bottom, LOW_RESOLUTION_FEATURE_OFF));
            } else {
                this.mOutsets.set(LOW_RESOLUTION_FEATURE_OFF, LOW_RESOLUTION_FEATURE_OFF, LOW_RESOLUTION_FEATURE_OFF, LOW_RESOLUTION_FEATURE_OFF);
            }
            if (isFloating) {
                if (!this.mFrame.isEmpty()) {
                    int height = Math.min(this.mFrame.height(), this.mContentFrame.height());
                    int width = Math.min(this.mContentFrame.width(), this.mFrame.width());
                    DisplayMetrics displayMetrics = getDisplayContent().getDisplayMetrics();
                    int minVisibleHeight = WindowManagerService.dipToPixel(MINIMUM_VISIBLE_HEIGHT_IN_DP, displayMetrics);
                    int minVisibleWidth = WindowManagerService.dipToPixel(MINIMUM_VISIBLE_WIDTH_IN_DP, displayMetrics);
                    int top = Math.max(this.mContentFrame.top, Math.min(this.mFrame.top, this.mContentFrame.bottom - minVisibleHeight));
                    int left = Math.max((this.mContentFrame.left + minVisibleWidth) - width, Math.min(this.mFrame.left, this.mContentFrame.right - minVisibleWidth));
                    this.mFrame.set(left, top, left + width, top + height);
                    this.mContentFrame.set(this.mFrame);
                    this.mVisibleFrame.set(this.mContentFrame);
                    this.mStableFrame.set(this.mContentFrame);
                    if (fullscreenTask && !isFloating) {
                        this.mOverscanInsets.set(Math.max(this.mOverscanFrame.left - layoutContainingFrame.left, LOW_RESOLUTION_FEATURE_OFF), Math.max(this.mOverscanFrame.top - layoutContainingFrame.top, LOW_RESOLUTION_FEATURE_OFF), Math.max(layoutContainingFrame.right - this.mOverscanFrame.right, LOW_RESOLUTION_FEATURE_OFF), Math.max(layoutContainingFrame.bottom - this.mOverscanFrame.bottom, LOW_RESOLUTION_FEATURE_OFF));
                    }
                    i = this.mAttrs.type;
                    if (r0 != 2034) {
                        this.mStableInsets.set(Math.max(this.mStableFrame.left - this.mDisplayFrame.left, LOW_RESOLUTION_FEATURE_OFF), Math.max(this.mStableFrame.top - this.mDisplayFrame.top, LOW_RESOLUTION_FEATURE_OFF), Math.max(this.mDisplayFrame.right - this.mStableFrame.right, LOW_RESOLUTION_FEATURE_OFF), Math.max(this.mDisplayFrame.bottom - this.mStableFrame.bottom, LOW_RESOLUTION_FEATURE_OFF));
                        this.mContentInsets.setEmpty();
                        this.mVisibleInsets.setEmpty();
                    } else {
                        if (getDisplayContent() == null) {
                            getDisplayContent().getLogicalDisplayRect(this.mTmpRect);
                        } else {
                            this.mDisplayContent.getLogicalDisplayRect(this.mTmpRect);
                        }
                        overrideRightInset = (fullscreenTask || this.mFrame.right <= this.mTmpRect.right) ? DEBUG_DISABLE_SAVING_SURFACES : true;
                        overrideBottomInset = (fullscreenTask || this.mFrame.bottom <= this.mTmpRect.bottom) ? DEBUG_DISABLE_SAVING_SURFACES : true;
                        rect = this.mContentInsets;
                        i2 = this.mContentFrame.left - this.mFrame.left;
                        i3 = this.mContentFrame.top - this.mFrame.top;
                        if (overrideRightInset) {
                            i4 = this.mFrame.right - this.mContentFrame.right;
                        } else {
                            i4 = this.mTmpRect.right - this.mContentFrame.right;
                        }
                        if (overrideBottomInset) {
                            i = this.mFrame.bottom - this.mContentFrame.bottom;
                        } else {
                            i = this.mTmpRect.bottom - this.mContentFrame.bottom;
                        }
                        rect.set(i2, i3, i4, i);
                        rect = this.mVisibleInsets;
                        i2 = this.mVisibleFrame.left - this.mFrame.left;
                        i3 = this.mVisibleFrame.top - this.mFrame.top;
                        if (overrideRightInset) {
                            i4 = this.mFrame.right - this.mVisibleFrame.right;
                        } else {
                            i4 = this.mTmpRect.right - this.mVisibleFrame.right;
                        }
                        if (overrideBottomInset) {
                            i = this.mFrame.bottom - this.mVisibleFrame.bottom;
                        } else {
                            i = this.mTmpRect.bottom - this.mVisibleFrame.bottom;
                        }
                        rect.set(i2, i3, i4, i);
                        rect = this.mStableInsets;
                        i2 = Math.max(this.mStableFrame.left - this.mFrame.left, LOW_RESOLUTION_FEATURE_OFF);
                        i3 = Math.max(this.mStableFrame.top - this.mFrame.top, LOW_RESOLUTION_FEATURE_OFF);
                        if (overrideRightInset) {
                            i4 = Math.max(this.mFrame.right - this.mStableFrame.right, LOW_RESOLUTION_FEATURE_OFF);
                        } else {
                            i4 = Math.max(this.mTmpRect.right - this.mStableFrame.right, LOW_RESOLUTION_FEATURE_OFF);
                        }
                        if (overrideBottomInset) {
                            i = Math.max(this.mFrame.bottom - this.mStableFrame.bottom, LOW_RESOLUTION_FEATURE_OFF);
                        } else {
                            i = Math.max(this.mTmpRect.bottom - this.mStableFrame.bottom, LOW_RESOLUTION_FEATURE_OFF);
                        }
                        rect.set(i2, i3, i4, i);
                    }
                    this.mFrame.offset(-layoutXDiff, -layoutYDiff);
                    this.mCompatFrame.offset(-layoutXDiff, -layoutYDiff);
                    this.mContentFrame.offset(-layoutXDiff, -layoutYDiff);
                    this.mVisibleFrame.offset(-layoutXDiff, -layoutYDiff);
                    this.mStableFrame.offset(-layoutXDiff, -layoutYDiff);
                    this.mCompatFrame.set(this.mFrame);
                    if (this.mEnforceSizeCompat) {
                        this.mOverscanInsets.scale(this.mInvGlobalScale);
                        this.mContentInsets.scale(this.mInvGlobalScale);
                        this.mVisibleInsets.scale(this.mInvGlobalScale);
                        this.mStableInsets.scale(this.mInvGlobalScale);
                        this.mOutsets.scale(this.mInvGlobalScale);
                        this.mCompatFrame.scale(this.mInvGlobalScale);
                    }
                    if (this.mIsWallpaper) {
                        if (fw == this.mFrame.width()) {
                        }
                        displayContent = getDisplayContent();
                        if (displayContent != null) {
                            displayInfo = displayContent.getDisplayInfo();
                            this.mService.mWallpaperControllerLocked.updateWallpaperOffset(this, displayInfo.logicalWidth, displayInfo.logicalHeight, DEBUG_DISABLE_SAVING_SURFACES);
                        }
                    }
                }
            }
            i = this.mAttrs.type;
            if (r0 == 2034) {
                this.mDisplayContent.getDockedDividerController().positionDockedStackedDivider(this.mFrame);
                this.mContentFrame.set(this.mFrame);
                if (!this.mFrame.equals(this.mLastFrame)) {
                    this.mMovedByResize = true;
                }
            } else {
                this.mContentFrame.set(Math.max(this.mContentFrame.left, this.mFrame.left), Math.max(this.mContentFrame.top, this.mFrame.top), Math.min(this.mContentFrame.right, this.mFrame.right), Math.min(this.mContentFrame.bottom, this.mFrame.bottom));
                this.mVisibleFrame.set(Math.max(this.mVisibleFrame.left, this.mFrame.left), Math.max(this.mVisibleFrame.top, this.mFrame.top), Math.min(this.mVisibleFrame.right, this.mFrame.right), Math.min(this.mVisibleFrame.bottom, this.mFrame.bottom));
                this.mStableFrame.set(Math.max(this.mStableFrame.left, this.mFrame.left), Math.max(this.mStableFrame.top, this.mFrame.top), Math.min(this.mStableFrame.right, this.mFrame.right), Math.min(this.mStableFrame.bottom, this.mFrame.bottom));
            }
            this.mOverscanInsets.set(Math.max(this.mOverscanFrame.left - layoutContainingFrame.left, LOW_RESOLUTION_FEATURE_OFF), Math.max(this.mOverscanFrame.top - layoutContainingFrame.top, LOW_RESOLUTION_FEATURE_OFF), Math.max(layoutContainingFrame.right - this.mOverscanFrame.right, LOW_RESOLUTION_FEATURE_OFF), Math.max(layoutContainingFrame.bottom - this.mOverscanFrame.bottom, LOW_RESOLUTION_FEATURE_OFF));
            i = this.mAttrs.type;
            if (r0 != 2034) {
                if (getDisplayContent() == null) {
                    this.mDisplayContent.getLogicalDisplayRect(this.mTmpRect);
                } else {
                    getDisplayContent().getLogicalDisplayRect(this.mTmpRect);
                }
                if (!fullscreenTask) {
                }
                if (!fullscreenTask) {
                }
                rect = this.mContentInsets;
                i2 = this.mContentFrame.left - this.mFrame.left;
                i3 = this.mContentFrame.top - this.mFrame.top;
                if (overrideRightInset) {
                    i4 = this.mFrame.right - this.mContentFrame.right;
                } else {
                    i4 = this.mTmpRect.right - this.mContentFrame.right;
                }
                if (overrideBottomInset) {
                    i = this.mFrame.bottom - this.mContentFrame.bottom;
                } else {
                    i = this.mTmpRect.bottom - this.mContentFrame.bottom;
                }
                rect.set(i2, i3, i4, i);
                rect = this.mVisibleInsets;
                i2 = this.mVisibleFrame.left - this.mFrame.left;
                i3 = this.mVisibleFrame.top - this.mFrame.top;
                if (overrideRightInset) {
                    i4 = this.mFrame.right - this.mVisibleFrame.right;
                } else {
                    i4 = this.mTmpRect.right - this.mVisibleFrame.right;
                }
                if (overrideBottomInset) {
                    i = this.mFrame.bottom - this.mVisibleFrame.bottom;
                } else {
                    i = this.mTmpRect.bottom - this.mVisibleFrame.bottom;
                }
                rect.set(i2, i3, i4, i);
                rect = this.mStableInsets;
                i2 = Math.max(this.mStableFrame.left - this.mFrame.left, LOW_RESOLUTION_FEATURE_OFF);
                i3 = Math.max(this.mStableFrame.top - this.mFrame.top, LOW_RESOLUTION_FEATURE_OFF);
                if (overrideRightInset) {
                    i4 = Math.max(this.mFrame.right - this.mStableFrame.right, LOW_RESOLUTION_FEATURE_OFF);
                } else {
                    i4 = Math.max(this.mTmpRect.right - this.mStableFrame.right, LOW_RESOLUTION_FEATURE_OFF);
                }
                if (overrideBottomInset) {
                    i = Math.max(this.mFrame.bottom - this.mStableFrame.bottom, LOW_RESOLUTION_FEATURE_OFF);
                } else {
                    i = Math.max(this.mTmpRect.bottom - this.mStableFrame.bottom, LOW_RESOLUTION_FEATURE_OFF);
                }
                rect.set(i2, i3, i4, i);
            } else {
                this.mStableInsets.set(Math.max(this.mStableFrame.left - this.mDisplayFrame.left, LOW_RESOLUTION_FEATURE_OFF), Math.max(this.mStableFrame.top - this.mDisplayFrame.top, LOW_RESOLUTION_FEATURE_OFF), Math.max(this.mDisplayFrame.right - this.mStableFrame.right, LOW_RESOLUTION_FEATURE_OFF), Math.max(this.mDisplayFrame.bottom - this.mStableFrame.bottom, LOW_RESOLUTION_FEATURE_OFF));
                this.mContentInsets.setEmpty();
                this.mVisibleInsets.setEmpty();
            }
            this.mFrame.offset(-layoutXDiff, -layoutYDiff);
            this.mCompatFrame.offset(-layoutXDiff, -layoutYDiff);
            this.mContentFrame.offset(-layoutXDiff, -layoutYDiff);
            this.mVisibleFrame.offset(-layoutXDiff, -layoutYDiff);
            this.mStableFrame.offset(-layoutXDiff, -layoutYDiff);
            this.mCompatFrame.set(this.mFrame);
            if (this.mEnforceSizeCompat) {
                this.mOverscanInsets.scale(this.mInvGlobalScale);
                this.mContentInsets.scale(this.mInvGlobalScale);
                this.mVisibleInsets.scale(this.mInvGlobalScale);
                this.mStableInsets.scale(this.mInvGlobalScale);
                this.mOutsets.scale(this.mInvGlobalScale);
                this.mCompatFrame.scale(this.mInvGlobalScale);
            }
            if (this.mIsWallpaper) {
                if (fw == this.mFrame.width()) {
                }
                displayContent = getDisplayContent();
                if (displayContent != null) {
                    displayInfo = displayContent.getDisplayInfo();
                    this.mService.mWallpaperControllerLocked.updateWallpaperOffset(this, displayInfo.logicalWidth, displayInfo.logicalHeight, DEBUG_DISABLE_SAVING_SURFACES);
                }
            }
        }
    }

    public Rect getFrameLw() {
        return this.mFrame;
    }

    public Point getShownPositionLw() {
        return this.mShownPosition;
    }

    public Rect getDisplayFrameLw() {
        return this.mDisplayFrame;
    }

    public Rect getOverscanFrameLw() {
        return this.mOverscanFrame;
    }

    public Rect getContentFrameLw() {
        return this.mContentFrame;
    }

    public Rect getVisibleFrameLw() {
        return this.mVisibleFrame;
    }

    public boolean getGivenInsetsPendingLw() {
        return this.mGivenInsetsPending;
    }

    public Rect getGivenContentInsetsLw() {
        return this.mGivenContentInsets;
    }

    public Rect getGivenVisibleInsetsLw() {
        return this.mGivenVisibleInsets;
    }

    public LayoutParams getAttrs() {
        return this.mAttrs;
    }

    public boolean getNeedsMenuLw(android.view.WindowManagerPolicy.WindowState bottom) {
        boolean z = true;
        int index = -1;
        android.view.WindowManagerPolicy.WindowState ws = this;
        WindowList windows = getWindowList();
        while (ws.mAttrs.needsMenuKey == 0) {
            if (ws == bottom) {
                return DEBUG_DISABLE_SAVING_SURFACES;
            }
            if (index < 0) {
                index = windows.indexOf(ws);
            }
            index--;
            if (index < 0) {
                return DEBUG_DISABLE_SAVING_SURFACES;
            }
            WindowState ws2 = (WindowState) windows.get(index);
        }
        if (ws.mAttrs.needsMenuKey != LOW_RESOLUTION_COMPOSITION_OFF) {
            z = DEBUG_DISABLE_SAVING_SURFACES;
        }
        return z;
    }

    public int getSystemUiVisibility() {
        return this.mSystemUiVisibility;
    }

    public int getSurfaceLayer() {
        return this.mLayer;
    }

    public int getBaseType() {
        WindowState win = this;
        while (win.isChildWindow()) {
            win = win.mAttachedWindow;
        }
        return win.mAttrs.type;
    }

    public IApplicationToken getAppToken() {
        return this.mAppToken != null ? this.mAppToken.appToken : null;
    }

    public boolean isVoiceInteraction() {
        return this.mAppToken != null ? this.mAppToken.voiceInteraction : DEBUG_DISABLE_SAVING_SURFACES;
    }

    boolean setInsetsChanged() {
        int i;
        int i2 = LOW_RESOLUTION_FEATURE_OFF;
        this.mOverscanInsetsChanged = (this.mLastOverscanInsets.equals(this.mOverscanInsets) ? LOW_RESOLUTION_FEATURE_OFF : LOW_RESOLUTION_COMPOSITION_OFF) | this.mOverscanInsetsChanged;
        boolean z = this.mContentInsetsChanged;
        if (this.mLastContentInsets.equals(this.mContentInsets)) {
            i = LOW_RESOLUTION_FEATURE_OFF;
        } else {
            i = LOW_RESOLUTION_COMPOSITION_OFF;
        }
        this.mContentInsetsChanged = i | z;
        z = this.mVisibleInsetsChanged;
        if (this.mLastVisibleInsets.equals(this.mVisibleInsets)) {
            i = LOW_RESOLUTION_FEATURE_OFF;
        } else {
            i = LOW_RESOLUTION_COMPOSITION_OFF;
        }
        this.mVisibleInsetsChanged = i | z;
        z = this.mStableInsetsChanged;
        if (this.mLastStableInsets.equals(this.mStableInsets)) {
            i = LOW_RESOLUTION_FEATURE_OFF;
        } else {
            i = LOW_RESOLUTION_COMPOSITION_OFF;
        }
        this.mStableInsetsChanged = i | z;
        boolean z2 = this.mOutsetsChanged;
        if (!this.mLastOutsets.equals(this.mOutsets)) {
            i2 = LOW_RESOLUTION_COMPOSITION_OFF;
        }
        this.mOutsetsChanged = z2 | i2;
        if (this.mOverscanInsetsChanged || this.mContentInsetsChanged || this.mVisibleInsetsChanged) {
            return true;
        }
        return this.mOutsetsChanged;
    }

    public DisplayContent getDisplayContent() {
        if (this.mAppToken == null || this.mNotOnAppsDisplay) {
            return this.mDisplayContent;
        }
        TaskStack stack = getStack();
        return stack == null ? this.mDisplayContent : stack.getDisplayContent();
    }

    public DisplayInfo getDisplayInfo() {
        DisplayContent displayContent = getDisplayContent();
        if (displayContent != null) {
            return displayContent.getDisplayInfo();
        }
        return null;
    }

    public int getDisplayId() {
        DisplayContent displayContent = getDisplayContent();
        if (displayContent == null) {
            return -1;
        }
        return displayContent.getDisplayId();
    }

    Task getTask() {
        return this.mAppToken != null ? this.mAppToken.mTask : null;
    }

    TaskStack getStack() {
        TaskStack taskStack = null;
        Task task = getTask();
        if (task != null && task.mStack != null) {
            return task.mStack;
        }
        if (this.mAttrs.type >= IHwShutdownThread.SHUTDOWN_ANIMATION_WAIT_TIME && this.mDisplayContent != null) {
            taskStack = this.mDisplayContent.getHomeStack();
        }
        return taskStack;
    }

    void getVisibleBounds(Rect bounds) {
        Task task = getTask();
        boolean cropWindowsToStackBounds = task != null ? task.cropWindowsToStackBounds() : DEBUG_DISABLE_SAVING_SURFACES;
        bounds.setEmpty();
        this.mTmpRect.setEmpty();
        if (cropWindowsToStackBounds) {
            TaskStack stack = task.mStack;
            if (stack != null) {
                stack.getDimBounds(this.mTmpRect);
            } else {
                cropWindowsToStackBounds = DEBUG_DISABLE_SAVING_SURFACES;
            }
        }
        bounds.set(this.mVisibleFrame);
        if (cropWindowsToStackBounds) {
            bounds.intersect(this.mTmpRect);
        }
        if (bounds.isEmpty()) {
            bounds.set(this.mFrame);
            if (cropWindowsToStackBounds) {
                bounds.intersect(this.mTmpRect);
            }
        }
    }

    public long getInputDispatchingTimeoutNanos() {
        if (this.mAppToken != null) {
            return this.mAppToken.inputDispatchingTimeoutNanos;
        }
        return 5000000000L;
    }

    public boolean hasAppShownWindows() {
        if (this.mAppToken != null) {
            return !this.mAppToken.firstWindowDrawn ? this.mAppToken.startingDisplayed : true;
        } else {
            return DEBUG_DISABLE_SAVING_SURFACES;
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    boolean isIdentityMatrix(float dsdx, float dtdx, float dsdy, float dtdy) {
        if (dsdx < 0.99999f || dsdx > 1.00001f || dtdy < 0.99999f || dtdy > 1.00001f || dtdx < -1.0E-6f || dtdx > 1.0E-6f || dsdy < -1.0E-6f || dsdy > 1.0E-6f) {
            return DEBUG_DISABLE_SAVING_SURFACES;
        }
        return true;
    }

    void prelayout() {
        if (!this.mEnforceSizeCompat) {
            this.mInvGlobalScale = 1.0f;
            this.mGlobalScale = 1.0f;
        } else if (isRogEnable()) {
            float rogScale = getRogScale();
            this.mGlobalScale = rogScale;
            this.mInvGlobalScale = 1.0f / rogScale;
        } else if (this.mForceCompatMode == LOW_RESOLUTION_COMPOSITION_OFF) {
            this.mGlobalScale = this.mService.mForceCompatibleScreenScale;
            this.mInvGlobalScale = 1.0f / this.mGlobalScale;
        } else {
            this.mGlobalScale = this.mService.mCompatibleScreenScale;
            this.mInvGlobalScale = 1.0f / this.mGlobalScale;
        }
    }

    private boolean isVisibleUnchecked() {
        if (!this.mHasSurface || !this.mPolicyVisibility || this.mAttachedHidden || this.mAnimatingExit || this.mDestroying) {
            return DEBUG_DISABLE_SAVING_SURFACES;
        }
        return this.mIsWallpaper ? this.mWallpaperVisible : true;
    }

    public boolean isVisibleLw() {
        return (this.mAppToken == null || !this.mAppToken.hiddenRequested) ? isVisibleUnchecked() : DEBUG_DISABLE_SAVING_SURFACES;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean isVisibleOrBehindKeyguardLw() {
        if (this.mRootToken.waitingToShow && this.mService.mAppTransition.isTransitionSet()) {
            return DEBUG_DISABLE_SAVING_SURFACES;
        }
        AppWindowToken atoken = this.mAppToken;
        boolean animating = (atoken == null || atoken.mAppAnimator.animation == null) ? DEBUG_DISABLE_SAVING_SURFACES : true;
        if (!(!this.mHasSurface || this.mDestroying || this.mAnimatingExit)) {
            if (atoken != null) {
                if (atoken.hiddenRequested) {
                }
            }
            if (!((this.mAttachedHidden || this.mViewVisibility != 0 || this.mRootToken.hidden) && this.mWinAnimator.mAnimation == null)) {
                animating = true;
            }
            return animating;
        }
        animating = DEBUG_DISABLE_SAVING_SURFACES;
        return animating;
    }

    public boolean isWinVisibleLw() {
        if (this.mAppToken == null || !this.mAppToken.hiddenRequested || this.mAppToken.mAppAnimator.animating) {
            return isVisibleUnchecked();
        }
        return DEBUG_DISABLE_SAVING_SURFACES;
    }

    boolean isVisibleNow() {
        if (!this.mRootToken.hidden || this.mAttrs.type == 3) {
            return isVisibleUnchecked();
        }
        return DEBUG_DISABLE_SAVING_SURFACES;
    }

    boolean isPotentialDragTarget() {
        if (!isVisibleNow() || this.mRemoved || this.mInputChannel == null || this.mInputWindowHandle == null) {
            return DEBUG_DISABLE_SAVING_SURFACES;
        }
        return true;
    }

    boolean isVisibleOrAdding() {
        AppWindowToken atoken = this.mAppToken;
        if ((!this.mHasSurface && (this.mRelayoutCalled || this.mViewVisibility != 0)) || !this.mPolicyVisibility || this.mAttachedHidden) {
            return DEBUG_DISABLE_SAVING_SURFACES;
        }
        if ((atoken != null && atoken.hiddenRequested) || this.mAnimatingExit || this.mDestroying) {
            return DEBUG_DISABLE_SAVING_SURFACES;
        }
        return true;
    }

    boolean isOnScreen() {
        return this.mPolicyVisibility ? isOnScreenIgnoringKeyguard() : DEBUG_DISABLE_SAVING_SURFACES;
    }

    boolean isOnScreenIgnoringKeyguard() {
        boolean z = true;
        if (!this.mHasSurface || this.mDestroying) {
            return DEBUG_DISABLE_SAVING_SURFACES;
        }
        AppWindowToken atoken = this.mAppToken;
        if (atoken != null) {
            if ((this.mAttachedHidden || atoken.hiddenRequested) && this.mWinAnimator.mAnimation == null && atoken.mAppAnimator.animation == null) {
                z = DEBUG_DISABLE_SAVING_SURFACES;
            }
            return z;
        }
        if (this.mAttachedHidden && this.mWinAnimator.mAnimation == null) {
            z = DEBUG_DISABLE_SAVING_SURFACES;
        }
        return z;
    }

    boolean mightAffectAllDrawn(boolean visibleOnly) {
        boolean isViewVisible = ((this.mAppToken == null || !this.mAppToken.clientHidden) && this.mViewVisibility == 0) ? this.mWindowRemovalAllowed ? DEBUG_DISABLE_SAVING_SURFACES : true : DEBUG_DISABLE_SAVING_SURFACES;
        if (((!isOnScreenIgnoringKeyguard() || (visibleOnly && !isViewVisible)) && this.mWinAnimator.mAttrType != LOW_RESOLUTION_COMPOSITION_OFF) || this.mAnimatingExit || this.mDestroying) {
            return DEBUG_DISABLE_SAVING_SURFACES;
        }
        return true;
    }

    boolean isInteresting() {
        if (this.mAppToken == null || this.mAppDied) {
            return DEBUG_DISABLE_SAVING_SURFACES;
        }
        if (this.mAppToken.mAppAnimator.freezingScreen && this.mAppFreezing) {
            return DEBUG_DISABLE_SAVING_SURFACES;
        }
        return true;
    }

    boolean isReadyForDisplay() {
        boolean z = true;
        if (this.mRootToken.waitingToShow && this.mService.mAppTransition.isTransitionSet()) {
            return DEBUG_DISABLE_SAVING_SURFACES;
        }
        if (!this.mHasSurface || !this.mPolicyVisibility || this.mDestroying) {
            z = DEBUG_DISABLE_SAVING_SURFACES;
        } else if ((this.mAttachedHidden || this.mViewVisibility != 0 || this.mRootToken.hidden) && this.mWinAnimator.mAnimation == null && (this.mAppToken == null || this.mAppToken.mAppAnimator.animation == null)) {
            z = DEBUG_DISABLE_SAVING_SURFACES;
        }
        return z;
    }

    boolean isReadyForDisplayIgnoringKeyguard() {
        boolean z = DEBUG_DISABLE_SAVING_SURFACES;
        if (this.mRootToken.waitingToShow && this.mService.mAppTransition.isTransitionSet()) {
            return DEBUG_DISABLE_SAVING_SURFACES;
        }
        AppWindowToken atoken = this.mAppToken;
        if (atoken == null && !this.mPolicyVisibility) {
            return DEBUG_DISABLE_SAVING_SURFACES;
        }
        if (this.mHasSurface && !this.mDestroying) {
            if ((!this.mAttachedHidden && this.mViewVisibility == 0 && !this.mRootToken.hidden) || this.mWinAnimator.mAnimation != null) {
                z = true;
            } else if (!(atoken == null || atoken.mAppAnimator.animation == null || this.mWinAnimator.isDummyAnimation())) {
                z = true;
            }
        }
        return z;
    }

    public boolean isDisplayedLw() {
        AppWindowToken atoken = this.mAppToken;
        if (!isDrawnLw() || !this.mPolicyVisibility) {
            return DEBUG_DISABLE_SAVING_SURFACES;
        }
        if ((!this.mAttachedHidden && (atoken == null || !atoken.hiddenRequested)) || this.mWinAnimator.mAnimating) {
            return true;
        }
        if (atoken == null || atoken.mAppAnimator.animation == null) {
            return DEBUG_DISABLE_SAVING_SURFACES;
        }
        return true;
    }

    public boolean isAnimatingLw() {
        if (this.mWinAnimator.mAnimation == null) {
            return (this.mAppToken == null || this.mAppToken.mAppAnimator.animation == null) ? DEBUG_DISABLE_SAVING_SURFACES : true;
        } else {
            return true;
        }
    }

    public boolean isGoneForLayoutLw() {
        AppWindowToken atoken = this.mAppToken;
        if (this.mViewVisibility == 8 || !this.mRelayoutCalled || ((atoken == null && this.mRootToken.hidden) || ((atoken != null && atoken.hiddenRequested) || this.mAttachedHidden || (this.mAnimatingExit && !isAnimatingLw())))) {
            return true;
        }
        return this.mDestroying;
    }

    public boolean isDrawFinishedLw() {
        if (!this.mHasSurface || this.mDestroying) {
            return DEBUG_DISABLE_SAVING_SURFACES;
        }
        if (this.mWinAnimator.mDrawState == LOW_RESOLUTION_COMPOSITION_ON || this.mWinAnimator.mDrawState == 3 || this.mWinAnimator.mDrawState == 4) {
            return true;
        }
        return DEBUG_DISABLE_SAVING_SURFACES;
    }

    public boolean isDrawnLw() {
        if (!this.mHasSurface || this.mDestroying) {
            return DEBUG_DISABLE_SAVING_SURFACES;
        }
        if (this.mWinAnimator.mDrawState == 3 || this.mWinAnimator.mDrawState == 4) {
            return true;
        }
        return DEBUG_DISABLE_SAVING_SURFACES;
    }

    boolean isOpaqueDrawn() {
        if (((!this.mIsWallpaper && this.mAttrs.format == -1) || (this.mIsWallpaper && this.mWallpaperVisible)) && isDrawnLw() && this.mWinAnimator.mAnimation == null) {
            return (this.mAppToken == null || this.mAppToken.mAppAnimator.animation == null) ? true : DEBUG_DISABLE_SAVING_SURFACES;
        } else {
            return DEBUG_DISABLE_SAVING_SURFACES;
        }
    }

    boolean hasMoved() {
        if (!this.mHasSurface || ((!this.mContentChanged && !this.mMovedByResize) || this.mAnimatingExit || !this.mService.okToDisplay() || (this.mFrame.top == this.mLastFrame.top && this.mFrame.left == this.mLastFrame.left))) {
            return DEBUG_DISABLE_SAVING_SURFACES;
        }
        if (this.mAttachedWindow == null || !this.mAttachedWindow.hasMoved()) {
            return true;
        }
        return DEBUG_DISABLE_SAVING_SURFACES;
    }

    boolean isObscuringFullscreen(DisplayInfo displayInfo) {
        Task task = getTask();
        if ((task == null || task.mStack == null || task.mStack.isFullscreen()) && isOpaqueDrawn() && isFrameFullscreen(displayInfo)) {
            return true;
        }
        return DEBUG_DISABLE_SAVING_SURFACES;
    }

    boolean isFrameFullscreen(DisplayInfo displayInfo) {
        if (this.mFrame.left > 0 || this.mFrame.top > 0 || this.mFrame.right < displayInfo.appWidth || this.mFrame.bottom < displayInfo.appHeight) {
            return DEBUG_DISABLE_SAVING_SURFACES;
        }
        return true;
    }

    boolean isConfigChanged() {
        getMergedConfig(this.mTmpConfig);
        boolean configChanged = !this.mMergedConfiguration.equals(Configuration.EMPTY) ? this.mTmpConfig.diff(this.mMergedConfiguration) != 0 ? true : DEBUG_DISABLE_SAVING_SURFACES : true;
        if ((this.mAttrs.privateFlags & DumpState.DUMP_PROVIDERS) == 0) {
            return configChanged;
        }
        this.mConfigHasChanged |= configChanged;
        return this.mConfigHasChanged;
    }

    boolean isAdjustedForMinimizedDock() {
        if (this.mAppToken == null || this.mAppToken.mTask == null) {
            return DEBUG_DISABLE_SAVING_SURFACES;
        }
        return this.mAppToken.mTask.mStack.isAdjustedForMinimizedDock();
    }

    void removeLocked() {
        disposeInputChannel();
        if (isChildWindow()) {
            this.mAttachedWindow.mChildWindows.remove(this);
        }
        this.mWinAnimator.destroyDeferredSurfaceLocked();
        this.mWinAnimator.destroySurfaceLocked();
        this.mSession.windowRemovedLocked();
        try {
            this.mClient.asBinder().unlinkToDeath(this.mDeathRecipient, LOW_RESOLUTION_FEATURE_OFF);
        } catch (RuntimeException e) {
        }
    }

    void setHasSurface(boolean hasSurface) {
        this.mHasSurface = hasSurface;
    }

    int getAnimLayerAdjustment() {
        if (this.mTargetAppToken != null) {
            return this.mTargetAppToken.mAppAnimator.animLayerAdjustment;
        }
        if (this.mAppToken != null) {
            return this.mAppToken.mAppAnimator.animLayerAdjustment;
        }
        return LOW_RESOLUTION_FEATURE_OFF;
    }

    void scheduleAnimationIfDimming() {
        if (this.mDisplayContent != null) {
            DimLayerUser dimLayerUser = getDimLayerUser();
            if (dimLayerUser != null && this.mDisplayContent.mDimLayerController.isDimming(dimLayerUser, this.mWinAnimator)) {
                this.mService.scheduleAnimationLocked();
            }
        }
    }

    void notifyMovedInStack() {
        this.mJustMovedInStack = true;
    }

    boolean hasJustMovedInStack() {
        return this.mJustMovedInStack;
    }

    void resetJustMovedInStack() {
        this.mJustMovedInStack = DEBUG_DISABLE_SAVING_SURFACES;
    }

    void openInputChannel(InputChannel outInputChannel) {
        if (this.mInputChannel != null) {
            throw new IllegalStateException("Window already has an input channel.");
        }
        InputChannel[] inputChannels = InputChannel.openInputChannelPair(makeInputChannelName());
        this.mInputChannel = inputChannels[LOW_RESOLUTION_FEATURE_OFF];
        this.mClientChannel = inputChannels[LOW_RESOLUTION_COMPOSITION_OFF];
        this.mInputWindowHandle.inputChannel = inputChannels[LOW_RESOLUTION_FEATURE_OFF];
        if (outInputChannel != null) {
            this.mClientChannel.transferTo(outInputChannel);
            this.mClientChannel.dispose();
            this.mClientChannel = null;
        } else {
            this.mDeadWindowEventReceiver = new DeadWindowEventReceiver(this.mClientChannel);
        }
        this.mService.mInputManager.registerInputChannel(this.mInputChannel, this.mInputWindowHandle);
    }

    void disposeInputChannel() {
        if (this.mDeadWindowEventReceiver != null) {
            this.mDeadWindowEventReceiver.dispose();
            this.mDeadWindowEventReceiver = null;
        }
        if (this.mInputChannel != null) {
            this.mService.mInputManager.unregisterInputChannel(this.mInputChannel);
            this.mInputChannel.dispose();
            this.mInputChannel = null;
        }
        if (this.mClientChannel != null) {
            this.mClientChannel.dispose();
            this.mClientChannel = null;
        }
        this.mInputWindowHandle.inputChannel = null;
    }

    void applyDimLayerIfNeeded() {
        AppWindowToken token = this.mAppToken;
        if (token == null || !token.removed) {
            if (!this.mAnimatingExit && this.mAppDied) {
                this.mDisplayContent.mDimLayerController.applyDimAbove(getDimLayerUser(), this.mWinAnimator);
            } else if (!((this.mAttrs.flags & LOW_RESOLUTION_COMPOSITION_ON) == 0 || this.mDisplayContent == null || this.mAnimatingExit || !isVisibleUnchecked())) {
                this.mDisplayContent.mDimLayerController.applyDimBehind(getDimLayerUser(), this.mWinAnimator);
            }
        }
    }

    DimLayerUser getDimLayerUser() {
        Task task = getTask();
        if (task != null) {
            return task;
        }
        return getStack();
    }

    void maybeRemoveReplacedWindow() {
        if (this.mAppToken != null) {
            for (int i = this.mAppToken.allAppWindows.size() - 1; i >= 0; i--) {
                WindowState win = (WindowState) this.mAppToken.allAppWindows.get(i);
                if (win.mWillReplaceWindow && win.mReplacingWindow == this && hasDrawnLw()) {
                    if (win.isDimming()) {
                        win.transferDimToReplacement();
                    }
                    win.mWillReplaceWindow = DEBUG_DISABLE_SAVING_SURFACES;
                    boolean animateReplacingWindow = win.mAnimateReplacingWindow;
                    win.mAnimateReplacingWindow = DEBUG_DISABLE_SAVING_SURFACES;
                    win.mReplacingRemoveRequested = DEBUG_DISABLE_SAVING_SURFACES;
                    win.mReplacingWindow = null;
                    this.mSkipEnterAnimationForSeamlessReplacement = DEBUG_DISABLE_SAVING_SURFACES;
                    if (win.mAnimatingExit || !animateReplacingWindow) {
                        this.mService.removeWindowInnerLocked(win);
                    }
                }
            }
        }
    }

    void setDisplayLayoutNeeded() {
        if (this.mDisplayContent != null) {
            this.mDisplayContent.layoutNeeded = true;
        }
    }

    boolean inDockedWorkspace() {
        Task task = getTask();
        return task != null ? task.inDockedWorkspace() : DEBUG_DISABLE_SAVING_SURFACES;
    }

    boolean inPinnedWorkspace() {
        Task task = getTask();
        return task != null ? task.inPinnedWorkspace() : DEBUG_DISABLE_SAVING_SURFACES;
    }

    boolean isDockedInEffect() {
        Task task = getTask();
        return task != null ? task.isDockedInEffect() : DEBUG_DISABLE_SAVING_SURFACES;
    }

    void applyScrollIfNeeded() {
        Task task = getTask();
        if (task != null) {
            task.applyScrollToWindowIfNeeded(this);
        }
    }

    void applyAdjustForImeIfNeeded() {
        Task task = getTask();
        if (task != null && task.mStack != null && task.mStack.isAdjustedForIme()) {
            task.mStack.applyAdjustForImeIfNeeded(task);
        }
    }

    int getTouchableRegion(Region region, int flags) {
        boolean modal = DEBUG_DISABLE_SAVING_SURFACES;
        if ((flags & 40) == 0) {
            modal = true;
        }
        if (!modal || this.mAppToken == null) {
            getTouchableRegion(region);
        } else {
            flags |= 32;
            DimLayerUser dimLayerUser = getDimLayerUser();
            if (dimLayerUser != null) {
                dimLayerUser.getDimBounds(this.mTmpRect);
            } else {
                getVisibleBounds(this.mTmpRect);
            }
            if (inFreeformWorkspace()) {
                int delta = WindowManagerService.dipToPixel(RESIZE_HANDLE_WIDTH_IN_DP, getDisplayContent().getDisplayMetrics());
                this.mTmpRect.inset(-delta, -delta);
            }
            region.set(this.mTmpRect);
            cropRegionToStackBoundsIfNeeded(region);
        }
        return flags;
    }

    void checkPolicyVisibilityChange() {
        if (this.mPolicyVisibility != this.mPolicyVisibilityAfterAnim) {
            this.mPolicyVisibility = this.mPolicyVisibilityAfterAnim;
            setDisplayLayoutNeeded();
            if (!this.mPolicyVisibility) {
                if (this.mService.mCurrentFocus == this) {
                    this.mService.mFocusMayChange = true;
                }
                this.mService.enableScreenIfNeededLocked();
            }
        }
    }

    void setRequestedSize(int requestedWidth, int requestedHeight) {
        if (this.mRequestedWidth != requestedWidth || this.mRequestedHeight != requestedHeight) {
            this.mLayoutNeeded = true;
            this.mRequestedWidth = requestedWidth;
            this.mRequestedHeight = requestedHeight;
        }
    }

    void prepareWindowToDisplayDuringRelayout(Configuration outConfig) {
        if ((this.mAttrs.softInputMode & 240) == 16) {
            this.mLayoutNeeded = true;
        }
        if (isDrawnLw() && this.mService.okToDisplay()) {
            this.mWinAnimator.applyEnterAnimationLocked();
        }
        if ((this.mAttrs.flags & 2097152) != 0) {
            this.mTurnOnScreen = true;
        }
        if (isConfigChanged()) {
            outConfig.setTo(updateConfiguration());
        }
    }

    void adjustStartingWindowFlags() {
        if (this.mAttrs.type == LOW_RESOLUTION_COMPOSITION_OFF && this.mAppToken != null && this.mAppToken.startingWindow != null) {
            LayoutParams sa = this.mAppToken.startingWindow.mAttrs;
            sa.flags = (sa.flags & -4718594) | (this.mAttrs.flags & 4718593);
        }
    }

    void setWindowScale(int requestedWidth, int requestedHeight) {
        boolean scaledWindow = DEBUG_DISABLE_SAVING_SURFACES;
        float f = 1.0f;
        if ((this.mAttrs.flags & DumpState.DUMP_KEYSETS) != 0) {
            scaledWindow = true;
        }
        if (scaledWindow) {
            float f2;
            if (this.mAttrs.width != requestedWidth) {
                f2 = ((float) this.mAttrs.width) / ((float) requestedWidth);
            } else {
                f2 = 1.0f;
            }
            this.mHScale = f2;
            if (this.mAttrs.height != requestedHeight) {
                f = ((float) this.mAttrs.height) / ((float) requestedHeight);
            }
            this.mVScale = f;
            return;
        }
        this.mVScale = 1.0f;
        this.mHScale = 1.0f;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    boolean shouldKeepVisibleDeadAppWindow() {
        boolean z = DEBUG_DISABLE_SAVING_SURFACES;
        if (!isWinVisibleLw() || this.mAppToken == null || this.mAppToken.clientHidden || this.mAttrs.token != this.mClient.asBinder() || this.mAttrs.type == 3) {
            return DEBUG_DISABLE_SAVING_SURFACES;
        }
        TaskStack stack = getStack();
        if (stack != null) {
            z = StackId.keepVisibleDeadAppWindowOnScreen(stack.mStackId);
        }
        return z;
    }

    boolean canReceiveKeys() {
        if (!isVisibleOrAdding() || this.mViewVisibility != 0 || this.mRemoveOnExit || (this.mAttrs.flags & 8) != 0) {
            return DEBUG_DISABLE_SAVING_SURFACES;
        }
        if ((this.mAppToken == null || this.mAppToken.windowsAreFocusable()) && !isAdjustedForMinimizedDock()) {
            return true;
        }
        return DEBUG_DISABLE_SAVING_SURFACES;
    }

    public boolean hasDrawnLw() {
        return this.mWinAnimator.mDrawState == 4 ? true : DEBUG_DISABLE_SAVING_SURFACES;
    }

    public boolean showLw(boolean doAnimation) {
        return showLw(doAnimation, true);
    }

    boolean showLw(boolean doAnimation, boolean requestAnim) {
        if (isHiddenFromUserLocked() || !this.mAppOpVisibility) {
            return DEBUG_DISABLE_SAVING_SURFACES;
        }
        if (this.mPolicyVisibility && this.mPolicyVisibilityAfterAnim) {
            return DEBUG_DISABLE_SAVING_SURFACES;
        }
        if (doAnimation) {
            if (!this.mService.okToDisplay()) {
                doAnimation = DEBUG_DISABLE_SAVING_SURFACES;
            } else if (this.mPolicyVisibility && this.mWinAnimator.mAnimation == null) {
                doAnimation = DEBUG_DISABLE_SAVING_SURFACES;
            }
        }
        this.mPolicyVisibility = true;
        this.mPolicyVisibilityAfterAnim = true;
        if (doAnimation) {
            this.mWinAnimator.applyAnimationLocked(LOW_RESOLUTION_COMPOSITION_OFF, true);
        }
        if (requestAnim) {
            this.mService.scheduleAnimationLocked();
        }
        return true;
    }

    public boolean hideLw(boolean doAnimation) {
        return hideLw(doAnimation, true);
    }

    boolean hideLw(boolean doAnimation, boolean requestAnim) {
        boolean current;
        if (doAnimation && !this.mService.okToDisplay()) {
            doAnimation = DEBUG_DISABLE_SAVING_SURFACES;
        }
        if (doAnimation) {
            current = this.mPolicyVisibilityAfterAnim;
        } else {
            current = this.mPolicyVisibility;
        }
        if (!current) {
            return DEBUG_DISABLE_SAVING_SURFACES;
        }
        if (doAnimation) {
            this.mWinAnimator.applyAnimationLocked(LOW_RESOLUTION_COMPOSITION_ON, DEBUG_DISABLE_SAVING_SURFACES);
            if (this.mWinAnimator.mAnimation == null) {
                doAnimation = DEBUG_DISABLE_SAVING_SURFACES;
            }
        }
        if (doAnimation) {
            this.mPolicyVisibilityAfterAnim = DEBUG_DISABLE_SAVING_SURFACES;
        } else {
            this.mPolicyVisibilityAfterAnim = DEBUG_DISABLE_SAVING_SURFACES;
            this.mPolicyVisibility = DEBUG_DISABLE_SAVING_SURFACES;
            this.mService.enableScreenIfNeededLocked();
            if (this.mService.mCurrentFocus == this) {
                this.mService.mFocusMayChange = true;
            }
        }
        if (requestAnim) {
            this.mService.scheduleAnimationLocked();
        }
        return true;
    }

    public void setAppOpVisibilityLw(boolean state) {
        if (this.mAppOpVisibility != state) {
            this.mAppOpVisibility = state;
            if (state) {
                showLw(true, true);
            } else {
                hideLw(true, true);
            }
        }
    }

    public void pokeDrawLockLw(long timeout) {
        if (isVisibleOrAdding()) {
            if (this.mDrawLock == null) {
                this.mDrawLock = this.mService.mPowerManager.newWakeLock(DumpState.DUMP_PACKAGES, "Window:" + getWindowTag());
                this.mDrawLock.setReferenceCounted(DEBUG_DISABLE_SAVING_SURFACES);
                this.mDrawLock.setWorkSource(new WorkSource(this.mOwnerUid, this.mAttrs.packageName));
            }
            this.mDrawLock.acquire(timeout);
        }
    }

    public boolean isAlive() {
        return this.mClient.asBinder().isBinderAlive();
    }

    boolean isClosing() {
        return !this.mAnimatingExit ? this.mService.mClosingApps.contains(this.mAppToken) : true;
    }

    boolean isAnimatingWithSavedSurface() {
        return this.mAnimatingWithSavedSurface;
    }

    boolean isAnimatingInvisibleWithSavedSurface() {
        if (this.mAnimatingWithSavedSurface) {
            return this.mViewVisibility == 0 ? this.mWindowRemovalAllowed : true;
        } else {
            return DEBUG_DISABLE_SAVING_SURFACES;
        }
    }

    public void setVisibleBeforeClientHidden() {
        this.mWasVisibleBeforeClientHidden = (this.mViewVisibility != 0 ? this.mAnimatingWithSavedSurface : LOW_RESOLUTION_COMPOSITION_OFF) | this.mWasVisibleBeforeClientHidden;
    }

    public void clearVisibleBeforeClientHidden() {
        this.mWasVisibleBeforeClientHidden = DEBUG_DISABLE_SAVING_SURFACES;
    }

    public boolean wasVisibleBeforeClientHidden() {
        return this.mWasVisibleBeforeClientHidden;
    }

    private boolean shouldSaveSurface() {
        if (this.mWinAnimator.mSurfaceController == null) {
            return DEBUG_DISABLE_SAVING_SURFACES;
        }
        if ((this.mAttrs.hwFlags & LOW_RESOLUTION_COMPOSITION_ON) == LOW_RESOLUTION_COMPOSITION_ON) {
            Slog.v(TAG, "dont Save surface because FLAG_DESTORY_SURFACE");
            return DEBUG_DISABLE_SAVING_SURFACES;
        } else if (!this.mWasVisibleBeforeClientHidden || (this.mAttrs.flags & DumpState.DUMP_PREFERRED_XML) != 0 || SystemProperties.getBoolean("ro.build.hw_emui_lite.enable", DEBUG_DISABLE_SAVING_SURFACES) || SystemProperties.getBoolean("ro.config.hw_disable_surface", true) || ActivityManager.isLowRamDeviceStatic()) {
            return DEBUG_DISABLE_SAVING_SURFACES;
        } else {
            Task task = getTask();
            if (task == null || task.inHomeStack()) {
                return DEBUG_DISABLE_SAVING_SURFACES;
            }
            AppWindowToken taskTop = task.getTopVisibleAppToken();
            if ((taskTop == null || taskTop == this.mAppToken) && !this.mResizedWhileGone) {
                return this.mAppToken.shouldSaveSurface();
            }
            return DEBUG_DISABLE_SAVING_SURFACES;
        }
    }

    void destroyOrSaveSurface() {
        this.mSurfaceSaved = shouldSaveSurface();
        if (this.mSurfaceSaved) {
            this.mSession.setTransparentRegion(this.mClient, sEmptyRegion);
            this.mWinAnimator.hide("saved surface");
            this.mWinAnimator.mDrawState = LOW_RESOLUTION_FEATURE_OFF;
            setHasSurface(DEBUG_DISABLE_SAVING_SURFACES);
            if (this.mWinAnimator.mSurfaceController != null) {
                this.mWinAnimator.mSurfaceController.disconnectInTransaction();
            }
            this.mAnimatingWithSavedSurface = DEBUG_DISABLE_SAVING_SURFACES;
        } else {
            this.mWinAnimator.destroySurfaceLocked();
        }
        this.mAnimatingExit = DEBUG_DISABLE_SAVING_SURFACES;
    }

    void destroySavedSurface() {
        if (this.mSurfaceSaved) {
            this.mWinAnimator.destroySurfaceLocked();
        }
        this.mWasVisibleBeforeClientHidden = DEBUG_DISABLE_SAVING_SURFACES;
    }

    void restoreSavedSurface() {
        if (this.mSurfaceSaved) {
            this.mSurfaceSaved = DEBUG_DISABLE_SAVING_SURFACES;
            if (this.mWinAnimator.mSurfaceController != null) {
                setHasSurface(true);
                this.mWinAnimator.mDrawState = 3;
                this.mAnimatingWithSavedSurface = true;
            } else {
                Slog.wtf(TAG, "Failed to restore saved surface: surface gone! " + this);
            }
        }
    }

    boolean canRestoreSurface() {
        return this.mWasVisibleBeforeClientHidden ? this.mSurfaceSaved : DEBUG_DISABLE_SAVING_SURFACES;
    }

    boolean hasSavedSurface() {
        return this.mSurfaceSaved;
    }

    void clearHasSavedSurface() {
        this.mSurfaceSaved = DEBUG_DISABLE_SAVING_SURFACES;
        this.mAnimatingWithSavedSurface = DEBUG_DISABLE_SAVING_SURFACES;
        if (this.mWasVisibleBeforeClientHidden) {
            this.mAppToken.destroySavedSurfaces();
        }
    }

    boolean clearAnimatingWithSavedSurface() {
        if (!this.mAnimatingWithSavedSurface) {
            return DEBUG_DISABLE_SAVING_SURFACES;
        }
        this.mAnimatingWithSavedSurface = DEBUG_DISABLE_SAVING_SURFACES;
        return true;
    }

    public boolean isDefaultDisplay() {
        DisplayContent displayContent = getDisplayContent();
        if (displayContent == null) {
            return DEBUG_DISABLE_SAVING_SURFACES;
        }
        return displayContent.isDefaultDisplay;
    }

    public boolean isDimming() {
        DimLayerUser dimLayerUser = getDimLayerUser();
        if (dimLayerUser == null || this.mDisplayContent == null) {
            return DEBUG_DISABLE_SAVING_SURFACES;
        }
        return this.mDisplayContent.mDimLayerController.isDimming(dimLayerUser, this.mWinAnimator);
    }

    public void setShowToOwnerOnlyLocked(boolean showToOwnerOnly) {
        this.mShowToOwnerOnly = showToOwnerOnly;
    }

    boolean isHiddenFromUserLocked() {
        boolean z = DEBUG_DISABLE_SAVING_SURFACES;
        WindowState win = this;
        while (win.isChildWindow()) {
            win = win.mAttachedWindow;
        }
        if (win.mAttrs.type < IHwShutdownThread.SHUTDOWN_ANIMATION_WAIT_TIME && win.mAppToken != null && win.mAppToken.showForAllUsers && win.mFrame.left <= win.mDisplayFrame.left && win.mFrame.top <= win.mDisplayFrame.top && win.mFrame.right >= win.mStableFrame.right && win.mFrame.bottom >= win.mStableFrame.bottom) {
            return DEBUG_DISABLE_SAVING_SURFACES;
        }
        if (win.mShowToOwnerOnly && !this.mService.isCurrentProfileLocked(UserHandle.getUserId(win.mOwnerUid))) {
            z = true;
        }
        return z;
    }

    private static void applyInsets(Region outRegion, Rect frame, Rect inset) {
        outRegion.set(frame.left + inset.left, frame.top + inset.top, frame.right - inset.right, frame.bottom - inset.bottom);
    }

    void getTouchableRegion(Region outRegion) {
        Rect frame = this.mFrame;
        switch (this.mTouchableInsets) {
            case LOW_RESOLUTION_COMPOSITION_OFF /*1*/:
                applyInsets(outRegion, frame, this.mGivenContentInsets);
                break;
            case LOW_RESOLUTION_COMPOSITION_ON /*2*/:
                applyInsets(outRegion, frame, this.mGivenVisibleInsets);
                break;
            case H.REPORT_LOSING_FOCUS /*3*/:
                outRegion.set(this.mGivenTouchableRegion);
                outRegion.translate(frame.left, frame.top);
                break;
            default:
                outRegion.set(frame);
                break;
        }
        cropRegionToStackBoundsIfNeeded(outRegion);
    }

    void cropRegionToStackBoundsIfNeeded(Region region) {
        Task task = getTask();
        if (task != null && task.cropWindowsToStackBounds()) {
            TaskStack stack = task.mStack;
            if (stack != null) {
                stack.getDimBounds(this.mTmpRect);
                region.op(this.mTmpRect, Op.INTERSECT);
            }
        }
    }

    WindowList getWindowList() {
        DisplayContent displayContent = getDisplayContent();
        if (displayContent == null) {
            return null;
        }
        return displayContent.getWindowList();
    }

    public void reportFocusChangedSerialized(boolean focused, boolean inTouchMode) {
        try {
            this.mClient.windowFocusChanged(focused, inTouchMode);
        } catch (RemoteException e) {
        }
        if (this.mFocusCallbacks != null) {
            int N = this.mFocusCallbacks.beginBroadcast();
            for (int i = LOW_RESOLUTION_FEATURE_OFF; i < N; i += LOW_RESOLUTION_COMPOSITION_OFF) {
                IWindowFocusObserver obs = (IWindowFocusObserver) this.mFocusCallbacks.getBroadcastItem(i);
                if (focused) {
                    try {
                        obs.focusGained(this.mWindowId.asBinder());
                    } catch (RemoteException e2) {
                    }
                } else {
                    obs.focusLost(this.mWindowId.asBinder());
                }
            }
            this.mFocusCallbacks.finishBroadcast();
        }
    }

    private Configuration updateConfiguration() {
        boolean configChanged = isConfigChanged();
        getMergedConfig(this.mMergedConfiguration);
        this.mConfigHasChanged = DEBUG_DISABLE_SAVING_SURFACES;
        return this.mMergedConfiguration;
    }

    private void getMergedConfig(Configuration outConfig) {
        if (this.mAppToken == null || this.mAppToken.mFrozenMergedConfig.size() <= 0) {
            Configuration overrideConfig;
            Task task = getTask();
            if (task != null) {
                overrideConfig = task.mOverrideConfig;
            } else {
                overrideConfig = Configuration.EMPTY;
            }
            outConfig.setTo(this.mService.mCurConfiguration);
            if (overrideConfig != Configuration.EMPTY) {
                outConfig.updateFrom(overrideConfig);
            }
            return;
        }
        outConfig.setTo((Configuration) this.mAppToken.mFrozenMergedConfig.peek());
    }

    void reportResized() {
        Trace.traceBegin(32, "wm.reportResized_" + getWindowTag());
        try {
            Configuration updateConfiguration = isConfigChanged() ? updateConfiguration() : null;
            Rect frame = this.mFrame;
            Rect overscanInsets = this.mLastOverscanInsets;
            Rect contentInsets = this.mLastContentInsets;
            Rect visibleInsets = this.mLastVisibleInsets;
            Rect stableInsets = this.mLastStableInsets;
            Rect outsets = this.mLastOutsets;
            boolean reportDraw = this.mWinAnimator.mDrawState == LOW_RESOLUTION_COMPOSITION_OFF ? true : DEBUG_DISABLE_SAVING_SURFACES;
            if (this.mAttrs.type == 3 || !(this.mClient instanceof IWindow.Stub)) {
                dispatchResized(frame, overscanInsets, contentInsets, visibleInsets, stableInsets, outsets, reportDraw, updateConfiguration);
            } else {
                this.mService.mH.post(new AnonymousClass2(frame, overscanInsets, contentInsets, visibleInsets, stableInsets, outsets, reportDraw, updateConfiguration));
            }
            if (this.mService.mAccessibilityController != null && getDisplayId() == 0) {
                this.mService.mAccessibilityController.onSomeWindowResizedOrMovedLocked();
            }
            this.mOverscanInsetsChanged = DEBUG_DISABLE_SAVING_SURFACES;
            this.mContentInsetsChanged = DEBUG_DISABLE_SAVING_SURFACES;
            this.mVisibleInsetsChanged = DEBUG_DISABLE_SAVING_SURFACES;
            this.mStableInsetsChanged = DEBUG_DISABLE_SAVING_SURFACES;
            this.mOutsetsChanged = DEBUG_DISABLE_SAVING_SURFACES;
            this.mResizedWhileNotDragResizingReported = true;
            this.mWinAnimator.mSurfaceResized = DEBUG_DISABLE_SAVING_SURFACES;
        } catch (RemoteException e) {
            this.mOrientationChanging = DEBUG_DISABLE_SAVING_SURFACES;
            this.mLastFreezeDuration = (int) (SystemClock.elapsedRealtime() - this.mService.mDisplayFreezeTime);
            this.mOverscanInsetsChanged = DEBUG_DISABLE_SAVING_SURFACES;
            this.mContentInsetsChanged = DEBUG_DISABLE_SAVING_SURFACES;
            this.mVisibleInsetsChanged = DEBUG_DISABLE_SAVING_SURFACES;
            this.mStableInsetsChanged = DEBUG_DISABLE_SAVING_SURFACES;
            this.mWinAnimator.mSurfaceResized = DEBUG_DISABLE_SAVING_SURFACES;
            Slog.w(TAG, "Failed to report 'resized' to the client of " + this + ", removing this window.");
            this.mService.mPendingRemove.add(this);
            this.mService.mWindowPlacerLocked.requestTraversal();
        }
        Trace.traceEnd(32);
    }

    Rect getBackdropFrame(Rect frame) {
        boolean isDragResizeChanged = !isDragResizing() ? isDragResizeChanged() : true;
        if (StackId.useWindowFrameForBackdrop(getStackId()) || !isDragResizeChanged) {
            return frame;
        }
        DisplayInfo displayInfo = getDisplayInfo();
        this.mTmpRect.set(LOW_RESOLUTION_FEATURE_OFF, LOW_RESOLUTION_FEATURE_OFF, displayInfo.logicalWidth, displayInfo.logicalHeight);
        return this.mTmpRect;
    }

    public int getStackId() {
        TaskStack stack = getStack();
        if (stack == null) {
            return -1;
        }
        return stack.mStackId;
    }

    private void dispatchResized(Rect frame, Rect overscanInsets, Rect contentInsets, Rect visibleInsets, Rect stableInsets, Rect outsets, boolean reportDraw, Configuration newConfig) throws RemoteException {
        this.mClient.resized(frame, overscanInsets, contentInsets, visibleInsets, stableInsets, outsets, reportDraw, newConfig, getBackdropFrame(frame), !isDragResizeChanged() ? this.mResizedWhileNotDragResizing : true, this.mPolicy.isNavBarForcedShownLw(this));
        this.mDragResizingChangeReported = true;
    }

    public void registerFocusObserver(IWindowFocusObserver observer) {
        synchronized (this.mService.mWindowMap) {
            if (this.mFocusCallbacks == null) {
                this.mFocusCallbacks = new RemoteCallbackList();
            }
            this.mFocusCallbacks.register(observer);
        }
    }

    public void unregisterFocusObserver(IWindowFocusObserver observer) {
        synchronized (this.mService.mWindowMap) {
            if (this.mFocusCallbacks != null) {
                this.mFocusCallbacks.unregister(observer);
            }
        }
    }

    public boolean isFocused() {
        boolean z;
        synchronized (this.mService.mWindowMap) {
            z = this.mService.mCurrentFocus == this ? true : DEBUG_DISABLE_SAVING_SURFACES;
        }
        return z;
    }

    boolean inFreeformWorkspace() {
        Task task = getTask();
        return task != null ? task.inFreeformWorkspace() : DEBUG_DISABLE_SAVING_SURFACES;
    }

    public boolean isInMultiWindowMode() {
        Task task = getTask();
        if (task == null || task.isFullscreen()) {
            return DEBUG_DISABLE_SAVING_SURFACES;
        }
        return true;
    }

    boolean isDragResizeChanged() {
        return this.mDragResizing != computeDragResizing() ? true : DEBUG_DISABLE_SAVING_SURFACES;
    }

    boolean isDragResizingChangeReported() {
        return this.mDragResizingChangeReported;
    }

    void resetDragResizingChangeReported() {
        this.mDragResizingChangeReported = DEBUG_DISABLE_SAVING_SURFACES;
    }

    void setResizedWhileNotDragResizing(boolean resizedWhileNotDragResizing) {
        this.mResizedWhileNotDragResizing = resizedWhileNotDragResizing;
        this.mResizedWhileNotDragResizingReported = resizedWhileNotDragResizing ? DEBUG_DISABLE_SAVING_SURFACES : true;
    }

    boolean isResizedWhileNotDragResizing() {
        return this.mResizedWhileNotDragResizing;
    }

    boolean isResizedWhileNotDragResizingReported() {
        return this.mResizedWhileNotDragResizingReported;
    }

    int getResizeMode() {
        return this.mResizeMode;
    }

    boolean computeDragResizing() {
        boolean z = DEBUG_DISABLE_SAVING_SURFACES;
        Task task = getTask();
        if (task == null || this.mAttrs.width != -1 || this.mAttrs.height != -1) {
            return DEBUG_DISABLE_SAVING_SURFACES;
        }
        if (task.isDragResizing()) {
            return true;
        }
        if (!((!this.mDisplayContent.mDividerControllerLocked.isResizing() && (this.mAppToken == null || this.mAppToken.mFrozenBounds.isEmpty())) || task.inFreeformWorkspace() || isGoneForLayoutLw())) {
            z = true;
        }
        return z;
    }

    void setDragResizing() {
        boolean resizing = computeDragResizing();
        if (resizing != this.mDragResizing) {
            this.mDragResizing = resizing;
            Task task = getTask();
            if (task == null || !task.isDragResizing()) {
                int i;
                if (this.mDragResizing && this.mDisplayContent.mDividerControllerLocked.isResizing()) {
                    i = LOW_RESOLUTION_COMPOSITION_OFF;
                } else {
                    i = LOW_RESOLUTION_FEATURE_OFF;
                }
                this.mResizeMode = i;
            } else {
                this.mResizeMode = task.getDragResizeMode();
            }
        }
    }

    boolean isDragResizing() {
        return this.mDragResizing;
    }

    boolean isDockedResizing() {
        return (this.mDragResizing && getResizeMode() == LOW_RESOLUTION_COMPOSITION_OFF) ? true : DEBUG_DISABLE_SAVING_SURFACES;
    }

    void dump(PrintWriter pw, String prefix, boolean dumpAll) {
        int i = LOW_RESOLUTION_FEATURE_OFF;
        TaskStack stack = getStack();
        pw.print(prefix);
        pw.print("mDisplayId=");
        pw.print(getDisplayId());
        if (stack != null) {
            pw.print(" stackId=");
            pw.print(stack.mStackId);
        }
        if (this.mNotOnAppsDisplay) {
            pw.print(" mNotOnAppsDisplay=");
            pw.print(this.mNotOnAppsDisplay);
        }
        pw.print(" mSession=");
        pw.print(this.mSession);
        pw.print(" mClient=");
        pw.println(this.mClient.asBinder());
        pw.print(prefix);
        pw.print("mOwnerUid=");
        pw.print(this.mOwnerUid);
        pw.print(" mShowToOwnerOnly=");
        pw.print(this.mShowToOwnerOnly);
        pw.print(" package=");
        pw.print(this.mAttrs.packageName);
        pw.print(" appop=");
        pw.println(AppOpsManager.opToName(this.mAppOp));
        pw.print(prefix);
        pw.print("mAttrs=");
        pw.println(this.mAttrs);
        pw.print(prefix);
        pw.print("Requested w=");
        pw.print(this.mRequestedWidth);
        pw.print(" h=");
        pw.print(this.mRequestedHeight);
        pw.print(" mLayoutSeq=");
        pw.println(this.mLayoutSeq);
        if (!(this.mRequestedWidth == this.mLastRequestedWidth && this.mRequestedHeight == this.mLastRequestedHeight)) {
            pw.print(prefix);
            pw.print("LastRequested w=");
            pw.print(this.mLastRequestedWidth);
            pw.print(" h=");
            pw.println(this.mLastRequestedHeight);
        }
        if (isChildWindow() || this.mLayoutAttached) {
            pw.print(prefix);
            pw.print("mAttachedWindow=");
            pw.print(this.mAttachedWindow);
            pw.print(" mLayoutAttached=");
            pw.println(this.mLayoutAttached);
        }
        if (this.mIsImWindow || this.mIsWallpaper || this.mIsFloatingLayer) {
            pw.print(prefix);
            pw.print("mIsImWindow=");
            pw.print(this.mIsImWindow);
            pw.print(" mIsWallpaper=");
            pw.print(this.mIsWallpaper);
            pw.print(" mIsFloatingLayer=");
            pw.print(this.mIsFloatingLayer);
            pw.print(" mWallpaperVisible=");
            pw.println(this.mWallpaperVisible);
        }
        if (dumpAll) {
            pw.print(prefix);
            pw.print("mBaseLayer=");
            pw.print(this.mBaseLayer);
            pw.print(" mSubLayer=");
            pw.print(this.mSubLayer);
            pw.print(" mAnimLayer=");
            pw.print(this.mLayer);
            pw.print("+");
            if (this.mTargetAppToken != null) {
                i = this.mTargetAppToken.mAppAnimator.animLayerAdjustment;
            } else if (this.mAppToken != null) {
                i = this.mAppToken.mAppAnimator.animLayerAdjustment;
            }
            pw.print(i);
            pw.print("=");
            pw.print(this.mWinAnimator.mAnimLayer);
            pw.print(" mLastLayer=");
            pw.println(this.mWinAnimator.mLastLayer);
        }
        if (dumpAll) {
            pw.print(prefix);
            pw.print("mToken=");
            pw.println(this.mToken);
            pw.print(prefix);
            pw.print("mRootToken=");
            pw.println(this.mRootToken);
            if (this.mAppToken != null) {
                pw.print(prefix);
                pw.print("mAppToken=");
                pw.println(this.mAppToken);
                pw.print(prefix);
                pw.print(" isAnimatingWithSavedSurface()=");
                pw.print(isAnimatingWithSavedSurface());
                pw.print(" mAppDied=");
                pw.println(this.mAppDied);
            }
            if (this.mTargetAppToken != null) {
                pw.print(prefix);
                pw.print("mTargetAppToken=");
                pw.println(this.mTargetAppToken);
            }
            pw.print(prefix);
            pw.print("mViewVisibility=0x");
            pw.print(Integer.toHexString(this.mViewVisibility));
            pw.print(" mHaveFrame=");
            pw.print(this.mHaveFrame);
            pw.print(" mObscured=");
            pw.println(this.mObscured);
            pw.print(prefix);
            pw.print("mSeq=");
            pw.print(this.mSeq);
            pw.print(" mSystemUiVisibility=0x");
            pw.println(Integer.toHexString(this.mSystemUiVisibility));
        }
        if (!(this.mPolicyVisibility && this.mPolicyVisibilityAfterAnim && this.mAppOpVisibility && !this.mAttachedHidden)) {
            pw.print(prefix);
            pw.print("mPolicyVisibility=");
            pw.print(this.mPolicyVisibility);
            pw.print(" mPolicyVisibilityAfterAnim=");
            pw.print(this.mPolicyVisibilityAfterAnim);
            pw.print(" mAppOpVisibility=");
            pw.print(this.mAppOpVisibility);
            pw.print(" mAttachedHidden=");
            pw.println(this.mAttachedHidden);
        }
        if (!this.mRelayoutCalled || this.mLayoutNeeded) {
            pw.print(prefix);
            pw.print("mRelayoutCalled=");
            pw.print(this.mRelayoutCalled);
            pw.print(" mLayoutNeeded=");
            pw.println(this.mLayoutNeeded);
        }
        if (!(this.mXOffset == 0 && this.mYOffset == 0)) {
            pw.print(prefix);
            pw.print("Offsets x=");
            pw.print(this.mXOffset);
            pw.print(" y=");
            pw.println(this.mYOffset);
        }
        if (dumpAll) {
            pw.print(prefix);
            pw.print("mGivenContentInsets=");
            this.mGivenContentInsets.printShortString(pw);
            pw.print(" mGivenVisibleInsets=");
            this.mGivenVisibleInsets.printShortString(pw);
            pw.println();
            if (this.mTouchableInsets != 0 || this.mGivenInsetsPending) {
                pw.print(prefix);
                pw.print("mTouchableInsets=");
                pw.print(this.mTouchableInsets);
                pw.print(" mGivenInsetsPending=");
                pw.println(this.mGivenInsetsPending);
                Region region = new Region();
                getTouchableRegion(region);
                pw.print(prefix);
                pw.print("touchable region=");
                pw.println(region);
            }
            pw.print(prefix);
            pw.print("mMergedConfiguration=");
            pw.println(this.mMergedConfiguration);
        }
        pw.print(prefix);
        pw.print("mHasSurface=");
        pw.print(this.mHasSurface);
        pw.print(" mShownPosition=");
        this.mShownPosition.printShortString(pw);
        pw.print(" isReadyForDisplay()=");
        pw.print(isReadyForDisplay());
        pw.print(" hasSavedSurface()=");
        pw.print(hasSavedSurface());
        pw.print(" mWindowRemovalAllowed=");
        pw.println(this.mWindowRemovalAllowed);
        if (dumpAll) {
            pw.print(prefix);
            pw.print("mFrame=");
            this.mFrame.printShortString(pw);
            pw.print(" last=");
            this.mLastFrame.printShortString(pw);
            pw.println();
        }
        if (this.mEnforceSizeCompat) {
            pw.print(prefix);
            pw.print("mCompatFrame=");
            this.mCompatFrame.printShortString(pw);
            pw.println();
        }
        if (dumpAll) {
            pw.print(prefix);
            pw.print("Frames: containing=");
            this.mContainingFrame.printShortString(pw);
            pw.print(" parent=");
            this.mParentFrame.printShortString(pw);
            pw.println();
            pw.print(prefix);
            pw.print("    display=");
            this.mDisplayFrame.printShortString(pw);
            pw.print(" overscan=");
            this.mOverscanFrame.printShortString(pw);
            pw.println();
            pw.print(prefix);
            pw.print("    content=");
            this.mContentFrame.printShortString(pw);
            pw.print(" visible=");
            this.mVisibleFrame.printShortString(pw);
            pw.println();
            pw.print(prefix);
            pw.print("    decor=");
            this.mDecorFrame.printShortString(pw);
            pw.println();
            pw.print(prefix);
            pw.print("    outset=");
            this.mOutsetFrame.printShortString(pw);
            pw.println();
            pw.print(prefix);
            pw.print("Cur insets: overscan=");
            this.mOverscanInsets.printShortString(pw);
            pw.print(" content=");
            this.mContentInsets.printShortString(pw);
            pw.print(" visible=");
            this.mVisibleInsets.printShortString(pw);
            pw.print(" stable=");
            this.mStableInsets.printShortString(pw);
            pw.print(" surface=");
            this.mAttrs.surfaceInsets.printShortString(pw);
            pw.print(" outsets=");
            this.mOutsets.printShortString(pw);
            pw.println();
            pw.print(prefix);
            pw.print("Lst insets: overscan=");
            this.mLastOverscanInsets.printShortString(pw);
            pw.print(" content=");
            this.mLastContentInsets.printShortString(pw);
            pw.print(" visible=");
            this.mLastVisibleInsets.printShortString(pw);
            pw.print(" stable=");
            this.mLastStableInsets.printShortString(pw);
            pw.print(" physical=");
            this.mLastOutsets.printShortString(pw);
            pw.print(" outset=");
            this.mLastOutsets.printShortString(pw);
            pw.println();
        }
        pw.print(prefix);
        pw.print(this.mWinAnimator);
        pw.println(":");
        this.mWinAnimator.dump(pw, prefix + "  ", dumpAll);
        if (this.mAnimatingExit || this.mRemoveOnExit || this.mDestroying || this.mRemoved) {
            pw.print(prefix);
            pw.print("mAnimatingExit=");
            pw.print(this.mAnimatingExit);
            pw.print(" mRemoveOnExit=");
            pw.print(this.mRemoveOnExit);
            pw.print(" mDestroying=");
            pw.print(this.mDestroying);
            pw.print(" mRemoved=");
            pw.println(this.mRemoved);
        }
        if (this.mOrientationChanging || this.mAppFreezing || this.mTurnOnScreen) {
            pw.print(prefix);
            pw.print("mOrientationChanging=");
            pw.print(this.mOrientationChanging);
            pw.print(" mAppFreezing=");
            pw.print(this.mAppFreezing);
            pw.print(" mTurnOnScreen=");
            pw.println(this.mTurnOnScreen);
        }
        if (this.mLastFreezeDuration != 0) {
            pw.print(prefix);
            pw.print("mLastFreezeDuration=");
            TimeUtils.formatDuration((long) this.mLastFreezeDuration, pw);
            pw.println();
        }
        if (!(this.mHScale == 1.0f && this.mVScale == 1.0f)) {
            pw.print(prefix);
            pw.print("mHScale=");
            pw.print(this.mHScale);
            pw.print(" mVScale=");
            pw.println(this.mVScale);
        }
        if (!(this.mWallpaperX == -1.0f && this.mWallpaperY == -1.0f)) {
            pw.print(prefix);
            pw.print("mWallpaperX=");
            pw.print(this.mWallpaperX);
            pw.print(" mWallpaperY=");
            pw.println(this.mWallpaperY);
        }
        if (!(this.mWallpaperXStep == -1.0f && this.mWallpaperYStep == -1.0f)) {
            pw.print(prefix);
            pw.print("mWallpaperXStep=");
            pw.print(this.mWallpaperXStep);
            pw.print(" mWallpaperYStep=");
            pw.println(this.mWallpaperYStep);
        }
        if (!(this.mWallpaperDisplayOffsetX == UsbAudioDevice.kAudioDeviceMeta_Alsa && this.mWallpaperDisplayOffsetY == UsbAudioDevice.kAudioDeviceMeta_Alsa)) {
            pw.print(prefix);
            pw.print("mWallpaperDisplayOffsetX=");
            pw.print(this.mWallpaperDisplayOffsetX);
            pw.print(" mWallpaperDisplayOffsetY=");
            pw.println(this.mWallpaperDisplayOffsetY);
        }
        if (this.mDrawLock != null) {
            pw.print(prefix);
            pw.println("mDrawLock=" + this.mDrawLock);
        }
        if (isDragResizing()) {
            pw.print(prefix);
            pw.println("isDragResizing=" + isDragResizing());
        }
        if (computeDragResizing()) {
            pw.print(prefix);
            pw.println("computeDragResizing=" + computeDragResizing());
        }
    }

    String makeInputChannelName() {
        return Integer.toHexString(System.identityHashCode(this)) + " " + getWindowTag();
    }

    CharSequence getWindowTag() {
        CharSequence tag = this.mAttrs.getTitle();
        if (tag == null || tag.length() <= 0) {
            return this.mAttrs.packageName;
        }
        return tag;
    }

    public String toString() {
        CharSequence title = getWindowTag();
        if (this.mStringNameCache != null && this.mLastTitle == title) {
            if (this.mWasExiting != this.mAnimatingExit) {
            }
            return this.mStringNameCache;
        }
        this.mLastTitle = title;
        this.mWasExiting = this.mAnimatingExit;
        this.mStringNameCache = "Window{" + Integer.toHexString(System.identityHashCode(this)) + " u" + UserHandle.getUserId(this.mSession.mUid) + " " + this.mLastTitle + (this.mAnimatingExit ? " EXITING}" : "}");
        return this.mStringNameCache;
    }

    void transformClipRectFromScreenToSurfaceSpace(Rect clipRect) {
        if (this.mHScale >= 0.0f) {
            clipRect.left = (int) (((float) clipRect.left) / this.mHScale);
            clipRect.right = (int) Math.ceil((double) (((float) clipRect.right) / this.mHScale));
        }
        if (this.mVScale >= 0.0f) {
            clipRect.top = (int) (((float) clipRect.top) / this.mVScale);
            clipRect.bottom = (int) Math.ceil((double) (((float) clipRect.bottom) / this.mVScale));
        }
    }

    void applyGravityAndUpdateFrame(Rect containingFrame, Rect displayFrame) {
        int w;
        int h;
        float x;
        float y;
        int pw = containingFrame.width();
        int ph = containingFrame.height();
        Task task = getTask();
        boolean nonFullscreenTask = isInMultiWindowMode();
        boolean fitToDisplay = (task == null || !nonFullscreenTask) ? true : (!isChildWindow() || ((this.mAttrs.flags & DumpState.DUMP_MESSAGES) != 0 ? true : DEBUG_DISABLE_SAVING_SURFACES)) ? DEBUG_DISABLE_SAVING_SURFACES : true;
        if ((this.mAttrs.flags & DumpState.DUMP_KEYSETS) != 0) {
            if (this.mAttrs.width < 0) {
                w = pw;
            } else if (this.mEnforceSizeCompat) {
                w = (int) ((((float) this.mAttrs.width) * this.mGlobalScale) + TaskPositioner.RESIZING_HINT_ALPHA);
            } else {
                w = this.mAttrs.width;
            }
            if (this.mAttrs.height < 0) {
                h = ph;
            } else if (this.mEnforceSizeCompat) {
                h = (int) ((((float) this.mAttrs.height) * this.mGlobalScale) + TaskPositioner.RESIZING_HINT_ALPHA);
            } else {
                h = this.mAttrs.height;
            }
        } else {
            if (this.mAttrs.width == -1) {
                w = pw;
            } else if (!this.mEnforceSizeCompat || isRogEnable()) {
                w = this.mRequestedWidth;
            } else {
                w = (int) ((((float) this.mRequestedWidth) * this.mGlobalScale) + TaskPositioner.RESIZING_HINT_ALPHA);
            }
            if (this.mAttrs.height == -1) {
                h = ph;
            } else if (!this.mEnforceSizeCompat || isRogEnable()) {
                h = this.mRequestedHeight;
            } else {
                h = (int) ((((float) this.mRequestedHeight) * this.mGlobalScale) + TaskPositioner.RESIZING_HINT_ALPHA);
            }
        }
        if (!this.mEnforceSizeCompat || isRogEnable()) {
            x = (float) this.mAttrs.x;
            y = (float) this.mAttrs.y;
        } else {
            x = ((float) this.mAttrs.x) * this.mGlobalScale;
            y = ((float) this.mAttrs.y) * this.mGlobalScale;
        }
        if (nonFullscreenTask && !layoutInParentFrame()) {
            w = Math.min(w, pw);
            h = Math.min(h, ph);
        }
        Gravity.apply(this.mAttrs.gravity, w, h, containingFrame, (int) ((this.mAttrs.horizontalMargin * ((float) pw)) + x), (int) ((this.mAttrs.verticalMargin * ((float) ph)) + y), this.mFrame);
        if (fitToDisplay) {
            Gravity.applyDisplay(this.mAttrs.gravity, displayFrame, this.mFrame);
        }
        this.mCompatFrame.set(this.mFrame);
        if (this.mEnforceSizeCompat) {
            this.mCompatFrame.scale(this.mInvGlobalScale);
        }
    }

    boolean isChildWindow() {
        return this.mAttachedWindow != null ? true : DEBUG_DISABLE_SAVING_SURFACES;
    }

    boolean layoutInParentFrame() {
        return (!isChildWindow() || (this.mAttrs.privateFlags & DumpState.DUMP_INSTALLS) == 0) ? DEBUG_DISABLE_SAVING_SURFACES : true;
    }

    void setReplacing(boolean animate) {
        if ((this.mAttrs.privateFlags & DumpState.DUMP_VERSION) == 0 && this.mAttrs.type != 3) {
            this.mWillReplaceWindow = true;
            this.mReplacingWindow = null;
            this.mAnimateReplacingWindow = animate;
        }
    }

    void resetReplacing() {
        this.mWillReplaceWindow = DEBUG_DISABLE_SAVING_SURFACES;
        this.mReplacingWindow = null;
        this.mAnimateReplacingWindow = DEBUG_DISABLE_SAVING_SURFACES;
    }

    void requestUpdateWallpaperIfNeeded() {
        if (this.mDisplayContent != null && (this.mAttrs.flags & DumpState.DUMP_DEXOPT) != 0) {
            DisplayContent displayContent = this.mDisplayContent;
            displayContent.pendingLayoutChanges |= 4;
            this.mDisplayContent.layoutNeeded = true;
            this.mService.mWindowPlacerLocked.requestTraversal();
        }
    }

    float translateToWindowX(float x) {
        float winX = x - ((float) this.mFrame.left);
        if (this.mEnforceSizeCompat) {
            return winX * this.mGlobalScale;
        }
        return winX;
    }

    float translateToWindowY(float y) {
        float winY = y - ((float) this.mFrame.top);
        if (this.mEnforceSizeCompat) {
            return winY * this.mGlobalScale;
        }
        return winY;
    }

    void transferDimToReplacement() {
        boolean z = DEBUG_DISABLE_SAVING_SURFACES;
        DimLayerUser dimLayerUser = getDimLayerUser();
        if (dimLayerUser != null && this.mDisplayContent != null) {
            DimLayerController dimLayerController = this.mDisplayContent.mDimLayerController;
            WindowStateAnimator windowStateAnimator = this.mReplacingWindow.mWinAnimator;
            if ((this.mAttrs.flags & LOW_RESOLUTION_COMPOSITION_ON) != 0) {
                z = true;
            }
            dimLayerController.applyDim(dimLayerUser, windowStateAnimator, z);
        }
    }

    boolean shouldBeReplacedWithChildren() {
        return (isChildWindow() || this.mAttrs.type == LOW_RESOLUTION_COMPOSITION_ON) ? true : DEBUG_DISABLE_SAVING_SURFACES;
    }

    public boolean canCarryColors() {
        return this.mCanCarryColors;
    }

    public void setCanCarryColors(boolean carry) {
        this.mCanCarryColors = carry;
    }

    private AppRogInfo getAppRogInfo() {
        IRogManager rogManager = (IRogManager) LocalServices.getService(IRogManager.class);
        if (rogManager != null) {
            return rogManager.getSpecifiedAppRogInfo(this.mAttrs.packageName);
        }
        return null;
    }

    boolean isRogEnable() {
        return (this.mAttrs.privateFlags & DumpState.DUMP_DEXOPT) != 0 ? true : DEBUG_DISABLE_SAVING_SURFACES;
    }

    private boolean isStatusBarWindow() {
        if (this.mAttrs.type == IHwShutdownThread.SHUTDOWN_ANIMATION_WAIT_TIME || this.mAttrs.type == 2014 || this.mAttrs.type == 2019 || this.mAttrs.type == 2024) {
            return true;
        }
        return DEBUG_DISABLE_SAVING_SURFACES;
    }

    int getLowResolutionMode() {
        return (!isStatusBarWindow() && ((ActivityManagerInternal) LocalServices.getService(ActivityManagerInternal.class)).getPackageScreenCompatMode(getOwningPackage()) == 0) ? LOW_RESOLUTION_FEATURE_OFF : LOW_RESOLUTION_COMPOSITION_ON;
    }

    float getRogScale() {
        AppRogInfo rogInfo = getAppRogInfo();
        return rogInfo != null ? rogInfo.mRogScale : 1.0f;
    }
}
