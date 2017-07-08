package com.android.server.wm;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.os.Debug;
import android.os.RemoteException;
import android.os.Trace;
import android.util.ArrayMap;
import android.util.ArraySet;
import android.util.Slog;
import android.view.Display;
import android.view.DisplayInfo;
import android.view.Surface;
import android.view.SurfaceControl;
import android.view.WindowManager.LayoutParams;
import android.view.animation.Animation;
import com.android.server.HwServiceFactory;
import com.android.server.HwServiceFactory.IDisplayEffectMonitor;
import com.android.server.job.controllers.JobStatus;
import com.android.server.power.IHwShutdownThread;
import com.android.server.wm.WindowManagerService.H;
import com.hisi.perfhub.PerfHub;
import java.io.PrintWriter;
import java.util.ArrayList;

class WindowSurfacePlacer {
    private static final boolean IS_DEBUG_VERSION = false;
    private static final boolean IS_TABLET = false;
    static final int SET_FORCE_HIDING_CHANGED = 4;
    static final int SET_ORIENTATION_CHANGE_COMPLETE = 8;
    static final int SET_TURN_ON_SCREEN = 16;
    static final int SET_UPDATE_ROTATION = 1;
    static final int SET_WALLPAPER_ACTION_PENDING = 32;
    static final int SET_WALLPAPER_MAY_CHANGE = 2;
    private static final String TAG = null;
    private float mAppBrightnessLast;
    private String mAppBrightnessPackageName;
    private String mAppBrightnessPackageNameLast;
    private float mButtonBrightness;
    private int mDeferDepth;
    private IDisplayEffectMonitor mDisplayEffectMonitor;
    private boolean mDisplayHasContent;
    private Session mHoldScreen;
    WindowState mHoldScreenWindow;
    private boolean mInLayout;
    private boolean mLastIsTopIsFullscreen;
    private Object mLastWindowFreezeSource;
    private int mLayoutRepeatCount;
    private boolean mObscureApplicationContentOnSecondaryDisplays;
    private boolean mObscured;
    WindowState mObsuringWindow;
    boolean mOrientationChangeComplete;
    private final ArrayList<SurfaceControl> mPendingDestroyingSurfaces;
    private PerfHub mPerfHub;
    private int mPreferredModeId;
    private float mPreferredRefreshRate;
    private float mScreenBrightness;
    private final WindowManagerService mService;
    private boolean mSustainedPerformanceModeCurrent;
    private boolean mSustainedPerformanceModeEnabled;
    private boolean mSyswin;
    private final Rect mTmpContentRect;
    private final LayerAndToken mTmpLayerAndToken;
    private final Rect mTmpStartRect;
    private boolean mTraversalScheduled;
    private boolean mUpdateRotation;
    private long mUserActivityTimeout;
    boolean mWallpaperActionPending;
    private final WallpaperController mWallpaperControllerLocked;
    private boolean mWallpaperForceHidingChanged;
    boolean mWallpaperMayChange;

    private static final class LayerAndToken {
        public int layer;
        public AppWindowToken token;

        private LayerAndToken() {
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.wm.WindowSurfacePlacer.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.wm.WindowSurfacePlacer.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.wm.WindowSurfacePlacer.<clinit>():void");
    }

    protected void performSurfacePlacementInner(boolean r33) {
        /* JADX: method processing error */
/*
        Error: java.lang.OutOfMemoryError: Java heap space
	at java.util.Arrays.copyOf(Arrays.java:3181)
	at java.util.ArrayList.grow(ArrayList.java:261)
	at java.util.ArrayList.ensureExplicitCapacity(ArrayList.java:235)
	at java.util.ArrayList.ensureCapacityInternal(ArrayList.java:227)
	at java.util.ArrayList.add(ArrayList.java:458)
	at jadx.core.utils.BlockUtils.collectWhileDominates(BlockUtils.java:447)
	at jadx.core.utils.BlockUtils.collectWhileDominates(BlockUtils.java:448)
	at jadx.core.utils.BlockUtils.collectWhileDominates(BlockUtils.java:448)
	at jadx.core.utils.BlockUtils.collectWhileDominates(BlockUtils.java:448)
	at jadx.core.utils.BlockUtils.collectWhileDominates(BlockUtils.java:448)
	at jadx.core.utils.BlockUtils.collectWhileDominates(BlockUtils.java:448)
	at jadx.core.utils.BlockUtils.collectWhileDominates(BlockUtils.java:448)
	at jadx.core.utils.BlockUtils.collectWhileDominates(BlockUtils.java:448)
	at jadx.core.utils.BlockUtils.collectWhileDominates(BlockUtils.java:448)
	at jadx.core.utils.BlockUtils.collectWhileDominates(BlockUtils.java:448)
	at jadx.core.utils.BlockUtils.collectWhileDominates(BlockUtils.java:448)
	at jadx.core.utils.BlockUtils.collectWhileDominates(BlockUtils.java:448)
	at jadx.core.utils.BlockUtils.collectWhileDominates(BlockUtils.java:448)
	at jadx.core.utils.BlockUtils.collectWhileDominates(BlockUtils.java:448)
	at jadx.core.utils.BlockUtils.collectWhileDominates(BlockUtils.java:448)
	at jadx.core.utils.BlockUtils.collectWhileDominates(BlockUtils.java:448)
	at jadx.core.utils.BlockUtils.collectWhileDominates(BlockUtils.java:448)
	at jadx.core.utils.BlockUtils.collectWhileDominates(BlockUtils.java:448)
	at jadx.core.utils.BlockUtils.collectWhileDominates(BlockUtils.java:448)
	at jadx.core.utils.BlockUtils.collectWhileDominates(BlockUtils.java:448)
	at jadx.core.utils.BlockUtils.collectWhileDominates(BlockUtils.java:448)
	at jadx.core.utils.BlockUtils.collectWhileDominates(BlockUtils.java:448)
	at jadx.core.utils.BlockUtils.collectWhileDominates(BlockUtils.java:448)
	at jadx.core.utils.BlockUtils.collectWhileDominates(BlockUtils.java:448)
	at jadx.core.utils.BlockUtils.collectWhileDominates(BlockUtils.java:448)
	at jadx.core.utils.BlockUtils.collectWhileDominates(BlockUtils.java:448)
	at jadx.core.utils.BlockUtils.collectWhileDominates(BlockUtils.java:448)
*/
        /*
        r32 = this;
        r23 = 0;
        r0 = r32;
        r0 = r0.mService;
        r27 = r0;
        r0 = r27;
        r0 = r0.mFocusMayChange;
        r27 = r0;
        if (r27 == 0) goto L_0x002c;
    L_0x0010:
        r0 = r32;
        r0 = r0.mService;
        r27 = r0;
        r28 = 0;
        r0 = r28;
        r1 = r27;
        r1.mFocusMayChange = r0;
        r0 = r32;
        r0 = r0.mService;
        r27 = r0;
        r28 = 3;
        r29 = 0;
        r23 = r27.updateFocusedWindowLocked(r28, r29);
    L_0x002c:
        r0 = r32;
        r0 = r0.mService;
        r27 = r0;
        r0 = r27;
        r0 = r0.mDisplayContents;
        r27 = r0;
        r18 = r27.size();
        r13 = 0;
    L_0x003d:
        r0 = r18;
        if (r13 >= r0) goto L_0x007d;
    L_0x0041:
        r0 = r32;
        r0 = r0.mService;
        r27 = r0;
        r0 = r27;
        r0 = r0.mDisplayContents;
        r27 = r0;
        r0 = r27;
        r10 = r0.valueAt(r13);
        r10 = (com.android.server.wm.DisplayContent) r10;
        r0 = r10.mExitingTokens;
        r27 = r0;
        r27 = r27.size();
        r17 = r27 + -1;
    L_0x005f:
        if (r17 < 0) goto L_0x007a;
    L_0x0061:
        r0 = r10.mExitingTokens;
        r27 = r0;
        r0 = r27;
        r1 = r17;
        r27 = r0.get(r1);
        r27 = (com.android.server.wm.WindowToken) r27;
        r28 = 0;
        r0 = r28;
        r1 = r27;
        r1.hasVisible = r0;
        r17 = r17 + -1;
        goto L_0x005f;
    L_0x007a:
        r13 = r13 + 1;
        goto L_0x003d;
    L_0x007d:
        r0 = r32;
        r0 = r0.mService;
        r27 = r0;
        r0 = r27;
        r0 = r0.mStackIdToStack;
        r27 = r0;
        r27 = r27.size();
        r19 = r27 + -1;
    L_0x008f:
        if (r19 < 0) goto L_0x00c9;
    L_0x0091:
        r0 = r32;
        r0 = r0.mService;
        r27 = r0;
        r0 = r27;
        r0 = r0.mStackIdToStack;
        r27 = r0;
        r0 = r27;
        r1 = r19;
        r27 = r0.valueAt(r1);
        r27 = (com.android.server.wm.TaskStack) r27;
        r0 = r27;
        r15 = r0.mExitingAppTokens;
        r27 = r15.size();
        r22 = r27 + -1;
    L_0x00b1:
        if (r22 < 0) goto L_0x00c6;
    L_0x00b3:
        r0 = r22;
        r27 = r15.get(r0);
        r27 = (com.android.server.wm.AppWindowToken) r27;
        r28 = 0;
        r0 = r28;
        r1 = r27;
        r1.hasVisible = r0;
        r22 = r22 + -1;
        goto L_0x00b1;
    L_0x00c6:
        r19 = r19 + -1;
        goto L_0x008f;
    L_0x00c9:
        r27 = 0;
        r0 = r27;
        r1 = r32;
        r1.mHoldScreen = r0;
        r27 = 0;
        r0 = r27;
        r1 = r32;
        r1.mHoldScreenWindow = r0;
        r27 = 0;
        r0 = r27;
        r1 = r32;
        r1.mObsuringWindow = r0;
        r27 = -1082130432; // 0xffffffffbf800000 float:-1.0 double:NaN;
        r0 = r27;
        r1 = r32;
        r1.mScreenBrightness = r0;
        r27 = -1082130432; // 0xffffffffbf800000 float:-1.0 double:NaN;
        r0 = r27;
        r1 = r32;
        r1.mButtonBrightness = r0;
        r28 = -1;
        r0 = r28;
        r2 = r32;
        r2.mUserActivityTimeout = r0;
        r27 = 0;
        r0 = r27;
        r1 = r32;
        r1.mObscureApplicationContentOnSecondaryDisplays = r0;
        r27 = 0;
        r0 = r27;
        r1 = r32;
        r1.mSustainedPerformanceModeCurrent = r0;
        r0 = r32;
        r0 = r0.mService;
        r27 = r0;
        r0 = r27;
        r0 = r0.mTransactionSequence;
        r28 = r0;
        r28 = r28 + 1;
        r0 = r28;
        r1 = r27;
        r1.mTransactionSequence = r0;
        r0 = r32;
        r0 = r0.mService;
        r27 = r0;
        r6 = r27.getDefaultDisplayContentLocked();
        r8 = r6.getDisplayInfo();
        r7 = r8.logicalWidth;
        r5 = r8.logicalHeight;
        android.view.SurfaceControl.openTransaction();
        r0 = r32;	 Catch:{ RuntimeException -> 0x025b, all -> 0x026d }
        r1 = r33;	 Catch:{ RuntimeException -> 0x025b, all -> 0x026d }
        r2 = r18;	 Catch:{ RuntimeException -> 0x025b, all -> 0x026d }
        r0.applySurfaceChangesTransaction(r1, r2, r7, r5);	 Catch:{ RuntimeException -> 0x025b, all -> 0x026d }
        android.view.SurfaceControl.closeTransaction();
    L_0x013e:
        r9 = r6.getWindowList();
        r0 = r32;
        r0 = r0.mService;
        r27 = r0;
        r0 = r27;
        r0 = r0.mAppTransition;
        r27 = r0;
        r27 = r27.isReady();
        if (r27 == 0) goto L_0x0164;
    L_0x0154:
        r0 = r6.pendingLayoutChanges;
        r27 = r0;
        r0 = r32;
        r28 = r0.handleAppTransitionReadyLocked(r9);
        r27 = r27 | r28;
        r0 = r27;
        r6.pendingLayoutChanges = r0;
    L_0x0164:
        r0 = r32;
        r0 = r0.mService;
        r27 = r0;
        r0 = r27;
        r0 = r0.mAnimator;
        r27 = r0;
        r0 = r27;
        r0 = r0.mAppWindowAnimating;
        r27 = r0;
        if (r27 != 0) goto L_0x019e;
    L_0x0178:
        r0 = r32;
        r0 = r0.mService;
        r27 = r0;
        r0 = r27;
        r0 = r0.mAppTransition;
        r27 = r0;
        r27 = r27.isRunning();
        if (r27 == 0) goto L_0x019e;
    L_0x018a:
        r0 = r6.pendingLayoutChanges;
        r27 = r0;
        r0 = r32;
        r0 = r0.mService;
        r28 = r0;
        r28 = r28.handleAnimatingStoppedAndTransitionLocked();
        r27 = r27 | r28;
        r0 = r27;
        r6.pendingLayoutChanges = r0;
    L_0x019e:
        r0 = r32;
        r0 = r0.mWallpaperForceHidingChanged;
        r27 = r0;
        if (r27 == 0) goto L_0x01be;
    L_0x01a6:
        r0 = r6.pendingLayoutChanges;
        r27 = r0;
        if (r27 != 0) goto L_0x01be;
    L_0x01ac:
        r0 = r32;
        r0 = r0.mService;
        r27 = r0;
        r0 = r27;
        r0 = r0.mAppTransition;
        r27 = r0;
        r27 = r27.isReady();
        if (r27 == 0) goto L_0x0272;
    L_0x01be:
        r27 = 0;
        r0 = r27;
        r1 = r32;
        r1.mWallpaperForceHidingChanged = r0;
        r0 = r32;
        r0 = r0.mWallpaperMayChange;
        r27 = r0;
        if (r27 == 0) goto L_0x01d8;
    L_0x01ce:
        r0 = r6.pendingLayoutChanges;
        r27 = r0;
        r27 = r27 | 4;
        r0 = r27;
        r6.pendingLayoutChanges = r0;
    L_0x01d8:
        r0 = r32;
        r0 = r0.mService;
        r27 = r0;
        r0 = r27;
        r0 = r0.mFocusMayChange;
        r27 = r0;
        if (r27 == 0) goto L_0x0210;
    L_0x01e6:
        r0 = r32;
        r0 = r0.mService;
        r27 = r0;
        r28 = 0;
        r0 = r28;
        r1 = r27;
        r1.mFocusMayChange = r0;
        r0 = r32;
        r0 = r0.mService;
        r27 = r0;
        r28 = 2;
        r29 = 0;
        r27 = r27.updateFocusedWindowLocked(r28, r29);
        if (r27 == 0) goto L_0x0210;
    L_0x0204:
        r23 = 1;
        r0 = r6.pendingLayoutChanges;
        r27 = r0;
        r27 = r27 | 8;
        r0 = r27;
        r6.pendingLayoutChanges = r0;
    L_0x0210:
        r0 = r32;
        r0 = r0.mService;
        r27 = r0;
        r27 = r27.needsLayout();
        if (r27 == 0) goto L_0x0226;
    L_0x021c:
        r0 = r6.pendingLayoutChanges;
        r27 = r0;
        r27 = r27 | 1;
        r0 = r27;
        r6.pendingLayoutChanges = r0;
    L_0x0226:
        r0 = r32;
        r0 = r0.mService;
        r27 = r0;
        r0 = r27;
        r0 = r0.mResizingWindows;
        r27 = r0;
        r27 = r27.size();
        r17 = r27 + -1;
    L_0x0238:
        if (r17 < 0) goto L_0x02a6;
    L_0x023a:
        r0 = r32;
        r0 = r0.mService;
        r27 = r0;
        r0 = r27;
        r0 = r0.mResizingWindows;
        r27 = r0;
        r0 = r27;
        r1 = r17;
        r26 = r0.get(r1);
        r26 = (com.android.server.wm.WindowState) r26;
        r0 = r26;
        r0 = r0.mAppFreezing;
        r27 = r0;
        if (r27 == 0) goto L_0x027e;
    L_0x0258:
        r17 = r17 + -1;
        goto L_0x0238;
    L_0x025b:
        r14 = move-exception;
        r27 = TAG;	 Catch:{ RuntimeException -> 0x025b, all -> 0x026d }
        r28 = "Unhandled exception in Window Manager";	 Catch:{ RuntimeException -> 0x025b, all -> 0x026d }
        r0 = r27;	 Catch:{ RuntimeException -> 0x025b, all -> 0x026d }
        r1 = r28;	 Catch:{ RuntimeException -> 0x025b, all -> 0x026d }
        android.util.Slog.wtf(r0, r1, r14);	 Catch:{ RuntimeException -> 0x025b, all -> 0x026d }
        android.view.SurfaceControl.closeTransaction();
        goto L_0x013e;
    L_0x026d:
        r27 = move-exception;
        android.view.SurfaceControl.closeTransaction();
        throw r27;
    L_0x0272:
        r0 = r6.pendingLayoutChanges;
        r27 = r0;
        r27 = r27 | 1;
        r0 = r27;
        r6.pendingLayoutChanges = r0;
        goto L_0x01be;
    L_0x027e:
        r0 = r26;
        r0 = r0.mAppToken;
        r27 = r0;
        if (r27 == 0) goto L_0x028f;
    L_0x0286:
        r0 = r26;
        r0 = r0.mAppToken;
        r27 = r0;
        r27.destroySavedSurfaces();
    L_0x028f:
        r26.reportResized();
        r0 = r32;
        r0 = r0.mService;
        r27 = r0;
        r0 = r27;
        r0 = r0.mResizingWindows;
        r27 = r0;
        r0 = r27;
        r1 = r17;
        r0.remove(r1);
        goto L_0x0258;
    L_0x02a6:
        r0 = r32;
        r0 = r0.mOrientationChangeComplete;
        r27 = r0;
        if (r27 == 0) goto L_0x03b2;
    L_0x02ae:
        r0 = r32;
        r0 = r0.mService;
        r27 = r0;
        r0 = r27;
        r0 = r0.mDisplayFrozen;
        r27 = r0;
        if (r27 == 0) goto L_0x0354;
    L_0x02bc:
        r0 = r32;
        r0 = r0.mLastWindowFreezeSource;
        r27 = r0;
        if (r27 == 0) goto L_0x0480;
    L_0x02c4:
        r0 = r32;
        r0 = r0.mLastWindowFreezeSource;
        r27 = r0;
        r27 = r27.toString();
        r27 = android.util.Jlog.extractAppName(r27);
        r28 = "";
        r29 = 59;
        r0 = r29;
        r1 = r27;
        r2 = r28;
        android.util.Jlog.d(r0, r1, r2);
    L_0x02e0:
        r0 = r32;
        r0 = r0.mPerfHub;
        r27 = r0;
        if (r27 != 0) goto L_0x02f3;
    L_0x02e8:
        r27 = new com.hisi.perfhub.PerfHub;
        r27.<init>();
        r0 = r27;
        r1 = r32;
        r1.mPerfHub = r0;
    L_0x02f3:
        r0 = r32;
        r0 = r0.mService;
        r27 = r0;
        r0 = r27;
        r0 = r0.mIsPerfBoost;
        r27 = r0;
        if (r27 == 0) goto L_0x033b;
    L_0x0301:
        r0 = r32;
        r0 = r0.mPerfHub;
        r27 = r0;
        if (r27 == 0) goto L_0x033b;
    L_0x0309:
        r0 = r32;
        r0 = r0.mService;
        r27 = r0;
        r28 = 0;
        r0 = r28;
        r1 = r27;
        r1.mIsPerfBoost = r0;
        r0 = r32;
        r0 = r0.mPerfHub;
        r27 = r0;
        r28 = "";
        r29 = 1;
        r0 = r29;
        r0 = new int[r0];
        r29 = r0;
        r30 = 0;
        r31 = 0;
        r29[r31] = r30;
        r30 = 6;
        r0 = r27;
        r1 = r30;
        r2 = r28;
        r3 = r29;
        r0.perfEvent(r1, r2, r3);
    L_0x033b:
        r0 = r32;
        r0 = r0.mService;
        r27 = r0;
        r0 = r27;
        r0 = r0.mRotation;
        r27 = r0;
        r27 = java.lang.Integer.toString(r27);
        r28 = 130; // 0x82 float:1.82E-43 double:6.4E-322;
        r0 = r28;
        r1 = r27;
        com.huawei.pgmng.log.LogPower.push(r0, r1);
    L_0x0354:
        r0 = r32;
        r0 = r0.mService;
        r27 = r0;
        r0 = r27;
        r0 = r0.mWindowsFreezingScreen;
        r27 = r0;
        if (r27 == 0) goto L_0x0393;
    L_0x0362:
        r0 = r32;
        r0 = r0.mService;
        r27 = r0;
        r28 = 0;
        r0 = r28;
        r1 = r27;
        r1.mWindowsFreezingScreen = r0;
        r0 = r32;
        r0 = r0.mService;
        r27 = r0;
        r0 = r32;
        r0 = r0.mLastWindowFreezeSource;
        r28 = r0;
        r0 = r28;
        r1 = r27;
        r1.mLastFinishedFreezeSource = r0;
        r0 = r32;
        r0 = r0.mService;
        r27 = r0;
        r0 = r27;
        r0 = r0.mH;
        r27 = r0;
        r28 = 11;
        r27.removeMessages(r28);
    L_0x0393:
        r0 = r32;
        r0 = r0.mService;
        r27 = r0;
        r0 = r27;
        r0 = r0.mDisplayFrozen;
        r27 = r0;
        if (r27 == 0) goto L_0x03a9;
    L_0x03a1:
        r27 = TAG;
        r28 = "orientation change is complete, call stopFreezingDisplayLocked";
        android.util.Slog.i(r27, r28);
    L_0x03a9:
        r0 = r32;
        r0 = r0.mService;
        r27 = r0;
        r27.stopFreezingDisplayLocked();
    L_0x03b2:
        r25 = 0;
        r0 = r32;
        r0 = r0.mService;
        r27 = r0;
        r0 = r27;
        r0 = r0.mDestroySurface;
        r27 = r0;
        r17 = r27.size();
        if (r17 <= 0) goto L_0x042c;
    L_0x03c6:
        r17 = r17 + -1;
        r0 = r32;
        r0 = r0.mService;
        r27 = r0;
        r0 = r27;
        r0 = r0.mDestroySurface;
        r27 = r0;
        r0 = r27;
        r1 = r17;
        r26 = r0.get(r1);
        r26 = (com.android.server.wm.WindowState) r26;
        r27 = 0;
        r0 = r27;
        r1 = r26;
        r1.mDestroying = r0;
        r0 = r32;
        r0 = r0.mService;
        r27 = r0;
        r0 = r27;
        r0 = r0.mInputMethodWindow;
        r27 = r0;
        r0 = r27;
        r1 = r26;
        if (r0 != r1) goto L_0x0406;
    L_0x03f8:
        r0 = r32;
        r0 = r0.mService;
        r27 = r0;
        r28 = 0;
        r0 = r28;
        r1 = r27;
        r1.mInputMethodWindow = r0;
    L_0x0406:
        r0 = r32;
        r0 = r0.mWallpaperControllerLocked;
        r27 = r0;
        r0 = r27;
        r1 = r26;
        r27 = r0.isWallpaperTarget(r1);
        if (r27 == 0) goto L_0x0418;
    L_0x0416:
        r25 = 1;
    L_0x0418:
        r26.destroyOrSaveSurface();
        if (r17 > 0) goto L_0x03c6;
    L_0x041d:
        r0 = r32;
        r0 = r0.mService;
        r27 = r0;
        r0 = r27;
        r0 = r0.mDestroySurface;
        r27 = r0;
        r27.clear();
    L_0x042c:
        r13 = 0;
    L_0x042d:
        r0 = r18;
        if (r13 >= r0) goto L_0x0491;
    L_0x0431:
        r0 = r32;
        r0 = r0.mService;
        r27 = r0;
        r0 = r27;
        r0 = r0.mDisplayContents;
        r27 = r0;
        r0 = r27;
        r10 = r0.valueAt(r13);
        r10 = (com.android.server.wm.DisplayContent) r10;
        r0 = r10.mExitingTokens;
        r16 = r0;
        r27 = r16.size();
        r17 = r27 + -1;
    L_0x044f:
        if (r17 < 0) goto L_0x048e;
    L_0x0451:
        r21 = r16.get(r17);
        r21 = (com.android.server.wm.WindowToken) r21;
        r0 = r21;
        r0 = r0.hasVisible;
        r27 = r0;
        if (r27 != 0) goto L_0x047d;
    L_0x045f:
        r16.remove(r17);
        r0 = r21;
        r0 = r0.windowType;
        r27 = r0;
        r28 = 2013; // 0x7dd float:2.821E-42 double:9.946E-321;
        r0 = r27;
        r1 = r28;
        if (r0 != r1) goto L_0x047d;
    L_0x0470:
        r0 = r32;
        r0 = r0.mWallpaperControllerLocked;
        r27 = r0;
        r0 = r27;
        r1 = r21;
        r0.removeWallpaperToken(r1);
    L_0x047d:
        r17 = r17 + -1;
        goto L_0x044f;
    L_0x0480:
        r27 = "";
        r28 = 59;
        r0 = r28;
        r1 = r27;
        android.util.Jlog.d(r0, r1);
        goto L_0x02e0;
    L_0x048e:
        r13 = r13 + 1;
        goto L_0x042d;
    L_0x0491:
        r0 = r32;
        r0 = r0.mService;
        r27 = r0;
        r0 = r27;
        r0 = r0.mStackIdToStack;
        r27 = r0;
        r27 = r27.size();
        r19 = r27 + -1;
    L_0x04a3:
        if (r19 < 0) goto L_0x0522;
    L_0x04a5:
        r0 = r32;
        r0 = r0.mService;
        r27 = r0;
        r0 = r27;
        r0 = r0.mStackIdToStack;
        r27 = r0;
        r0 = r27;
        r1 = r19;
        r27 = r0.valueAt(r1);
        r27 = (com.android.server.wm.TaskStack) r27;
        r0 = r27;
        r15 = r0.mExitingAppTokens;
        r27 = r15.size();
        r17 = r27 + -1;
    L_0x04c5:
        if (r17 < 0) goto L_0x051f;
    L_0x04c7:
        r0 = r17;
        r20 = r15.get(r0);
        r20 = (com.android.server.wm.AppWindowToken) r20;
        r0 = r20;
        r0 = r0.hasVisible;
        r27 = r0;
        if (r27 != 0) goto L_0x04ed;
    L_0x04d7:
        r0 = r32;
        r0 = r0.mService;
        r27 = r0;
        r0 = r27;
        r0 = r0.mClosingApps;
        r27 = r0;
        r0 = r27;
        r1 = r20;
        r27 = r0.contains(r1);
        if (r27 == 0) goto L_0x04f0;
    L_0x04ed:
        r17 = r17 + -1;
        goto L_0x04c5;
    L_0x04f0:
        r0 = r20;
        r0 = r0.mIsExiting;
        r27 = r0;
        if (r27 == 0) goto L_0x0504;
    L_0x04f8:
        r0 = r20;
        r0 = r0.allAppWindows;
        r27 = r0;
        r27 = r27.isEmpty();
        if (r27 == 0) goto L_0x04ed;
    L_0x0504:
        r0 = r20;
        r0 = r0.mAppAnimator;
        r27 = r0;
        r27.clearAnimation();
        r0 = r20;
        r0 = r0.mAppAnimator;
        r27 = r0;
        r28 = 0;
        r0 = r28;
        r1 = r27;
        r1.animating = r0;
        r20.removeAppFromTaskLocked();
        goto L_0x04ed;
    L_0x051f:
        r19 = r19 + -1;
        goto L_0x04a3;
    L_0x0522:
        if (r25 == 0) goto L_0x0534;
    L_0x0524:
        r0 = r6.pendingLayoutChanges;
        r27 = r0;
        r27 = r27 | 4;
        r0 = r27;
        r6.pendingLayoutChanges = r0;
        r27 = 1;
        r0 = r27;
        r6.layoutNeeded = r0;
    L_0x0534:
        r13 = 0;
    L_0x0535:
        r0 = r18;
        if (r13 >= r0) goto L_0x055c;
    L_0x0539:
        r0 = r32;
        r0 = r0.mService;
        r27 = r0;
        r0 = r27;
        r0 = r0.mDisplayContents;
        r27 = r0;
        r0 = r27;
        r10 = r0.valueAt(r13);
        r10 = (com.android.server.wm.DisplayContent) r10;
        r0 = r10.pendingLayoutChanges;
        r27 = r0;
        if (r27 == 0) goto L_0x0559;
    L_0x0553:
        r27 = 1;
        r0 = r27;
        r10.layoutNeeded = r0;
    L_0x0559:
        r13 = r13 + 1;
        goto L_0x0535;
    L_0x055c:
        r0 = r32;
        r0 = r0.mService;
        r27 = r0;
        r0 = r27;
        r0 = r0.mInputMonitor;
        r27 = r0;
        r28 = 1;
        r27.updateInputWindowsLw(r28);
        r0 = r32;
        r0 = r0.mService;
        r27 = r0;
        r0 = r32;
        r0 = r0.mHoldScreen;
        r28 = r0;
        r27.setHoldScreenLocked(r28);
        r0 = r32;
        r0 = r0.mService;
        r27 = r0;
        r0 = r27;
        r0 = r0.mDisplayFrozen;
        r27 = r0;
        if (r27 != 0) goto L_0x05ff;
    L_0x058a:
        r0 = r32;
        r0 = r0.mScreenBrightness;
        r27 = r0;
        r28 = 0;
        r27 = (r27 > r28 ? 1 : (r27 == r28 ? 0 : -1));
        if (r27 >= 0) goto L_0x078d;
    L_0x0596:
        r27 = "android";
        r0 = r27;
        r1 = r32;
        r1.mAppBrightnessPackageName = r0;
        r27 = -1082130432; // 0xffffffffbf800000 float:-1.0 double:NaN;
        r0 = r32;
        r0 = r0.mAppBrightnessPackageName;
        r28 = r0;
        r0 = r32;
        r1 = r27;
        r2 = r28;
        r0.sendBrightnessToMonitor(r1, r2);
        r0 = r32;
        r0 = r0.mService;
        r27 = r0;
        r0 = r27;
        r0 = r0.mPowerManagerInternal;
        r27 = r0;
        r28 = -1;
        r27.setScreenBrightnessOverrideFromWindowManager(r28);
    L_0x05c1:
        r0 = r32;
        r0 = r0.mButtonBrightness;
        r27 = r0;
        r28 = 0;
        r27 = (r27 > r28 ? 1 : (r27 == r28 ? 0 : -1));
        if (r27 < 0) goto L_0x05d9;
    L_0x05cd:
        r0 = r32;
        r0 = r0.mButtonBrightness;
        r27 = r0;
        r28 = 1065353216; // 0x3f800000 float:1.0 double:5.263544247E-315;
        r27 = (r27 > r28 ? 1 : (r27 == r28 ? 0 : -1));
        if (r27 <= 0) goto L_0x07bd;
    L_0x05d9:
        r0 = r32;
        r0 = r0.mService;
        r27 = r0;
        r0 = r27;
        r0 = r0.mPowerManagerInternal;
        r27 = r0;
        r28 = -1;
        r27.setButtonBrightnessOverrideFromWindowManager(r28);
    L_0x05ea:
        r0 = r32;
        r0 = r0.mService;
        r27 = r0;
        r0 = r27;
        r0 = r0.mPowerManagerInternal;
        r27 = r0;
        r0 = r32;
        r0 = r0.mUserActivityTimeout;
        r28 = r0;
        r27.setUserActivityTimeoutOverrideFromWindowManager(r28);
    L_0x05ff:
        r0 = r32;
        r0 = r0.mSustainedPerformanceModeCurrent;
        r27 = r0;
        r0 = r32;
        r0 = r0.mSustainedPerformanceModeEnabled;
        r28 = r0;
        r0 = r27;
        r1 = r28;
        if (r0 == r1) goto L_0x063e;
    L_0x0611:
        r0 = r32;
        r0 = r0.mSustainedPerformanceModeCurrent;
        r27 = r0;
        r0 = r27;
        r1 = r32;
        r1.mSustainedPerformanceModeEnabled = r0;
        r0 = r32;
        r0 = r0.mService;
        r27 = r0;
        r0 = r27;
        r0 = r0.mPowerManagerInternal;
        r28 = r0;
        r0 = r32;
        r0 = r0.mSustainedPerformanceModeEnabled;
        r27 = r0;
        if (r27 == 0) goto L_0x07d8;
    L_0x0631:
        r27 = 1;
    L_0x0633:
        r29 = 6;
        r0 = r28;
        r1 = r29;
        r2 = r27;
        r0.powerHint(r1, r2);
    L_0x063e:
        r0 = r32;
        r0 = r0.mService;
        r27 = r0;
        r0 = r27;
        r0 = r0.mTurnOnScreen;
        r27 = r0;
        if (r27 == 0) goto L_0x0699;
    L_0x064c:
        r0 = r32;
        r0 = r0.mService;
        r27 = r0;
        r0 = r27;
        r0 = r0.mAllowTheaterModeWakeFromLayout;
        r27 = r0;
        if (r27 != 0) goto L_0x0675;
    L_0x065a:
        r0 = r32;
        r0 = r0.mService;
        r27 = r0;
        r0 = r27;
        r0 = r0.mContext;
        r27 = r0;
        r27 = r27.getContentResolver();
        r28 = "theater_mode_on";
        r29 = 0;
        r27 = android.provider.Settings.Global.getInt(r27, r28, r29);
        if (r27 != 0) goto L_0x068b;
    L_0x0675:
        r0 = r32;
        r0 = r0.mService;
        r27 = r0;
        r0 = r27;
        r0 = r0.mPowerManager;
        r27 = r0;
        r28 = android.os.SystemClock.uptimeMillis();
        r30 = "android.server.wm:TURN_ON";
        r27.wakeUp(r28, r30);
    L_0x068b:
        r0 = r32;
        r0 = r0.mService;
        r27 = r0;
        r28 = 0;
        r0 = r28;
        r1 = r27;
        r1.mTurnOnScreen = r0;
    L_0x0699:
        r0 = r32;
        r0 = r0.mUpdateRotation;
        r27 = r0;
        if (r27 == 0) goto L_0x06c0;
    L_0x06a1:
        r0 = r32;
        r0 = r0.mService;
        r27 = r0;
        r28 = 0;
        r27 = r27.updateRotationUncheckedLocked(r28);
        if (r27 == 0) goto L_0x07dc;
    L_0x06af:
        r0 = r32;
        r0 = r0.mService;
        r27 = r0;
        r0 = r27;
        r0 = r0.mH;
        r27 = r0;
        r28 = 18;
        r27.sendEmptyMessage(r28);
    L_0x06c0:
        r0 = r32;
        r0 = r0.mService;
        r27 = r0;
        r0 = r27;
        r0 = r0.mWaitingForDrawnCallback;
        r27 = r0;
        if (r27 != 0) goto L_0x07ee;
    L_0x06ce:
        r0 = r32;
        r0 = r0.mOrientationChangeComplete;
        r27 = r0;
        if (r27 == 0) goto L_0x06dc;
    L_0x06d6:
        r0 = r6.layoutNeeded;
        r27 = r0;
        if (r27 == 0) goto L_0x07e6;
    L_0x06dc:
        r0 = r32;
        r0 = r0.mService;
        r27 = r0;
        r0 = r27;
        r0 = r0.mKeyguardDismissDoneCallback;
        r27 = r0;
        if (r27 == 0) goto L_0x06f3;
    L_0x06ea:
        r0 = r32;
        r0 = r0.mService;
        r27 = r0;
        r27.checkKeyguardDismissDoneLocked();
    L_0x06f3:
        r0 = r32;
        r0 = r0.mService;
        r27 = r0;
        r0 = r27;
        r0 = r0.mPendingRemove;
        r27 = r0;
        r4 = r27.size();
        if (r4 <= 0) goto L_0x0827;
    L_0x0705:
        r0 = r32;
        r0 = r0.mService;
        r27 = r0;
        r0 = r27;
        r0 = r0.mPendingRemoveTmp;
        r27 = r0;
        r0 = r27;
        r0 = r0.length;
        r27 = r0;
        r0 = r27;
        if (r0 >= r4) goto L_0x072e;
    L_0x071a:
        r0 = r32;
        r0 = r0.mService;
        r27 = r0;
        r28 = r4 + 10;
        r0 = r28;
        r0 = new com.android.server.wm.WindowState[r0];
        r28 = r0;
        r0 = r28;
        r1 = r27;
        r1.mPendingRemoveTmp = r0;
    L_0x072e:
        r0 = r32;
        r0 = r0.mService;
        r27 = r0;
        r0 = r27;
        r0 = r0.mPendingRemove;
        r27 = r0;
        r0 = r32;
        r0 = r0.mService;
        r28 = r0;
        r0 = r28;
        r0 = r0.mPendingRemoveTmp;
        r28 = r0;
        r27.toArray(r28);
        r0 = r32;
        r0 = r0.mService;
        r27 = r0;
        r0 = r27;
        r0 = r0.mPendingRemove;
        r27 = r0;
        r27.clear();
        r12 = new com.android.server.wm.DisplayContentList;
        r12.<init>();
        r17 = 0;
    L_0x075f:
        r0 = r17;
        if (r0 >= r4) goto L_0x07fd;
    L_0x0763:
        r0 = r32;
        r0 = r0.mService;
        r27 = r0;
        r0 = r27;
        r0 = r0.mPendingRemoveTmp;
        r27 = r0;
        r24 = r27[r17];
        r0 = r32;
        r0 = r0.mService;
        r27 = r0;
        r0 = r27;
        r1 = r24;
        r0.removeWindowInnerLocked(r1);
        r10 = r24.getDisplayContent();
        if (r10 == 0) goto L_0x078a;
    L_0x0784:
        r27 = r12.contains(r10);
        if (r27 == 0) goto L_0x07f9;
    L_0x078a:
        r17 = r17 + 1;
        goto L_0x075f;
    L_0x078d:
        r0 = r32;
        r0 = r0.mScreenBrightness;
        r27 = r0;
        r0 = r32;
        r0 = r0.mAppBrightnessPackageName;
        r28 = r0;
        r0 = r32;
        r1 = r27;
        r2 = r28;
        r0.sendBrightnessToMonitor(r1, r2);
        r0 = r32;
        r0 = r0.mService;
        r27 = r0;
        r0 = r27;
        r0 = r0.mPowerManagerInternal;
        r27 = r0;
        r0 = r32;
        r0 = r0.mScreenBrightness;
        r28 = r0;
        r28 = toBrightnessOverride(r28);
        r27.setScreenBrightnessOverrideFromWindowManager(r28);
        goto L_0x05c1;
    L_0x07bd:
        r0 = r32;
        r0 = r0.mService;
        r27 = r0;
        r0 = r27;
        r0 = r0.mPowerManagerInternal;
        r27 = r0;
        r0 = r32;
        r0 = r0.mButtonBrightness;
        r28 = r0;
        r28 = toBrightnessOverride(r28);
        r27.setButtonBrightnessOverrideFromWindowManager(r28);
        goto L_0x05ea;
    L_0x07d8:
        r27 = 0;
        goto L_0x0633;
    L_0x07dc:
        r27 = 0;
        r0 = r27;
        r1 = r32;
        r1.mUpdateRotation = r0;
        goto L_0x06c0;
    L_0x07e6:
        r0 = r32;
        r0 = r0.mUpdateRotation;
        r27 = r0;
        if (r27 != 0) goto L_0x06dc;
    L_0x07ee:
        r0 = r32;
        r0 = r0.mService;
        r27 = r0;
        r27.checkDrawnWindowsLocked();
        goto L_0x06dc;
    L_0x07f9:
        r12.add(r10);
        goto L_0x078a;
    L_0x07fd:
        r11 = r12.iterator();
    L_0x0801:
        r27 = r11.hasNext();
        if (r27 == 0) goto L_0x0827;
    L_0x0807:
        r10 = r11.next();
        r10 = (com.android.server.wm.DisplayContent) r10;
        r0 = r32;
        r0 = r0.mService;
        r27 = r0;
        r0 = r27;
        r0 = r0.mLayersController;
        r27 = r0;
        r28 = r10.getWindowList();
        r27.assignLayersLocked(r28);
        r27 = 1;
        r0 = r27;
        r10.layoutNeeded = r0;
        goto L_0x0801;
    L_0x0827:
        r0 = r32;
        r0 = r0.mService;
        r27 = r0;
        r0 = r27;
        r0 = r0.mDisplayContents;
        r27 = r0;
        r27 = r27.size();
        r13 = r27 + -1;
    L_0x0839:
        if (r13 < 0) goto L_0x0855;
    L_0x083b:
        r0 = r32;
        r0 = r0.mService;
        r27 = r0;
        r0 = r27;
        r0 = r0.mDisplayContents;
        r27 = r0;
        r0 = r27;
        r27 = r0.valueAt(r13);
        r27 = (com.android.server.wm.DisplayContent) r27;
        r27.checkForDeferredActions();
        r13 = r13 + -1;
        goto L_0x0839;
    L_0x0855:
        if (r23 == 0) goto L_0x0868;
    L_0x0857:
        r0 = r32;
        r0 = r0.mService;
        r27 = r0;
        r0 = r27;
        r0 = r0.mInputMonitor;
        r27 = r0;
        r28 = 0;
        r27.updateInputWindowsLw(r28);
    L_0x0868:
        r0 = r32;
        r0 = r0.mService;
        r27 = r0;
        r27.setFocusTaskRegionLocked();
        r0 = r32;
        r0 = r0.mService;
        r27 = r0;
        r27.enableScreenIfNeededLocked();
        r0 = r32;
        r0 = r0.mService;
        r27 = r0;
        r27.scheduleAnimationLocked();
        r0 = r32;
        r0 = r0.mService;
        r27 = r0;
        r0 = r27;
        r0 = r0.mWindowPlacerLocked;
        r27 = r0;
        r27.destroyPendingSurfaces();
        return;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.wm.WindowSurfacePlacer.performSurfacePlacementInner(boolean):void");
    }

    public WindowSurfacePlacer(WindowManagerService service) {
        this.mInLayout = IS_TABLET;
        this.mWallpaperMayChange = IS_TABLET;
        this.mOrientationChangeComplete = true;
        this.mWallpaperActionPending = IS_TABLET;
        this.mWallpaperForceHidingChanged = IS_TABLET;
        this.mLastWindowFreezeSource = null;
        this.mHoldScreen = null;
        this.mObscured = IS_TABLET;
        this.mSyswin = IS_TABLET;
        this.mScreenBrightness = -1.0f;
        this.mButtonBrightness = -1.0f;
        this.mUserActivityTimeout = -1;
        this.mUpdateRotation = IS_TABLET;
        this.mTmpStartRect = new Rect();
        this.mTmpContentRect = new Rect();
        this.mDisplayHasContent = IS_TABLET;
        this.mObscureApplicationContentOnSecondaryDisplays = IS_TABLET;
        this.mPreferredRefreshRate = 0.0f;
        this.mPreferredModeId = 0;
        this.mDeferDepth = 0;
        this.mSustainedPerformanceModeEnabled = IS_TABLET;
        this.mSustainedPerformanceModeCurrent = IS_TABLET;
        this.mHoldScreenWindow = null;
        this.mObsuringWindow = null;
        this.mTmpLayerAndToken = new LayerAndToken();
        this.mPendingDestroyingSurfaces = new ArrayList();
        this.mLastIsTopIsFullscreen = IS_TABLET;
        this.mAppBrightnessLast = -1.0f;
        this.mAppBrightnessPackageNameLast = "";
        this.mService = service;
        this.mWallpaperControllerLocked = this.mService.mWallpaperControllerLocked;
        this.mDisplayEffectMonitor = HwServiceFactory.getDisplayEffectMonitor(this.mService.mContext);
        if (this.mDisplayEffectMonitor == null) {
            Slog.e(TAG, "HwServiceFactory getDisplayEffectMonitor failed!");
        }
    }

    void deferLayout() {
        this.mDeferDepth += SET_UPDATE_ROTATION;
    }

    void continueLayout() {
        this.mDeferDepth--;
        if (this.mDeferDepth <= 0) {
            performSurfacePlacement();
        }
    }

    final void performSurfacePlacement() {
        if (this.mDeferDepth <= 0) {
            int loopCount = 6;
            do {
                this.mTraversalScheduled = IS_TABLET;
                performSurfacePlacementLoop();
                this.mService.mH.removeMessages(SET_FORCE_HIDING_CHANGED);
                loopCount--;
                if (!this.mTraversalScheduled) {
                    break;
                }
            } while (loopCount > 0);
            this.mWallpaperActionPending = IS_TABLET;
            boolean isTopIsFullscreen = this.mService.mPolicy.isTopIsFullscreen();
            if (this.mLastIsTopIsFullscreen != isTopIsFullscreen) {
                this.mLastIsTopIsFullscreen = isTopIsFullscreen;
                this.mService.mInputManager.setIsTopFullScreen(this.mLastIsTopIsFullscreen);
            }
        }
    }

    private void performSurfacePlacementLoop() {
        if (this.mInLayout) {
            Slog.w(TAG, "performLayoutAndPlaceSurfacesLocked called while in layout. Callers=" + Debug.getCallers(3));
        } else if (!this.mService.mWaitingForConfig && this.mService.mDisplayReady) {
            Trace.traceBegin(32, "wmLayout");
            this.mInLayout = true;
            boolean recoveringMemory = IS_TABLET;
            if (!this.mService.mForceRemoves.isEmpty()) {
                recoveringMemory = true;
                while (!this.mService.mForceRemoves.isEmpty()) {
                    WindowState ws = (WindowState) this.mService.mForceRemoves.remove(0);
                    Slog.i(TAG, "Force removing: " + ws);
                    this.mService.removeWindowInnerLocked(ws);
                }
                Slog.w(TAG, "Due to memory failure, waiting a bit for next layout");
                Object tmp = new Object();
                synchronized (tmp) {
                    try {
                        tmp.wait(250);
                    } catch (InterruptedException e) {
                    }
                }
            }
            try {
                performSurfacePlacementInner(recoveringMemory);
                this.mInLayout = IS_TABLET;
                if (this.mService.needsLayout()) {
                    int i = this.mLayoutRepeatCount + SET_UPDATE_ROTATION;
                    this.mLayoutRepeatCount = i;
                    if (i < 6) {
                        requestTraversal();
                    } else {
                        Slog.e(TAG, "Performed 6 layouts in a row. Skipping");
                        this.mLayoutRepeatCount = 0;
                    }
                } else {
                    this.mLayoutRepeatCount = 0;
                }
                if (this.mService.mWindowsChanged && !this.mService.mWindowChangeListeners.isEmpty()) {
                    this.mService.mH.removeMessages(19);
                    this.mService.mH.sendEmptyMessage(19);
                }
            } catch (RuntimeException e2) {
                this.mInLayout = IS_TABLET;
                Slog.wtf(TAG, "Unhandled exception while laying out windows", e2);
            }
            Trace.traceEnd(32);
        }
    }

    void debugLayoutRepeats(String msg, int pendingLayoutChanges) {
        if (this.mLayoutRepeatCount >= SET_FORCE_HIDING_CHANGED) {
            Slog.v(TAG, "Layouts looping: " + msg + ", mPendingLayoutChanges = 0x" + Integer.toHexString(pendingLayoutChanges));
        }
    }

    private void applySurfaceChangesTransaction(boolean recoveringMemory, int numDisplays, int defaultDw, int defaultDh) {
        if (this.mService.mWatermark != null) {
            this.mService.mWatermark.positionSurface(defaultDw, defaultDh);
        }
        if (this.mService.mStrictModeFlash != null) {
            this.mService.mStrictModeFlash.positionSurface(defaultDw, defaultDh);
        }
        if (this.mService.mCircularDisplayMask != null) {
            this.mService.mCircularDisplayMask.positionSurface(defaultDw, defaultDh, this.mService.mRotation);
        }
        if (this.mService.mEmulatorDisplayOverlay != null) {
            this.mService.mEmulatorDisplayOverlay.positionSurface(defaultDw, defaultDh, this.mService.mRotation);
        }
        boolean focusDisplayed = IS_TABLET;
        for (int displayNdx = 0; displayNdx < numDisplays; displayNdx += SET_UPDATE_ROTATION) {
            WindowState w;
            DisplayContent displayContent = (DisplayContent) this.mService.mDisplayContents.valueAt(displayNdx);
            boolean updateAllDrawn = IS_TABLET;
            WindowList windows = displayContent.getWindowList();
            DisplayInfo displayInfo = displayContent.getDisplayInfo();
            int displayId = displayContent.getDisplayId();
            int dw = displayInfo.logicalWidth;
            int dh = displayInfo.logicalHeight;
            int innerDw = displayInfo.appWidth;
            int innerDh = displayInfo.appHeight;
            boolean isDefaultDisplay = displayId == 0 ? true : IS_TABLET;
            this.mDisplayHasContent = IS_TABLET;
            this.mPreferredRefreshRate = 0.0f;
            this.mPreferredModeId = 0;
            int repeats = 0;
            while (true) {
                int i;
                repeats += SET_UPDATE_ROTATION;
                if (repeats <= 6) {
                    if ((displayContent.pendingLayoutChanges & SET_FORCE_HIDING_CHANGED) != 0 && this.mWallpaperControllerLocked.adjustWallpaperWindows()) {
                        this.mService.mLayersController.assignLayersLocked(windows);
                        displayContent.layoutNeeded = true;
                    }
                    if (isDefaultDisplay && (displayContent.pendingLayoutChanges & SET_WALLPAPER_MAY_CHANGE) != 0 && this.mService.updateOrientationFromAppTokensLocked(true)) {
                        displayContent.layoutNeeded = true;
                        this.mService.mH.sendEmptyMessage(18);
                    }
                    if ((displayContent.pendingLayoutChanges & SET_UPDATE_ROTATION) != 0) {
                        displayContent.layoutNeeded = true;
                    }
                    if (repeats < SET_FORCE_HIDING_CHANGED) {
                        performLayoutLockedInner(displayContent, repeats == SET_UPDATE_ROTATION ? true : IS_TABLET, IS_TABLET);
                    } else {
                        Slog.w(TAG, "Layout repeat skipped after too many iterations");
                    }
                    displayContent.pendingLayoutChanges = 0;
                    if (isDefaultDisplay) {
                        this.mService.mPolicy.beginPostLayoutPolicyLw(dw, dh);
                        for (i = windows.size() - 1; i >= 0; i--) {
                            w = (WindowState) windows.get(i);
                            if (w.mHasSurface) {
                                this.mService.mPolicy.applyPostLayoutPolicyLw(w, w.mAttrs, w.mAttachedWindow);
                            }
                        }
                        displayContent.pendingLayoutChanges |= this.mService.mPolicy.finishPostLayoutPolicyLw();
                    }
                    if (displayContent.pendingLayoutChanges == 0) {
                        break;
                    }
                } else {
                    break;
                }
            }
            Slog.w(TAG, "Animation repeat aborted after too many iterations");
            displayContent.layoutNeeded = IS_TABLET;
            this.mObscured = IS_TABLET;
            this.mSyswin = IS_TABLET;
            displayContent.resetDimming();
            boolean someoneLosingFocus = this.mService.mLosingFocus.isEmpty() ? IS_TABLET : true;
            for (i = windows.size() - 1; i >= 0; i--) {
                w = (WindowState) windows.get(i);
                Task task = w.getTask();
                boolean obscuredChanged = w.mObscured != this.mObscured ? true : IS_TABLET;
                w.mObscured = this.mObscured;
                if (!this.mObscured) {
                    handleNotObscuredLocked(w, displayInfo);
                }
                w.applyDimLayerIfNeeded();
                if (isDefaultDisplay && obscuredChanged && this.mWallpaperControllerLocked.isWallpaperTarget(w) && w.isVisibleLw()) {
                    this.mWallpaperControllerLocked.updateWallpaperVisibility();
                }
                WindowStateAnimator winAnimator = w.mWinAnimator;
                if (w.hasMoved()) {
                    int left = w.mFrame.left;
                    int top = w.mFrame.top;
                    boolean adjustedForMinimizedDockOrIme;
                    if (task == null) {
                        adjustedForMinimizedDockOrIme = IS_TABLET;
                    } else if (task.mStack.isAdjustedForMinimizedDockedStack()) {
                        adjustedForMinimizedDockOrIme = true;
                    } else {
                        adjustedForMinimizedDockOrIme = task.mStack.isAdjustedForIme();
                    }
                    if ((w.mAttrs.privateFlags & 64) == 0 && !w.isDragResizing() && !adjustedForMinimizedDockOrIme && ((task == null || w.getTask().mStack.hasMovementAnimations()) && !w.mWinAnimator.mLastHidden)) {
                        winAnimator.setMoveAnimation(left, top);
                    }
                    if (this.mService.mAccessibilityController != null && displayId == 0) {
                        this.mService.mAccessibilityController.onSomeWindowResizedOrMovedLocked();
                    }
                    try {
                        w.mClient.moved(left, top);
                    } catch (RemoteException e) {
                    }
                    w.mMovedByResize = IS_TABLET;
                }
                w.mContentChanged = IS_TABLET;
                if (w.mHasSurface) {
                    winAnimator.deferToPendingTransaction();
                    boolean committed = winAnimator.commitFinishDrawingLocked();
                    if (isDefaultDisplay && committed) {
                        if (w.mAttrs.type == 2023) {
                            displayContent.pendingLayoutChanges |= SET_UPDATE_ROTATION;
                        }
                        if ((w.mAttrs.flags & DumpState.DUMP_DEXOPT) != 0) {
                            this.mWallpaperMayChange = true;
                            displayContent.pendingLayoutChanges |= SET_FORCE_HIDING_CHANGED;
                        }
                    }
                    if (!(winAnimator.isAnimationStarting() || winAnimator.isWaitingForOpening())) {
                        winAnimator.computeShownFrameLocked();
                    }
                    winAnimator.setSurfaceBoundariesLocked(recoveringMemory);
                }
                AppWindowToken atoken = w.mAppToken;
                if (!(atoken == null || (atoken.allDrawn && atoken.allDrawnExcludingSaved && !atoken.mAppAnimator.freezingScreen))) {
                    if (atoken.lastTransactionSequence != ((long) this.mService.mTransactionSequence)) {
                        atoken.lastTransactionSequence = (long) this.mService.mTransactionSequence;
                        atoken.numDrawnWindows = 0;
                        atoken.numInterestingWindows = 0;
                        atoken.numInterestingWindowsExcludingSaved = 0;
                        atoken.numDrawnWindowsExclusingSaved = 0;
                        atoken.startingDisplayed = IS_TABLET;
                    }
                    if (!atoken.allDrawn && w.mightAffectAllDrawn(IS_TABLET)) {
                        if (w != atoken.startingWindow) {
                            if (w.isInteresting()) {
                                atoken.numInterestingWindows += SET_UPDATE_ROTATION;
                                if (w.isDrawnLw()) {
                                    atoken.numDrawnWindows += SET_UPDATE_ROTATION;
                                    updateAllDrawn = true;
                                }
                            }
                        } else if (w.isDrawnLw()) {
                            this.mService.mH.sendEmptyMessage(50);
                            atoken.startingDisplayed = true;
                        }
                    }
                    if (!atoken.allDrawnExcludingSaved && w.mightAffectAllDrawn(true) && w != atoken.startingWindow && w.isInteresting()) {
                        atoken.numInterestingWindowsExcludingSaved += SET_UPDATE_ROTATION;
                        if (w.isDrawnLw() && !w.isAnimatingWithSavedSurface()) {
                            atoken.numDrawnWindowsExclusingSaved += SET_UPDATE_ROTATION;
                            updateAllDrawn = true;
                        }
                    }
                }
                if (isDefaultDisplay && someoneLosingFocus && w == this.mService.mCurrentFocus && w.isDisplayedLw()) {
                    focusDisplayed = true;
                }
                this.mService.updateResizingWindows(w);
            }
            this.mService.mDisplayManagerInternal.setDisplayProperties(displayId, this.mDisplayHasContent, this.mPreferredRefreshRate, this.mPreferredModeId, true);
            this.mService.getDisplayContentLocked(displayId).stopDimmingIfNeeded();
            if (updateAllDrawn) {
                updateAllDrawnLocked(displayContent);
            }
        }
        if (focusDisplayed) {
            this.mService.mH.sendEmptyMessage(3);
        }
        this.mService.mDisplayManagerInternal.performTraversalInTransactionFromWindowManager();
    }

    boolean isInLayout() {
        return this.mInLayout;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    final void performLayoutLockedInner(DisplayContent displayContent, boolean initial, boolean updateInputWindows) {
        if (displayContent.layoutNeeded) {
            WindowState win;
            displayContent.layoutNeeded = IS_TABLET;
            WindowList windows = displayContent.getWindowList();
            boolean isDefaultDisplay = displayContent.isDefaultDisplay;
            DisplayInfo displayInfo = displayContent.getDisplayInfo();
            int dw = displayInfo.logicalWidth;
            int dh = displayInfo.logicalHeight;
            if (this.mService.mInputConsumer != null) {
                this.mService.mInputConsumer.layout(dw, dh);
            }
            if (this.mService.mWallpaperInputConsumer != null) {
                this.mService.mWallpaperInputConsumer.layout(dw, dh);
            }
            int N = windows.size();
            this.mService.setNaviBarFlag();
            this.mService.updateInputImmersiveMode();
            this.mService.mPolicy.beginLayoutLw(isDefaultDisplay, dw, dh, this.mService.mRotation, this.mService.mCurConfiguration.uiMode);
            if (isDefaultDisplay) {
                this.mService.mSystemDecorLayer = this.mService.mPolicy.getSystemDecorLayerLw();
                this.mService.mScreenRect.set(0, 0, dw, dh);
            }
            this.mService.mPolicy.getContentRectLw(this.mTmpContentRect);
            displayContent.resize(this.mTmpContentRect);
            int seq = this.mService.mLayoutSeq + SET_UPDATE_ROTATION;
            if (seq < 0) {
                seq = 0;
            }
            this.mService.mLayoutSeq = seq;
            boolean behindDream = IS_TABLET;
            int topAttached = -1;
            int i = N - 1;
            while (i >= 0) {
                boolean gone;
                Task task;
                win = (WindowState) windows.get(i);
                if (initial && IS_DEBUG_VERSION) {
                    ArrayMap<String, Object> params = new ArrayMap();
                    params.put("checkType", "HighWindowLayerScene");
                    params.put("newCircle", Boolean.valueOf(i == N + -1 ? true : IS_TABLET));
                    params.put("number", Integer.valueOf(i));
                    params.put("windowState", win);
                    if (HwServiceFactory.getWinFreezeScreenMonitor() != null) {
                        HwServiceFactory.getWinFreezeScreenMonitor().checkFreezeScreen(params);
                    }
                }
                boolean isScreenOn = IS_TABLET ? !this.mService.isCoverOpen() ? this.mService.mPowerManager.isScreenOn() : true : this.mService.isCoverOpen();
                if (behindDream) {
                }
                if (!win.isGoneForLayoutLw() && r13) {
                    gone = IS_TABLET;
                    if (!gone || !win.mHaveFrame || win.mLayoutNeeded || ((win.isConfigChanged() || win.setInsetsChanged()) && !win.isGoneForLayoutLw() && ((win.mAttrs.privateFlags & DumpState.DUMP_PROVIDERS) != 0 || (win.mHasSurface && win.mAppToken != null && win.mAppToken.layoutConfigChanges)))) {
                        if (win.mLayoutAttached) {
                            if (initial) {
                                win.mContentChanged = IS_TABLET;
                            }
                            if (win.mAttrs.type == 2023) {
                                behindDream = true;
                            }
                            win.mLayoutNeeded = IS_TABLET;
                            win.prelayout();
                            this.mService.mPolicy.layoutWindowLw(win, null);
                            win.mLayoutSeq = seq;
                            task = win.getTask();
                            if (task == null) {
                                displayContent.mDimLayerController.updateDimLayer(task);
                            }
                        } else if (topAttached >= 0) {
                            topAttached = i;
                        }
                    }
                    i--;
                }
                if (win.mAttrs.type == 2100 || win.mAttrs.type == 2101) {
                    gone = IS_TABLET;
                    if (win.mLayoutAttached) {
                        if (initial) {
                            win.mContentChanged = IS_TABLET;
                        }
                        if (win.mAttrs.type == 2023) {
                            behindDream = true;
                        }
                        win.mLayoutNeeded = IS_TABLET;
                        win.prelayout();
                        this.mService.mPolicy.layoutWindowLw(win, null);
                        win.mLayoutSeq = seq;
                        task = win.getTask();
                        if (task == null) {
                            displayContent.mDimLayerController.updateDimLayer(task);
                        }
                    } else if (topAttached >= 0) {
                        topAttached = i;
                    }
                    i--;
                } else {
                    gone = win.mAttrs.type != IHwShutdownThread.SHUTDOWN_ANIMATION_WAIT_TIME ? true : IS_TABLET;
                    if (win.mLayoutAttached) {
                        if (initial) {
                            win.mContentChanged = IS_TABLET;
                        }
                        if (win.mAttrs.type == 2023) {
                            behindDream = true;
                        }
                        win.mLayoutNeeded = IS_TABLET;
                        win.prelayout();
                        this.mService.mPolicy.layoutWindowLw(win, null);
                        win.mLayoutSeq = seq;
                        task = win.getTask();
                        if (task == null) {
                            displayContent.mDimLayerController.updateDimLayer(task);
                        }
                    } else if (topAttached >= 0) {
                        topAttached = i;
                    }
                    i--;
                }
            }
            boolean attachedBehindDream = IS_TABLET;
            for (i = topAttached; i >= 0; i--) {
                win = (WindowState) windows.get(i);
                if (win.mLayoutAttached) {
                    if (attachedBehindDream) {
                        if (this.mService.mPolicy.canBeForceHidden(win, win.mAttrs)) {
                        }
                    }
                    if ((win.mViewVisibility != SET_ORIENTATION_CHANGE_COMPLETE && win.mRelayoutCalled) || !win.mHaveFrame || win.mLayoutNeeded) {
                        if (initial) {
                            win.mContentChanged = IS_TABLET;
                        }
                        win.mLayoutNeeded = IS_TABLET;
                        win.prelayout();
                        this.mService.mPolicy.layoutWindowLw(win, win.mAttachedWindow);
                        win.mLayoutSeq = seq;
                    }
                } else if (win.mAttrs.type == 2023) {
                    attachedBehindDream = behindDream;
                }
            }
            this.mService.mInputMonitor.setUpdateInputWindowsNeededLw();
            if (updateInputWindows) {
                this.mService.mInputMonitor.updateInputWindowsLw(IS_TABLET);
            }
            this.mService.mPolicy.finishLayoutLw();
            this.mService.mH.sendEmptyMessage(41);
        }
    }

    private int handleAppTransitionReadyLocked(WindowList windows) {
        if (!transitionGoodToGo(this.mService.mOpeningApps.size())) {
            return 0;
        }
        AppWindowToken appWindowToken;
        AppWindowAnimator appWindowAnimator;
        AppWindowAnimator appWindowAnimator2;
        int transit = this.mService.mAppTransition.getAppTransition();
        if (this.mService.mSkipAppTransitionAnimation) {
            transit = -1;
        }
        this.mService.mSkipAppTransitionAnimation = IS_TABLET;
        this.mService.mNoAnimationNotifyOnTransitionFinished.clear();
        this.mService.mH.removeMessages(13);
        this.mService.rebuildAppWindowListLocked();
        this.mWallpaperMayChange = IS_TABLET;
        LayoutParams animLp = null;
        int bestAnimLayer = -1;
        boolean fullscreenAnim = IS_TABLET;
        boolean voiceInteraction = IS_TABLET;
        WindowState lowerWallpaperTarget = this.mWallpaperControllerLocked.getLowerWallpaperTarget();
        WindowState upperWallpaperTarget = this.mWallpaperControllerLocked.getUpperWallpaperTarget();
        boolean openingAppHasWallpaper = IS_TABLET;
        boolean closingAppHasWallpaper = IS_TABLET;
        AppWindowToken upperWallpaperAppToken;
        if (lowerWallpaperTarget == null) {
            upperWallpaperAppToken = null;
            appWindowToken = null;
        } else {
            appWindowToken = lowerWallpaperTarget.mAppToken;
            upperWallpaperAppToken = upperWallpaperTarget.mAppToken;
        }
        int closingAppsCount = this.mService.mClosingApps.size();
        int appsCount = closingAppsCount + this.mService.mOpeningApps.size();
        for (int i = 0; i < appsCount; i += SET_UPDATE_ROTATION) {
            AppWindowToken wtoken;
            if (i < closingAppsCount) {
                wtoken = (AppWindowToken) this.mService.mClosingApps.valueAt(i);
                if (wtoken == appWindowToken || wtoken == r21) {
                    closingAppHasWallpaper = true;
                }
            } else {
                wtoken = (AppWindowToken) this.mService.mOpeningApps.valueAt(i - closingAppsCount);
                if (wtoken == appWindowToken || wtoken == r21) {
                    openingAppHasWallpaper = true;
                }
            }
            voiceInteraction |= wtoken.voiceInteraction;
            WindowState ws;
            if (wtoken.appFullscreen) {
                ws = wtoken.findMainWindow();
                if (ws != null) {
                    animLp = ws.mAttrs;
                    bestAnimLayer = ws.mLayer;
                    fullscreenAnim = true;
                }
            } else if (!fullscreenAnim) {
                ws = wtoken.findMainWindow();
                if (ws != null && ws.mLayer > bestAnimLayer) {
                    animLp = ws.mAttrs;
                    bestAnimLayer = ws.mLayer;
                }
            }
        }
        transit = maybeUpdateTransitToWallpaper(transit, openingAppHasWallpaper, closingAppHasWallpaper, lowerWallpaperTarget, upperWallpaperTarget);
        if (!this.mService.mPolicy.allowAppAnimationsLw()) {
            animLp = null;
        }
        processApplicationsAnimatingInPlace(transit);
        this.mTmpLayerAndToken.token = null;
        handleClosingApps(transit, animLp, voiceInteraction, this.mTmpLayerAndToken);
        AppWindowToken topClosingApp = this.mTmpLayerAndToken.token;
        AppWindowToken topOpeningApp = handleOpeningApps(transit, animLp, voiceInteraction, this.mTmpLayerAndToken.layer);
        if (topOpeningApp == null) {
            appWindowAnimator = null;
        } else {
            appWindowAnimator = topOpeningApp.mAppAnimator;
        }
        if (topClosingApp == null) {
            appWindowAnimator2 = null;
        } else {
            appWindowAnimator2 = topClosingApp.mAppAnimator;
        }
        this.mService.mAppTransition.goodToGo(appWindowAnimator, appWindowAnimator2, this.mService.mOpeningApps, this.mService.mClosingApps);
        this.mService.mAppTransition.postAnimationCallback();
        this.mService.mAppTransition.clear();
        this.mService.mOpeningApps.clear();
        this.mService.mClosingApps.clear();
        this.mService.getDefaultDisplayContentLocked().layoutNeeded = true;
        if (windows == this.mService.getDefaultWindowListLocked() && !this.mService.moveInputMethodWindowsIfNeededLocked(true)) {
            this.mService.mLayersController.assignLayersLocked(windows);
        }
        this.mService.updateFocusedWindowLocked(SET_WALLPAPER_MAY_CHANGE, true);
        this.mService.mFocusMayChange = IS_TABLET;
        this.mService.notifyActivityDrawnForKeyguard();
        return 3;
    }

    private AppWindowToken handleOpeningApps(int transit, LayoutParams animLp, boolean voiceInteraction, int topClosingLayer) {
        AppWindowToken topOpeningApp = null;
        int appsCount = this.mService.mOpeningApps.size();
        int i = 0;
        while (i < appsCount) {
            int j;
            AppWindowToken wtoken = (AppWindowToken) this.mService.mOpeningApps.valueAt(i);
            AppWindowAnimator appAnimator = wtoken.mAppAnimator;
            if (!appAnimator.usingTransferredAnimation) {
                appAnimator.clearThumbnail();
                appAnimator.setNullAnimation();
            }
            wtoken.inPendingTransaction = IS_TABLET;
            if (!this.mService.setTokenVisibilityLocked(wtoken, animLp, true, transit, IS_TABLET, voiceInteraction)) {
                this.mService.mNoAnimationNotifyOnTransitionFinished.add(wtoken.token);
            }
            wtoken.updateReportedVisibilityLocked();
            wtoken.waitingToShow = IS_TABLET;
            appAnimator.mAllAppWinAnimators.clear();
            int windowsCount = wtoken.allAppWindows.size();
            for (j = 0; j < windowsCount; j += SET_UPDATE_ROTATION) {
                appAnimator.mAllAppWinAnimators.add(((WindowState) wtoken.allAppWindows.get(j)).mWinAnimator);
            }
            SurfaceControl.openTransaction();
            try {
                this.mService.mAnimator.orAnimating(appAnimator.showAllWindowsLocked());
                WindowAnimator windowAnimator = this.mService.mAnimator;
                windowAnimator.mAppWindowAnimating |= appAnimator.isAnimating();
                int topOpeningLayer = 0;
                if (animLp != null) {
                    int layer = -1;
                    for (j = 0; j < wtoken.allAppWindows.size(); j += SET_UPDATE_ROTATION) {
                        WindowState win = (WindowState) wtoken.allAppWindows.get(j);
                        if (!(win.mWillReplaceWindow || win.mRemoveOnExit)) {
                            win.mAnimatingExit = IS_TABLET;
                            win.mWinAnimator.mAnimating = IS_TABLET;
                        }
                        if (win.mWinAnimator.mAnimLayer > layer) {
                            layer = win.mWinAnimator.mAnimLayer;
                        }
                    }
                    if (topOpeningApp == null || layer > 0) {
                        topOpeningApp = wtoken;
                        topOpeningLayer = layer;
                    }
                }
                if (this.mService.mAppTransition.isNextAppTransitionThumbnailUp()) {
                    createThumbnailAppAnimator(transit, wtoken, topOpeningLayer, topClosingLayer);
                }
                i += SET_UPDATE_ROTATION;
            } finally {
                SurfaceControl.closeTransaction();
            }
        }
        return topOpeningApp;
    }

    private void handleClosingApps(int transit, LayoutParams animLp, boolean voiceInteraction, LayerAndToken layerAndToken) {
        int appsCount = this.mService.mClosingApps.size();
        for (int i = 0; i < appsCount; i += SET_UPDATE_ROTATION) {
            AppWindowToken wtoken = (AppWindowToken) this.mService.mClosingApps.valueAt(i);
            wtoken.markSavedSurfaceExiting();
            AppWindowAnimator appAnimator = wtoken.mAppAnimator;
            appAnimator.clearThumbnail();
            appAnimator.setNullAnimation();
            wtoken.inPendingTransaction = IS_TABLET;
            this.mService.setTokenVisibilityLocked(wtoken, animLp, IS_TABLET, transit, IS_TABLET, voiceInteraction);
            wtoken.updateReportedVisibilityLocked();
            wtoken.allDrawn = true;
            wtoken.deferClearAllDrawn = IS_TABLET;
            if (!(wtoken.startingWindow == null || wtoken.startingWindow.mAnimatingExit)) {
                this.mService.scheduleRemoveStartingWindowLocked(wtoken);
            }
            WindowAnimator windowAnimator = this.mService.mAnimator;
            windowAnimator.mAppWindowAnimating |= appAnimator.isAnimating();
            if (animLp != null) {
                int layer = -1;
                for (int j = 0; j < wtoken.windows.size(); j += SET_UPDATE_ROTATION) {
                    WindowState win = (WindowState) wtoken.windows.get(j);
                    if (win.mWinAnimator.mAnimLayer > layer) {
                        layer = win.mWinAnimator.mAnimLayer;
                    }
                }
                if (layerAndToken.token == null || layer > layerAndToken.layer) {
                    layerAndToken.token = wtoken;
                    layerAndToken.layer = layer;
                }
            }
            if (this.mService.mAppTransition.isNextAppTransitionThumbnailDown()) {
                createThumbnailAppAnimator(transit, wtoken, 0, layerAndToken.layer);
            }
        }
    }

    private boolean transitionGoodToGo(int appsCount) {
        int reason = 3;
        if (this.mService.mAppTransition.isTimeout()) {
            this.mService.mH.obtainMessage(47, 3, 0).sendToTarget();
            return true;
        }
        for (int i = 0; i < appsCount; i += SET_UPDATE_ROTATION) {
            AppWindowToken wtoken = (AppWindowToken) this.mService.mOpeningApps.valueAt(i);
            if (wtoken.isRelaunching()) {
                return IS_TABLET;
            }
            boolean drawnBeforeRestoring = wtoken.allDrawn;
            wtoken.restoreSavedSurfaces();
            if (!wtoken.allDrawn && !wtoken.startingDisplayed && !wtoken.startingMoved) {
                return IS_TABLET;
            }
            if (!wtoken.allDrawn) {
                reason = SET_UPDATE_ROTATION;
            } else if (drawnBeforeRestoring) {
                reason = SET_WALLPAPER_MAY_CHANGE;
            } else {
                reason = 0;
            }
        }
        if (this.mService.mAppTransition.isFetchingAppTransitionsSpecs()) {
            return IS_TABLET;
        }
        boolean wallpaperReady;
        if (this.mWallpaperControllerLocked.isWallpaperVisible()) {
            wallpaperReady = this.mWallpaperControllerLocked.wallpaperTransitionReady();
        } else {
            wallpaperReady = true;
        }
        if (!wallpaperReady) {
            return IS_TABLET;
        }
        this.mService.mH.obtainMessage(47, reason, 0).sendToTarget();
        return true;
    }

    private int maybeUpdateTransitToWallpaper(int transit, boolean openingAppHasWallpaper, boolean closingAppHasWallpaper, WindowState lowerWallpaperTarget, WindowState upperWallpaperTarget) {
        WindowState wallpaperTarget = this.mWallpaperControllerLocked.getWallpaperTarget();
        WindowState windowState = this.mWallpaperControllerLocked.isWallpaperTargetAnimating() ? null : wallpaperTarget;
        ArraySet<AppWindowToken> openingApps = this.mService.mOpeningApps;
        ArraySet<AppWindowToken> closingApps = this.mService.mClosingApps;
        this.mService.mAnimateWallpaperWithTarget = IS_TABLET;
        if (closingAppHasWallpaper && openingAppHasWallpaper) {
            switch (transit) {
                case H.REMOVE_STARTING /*6*/:
                case SET_ORIENTATION_CHANGE_COMPLETE /*8*/:
                case AppTransition.TRANSIT_TASK_TO_FRONT /*10*/:
                    return 14;
                case H.FINISHED_STARTING /*7*/:
                case H.REPORT_APPLICATION_TOKEN_DRAWN /*9*/:
                case H.WINDOW_FREEZE_TIMEOUT /*11*/:
                    return 15;
                default:
                    return transit;
            }
        } else if (windowState != null && !this.mService.mOpeningApps.isEmpty() && !openingApps.contains(windowState.mAppToken) && closingApps.contains(windowState.mAppToken)) {
            return 12;
        } else {
            if (wallpaperTarget != null && wallpaperTarget.isVisibleLw() && openingApps.contains(wallpaperTarget.mAppToken)) {
                return 13;
            }
            this.mService.mAnimateWallpaperWithTarget = true;
            return transit;
        }
    }

    private boolean isWindowVisibleInKeyguard(LayoutParams attrs) {
        boolean z = true;
        if (!this.mService.mPolicy.isKeyguardShowingOrOccluded() || (attrs.flags & DumpState.DUMP_FROZEN) != 0) {
            return true;
        }
        if (attrs.type == SET_UPDATE_ROTATION) {
            z = IS_TABLET;
        }
        return z;
    }

    private void handleNotObscuredLocked(WindowState w, DisplayInfo dispInfo) {
        LayoutParams attrs = w.mAttrs;
        int attrFlags = attrs.flags;
        boolean canBeSeen = w.isDisplayedLw();
        int privateflags = attrs.privateFlags;
        if (canBeSeen && w.isObscuringFullscreen(dispInfo)) {
            if (!this.mObscured) {
                this.mObsuringWindow = w;
            }
            this.mObscured = true;
        }
        if (w.mHasSurface) {
            if ((attrFlags & DumpState.DUMP_PACKAGES) != 0 && isWindowVisibleInKeyguard(attrs)) {
                this.mHoldScreen = w.mSession;
                this.mHoldScreenWindow = w;
            }
            if (!this.mSyswin && w.mAttrs.screenBrightness >= 0.0f && this.mScreenBrightness < 0.0f && w.isVisibleLw()) {
                this.mScreenBrightness = w.mAttrs.screenBrightness;
                this.mAppBrightnessPackageName = w.mAttrs.packageName;
            }
            if (!this.mSyswin && w.mAttrs.buttonBrightness >= 0.0f && this.mButtonBrightness < 0.0f) {
                this.mButtonBrightness = w.mAttrs.buttonBrightness;
            }
            if (!this.mSyswin && w.mAttrs.userActivityTimeout >= 0 && this.mUserActivityTimeout < 0) {
                if ((w.mAttrs.privateFlags & DumpState.DUMP_PROVIDERS) == 0 || !this.mService.mDestroySurface.contains(w)) {
                    this.mUserActivityTimeout = w.mAttrs.userActivityTimeout;
                } else {
                    Slog.e(TAG, "do not set userActivityTimeout this time");
                }
            }
            int type = attrs.type;
            if (canBeSeen) {
                if (!(type == 2008 || type == 2010)) {
                    if ((attrs.privateFlags & DumpState.DUMP_PROVIDERS) != 0) {
                    }
                }
                this.mSyswin = true;
            }
            if (canBeSeen) {
                DisplayContent displayContent = w.getDisplayContent();
                if (displayContent != null && displayContent.isDefaultDisplay) {
                    if (type == 2023 || (attrs.privateFlags & DumpState.DUMP_PROVIDERS) != 0) {
                        this.mObscureApplicationContentOnSecondaryDisplays = true;
                    }
                    this.mDisplayHasContent = true;
                } else if (displayContent != null && (!this.mObscureApplicationContentOnSecondaryDisplays || (this.mObscured && type == 2009))) {
                    this.mDisplayHasContent = true;
                }
                if (this.mPreferredRefreshRate == 0.0f && w.mAttrs.preferredRefreshRate != 0.0f) {
                    this.mPreferredRefreshRate = w.mAttrs.preferredRefreshRate;
                }
                if (this.mPreferredModeId == 0 && w.mAttrs.preferredDisplayModeId != 0) {
                    this.mPreferredModeId = w.mAttrs.preferredDisplayModeId;
                }
                if ((DumpState.DUMP_DOMAIN_PREFERRED & privateflags) != 0) {
                    this.mSustainedPerformanceModeCurrent = true;
                }
            }
        }
    }

    private void updateAllDrawnLocked(DisplayContent displayContent) {
        ArrayList<TaskStack> stacks = displayContent.getStacks();
        for (int stackNdx = stacks.size() - 1; stackNdx >= 0; stackNdx--) {
            ArrayList<Task> tasks = ((TaskStack) stacks.get(stackNdx)).getTasks();
            for (int taskNdx = tasks.size() - 1; taskNdx >= 0; taskNdx--) {
                AppTokenList tokens = ((Task) tasks.get(taskNdx)).mAppTokens;
                for (int tokenNdx = tokens.size() - 1; tokenNdx >= 0; tokenNdx--) {
                    int numInteresting;
                    AppWindowToken wtoken = (AppWindowToken) tokens.get(tokenNdx);
                    if (!wtoken.allDrawn) {
                        numInteresting = wtoken.numInterestingWindows;
                        if (numInteresting > 0 && wtoken.numDrawnWindows >= numInteresting) {
                            wtoken.allDrawn = true;
                            displayContent.layoutNeeded = true;
                            this.mService.mH.obtainMessage(SET_WALLPAPER_ACTION_PENDING, wtoken.token).sendToTarget();
                        }
                    }
                    if (!wtoken.allDrawnExcludingSaved) {
                        numInteresting = wtoken.numInterestingWindowsExcludingSaved;
                        if (numInteresting > 0 && wtoken.numDrawnWindowsExclusingSaved >= numInteresting) {
                            wtoken.allDrawnExcludingSaved = true;
                            displayContent.layoutNeeded = true;
                            if (wtoken.isAnimatingInvisibleWithSavedSurface() && !this.mService.mFinishedEarlyAnim.contains(wtoken)) {
                                this.mService.mFinishedEarlyAnim.add(wtoken);
                            }
                        }
                    }
                }
            }
        }
    }

    private static int toBrightnessOverride(float value) {
        return (int) (255.0f * value);
    }

    private void processApplicationsAnimatingInPlace(int transit) {
        if (transit == 17) {
            WindowState win = this.mService.findFocusedWindowLocked(this.mService.getDefaultDisplayContentLocked());
            if (win != null) {
                AppWindowToken wtoken = win.mAppToken;
                AppWindowAnimator appAnimator = wtoken.mAppAnimator;
                appAnimator.clearThumbnail();
                appAnimator.setNullAnimation();
                this.mService.updateTokenInPlaceLocked(wtoken, transit);
                wtoken.updateReportedVisibilityLocked();
                appAnimator.mAllAppWinAnimators.clear();
                int N = wtoken.allAppWindows.size();
                for (int j = 0; j < N; j += SET_UPDATE_ROTATION) {
                    appAnimator.mAllAppWinAnimators.add(((WindowState) wtoken.allAppWindows.get(j)).mWinAnimator);
                }
                WindowAnimator windowAnimator = this.mService.mAnimator;
                windowAnimator.mAppWindowAnimating |= appAnimator.isAnimating();
                this.mService.mAnimator.orAnimating(appAnimator.showAllWindowsLocked());
            }
        }
    }

    private void createThumbnailAppAnimator(int transit, AppWindowToken appToken, int openingLayer, int closingLayer) {
        AppWindowAnimator openingAppAnimator = appToken == null ? null : appToken.mAppAnimator;
        if (openingAppAnimator != null && openingAppAnimator.animation != null) {
            int taskId = appToken.mTask.mTaskId;
            Bitmap thumbnailHeader = this.mService.mAppTransition.getAppTransitionThumbnailHeader(taskId);
            if (thumbnailHeader != null && thumbnailHeader.getConfig() != Config.ALPHA_8) {
                Rect dirty = new Rect(0, 0, thumbnailHeader.getWidth(), thumbnailHeader.getHeight());
                try {
                    Animation anim;
                    DisplayContent displayContent = this.mService.getDefaultDisplayContentLocked();
                    Display display = displayContent.getDisplay();
                    DisplayInfo displayInfo = displayContent.getDisplayInfo();
                    SurfaceControl surfaceControl = new SurfaceControl(this.mService.mFxSession, "thumbnail anim", dirty.width(), dirty.height(), -3, SET_FORCE_HIDING_CHANGED);
                    surfaceControl.setLayerStack(display.getLayerStack());
                    Surface drawSurface = new Surface();
                    drawSurface.copyFrom(surfaceControl);
                    Canvas c = drawSurface.lockCanvas(dirty);
                    c.drawBitmap(thumbnailHeader, 0.0f, 0.0f, null);
                    drawSurface.unlockCanvasAndPost(c);
                    drawSurface.release();
                    if (this.mService.mAppTransition.isNextThumbnailTransitionAspectScaled()) {
                        Rect appRect;
                        WindowState win = appToken.findMainWindow();
                        if (win != null) {
                            appRect = win.getContentFrameLw();
                        } else {
                            appRect = new Rect(0, 0, displayInfo.appWidth, displayInfo.appHeight);
                        }
                        anim = this.mService.mAppTransition.createThumbnailAspectScaleAnimationLocked(appRect, win != null ? win.mContentInsets : null, thumbnailHeader, taskId, this.mService.mCurConfiguration.uiMode, this.mService.mCurConfiguration.orientation);
                        openingAppAnimator.thumbnailForceAboveLayer = Math.max(openingLayer, closingLayer);
                        openingAppAnimator.deferThumbnailDestruction = this.mService.mAppTransition.isNextThumbnailTransitionScaleUp() ? IS_TABLET : true;
                    } else {
                        anim = this.mService.mAppTransition.createThumbnailScaleAnimationLocked(displayInfo.appWidth, displayInfo.appHeight, transit, thumbnailHeader);
                    }
                    anim.restrictDuration(JobStatus.DEFAULT_TRIGGER_UPDATE_DELAY);
                    anim.scaleCurrentDuration(this.mService.getTransitionAnimationScaleLocked());
                    openingAppAnimator.thumbnail = surfaceControl;
                    openingAppAnimator.thumbnailLayer = openingLayer;
                    openingAppAnimator.thumbnailAnimation = anim;
                    this.mService.mAppTransition.getNextAppTransitionStartRect(taskId, this.mTmpStartRect);
                } catch (Throwable e) {
                    Slog.e(TAG, "Can't allocate thumbnail/Canvas surface w=" + dirty.width() + " h=" + dirty.height(), e);
                    openingAppAnimator.clearThumbnail();
                }
            }
        }
    }

    boolean copyAnimToLayoutParamsLocked() {
        boolean doRequest = IS_TABLET;
        int bulkUpdateParams = this.mService.mAnimator.mBulkUpdateParams;
        if ((bulkUpdateParams & SET_UPDATE_ROTATION) != 0) {
            this.mUpdateRotation = true;
            doRequest = true;
        }
        if ((bulkUpdateParams & SET_WALLPAPER_MAY_CHANGE) != 0) {
            this.mWallpaperMayChange = true;
            doRequest = true;
        }
        if ((bulkUpdateParams & SET_FORCE_HIDING_CHANGED) != 0) {
            this.mWallpaperForceHidingChanged = true;
            doRequest = true;
        }
        if ((bulkUpdateParams & SET_ORIENTATION_CHANGE_COMPLETE) == 0) {
            this.mOrientationChangeComplete = IS_TABLET;
        } else {
            this.mOrientationChangeComplete = true;
            this.mLastWindowFreezeSource = this.mService.mAnimator.mLastWindowFreezeSource;
            if (this.mService.mWindowsFreezingScreen != 0) {
                doRequest = true;
            }
        }
        if ((bulkUpdateParams & SET_TURN_ON_SCREEN) != 0) {
            this.mService.mTurnOnScreen = true;
        }
        if ((bulkUpdateParams & SET_WALLPAPER_ACTION_PENDING) != 0) {
            this.mWallpaperActionPending = true;
        }
        return doRequest;
    }

    void requestTraversal() {
        if (!this.mTraversalScheduled) {
            this.mTraversalScheduled = true;
            this.mService.mH.sendEmptyMessage(SET_FORCE_HIDING_CHANGED);
        }
    }

    void destroyAfterTransaction(SurfaceControl surface) {
        this.mPendingDestroyingSurfaces.add(surface);
    }

    void destroyPendingSurfaces() {
        for (int i = this.mPendingDestroyingSurfaces.size() - 1; i >= 0; i--) {
            ((SurfaceControl) this.mPendingDestroyingSurfaces.get(i)).destroy();
        }
        this.mPendingDestroyingSurfaces.clear();
    }

    public void dump(PrintWriter pw, String prefix) {
        pw.print(prefix);
        pw.print("mTraversalScheduled=");
        pw.println(this.mTraversalScheduled);
        pw.print(prefix);
        pw.print("mHoldScreenWindow=");
        pw.println(this.mHoldScreenWindow);
        pw.print(prefix);
        pw.print("mObsuringWindow=");
        pw.println(this.mObsuringWindow);
    }

    private void sendBrightnessToMonitor(float brightness, String packageName) {
        if (this.mDisplayEffectMonitor != null && packageName != null) {
            if (((double) Math.abs(brightness - this.mAppBrightnessLast)) > 1.0E-7d || !this.mAppBrightnessPackageNameLast.equals(packageName)) {
                ArrayMap<String, Object> params = new ArrayMap();
                params.put("paramType", "windowManagerBrightness");
                params.put("brightness", Integer.valueOf(toBrightnessOverride(brightness)));
                params.put("packageName", packageName);
                this.mDisplayEffectMonitor.sendMonitorParam(params);
                this.mAppBrightnessLast = brightness;
                this.mAppBrightnessPackageNameLast = packageName;
            }
        }
    }
}
