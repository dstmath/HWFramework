package android.service.wallpaper;

import android.annotation.SystemApi;
import android.annotation.UnsupportedAppUsage;
import android.app.Service;
import android.app.WallpaperColors;
import android.content.Context;
import android.content.Intent;
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
import android.view.DisplayInfo;
import android.view.IWindowSession;
import android.view.InputChannel;
import android.view.InputEvent;
import android.view.InputEventReceiver;
import android.view.InsetsState;
import android.view.MotionEvent;
import android.view.SurfaceControl;
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
import java.util.concurrent.atomic.AtomicBoolean;
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
    @UnsupportedAppUsage
    private static final int MSG_WINDOW_RESIZED = 10030;
    private static final int NOTIFY_COLORS_RATE_LIMIT_MS = 1000;
    public static final String SERVICE_INTERFACE = "android.service.wallpaper.WallpaperService";
    public static final String SERVICE_META_DATA = "android.service.wallpaper";
    static final String TAG = "WallpaperService";
    private final ArrayList<Engine> mActiveEngines = new ArrayList<Engine>() {
        /* class android.service.wallpaper.WallpaperService.AnonymousClass1 */

        @Override // java.util.List, java.util.AbstractList, java.util.ArrayList
        public synchronized Engine remove(int index) {
            return index < size() ? (Engine) super.remove(index) : null;
        }

        public synchronized boolean contains(Object o) {
            return super.contains(o);
        }

        public synchronized boolean add(Engine o) {
            return super.add((Object) o);
        }

        @Override // java.util.List, java.util.AbstractList, java.util.ArrayList
        public synchronized Engine get(int index) {
            return index < size() ? (Engine) super.get(index) : null;
        }

        public synchronized int size() {
            return super.size();
        }
    };
    private Looper mRenderLooper = null;

    public abstract Engine onCreateEngine();

    /* access modifiers changed from: package-private */
    public static final class WallpaperCommand {
        String action;
        Bundle extras;
        boolean sync;
        int x;
        int y;
        int z;

        WallpaperCommand() {
        }
    }

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
        private Display mDisplay;
        private Context mDisplayContext;
        final DisplayCutout.ParcelableWrapper mDisplayCutout;
        private final DisplayManager.DisplayListener mDisplayListener;
        private int mDisplayState;
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
        final InsetsState mInsetsState;
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
        @UnsupportedAppUsage(maxTargetSdk = 28, trackingBug = 115609023)
        float mPendingXOffset;
        float mPendingXOffsetStep;
        float mPendingYOffset;
        float mPendingYOffsetStep;
        boolean mReportedVisible;
        IWindowSession mSession;
        final Rect mStableInsets;
        SurfaceControl mSurfaceControl;
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

        /* access modifiers changed from: package-private */
        public final class WallpaperInputEventReceiver extends InputEventReceiver {
            public WallpaperInputEventReceiver(InputChannel inputChannel, Looper looper) {
                super(inputChannel, looper);
            }

            @Override // android.view.InputEventReceiver
            public void onInputEvent(InputEvent event) {
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
            this.mInsetsState = new InsetsState();
            this.mMergedConfiguration = new MergedConfiguration();
            this.mLayout = new WindowManager.LayoutParams();
            this.mLock = new Object();
            this.mNotifyColorsChanged = new Runnable() {
                /* class android.service.wallpaper.$$Lambda$vsWBQpiXExY07tlrSzTqh4pNQAQ */

                public final void run() {
                    WallpaperService.Engine.this.notifyColorsChanged();
                }
            };
            this.mSurfaceControl = new SurfaceControl();
            this.mSurfaceHolder = new BaseSurfaceHolder() {
                /* class android.service.wallpaper.WallpaperService.Engine.AnonymousClass1 */

                {
                    this.mRequestedFormat = 2;
                }

                @Override // com.android.internal.view.BaseSurfaceHolder
                public boolean onAllowLockCanvas() {
                    return Engine.this.mDrawingAllowed;
                }

                @Override // com.android.internal.view.BaseSurfaceHolder
                public void onRelayoutContainer() {
                    Engine.this.mCaller.sendMessage(Engine.this.mCaller.obtainMessage(10000));
                }

                @Override // com.android.internal.view.BaseSurfaceHolder
                public void onUpdateSurface() {
                    Engine.this.mCaller.sendMessage(Engine.this.mCaller.obtainMessage(10000));
                }

                @Override // android.view.SurfaceHolder
                public boolean isCreating() {
                    return Engine.this.mIsCreating;
                }

                @Override // android.view.SurfaceHolder, com.android.internal.view.BaseSurfaceHolder
                public void setFixedSize(int width, int height) {
                    if (Engine.this.mFixedSizeAllowed) {
                        super.setFixedSize(width, height);
                        return;
                    }
                    throw new UnsupportedOperationException("Wallpapers currently only support sizing from layout");
                }

                @Override // android.view.SurfaceHolder
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

                @Override // android.view.SurfaceHolder, com.android.internal.view.BaseSurfaceHolder
                public Canvas lockCanvas() {
                    prepareToDraw();
                    return super.lockCanvas();
                }

                @Override // android.view.SurfaceHolder, com.android.internal.view.BaseSurfaceHolder
                public Canvas lockCanvas(Rect dirty) {
                    prepareToDraw();
                    return super.lockCanvas(dirty);
                }

                @Override // android.view.SurfaceHolder, com.android.internal.view.BaseSurfaceHolder
                public Canvas lockHardwareCanvas() {
                    prepareToDraw();
                    return super.lockHardwareCanvas();
                }
            };
            this.mWindow = new BaseIWindow() {
                /* class android.service.wallpaper.WallpaperService.Engine.AnonymousClass2 */

                @Override // com.android.internal.view.BaseIWindow, android.view.IWindow
                public void resized(Rect frame, Rect overscanInsets, Rect contentInsets, Rect visibleInsets, Rect stableInsets, Rect outsets, boolean reportDraw, MergedConfiguration mergedConfiguration, Rect backDropRect, boolean forceLayout, boolean alwaysConsumeSystemBars, int displayId, DisplayCutout.ParcelableWrapper displayCutout) {
                    Engine.this.mCaller.sendMessage(Engine.this.mCaller.obtainMessageIO(10030, reportDraw ? 1 : 0, outsets));
                }

                @Override // com.android.internal.view.BaseIWindow, android.view.IWindow
                public void moved(int newX, int newY) {
                    Engine.this.mCaller.sendMessage(Engine.this.mCaller.obtainMessageII(10035, newX, newY));
                }

                @Override // com.android.internal.view.BaseIWindow, android.view.IWindow
                public void dispatchAppVisibility(boolean visible) {
                    if (!Engine.this.mIWallpaperEngine.mIsPreview) {
                        Engine.this.mCaller.sendMessage(Engine.this.mCaller.obtainMessageI(10010, visible ? 1 : 0));
                    }
                }

                @Override // com.android.internal.view.BaseIWindow, android.view.IWindow
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

                @Override // com.android.internal.view.BaseIWindow, android.view.IWindow
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
                /* class android.service.wallpaper.WallpaperService.Engine.AnonymousClass3 */

                @Override // android.hardware.display.DisplayManager.DisplayListener
                public void onDisplayChanged(int displayId) {
                    if (Engine.this.mDisplay.getDisplayId() == displayId) {
                        Engine.this.reportVisibility();
                    }
                }

                @Override // android.hardware.display.DisplayManager.DisplayListener
                public void onDisplayRemoved(int displayId) {
                }

                @Override // android.hardware.display.DisplayManager.DisplayListener
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

        @SystemApi
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

        @UnsupportedAppUsage
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

        @SystemApi
        public void onAmbientModeChanged(boolean inAmbientMode, long animationDuration) {
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
                    return;
                }
                return;
            }
            this.mLastColorInvalidation = now;
            this.mHandler.removeCallbacks(this.mNotifyColorsChanged);
            try {
                WallpaperColors newColors = onComputeColors();
                if (this.mConnection != null) {
                    this.mConnection.onWallpaperColorsChanged(newColors, this.mDisplay.getDisplayId());
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
        /* access modifiers changed from: public */
        private void dispatchPointer(MotionEvent event) {
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
        /* JADX WARNING: Removed duplicated region for block: B:220:0x05cb A[Catch:{ RemoteException -> 0x05d8 }] */
        public void updateSurface(boolean forceRelayout, boolean forceReport, boolean redrawNeeded) {
            boolean fixedSize;
            boolean insetsChanged;
            boolean surfaceCreating;
            boolean creating;
            boolean formatChanged;
            boolean sizeChanged;
            int h;
            int h2;
            boolean sizeChanged2;
            boolean redrawNeeded2;
            if (this.mDestroyed) {
                Log.w(WallpaperService.TAG, "Ignoring updateSurface: destroyed");
            }
            boolean fixedSize2 = false;
            int myWidth = this.mSurfaceHolder.getRequestedWidth();
            if (myWidth <= 0) {
                myWidth = -1;
            } else {
                fixedSize2 = true;
            }
            int myHeight = this.mSurfaceHolder.getRequestedHeight();
            if (myHeight <= 0) {
                myHeight = -1;
                fixedSize = fixedSize2;
            } else {
                fixedSize = true;
            }
            boolean creating2 = !this.mCreated;
            boolean surfaceCreating2 = !this.mSurfaceCreated;
            boolean formatChanged2 = this.mFormat != this.mSurfaceHolder.getRequestedFormat();
            boolean sizeChanged3 = (this.mWidth == myWidth && this.mHeight == myHeight) ? false : true;
            boolean insetsChanged2 = !this.mCreated;
            boolean typeChanged = this.mType != this.mSurfaceHolder.getRequestedType();
            boolean flagsChanged = (this.mCurWindowFlags == this.mWindowFlags && this.mCurWindowPrivateFlags == this.mWindowPrivateFlags) ? false : true;
            if (forceRelayout || creating2 || surfaceCreating2 || formatChanged2 || sizeChanged3 || typeChanged || flagsChanged || redrawNeeded || !this.mIWallpaperEngine.mShownReported) {
                try {
                    this.mWidth = myWidth;
                    this.mHeight = myHeight;
                    this.mFormat = this.mSurfaceHolder.getRequestedFormat();
                    this.mType = this.mSurfaceHolder.getRequestedType();
                    this.mLayout.x = 0;
                    this.mLayout.y = 0;
                    if (!fixedSize) {
                        try {
                            this.mLayout.width = myWidth;
                            this.mLayout.height = myHeight;
                        } catch (RemoteException e) {
                            return;
                        }
                    } else {
                        DisplayInfo displayInfo = new DisplayInfo();
                        this.mDisplay.getDisplayInfo(displayInfo);
                        this.mLayout.width = Math.max(displayInfo.logicalWidth, myWidth);
                        this.mLayout.height = Math.max(displayInfo.logicalHeight, myHeight);
                        this.mWindowFlags |= 16384;
                    }
                    this.mLayout.format = this.mFormat;
                    this.mCurWindowFlags = this.mWindowFlags;
                    this.mLayout.flags = this.mWindowFlags | 512 | 65536 | 256 | 8;
                    this.mCurWindowPrivateFlags = this.mWindowPrivateFlags;
                    this.mLayout.privateFlags = this.mWindowPrivateFlags;
                    this.mLayout.memoryType = this.mType;
                    this.mLayout.token = this.mWindowToken;
                    if (!this.mCreated) {
                        try {
                            WallpaperService.this.obtainStyledAttributes(R.styleable.Window).recycle();
                            this.mLayout.type = this.mIWallpaperEngine.mWindowType;
                            this.mLayout.gravity = 8388659;
                            this.mLayout.setTitle(WallpaperService.this.getClass().getName());
                            this.mLayout.windowAnimations = R.style.Animation_Wallpaper;
                            this.mInputChannel = new InputChannel();
                            sizeChanged = sizeChanged3;
                            try {
                                try {
                                    try {
                                        formatChanged = formatChanged2;
                                        try {
                                            creating = creating2;
                                            try {
                                                surfaceCreating = surfaceCreating2;
                                                try {
                                                    insetsChanged = insetsChanged2;
                                                    try {
                                                        if (this.mSession.addToDisplay(this.mWindow, this.mWindow.mSeq, this.mLayout, 0, this.mDisplay.getDisplayId(), this.mWinFrame, this.mContentInsets, this.mStableInsets, this.mOutsets, this.mDisplayCutout, this.mInputChannel, this.mInsetsState) < 0) {
                                                            Log.w(WallpaperService.TAG, "Failed to add window while updating wallpaper surface.");
                                                            return;
                                                        } else {
                                                            this.mCreated = true;
                                                            this.mInputEventReceiver = new WallpaperInputEventReceiver(this.mInputChannel, Looper.myLooper());
                                                        }
                                                    } catch (RemoteException e2) {
                                                        return;
                                                    }
                                                } catch (RemoteException e3) {
                                                    return;
                                                }
                                            } catch (RemoteException e4) {
                                                return;
                                            }
                                        } catch (RemoteException e5) {
                                            return;
                                        }
                                    } catch (RemoteException e6) {
                                        return;
                                    }
                                } catch (RemoteException e7) {
                                    return;
                                }
                            } catch (RemoteException e8) {
                                return;
                            }
                        } catch (RemoteException e9) {
                            return;
                        }
                    } else {
                        creating = creating2;
                        surfaceCreating = surfaceCreating2;
                        formatChanged = formatChanged2;
                        sizeChanged = sizeChanged3;
                        insetsChanged = insetsChanged2;
                    }
                    try {
                        this.mSurfaceHolder.mSurfaceLock.lock();
                        this.mDrawingAllowed = true;
                        if (!fixedSize) {
                            this.mLayout.surfaceInsets.set(this.mIWallpaperEngine.mDisplayPadding);
                            this.mLayout.surfaceInsets.left += this.mOutsets.left;
                            this.mLayout.surfaceInsets.top += this.mOutsets.top;
                            this.mLayout.surfaceInsets.right += this.mOutsets.right;
                            this.mLayout.surfaceInsets.bottom += this.mOutsets.bottom;
                        } else {
                            this.mLayout.surfaceInsets.set(0, 0, 0, 0);
                        }
                        try {
                            try {
                                try {
                                    int relayoutResult = this.mSession.relayout(this.mWindow, this.mWindow.mSeq, this.mLayout, this.mWidth, this.mHeight, 0, 0, -1, this.mWinFrame, this.mOverscanInsets, this.mContentInsets, this.mVisibleInsets, this.mStableInsets, this.mOutsets, this.mBackdropFrame, this.mDisplayCutout, this.mMergedConfiguration, this.mSurfaceControl, this.mInsetsState);
                                    if (this.mSurfaceControl.isValid()) {
                                        this.mSurfaceHolder.mSurface.copyFrom(this.mSurfaceControl);
                                        this.mSurfaceControl.release();
                                    }
                                    int w = this.mWinFrame.width();
                                    int h3 = this.mWinFrame.height();
                                    if (!fixedSize) {
                                        Rect padding = this.mIWallpaperEngine.mDisplayPadding;
                                        int w2 = w + padding.left + padding.right + this.mOutsets.left + this.mOutsets.right;
                                        int h4 = h3 + padding.top + padding.bottom + this.mOutsets.top + this.mOutsets.bottom;
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
                                        h = h4;
                                        h2 = w2;
                                    } else {
                                        h = myHeight;
                                        h2 = myWidth;
                                    }
                                    if (this.mCurWidth != h2) {
                                        try {
                                            this.mCurWidth = h2;
                                            sizeChanged = true;
                                        } catch (RemoteException e10) {
                                            return;
                                        }
                                    }
                                    if (this.mCurHeight != h) {
                                        try {
                                            this.mCurHeight = h;
                                            sizeChanged2 = true;
                                        } catch (RemoteException e11) {
                                            return;
                                        }
                                    } else {
                                        sizeChanged2 = sizeChanged;
                                    }
                                    try {
                                        boolean insetsChanged3 = insetsChanged | (!this.mDispatchedOverscanInsets.equals(this.mOverscanInsets)) | (!this.mDispatchedContentInsets.equals(this.mContentInsets)) | (!this.mDispatchedStableInsets.equals(this.mStableInsets)) | (!this.mDispatchedOutsets.equals(this.mOutsets)) | (!this.mDispatchedDisplayCutout.equals(this.mDisplayCutout.get()));
                                        try {
                                            this.mSurfaceHolder.setSurfaceFrameSize(h2, h);
                                            this.mSurfaceHolder.mSurfaceLock.unlock();
                                            if (!this.mSurfaceHolder.mSurface.isValid()) {
                                                reportSurfaceDestroyed();
                                                return;
                                            }
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
                                                redrawNeeded2 = redrawNeeded | (creating || (relayoutResult & 2) != 0);
                                                if (forceReport || creating || surfaceCreating || formatChanged || sizeChanged2) {
                                                    didSurface = true;
                                                    try {
                                                        onSurfaceChanged(this.mSurfaceHolder, this.mFormat, this.mCurWidth, this.mCurHeight);
                                                        SurfaceHolder.Callback[] callbacks2 = this.mSurfaceHolder.getCallbacks();
                                                        if (callbacks2 != null) {
                                                            for (SurfaceHolder.Callback c2 : callbacks2) {
                                                                c2.surfaceChanged(this.mSurfaceHolder, this.mFormat, this.mCurWidth, this.mCurHeight);
                                                            }
                                                        }
                                                    } catch (Throwable th) {
                                                        th = th;
                                                        this.mIsCreating = false;
                                                        this.mSurfaceCreated = true;
                                                        if (redrawNeeded2) {
                                                        }
                                                        this.mIWallpaperEngine.reportShown();
                                                        throw th;
                                                    }
                                                }
                                                if (insetsChanged3) {
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
                                                    onApplyWindowInsets(new WindowInsets(this.mFinalSystemInsets, this.mFinalStableInsets, WallpaperService.this.getResources().getConfiguration().isScreenRound(), false, this.mDispatchedDisplayCutout));
                                                }
                                                if (redrawNeeded2) {
                                                    onSurfaceRedrawNeeded(this.mSurfaceHolder);
                                                    SurfaceHolder.Callback[] callbacks3 = this.mSurfaceHolder.getCallbacks();
                                                    if (callbacks3 != null) {
                                                        for (SurfaceHolder.Callback c3 : callbacks3) {
                                                            if (c3 instanceof SurfaceHolder.Callback2) {
                                                                ((SurfaceHolder.Callback2) c3).surfaceRedrawNeeded(this.mSurfaceHolder);
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
                                                } catch (RemoteException e12) {
                                                }
                                            } catch (Throwable th2) {
                                                th = th2;
                                                redrawNeeded2 = redrawNeeded;
                                                this.mIsCreating = false;
                                                this.mSurfaceCreated = true;
                                                if (redrawNeeded2) {
                                                    this.mSession.finishDrawing(this.mWindow);
                                                }
                                                this.mIWallpaperEngine.reportShown();
                                                throw th;
                                            }
                                        } catch (RemoteException e13) {
                                        }
                                    } catch (RemoteException e14) {
                                    }
                                } catch (RemoteException e15) {
                                }
                            } catch (RemoteException e16) {
                            }
                        } catch (RemoteException e17) {
                        }
                    } catch (RemoteException e18) {
                    }
                } catch (RemoteException e19) {
                }
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
                this.mIWallpaperEngine.mDisplayManager.registerDisplayListener(this.mDisplayListener, this.mCaller.getHandler());
                this.mDisplay = this.mIWallpaperEngine.mDisplay;
                this.mDisplayContext = WallpaperService.this.createDisplayContext(this.mDisplay);
                this.mDisplayState = this.mDisplay.getState();
                onCreate(this.mSurfaceHolder);
                this.mInitializing = false;
                this.mReportedVisible = false;
                updateSurface(false, false, false);
            }
        }

        public Context getDisplayContext() {
            return this.mDisplayContext;
        }

        @VisibleForTesting
        public void doAmbientModeChanged(boolean inAmbientMode, long animationDuration) {
            if (!this.mDestroyed) {
                this.mIsInAmbientMode = inAmbientMode;
                if (this.mCreated) {
                    onAmbientModeChanged(inAmbientMode, animationDuration);
                }
            }
        }

        /* access modifiers changed from: package-private */
        public void doDesiredSizeChanged(int desiredWidth, int desiredHeight) {
            if (!this.mDestroyed) {
                IWallpaperEngineWrapper iWallpaperEngineWrapper = this.mIWallpaperEngine;
                iWallpaperEngineWrapper.mReqWidth = desiredWidth;
                iWallpaperEngineWrapper.mReqHeight = desiredHeight;
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
                Display display = this.mDisplay;
                this.mDisplayState = display == null ? 0 : display.getState();
                boolean visible = true;
                if (!this.mVisible || this.mDisplayState == 1) {
                    visible = false;
                }
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
                if (this.mIWallpaperEngine.mDisplayManager != null) {
                    this.mIWallpaperEngine.mDisplayManager.unregisterDisplayListener(this.mDisplayListener);
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
                    InputChannel inputChannel = this.mInputChannel;
                    if (inputChannel != null) {
                        inputChannel.dispose();
                        this.mInputChannel = null;
                    }
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    public class IWallpaperEngineWrapper extends IWallpaperEngine.Stub implements HandlerCaller.Callback {
        private final HandlerCaller mCaller;
        final IWallpaperConnection mConnection;
        private final AtomicBoolean mDetached = new AtomicBoolean();
        final Display mDisplay;
        final int mDisplayId;
        final DisplayManager mDisplayManager;
        final Rect mDisplayPadding = new Rect();
        Engine mEngine;
        final boolean mIsPreview;
        int mReqHeight;
        int mReqWidth;
        boolean mShownReported;
        final IBinder mWindowToken;
        final int mWindowType;

        IWallpaperEngineWrapper(WallpaperService context, IWallpaperConnection conn, IBinder windowToken, int windowType, boolean isPreview, int reqWidth, int reqHeight, Rect padding, int displayId) {
            this.mCaller = new HandlerCaller(context, WallpaperService.this.getRenderLooper(), this, true);
            this.mConnection = conn;
            this.mWindowToken = windowToken;
            this.mWindowType = windowType;
            this.mIsPreview = isPreview;
            this.mReqWidth = reqWidth;
            this.mReqHeight = reqHeight;
            this.mDisplayPadding.set(padding);
            this.mDisplayId = displayId;
            this.mDisplayManager = (DisplayManager) WallpaperService.this.getSystemService(DisplayManager.class);
            this.mDisplay = this.mDisplayManager.getDisplay(this.mDisplayId);
            if (this.mDisplay != null) {
                this.mCaller.sendMessage(this.mCaller.obtainMessage(10));
                return;
            }
            throw new IllegalArgumentException("Cannot find display with id" + this.mDisplayId);
        }

        @Override // android.service.wallpaper.IWallpaperEngine
        public void setDesiredSize(int width, int height) {
            this.mCaller.sendMessage(this.mCaller.obtainMessageII(30, width, height));
        }

        @Override // android.service.wallpaper.IWallpaperEngine
        public void setDisplayPadding(Rect padding) {
            this.mCaller.sendMessage(this.mCaller.obtainMessageO(40, padding));
        }

        @Override // android.service.wallpaper.IWallpaperEngine
        public void setVisibility(boolean visible) {
            this.mCaller.sendMessage(this.mCaller.obtainMessageI(10010, visible ? 1 : 0));
        }

        @Override // android.service.wallpaper.IWallpaperEngine
        public void setInAmbientMode(boolean inAmbientDisplay, long animationDuration) throws RemoteException {
            this.mCaller.sendMessage(this.mCaller.obtainMessageIO(50, inAmbientDisplay ? 1 : 0, Long.valueOf(animationDuration)));
        }

        @Override // android.service.wallpaper.IWallpaperEngine
        public void dispatchPointer(MotionEvent event) {
            Engine engine = this.mEngine;
            if (engine != null) {
                engine.dispatchPointer(event);
            } else {
                event.recycle();
            }
        }

        @Override // android.service.wallpaper.IWallpaperEngine
        public void dispatchWallpaperCommand(String action, int x, int y, int z, Bundle extras) {
            Engine engine = this.mEngine;
            if (engine != null) {
                engine.mWindow.dispatchWallpaperCommand(action, x, y, z, extras, false);
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

        @Override // android.service.wallpaper.IWallpaperEngine
        public void requestWallpaperColors() {
            this.mCaller.sendMessage(this.mCaller.obtainMessage(10050));
        }

        @Override // android.service.wallpaper.IWallpaperEngine
        public void destroy() {
            this.mCaller.sendMessage(this.mCaller.obtainMessage(20));
        }

        public void detach() {
            this.mDetached.set(true);
        }

        private void doDetachEngine() {
            WallpaperService.this.mActiveEngines.remove(this.mEngine);
            this.mEngine.detach();
        }

        @Override // com.android.internal.os.HandlerCaller.Callback
        public void executeMessage(Message message) {
            if (!this.mDetached.get()) {
                boolean z = false;
                switch (message.what) {
                    case 10:
                        try {
                            this.mConnection.attachEngine(this, this.mDisplayId);
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
                        doDetachEngine();
                        return;
                    case 30:
                        this.mEngine.doDesiredSizeChanged(message.arg1, message.arg2);
                        return;
                    case 40:
                        this.mEngine.doDisplayPaddingChanged((Rect) message.obj);
                        return;
                    case 50:
                        Engine engine2 = this.mEngine;
                        if (message.arg1 != 0) {
                            z = true;
                        }
                        engine2.doAmbientModeChanged(z, ((Long) message.obj).longValue());
                        return;
                    case 10000:
                        this.mEngine.updateSurface(true, false, false);
                        return;
                    case 10010:
                        Engine engine3 = this.mEngine;
                        if (message.arg1 != 0) {
                            z = true;
                        }
                        engine3.doVisibilityChanged(z);
                        return;
                    case 10020:
                        this.mEngine.doOffsetsChanged(true);
                        return;
                    case 10025:
                        this.mEngine.doCommand((WallpaperCommand) message.obj);
                        return;
                    case 10030:
                        boolean reportDraw = message.arg1 != 0;
                        this.mEngine.mOutsets.set((Rect) message.obj);
                        this.mEngine.updateSurface(true, false, reportDraw);
                        this.mEngine.doOffsetsChanged(true);
                        return;
                    case 10035:
                        return;
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
                        return;
                    case 10050:
                        IWallpaperConnection iWallpaperConnection = this.mConnection;
                        if (iWallpaperConnection != null) {
                            try {
                                iWallpaperConnection.onWallpaperColorsChanged(this.mEngine.onComputeColors(), this.mDisplayId);
                                return;
                            } catch (RemoteException e2) {
                                return;
                            }
                        } else {
                            return;
                        }
                    default:
                        Log.w(WallpaperService.TAG, "Unknown message type " + message.what);
                        return;
                }
            } else if (WallpaperService.this.mActiveEngines.contains(this.mEngine)) {
                doDetachEngine();
            }
        }
    }

    class IWallpaperServiceWrapper extends IWallpaperService.Stub {
        private IWallpaperEngineWrapper mEngineWrapper;
        private final WallpaperService mTarget;

        public IWallpaperServiceWrapper(WallpaperService context) {
            this.mTarget = context;
        }

        @Override // android.service.wallpaper.IWallpaperService
        public void attach(IWallpaperConnection conn, IBinder windowToken, int windowType, boolean isPreview, int reqWidth, int reqHeight, Rect padding, int displayId) {
            this.mEngineWrapper = new IWallpaperEngineWrapper(this.mTarget, conn, windowToken, windowType, isPreview, reqWidth, reqHeight, padding, displayId);
        }

        @Override // android.service.wallpaper.IWallpaperService
        public void detach() {
            this.mEngineWrapper.detach();
        }
    }

    @Override // android.app.Service
    public void onCreate() {
        super.onCreate();
    }

    @Override // android.app.Service
    public void onDestroy() {
        super.onDestroy();
        Engine engine = this.mActiveEngines.remove(0);
        while (engine != null) {
            engine.detach();
            engine = this.mActiveEngines.remove(0);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private Looper getRenderLooper() {
        Log.i(TAG, "getRenderLooper : " + this.mRenderLooper);
        Looper looper = this.mRenderLooper;
        if (looper == null) {
            return getMainLooper();
        }
        return looper;
    }

    public void setRenderLooper(Looper looper) {
        this.mRenderLooper = looper;
        Log.i(TAG, "Custom rendering thread");
    }

    @Override // android.app.Service
    public final IBinder onBind(Intent intent) {
        return new IWallpaperServiceWrapper(this);
    }

    /* access modifiers changed from: protected */
    @Override // android.app.Service
    public void dump(FileDescriptor fd, PrintWriter out, String[] args) {
        Engine engine;
        out.print("State of wallpaper ");
        out.print(this);
        out.println(SettingsStringUtil.DELIMITER);
        int i = 0;
        while (i < this.mActiveEngines.size() && (engine = this.mActiveEngines.get(i)) != null) {
            out.print("  Engine ");
            out.print(engine);
            out.println(SettingsStringUtil.DELIMITER);
            engine.dump("    ", fd, out, args);
            i++;
        }
    }
}
