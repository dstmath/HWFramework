package android.service.wallpaper;

import android.app.Service;
import android.app.WallpaperColors;
import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.hardware.display.DisplayManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import android.provider.SettingsStringUtil;
import android.service.wallpaper.IWallpaperEngine;
import android.service.wallpaper.IWallpaperService;
import android.service.wallpaper.WallpaperService;
import android.util.Log;
import android.util.MergedConfiguration;
import android.view.Display;
import android.view.DisplayCutout;
import android.view.IWindowSession;
import android.view.InputChannel;
import android.view.InputEvent;
import android.view.InputEventReceiver;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.WindowInsets;
import android.view.WindowManager;
import android.view.WindowManagerGlobal;
import com.android.internal.R;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.os.HandlerCaller;
import com.android.internal.view.BaseIWindow;
import com.android.internal.view.BaseSurfaceHolder;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.function.Supplier;

public abstract class WallpaperService extends Service {
    static final boolean DEBUG = false;
    private static final int DO_ATTACH = 10;
    private static final int DO_DETACH = 20;
    private static final int DO_IN_AMBIENT_MODE = 50;
    private static final int DO_SET_DESIRED_SIZE = 30;
    private static final int DO_SET_DISPLAY_PADDING = 40;
    private static final int MSG_REQUEST_WALLPAPER_COLORS = 10050;
    private static final int MSG_TOUCH_EVENT = 10040;
    private static final int MSG_UPDATE_SURFACE = 10000;
    private static final int MSG_VISIBILITY_CHANGED = 10010;
    private static final int MSG_WALLPAPER_COMMAND = 10025;
    private static final int MSG_WALLPAPER_OFFSETS = 10020;
    private static final int MSG_WINDOW_MOVED = 10035;
    private static final int MSG_WINDOW_RESIZED = 10030;
    private static final int NOTIFY_COLORS_RATE_LIMIT_MS = 1000;
    public static final String SERVICE_INTERFACE = "android.service.wallpaper.WallpaperService";
    public static final String SERVICE_META_DATA = "android.service.wallpaper";
    static final String TAG = "WallpaperService";
    /* access modifiers changed from: private */
    public final ArrayList<Engine> mActiveEngines = new ArrayList<>();

    public class Engine {
        final Rect mBackdropFrame;
        HandlerCaller mCaller;
        private final Supplier<Long> mClockFunction;
        IWallpaperConnection mConnection;
        final Rect mContentInsets;
        boolean mCreated;
        int mCurHeight;
        int mCurWidth;
        int mCurWindowFlags;
        int mCurWindowPrivateFlags;
        boolean mDestroyed;
        final Rect mDispatchedContentInsets;
        DisplayCutout mDispatchedDisplayCutout;
        final Rect mDispatchedOutsets;
        final Rect mDispatchedOverscanInsets;
        final Rect mDispatchedStableInsets;
        Display mDisplay;
        final DisplayCutout.ParcelableWrapper mDisplayCutout;
        private final DisplayManager.DisplayListener mDisplayListener;
        DisplayManager mDisplayManager;
        /* access modifiers changed from: private */
        public int mDisplayState;
        boolean mDrawingAllowed;
        final Rect mFinalStableInsets;
        final Rect mFinalSystemInsets;
        boolean mFixedSizeAllowed;
        int mFormat;
        private final Handler mHandler;
        int mHeight;
        IWallpaperEngineWrapper mIWallpaperEngine;
        boolean mInitializing;
        InputChannel mInputChannel;
        WallpaperInputEventReceiver mInputEventReceiver;
        boolean mIsCreating;
        boolean mIsInAmbientMode;
        private long mLastColorInvalidation;
        final WindowManager.LayoutParams mLayout;
        final Object mLock;
        final MergedConfiguration mMergedConfiguration;
        private final Runnable mNotifyColorsChanged;
        boolean mOffsetMessageEnqueued;
        boolean mOffsetsChanged;
        final Rect mOutsets;
        final Rect mOverscanInsets;
        MotionEvent mPendingMove;
        boolean mPendingSync;
        float mPendingXOffset;
        float mPendingXOffsetStep;
        float mPendingYOffset;
        float mPendingYOffsetStep;
        boolean mReportedVisible;
        IWindowSession mSession;
        final Rect mStableInsets;
        boolean mSurfaceCreated;
        final BaseSurfaceHolder mSurfaceHolder;
        int mType;
        boolean mVisible;
        final Rect mVisibleInsets;
        int mWidth;
        final Rect mWinFrame;
        final BaseIWindow mWindow;
        int mWindowFlags;
        int mWindowPrivateFlags;
        IBinder mWindowToken;

        final class WallpaperInputEventReceiver extends InputEventReceiver {
            public WallpaperInputEventReceiver(InputChannel inputChannel, Looper looper) {
                super(inputChannel, looper);
            }

            public void onInputEvent(InputEvent event, int displayId) {
                boolean handled = false;
                try {
                    if ((event instanceof MotionEvent) && (event.getSource() & 2) != 0) {
                        Engine.this.dispatchPointer(MotionEvent.obtainNoHistory((MotionEvent) event));
                        handled = true;
                    }
                } finally {
                    finishInputEvent(event, handled);
                }
            }
        }

        public Engine(WallpaperService this$02) {
            this($$Lambda$87DoTfJA3qVM7QF6F_6BpQlQTA.INSTANCE, Handler.getMain());
        }

        @VisibleForTesting
        public Engine(Supplier<Long> clockFunction, Handler handler) {
            this.mInitializing = true;
            this.mWindowFlags = 16;
            this.mWindowPrivateFlags = 4;
            this.mCurWindowFlags = this.mWindowFlags;
            this.mCurWindowPrivateFlags = this.mWindowPrivateFlags;
            this.mVisibleInsets = new Rect();
            this.mWinFrame = new Rect();
            this.mOverscanInsets = new Rect();
            this.mContentInsets = new Rect();
            this.mStableInsets = new Rect();
            this.mOutsets = new Rect();
            this.mDispatchedOverscanInsets = new Rect();
            this.mDispatchedContentInsets = new Rect();
            this.mDispatchedStableInsets = new Rect();
            this.mDispatchedOutsets = new Rect();
            this.mFinalSystemInsets = new Rect();
            this.mFinalStableInsets = new Rect();
            this.mBackdropFrame = new Rect();
            this.mDisplayCutout = new DisplayCutout.ParcelableWrapper();
            this.mDispatchedDisplayCutout = DisplayCutout.NO_CUTOUT;
            this.mMergedConfiguration = new MergedConfiguration();
            this.mLayout = new WindowManager.LayoutParams();
            this.mLock = new Object();
            this.mNotifyColorsChanged = new Runnable() {
                public final void run() {
                    WallpaperService.Engine.this.notifyColorsChanged();
                }
            };
            this.mSurfaceHolder = new BaseSurfaceHolder() {
                {
                    this.mRequestedFormat = 2;
                }

                public boolean onAllowLockCanvas() {
                    return Engine.this.mDrawingAllowed;
                }

                public void onRelayoutContainer() {
                    Engine.this.mCaller.sendMessage(Engine.this.mCaller.obtainMessage(10000));
                }

                public void onUpdateSurface() {
                    Engine.this.mCaller.sendMessage(Engine.this.mCaller.obtainMessage(10000));
                }

                public boolean isCreating() {
                    return Engine.this.mIsCreating;
                }

                public void setFixedSize(int width, int height) {
                    if (Engine.this.mFixedSizeAllowed) {
                        Engine.super.setFixedSize(width, height);
                        return;
                    }
                    throw new UnsupportedOperationException("Wallpapers currently only support sizing from layout");
                }

                public void setKeepScreenOn(boolean screenOn) {
                    throw new UnsupportedOperationException("Wallpapers do not support keep screen on");
                }

                private void prepareToDraw() {
                    if (Engine.this.mDisplayState == 3 || Engine.this.mDisplayState == 4) {
                        try {
                            Engine.this.mSession.pokeDrawLock(Engine.this.mWindow);
                        } catch (RemoteException e) {
                        }
                    }
                }

                public Canvas lockCanvas() {
                    prepareToDraw();
                    return Engine.super.lockCanvas();
                }

                public Canvas lockCanvas(Rect dirty) {
                    prepareToDraw();
                    return Engine.super.lockCanvas(dirty);
                }

                public Canvas lockHardwareCanvas() {
                    prepareToDraw();
                    return Engine.super.lockHardwareCanvas();
                }
            };
            this.mWindow = new BaseIWindow() {
                public void resized(Rect frame, Rect overscanInsets, Rect contentInsets, Rect visibleInsets, Rect stableInsets, Rect outsets, boolean reportDraw, MergedConfiguration mergedConfiguration, Rect backDropRect, boolean forceLayout, boolean alwaysConsumeNavBar, int displayId, DisplayCutout.ParcelableWrapper displayCutout) {
                    Engine.this.mCaller.sendMessage(Engine.this.mCaller.obtainMessageIO(10030, reportDraw, outsets));
                }

                public void moved(int newX, int newY) {
                    Engine.this.mCaller.sendMessage(Engine.this.mCaller.obtainMessageII(10035, newX, newY));
                }

                public void dispatchAppVisibility(boolean visible) {
                    if (!Engine.this.mIWallpaperEngine.mIsPreview) {
                        Engine.this.mCaller.sendMessage(Engine.this.mCaller.obtainMessageI(10010, visible));
                    }
                }

                public void dispatchWallpaperOffsets(float x, float y, float xStep, float yStep, boolean sync) {
                    synchronized (Engine.this.mLock) {
                        Engine.this.mPendingXOffset = x;
                        Engine.this.mPendingYOffset = y;
                        Engine.this.mPendingXOffsetStep = xStep;
                        Engine.this.mPendingYOffsetStep = yStep;
                        if (sync) {
                            Engine.this.mPendingSync = true;
                        }
                        if (!Engine.this.mOffsetMessageEnqueued) {
                            Engine.this.mOffsetMessageEnqueued = true;
                            Engine.this.mCaller.sendMessage(Engine.this.mCaller.obtainMessage(10020));
                        }
                    }
                }

                public void dispatchWallpaperCommand(String action, int x, int y, int z, Bundle extras, boolean sync) {
                    synchronized (Engine.this.mLock) {
                        WallpaperCommand cmd = new WallpaperCommand();
                        cmd.action = action;
                        cmd.x = x;
                        cmd.y = y;
                        cmd.z = z;
                        cmd.extras = extras;
                        cmd.sync = sync;
                        Message msg = Engine.this.mCaller.obtainMessage(10025);
                        msg.obj = cmd;
                        Engine.this.mCaller.sendMessage(msg);
                    }
                }
            };
            this.mDisplayListener = new DisplayManager.DisplayListener() {
                public void onDisplayChanged(int displayId) {
                    if (Engine.this.mDisplay.getDisplayId() == displayId) {
                        Engine.this.reportVisibility();
                    }
                }

                public void onDisplayRemoved(int displayId) {
                }

                public void onDisplayAdded(int displayId) {
                }
            };
            this.mClockFunction = clockFunction;
            this.mHandler = handler;
        }

        public SurfaceHolder getSurfaceHolder() {
            return this.mSurfaceHolder;
        }

        public int getDesiredMinimumWidth() {
            return this.mIWallpaperEngine.mReqWidth;
        }

        public int getDesiredMinimumHeight() {
            return this.mIWallpaperEngine.mReqHeight;
        }

        public boolean isVisible() {
            return this.mReportedVisible;
        }

        public boolean isPreview() {
            return this.mIWallpaperEngine.mIsPreview;
        }

        public boolean isInAmbientMode() {
            return this.mIsInAmbientMode;
        }

        public void setTouchEventsEnabled(boolean enabled) {
            int i;
            if (enabled) {
                i = this.mWindowFlags & -17;
            } else {
                i = this.mWindowFlags | 16;
            }
            this.mWindowFlags = i;
            if (this.mCreated) {
                updateSurface(false, false, false);
            }
        }

        public void setOffsetNotificationsEnabled(boolean enabled) {
            int i;
            if (enabled) {
                i = this.mWindowPrivateFlags | 4;
            } else {
                i = this.mWindowPrivateFlags & -5;
            }
            this.mWindowPrivateFlags = i;
            if (this.mCreated) {
                updateSurface(false, false, false);
            }
        }

        public void setFixedSizeAllowed(boolean allowed) {
            this.mFixedSizeAllowed = allowed;
        }

        public void onCreate(SurfaceHolder surfaceHolder) {
        }

        public void onDestroy() {
        }

        public void onVisibilityChanged(boolean visible) {
        }

        public void onApplyWindowInsets(WindowInsets insets) {
        }

        public void onTouchEvent(MotionEvent event) {
        }

        public void onOffsetsChanged(float xOffset, float yOffset, float xOffsetStep, float yOffsetStep, int xPixelOffset, int yPixelOffset) {
        }

        public Bundle onCommand(String action, int x, int y, int z, Bundle extras, boolean resultRequested) {
            return null;
        }

        public void onAmbientModeChanged(boolean inAmbientMode, boolean animated) {
        }

        public void onDesiredSizeChanged(int desiredWidth, int desiredHeight) {
        }

        public void onSurfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        }

        public void onSurfaceRedrawNeeded(SurfaceHolder holder) {
        }

        public void onSurfaceCreated(SurfaceHolder holder) {
        }

        public void onSurfaceDestroyed(SurfaceHolder holder) {
        }

        public void notifyColorsChanged() {
            long now = this.mClockFunction.get().longValue();
            if (now - this.mLastColorInvalidation < 1000) {
                Log.w(WallpaperService.TAG, "This call has been deferred. You should only call notifyColorsChanged() once every 1.0 seconds.");
                if (!this.mHandler.hasCallbacks(this.mNotifyColorsChanged)) {
                    this.mHandler.postDelayed(this.mNotifyColorsChanged, 1000);
                }
                return;
            }
            this.mLastColorInvalidation = now;
            this.mHandler.removeCallbacks(this.mNotifyColorsChanged);
            try {
                WallpaperColors newColors = onComputeColors();
                if (this.mConnection != null) {
                    this.mConnection.onWallpaperColorsChanged(newColors);
                } else {
                    Log.w(WallpaperService.TAG, "Can't notify system because wallpaper connection was not established.");
                }
            } catch (RemoteException e) {
                Log.w(WallpaperService.TAG, "Can't notify system because wallpaper connection was lost.", e);
            }
        }

        public WallpaperColors onComputeColors() {
            return null;
        }

        @VisibleForTesting
        public void setCreated(boolean created) {
            this.mCreated = created;
        }

        /* access modifiers changed from: protected */
        public void dump(String prefix, FileDescriptor fd, PrintWriter out, String[] args) {
            out.print(prefix);
            out.print("mInitializing=");
            out.print(this.mInitializing);
            out.print(" mDestroyed=");
            out.println(this.mDestroyed);
            out.print(prefix);
            out.print("mVisible=");
            out.print(this.mVisible);
            out.print(" mReportedVisible=");
            out.println(this.mReportedVisible);
            out.print(prefix);
            out.print("mDisplay=");
            out.println(this.mDisplay);
            out.print(prefix);
            out.print("mCreated=");
            out.print(this.mCreated);
            out.print(" mSurfaceCreated=");
            out.print(this.mSurfaceCreated);
            out.print(" mIsCreating=");
            out.print(this.mIsCreating);
            out.print(" mDrawingAllowed=");
            out.println(this.mDrawingAllowed);
            out.print(prefix);
            out.print("mWidth=");
            out.print(this.mWidth);
            out.print(" mCurWidth=");
            out.print(this.mCurWidth);
            out.print(" mHeight=");
            out.print(this.mHeight);
            out.print(" mCurHeight=");
            out.println(this.mCurHeight);
            out.print(prefix);
            out.print("mType=");
            out.print(this.mType);
            out.print(" mWindowFlags=");
            out.print(this.mWindowFlags);
            out.print(" mCurWindowFlags=");
            out.println(this.mCurWindowFlags);
            out.print(prefix);
            out.print("mWindowPrivateFlags=");
            out.print(this.mWindowPrivateFlags);
            out.print(" mCurWindowPrivateFlags=");
            out.println(this.mCurWindowPrivateFlags);
            out.print(prefix);
            out.print("mVisibleInsets=");
            out.print(this.mVisibleInsets.toShortString());
            out.print(" mWinFrame=");
            out.print(this.mWinFrame.toShortString());
            out.print(" mContentInsets=");
            out.println(this.mContentInsets.toShortString());
            out.print(prefix);
            out.print("mConfiguration=");
            out.println(this.mMergedConfiguration.getMergedConfiguration());
            out.print(prefix);
            out.print("mLayout=");
            out.println(this.mLayout);
            synchronized (this.mLock) {
                out.print(prefix);
                out.print("mPendingXOffset=");
                out.print(this.mPendingXOffset);
                out.print(" mPendingXOffset=");
                out.println(this.mPendingXOffset);
                out.print(prefix);
                out.print("mPendingXOffsetStep=");
                out.print(this.mPendingXOffsetStep);
                out.print(" mPendingXOffsetStep=");
                out.println(this.mPendingXOffsetStep);
                out.print(prefix);
                out.print("mOffsetMessageEnqueued=");
                out.print(this.mOffsetMessageEnqueued);
                out.print(" mPendingSync=");
                out.println(this.mPendingSync);
                if (this.mPendingMove != null) {
                    out.print(prefix);
                    out.print("mPendingMove=");
                    out.println(this.mPendingMove);
                }
            }
        }

        /* access modifiers changed from: private */
        public void dispatchPointer(MotionEvent event) {
            if (event.isTouchEvent()) {
                synchronized (this.mLock) {
                    if (event.getAction() == 2) {
                        this.mPendingMove = event;
                    } else {
                        this.mPendingMove = null;
                    }
                }
                this.mCaller.sendMessage(this.mCaller.obtainMessageO(10040, event));
                return;
            }
            event.recycle();
        }

        /* access modifiers changed from: package-private */
        /* JADX WARNING: Removed duplicated region for block: B:194:0x053a A[Catch:{ RemoteException -> 0x0547 }] */
        public void updateSurface(boolean forceRelayout, boolean forceReport, boolean redrawNeeded) {
            boolean creating;
            boolean sizeChanged;
            boolean formatChanged;
            boolean creating2;
            boolean surfaceCreating;
            int relayoutResult;
            int h;
            int h2;
            boolean sizeChanged2;
            boolean insetsChanged;
            if (this.mDestroyed) {
                Log.w(WallpaperService.TAG, "Ignoring updateSurface: destroyed");
            }
            boolean fixedSize = false;
            int myWidth = this.mSurfaceHolder.getRequestedWidth();
            if (myWidth <= 0) {
                myWidth = -1;
            } else {
                fixedSize = true;
            }
            int myHeight = this.mSurfaceHolder.getRequestedHeight();
            if (myHeight <= 0) {
                myHeight = -1;
            } else {
                fixedSize = true;
            }
            boolean fixedSize2 = fixedSize;
            boolean creating3 = !this.mCreated;
            boolean surfaceCreating2 = !this.mSurfaceCreated;
            boolean formatChanged2 = this.mFormat != this.mSurfaceHolder.getRequestedFormat();
            boolean sizeChanged3 = (this.mWidth == myWidth && this.mHeight == myHeight) ? false : true;
            boolean insetsChanged2 = !this.mCreated;
            boolean typeChanged = this.mType != this.mSurfaceHolder.getRequestedType();
            boolean flagsChanged = (this.mCurWindowFlags == this.mWindowFlags && this.mCurWindowPrivateFlags == this.mWindowPrivateFlags) ? false : true;
            if (forceRelayout || creating3 || surfaceCreating2 || formatChanged2 || sizeChanged3 || typeChanged || flagsChanged || redrawNeeded || !this.mIWallpaperEngine.mShownReported) {
                try {
                    this.mWidth = myWidth;
                    this.mHeight = myHeight;
                    this.mFormat = this.mSurfaceHolder.getRequestedFormat();
                    this.mType = this.mSurfaceHolder.getRequestedType();
                    this.mLayout.x = 0;
                    this.mLayout.y = 0;
                    this.mLayout.width = myWidth;
                    this.mLayout.height = myHeight;
                    this.mLayout.format = this.mFormat;
                    this.mCurWindowFlags = this.mWindowFlags;
                    this.mLayout.flags = this.mWindowFlags | 512 | 65536 | 256 | 8;
                    this.mCurWindowPrivateFlags = this.mWindowPrivateFlags;
                    this.mLayout.privateFlags = this.mWindowPrivateFlags;
                    this.mLayout.memoryType = this.mType;
                    this.mLayout.token = this.mWindowToken;
                    if (!this.mCreated) {
                        try {
                            TypedArray windowStyle = WallpaperService.this.obtainStyledAttributes(R.styleable.Window);
                            windowStyle.recycle();
                            this.mLayout.type = this.mIWallpaperEngine.mWindowType;
                            this.mLayout.gravity = 8388659;
                            this.mLayout.setTitle(WallpaperService.this.getClass().getName());
                            this.mLayout.windowAnimations = 16974600;
                            this.mInputChannel = new InputChannel();
                            TypedArray typedArray = windowStyle;
                            int i = myWidth;
                            try {
                                int i2 = myHeight;
                                try {
                                    sizeChanged = sizeChanged3;
                                } catch (RemoteException e) {
                                    boolean z = formatChanged2;
                                    boolean z2 = sizeChanged3;
                                    boolean z3 = typeChanged;
                                    boolean z4 = flagsChanged;
                                    boolean z5 = fixedSize2;
                                    boolean z6 = creating3;
                                    boolean z7 = surfaceCreating2;
                                    creating = redrawNeeded;
                                    boolean z8 = creating;
                                }
                            } catch (RemoteException e2) {
                                int i3 = myHeight;
                                boolean z9 = formatChanged2;
                                boolean z10 = sizeChanged3;
                                boolean z11 = typeChanged;
                                boolean z12 = flagsChanged;
                                boolean z13 = fixedSize2;
                                boolean z14 = creating3;
                                boolean z15 = surfaceCreating2;
                                creating = redrawNeeded;
                                boolean z82 = creating;
                            }
                        } catch (RemoteException e3) {
                            int i4 = myWidth;
                            int i5 = myHeight;
                            boolean z16 = formatChanged2;
                            boolean z17 = sizeChanged3;
                            boolean z18 = typeChanged;
                            boolean z19 = flagsChanged;
                            boolean z20 = fixedSize2;
                            boolean z21 = creating3;
                            boolean z22 = surfaceCreating2;
                            creating = redrawNeeded;
                            boolean z822 = creating;
                        }
                        try {
                            boolean z23 = typeChanged;
                            try {
                                boolean z24 = flagsChanged;
                                try {
                                    formatChanged = formatChanged2;
                                    try {
                                        if (this.mSession.addToDisplay(this.mWindow, this.mWindow.mSeq, this.mLayout, 0, 0, this.mWinFrame, this.mContentInsets, this.mStableInsets, this.mOutsets, this.mDisplayCutout, this.mInputChannel) < 0) {
                                            Log.w(WallpaperService.TAG, "Failed to add window while updating wallpaper surface.");
                                            return;
                                        } else {
                                            this.mCreated = true;
                                            this.mInputEventReceiver = new WallpaperInputEventReceiver(this.mInputChannel, Looper.myLooper());
                                        }
                                    } catch (RemoteException e4) {
                                        boolean z25 = fixedSize2;
                                        boolean z26 = creating3;
                                        boolean z27 = surfaceCreating2;
                                        boolean z28 = sizeChanged;
                                    }
                                } catch (RemoteException e5) {
                                    boolean z29 = formatChanged2;
                                    boolean z30 = fixedSize2;
                                    boolean z31 = creating3;
                                    boolean z32 = surfaceCreating2;
                                    boolean z33 = sizeChanged;
                                    creating = redrawNeeded;
                                    boolean z8222 = creating;
                                }
                            } catch (RemoteException e6) {
                                boolean z34 = formatChanged2;
                                boolean z35 = flagsChanged;
                                boolean z36 = fixedSize2;
                                boolean z37 = creating3;
                                boolean z38 = surfaceCreating2;
                                boolean z39 = sizeChanged;
                                creating = redrawNeeded;
                                boolean z82222 = creating;
                            }
                        } catch (RemoteException e7) {
                            boolean z40 = formatChanged2;
                            boolean z41 = typeChanged;
                            boolean z42 = flagsChanged;
                            boolean z43 = fixedSize2;
                            boolean z44 = creating3;
                            boolean z45 = surfaceCreating2;
                            boolean z46 = sizeChanged;
                            creating = redrawNeeded;
                            boolean z822222 = creating;
                        }
                    } else {
                        int i6 = myHeight;
                        formatChanged = formatChanged2;
                        sizeChanged = sizeChanged3;
                        boolean z47 = typeChanged;
                        boolean z48 = flagsChanged;
                    }
                    try {
                        this.mSurfaceHolder.mSurfaceLock.lock();
                        this.mDrawingAllowed = true;
                        if (!fixedSize2) {
                            this.mLayout.surfaceInsets.set(this.mIWallpaperEngine.mDisplayPadding);
                            this.mLayout.surfaceInsets.left += this.mOutsets.left;
                            this.mLayout.surfaceInsets.top += this.mOutsets.top;
                            this.mLayout.surfaceInsets.right += this.mOutsets.right;
                            this.mLayout.surfaceInsets.bottom += this.mOutsets.bottom;
                        } else {
                            this.mLayout.surfaceInsets.set(0, 0, 0, 0);
                        }
                        creating2 = creating3;
                        try {
                            surfaceCreating = surfaceCreating2;
                            try {
                                boolean insetsChanged3 = insetsChanged2;
                                try {
                                    boolean fixedSize3 = fixedSize2;
                                    try {
                                        relayoutResult = this.mSession.relayout(this.mWindow, this.mWindow.mSeq, this.mLayout, this.mWidth, this.mHeight, 0, 0, -1, this.mWinFrame, this.mOverscanInsets, this.mContentInsets, this.mVisibleInsets, this.mStableInsets, this.mOutsets, this.mBackdropFrame, this.mDisplayCutout, this.mMergedConfiguration, this.mSurfaceHolder.mSurface);
                                        int w = this.mWinFrame.width();
                                        int h3 = this.mWinFrame.height();
                                        if (!fixedSize3) {
                                            Rect padding = this.mIWallpaperEngine.mDisplayPadding;
                                            w += padding.left + padding.right + this.mOutsets.left + this.mOutsets.right;
                                            h3 += padding.top + padding.bottom + this.mOutsets.top + this.mOutsets.bottom;
                                            this.mOverscanInsets.left += padding.left;
                                            this.mOverscanInsets.top += padding.top;
                                            this.mOverscanInsets.right += padding.right;
                                            this.mOverscanInsets.bottom += padding.bottom;
                                            this.mContentInsets.left += padding.left;
                                            this.mContentInsets.top += padding.top;
                                            this.mContentInsets.right += padding.right;
                                            this.mContentInsets.bottom += padding.bottom;
                                            this.mStableInsets.left += padding.left;
                                            this.mStableInsets.top += padding.top;
                                            this.mStableInsets.right += padding.right;
                                            this.mStableInsets.bottom += padding.bottom;
                                            this.mDisplayCutout.set(this.mDisplayCutout.get().inset(-padding.left, -padding.top, -padding.right, -padding.bottom));
                                        }
                                        h = h3;
                                        h2 = w;
                                        if (this.mCurWidth != h2) {
                                            sizeChanged2 = true;
                                            try {
                                                this.mCurWidth = h2;
                                            } catch (RemoteException e8) {
                                                creating = redrawNeeded;
                                                boolean z8222222 = creating;
                                            }
                                        } else {
                                            sizeChanged2 = sizeChanged;
                                        }
                                        if (this.mCurHeight != h) {
                                            try {
                                                this.mCurHeight = h;
                                                sizeChanged2 = true;
                                            } catch (RemoteException e9) {
                                                creating = redrawNeeded;
                                                boolean z82222222 = creating;
                                            }
                                        }
                                        insetsChanged = insetsChanged3 | (!this.mDispatchedOverscanInsets.equals(this.mOverscanInsets));
                                    } catch (RemoteException e10) {
                                        creating = redrawNeeded;
                                        boolean z49 = sizeChanged;
                                        boolean z822222222 = creating;
                                    }
                                } catch (RemoteException e11) {
                                    boolean z50 = fixedSize2;
                                    creating = redrawNeeded;
                                    boolean z51 = sizeChanged;
                                    boolean z52 = insetsChanged3;
                                    boolean z8222222222 = creating;
                                }
                            } catch (RemoteException e12) {
                                boolean z53 = fixedSize2;
                                boolean z54 = insetsChanged2;
                                creating = redrawNeeded;
                                boolean z55 = sizeChanged;
                                boolean z82222222222 = creating;
                            }
                        } catch (RemoteException e13) {
                            boolean z56 = fixedSize2;
                            boolean z57 = surfaceCreating2;
                            boolean z58 = insetsChanged2;
                            creating = redrawNeeded;
                            boolean z59 = sizeChanged;
                            boolean z822222222222 = creating;
                        }
                    } catch (RemoteException e14) {
                        boolean z60 = fixedSize2;
                        boolean z61 = creating3;
                        boolean z62 = surfaceCreating2;
                        boolean z63 = insetsChanged2;
                        creating = redrawNeeded;
                        boolean z64 = sizeChanged;
                        boolean z8222222222222 = creating;
                    }
                    try {
                        boolean insetsChanged4 = insetsChanged | (!this.mDispatchedContentInsets.equals(this.mContentInsets)) | (!this.mDispatchedStableInsets.equals(this.mStableInsets)) | (!this.mDispatchedOutsets.equals(this.mOutsets)) | (!this.mDispatchedDisplayCutout.equals(this.mDisplayCutout.get()));
                        try {
                            this.mSurfaceHolder.setSurfaceFrameSize(h2, h);
                            this.mSurfaceHolder.mSurfaceLock.unlock();
                            if (!this.mSurfaceHolder.mSurface.isValid()) {
                                reportSurfaceDestroyed();
                                return;
                            }
                            boolean redrawNeeded2 = false;
                            boolean didSurface = false;
                            try {
                                this.mSurfaceHolder.ungetCallbacks();
                                if (surfaceCreating) {
                                    this.mIsCreating = true;
                                    didSurface = true;
                                    onSurfaceCreated(this.mSurfaceHolder);
                                    SurfaceHolder.Callback[] callbacks = this.mSurfaceHolder.getCallbacks();
                                    if (callbacks != null) {
                                        for (SurfaceHolder.Callback c : callbacks) {
                                            c.surfaceCreated(this.mSurfaceHolder);
                                        }
                                    }
                                }
                                redrawNeeded2 = redrawNeeded | (creating2 || (relayoutResult & 2) != 0);
                                if (forceReport || creating2 || surfaceCreating || formatChanged || sizeChanged2) {
                                    didSurface = true;
                                    try {
                                        onSurfaceChanged(this.mSurfaceHolder, this.mFormat, this.mCurWidth, this.mCurHeight);
                                        SurfaceHolder.Callback[] callbacks2 = this.mSurfaceHolder.getCallbacks();
                                        if (callbacks2 != null) {
                                            int length = callbacks2.length;
                                            int i7 = 0;
                                            while (i7 < length) {
                                                callbacks2[i7].surfaceChanged(this.mSurfaceHolder, this.mFormat, this.mCurWidth, this.mCurHeight);
                                                i7++;
                                                callbacks2 = callbacks2;
                                            }
                                        }
                                    } catch (Throwable th) {
                                        th = th;
                                        this.mIsCreating = false;
                                        this.mSurfaceCreated = true;
                                        if (redrawNeeded2) {
                                            this.mSession.finishDrawing(this.mWindow);
                                        }
                                        this.mIWallpaperEngine.reportShown();
                                        throw th;
                                    }
                                }
                                if (insetsChanged4) {
                                    this.mDispatchedOverscanInsets.set(this.mOverscanInsets);
                                    this.mDispatchedOverscanInsets.left += this.mOutsets.left;
                                    this.mDispatchedOverscanInsets.top += this.mOutsets.top;
                                    this.mDispatchedOverscanInsets.right += this.mOutsets.right;
                                    this.mDispatchedOverscanInsets.bottom += this.mOutsets.bottom;
                                    this.mDispatchedContentInsets.set(this.mContentInsets);
                                    this.mDispatchedStableInsets.set(this.mStableInsets);
                                    this.mDispatchedOutsets.set(this.mOutsets);
                                    this.mDispatchedDisplayCutout = this.mDisplayCutout.get();
                                    this.mFinalSystemInsets.set(this.mDispatchedOverscanInsets);
                                    this.mFinalStableInsets.set(this.mDispatchedStableInsets);
                                    WindowInsets windowInsets = new WindowInsets(this.mFinalSystemInsets, null, this.mFinalStableInsets, WallpaperService.this.getResources().getConfiguration().isScreenRound(), false, this.mDispatchedDisplayCutout);
                                    onApplyWindowInsets(windowInsets);
                                }
                                if (redrawNeeded2) {
                                    onSurfaceRedrawNeeded(this.mSurfaceHolder);
                                    SurfaceHolder.Callback[] callbacks3 = this.mSurfaceHolder.getCallbacks();
                                    if (callbacks3 != null) {
                                        for (SurfaceHolder.Callback c2 : callbacks3) {
                                            if (c2 instanceof SurfaceHolder.Callback2) {
                                                ((SurfaceHolder.Callback2) c2).surfaceRedrawNeeded(this.mSurfaceHolder);
                                            }
                                        }
                                    }
                                }
                                if (didSurface && !this.mReportedVisible) {
                                    if (this.mIsCreating) {
                                        onVisibilityChanged(true);
                                    }
                                    onVisibilityChanged(false);
                                }
                                try {
                                    this.mIsCreating = false;
                                    this.mSurfaceCreated = true;
                                    if (redrawNeeded2) {
                                        this.mSession.finishDrawing(this.mWindow);
                                    }
                                    this.mIWallpaperEngine.reportShown();
                                    boolean z65 = redrawNeeded2;
                                } catch (RemoteException e15) {
                                    creating = redrawNeeded2;
                                    boolean z82222222222222 = creating;
                                }
                            } catch (Throwable th2) {
                                th = th2;
                                redrawNeeded2 = redrawNeeded;
                                this.mIsCreating = false;
                                this.mSurfaceCreated = true;
                                if (redrawNeeded2) {
                                }
                                this.mIWallpaperEngine.reportShown();
                                throw th;
                            }
                        } catch (RemoteException e16) {
                            creating = redrawNeeded;
                            boolean z822222222222222 = creating;
                        }
                    } catch (RemoteException e17) {
                        creating = redrawNeeded;
                        boolean z66 = insetsChanged;
                        boolean z8222222222222222 = creating;
                    }
                } catch (RemoteException e18) {
                    int i8 = myWidth;
                    int i9 = myHeight;
                    boolean z67 = fixedSize2;
                    boolean z68 = creating3;
                    boolean z69 = surfaceCreating2;
                    boolean z70 = formatChanged2;
                    boolean z71 = sizeChanged3;
                    boolean z72 = insetsChanged2;
                    boolean z73 = typeChanged;
                    boolean z74 = flagsChanged;
                    creating = redrawNeeded;
                    boolean z82222222222222222 = creating;
                }
            } else {
                boolean z75 = redrawNeeded;
                int i10 = myWidth;
                int i11 = myHeight;
                boolean z76 = fixedSize2;
                boolean z77 = creating3;
                boolean z78 = surfaceCreating2;
                boolean z79 = formatChanged2;
                boolean z80 = typeChanged;
                boolean z81 = flagsChanged;
            }
        }

        /* access modifiers changed from: package-private */
        public void attach(IWallpaperEngineWrapper wrapper) {
            if (!this.mDestroyed) {
                this.mIWallpaperEngine = wrapper;
                this.mCaller = wrapper.mCaller;
                this.mConnection = wrapper.mConnection;
                this.mWindowToken = wrapper.mWindowToken;
                this.mSurfaceHolder.setSizeFromLayout();
                this.mInitializing = true;
                this.mSession = WindowManagerGlobal.getWindowSession();
                this.mWindow.setSession(this.mSession);
                this.mLayout.packageName = WallpaperService.this.getPackageName();
                this.mDisplayManager = (DisplayManager) WallpaperService.this.getSystemService("display");
                this.mDisplayManager.registerDisplayListener(this.mDisplayListener, this.mCaller.getHandler());
                this.mDisplay = this.mDisplayManager.getDisplay(0);
                this.mDisplayState = this.mDisplay.getState();
                onCreate(this.mSurfaceHolder);
                this.mInitializing = false;
                this.mReportedVisible = false;
                updateSurface(false, false, false);
            }
        }

        @VisibleForTesting
        public void doAmbientModeChanged(boolean inAmbientMode, boolean animated) {
            if (!this.mDestroyed) {
                this.mIsInAmbientMode = inAmbientMode;
                if (this.mCreated) {
                    onAmbientModeChanged(inAmbientMode, animated);
                }
            }
        }

        /* access modifiers changed from: package-private */
        public void doDesiredSizeChanged(int desiredWidth, int desiredHeight) {
            if (!this.mDestroyed) {
                this.mIWallpaperEngine.mReqWidth = desiredWidth;
                this.mIWallpaperEngine.mReqHeight = desiredHeight;
                onDesiredSizeChanged(desiredWidth, desiredHeight);
                doOffsetsChanged(true);
            }
        }

        /* access modifiers changed from: package-private */
        public void doDisplayPaddingChanged(Rect padding) {
            if (!this.mDestroyed && !this.mIWallpaperEngine.mDisplayPadding.equals(padding)) {
                this.mIWallpaperEngine.mDisplayPadding.set(padding);
                updateSurface(true, false, false);
            }
        }

        /* access modifiers changed from: package-private */
        public void doVisibilityChanged(boolean visible) {
            if (!this.mDestroyed) {
                this.mVisible = visible;
                reportVisibility();
            }
        }

        /* access modifiers changed from: package-private */
        public void reportVisibility() {
            if (!this.mDestroyed) {
                this.mDisplayState = this.mDisplay == null ? 0 : this.mDisplay.getState();
                boolean z = true;
                if (!this.mVisible || this.mDisplayState == 1) {
                    z = false;
                }
                boolean visible = z;
                if (this.mReportedVisible != visible) {
                    this.mReportedVisible = visible;
                    if (visible) {
                        doOffsetsChanged(false);
                        updateSurface(false, false, false);
                    }
                    onVisibilityChanged(visible);
                }
            }
        }

        /* access modifiers changed from: package-private */
        public void doOffsetsChanged(boolean always) {
            float xOffset;
            float yOffset;
            float xOffsetStep;
            float yOffsetStep;
            boolean sync;
            int yPixels;
            if (!this.mDestroyed) {
                if (always || this.mOffsetsChanged) {
                    synchronized (this.mLock) {
                        xOffset = this.mPendingXOffset;
                        yOffset = this.mPendingYOffset;
                        xOffsetStep = this.mPendingXOffsetStep;
                        yOffsetStep = this.mPendingYOffsetStep;
                        sync = this.mPendingSync;
                        yPixels = 0;
                        this.mPendingSync = false;
                        this.mOffsetMessageEnqueued = false;
                    }
                    if (this.mSurfaceCreated) {
                        if (this.mReportedVisible) {
                            int availw = this.mIWallpaperEngine.mReqWidth - this.mCurWidth;
                            int xPixels = availw > 0 ? -((int) ((((float) availw) * xOffset) + 0.5f)) : 0;
                            int availh = this.mIWallpaperEngine.mReqHeight - this.mCurHeight;
                            if (availh > 0) {
                                yPixels = -((int) ((((float) availh) * yOffset) + 0.5f));
                            }
                            onOffsetsChanged(xOffset, yOffset, xOffsetStep, yOffsetStep, xPixels, yPixels);
                        } else {
                            this.mOffsetsChanged = true;
                        }
                    }
                    if (sync) {
                        try {
                            this.mSession.wallpaperOffsetsComplete(this.mWindow.asBinder());
                        } catch (RemoteException e) {
                        }
                    }
                }
            }
        }

        /* access modifiers changed from: package-private */
        public void doCommand(WallpaperCommand cmd) {
            Bundle result;
            if (!this.mDestroyed) {
                result = onCommand(cmd.action, cmd.x, cmd.y, cmd.z, cmd.extras, cmd.sync);
            } else {
                result = null;
            }
            if (cmd.sync) {
                try {
                    this.mSession.wallpaperCommandComplete(this.mWindow.asBinder(), result);
                } catch (RemoteException e) {
                }
            }
        }

        /* access modifiers changed from: package-private */
        public void reportSurfaceDestroyed() {
            if (this.mSurfaceCreated) {
                this.mSurfaceCreated = false;
                this.mSurfaceHolder.ungetCallbacks();
                SurfaceHolder.Callback[] callbacks = this.mSurfaceHolder.getCallbacks();
                if (callbacks != null) {
                    for (SurfaceHolder.Callback c : callbacks) {
                        c.surfaceDestroyed(this.mSurfaceHolder);
                    }
                }
                onSurfaceDestroyed(this.mSurfaceHolder);
            }
        }

        /* access modifiers changed from: package-private */
        public void detach() {
            if (!this.mDestroyed) {
                this.mDestroyed = true;
                if (this.mDisplayManager != null) {
                    this.mDisplayManager.unregisterDisplayListener(this.mDisplayListener);
                }
                if (this.mVisible) {
                    this.mVisible = false;
                    onVisibilityChanged(false);
                }
                reportSurfaceDestroyed();
                onDestroy();
                if (this.mCreated) {
                    try {
                        if (this.mInputEventReceiver != null) {
                            this.mInputEventReceiver.dispose();
                            this.mInputEventReceiver = null;
                        }
                        this.mSession.remove(this.mWindow);
                    } catch (RemoteException e) {
                    }
                    this.mSurfaceHolder.mSurface.release();
                    this.mCreated = false;
                    if (this.mInputChannel != null) {
                        this.mInputChannel.dispose();
                        this.mInputChannel = null;
                    }
                }
            }
        }
    }

    class IWallpaperEngineWrapper extends IWallpaperEngine.Stub implements HandlerCaller.Callback {
        /* access modifiers changed from: private */
        public final HandlerCaller mCaller;
        final IWallpaperConnection mConnection;
        final Rect mDisplayPadding = new Rect();
        Engine mEngine;
        final boolean mIsPreview;
        int mReqHeight;
        int mReqWidth;
        boolean mShownReported;
        final IBinder mWindowToken;
        final int mWindowType;

        IWallpaperEngineWrapper(WallpaperService context, IWallpaperConnection conn, IBinder windowToken, int windowType, boolean isPreview, int reqWidth, int reqHeight, Rect padding) {
            this.mCaller = new HandlerCaller(context, context.getMainLooper(), this, true);
            this.mConnection = conn;
            this.mWindowToken = windowToken;
            this.mWindowType = windowType;
            this.mIsPreview = isPreview;
            this.mReqWidth = reqWidth;
            this.mReqHeight = reqHeight;
            this.mDisplayPadding.set(padding);
            this.mCaller.getHandler().sendMessageAtFrontOfQueue(this.mCaller.obtainMessage(10));
        }

        public void setDesiredSize(int width, int height) {
            this.mCaller.sendMessage(this.mCaller.obtainMessageII(30, width, height));
        }

        public void setDisplayPadding(Rect padding) {
            this.mCaller.sendMessage(this.mCaller.obtainMessageO(40, padding));
        }

        public void setVisibility(boolean visible) {
            this.mCaller.sendMessage(this.mCaller.obtainMessageI(10010, visible));
        }

        public void setInAmbientMode(boolean inAmbientDisplay, boolean animated) throws RemoteException {
            this.mCaller.sendMessage(this.mCaller.obtainMessageII(50, inAmbientDisplay, animated));
        }

        public void dispatchPointer(MotionEvent event) {
            if (this.mEngine != null) {
                this.mEngine.dispatchPointer(event);
            } else {
                event.recycle();
            }
        }

        public void dispatchWallpaperCommand(String action, int x, int y, int z, Bundle extras) {
            if (this.mEngine != null) {
                this.mEngine.mWindow.dispatchWallpaperCommand(action, x, y, z, extras, false);
            }
        }

        public void reportShown() {
            if (!this.mShownReported) {
                this.mShownReported = true;
                try {
                    this.mConnection.engineShown(this);
                } catch (RemoteException e) {
                    Log.w(WallpaperService.TAG, "Wallpaper host disappeared", e);
                }
            }
        }

        public void requestWallpaperColors() {
            this.mCaller.sendMessage(this.mCaller.obtainMessage(10050));
        }

        public void destroy() {
            this.mCaller.sendMessage(this.mCaller.obtainMessage(20));
        }

        public void executeMessage(Message message) {
            boolean z = false;
            switch (message.what) {
                case 10:
                    try {
                        this.mConnection.attachEngine(this);
                        Engine engine = WallpaperService.this.onCreateEngine();
                        this.mEngine = engine;
                        WallpaperService.this.mActiveEngines.add(engine);
                        engine.attach(this);
                        return;
                    } catch (RemoteException e) {
                        Log.w(WallpaperService.TAG, "Wallpaper host disappeared", e);
                        return;
                    }
                case 20:
                    WallpaperService.this.mActiveEngines.remove(this.mEngine);
                    this.mEngine.detach();
                    return;
                case 30:
                    this.mEngine.doDesiredSizeChanged(message.arg1, message.arg2);
                    return;
                case 40:
                    this.mEngine.doDisplayPaddingChanged((Rect) message.obj);
                    return;
                case 50:
                    Engine engine2 = this.mEngine;
                    boolean z2 = message.arg1 != 0;
                    if (message.arg2 != 0) {
                        z = true;
                    }
                    engine2.doAmbientModeChanged(z2, z);
                    return;
                case 10000:
                    this.mEngine.updateSurface(true, false, false);
                    break;
                case 10010:
                    Engine engine3 = this.mEngine;
                    if (message.arg1 != 0) {
                        z = true;
                    }
                    engine3.doVisibilityChanged(z);
                    break;
                case 10020:
                    this.mEngine.doOffsetsChanged(true);
                    break;
                case 10025:
                    this.mEngine.doCommand((WallpaperCommand) message.obj);
                    break;
                case 10030:
                    boolean reportDraw = message.arg1 != 0;
                    this.mEngine.mOutsets.set((Rect) message.obj);
                    this.mEngine.updateSurface(true, false, reportDraw);
                    this.mEngine.doOffsetsChanged(true);
                    break;
                case 10035:
                    break;
                case 10040:
                    boolean skip = false;
                    MotionEvent ev = (MotionEvent) message.obj;
                    if (ev.getAction() == 2) {
                        synchronized (this.mEngine.mLock) {
                            if (this.mEngine.mPendingMove == ev) {
                                this.mEngine.mPendingMove = null;
                            } else {
                                skip = true;
                            }
                        }
                    }
                    if (!skip) {
                        this.mEngine.onTouchEvent(ev);
                    }
                    ev.recycle();
                    break;
                case 10050:
                    if (this.mConnection != null) {
                        try {
                            this.mConnection.onWallpaperColorsChanged(this.mEngine.onComputeColors());
                            break;
                        } catch (RemoteException e2) {
                            break;
                        }
                    }
                    break;
                default:
                    Log.w(WallpaperService.TAG, "Unknown message type " + message.what);
                    break;
            }
        }
    }

    class IWallpaperServiceWrapper extends IWallpaperService.Stub {
        private final WallpaperService mTarget;

        public IWallpaperServiceWrapper(WallpaperService context) {
            this.mTarget = context;
        }

        public void attach(IWallpaperConnection conn, IBinder windowToken, int windowType, boolean isPreview, int reqWidth, int reqHeight, Rect padding) {
            new IWallpaperEngineWrapper(this.mTarget, conn, windowToken, windowType, isPreview, reqWidth, reqHeight, padding);
        }
    }

    static final class WallpaperCommand {
        String action;
        Bundle extras;
        boolean sync;
        int x;
        int y;
        int z;

        WallpaperCommand() {
        }
    }

    public abstract Engine onCreateEngine();

    public void onCreate() {
        super.onCreate();
    }

    public void onDestroy() {
        super.onDestroy();
        for (int i = 0; i < this.mActiveEngines.size(); i++) {
            this.mActiveEngines.get(i).detach();
        }
        this.mActiveEngines.clear();
    }

    public final IBinder onBind(Intent intent) {
        return new IWallpaperServiceWrapper(this);
    }

    /* access modifiers changed from: protected */
    public void dump(FileDescriptor fd, PrintWriter out, String[] args) {
        out.print("State of wallpaper ");
        out.print(this);
        out.println(SettingsStringUtil.DELIMITER);
        for (int i = 0; i < this.mActiveEngines.size(); i++) {
            Engine engine = this.mActiveEngines.get(i);
            out.print("  Engine ");
            out.print(engine);
            out.println(SettingsStringUtil.DELIMITER);
            engine.dump("    ", fd, out, args);
        }
    }
}
