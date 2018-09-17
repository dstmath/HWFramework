package android.view;

import android.common.HwFrameworkFactory;
import android.content.Context;
import android.content.res.CompatibilityInfo.Translator;
import android.content.res.Configuration;
import android.graphics.Canvas;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff.Mode;
import android.graphics.Rect;
import android.graphics.Region;
import android.graphics.Region.Op;
import android.os.Handler;
import android.os.Message;
import android.os.ParcelFileDescriptor;
import android.os.RemoteException;
import android.os.SystemClock;
import android.rog.HwRogTranslater;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceHolder.Callback2;
import android.view.ViewTreeObserver.OnPreDrawListener;
import android.view.ViewTreeObserver.OnScrollChangedListener;
import android.view.WindowManager.LayoutParams;
import android.view.accessibility.AccessibilityNodeInfo;
import com.android.internal.telephony.RILConstants;
import com.android.internal.util.Protocol;
import com.android.internal.view.BaseIWindow;
import com.huawei.pgmng.log.LogPower;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.concurrent.locks.ReentrantLock;

public class SurfaceView extends View {
    private static final boolean DEBUG = false;
    static final int GET_NEW_SURFACE_MSG = 2;
    static final int KEEP_SCREEN_ON_MSG = 1;
    private static final String TAG = "SurfaceView";
    static final ArrayList<String> TARGET_GAME_SET = null;
    static final int UPDATE_WINDOW_MSG = 3;
    final Rect mBackdropFrame;
    final ArrayList<Callback> mCallbacks;
    final Configuration mConfiguration;
    final Rect mContentInsets;
    private final OnPreDrawListener mDrawListener;
    boolean mDrawingStopped;
    int mFormat;
    private boolean mGlobalListenersAdded;
    final Handler mHandler;
    boolean mHaveFrame;
    boolean mIsCreating;
    long mLastLockTime;
    int mLastSurfaceHeight;
    int mLastSurfaceWidth;
    final LayoutParams mLayout;
    final int[] mLocation;
    final Surface mNewSurface;
    final Rect mOutsets;
    final Rect mOverscanInsets;
    private Rect mRTLastReportedPosition;
    boolean mReportDrawNeeded;
    int mRequestedFormat;
    int mRequestedHeight;
    boolean mRequestedVisible;
    int mRequestedWidth;
    private HwRogTranslater mRogTranslater;
    private volatile boolean mRtHandlingPositionUpdates;
    private final OnScrollChangedListener mScrollChangedListener;
    IWindowSession mSession;
    final Rect mStableInsets;
    final Surface mSurface;
    boolean mSurfaceCreated;
    final Rect mSurfaceFrame;
    private final SurfaceHolder mSurfaceHolder;
    final ReentrantLock mSurfaceLock;
    private Translator mTranslator;
    boolean mUpdateWindowNeeded;
    boolean mViewVisibility;
    boolean mVisible;
    final Rect mVisibleInsets;
    final Rect mWinFrame;
    MyWindow mWindow;
    private int mWindowInsetLeft;
    private int mWindowInsetTop;
    int mWindowSpaceHeight;
    int mWindowSpaceLeft;
    int mWindowSpaceTop;
    int mWindowSpaceWidth;
    int mWindowType;
    boolean mWindowVisibility;

    private static class MyWindow extends BaseIWindow {
        int mCurHeight;
        int mCurWidth;
        private final WeakReference<SurfaceView> mSurfaceView;

        public MyWindow(SurfaceView surfaceView) {
            this.mCurWidth = -1;
            this.mCurHeight = -1;
            this.mSurfaceView = new WeakReference(surfaceView);
        }

        public void resized(Rect frame, Rect overscanInsets, Rect contentInsets, Rect visibleInsets, Rect stableInsets, Rect outsets, boolean reportDraw, Configuration newConfig, Rect backDropRect, boolean forceLayout, boolean alwaysConsumeNavBar) {
            SurfaceView surfaceView = (SurfaceView) this.mSurfaceView.get();
            if (surfaceView != null) {
                surfaceView.mSurfaceLock.lock();
                if (reportDraw) {
                    try {
                        surfaceView.mUpdateWindowNeeded = true;
                        surfaceView.mReportDrawNeeded = true;
                        surfaceView.mHandler.sendEmptyMessage(SurfaceView.UPDATE_WINDOW_MSG);
                    } catch (Throwable th) {
                        surfaceView.mSurfaceLock.unlock();
                    }
                } else {
                    if (surfaceView.mWinFrame.width() == frame.width() && surfaceView.mWinFrame.height() == frame.height()) {
                        if (forceLayout) {
                        }
                    }
                    surfaceView.mUpdateWindowNeeded = true;
                    surfaceView.mHandler.sendEmptyMessage(SurfaceView.UPDATE_WINDOW_MSG);
                }
                surfaceView.mSurfaceLock.unlock();
            }
        }

        public void dispatchAppVisibility(boolean visible) {
        }

        public void dispatchGetNewSurface() {
            SurfaceView surfaceView = (SurfaceView) this.mSurfaceView.get();
            if (surfaceView != null) {
                surfaceView.mHandler.sendMessage(surfaceView.mHandler.obtainMessage(SurfaceView.GET_NEW_SURFACE_MSG));
            }
        }

        public void windowFocusChanged(boolean hasFocus, boolean touchEnabled) {
            Log.w(SurfaceView.TAG, "Unexpected focus in surface: focus=" + hasFocus + ", touchEnabled=" + touchEnabled);
        }

        public void executeCommand(String command, String parameters, ParcelFileDescriptor out) {
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.view.SurfaceView.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.view.SurfaceView.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: android.view.SurfaceView.<clinit>():void");
    }

    public SurfaceView(Context context) {
        super(context);
        this.mCallbacks = new ArrayList();
        this.mLocation = new int[GET_NEW_SURFACE_MSG];
        this.mSurfaceLock = new ReentrantLock();
        this.mSurface = new Surface();
        this.mNewSurface = new Surface();
        this.mDrawingStopped = true;
        this.mLayout = new LayoutParams();
        this.mVisibleInsets = new Rect();
        this.mWinFrame = new Rect();
        this.mOverscanInsets = new Rect();
        this.mContentInsets = new Rect();
        this.mStableInsets = new Rect();
        this.mOutsets = new Rect();
        this.mBackdropFrame = new Rect();
        this.mConfiguration = new Configuration();
        TARGET_GAME_SET.add("com.tencent.tmgp.sgame");
        TARGET_GAME_SET.add("com.tencent.tmgp.ylm");
        TARGET_GAME_SET.add("com.tencent.tmgp.cf");
        TARGET_GAME_SET.add("com.tencent.KiHan");
        this.mWindowType = RILConstants.RIL_UNSOL_RESPONSE_CALL_STATE_CHANGED;
        this.mIsCreating = DEBUG;
        this.mRtHandlingPositionUpdates = DEBUG;
        this.mHandler = new Handler() {
            public void handleMessage(Message msg) {
                boolean z = SurfaceView.DEBUG;
                switch (msg.what) {
                    case SurfaceView.KEEP_SCREEN_ON_MSG /*1*/:
                        SurfaceView surfaceView = SurfaceView.this;
                        if (msg.arg1 != 0) {
                            z = true;
                        }
                        surfaceView.setKeepScreenOn(z);
                    case SurfaceView.GET_NEW_SURFACE_MSG /*2*/:
                        SurfaceView.this.handleGetNewSurface();
                    case SurfaceView.UPDATE_WINDOW_MSG /*3*/:
                        SurfaceView.this.updateWindow(SurfaceView.DEBUG, SurfaceView.DEBUG);
                    default:
                }
            }
        };
        this.mScrollChangedListener = new OnScrollChangedListener() {
            public void onScrollChanged() {
                SurfaceView.this.updateWindow(SurfaceView.DEBUG, SurfaceView.DEBUG);
            }
        };
        this.mDrawListener = new OnPreDrawListener() {
            public boolean onPreDraw() {
                boolean z;
                SurfaceView surfaceView = SurfaceView.this;
                if (SurfaceView.this.getWidth() <= 0 || SurfaceView.this.getHeight() <= 0) {
                    z = SurfaceView.DEBUG;
                } else {
                    z = true;
                }
                surfaceView.mHaveFrame = z;
                SurfaceView.this.updateWindow(SurfaceView.DEBUG, SurfaceView.DEBUG);
                return true;
            }
        };
        this.mRequestedVisible = DEBUG;
        this.mWindowVisibility = DEBUG;
        this.mViewVisibility = DEBUG;
        this.mRequestedWidth = -1;
        this.mRequestedHeight = -1;
        this.mRequestedFormat = 4;
        this.mHaveFrame = DEBUG;
        this.mSurfaceCreated = DEBUG;
        this.mLastLockTime = 0;
        this.mVisible = DEBUG;
        this.mWindowSpaceLeft = -1;
        this.mWindowSpaceTop = -1;
        this.mWindowSpaceWidth = -1;
        this.mWindowSpaceHeight = -1;
        this.mFormat = -1;
        this.mSurfaceFrame = new Rect();
        this.mLastSurfaceWidth = -1;
        this.mLastSurfaceHeight = -1;
        this.mRTLastReportedPosition = new Rect();
        this.mSurfaceHolder = new SurfaceHolder() {
            private static final String LOG_TAG = "SurfaceHolder";

            public boolean isCreating() {
                return SurfaceView.this.mIsCreating;
            }

            public void addCallback(Callback callback) {
                synchronized (SurfaceView.this.mCallbacks) {
                    if (!SurfaceView.this.mCallbacks.contains(callback)) {
                        SurfaceView.this.mCallbacks.add(callback);
                    }
                }
            }

            public void removeCallback(Callback callback) {
                synchronized (SurfaceView.this.mCallbacks) {
                    SurfaceView.this.mCallbacks.remove(callback);
                }
            }

            public void setFixedSize(int width, int height) {
                if (SurfaceView.this.mRequestedWidth != width || SurfaceView.this.mRequestedHeight != height) {
                    SurfaceView.this.mRequestedWidth = width;
                    SurfaceView.this.mRequestedHeight = height;
                    SurfaceView.this.requestLayout();
                }
            }

            public void setSizeFromLayout() {
                if (SurfaceView.this.mRequestedWidth != -1 || SurfaceView.this.mRequestedHeight != -1) {
                    SurfaceView surfaceView = SurfaceView.this;
                    SurfaceView.this.mRequestedHeight = -1;
                    surfaceView.mRequestedWidth = -1;
                    SurfaceView.this.requestLayout();
                }
            }

            public void setFormat(int format) {
                if (format == -1) {
                    format = 4;
                }
                SurfaceView.this.mRequestedFormat = format;
                if (SurfaceView.this.mWindow != null) {
                    SurfaceView.this.updateWindow(SurfaceView.DEBUG, SurfaceView.DEBUG);
                }
            }

            @Deprecated
            public void setType(int type) {
            }

            public void setKeepScreenOn(boolean screenOn) {
                int i = SurfaceView.KEEP_SCREEN_ON_MSG;
                Message msg = SurfaceView.this.mHandler.obtainMessage(SurfaceView.KEEP_SCREEN_ON_MSG);
                if (!screenOn) {
                    i = 0;
                }
                msg.arg1 = i;
                SurfaceView.this.mHandler.sendMessage(msg);
            }

            public Canvas lockCanvas() {
                return internalLockCanvas(null);
            }

            public Canvas lockCanvas(Rect inOutDirty) {
                return internalLockCanvas(inOutDirty);
            }

            private final Canvas internalLockCanvas(Rect dirty) {
                SurfaceView.this.mSurfaceLock.lock();
                Canvas c = null;
                if (!(SurfaceView.this.mDrawingStopped || SurfaceView.this.mWindow == null)) {
                    try {
                        c = SurfaceView.this.mSurface.lockCanvas(dirty);
                    } catch (Exception e) {
                        Log.e(LOG_TAG, "Exception locking surface", e);
                    }
                }
                if (c != null) {
                    SurfaceView.this.mLastLockTime = SystemClock.uptimeMillis();
                    return c;
                }
                long now = SystemClock.uptimeMillis();
                long nextTime = SurfaceView.this.mLastLockTime + 100;
                if (nextTime > now) {
                    try {
                        Thread.sleep(nextTime - now);
                    } catch (InterruptedException e2) {
                    }
                    now = SystemClock.uptimeMillis();
                }
                SurfaceView.this.mLastLockTime = now;
                SurfaceView.this.mSurfaceLock.unlock();
                return null;
            }

            public void unlockCanvasAndPost(Canvas canvas) {
                SurfaceView.this.mSurface.unlockCanvasAndPost(canvas);
                SurfaceView.this.mSurfaceLock.unlock();
                if (HwFrameworkFactory.getHwNsdImpl().isSupportAps() && HwFrameworkFactory.getHwNsdImpl().isAPSReady()) {
                    HwFrameworkFactory.getHwNsdImpl().powerCtroll();
                }
            }

            public Surface getSurface() {
                return SurfaceView.this.mSurface;
            }

            public Rect getSurfaceFrame() {
                return SurfaceView.this.mSurfaceFrame;
            }
        };
        init();
    }

    public SurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mCallbacks = new ArrayList();
        this.mLocation = new int[GET_NEW_SURFACE_MSG];
        this.mSurfaceLock = new ReentrantLock();
        this.mSurface = new Surface();
        this.mNewSurface = new Surface();
        this.mDrawingStopped = true;
        this.mLayout = new LayoutParams();
        this.mVisibleInsets = new Rect();
        this.mWinFrame = new Rect();
        this.mOverscanInsets = new Rect();
        this.mContentInsets = new Rect();
        this.mStableInsets = new Rect();
        this.mOutsets = new Rect();
        this.mBackdropFrame = new Rect();
        this.mConfiguration = new Configuration();
        TARGET_GAME_SET.add("com.tencent.tmgp.sgame");
        TARGET_GAME_SET.add("com.tencent.tmgp.ylm");
        TARGET_GAME_SET.add("com.tencent.tmgp.cf");
        TARGET_GAME_SET.add("com.tencent.KiHan");
        this.mWindowType = RILConstants.RIL_UNSOL_RESPONSE_CALL_STATE_CHANGED;
        this.mIsCreating = DEBUG;
        this.mRtHandlingPositionUpdates = DEBUG;
        this.mHandler = new Handler() {
            public void handleMessage(Message msg) {
                boolean z = SurfaceView.DEBUG;
                switch (msg.what) {
                    case SurfaceView.KEEP_SCREEN_ON_MSG /*1*/:
                        SurfaceView surfaceView = SurfaceView.this;
                        if (msg.arg1 != 0) {
                            z = true;
                        }
                        surfaceView.setKeepScreenOn(z);
                    case SurfaceView.GET_NEW_SURFACE_MSG /*2*/:
                        SurfaceView.this.handleGetNewSurface();
                    case SurfaceView.UPDATE_WINDOW_MSG /*3*/:
                        SurfaceView.this.updateWindow(SurfaceView.DEBUG, SurfaceView.DEBUG);
                    default:
                }
            }
        };
        this.mScrollChangedListener = new OnScrollChangedListener() {
            public void onScrollChanged() {
                SurfaceView.this.updateWindow(SurfaceView.DEBUG, SurfaceView.DEBUG);
            }
        };
        this.mDrawListener = new OnPreDrawListener() {
            public boolean onPreDraw() {
                boolean z;
                SurfaceView surfaceView = SurfaceView.this;
                if (SurfaceView.this.getWidth() <= 0 || SurfaceView.this.getHeight() <= 0) {
                    z = SurfaceView.DEBUG;
                } else {
                    z = true;
                }
                surfaceView.mHaveFrame = z;
                SurfaceView.this.updateWindow(SurfaceView.DEBUG, SurfaceView.DEBUG);
                return true;
            }
        };
        this.mRequestedVisible = DEBUG;
        this.mWindowVisibility = DEBUG;
        this.mViewVisibility = DEBUG;
        this.mRequestedWidth = -1;
        this.mRequestedHeight = -1;
        this.mRequestedFormat = 4;
        this.mHaveFrame = DEBUG;
        this.mSurfaceCreated = DEBUG;
        this.mLastLockTime = 0;
        this.mVisible = DEBUG;
        this.mWindowSpaceLeft = -1;
        this.mWindowSpaceTop = -1;
        this.mWindowSpaceWidth = -1;
        this.mWindowSpaceHeight = -1;
        this.mFormat = -1;
        this.mSurfaceFrame = new Rect();
        this.mLastSurfaceWidth = -1;
        this.mLastSurfaceHeight = -1;
        this.mRTLastReportedPosition = new Rect();
        this.mSurfaceHolder = new SurfaceHolder() {
            private static final String LOG_TAG = "SurfaceHolder";

            public boolean isCreating() {
                return SurfaceView.this.mIsCreating;
            }

            public void addCallback(Callback callback) {
                synchronized (SurfaceView.this.mCallbacks) {
                    if (!SurfaceView.this.mCallbacks.contains(callback)) {
                        SurfaceView.this.mCallbacks.add(callback);
                    }
                }
            }

            public void removeCallback(Callback callback) {
                synchronized (SurfaceView.this.mCallbacks) {
                    SurfaceView.this.mCallbacks.remove(callback);
                }
            }

            public void setFixedSize(int width, int height) {
                if (SurfaceView.this.mRequestedWidth != width || SurfaceView.this.mRequestedHeight != height) {
                    SurfaceView.this.mRequestedWidth = width;
                    SurfaceView.this.mRequestedHeight = height;
                    SurfaceView.this.requestLayout();
                }
            }

            public void setSizeFromLayout() {
                if (SurfaceView.this.mRequestedWidth != -1 || SurfaceView.this.mRequestedHeight != -1) {
                    SurfaceView surfaceView = SurfaceView.this;
                    SurfaceView.this.mRequestedHeight = -1;
                    surfaceView.mRequestedWidth = -1;
                    SurfaceView.this.requestLayout();
                }
            }

            public void setFormat(int format) {
                if (format == -1) {
                    format = 4;
                }
                SurfaceView.this.mRequestedFormat = format;
                if (SurfaceView.this.mWindow != null) {
                    SurfaceView.this.updateWindow(SurfaceView.DEBUG, SurfaceView.DEBUG);
                }
            }

            @Deprecated
            public void setType(int type) {
            }

            public void setKeepScreenOn(boolean screenOn) {
                int i = SurfaceView.KEEP_SCREEN_ON_MSG;
                Message msg = SurfaceView.this.mHandler.obtainMessage(SurfaceView.KEEP_SCREEN_ON_MSG);
                if (!screenOn) {
                    i = 0;
                }
                msg.arg1 = i;
                SurfaceView.this.mHandler.sendMessage(msg);
            }

            public Canvas lockCanvas() {
                return internalLockCanvas(null);
            }

            public Canvas lockCanvas(Rect inOutDirty) {
                return internalLockCanvas(inOutDirty);
            }

            private final Canvas internalLockCanvas(Rect dirty) {
                SurfaceView.this.mSurfaceLock.lock();
                Canvas c = null;
                if (!(SurfaceView.this.mDrawingStopped || SurfaceView.this.mWindow == null)) {
                    try {
                        c = SurfaceView.this.mSurface.lockCanvas(dirty);
                    } catch (Exception e) {
                        Log.e(LOG_TAG, "Exception locking surface", e);
                    }
                }
                if (c != null) {
                    SurfaceView.this.mLastLockTime = SystemClock.uptimeMillis();
                    return c;
                }
                long now = SystemClock.uptimeMillis();
                long nextTime = SurfaceView.this.mLastLockTime + 100;
                if (nextTime > now) {
                    try {
                        Thread.sleep(nextTime - now);
                    } catch (InterruptedException e2) {
                    }
                    now = SystemClock.uptimeMillis();
                }
                SurfaceView.this.mLastLockTime = now;
                SurfaceView.this.mSurfaceLock.unlock();
                return null;
            }

            public void unlockCanvasAndPost(Canvas canvas) {
                SurfaceView.this.mSurface.unlockCanvasAndPost(canvas);
                SurfaceView.this.mSurfaceLock.unlock();
                if (HwFrameworkFactory.getHwNsdImpl().isSupportAps() && HwFrameworkFactory.getHwNsdImpl().isAPSReady()) {
                    HwFrameworkFactory.getHwNsdImpl().powerCtroll();
                }
            }

            public Surface getSurface() {
                return SurfaceView.this.mSurface;
            }

            public Rect getSurfaceFrame() {
                return SurfaceView.this.mSurfaceFrame;
            }
        };
        init();
    }

    public SurfaceView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.mCallbacks = new ArrayList();
        this.mLocation = new int[GET_NEW_SURFACE_MSG];
        this.mSurfaceLock = new ReentrantLock();
        this.mSurface = new Surface();
        this.mNewSurface = new Surface();
        this.mDrawingStopped = true;
        this.mLayout = new LayoutParams();
        this.mVisibleInsets = new Rect();
        this.mWinFrame = new Rect();
        this.mOverscanInsets = new Rect();
        this.mContentInsets = new Rect();
        this.mStableInsets = new Rect();
        this.mOutsets = new Rect();
        this.mBackdropFrame = new Rect();
        this.mConfiguration = new Configuration();
        TARGET_GAME_SET.add("com.tencent.tmgp.sgame");
        TARGET_GAME_SET.add("com.tencent.tmgp.ylm");
        TARGET_GAME_SET.add("com.tencent.tmgp.cf");
        TARGET_GAME_SET.add("com.tencent.KiHan");
        this.mWindowType = RILConstants.RIL_UNSOL_RESPONSE_CALL_STATE_CHANGED;
        this.mIsCreating = DEBUG;
        this.mRtHandlingPositionUpdates = DEBUG;
        this.mHandler = new Handler() {
            public void handleMessage(Message msg) {
                boolean z = SurfaceView.DEBUG;
                switch (msg.what) {
                    case SurfaceView.KEEP_SCREEN_ON_MSG /*1*/:
                        SurfaceView surfaceView = SurfaceView.this;
                        if (msg.arg1 != 0) {
                            z = true;
                        }
                        surfaceView.setKeepScreenOn(z);
                    case SurfaceView.GET_NEW_SURFACE_MSG /*2*/:
                        SurfaceView.this.handleGetNewSurface();
                    case SurfaceView.UPDATE_WINDOW_MSG /*3*/:
                        SurfaceView.this.updateWindow(SurfaceView.DEBUG, SurfaceView.DEBUG);
                    default:
                }
            }
        };
        this.mScrollChangedListener = new OnScrollChangedListener() {
            public void onScrollChanged() {
                SurfaceView.this.updateWindow(SurfaceView.DEBUG, SurfaceView.DEBUG);
            }
        };
        this.mDrawListener = new OnPreDrawListener() {
            public boolean onPreDraw() {
                boolean z;
                SurfaceView surfaceView = SurfaceView.this;
                if (SurfaceView.this.getWidth() <= 0 || SurfaceView.this.getHeight() <= 0) {
                    z = SurfaceView.DEBUG;
                } else {
                    z = true;
                }
                surfaceView.mHaveFrame = z;
                SurfaceView.this.updateWindow(SurfaceView.DEBUG, SurfaceView.DEBUG);
                return true;
            }
        };
        this.mRequestedVisible = DEBUG;
        this.mWindowVisibility = DEBUG;
        this.mViewVisibility = DEBUG;
        this.mRequestedWidth = -1;
        this.mRequestedHeight = -1;
        this.mRequestedFormat = 4;
        this.mHaveFrame = DEBUG;
        this.mSurfaceCreated = DEBUG;
        this.mLastLockTime = 0;
        this.mVisible = DEBUG;
        this.mWindowSpaceLeft = -1;
        this.mWindowSpaceTop = -1;
        this.mWindowSpaceWidth = -1;
        this.mWindowSpaceHeight = -1;
        this.mFormat = -1;
        this.mSurfaceFrame = new Rect();
        this.mLastSurfaceWidth = -1;
        this.mLastSurfaceHeight = -1;
        this.mRTLastReportedPosition = new Rect();
        this.mSurfaceHolder = new SurfaceHolder() {
            private static final String LOG_TAG = "SurfaceHolder";

            public boolean isCreating() {
                return SurfaceView.this.mIsCreating;
            }

            public void addCallback(Callback callback) {
                synchronized (SurfaceView.this.mCallbacks) {
                    if (!SurfaceView.this.mCallbacks.contains(callback)) {
                        SurfaceView.this.mCallbacks.add(callback);
                    }
                }
            }

            public void removeCallback(Callback callback) {
                synchronized (SurfaceView.this.mCallbacks) {
                    SurfaceView.this.mCallbacks.remove(callback);
                }
            }

            public void setFixedSize(int width, int height) {
                if (SurfaceView.this.mRequestedWidth != width || SurfaceView.this.mRequestedHeight != height) {
                    SurfaceView.this.mRequestedWidth = width;
                    SurfaceView.this.mRequestedHeight = height;
                    SurfaceView.this.requestLayout();
                }
            }

            public void setSizeFromLayout() {
                if (SurfaceView.this.mRequestedWidth != -1 || SurfaceView.this.mRequestedHeight != -1) {
                    SurfaceView surfaceView = SurfaceView.this;
                    SurfaceView.this.mRequestedHeight = -1;
                    surfaceView.mRequestedWidth = -1;
                    SurfaceView.this.requestLayout();
                }
            }

            public void setFormat(int format) {
                if (format == -1) {
                    format = 4;
                }
                SurfaceView.this.mRequestedFormat = format;
                if (SurfaceView.this.mWindow != null) {
                    SurfaceView.this.updateWindow(SurfaceView.DEBUG, SurfaceView.DEBUG);
                }
            }

            @Deprecated
            public void setType(int type) {
            }

            public void setKeepScreenOn(boolean screenOn) {
                int i = SurfaceView.KEEP_SCREEN_ON_MSG;
                Message msg = SurfaceView.this.mHandler.obtainMessage(SurfaceView.KEEP_SCREEN_ON_MSG);
                if (!screenOn) {
                    i = 0;
                }
                msg.arg1 = i;
                SurfaceView.this.mHandler.sendMessage(msg);
            }

            public Canvas lockCanvas() {
                return internalLockCanvas(null);
            }

            public Canvas lockCanvas(Rect inOutDirty) {
                return internalLockCanvas(inOutDirty);
            }

            private final Canvas internalLockCanvas(Rect dirty) {
                SurfaceView.this.mSurfaceLock.lock();
                Canvas c = null;
                if (!(SurfaceView.this.mDrawingStopped || SurfaceView.this.mWindow == null)) {
                    try {
                        c = SurfaceView.this.mSurface.lockCanvas(dirty);
                    } catch (Exception e) {
                        Log.e(LOG_TAG, "Exception locking surface", e);
                    }
                }
                if (c != null) {
                    SurfaceView.this.mLastLockTime = SystemClock.uptimeMillis();
                    return c;
                }
                long now = SystemClock.uptimeMillis();
                long nextTime = SurfaceView.this.mLastLockTime + 100;
                if (nextTime > now) {
                    try {
                        Thread.sleep(nextTime - now);
                    } catch (InterruptedException e2) {
                    }
                    now = SystemClock.uptimeMillis();
                }
                SurfaceView.this.mLastLockTime = now;
                SurfaceView.this.mSurfaceLock.unlock();
                return null;
            }

            public void unlockCanvasAndPost(Canvas canvas) {
                SurfaceView.this.mSurface.unlockCanvasAndPost(canvas);
                SurfaceView.this.mSurfaceLock.unlock();
                if (HwFrameworkFactory.getHwNsdImpl().isSupportAps() && HwFrameworkFactory.getHwNsdImpl().isAPSReady()) {
                    HwFrameworkFactory.getHwNsdImpl().powerCtroll();
                }
            }

            public Surface getSurface() {
                return SurfaceView.this.mSurface;
            }

            public Rect getSurfaceFrame() {
                return SurfaceView.this.mSurfaceFrame;
            }
        };
        init();
    }

    public SurfaceView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        this.mCallbacks = new ArrayList();
        this.mLocation = new int[GET_NEW_SURFACE_MSG];
        this.mSurfaceLock = new ReentrantLock();
        this.mSurface = new Surface();
        this.mNewSurface = new Surface();
        this.mDrawingStopped = true;
        this.mLayout = new LayoutParams();
        this.mVisibleInsets = new Rect();
        this.mWinFrame = new Rect();
        this.mOverscanInsets = new Rect();
        this.mContentInsets = new Rect();
        this.mStableInsets = new Rect();
        this.mOutsets = new Rect();
        this.mBackdropFrame = new Rect();
        this.mConfiguration = new Configuration();
        TARGET_GAME_SET.add("com.tencent.tmgp.sgame");
        TARGET_GAME_SET.add("com.tencent.tmgp.ylm");
        TARGET_GAME_SET.add("com.tencent.tmgp.cf");
        TARGET_GAME_SET.add("com.tencent.KiHan");
        this.mWindowType = RILConstants.RIL_UNSOL_RESPONSE_CALL_STATE_CHANGED;
        this.mIsCreating = DEBUG;
        this.mRtHandlingPositionUpdates = DEBUG;
        this.mHandler = new Handler() {
            public void handleMessage(Message msg) {
                boolean z = SurfaceView.DEBUG;
                switch (msg.what) {
                    case SurfaceView.KEEP_SCREEN_ON_MSG /*1*/:
                        SurfaceView surfaceView = SurfaceView.this;
                        if (msg.arg1 != 0) {
                            z = true;
                        }
                        surfaceView.setKeepScreenOn(z);
                    case SurfaceView.GET_NEW_SURFACE_MSG /*2*/:
                        SurfaceView.this.handleGetNewSurface();
                    case SurfaceView.UPDATE_WINDOW_MSG /*3*/:
                        SurfaceView.this.updateWindow(SurfaceView.DEBUG, SurfaceView.DEBUG);
                    default:
                }
            }
        };
        this.mScrollChangedListener = new OnScrollChangedListener() {
            public void onScrollChanged() {
                SurfaceView.this.updateWindow(SurfaceView.DEBUG, SurfaceView.DEBUG);
            }
        };
        this.mDrawListener = new OnPreDrawListener() {
            public boolean onPreDraw() {
                boolean z;
                SurfaceView surfaceView = SurfaceView.this;
                if (SurfaceView.this.getWidth() <= 0 || SurfaceView.this.getHeight() <= 0) {
                    z = SurfaceView.DEBUG;
                } else {
                    z = true;
                }
                surfaceView.mHaveFrame = z;
                SurfaceView.this.updateWindow(SurfaceView.DEBUG, SurfaceView.DEBUG);
                return true;
            }
        };
        this.mRequestedVisible = DEBUG;
        this.mWindowVisibility = DEBUG;
        this.mViewVisibility = DEBUG;
        this.mRequestedWidth = -1;
        this.mRequestedHeight = -1;
        this.mRequestedFormat = 4;
        this.mHaveFrame = DEBUG;
        this.mSurfaceCreated = DEBUG;
        this.mLastLockTime = 0;
        this.mVisible = DEBUG;
        this.mWindowSpaceLeft = -1;
        this.mWindowSpaceTop = -1;
        this.mWindowSpaceWidth = -1;
        this.mWindowSpaceHeight = -1;
        this.mFormat = -1;
        this.mSurfaceFrame = new Rect();
        this.mLastSurfaceWidth = -1;
        this.mLastSurfaceHeight = -1;
        this.mRTLastReportedPosition = new Rect();
        this.mSurfaceHolder = new SurfaceHolder() {
            private static final String LOG_TAG = "SurfaceHolder";

            public boolean isCreating() {
                return SurfaceView.this.mIsCreating;
            }

            public void addCallback(Callback callback) {
                synchronized (SurfaceView.this.mCallbacks) {
                    if (!SurfaceView.this.mCallbacks.contains(callback)) {
                        SurfaceView.this.mCallbacks.add(callback);
                    }
                }
            }

            public void removeCallback(Callback callback) {
                synchronized (SurfaceView.this.mCallbacks) {
                    SurfaceView.this.mCallbacks.remove(callback);
                }
            }

            public void setFixedSize(int width, int height) {
                if (SurfaceView.this.mRequestedWidth != width || SurfaceView.this.mRequestedHeight != height) {
                    SurfaceView.this.mRequestedWidth = width;
                    SurfaceView.this.mRequestedHeight = height;
                    SurfaceView.this.requestLayout();
                }
            }

            public void setSizeFromLayout() {
                if (SurfaceView.this.mRequestedWidth != -1 || SurfaceView.this.mRequestedHeight != -1) {
                    SurfaceView surfaceView = SurfaceView.this;
                    SurfaceView.this.mRequestedHeight = -1;
                    surfaceView.mRequestedWidth = -1;
                    SurfaceView.this.requestLayout();
                }
            }

            public void setFormat(int format) {
                if (format == -1) {
                    format = 4;
                }
                SurfaceView.this.mRequestedFormat = format;
                if (SurfaceView.this.mWindow != null) {
                    SurfaceView.this.updateWindow(SurfaceView.DEBUG, SurfaceView.DEBUG);
                }
            }

            @Deprecated
            public void setType(int type) {
            }

            public void setKeepScreenOn(boolean screenOn) {
                int i = SurfaceView.KEEP_SCREEN_ON_MSG;
                Message msg = SurfaceView.this.mHandler.obtainMessage(SurfaceView.KEEP_SCREEN_ON_MSG);
                if (!screenOn) {
                    i = 0;
                }
                msg.arg1 = i;
                SurfaceView.this.mHandler.sendMessage(msg);
            }

            public Canvas lockCanvas() {
                return internalLockCanvas(null);
            }

            public Canvas lockCanvas(Rect inOutDirty) {
                return internalLockCanvas(inOutDirty);
            }

            private final Canvas internalLockCanvas(Rect dirty) {
                SurfaceView.this.mSurfaceLock.lock();
                Canvas c = null;
                if (!(SurfaceView.this.mDrawingStopped || SurfaceView.this.mWindow == null)) {
                    try {
                        c = SurfaceView.this.mSurface.lockCanvas(dirty);
                    } catch (Exception e) {
                        Log.e(LOG_TAG, "Exception locking surface", e);
                    }
                }
                if (c != null) {
                    SurfaceView.this.mLastLockTime = SystemClock.uptimeMillis();
                    return c;
                }
                long now = SystemClock.uptimeMillis();
                long nextTime = SurfaceView.this.mLastLockTime + 100;
                if (nextTime > now) {
                    try {
                        Thread.sleep(nextTime - now);
                    } catch (InterruptedException e2) {
                    }
                    now = SystemClock.uptimeMillis();
                }
                SurfaceView.this.mLastLockTime = now;
                SurfaceView.this.mSurfaceLock.unlock();
                return null;
            }

            public void unlockCanvasAndPost(Canvas canvas) {
                SurfaceView.this.mSurface.unlockCanvasAndPost(canvas);
                SurfaceView.this.mSurfaceLock.unlock();
                if (HwFrameworkFactory.getHwNsdImpl().isSupportAps() && HwFrameworkFactory.getHwNsdImpl().isAPSReady()) {
                    HwFrameworkFactory.getHwNsdImpl().powerCtroll();
                }
            }

            public Surface getSurface() {
                return SurfaceView.this.mSurface;
            }

            public Rect getSurfaceFrame() {
                return SurfaceView.this.mSurfaceFrame;
            }
        };
        init();
    }

    private void init() {
        setWillNotDraw(true);
    }

    public SurfaceHolder getHolder() {
        return this.mSurfaceHolder;
    }

    protected void onAttachedToWindow() {
        boolean z = DEBUG;
        super.onAttachedToWindow();
        this.mParent.requestTransparentRegion(this);
        this.mSession = getWindowSession();
        this.mLayout.token = getWindowToken();
        this.mLayout.setTitle("SurfaceView - " + getViewRootImpl().getTitle());
        if (getVisibility() == 0) {
            z = true;
        }
        this.mViewVisibility = z;
        if (!this.mGlobalListenersAdded) {
            ViewTreeObserver observer = getViewTreeObserver();
            observer.addOnScrollChangedListener(this.mScrollChangedListener);
            observer.addOnPreDrawListener(this.mDrawListener);
            this.mGlobalListenersAdded = true;
        }
    }

    protected void onWindowVisibilityChanged(int visibility) {
        boolean z;
        super.onWindowVisibilityChanged(visibility);
        if (visibility == 0) {
            z = true;
        } else {
            z = DEBUG;
        }
        this.mWindowVisibility = z;
        if (this.mWindowVisibility) {
            z = this.mViewVisibility;
        } else {
            z = DEBUG;
        }
        this.mRequestedVisible = z;
        updateWindow(DEBUG, DEBUG);
    }

    public void setVisibility(int visibility) {
        boolean z;
        super.setVisibility(visibility);
        if (visibility == 0) {
            z = true;
        } else {
            z = DEBUG;
        }
        this.mViewVisibility = z;
        boolean z2 = this.mWindowVisibility ? this.mViewVisibility : DEBUG;
        if (z2 != this.mRequestedVisible) {
            requestLayout();
        }
        this.mRequestedVisible = z2;
        updateWindow(DEBUG, DEBUG);
    }

    protected void onDetachedFromWindow() {
        if (this.mGlobalListenersAdded) {
            ViewTreeObserver observer = getViewTreeObserver();
            observer.removeOnScrollChangedListener(this.mScrollChangedListener);
            observer.removeOnPreDrawListener(this.mDrawListener);
            this.mGlobalListenersAdded = DEBUG;
        }
        this.mRequestedVisible = DEBUG;
        updateWindow(DEBUG, DEBUG);
        this.mHaveFrame = DEBUG;
        if (this.mWindow != null) {
            try {
                this.mSession.remove(this.mWindow);
            } catch (RemoteException e) {
            }
            this.mWindow = null;
        }
        this.mSession = null;
        this.mLayout.token = null;
        super.onDetachedFromWindow();
    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width;
        int height;
        if (this.mRequestedWidth >= 0) {
            width = View.resolveSizeAndState(this.mRequestedWidth, widthMeasureSpec, 0);
        } else {
            width = View.getDefaultSize(0, widthMeasureSpec);
        }
        if (this.mRequestedHeight >= 0) {
            height = View.resolveSizeAndState(this.mRequestedHeight, heightMeasureSpec, 0);
        } else {
            height = View.getDefaultSize(0, heightMeasureSpec);
        }
        setMeasuredDimension(width, height);
    }

    protected boolean setFrame(int left, int top, int right, int bottom) {
        boolean result = super.setFrame(left, top, right, bottom);
        updateWindow(DEBUG, DEBUG);
        return result;
    }

    public boolean gatherTransparentRegion(Region region) {
        if (this.mWindowType == RILConstants.RIL_UNSOL_RESPONSE_RADIO_STATE_CHANGED) {
            return super.gatherTransparentRegion(region);
        }
        boolean opaque = true;
        if ((this.mPrivateFlags & LogPower.START_CHG_ROTATION) == 0) {
            opaque = super.gatherTransparentRegion(region);
        } else if (region != null) {
            int w = getWidth();
            int h = getHeight();
            if (w > 0 && h > 0) {
                getLocationInWindow(this.mLocation);
                int l = this.mLocation[0];
                int t = this.mLocation[KEEP_SCREEN_ON_MSG];
                region.op(l, t, l + w, t + h, Op.UNION);
            }
        }
        if (PixelFormat.formatHasAlpha(this.mRequestedFormat)) {
            opaque = DEBUG;
        }
        return opaque;
    }

    public void draw(Canvas canvas) {
        if (this.mWindowType != RILConstants.RIL_UNSOL_RESPONSE_RADIO_STATE_CHANGED && (this.mPrivateFlags & LogPower.START_CHG_ROTATION) == 0) {
            canvas.drawColor(0, Mode.CLEAR);
        }
        super.draw(canvas);
    }

    protected void dispatchDraw(Canvas canvas) {
        if (this.mWindowType != RILConstants.RIL_UNSOL_RESPONSE_RADIO_STATE_CHANGED && (this.mPrivateFlags & LogPower.START_CHG_ROTATION) == LogPower.START_CHG_ROTATION) {
            canvas.drawColor(0, Mode.CLEAR);
        }
        super.dispatchDraw(canvas);
    }

    public void setZOrderMediaOverlay(boolean isMediaOverlay) {
        int i;
        if (isMediaOverlay) {
            i = RILConstants.RIL_UNSOL_RESPONSE_NEW_SMS_STATUS_REPORT;
        } else {
            i = RILConstants.RIL_UNSOL_RESPONSE_CALL_STATE_CHANGED;
        }
        this.mWindowType = i;
    }

    public void setZOrderOnTop(boolean onTop) {
        if (onTop) {
            this.mWindowType = RILConstants.RIL_UNSOL_RESPONSE_RADIO_STATE_CHANGED;
            LayoutParams layoutParams = this.mLayout;
            layoutParams.flags |= Protocol.BASE_WIFI;
            return;
        }
        this.mWindowType = RILConstants.RIL_UNSOL_RESPONSE_CALL_STATE_CHANGED;
        layoutParams = this.mLayout;
        layoutParams.flags &= -131073;
    }

    public void setSecure(boolean isSecure) {
        if (isSecure) {
            LayoutParams layoutParams = this.mLayout;
            layoutParams.flags |= AccessibilityNodeInfo.ACTION_SCROLL_BACKWARD;
            return;
        }
        layoutParams = this.mLayout;
        layoutParams.flags &= -8193;
    }

    public void setWindowType(int type) {
        this.mWindowType = type;
    }

    protected void updateWindow(boolean force, boolean redrawNeeded) {
        if (this.mHaveFrame) {
            ViewRootImpl viewRoot = getViewRootImpl();
            if (viewRoot != null) {
                this.mTranslator = viewRoot.mTranslator;
                this.mRogTranslater = viewRoot.mRogTranslater;
            }
            if (this.mTranslator != null) {
                this.mSurface.setCompatibilityTranslator(this.mTranslator);
            }
            int myWidth = this.mRequestedWidth;
            if (myWidth <= 0) {
                myWidth = getWidth();
            }
            int myHeight = this.mRequestedHeight;
            if (myHeight <= 0) {
                myHeight = getHeight();
            }
            boolean creating = this.mWindow == null ? true : DEBUG;
            boolean formatChanged = this.mFormat != this.mRequestedFormat ? true : DEBUG;
            boolean sizeChanged = (this.mWindowSpaceWidth == myWidth && this.mWindowSpaceHeight == myHeight) ? DEBUG : true;
            boolean visibleChanged = this.mVisible != this.mRequestedVisible ? true : DEBUG;
            boolean layoutSizeChanged = getWidth() == this.mLayout.width ? getHeight() != this.mLayout.height ? true : DEBUG : true;
            int length;
            if (force || creating || formatChanged || sizeChanged || visibleChanged || this.mUpdateWindowNeeded || this.mReportDrawNeeded || redrawNeeded) {
                getLocationInWindow(this.mLocation);
                try {
                    Display display;
                    int reportDrawNeeded;
                    int relayoutResult;
                    int surfaceWidth;
                    int surfaceHeight;
                    boolean realSizeChanged;
                    Callback[] callbackArr;
                    boolean surfaceChanged;
                    int i;
                    Callback c;
                    boolean visible = this.mRequestedVisible;
                    this.mVisible = visible;
                    this.mWindowSpaceLeft = this.mLocation[0];
                    this.mWindowSpaceTop = this.mLocation[KEEP_SCREEN_ON_MSG];
                    this.mWindowSpaceWidth = myWidth;
                    this.mWindowSpaceHeight = myHeight;
                    this.mFormat = this.mRequestedFormat;
                    this.mLayout.x = this.mWindowSpaceLeft;
                    this.mLayout.y = this.mWindowSpaceTop;
                    this.mLayout.width = getWidth();
                    this.mLayout.height = getHeight();
                    if (this.mRogTranslater != null) {
                        this.mRogTranslater.translateWindowLayout(this.mLayout);
                    } else if (this.mTranslator != null) {
                        this.mTranslator.translateLayoutParamsInAppWindowToScreen(this.mLayout);
                    }
                    this.mLayout.format = this.mRequestedFormat;
                    LayoutParams layoutParams = this.mLayout;
                    layoutParams.flags |= 16920;
                    if (!(creating || force)) {
                        if (!(this.mUpdateWindowNeeded || sizeChanged)) {
                            layoutParams = this.mLayout;
                            layoutParams.privateFlags |= AccessibilityNodeInfo.ACTION_SCROLL_BACKWARD;
                            if (!getContext().getResources().getCompatibilityInfo().supportsScreen()) {
                                layoutParams = this.mLayout;
                                layoutParams.privateFlags |= LogPower.START_CHG_ROTATION;
                            }
                            if (this.mRogTranslater == null) {
                                layoutParams = this.mLayout;
                                layoutParams.privateFlags |= AccessibilityNodeInfo.ACTION_DISMISS;
                            } else {
                                layoutParams = this.mLayout;
                                layoutParams.privateFlags &= -1048577;
                            }
                            layoutParams = this.mLayout;
                            layoutParams.privateFlags |= 65600;
                            if (this.mWindow == null) {
                                display = getDisplay();
                                this.mWindow = new MyWindow(this);
                                this.mLayout.type = this.mWindowType;
                                this.mLayout.gravity = 8388659;
                                this.mSession.addToDisplayWithoutInputChannel(this.mWindow, this.mWindow.mSeq, this.mLayout, this.mVisible ? 0 : 8, display.getDisplayId(), this.mContentInsets, this.mStableInsets);
                            }
                            this.mSurfaceLock.lock();
                            this.mUpdateWindowNeeded = DEBUG;
                            reportDrawNeeded = this.mReportDrawNeeded;
                            this.mReportDrawNeeded = DEBUG;
                            this.mDrawingStopped = visible ? DEBUG : true;
                            relayoutResult = this.mSession.relayout(this.mWindow, this.mWindow.mSeq, this.mLayout, this.mWindowSpaceWidth, this.mWindowSpaceHeight, visible ? 0 : 8, GET_NEW_SURFACE_MSG, this.mWinFrame, this.mOverscanInsets, this.mContentInsets, this.mVisibleInsets, this.mStableInsets, this.mOutsets, this.mBackdropFrame, this.mConfiguration, this.mNewSurface);
                            if ((relayoutResult & GET_NEW_SURFACE_MSG) != 0) {
                                reportDrawNeeded = KEEP_SCREEN_ON_MSG;
                            }
                            this.mSurfaceFrame.left = 0;
                            this.mSurfaceFrame.top = 0;
                            if (this.mTranslator == null && this.mRogTranslater == null) {
                                float appInvertedScale = this.mTranslator.applicationInvertedScale;
                                this.mSurfaceFrame.right = (int) ((((float) this.mWinFrame.width()) * appInvertedScale) + 0.5f);
                                this.mSurfaceFrame.bottom = (int) ((((float) this.mWinFrame.height()) * appInvertedScale) + 0.5f);
                            } else {
                                this.mSurfaceFrame.right = this.mWinFrame.width();
                                this.mSurfaceFrame.bottom = this.mWinFrame.height();
                            }
                            surfaceWidth = this.mSurfaceFrame.right;
                            surfaceHeight = this.mSurfaceFrame.bottom;
                            realSizeChanged = this.mLastSurfaceWidth != surfaceWidth ? this.mLastSurfaceHeight == surfaceHeight ? true : DEBUG : true;
                            this.mLastSurfaceWidth = surfaceWidth;
                            this.mLastSurfaceHeight = surfaceHeight;
                            this.mSurfaceLock.unlock();
                            redrawNeeded |= creating | reportDrawNeeded;
                            callbackArr = null;
                            surfaceChanged = (relayoutResult & 4) == 0 ? true : DEBUG;
                            if (this.mSurfaceCreated && (surfaceChanged || (!visible && visibleChanged))) {
                                this.mSurfaceCreated = DEBUG;
                                if (this.mSurface.isValid()) {
                                    callbackArr = getSurfaceCallbacks();
                                    length = callbackArr.length;
                                    for (i = 0; i < length; i += KEEP_SCREEN_ON_MSG) {
                                        callbackArr[i].surfaceDestroyed(this.mSurfaceHolder);
                                    }
                                    LogPower.push(LogPower.SURFACEVIEW_DESTROYED);
                                }
                            }
                            this.mSurface.transferFrom(this.mNewSurface);
                            if (visible && this.mSurface.isValid()) {
                                if (!this.mSurfaceCreated && (surfaceChanged || visibleChanged)) {
                                    this.mSurfaceCreated = true;
                                    this.mIsCreating = true;
                                    if (callbackArr == null) {
                                        callbackArr = getSurfaceCallbacks();
                                    }
                                    length = callbackArr.length;
                                    for (i = 0; i < length; i += KEEP_SCREEN_ON_MSG) {
                                        callbackArr[i].surfaceCreated(this.mSurfaceHolder);
                                    }
                                    LogPower.push(LogPower.SURFACEVIEW_CREATED);
                                }
                                if (creating || formatChanged || sizeChanged || visibleChanged || realSizeChanged) {
                                    if (callbackArr == null) {
                                        callbackArr = getSurfaceCallbacks();
                                    }
                                    length = callbackArr.length;
                                    for (i = 0; i < length; i += KEEP_SCREEN_ON_MSG) {
                                        callbackArr[i].surfaceChanged(this.mSurfaceHolder, this.mFormat, myWidth, myHeight);
                                    }
                                }
                                if (redrawNeeded) {
                                    if (callbackArr == null) {
                                        callbackArr = getSurfaceCallbacks();
                                    }
                                    length = callbackArr.length;
                                    for (i = 0; i < length; i += KEEP_SCREEN_ON_MSG) {
                                        c = callbackArr[i];
                                        if (c instanceof Callback2) {
                                            ((Callback2) c).surfaceRedrawNeeded(this.mSurfaceHolder);
                                        }
                                    }
                                }
                            }
                            this.mIsCreating = DEBUG;
                            if (redrawNeeded) {
                                this.mSession.finishDrawing(this.mWindow);
                            }
                            this.mSession.performDeferredDestroy(this.mWindow);
                        }
                    }
                    layoutParams = this.mLayout;
                    layoutParams.privateFlags &= -8193;
                    if (getContext().getResources().getCompatibilityInfo().supportsScreen()) {
                        layoutParams = this.mLayout;
                        layoutParams.privateFlags |= LogPower.START_CHG_ROTATION;
                    }
                    if (this.mRogTranslater == null) {
                        layoutParams = this.mLayout;
                        layoutParams.privateFlags &= -1048577;
                    } else {
                        layoutParams = this.mLayout;
                        layoutParams.privateFlags |= AccessibilityNodeInfo.ACTION_DISMISS;
                    }
                    layoutParams = this.mLayout;
                    layoutParams.privateFlags |= 65600;
                    if (this.mWindow == null) {
                        display = getDisplay();
                        this.mWindow = new MyWindow(this);
                        this.mLayout.type = this.mWindowType;
                        this.mLayout.gravity = 8388659;
                        if (this.mVisible) {
                        }
                        this.mSession.addToDisplayWithoutInputChannel(this.mWindow, this.mWindow.mSeq, this.mLayout, this.mVisible ? 0 : 8, display.getDisplayId(), this.mContentInsets, this.mStableInsets);
                    }
                    this.mSurfaceLock.lock();
                    this.mUpdateWindowNeeded = DEBUG;
                    reportDrawNeeded = this.mReportDrawNeeded;
                    this.mReportDrawNeeded = DEBUG;
                    if (visible) {
                    }
                    this.mDrawingStopped = visible ? DEBUG : true;
                    if (visible) {
                    }
                    relayoutResult = this.mSession.relayout(this.mWindow, this.mWindow.mSeq, this.mLayout, this.mWindowSpaceWidth, this.mWindowSpaceHeight, visible ? 0 : 8, GET_NEW_SURFACE_MSG, this.mWinFrame, this.mOverscanInsets, this.mContentInsets, this.mVisibleInsets, this.mStableInsets, this.mOutsets, this.mBackdropFrame, this.mConfiguration, this.mNewSurface);
                    if ((relayoutResult & GET_NEW_SURFACE_MSG) != 0) {
                        reportDrawNeeded = KEEP_SCREEN_ON_MSG;
                    }
                    this.mSurfaceFrame.left = 0;
                    this.mSurfaceFrame.top = 0;
                    if (this.mTranslator == null) {
                    }
                    this.mSurfaceFrame.right = this.mWinFrame.width();
                    this.mSurfaceFrame.bottom = this.mWinFrame.height();
                    surfaceWidth = this.mSurfaceFrame.right;
                    surfaceHeight = this.mSurfaceFrame.bottom;
                    if (this.mLastSurfaceWidth != surfaceWidth) {
                    }
                    this.mLastSurfaceWidth = surfaceWidth;
                    this.mLastSurfaceHeight = surfaceHeight;
                    this.mSurfaceLock.unlock();
                    redrawNeeded |= creating | reportDrawNeeded;
                    callbackArr = null;
                    if ((relayoutResult & 4) == 0) {
                    }
                    this.mSurfaceCreated = DEBUG;
                    if (this.mSurface.isValid()) {
                        callbackArr = getSurfaceCallbacks();
                        length = callbackArr.length;
                        for (i = 0; i < length; i += KEEP_SCREEN_ON_MSG) {
                            callbackArr[i].surfaceDestroyed(this.mSurfaceHolder);
                        }
                        LogPower.push(LogPower.SURFACEVIEW_DESTROYED);
                    }
                    this.mSurface.transferFrom(this.mNewSurface);
                    this.mSurfaceCreated = true;
                    this.mIsCreating = true;
                    if (callbackArr == null) {
                        callbackArr = getSurfaceCallbacks();
                    }
                    length = callbackArr.length;
                    for (i = 0; i < length; i += KEEP_SCREEN_ON_MSG) {
                        callbackArr[i].surfaceCreated(this.mSurfaceHolder);
                    }
                    LogPower.push(LogPower.SURFACEVIEW_CREATED);
                    if (callbackArr == null) {
                        callbackArr = getSurfaceCallbacks();
                    }
                    length = callbackArr.length;
                    for (i = 0; i < length; i += KEEP_SCREEN_ON_MSG) {
                        callbackArr[i].surfaceChanged(this.mSurfaceHolder, this.mFormat, myWidth, myHeight);
                    }
                    if (redrawNeeded) {
                        if (callbackArr == null) {
                            callbackArr = getSurfaceCallbacks();
                        }
                        length = callbackArr.length;
                        for (i = 0; i < length; i += KEEP_SCREEN_ON_MSG) {
                            c = callbackArr[i];
                            if (c instanceof Callback2) {
                                ((Callback2) c).surfaceRedrawNeeded(this.mSurfaceHolder);
                            }
                        }
                    }
                    this.mIsCreating = DEBUG;
                    if (redrawNeeded) {
                        this.mSession.finishDrawing(this.mWindow);
                    }
                    this.mSession.performDeferredDestroy(this.mWindow);
                } catch (Throwable ex) {
                    Log.e(TAG, "Exception from relayout", ex);
                } catch (Throwable th) {
                    this.mSurfaceLock.unlock();
                }
            } else {
                getLocationInWindow(this.mLocation);
                boolean positionChanged = this.mWindowSpaceLeft == this.mLocation[0] ? this.mWindowSpaceTop != this.mLocation[KEEP_SCREEN_ON_MSG] ? true : DEBUG : true;
                if (positionChanged || layoutSizeChanged) {
                    this.mWindowSpaceLeft = this.mLocation[0];
                    this.mWindowSpaceTop = this.mLocation[KEEP_SCREEN_ON_MSG];
                    int[] iArr = this.mLocation;
                    length = getWidth();
                    this.mLayout.width = length;
                    iArr[0] = length;
                    iArr = this.mLocation;
                    length = getHeight();
                    this.mLayout.height = length;
                    iArr[KEEP_SCREEN_ON_MSG] = length;
                    transformFromViewToWindowSpace(this.mLocation);
                    this.mWinFrame.set(this.mWindowSpaceLeft, this.mWindowSpaceTop, this.mLocation[0], this.mLocation[KEEP_SCREEN_ON_MSG]);
                    if (this.mRogTranslater != null) {
                        this.mRogTranslater.translateRectInAppWindowToScreen(this.mWinFrame);
                    } else if (this.mTranslator != null) {
                        this.mTranslator.translateRectInAppWindowToScreen(this.mWinFrame);
                    }
                    doSurfaceChange(layoutSizeChanged, myWidth, myHeight);
                    if (!(isHardwareAccelerated() && this.mRtHandlingPositionUpdates)) {
                        try {
                            this.mSession.repositionChild(this.mWindow, this.mWinFrame.left, this.mWinFrame.top, this.mWinFrame.right, this.mWinFrame.bottom, -1, this.mWinFrame);
                        } catch (Throwable ex2) {
                            Log.e(TAG, "Exception from relayout", ex2);
                        }
                    }
                }
            }
        }
    }

    private void doSurfaceChange(boolean layoutSizeChanged, int width, int height) {
        if (TARGET_GAME_SET.indexOf(this.mContext.getPackageName()) >= 0 && layoutSizeChanged && this.mVisible && this.mSurface.isValid()) {
            Callback[] callbacks = getSurfaceCallbacks();
            int length = callbacks.length;
            for (int i = 0; i < length; i += KEEP_SCREEN_ON_MSG) {
                callbacks[i].surfaceChanged(this.mSurfaceHolder, this.mFormat, width, height);
            }
        }
    }

    public final void updateWindowPositionRT(long frameNumber, int left, int top, int right, int bottom) {
        IWindowSession session = this.mSession;
        MyWindow window = this.mWindow;
        if (session != null && window != null) {
            this.mRtHandlingPositionUpdates = true;
            if (this.mRTLastReportedPosition.left != left || this.mRTLastReportedPosition.top != top || this.mRTLastReportedPosition.right != right || this.mRTLastReportedPosition.bottom != bottom) {
                try {
                    session.repositionChild(window, left, top, right, bottom, frameNumber, this.mRTLastReportedPosition);
                    this.mRTLastReportedPosition.set(left, top, right, bottom);
                } catch (RemoteException ex) {
                    Log.e(TAG, "Exception from repositionChild", ex);
                }
            }
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public final void windowPositionLostRT(long frameNumber) {
        IWindowSession session = this.mSession;
        MyWindow window = this.mWindow;
        if (!(session == null || window == null || !this.mRtHandlingPositionUpdates)) {
            this.mRtHandlingPositionUpdates = DEBUG;
            if (!(this.mWinFrame.isEmpty() || this.mWinFrame.equals(this.mRTLastReportedPosition))) {
                try {
                    session.repositionChild(window, this.mWinFrame.left, this.mWinFrame.top, this.mWinFrame.right, this.mWinFrame.bottom, frameNumber, this.mWinFrame);
                } catch (RemoteException ex) {
                    Log.e(TAG, "Exception from relayout", ex);
                }
            }
            this.mRTLastReportedPosition.setEmpty();
        }
    }

    private Callback[] getSurfaceCallbacks() {
        Callback[] callbacks;
        synchronized (this.mCallbacks) {
            callbacks = new Callback[this.mCallbacks.size()];
            this.mCallbacks.toArray(callbacks);
        }
        return callbacks;
    }

    void handleGetNewSurface() {
        updateWindow(DEBUG, DEBUG);
    }

    public boolean isFixedSize() {
        return (this.mRequestedWidth == -1 && this.mRequestedHeight == -1) ? DEBUG : true;
    }
}
