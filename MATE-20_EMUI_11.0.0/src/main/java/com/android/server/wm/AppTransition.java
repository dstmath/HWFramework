package com.android.server.wm;

import android.app.ActivityManager;
import android.content.Context;
import android.content.res.ResourceId;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.GraphicBuffer;
import android.graphics.Path;
import android.graphics.Picture;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Binder;
import android.os.Bundle;
import android.os.Debug;
import android.os.Handler;
import android.os.IBinder;
import android.os.IRemoteCallback;
import android.os.RemoteException;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.util.ArraySet;
import android.util.Flog;
import android.util.Slog;
import android.util.SparseArray;
import android.util.proto.ProtoOutputStream;
import android.view.AppTransitionAnimationSpec;
import android.view.IAppTransitionAnimationSpecsFuture;
import android.view.RemoteAnimationAdapter;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.AnimationUtils;
import android.view.animation.ClipRectAnimation;
import android.view.animation.Interpolator;
import android.view.animation.PathInterpolator;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;
import com.android.internal.R;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.util.DumpUtils;
import com.android.internal.util.function.pooled.PooledLambda;
import com.android.server.AttributeCache;
import com.android.server.HwServiceFactory;
import com.android.server.policy.WindowManagerPolicy;
import com.android.server.wm.WindowManagerInternal;
import com.android.server.wm.WindowManagerService;
import com.android.server.wm.animation.ClipRectLRAnimation;
import com.android.server.wm.animation.ClipRectTBAnimation;
import com.android.server.wm.animation.CurvedTranslateAnimation;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AppTransition implements DumpUtils.Dump {
    private static final int ACCRUACY_SCALE = 2;
    private static final int APP_STATE_IDLE = 0;
    private static final int APP_STATE_READY = 1;
    private static final int APP_STATE_RUNNING = 2;
    private static final int APP_STATE_TIMEOUT = 3;
    private static final long APP_TRANSITION_TIMEOUT_MS = 5000;
    private static final int CLIP_REVEAL_TRANSLATION_Y_DP = 8;
    static final int DEFAULT_APP_TRANSITION_DURATION = 200;
    private static final float GREAT_SCREEN_DURATION_PARA = 1.3f;
    private static final boolean IS_APP_LOW_PERF_ANIM = SystemProperties.getBoolean("ro.feature.appstart.low_perf_anim", false);
    private static final boolean IS_EMUI_LITE = SystemProperties.getBoolean("ro.build.hw_emui_lite.enable", false);
    private static final int LITE_ANIMATION_DURATION = 250;
    private static final int LOW_PERF_ANIM_DURATION = 150;
    private static final int MAX_CLIP_REVEAL_TRANSITION_DURATION = 420;
    private static final int NEXT_TRANSIT_TYPE_CLIP_REVEAL = 8;
    private static final int NEXT_TRANSIT_TYPE_CUSTOM = 1;
    private static final int NEXT_TRANSIT_TYPE_CUSTOM_IN_PLACE = 7;
    private static final int NEXT_TRANSIT_TYPE_NONE = 0;
    private static final int NEXT_TRANSIT_TYPE_OPEN_CROSS_PROFILE_APPS = 9;
    private static final int NEXT_TRANSIT_TYPE_REMOTE = 10;
    private static final int NEXT_TRANSIT_TYPE_SCALE_UP = 2;
    private static final int NEXT_TRANSIT_TYPE_THUMBNAIL_ASPECT_SCALE_DOWN = 6;
    private static final int NEXT_TRANSIT_TYPE_THUMBNAIL_ASPECT_SCALE_UP = 5;
    private static final int NEXT_TRANSIT_TYPE_THUMBNAIL_SCALE_DOWN = 4;
    private static final int NEXT_TRANSIT_TYPE_THUMBNAIL_SCALE_UP = 3;
    private static final float RECENTS_THUMBNAIL_FADEIN_FRACTION = 0.5f;
    private static final float RECENTS_THUMBNAIL_FADEOUT_FRACTION = 0.5f;
    private static final double SCREEN_SIZE_8_INCH = 8.0d;
    private static final int SQUARE_METRE = 2;
    private static final String TAG = "WindowManager";
    private static final int THUMBNAIL_APP_TRANSITION_DURATION = 200;
    private static final Interpolator THUMBNAIL_DOCK_INTERPOLATOR = new PathInterpolator(0.85f, 0.0f, 1.0f, 1.0f);
    private static final int THUMBNAIL_TRANSITION_ENTER_SCALE_DOWN = 2;
    private static final int THUMBNAIL_TRANSITION_ENTER_SCALE_UP = 0;
    private static final int THUMBNAIL_TRANSITION_EXIT_SCALE_DOWN = 3;
    private static final int THUMBNAIL_TRANSITION_EXIT_SCALE_UP = 1;
    static final Interpolator TOUCH_RESPONSE_INTERPOLATOR = new PathInterpolator(0.3f, 0.0f, 0.1f, 1.0f);
    private IRemoteCallback mAnimationFinishedCallback;
    private int mAppTransitionState = 0;
    private final Interpolator mClipHorizontalInterpolator = new PathInterpolator(0.0f, 0.0f, 0.4f, 1.0f);
    private final int mClipRevealTranslationY;
    private final int mConfigShortAnimTime;
    private final Context mContext;
    private final Interpolator mCubicBezierInterpolatorType_20_80;
    private int mCurrentUserId = 0;
    private final Interpolator mDecelerateInterpolator;
    private final ExecutorService mDefaultExecutor = Executors.newSingleThreadExecutor();
    private AppTransitionAnimationSpec mDefaultNextAppTransitionAnimationSpec;
    private final int mDefaultWindowAnimationStyleResId;
    private double mDeviceSize = 0.0d;
    private final DisplayContent mDisplayContent;
    private final Interpolator mFastOutLinearInInterpolator;
    private final Interpolator mFastOutSlowInInterpolator;
    private final boolean mGridLayoutRecentsEnabled;
    final Runnable mHandleAppTransitionTimeoutRunnable = new Runnable() {
        /* class com.android.server.wm.$$Lambda$AppTransition$xrqGwel_FcpfDvO2DrCfGN_3bk */

        @Override // java.lang.Runnable
        public final void run() {
            AppTransition.this.lambda$new$0$AppTransition();
        }
    };
    final Handler mHandler;
    private String mLastChangingApp;
    private int mLastClipRevealMaxTranslation;
    private long mLastClipRevealTransitionDuration = 200;
    private String mLastClosingApp;
    private boolean mLastHadClipReveal;
    private String mLastOpeningApp;
    private int mLastUsedAppTransition = -1;
    private final Interpolator mLinearOutSlowInInterpolator;
    private final ArrayList<WindowManagerInternal.AppTransitionListener> mListeners = new ArrayList<>();
    private final boolean mLowRamRecentsEnabled;
    private int mNextAppTransition = -1;
    private final SparseArray<AppTransitionAnimationSpec> mNextAppTransitionAnimationsSpecs = new SparseArray<>();
    private IAppTransitionAnimationSpecsFuture mNextAppTransitionAnimationsSpecsFuture;
    private boolean mNextAppTransitionAnimationsSpecsPending;
    private IRemoteCallback mNextAppTransitionCallback;
    private int mNextAppTransitionEnter;
    private int mNextAppTransitionExit;
    private int mNextAppTransitionFlags = 0;
    private IRemoteCallback mNextAppTransitionFutureCallback;
    private int mNextAppTransitionInPlace;
    private Rect mNextAppTransitionInsets = new Rect();
    private String mNextAppTransitionPackage;
    private boolean mNextAppTransitionScaleUp;
    private int mNextAppTransitionType = 0;
    private RemoteAnimationController mRemoteAnimationController;
    private final WindowManagerService mService;
    private final Interpolator mThumbnailFadeInInterpolator;
    private final Interpolator mThumbnailFadeOutInterpolator;
    private Rect mTmpFromClipRect = new Rect();
    private final Rect mTmpRect = new Rect();
    private Rect mTmpToClipRect = new Rect();

    public AppTransition(Context context, WindowManagerService service, DisplayContent displayContent) {
        this.mContext = context;
        this.mService = service;
        this.mHandler = new Handler(service.mH.getLooper());
        this.mDisplayContent = displayContent;
        this.mLinearOutSlowInInterpolator = AnimationUtils.loadInterpolator(context, 17563662);
        this.mFastOutLinearInInterpolator = AnimationUtils.loadInterpolator(context, 17563663);
        this.mFastOutSlowInInterpolator = AnimationUtils.loadInterpolator(context, 17563661);
        this.mCubicBezierInterpolatorType_20_80 = AnimationUtils.loadInterpolator(context, 34078893);
        this.mConfigShortAnimTime = context.getResources().getInteger(17694720);
        this.mDecelerateInterpolator = AnimationUtils.loadInterpolator(context, 17563651);
        this.mThumbnailFadeInInterpolator = new Interpolator() {
            /* class com.android.server.wm.AppTransition.AnonymousClass1 */

            @Override // android.animation.TimeInterpolator
            public float getInterpolation(float input) {
                if (input < 0.5f) {
                    return 0.0f;
                }
                return AppTransition.this.mFastOutLinearInInterpolator.getInterpolation((input - 0.5f) / 0.5f);
            }
        };
        this.mThumbnailFadeOutInterpolator = new Interpolator() {
            /* class com.android.server.wm.AppTransition.AnonymousClass2 */

            @Override // android.animation.TimeInterpolator
            public float getInterpolation(float input) {
                if (input >= 0.5f) {
                    return 1.0f;
                }
                return AppTransition.this.mLinearOutSlowInInterpolator.getInterpolation(input / 0.5f);
            }
        };
        this.mClipRevealTranslationY = (int) (this.mContext.getResources().getDisplayMetrics().density * 8.0f);
        this.mGridLayoutRecentsEnabled = SystemProperties.getBoolean("ro.recents.grid", false);
        this.mLowRamRecentsEnabled = ActivityManager.isLowRamDeviceStatic();
        TypedArray windowStyle = this.mContext.getTheme().obtainStyledAttributes(R.styleable.Window);
        this.mDefaultWindowAnimationStyleResId = windowStyle.getResourceId(8, 0);
        windowStyle.recycle();
    }

    /* access modifiers changed from: package-private */
    public boolean isTransitionSet() {
        return this.mNextAppTransition != -1;
    }

    /* access modifiers changed from: package-private */
    public boolean isTransitionEqual(int transit) {
        return this.mNextAppTransition == transit;
    }

    /* access modifiers changed from: package-private */
    public int getAppTransition() {
        return this.mNextAppTransition;
    }

    private void setAppTransition(int transit, int flags) {
        if (WindowManagerDebugConfig.DEBUG_APP_TRANSITIONS) {
            Slog.v(TAG, "Set app transition from " + appTransitionToString(this.mNextAppTransition) + " to " + appTransitionToString(transit) + ",flags=" + flags, new Throwable());
        }
        if (transit != this.mNextAppTransition) {
            Flog.i(310, "set app transition from " + appTransitionToString(this.mNextAppTransition) + " to " + appTransitionToString(transit));
        }
        this.mNextAppTransition = transit;
        this.mNextAppTransitionFlags |= flags;
        setLastAppTransition(-1, null, null, null);
        updateBooster();
    }

    /* access modifiers changed from: package-private */
    public void setLastAppTransition(int transit, AppWindowToken openingApp, AppWindowToken closingApp, AppWindowToken changingApp) {
        this.mLastUsedAppTransition = transit;
        this.mLastOpeningApp = "" + openingApp;
        this.mLastClosingApp = "" + closingApp;
        this.mLastChangingApp = "" + changingApp;
    }

    /* access modifiers changed from: package-private */
    public boolean isReady() {
        int i = this.mAppTransitionState;
        return i == 1 || i == 3;
    }

    /* access modifiers changed from: package-private */
    public void setReady() {
        setAppTransitionState(1);
        fetchAppTransitionSpecsFromFuture();
    }

    /* access modifiers changed from: package-private */
    public boolean isRunning() {
        return this.mAppTransitionState == 2;
    }

    /* access modifiers changed from: package-private */
    public void setIdle() {
        setAppTransitionState(0);
    }

    /* access modifiers changed from: package-private */
    public boolean isTimeout() {
        return this.mAppTransitionState == 3;
    }

    /* access modifiers changed from: package-private */
    public void setTimeout() {
        setAppTransitionState(3);
    }

    /* access modifiers changed from: package-private */
    public GraphicBuffer getAppTransitionThumbnailHeader(int taskId) {
        AppTransitionAnimationSpec spec = this.mNextAppTransitionAnimationsSpecs.get(taskId);
        if (spec == null) {
            spec = this.mDefaultNextAppTransitionAnimationSpec;
        }
        if (spec != null) {
            return spec.buffer;
        }
        return null;
    }

    /* access modifiers changed from: package-private */
    public boolean isNextThumbnailTransitionAspectScaled() {
        int i = this.mNextAppTransitionType;
        return i == 5 || i == NEXT_TRANSIT_TYPE_THUMBNAIL_ASPECT_SCALE_DOWN;
    }

    /* access modifiers changed from: package-private */
    public boolean isNextThumbnailTransitionScaleUp() {
        return this.mNextAppTransitionScaleUp;
    }

    /* access modifiers changed from: package-private */
    public boolean isNextAppTransitionThumbnailUp() {
        int i = this.mNextAppTransitionType;
        return i == 3 || i == 5;
    }

    /* access modifiers changed from: package-private */
    public boolean isNextAppTransitionThumbnailDown() {
        int i = this.mNextAppTransitionType;
        return i == 4 || i == NEXT_TRANSIT_TYPE_THUMBNAIL_ASPECT_SCALE_DOWN;
    }

    /* access modifiers changed from: package-private */
    public boolean isNextAppTransitionOpenCrossProfileApps() {
        return this.mNextAppTransitionType == NEXT_TRANSIT_TYPE_OPEN_CROSS_PROFILE_APPS;
    }

    /* access modifiers changed from: package-private */
    public boolean isFetchingAppTransitionsSpecs() {
        return this.mNextAppTransitionAnimationsSpecsPending;
    }

    private boolean prepare() {
        if (isRunning()) {
            return false;
        }
        setAppTransitionState(0);
        notifyAppTransitionPendingLocked();
        this.mLastHadClipReveal = false;
        this.mLastClipRevealMaxTranslation = 0;
        this.mLastClipRevealTransitionDuration = 200;
        return true;
    }

    /* access modifiers changed from: package-private */
    public int goodToGo(int transit, AppWindowToken topOpeningApp, ArraySet<AppWindowToken> openingApps) {
        AnimationAdapter topOpeningAnim;
        long j;
        this.mNextAppTransition = -1;
        this.mNextAppTransitionFlags = 0;
        setAppTransitionState(2);
        if (topOpeningApp != null) {
            topOpeningAnim = topOpeningApp.getAnimation();
        } else {
            topOpeningAnim = null;
        }
        long durationHint = topOpeningAnim != null ? topOpeningAnim.getDurationHint() : 0;
        if (topOpeningAnim != null) {
            j = topOpeningAnim.getStatusBarTransitionsStartTime();
        } else {
            j = SystemClock.uptimeMillis();
        }
        int redoLayout = notifyAppTransitionStartingLocked(transit, durationHint, j, 120);
        this.mDisplayContent.getDockedDividerController().notifyAppTransitionStarting(openingApps, transit);
        RemoteAnimationController remoteAnimationController = this.mRemoteAnimationController;
        if (remoteAnimationController != null) {
            remoteAnimationController.goodToGo();
        }
        return redoLayout;
    }

    /* access modifiers changed from: package-private */
    public void clear() {
        this.mNextAppTransitionType = 0;
        this.mNextAppTransitionPackage = null;
        this.mNextAppTransitionAnimationsSpecs.clear();
        this.mRemoteAnimationController = null;
        this.mNextAppTransitionAnimationsSpecsFuture = null;
        this.mDefaultNextAppTransitionAnimationSpec = null;
        this.mAnimationFinishedCallback = null;
    }

    /* access modifiers changed from: package-private */
    public void freeze() {
        int transit = this.mNextAppTransition;
        RemoteAnimationController remoteAnimationController = this.mRemoteAnimationController;
        if (remoteAnimationController != null) {
            remoteAnimationController.cancelAnimation("freeze");
        }
        setAppTransition(-1, 0);
        clear();
        setReady();
        notifyAppTransitionCancelledLocked(transit);
    }

    private void setAppTransitionState(int state) {
        this.mAppTransitionState = state;
        updateBooster();
    }

    /* access modifiers changed from: package-private */
    public void updateBooster() {
        WindowManagerService.sThreadPriorityBooster.setAppTransitionRunning(needsBoosting());
    }

    private boolean needsBoosting() {
        int i;
        return this.mNextAppTransition != -1 || (i = this.mAppTransitionState) == 1 || i == 2 || (this.mService.getRecentsAnimationController() != null);
    }

    /* access modifiers changed from: package-private */
    public void registerListenerLocked(WindowManagerInternal.AppTransitionListener listener) {
        this.mListeners.add(listener);
    }

    /* access modifiers changed from: package-private */
    public void unregisterListener(WindowManagerInternal.AppTransitionListener listener) {
        this.mListeners.remove(listener);
    }

    public void notifyAppTransitionFinishedLocked(IBinder token) {
        for (int i = 0; i < this.mListeners.size(); i++) {
            this.mListeners.get(i).onAppTransitionFinishedLocked(token);
        }
    }

    private void notifyAppTransitionPendingLocked() {
        for (int i = 0; i < this.mListeners.size(); i++) {
            this.mListeners.get(i).onAppTransitionPendingLocked();
        }
    }

    private void notifyAppTransitionCancelledLocked(int transit) {
        for (int i = 0; i < this.mListeners.size(); i++) {
            this.mListeners.get(i).onAppTransitionCancelledLocked(transit);
        }
    }

    private int notifyAppTransitionStartingLocked(int transit, long duration, long statusBarAnimationStartTime, long statusBarAnimationDuration) {
        int redoLayout = 0;
        for (int i = 0; i < this.mListeners.size(); i++) {
            redoLayout |= this.mListeners.get(i).onAppTransitionStartingLocked(transit, duration, statusBarAnimationStartTime, statusBarAnimationDuration);
        }
        return redoLayout;
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public int getDefaultWindowAnimationStyleResId() {
        return this.mDefaultWindowAnimationStyleResId;
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public int getAnimationStyleResId(WindowManager.LayoutParams lp) {
        int resId = lp.windowAnimations;
        if (lp.type == 3) {
            return this.mDefaultWindowAnimationStyleResId;
        }
        return resId;
    }

    private AttributeCache.Entry getCachedAnimations(WindowManager.LayoutParams lp) {
        if (WindowManagerDebugConfig.DEBUG_ANIM) {
            StringBuilder sb = new StringBuilder();
            sb.append("Loading animations: layout params pkg=");
            sb.append(lp != null ? lp.packageName : null);
            sb.append(" resId=0x");
            sb.append(lp != null ? Integer.toHexString(lp.windowAnimations) : null);
            Slog.v(TAG, sb.toString());
        }
        if (lp == null || lp.windowAnimations == 0) {
            return null;
        }
        String packageName = lp.packageName != null ? lp.packageName : "android";
        int resId = getAnimationStyleResId(lp);
        if ((-16777216 & resId) == 16777216) {
            packageName = "android";
        }
        if (WindowManagerDebugConfig.DEBUG_ANIM) {
            Slog.v(TAG, "Loading animations: picked package=" + packageName);
        }
        return AttributeCache.instance().get(packageName, resId, R.styleable.WindowAnimation, this.mCurrentUserId);
    }

    /* access modifiers changed from: protected */
    public AttributeCache.Entry getCachedAnimations(String packageName, int resId) {
        if (WindowManagerDebugConfig.DEBUG_ANIM) {
            Slog.v(TAG, "Loading animations: package=" + packageName + " resId=0x" + Integer.toHexString(resId));
        }
        if (packageName == null) {
            return null;
        }
        if ((-16777216 & resId) == 16777216) {
            packageName = "android";
        }
        if (WindowManagerDebugConfig.DEBUG_ANIM) {
            Slog.v(TAG, "Loading animations: picked package=" + packageName);
        }
        return AttributeCache.instance().get(packageName, resId, R.styleable.WindowAnimation, this.mCurrentUserId);
    }

    /* access modifiers changed from: package-private */
    public Animation loadAnimationAttr(WindowManager.LayoutParams lp, int animAttr, int transit) {
        int resId = 0;
        Context context = this.mContext;
        if (animAttr >= 0) {
            AttributeCache.Entry ent = getCachedAnimations(lp);
            if (ent != null) {
                context = ent.context;
                resId = ent.array.getResourceId(animAttr, 0);
            }
            AttributeCache.Entry ent2 = HwServiceFactory.getHwAppTransition().overrideAnimation(lp, animAttr, this.mContext, ent, this);
            if (ent2 != null) {
                context = ent2.context;
                resId = ent2.array.getResourceId(animAttr, 0);
            }
        }
        int resId2 = updateToTranslucentAnimIfNeeded(resId, transit);
        if (ResourceId.isValid(resId2)) {
            return loadAnimationSafely(context, resId2);
        }
        return null;
    }

    /* access modifiers changed from: package-private */
    public Animation loadAnimationRes(WindowManager.LayoutParams lp, int resId) {
        Context context = this.mContext;
        if (!ResourceId.isValid(resId)) {
            return null;
        }
        AttributeCache.Entry ent = getCachedAnimations(lp);
        if (ent != null) {
            context = ent.context;
        }
        return loadAnimationSafely(context, resId);
    }

    private Animation loadAnimationRes(String packageName, int resId) {
        AttributeCache.Entry ent;
        if (!ResourceId.isValid(resId) || (ent = getCachedAnimations(packageName, resId)) == null) {
            return null;
        }
        return loadAnimationSafely(ent.context, resId);
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public Animation loadAnimationSafely(Context context, int resId) {
        try {
            return AnimationUtils.loadAnimation(context, resId);
        } catch (Resources.NotFoundException e) {
            Slog.w(TAG, "Unable to load animation resource", e);
            return null;
        } catch (RuntimeException e2) {
            Slog.w(TAG, "Unable to load animation resource due to unknow animation name.");
            return null;
        }
    }

    private int updateToTranslucentAnimIfNeeded(int anim, int transit) {
        if (transit == 24 && (anim == 17432726 || anim == 34209800)) {
            return 17432729;
        }
        if (transit == 25 && (anim == 17432725 || anim == 34209805)) {
            return 17432728;
        }
        return anim;
    }

    private static float computePivot(int startPos, float finalScale) {
        float denom = finalScale - 1.0f;
        if (Math.abs(denom) < 1.0E-4f) {
            return (float) startPos;
        }
        return ((float) (-startPos)) / denom;
    }

    /* JADX DEBUG: Multi-variable search result rejected for r6v12, resolved type: android.view.animation.AnimationSet */
    /* JADX WARN: Multi-variable type inference failed */
    private Animation createScaleUpAnimationLocked(int transit, boolean enter, Rect containingFrame) {
        Animation alpha;
        long duration;
        getDefaultNextAppTransitionStartRect(this.mTmpRect);
        int appWidth = containingFrame.width();
        int appHeight = containingFrame.height();
        if (enter) {
            float scaleW = ((float) this.mTmpRect.width()) / ((float) appWidth);
            float scaleH = ((float) this.mTmpRect.height()) / ((float) appHeight);
            Animation scale = new ScaleAnimation(scaleW, 1.0f, scaleH, 1.0f, computePivot(this.mTmpRect.left, scaleW), computePivot(this.mTmpRect.top, scaleH));
            scale.setInterpolator(this.mCubicBezierInterpolatorType_20_80);
            Animation alpha2 = new AlphaAnimation(0.0f, 1.0f);
            alpha2.setInterpolator(this.mCubicBezierInterpolatorType_20_80);
            AnimationSet set = new AnimationSet(false);
            set.addAnimation(scale);
            if (!IS_APP_LOW_PERF_ANIM) {
                set.addAnimation(alpha2);
            }
            set.setDetachWallpaper(true);
            alpha = set;
        } else if (transit == 14 || transit == 15) {
            alpha = new AlphaAnimation(1.0f, 0.0f);
            alpha.setDetachWallpaper(true);
        } else {
            alpha = new AlphaAnimation(1.0f, 1.0f);
        }
        if (transit == NEXT_TRANSIT_TYPE_THUMBNAIL_ASPECT_SCALE_DOWN || transit == NEXT_TRANSIT_TYPE_CUSTOM_IN_PLACE) {
            duration = (long) this.mConfigShortAnimTime;
        } else {
            duration = 200;
        }
        if (IS_APP_LOW_PERF_ANIM && transit == 12) {
            duration = 150;
        }
        if (IS_EMUI_LITE) {
            duration = 250;
        }
        alpha.setDuration(duration);
        alpha.setFillAfter(true);
        alpha.setInterpolator(this.mCubicBezierInterpolatorType_20_80);
        alpha.initialize(appWidth, appHeight, appWidth, appHeight);
        return alpha;
    }

    private void getDefaultNextAppTransitionStartRect(Rect rect) {
        AppTransitionAnimationSpec appTransitionAnimationSpec = this.mDefaultNextAppTransitionAnimationSpec;
        if (appTransitionAnimationSpec == null || appTransitionAnimationSpec.rect == null) {
            Slog.e(TAG, "Starting rect for app requested, but none available", new Throwable());
            rect.setEmpty();
            return;
        }
        rect.set(this.mDefaultNextAppTransitionAnimationSpec.rect);
    }

    /* access modifiers changed from: package-private */
    public void getNextAppTransitionStartRect(int taskId, Rect rect) {
        AppTransitionAnimationSpec spec = this.mNextAppTransitionAnimationsSpecs.get(taskId);
        if (spec == null) {
            spec = this.mDefaultNextAppTransitionAnimationSpec;
        }
        if (spec == null || spec.rect == null) {
            Slog.e(TAG, "Starting rect for task: " + taskId + " requested, but not available", new Throwable());
            rect.setEmpty();
            return;
        }
        rect.set(spec.rect);
    }

    private void putDefaultNextAppTransitionCoordinates(int left, int top, int width, int height, GraphicBuffer buffer) {
        this.mDefaultNextAppTransitionAnimationSpec = new AppTransitionAnimationSpec(-1, buffer, new Rect(left, top, left + width, top + height));
    }

    /* access modifiers changed from: package-private */
    public long getLastClipRevealTransitionDuration() {
        return this.mLastClipRevealTransitionDuration;
    }

    /* access modifiers changed from: package-private */
    public int getLastClipRevealMaxTranslation() {
        return this.mLastClipRevealMaxTranslation;
    }

    /* access modifiers changed from: package-private */
    public boolean hadClipRevealAnimation() {
        return this.mLastHadClipReveal;
    }

    private long calculateClipRevealTransitionDuration(boolean cutOff, float translationX, float translationY, Rect displayFrame) {
        if (!cutOff) {
            return 200;
        }
        return (long) ((220.0f * Math.max(Math.abs(translationX) / ((float) displayFrame.width()), Math.abs(translationY) / ((float) displayFrame.height()))) + 200.0f);
    }

    /* JADX DEBUG: Multi-variable search result rejected for r12v8, resolved type: android.view.animation.AnimationSet */
    /* JADX WARN: Multi-variable type inference failed */
    /* JADX WARN: Type inference failed for: r7v7, types: [com.android.server.wm.animation.ClipRectLRAnimation, android.view.animation.Animation] */
    /* JADX WARN: Type inference failed for: r5v4, types: [com.android.server.wm.animation.ClipRectTBAnimation, android.view.animation.Animation] */
    /* JADX WARNING: Unknown variable types count: 2 */
    private Animation createClipRevealAnimationLocked(int transit, boolean enter, Rect appFrame, Rect displayFrame) {
        long duration;
        boolean z;
        Animation anim;
        float f;
        float t;
        int translationYCorrection;
        int clipStartY;
        int translationY;
        boolean cutOff;
        int translationX;
        int clipStartX;
        Interpolator interpolator;
        if (enter) {
            int appWidth = appFrame.width();
            int appHeight = appFrame.height();
            getDefaultNextAppTransitionStartRect(this.mTmpRect);
            if (appHeight > 0) {
                t = ((float) this.mTmpRect.top) / ((float) displayFrame.height());
            } else {
                t = 0.0f;
            }
            int translationY2 = this.mClipRevealTranslationY + ((int) ((((float) displayFrame.height()) / 7.0f) * t));
            int translationX2 = 0;
            int centerX = this.mTmpRect.centerX();
            int centerY = this.mTmpRect.centerY();
            int halfWidth = this.mTmpRect.width() / 2;
            int halfHeight = this.mTmpRect.height() / 2;
            int clipStartX2 = (centerX - halfWidth) - appFrame.left;
            int clipStartY2 = (centerY - halfHeight) - appFrame.top;
            boolean cutOff2 = false;
            if (appFrame.top > centerY - halfHeight) {
                cutOff2 = true;
                translationY = (centerY - halfHeight) - appFrame.top;
                translationYCorrection = 0;
                clipStartY = 0;
            } else {
                translationY = translationY2;
                translationYCorrection = translationY2;
                clipStartY = clipStartY2;
            }
            if (appFrame.left > centerX - halfWidth) {
                translationX2 = (centerX - halfWidth) - appFrame.left;
                clipStartX2 = 0;
                cutOff2 = true;
            }
            if (appFrame.right < centerX + halfWidth) {
                int translationX3 = (centerX + halfWidth) - appFrame.right;
                clipStartX = appWidth - this.mTmpRect.width();
                cutOff = true;
                translationX = translationX3;
            } else {
                clipStartX = clipStartX2;
                cutOff = cutOff2;
                translationX = translationX2;
            }
            long duration2 = calculateClipRevealTransitionDuration(cutOff, (float) translationX, (float) translationY, displayFrame);
            ?? clipRectLRAnimation = new ClipRectLRAnimation(clipStartX, this.mTmpRect.width() + clipStartX, 0, appWidth);
            clipRectLRAnimation.setInterpolator(this.mClipHorizontalInterpolator);
            clipRectLRAnimation.setDuration((long) (((float) duration2) / 2.5f));
            TranslateAnimation translate = new TranslateAnimation((float) translationX, 0.0f, (float) translationY, 0.0f);
            if (cutOff) {
                interpolator = TOUCH_RESPONSE_INTERPOLATOR;
            } else {
                interpolator = this.mLinearOutSlowInInterpolator;
            }
            translate.setInterpolator(interpolator);
            translate.setDuration(duration2);
            ?? clipRectTBAnimation = new ClipRectTBAnimation(clipStartY, clipStartY + this.mTmpRect.height(), 0, appHeight, translationYCorrection, 0, this.mLinearOutSlowInInterpolator);
            clipRectTBAnimation.setInterpolator(TOUCH_RESPONSE_INTERPOLATOR);
            clipRectTBAnimation.setDuration(duration2);
            AlphaAnimation alpha = new AlphaAnimation(0.5f, 1.0f);
            alpha.setDuration(duration2 / 4);
            alpha.setInterpolator(this.mLinearOutSlowInInterpolator);
            AnimationSet set = new AnimationSet(false);
            set.addAnimation(clipRectLRAnimation);
            set.addAnimation(clipRectTBAnimation);
            set.addAnimation(translate);
            set.addAnimation(alpha);
            set.setZAdjustment(1);
            set.initialize(appWidth, appHeight, appWidth, appHeight);
            this.mLastHadClipReveal = true;
            this.mLastClipRevealTransitionDuration = duration2;
            this.mLastClipRevealMaxTranslation = cutOff ? Math.max(Math.abs(translationY), Math.abs(translationX)) : 0;
            return set;
        }
        if (transit == NEXT_TRANSIT_TYPE_THUMBNAIL_ASPECT_SCALE_DOWN || transit == NEXT_TRANSIT_TYPE_CUSTOM_IN_PLACE) {
            duration = (long) this.mConfigShortAnimTime;
        } else {
            duration = 200;
        }
        if (transit == 14) {
            f = 1.0f;
        } else if (transit == 15) {
            f = 1.0f;
        } else {
            anim = new AlphaAnimation(1.0f, 1.0f);
            z = true;
            anim.setInterpolator(this.mDecelerateInterpolator);
            anim.setDuration(duration);
            anim.setFillAfter(z);
            return anim;
        }
        anim = new AlphaAnimation(f, 0.0f);
        z = true;
        anim.setDetachWallpaper(true);
        anim.setInterpolator(this.mDecelerateInterpolator);
        anim.setDuration(duration);
        anim.setFillAfter(z);
        return anim;
    }

    /* access modifiers changed from: package-private */
    public Animation prepareThumbnailAnimationWithDuration(Animation a, int appWidth, int appHeight, long duration, Interpolator interpolator) {
        if (duration > 0) {
            a.setDuration(duration);
        }
        a.setFillAfter(true);
        if (interpolator != null) {
            a.setInterpolator(interpolator);
        }
        a.initialize(appWidth, appHeight, appWidth, appHeight);
        return a;
    }

    /* access modifiers changed from: package-private */
    public Animation prepareThumbnailAnimation(Animation a, int appWidth, int appHeight, int transit) {
        int duration;
        if (transit == NEXT_TRANSIT_TYPE_THUMBNAIL_ASPECT_SCALE_DOWN || transit == NEXT_TRANSIT_TYPE_CUSTOM_IN_PLACE) {
            duration = this.mConfigShortAnimTime;
        } else {
            duration = 200;
        }
        return prepareThumbnailAnimationWithDuration(a, appWidth, appHeight, (long) duration, this.mDecelerateInterpolator);
    }

    /* access modifiers changed from: package-private */
    public int getThumbnailTransitionState(boolean enter) {
        if (enter) {
            if (this.mNextAppTransitionScaleUp) {
                return 0;
            }
            return 2;
        } else if (this.mNextAppTransitionScaleUp) {
            return 1;
        } else {
            return 3;
        }
    }

    /* access modifiers changed from: package-private */
    public GraphicBuffer createCrossProfileAppsThumbnail(int thumbnailDrawableRes, Rect frame) {
        int width = frame.width();
        int height = frame.height();
        Picture picture = new Picture();
        Canvas canvas = picture.beginRecording(width, height);
        canvas.drawColor(Color.argb(0.6f, 0.0f, 0.0f, 0.0f));
        int thumbnailSize = this.mService.mContext.getResources().getDimensionPixelSize(17105085);
        Drawable drawable = this.mService.mContext.getDrawable(thumbnailDrawableRes);
        drawable.setBounds((width - thumbnailSize) / 2, (height - thumbnailSize) / 2, (width + thumbnailSize) / 2, (height + thumbnailSize) / 2);
        drawable.setTint(this.mContext.getColor(17170443));
        drawable.draw(canvas);
        picture.endRecording();
        return Bitmap.createBitmap(picture).createGraphicBufferHandle();
    }

    /* access modifiers changed from: package-private */
    public Animation createCrossProfileAppsThumbnailAnimationLocked(Rect appRect) {
        return prepareThumbnailAnimationWithDuration(loadAnimationRes("android", 17432746), appRect.width(), appRect.height(), 0, null);
    }

    /* access modifiers changed from: package-private */
    public Animation createThumbnailAspectScaleAnimationLocked(Rect appRect, Rect contentInsets, GraphicBuffer thumbnailHeader, int taskId, int uiMode, int orientation) {
        float pivotY;
        float pivotX;
        float fromY;
        float fromY2;
        float fromX;
        float pivotX2;
        int appWidth;
        Animation translate;
        float fromY3;
        long j;
        float fromY4;
        int thumbWidthI = thumbnailHeader.getWidth();
        float thumbWidth = thumbWidthI > 0 ? (float) thumbWidthI : 1.0f;
        int thumbHeightI = thumbnailHeader.getHeight();
        int appWidth2 = appRect.width();
        float scaleW = ((float) appWidth2) / thumbWidth;
        getNextAppTransitionStartRect(taskId, this.mTmpRect);
        if (shouldScaleDownThumbnailTransition(uiMode, orientation)) {
            float fromX2 = (float) this.mTmpRect.left;
            float fromY5 = (float) this.mTmpRect.top;
            fromY = (((float) (this.mTmpRect.width() / 2)) * (scaleW - 1.0f)) + ((float) appRect.left);
            float toY = (((float) (appRect.height() / 2)) * (1.0f - (1.0f / scaleW))) + ((float) appRect.top);
            float pivotX3 = (float) (this.mTmpRect.width() / 2);
            float pivotY2 = ((float) (appRect.height() / 2)) / scaleW;
            if (this.mGridLayoutRecentsEnabled) {
                pivotX2 = fromY5 - ((float) thumbHeightI);
                pivotX = pivotX3;
                pivotY = pivotY2;
                fromY2 = fromX2;
                fromX = toY - (((float) thumbHeightI) * scaleW);
            } else {
                pivotX2 = fromY5;
                pivotX = pivotX3;
                pivotY = pivotY2;
                fromY2 = fromX2;
                fromX = toY;
            }
        } else {
            fromY2 = (float) this.mTmpRect.left;
            pivotX = 0.0f;
            pivotY = 0.0f;
            pivotX2 = (float) this.mTmpRect.top;
            fromY = (float) appRect.left;
            fromX = (float) appRect.top;
        }
        long duration = getAspectScaleDuration();
        Interpolator interpolator = getAspectScaleInterpolator();
        if (this.mNextAppTransitionScaleUp) {
            Animation scale = new ScaleAnimation(1.0f, scaleW, 1.0f, scaleW, pivotX, pivotY);
            scale.setInterpolator(interpolator);
            scale.setDuration(duration);
            appWidth = appWidth2;
            Animation alpha = new AlphaAnimation(1.0f, 0.0f);
            alpha.setInterpolator(this.mNextAppTransition == 19 ? THUMBNAIL_DOCK_INTERPOLATOR : this.mThumbnailFadeOutInterpolator);
            if (this.mNextAppTransition == 19) {
                j = duration / 2;
            } else {
                j = duration;
            }
            alpha.setDuration(j);
            Animation translate2 = createCurvedMotion(fromY2, fromY, pivotX2, fromX);
            translate2.setInterpolator(interpolator);
            translate2.setDuration(duration);
            this.mTmpFromClipRect.set(0, 0, thumbWidthI, thumbHeightI);
            this.mTmpToClipRect.set(appRect);
            this.mTmpToClipRect.offsetTo(0, 0);
            Rect rect = this.mTmpToClipRect;
            rect.right = (int) (((float) rect.right) / scaleW);
            Rect rect2 = this.mTmpToClipRect;
            rect2.bottom = (int) (((float) rect2.bottom) / scaleW);
            if (contentInsets != null) {
                fromY4 = pivotX2;
                this.mTmpToClipRect.inset((int) (((float) (-contentInsets.left)) * scaleW), (int) (((float) (-contentInsets.top)) * scaleW), (int) (((float) (-contentInsets.right)) * scaleW), (int) (((float) (-contentInsets.bottom)) * scaleW));
            } else {
                fromY4 = pivotX2;
            }
            Animation clipAnim = new ClipRectAnimation(this.mTmpFromClipRect, this.mTmpToClipRect);
            clipAnim.setInterpolator(interpolator);
            clipAnim.setDuration(duration);
            AnimationSet set = new AnimationSet(false);
            set.addAnimation(scale);
            if (!this.mGridLayoutRecentsEnabled) {
                set.addAnimation(alpha);
            }
            set.addAnimation(translate2);
            set.addAnimation(clipAnim);
            translate = set;
            fromY3 = fromY4;
        } else {
            appWidth = appWidth2;
            Animation scale2 = new ScaleAnimation(scaleW, 1.0f, scaleW, 1.0f, pivotX, pivotY);
            scale2.setInterpolator(interpolator);
            scale2.setDuration(duration);
            Animation alpha2 = new AlphaAnimation(0.0f, 1.0f);
            alpha2.setInterpolator(this.mThumbnailFadeInInterpolator);
            alpha2.setDuration(duration);
            fromY3 = pivotX2;
            Animation translate3 = createCurvedMotion(fromY, fromY2, fromX, fromY3);
            translate3.setInterpolator(interpolator);
            translate3.setDuration(duration);
            AnimationSet set2 = new AnimationSet(false);
            set2.addAnimation(scale2);
            if (!this.mGridLayoutRecentsEnabled) {
                set2.addAnimation(alpha2);
            }
            set2.addAnimation(translate3);
            translate = set2;
        }
        return prepareThumbnailAnimationWithDuration(translate, appWidth, appRect.height(), 0, null);
    }

    private Animation createCurvedMotion(float fromX, float toX, float fromY, float toY) {
        if (Math.abs(toX - fromX) < 1.0f || this.mNextAppTransition != 19) {
            return new TranslateAnimation(fromX, toX, fromY, toY);
        }
        return new CurvedTranslateAnimation(createCurvedPath(fromX, toX, fromY, toY));
    }

    private Path createCurvedPath(float fromX, float toX, float fromY, float toY) {
        Path path = new Path();
        path.moveTo(fromX, fromY);
        if (fromY > toY) {
            path.cubicTo(fromX, fromY, toX, (0.9f * fromY) + (0.1f * toY), toX, toY);
        } else {
            path.cubicTo(fromX, fromY, fromX, (0.1f * fromY) + (0.9f * toY), toX, toY);
        }
        return path;
    }

    private long getAspectScaleDuration() {
        if (this.mNextAppTransition == 19) {
            return 270;
        }
        return 200;
    }

    private Interpolator getAspectScaleInterpolator() {
        if (this.mNextAppTransition == 19) {
            return this.mFastOutSlowInInterpolator;
        }
        return TOUCH_RESPONSE_INTERPOLATOR;
    }

    /* JADX INFO: Multiple debug info for r1v25 float: [D('startX' float), D('scale' float)] */
    /* access modifiers changed from: package-private */
    public Animation createAspectScaledThumbnailEnterExitAnimationLocked(int thumbTransitState, int uiMode, int orientation, int transit, Rect containingFrame, Rect contentInsets, Rect surfaceInsets, Rect stableInsets, boolean freeform, int taskId) {
        int appWidth;
        Animation a;
        Animation clipAnim;
        Animation translateAnim;
        Animation clipAnim2;
        Animation translateAnim2;
        int appWidth2 = containingFrame.width();
        int appHeight = containingFrame.height();
        getDefaultNextAppTransitionStartRect(this.mTmpRect);
        int thumbWidthI = this.mTmpRect.width();
        float thumbWidth = thumbWidthI > 0 ? (float) thumbWidthI : 1.0f;
        int thumbHeightI = this.mTmpRect.height();
        float thumbHeight = thumbHeightI > 0 ? (float) thumbHeightI : 1.0f;
        int thumbStartX = (this.mTmpRect.left - containingFrame.left) - contentInsets.left;
        int thumbStartY = this.mTmpRect.top - containingFrame.top;
        if (thumbTransitState != 0) {
            if (thumbTransitState != 1) {
                if (thumbTransitState != 2) {
                    if (thumbTransitState != 3) {
                        throw new RuntimeException("Invalid thumbnail transition state");
                    }
                } else if (transit == 14) {
                    a = new AlphaAnimation(0.0f, 1.0f);
                    appWidth = appWidth2;
                } else {
                    a = new AlphaAnimation(1.0f, 1.0f);
                    appWidth = appWidth2;
                }
            } else if (transit == 14) {
                a = new AlphaAnimation(1.0f, 0.0f);
                appWidth = appWidth2;
            } else {
                a = new AlphaAnimation(1.0f, 1.0f);
                appWidth = appWidth2;
            }
            return prepareThumbnailAnimationWithDuration(a, appWidth, appHeight, getAspectScaleDuration(), getAspectScaleInterpolator());
        }
        boolean scaleUp = thumbTransitState == 0;
        if (!freeform || !scaleUp) {
            if (freeform) {
                a = createAspectScaledThumbnailExitFreeformAnimationLocked(containingFrame, surfaceInsets, taskId);
                appWidth = appWidth2;
            } else {
                AnimationSet set = new AnimationSet(true);
                this.mTmpFromClipRect.set(containingFrame);
                this.mTmpToClipRect.set(containingFrame);
                this.mTmpFromClipRect.offsetTo(0, 0);
                this.mTmpToClipRect.offsetTo(0, 0);
                this.mTmpFromClipRect.inset(contentInsets);
                this.mNextAppTransitionInsets.set(contentInsets);
                if (shouldScaleDownThumbnailTransition(uiMode, orientation)) {
                    float scale = thumbWidth / ((float) ((appWidth2 - contentInsets.left) - contentInsets.right));
                    if (!this.mGridLayoutRecentsEnabled) {
                        Rect rect = this.mTmpFromClipRect;
                        rect.bottom = rect.top + ((int) (thumbHeight / scale));
                    }
                    this.mNextAppTransitionInsets.set(contentInsets);
                    Animation scaleAnim = new ScaleAnimation(scaleUp ? scale : 1.0f, scaleUp ? 1.0f : scale, scaleUp ? scale : 1.0f, scaleUp ? 1.0f : scale, ((float) containingFrame.width()) / 2.0f, (((float) containingFrame.height()) / 2.0f) + ((float) contentInsets.top));
                    float targetX = (float) (this.mTmpRect.left - containingFrame.left);
                    float x = (((float) containingFrame.width()) / 2.0f) - ((((float) containingFrame.width()) / 2.0f) * scale);
                    float targetY = (float) (this.mTmpRect.top - containingFrame.top);
                    float y = (((float) containingFrame.height()) / 2.0f) - ((((float) containingFrame.height()) / 2.0f) * scale);
                    if (!this.mLowRamRecentsEnabled || contentInsets.top != 0 || !scaleUp) {
                        appWidth = appWidth2;
                    } else {
                        appWidth = appWidth2;
                        this.mTmpFromClipRect.top += stableInsets.top;
                        y += (float) stableInsets.top;
                    }
                    float scale2 = targetX - x;
                    float startY = targetY - y;
                    if (scaleUp) {
                        clipAnim2 = new ClipRectAnimation(this.mTmpFromClipRect, this.mTmpToClipRect);
                    } else {
                        clipAnim2 = new ClipRectAnimation(this.mTmpToClipRect, this.mTmpFromClipRect);
                    }
                    if (scaleUp) {
                        translateAnim2 = createCurvedMotion(scale2, 0.0f, startY - ((float) contentInsets.top), 0.0f);
                    } else {
                        translateAnim2 = createCurvedMotion(0.0f, scale2, 0.0f, startY - ((float) contentInsets.top));
                    }
                    set.addAnimation(clipAnim2);
                    set.addAnimation(scaleAnim);
                    set.addAnimation(translateAnim2);
                } else {
                    appWidth = appWidth2;
                    Rect rect2 = this.mTmpFromClipRect;
                    rect2.bottom = rect2.top + thumbHeightI;
                    Rect rect3 = this.mTmpFromClipRect;
                    rect3.right = rect3.left + thumbWidthI;
                    if (scaleUp) {
                        clipAnim = new ClipRectAnimation(this.mTmpFromClipRect, this.mTmpToClipRect);
                    } else {
                        clipAnim = new ClipRectAnimation(this.mTmpToClipRect, this.mTmpFromClipRect);
                    }
                    if (scaleUp) {
                        translateAnim = createCurvedMotion((float) thumbStartX, 0.0f, (float) (thumbStartY - contentInsets.top), 0.0f);
                    } else {
                        translateAnim = createCurvedMotion(0.0f, (float) thumbStartX, 0.0f, (float) (thumbStartY - contentInsets.top));
                    }
                    set.addAnimation(clipAnim);
                    set.addAnimation(translateAnim);
                }
                set.setZAdjustment(1);
                a = set;
            }
            return prepareThumbnailAnimationWithDuration(a, appWidth, appHeight, getAspectScaleDuration(), getAspectScaleInterpolator());
        }
        a = createAspectScaledThumbnailEnterFreeformAnimationLocked(containingFrame, surfaceInsets, taskId);
        appWidth = appWidth2;
        return prepareThumbnailAnimationWithDuration(a, appWidth, appHeight, getAspectScaleDuration(), getAspectScaleInterpolator());
    }

    private Animation createAspectScaledThumbnailEnterFreeformAnimationLocked(Rect frame, Rect surfaceInsets, int taskId) {
        getNextAppTransitionStartRect(taskId, this.mTmpRect);
        return createAspectScaledThumbnailFreeformAnimationLocked(this.mTmpRect, frame, surfaceInsets, true);
    }

    private Animation createAspectScaledThumbnailExitFreeformAnimationLocked(Rect frame, Rect surfaceInsets, int taskId) {
        getNextAppTransitionStartRect(taskId, this.mTmpRect);
        return createAspectScaledThumbnailFreeformAnimationLocked(frame, this.mTmpRect, surfaceInsets, false);
    }

    private AnimationSet createAspectScaledThumbnailFreeformAnimationLocked(Rect sourceFrame, Rect destFrame, Rect surfaceInsets, boolean enter) {
        ScaleAnimation scale;
        TranslateAnimation translation;
        float sourceWidth = (float) sourceFrame.width();
        float sourceHeight = (float) sourceFrame.height();
        float destWidth = (float) destFrame.width();
        float destHeight = (float) destFrame.height();
        float scaleH = enter ? sourceWidth / destWidth : destWidth / sourceWidth;
        float scaleV = enter ? sourceHeight / destHeight : destHeight / sourceHeight;
        AnimationSet set = new AnimationSet(true);
        int surfaceInsetsV = 0;
        int surfaceInsetsH = surfaceInsets == null ? 0 : surfaceInsets.left + surfaceInsets.right;
        if (surfaceInsets != null) {
            surfaceInsetsV = surfaceInsets.top + surfaceInsets.bottom;
        }
        float scaleHCenter = ((enter ? destWidth : sourceWidth) + ((float) surfaceInsetsH)) / 2.0f;
        float scaleVCenter = ((enter ? destHeight : sourceHeight) + ((float) surfaceInsetsV)) / 2.0f;
        if (enter) {
            scale = new ScaleAnimation(scaleH, 1.0f, scaleV, 1.0f, scaleHCenter, scaleVCenter);
        } else {
            scale = new ScaleAnimation(1.0f, scaleH, 1.0f, scaleV, scaleHCenter, scaleVCenter);
        }
        int sourceHCenter = sourceFrame.left + (sourceFrame.width() / 2);
        int sourceVCenter = sourceFrame.top + (sourceFrame.height() / 2);
        int destHCenter = destFrame.left + (destFrame.width() / 2);
        int destVCenter = destFrame.top + (destFrame.height() / 2);
        int fromX = enter ? sourceHCenter - destHCenter : destHCenter - sourceHCenter;
        int fromY = enter ? sourceVCenter - destVCenter : destVCenter - sourceVCenter;
        if (enter) {
            translation = new TranslateAnimation((float) fromX, 0.0f, (float) fromY, 0.0f);
        } else {
            translation = new TranslateAnimation(0.0f, (float) fromX, 0.0f, (float) fromY);
        }
        set.addAnimation(scale);
        set.addAnimation(translation);
        final IRemoteCallback callback = this.mAnimationFinishedCallback;
        if (callback != null) {
            set.setAnimationListener(new Animation.AnimationListener() {
                /* class com.android.server.wm.AppTransition.AnonymousClass3 */

                @Override // android.view.animation.Animation.AnimationListener
                public void onAnimationStart(Animation animation) {
                }

                @Override // android.view.animation.Animation.AnimationListener
                public void onAnimationEnd(Animation animation) {
                    AppTransition.this.mHandler.sendMessage(PooledLambda.obtainMessage($$Lambda$AppTransition$3$llbNiZO5SMSamZHTNM_5S77eNNU.INSTANCE, callback));
                }

                @Override // android.view.animation.Animation.AnimationListener
                public void onAnimationRepeat(Animation animation) {
                }
            });
        }
        return set;
    }

    /* JADX DEBUG: Multi-variable search result rejected for r10v6, resolved type: android.view.animation.AnimationSet */
    /* JADX WARN: Multi-variable type inference failed */
    /* access modifiers changed from: package-private */
    public Animation createThumbnailScaleAnimationLocked(int appWidth, int appHeight, int transit, GraphicBuffer thumbnailHeader) {
        Animation alpha;
        getDefaultNextAppTransitionStartRect(this.mTmpRect);
        int thumbWidthI = thumbnailHeader.getWidth();
        float thumbWidth = thumbWidthI > 0 ? (float) thumbWidthI : 1.0f;
        int thumbHeightI = thumbnailHeader.getHeight();
        float thumbHeight = thumbHeightI > 0 ? (float) thumbHeightI : 1.0f;
        if (this.mNextAppTransitionScaleUp) {
            float scaleW = ((float) appWidth) / thumbWidth;
            float scaleH = ((float) appHeight) / thumbHeight;
            Animation scale = new ScaleAnimation(1.0f, scaleW, 1.0f, scaleH, computePivot(this.mTmpRect.left, 1.0f / scaleW), computePivot(this.mTmpRect.top, 1.0f / scaleH));
            scale.setInterpolator(this.mDecelerateInterpolator);
            Animation alpha2 = new AlphaAnimation(1.0f, 0.0f);
            alpha2.setInterpolator(this.mThumbnailFadeOutInterpolator);
            AnimationSet set = new AnimationSet(false);
            set.addAnimation(scale);
            set.addAnimation(alpha2);
            alpha = set;
        } else {
            float scaleW2 = ((float) appWidth) / thumbWidth;
            float scaleH2 = ((float) appHeight) / thumbHeight;
            alpha = new ScaleAnimation(scaleW2, 1.0f, scaleH2, 1.0f, computePivot(this.mTmpRect.left, 1.0f / scaleW2), computePivot(this.mTmpRect.top, 1.0f / scaleH2));
        }
        return prepareThumbnailAnimation(alpha, appWidth, appHeight, transit);
    }

    /* access modifiers changed from: package-private */
    public Animation createThumbnailEnterExitAnimationLocked(int thumbTransitState, Rect containingFrame, int transit, int taskId) {
        Animation a;
        int appWidth = containingFrame.width();
        int appHeight = containingFrame.height();
        GraphicBuffer thumbnailHeader = getAppTransitionThumbnailHeader(taskId);
        getDefaultNextAppTransitionStartRect(this.mTmpRect);
        int thumbWidthI = thumbnailHeader != null ? thumbnailHeader.getWidth() : appWidth;
        float thumbWidth = thumbWidthI > 0 ? (float) thumbWidthI : 1.0f;
        int thumbHeightI = thumbnailHeader != null ? thumbnailHeader.getHeight() : appHeight;
        float thumbHeight = thumbHeightI > 0 ? (float) thumbHeightI : 1.0f;
        if (thumbTransitState == 0) {
            float scaleW = thumbWidth / ((float) appWidth);
            float scaleH = thumbHeight / ((float) appHeight);
            a = new ScaleAnimation(scaleW, 1.0f, scaleH, 1.0f, computePivot(this.mTmpRect.left, scaleW), computePivot(this.mTmpRect.top, scaleH));
        } else if (thumbTransitState != 1) {
            if (thumbTransitState == 2) {
                a = new AlphaAnimation(1.0f, 1.0f);
            } else if (thumbTransitState == 3) {
                float scaleW2 = thumbWidth / ((float) appWidth);
                float scaleH2 = thumbHeight / ((float) appHeight);
                Animation scale = new ScaleAnimation(1.0f, scaleW2, 1.0f, scaleH2, computePivot(this.mTmpRect.left, scaleW2), computePivot(this.mTmpRect.top, scaleH2));
                Animation alpha = new AlphaAnimation(1.0f, 0.0f);
                AnimationSet set = new AnimationSet(true);
                set.addAnimation(scale);
                set.addAnimation(alpha);
                set.setZAdjustment(1);
                a = set;
            } else {
                throw new RuntimeException("Invalid thumbnail transition state");
            }
        } else if (transit == 14) {
            a = new AlphaAnimation(1.0f, 0.0f);
        } else {
            a = new AlphaAnimation(1.0f, 1.0f);
        }
        return prepareThumbnailAnimation(a, appWidth, appHeight, transit);
    }

    private Animation createRelaunchAnimation(Rect containingFrame, Rect contentInsets) {
        getDefaultNextAppTransitionStartRect(this.mTmpFromClipRect);
        int left = this.mTmpFromClipRect.left;
        int top = this.mTmpFromClipRect.top;
        this.mTmpFromClipRect.offset(-left, -top);
        this.mTmpToClipRect.set(0, 0, containingFrame.width(), containingFrame.height());
        AnimationSet set = new AnimationSet(true);
        float fromWidth = (float) this.mTmpFromClipRect.width();
        float toWidth = (float) this.mTmpToClipRect.width();
        float fromHeight = (float) this.mTmpFromClipRect.height();
        float toHeight = (float) ((this.mTmpToClipRect.height() - contentInsets.top) - contentInsets.bottom);
        int translateAdjustment = 0;
        if (fromWidth > toWidth || fromHeight > toHeight) {
            set.addAnimation(new ScaleAnimation(fromWidth / toWidth, 1.0f, fromHeight / toHeight, 1.0f));
            translateAdjustment = (int) ((((float) contentInsets.top) * fromHeight) / toHeight);
        } else {
            set.addAnimation(new ClipRectAnimation(this.mTmpFromClipRect, this.mTmpToClipRect));
        }
        set.addAnimation(new TranslateAnimation((float) (left - containingFrame.left), 0.0f, (float) ((top - containingFrame.top) - translateAdjustment), 0.0f));
        set.setDuration(200);
        set.setZAdjustment(1);
        return set;
    }

    /* access modifiers changed from: package-private */
    public boolean canSkipFirstFrame() {
        int i = this.mNextAppTransitionType;
        return (i == 1 || i == NEXT_TRANSIT_TYPE_CUSTOM_IN_PLACE || i == 8 || this.mNextAppTransition == 20) ? false : true;
    }

    /* access modifiers changed from: package-private */
    public RemoteAnimationController getRemoteAnimationController() {
        return this.mRemoteAnimationController;
    }

    /* access modifiers changed from: package-private */
    /* JADX WARNING: Removed duplicated region for block: B:157:0x039d  */
    /* JADX WARNING: Removed duplicated region for block: B:158:0x03a2  */
    public Animation loadAnimation(WindowManager.LayoutParams lp, int transit, boolean enter, int uiMode, int orientation, Rect frame, Rect displayFrame, Rect insets, Rect surfaceInsets, Rect stableInsets, boolean isVoiceInteraction, boolean freeform, int taskId) {
        String str;
        String str2;
        String str3;
        String str4;
        boolean z;
        Animation a;
        int i;
        String str5;
        String str6;
        String str7;
        int i2;
        int i3;
        int i4;
        int i5;
        int i6;
        int i7;
        int i8;
        int i9;
        int i10;
        int i11;
        Animation a2;
        int i12;
        int i13;
        if (isKeyguardGoingAwayTransit(transit) && enter) {
            a = loadKeyguardExitAnimation(transit);
            str2 = " transit=";
            str4 = " nextAppTransition=";
            z = enter;
            str = " isEntrance=";
            str3 = "applyAnimation: anim=";
        } else if (transit == 22) {
            a = null;
            str2 = " transit=";
            str4 = " nextAppTransition=";
            z = enter;
            str = " isEntrance=";
            str3 = "applyAnimation: anim=";
        } else if (transit == 23 && !enter) {
            a = loadAnimationRes(lp, 17432921);
            str2 = " transit=";
            str4 = " nextAppTransition=";
            z = enter;
            str = " isEntrance=";
            str3 = "applyAnimation: anim=";
        } else if (transit == 26) {
            a = null;
            str2 = " transit=";
            str4 = " nextAppTransition=";
            z = enter;
            str = " isEntrance=";
            str3 = "applyAnimation: anim=";
        } else {
            if (isVoiceInteraction && (transit == NEXT_TRANSIT_TYPE_THUMBNAIL_ASPECT_SCALE_DOWN || transit == 8 || transit == NEXT_TRANSIT_TYPE_REMOTE)) {
                if (enter) {
                    i13 = 17432908;
                } else {
                    i13 = 17432909;
                }
                a = loadAnimationRes(lp, i13);
                if (WindowManagerDebugConfig.DEBUG_APP_TRANSITIONS || WindowManagerDebugConfig.DEBUG_ANIM) {
                    Slog.v(TAG, "applyAnimation voice: anim=" + a + " transit=" + appTransitionToString(transit) + " isEntrance=" + enter + " Callers=" + Debug.getCallers(3));
                }
            } else if (!isVoiceInteraction || !(transit == NEXT_TRANSIT_TYPE_CUSTOM_IN_PLACE || transit == NEXT_TRANSIT_TYPE_OPEN_CROSS_PROFILE_APPS || transit == 11)) {
                if (transit == 18) {
                    a2 = createRelaunchAnimation(frame, insets);
                    if (WindowManagerDebugConfig.DEBUG_APP_TRANSITIONS || WindowManagerDebugConfig.DEBUG_ANIM) {
                        Slog.v(TAG, "applyAnimation: anim=" + a2 + " nextAppTransition=" + this.mNextAppTransition + " transit=" + appTransitionToString(transit) + " Callers=" + Debug.getCallers(3));
                    }
                } else {
                    int i14 = this.mNextAppTransitionType;
                    boolean z2 = true;
                    if (i14 == 1) {
                        a2 = loadAnimationRes(this.mNextAppTransitionPackage, enter ? this.mNextAppTransitionEnter : this.mNextAppTransitionExit);
                        if (WindowManagerDebugConfig.DEBUG_APP_TRANSITIONS || WindowManagerDebugConfig.DEBUG_ANIM) {
                            Slog.v(TAG, "applyAnimation: anim=" + a2 + " nextAppTransition=ANIM_CUSTOM transit=" + appTransitionToString(transit) + " isEntrance=" + enter + " Callers=" + Debug.getCallers(3));
                        }
                    } else if (i14 == NEXT_TRANSIT_TYPE_CUSTOM_IN_PLACE) {
                        a2 = loadAnimationRes(this.mNextAppTransitionPackage, this.mNextAppTransitionInPlace);
                        if (WindowManagerDebugConfig.DEBUG_APP_TRANSITIONS || WindowManagerDebugConfig.DEBUG_ANIM) {
                            Slog.v(TAG, "applyAnimation: anim=" + a2 + " nextAppTransition=ANIM_CUSTOM_IN_PLACE transit=" + appTransitionToString(transit) + " Callers=" + Debug.getCallers(3));
                        }
                    } else if (i14 == 8) {
                        Animation a3 = createClipRevealAnimationLocked(transit, enter, frame, displayFrame);
                        if (WindowManagerDebugConfig.DEBUG_APP_TRANSITIONS || WindowManagerDebugConfig.DEBUG_ANIM) {
                            Slog.v(TAG, "applyAnimation: anim=" + a3 + " nextAppTransition=ANIM_CLIP_REVEAL transit=" + appTransitionToString(transit) + " Callers=" + Debug.getCallers(3));
                        }
                        a = a3;
                        str2 = " transit=";
                        str4 = " nextAppTransition=";
                        z = enter;
                        str = " isEntrance=";
                        str3 = "applyAnimation: anim=";
                    } else if (i14 == 2) {
                        a = createScaleUpAnimationLocked(transit, enter, frame);
                        if (WindowManagerDebugConfig.DEBUG_APP_TRANSITIONS || WindowManagerDebugConfig.DEBUG_ANIM) {
                            Slog.v(TAG, "applyAnimation: anim=" + a + " nextAppTransition=ANIM_SCALE_UP transit=" + appTransitionToString(transit) + " isEntrance=" + enter + " Callers=" + Debug.getCallers(3));
                        }
                    } else {
                        if (i14 == 3) {
                            i = 3;
                            str5 = "applyAnimation: anim=";
                            str2 = " transit=";
                            str = " isEntrance=";
                            str6 = TAG;
                            str7 = " Callers=";
                            str4 = " nextAppTransition=";
                            z = enter;
                        } else if (i14 == 4) {
                            str5 = "applyAnimation: anim=";
                            str2 = " transit=";
                            str = " isEntrance=";
                            i = 3;
                            str6 = TAG;
                            str7 = " Callers=";
                            str4 = " nextAppTransition=";
                            z = enter;
                        } else {
                            int i15 = 5;
                            if (i14 == 5) {
                                i2 = 3;
                            } else if (i14 == NEXT_TRANSIT_TYPE_THUMBNAIL_ASPECT_SCALE_DOWN) {
                                i2 = 3;
                            } else {
                                int i16 = NEXT_TRANSIT_TYPE_OPEN_CROSS_PROFILE_APPS;
                                if (i14 == NEXT_TRANSIT_TYPE_OPEN_CROSS_PROFILE_APPS && enter) {
                                    a = loadAnimationRes("android", 17432898);
                                    Slog.v(TAG, "applyAnimation NEXT_TRANSIT_TYPE_OPEN_CROSS_PROFILE_APPS: anim=" + a + " transit=" + appTransitionToString(transit) + " isEntrance=true Callers=" + Debug.getCallers(3));
                                    str2 = " transit=";
                                    str4 = " nextAppTransition=";
                                    z = enter;
                                    str = " isEntrance=";
                                    str3 = "applyAnimation: anim=";
                                } else if (transit == 27) {
                                    a = new AlphaAnimation(1.0f, 1.0f);
                                    a.setDuration(200);
                                    if (WindowManagerDebugConfig.DEBUG_APP_TRANSITIONS || WindowManagerDebugConfig.DEBUG_ANIM) {
                                        Slog.v(TAG, "applyAnimation: anim=" + a + " transit=" + appTransitionToString(transit) + " isEntrance=" + enter + " Callers=" + Debug.getCallers(3));
                                    }
                                } else {
                                    int animAttr = 0;
                                    if (transit != 19) {
                                        if (transit != 24) {
                                            if (transit != 25) {
                                                switch (transit) {
                                                    case NEXT_TRANSIT_TYPE_OPEN_CROSS_PROFILE_APPS /* 9 */:
                                                        if (enter) {
                                                            i4 = NEXT_TRANSIT_TYPE_REMOTE;
                                                        } else {
                                                            i4 = 11;
                                                        }
                                                        animAttr = i4;
                                                        break;
                                                    case NEXT_TRANSIT_TYPE_REMOTE /* 10 */:
                                                        if (enter) {
                                                            i5 = 12;
                                                        } else {
                                                            i5 = 13;
                                                        }
                                                        animAttr = i5;
                                                        break;
                                                    case WindowManagerService.H.WINDOW_FREEZE_TIMEOUT /* 11 */:
                                                        if (enter) {
                                                            i6 = 14;
                                                        } else {
                                                            i6 = 15;
                                                        }
                                                        animAttr = i6;
                                                        break;
                                                    case 12:
                                                        if (!this.mService.mPolicy.isKeyguardShowingOrOccluded()) {
                                                            if (enter) {
                                                                i7 = 18;
                                                            } else {
                                                                i7 = 19;
                                                            }
                                                            animAttr = i7;
                                                            break;
                                                        }
                                                        break;
                                                    case 13:
                                                        if (enter) {
                                                            i8 = 16;
                                                        } else {
                                                            i8 = 17;
                                                        }
                                                        animAttr = i8;
                                                        break;
                                                    case WindowManagerService.H.PERSIST_ANIMATION_SCALE /* 14 */:
                                                        if (enter) {
                                                            i9 = 20;
                                                        } else {
                                                            i9 = 21;
                                                        }
                                                        animAttr = i9;
                                                        break;
                                                    case WindowManagerService.H.FORCE_GC /* 15 */:
                                                        if (enter) {
                                                            i10 = 22;
                                                        } else {
                                                            i10 = 23;
                                                        }
                                                        animAttr = i10;
                                                        break;
                                                    case WindowManagerService.H.ENABLE_SCREEN /* 16 */:
                                                        if (enter) {
                                                            i11 = 25;
                                                        } else {
                                                            i11 = 24;
                                                        }
                                                        animAttr = i11;
                                                        break;
                                                }
                                                a = animAttr == 0 ? loadAnimationAttr(lp, animAttr, transit) : null;
                                                reloadAnimDuration(a, transit, isVoiceInteraction);
                                                if (WindowManagerDebugConfig.DEBUG_APP_TRANSITIONS || WindowManagerDebugConfig.DEBUG_ANIM) {
                                                    Slog.v(TAG, "applyAnimation: anim=" + a + " animAttr=0x" + Integer.toHexString(animAttr) + " transit=" + appTransitionToString(transit) + " isEntrance=" + enter + " Callers=" + Debug.getCallers(3));
                                                }
                                            }
                                            if (enter) {
                                                i3 = NEXT_TRANSIT_TYPE_THUMBNAIL_ASPECT_SCALE_DOWN;
                                            } else {
                                                i3 = NEXT_TRANSIT_TYPE_CUSTOM_IN_PLACE;
                                            }
                                            animAttr = i3;
                                            if (animAttr == 0) {
                                            }
                                            reloadAnimDuration(a, transit, isVoiceInteraction);
                                            Slog.v(TAG, "applyAnimation: anim=" + a + " animAttr=0x" + Integer.toHexString(animAttr) + " transit=" + appTransitionToString(transit) + " isEntrance=" + enter + " Callers=" + Debug.getCallers(3));
                                        }
                                        if (enter) {
                                            i15 = 4;
                                        }
                                        animAttr = i15;
                                        if (animAttr == 0) {
                                        }
                                        reloadAnimDuration(a, transit, isVoiceInteraction);
                                        Slog.v(TAG, "applyAnimation: anim=" + a + " animAttr=0x" + Integer.toHexString(animAttr) + " transit=" + appTransitionToString(transit) + " isEntrance=" + enter + " Callers=" + Debug.getCallers(3));
                                    }
                                    if (enter) {
                                        i16 = 8;
                                    }
                                    animAttr = i16;
                                    if (animAttr == 0) {
                                    }
                                    reloadAnimDuration(a, transit, isVoiceInteraction);
                                    Slog.v(TAG, "applyAnimation: anim=" + a + " animAttr=0x" + Integer.toHexString(animAttr) + " transit=" + appTransitionToString(transit) + " isEntrance=" + enter + " Callers=" + Debug.getCallers(3));
                                }
                            }
                            if (this.mNextAppTransitionType != 5) {
                                z2 = false;
                            }
                            this.mNextAppTransitionScaleUp = z2;
                            str2 = " transit=";
                            str = " isEntrance=";
                            a = createAspectScaledThumbnailEnterExitAnimationLocked(getThumbnailTransitionState(enter), uiMode, orientation, transit, frame, insets, surfaceInsets, stableInsets, freeform, taskId);
                            if (WindowManagerDebugConfig.DEBUG_APP_TRANSITIONS || WindowManagerDebugConfig.DEBUG_ANIM) {
                                String animName = this.mNextAppTransitionScaleUp ? "ANIM_THUMBNAIL_ASPECT_SCALE_UP" : "ANIM_THUMBNAIL_ASPECT_SCALE_DOWN";
                                StringBuilder sb = new StringBuilder();
                                sb.append("applyAnimation: anim=");
                                sb.append(a);
                                sb.append(" nextAppTransition=");
                                sb.append(animName);
                                sb.append(str2);
                                sb.append(appTransitionToString(transit));
                                sb.append(str);
                                z = enter;
                                str4 = " nextAppTransition=";
                                sb.append(z);
                                sb.append(" Callers=");
                                sb.append(Debug.getCallers(i2));
                                Slog.v(TAG, sb.toString());
                            } else {
                                z = enter;
                                str4 = " nextAppTransition=";
                            }
                            str3 = "applyAnimation: anim=";
                        }
                        if (this.mNextAppTransitionType != i) {
                            z2 = false;
                        }
                        this.mNextAppTransitionScaleUp = z2;
                        str3 = str5;
                        a = createThumbnailEnterExitAnimationLocked(getThumbnailTransitionState(z), frame, transit, taskId);
                        if (WindowManagerDebugConfig.DEBUG_APP_TRANSITIONS || WindowManagerDebugConfig.DEBUG_ANIM) {
                            Slog.v(str6, str3 + a + str4 + (this.mNextAppTransitionScaleUp ? "ANIM_THUMBNAIL_SCALE_UP" : "ANIM_THUMBNAIL_SCALE_DOWN") + str2 + appTransitionToString(transit) + str + z + str7 + Debug.getCallers(i));
                        }
                    }
                }
                a = a2;
                str2 = " transit=";
                str4 = " nextAppTransition=";
                z = enter;
                str = " isEntrance=";
                str3 = "applyAnimation: anim=";
            } else {
                if (enter) {
                    i12 = 17432906;
                } else {
                    i12 = 17432907;
                }
                a = loadAnimationRes(lp, i12);
                if (WindowManagerDebugConfig.DEBUG_APP_TRANSITIONS || WindowManagerDebugConfig.DEBUG_ANIM) {
                    Slog.v(TAG, "applyAnimation voice: anim=" + a + " transit=" + appTransitionToString(transit) + " isEntrance=" + enter + " Callers=" + Debug.getCallers(3));
                }
            }
            str2 = " transit=";
            str4 = " nextAppTransition=";
            z = enter;
            str = " isEntrance=";
            str3 = "applyAnimation: anim=";
        }
        StringBuilder sb2 = new StringBuilder();
        sb2.append(str3);
        sb2.append(a);
        sb2.append(" duration=");
        sb2.append(a == null ? 0 : a.getDuration());
        sb2.append(str4);
        sb2.append(transitTypeToString());
        sb2.append(str2);
        sb2.append(appTransitionToString(transit));
        sb2.append(str);
        sb2.append(z);
        sb2.append(" isVoiceInteraction=");
        sb2.append(isVoiceInteraction);
        Flog.i(310, sb2.toString());
        return a;
    }

    private void reloadAnimDuration(Animation a, int transit, boolean isVoiceInteraction) {
        if (a != null && !isVoiceInteraction) {
            if (transit == NEXT_TRANSIT_TYPE_THUMBNAIL_ASPECT_SCALE_DOWN || transit == NEXT_TRANSIT_TYPE_CUSTOM_IN_PLACE) {
                this.mDeviceSize = calculateDeviceSize();
                if (this.mDeviceSize >= SCREEN_SIZE_8_INCH) {
                    a.setDuration((long) (((float) a.getDuration()) * GREAT_SCREEN_DURATION_PARA));
                }
            }
        }
    }

    private double calculateDeviceSize() {
        double d = this.mDeviceSize;
        if (d > 0.0d) {
            return d;
        }
        Point point = new Point();
        float xdpi = this.mService.getDefaultDisplayContentLocked().mDisplayInfo.physicalXDpi;
        float ydpi = this.mService.getDefaultDisplayContentLocked().mDisplayInfo.physicalYDpi;
        this.mService.getInitialDisplaySize(0, point);
        this.mDeviceSize = new BigDecimal(Math.sqrt(Math.pow((double) (((float) point.x) / xdpi), 2.0d) + Math.pow((double) (((float) point.y) / ydpi), 2.0d))).setScale(2, 4).doubleValue();
        return this.mDeviceSize;
    }

    private Animation loadKeyguardExitAnimation(int transit) {
        int i = this.mNextAppTransitionFlags;
        if ((i & 2) != 0) {
            return null;
        }
        boolean z = true;
        boolean toShade = (i & 1) != 0;
        WindowManagerPolicy windowManagerPolicy = this.mService.mPolicy;
        if (transit != 21) {
            z = false;
        }
        return windowManagerPolicy.createHiddenByKeyguardExit(z, toShade);
    }

    /* access modifiers changed from: package-private */
    public int getAppStackClipMode() {
        int i = this.mNextAppTransition;
        if (i == 20 || i == 21) {
            return 1;
        }
        if (i == 18 || i == 19 || this.mNextAppTransitionType == 8) {
            return 2;
        }
        return 0;
    }

    public int getTransitFlags() {
        return this.mNextAppTransitionFlags;
    }

    /* access modifiers changed from: package-private */
    public void postAnimationCallback() {
        IRemoteCallback iRemoteCallback = this.mNextAppTransitionCallback;
        if (iRemoteCallback != null) {
            this.mHandler.sendMessage(PooledLambda.obtainMessage($$Lambda$AppTransition$B95jxKE2FnT5RNLStTafenhEYj4.INSTANCE, iRemoteCallback));
            this.mNextAppTransitionCallback = null;
        }
    }

    /* access modifiers changed from: package-private */
    public void overridePendingAppTransition(String packageName, int enterAnim, int exitAnim, IRemoteCallback startedCallback) {
        if (canOverridePendingAppTransition()) {
            clear();
            this.mNextAppTransitionType = 1;
            this.mNextAppTransitionPackage = packageName;
            this.mNextAppTransitionEnter = enterAnim;
            this.mNextAppTransitionExit = exitAnim;
            postAnimationCallback();
            this.mNextAppTransitionCallback = startedCallback;
        }
    }

    /* access modifiers changed from: package-private */
    public void overridePendingAppTransitionScaleUp(int startX, int startY, int startWidth, int startHeight) {
        if (canOverridePendingAppTransition()) {
            clear();
            this.mNextAppTransitionType = 2;
            putDefaultNextAppTransitionCoordinates(startX, startY, startWidth, startHeight, null);
            postAnimationCallback();
        }
    }

    /* access modifiers changed from: package-private */
    public void overridePendingAppTransitionClipReveal(int startX, int startY, int startWidth, int startHeight) {
        if (canOverridePendingAppTransition()) {
            clear();
            this.mNextAppTransitionType = 8;
            putDefaultNextAppTransitionCoordinates(startX, startY, startWidth, startHeight, null);
            postAnimationCallback();
        }
    }

    /* access modifiers changed from: package-private */
    public void overridePendingAppTransitionThumb(GraphicBuffer srcThumb, int startX, int startY, IRemoteCallback startedCallback, boolean scaleUp) {
        int i;
        if (canOverridePendingAppTransition()) {
            clear();
            if (scaleUp) {
                i = 3;
            } else {
                i = 4;
            }
            this.mNextAppTransitionType = i;
            this.mNextAppTransitionScaleUp = scaleUp;
            putDefaultNextAppTransitionCoordinates(startX, startY, 0, 0, srcThumb);
            postAnimationCallback();
            this.mNextAppTransitionCallback = startedCallback;
        }
    }

    /* access modifiers changed from: package-private */
    public void overridePendingAppTransitionAspectScaledThumb(GraphicBuffer srcThumb, int startX, int startY, int targetWidth, int targetHeight, IRemoteCallback startedCallback, boolean scaleUp) {
        int i;
        if (canOverridePendingAppTransition()) {
            clear();
            if (scaleUp) {
                i = 5;
            } else {
                i = NEXT_TRANSIT_TYPE_THUMBNAIL_ASPECT_SCALE_DOWN;
            }
            this.mNextAppTransitionType = i;
            this.mNextAppTransitionScaleUp = scaleUp;
            putDefaultNextAppTransitionCoordinates(startX, startY, targetWidth, targetHeight, srcThumb);
            postAnimationCallback();
            this.mNextAppTransitionCallback = startedCallback;
        }
    }

    /* access modifiers changed from: package-private */
    public void overridePendingAppTransitionMultiThumb(AppTransitionAnimationSpec[] specs, IRemoteCallback onAnimationStartedCallback, IRemoteCallback onAnimationFinishedCallback, boolean scaleUp) {
        int i;
        if (canOverridePendingAppTransition()) {
            clear();
            if (scaleUp) {
                i = 5;
            } else {
                i = NEXT_TRANSIT_TYPE_THUMBNAIL_ASPECT_SCALE_DOWN;
            }
            this.mNextAppTransitionType = i;
            this.mNextAppTransitionScaleUp = scaleUp;
            if (specs != null) {
                for (int i2 = 0; i2 < specs.length; i2++) {
                    AppTransitionAnimationSpec spec = specs[i2];
                    if (spec != null) {
                        this.mNextAppTransitionAnimationsSpecs.put(spec.taskId, spec);
                        if (i2 == 0) {
                            Rect rect = spec.rect;
                            putDefaultNextAppTransitionCoordinates(rect.left, rect.top, rect.width(), rect.height(), spec.buffer);
                        }
                    }
                }
            }
            postAnimationCallback();
            this.mNextAppTransitionCallback = onAnimationStartedCallback;
            this.mAnimationFinishedCallback = onAnimationFinishedCallback;
        }
    }

    /* access modifiers changed from: package-private */
    public void overridePendingAppTransitionMultiThumbFuture(IAppTransitionAnimationSpecsFuture specsFuture, IRemoteCallback callback, boolean scaleUp) {
        int i;
        if (canOverridePendingAppTransition()) {
            clear();
            if (scaleUp) {
                i = 5;
            } else {
                i = NEXT_TRANSIT_TYPE_THUMBNAIL_ASPECT_SCALE_DOWN;
            }
            this.mNextAppTransitionType = i;
            this.mNextAppTransitionAnimationsSpecsFuture = specsFuture;
            this.mNextAppTransitionScaleUp = scaleUp;
            this.mNextAppTransitionFutureCallback = callback;
            if (isReady()) {
                fetchAppTransitionSpecsFromFuture();
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void overridePendingAppTransitionRemote(RemoteAnimationAdapter remoteAnimationAdapter) {
        if (WindowManagerDebugConfig.DEBUG_APP_TRANSITIONS) {
            Slog.i(TAG, "Override pending remote transitionSet=" + isTransitionSet() + " adapter=" + remoteAnimationAdapter);
        }
        if (isTransitionSet()) {
            clear();
            this.mNextAppTransitionType = NEXT_TRANSIT_TYPE_REMOTE;
            this.mRemoteAnimationController = new RemoteAnimationController(this.mService, remoteAnimationAdapter, this.mHandler);
        }
    }

    /* access modifiers changed from: package-private */
    public void overrideInPlaceAppTransition(String packageName, int anim) {
        if (canOverridePendingAppTransition()) {
            clear();
            this.mNextAppTransitionType = NEXT_TRANSIT_TYPE_CUSTOM_IN_PLACE;
            this.mNextAppTransitionPackage = packageName;
            this.mNextAppTransitionInPlace = anim;
        }
    }

    /* access modifiers changed from: package-private */
    public void overridePendingAppTransitionStartCrossProfileApps() {
        if (canOverridePendingAppTransition()) {
            clear();
            this.mNextAppTransitionType = NEXT_TRANSIT_TYPE_OPEN_CROSS_PROFILE_APPS;
            postAnimationCallback();
        }
    }

    private boolean canOverridePendingAppTransition() {
        return isTransitionSet() && this.mNextAppTransitionType != NEXT_TRANSIT_TYPE_REMOTE;
    }

    private void fetchAppTransitionSpecsFromFuture() {
        if (this.mNextAppTransitionAnimationsSpecsFuture != null) {
            this.mNextAppTransitionAnimationsSpecsPending = true;
            IAppTransitionAnimationSpecsFuture future = this.mNextAppTransitionAnimationsSpecsFuture;
            this.mNextAppTransitionAnimationsSpecsFuture = null;
            this.mDefaultExecutor.execute(new Runnable(future) {
                /* class com.android.server.wm.$$Lambda$AppTransition$9JtLlCXlArIsRNjLJ0_3RWFSHts */
                private final /* synthetic */ IAppTransitionAnimationSpecsFuture f$1;

                {
                    this.f$1 = r2;
                }

                @Override // java.lang.Runnable
                public final void run() {
                    AppTransition.this.lambda$fetchAppTransitionSpecsFromFuture$1$AppTransition(this.f$1);
                }
            });
        }
    }

    /* JADX INFO: finally extract failed */
    public /* synthetic */ void lambda$fetchAppTransitionSpecsFromFuture$1$AppTransition(IAppTransitionAnimationSpecsFuture future) {
        AppTransitionAnimationSpec[] specs = null;
        try {
            Binder.allowBlocking(future.asBinder());
            specs = future.get();
        } catch (RemoteException e) {
            Slog.w(TAG, "Failed to fetch app transition specs: " + e);
        }
        synchronized (this.mService.mGlobalLock) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                this.mNextAppTransitionAnimationsSpecsPending = false;
                overridePendingAppTransitionMultiThumb(specs, this.mNextAppTransitionFutureCallback, null, this.mNextAppTransitionScaleUp);
                this.mNextAppTransitionFutureCallback = null;
            } catch (Throwable th) {
                WindowManagerService.resetPriorityAfterLockedSection();
                throw th;
            }
        }
        WindowManagerService.resetPriorityAfterLockedSection();
        this.mService.requestTraversal();
    }

    public String toString() {
        return "mNextAppTransition=" + appTransitionToString(this.mNextAppTransition);
    }

    public static String appTransitionToString(int transition) {
        if (transition == -1) {
            return "TRANSIT_UNSET";
        }
        if (transition == 0) {
            return "TRANSIT_NONE";
        }
        switch (transition) {
            case NEXT_TRANSIT_TYPE_THUMBNAIL_ASPECT_SCALE_DOWN /* 6 */:
                return "TRANSIT_ACTIVITY_OPEN";
            case NEXT_TRANSIT_TYPE_CUSTOM_IN_PLACE /* 7 */:
                return "TRANSIT_ACTIVITY_CLOSE";
            case 8:
                return "TRANSIT_TASK_OPEN";
            case NEXT_TRANSIT_TYPE_OPEN_CROSS_PROFILE_APPS /* 9 */:
                return "TRANSIT_TASK_CLOSE";
            case NEXT_TRANSIT_TYPE_REMOTE /* 10 */:
                return "TRANSIT_TASK_TO_FRONT";
            case WindowManagerService.H.WINDOW_FREEZE_TIMEOUT /* 11 */:
                return "TRANSIT_TASK_TO_BACK";
            case 12:
                return "TRANSIT_WALLPAPER_CLOSE";
            case 13:
                return "TRANSIT_WALLPAPER_OPEN";
            case WindowManagerService.H.PERSIST_ANIMATION_SCALE /* 14 */:
                return "TRANSIT_WALLPAPER_INTRA_OPEN";
            case WindowManagerService.H.FORCE_GC /* 15 */:
                return "TRANSIT_WALLPAPER_INTRA_CLOSE";
            case WindowManagerService.H.ENABLE_SCREEN /* 16 */:
                return "TRANSIT_TASK_OPEN_BEHIND";
            default:
                switch (transition) {
                    case WindowManagerService.H.SEND_NEW_CONFIGURATION /* 18 */:
                        return "TRANSIT_ACTIVITY_RELAUNCH";
                    case WindowManagerService.H.REPORT_WINDOWS_CHANGE /* 19 */:
                        return "TRANSIT_DOCK_TASK_FROM_RECENTS";
                    case 20:
                        return "TRANSIT_KEYGUARD_GOING_AWAY";
                    case 21:
                        return "TRANSIT_KEYGUARD_GOING_AWAY_ON_WALLPAPER";
                    case WindowManagerService.H.REPORT_HARD_KEYBOARD_STATUS_CHANGE /* 22 */:
                        return "TRANSIT_KEYGUARD_OCCLUDE";
                    case WindowManagerService.H.BOOT_TIMEOUT /* 23 */:
                        return "TRANSIT_KEYGUARD_UNOCCLUDE";
                    case WindowManagerService.H.WAITING_FOR_DRAWN_TIMEOUT /* 24 */:
                        return "TRANSIT_TRANSLUCENT_ACTIVITY_OPEN";
                    case WindowManagerService.H.SHOW_STRICT_MODE_VIOLATION /* 25 */:
                        return "TRANSIT_TRANSLUCENT_ACTIVITY_CLOSE";
                    case 26:
                        return "TRANSIT_CRASHING_ACTIVITY_CLOSE";
                    default:
                        return "<UNKNOWN: " + transition + ">";
                }
        }
    }

    private String appStateToString() {
        int i = this.mAppTransitionState;
        if (i == 0) {
            return "APP_STATE_IDLE";
        }
        if (i == 1) {
            return "APP_STATE_READY";
        }
        if (i == 2) {
            return "APP_STATE_RUNNING";
        }
        if (i == 3) {
            return "APP_STATE_TIMEOUT";
        }
        return "unknown state=" + this.mAppTransitionState;
    }

    private String transitTypeToString() {
        switch (this.mNextAppTransitionType) {
            case 0:
                return "NEXT_TRANSIT_TYPE_NONE";
            case 1:
                return "NEXT_TRANSIT_TYPE_CUSTOM";
            case 2:
                return "NEXT_TRANSIT_TYPE_SCALE_UP";
            case 3:
                return "NEXT_TRANSIT_TYPE_THUMBNAIL_SCALE_UP";
            case 4:
                return "NEXT_TRANSIT_TYPE_THUMBNAIL_SCALE_DOWN";
            case 5:
                return "NEXT_TRANSIT_TYPE_THUMBNAIL_ASPECT_SCALE_UP";
            case NEXT_TRANSIT_TYPE_THUMBNAIL_ASPECT_SCALE_DOWN /* 6 */:
                return "NEXT_TRANSIT_TYPE_THUMBNAIL_ASPECT_SCALE_DOWN";
            case NEXT_TRANSIT_TYPE_CUSTOM_IN_PLACE /* 7 */:
                return "NEXT_TRANSIT_TYPE_CUSTOM_IN_PLACE";
            case 8:
            default:
                return "unknown type=" + this.mNextAppTransitionType;
            case NEXT_TRANSIT_TYPE_OPEN_CROSS_PROFILE_APPS /* 9 */:
                return "NEXT_TRANSIT_TYPE_OPEN_CROSS_PROFILE_APPS";
        }
    }

    /* access modifiers changed from: package-private */
    public void writeToProto(ProtoOutputStream proto, long fieldId) {
        long token = proto.start(fieldId);
        proto.write(1159641169921L, this.mAppTransitionState);
        proto.write(1159641169922L, this.mLastUsedAppTransition);
        proto.end(token);
    }

    public void dump(PrintWriter pw, String prefix) {
        pw.print(prefix);
        pw.println(this);
        pw.print(prefix);
        pw.print("mAppTransitionState=");
        pw.println(appStateToString());
        if (this.mNextAppTransitionType != 0) {
            pw.print(prefix);
            pw.print("mNextAppTransitionType=");
            pw.println(transitTypeToString());
        }
        switch (this.mNextAppTransitionType) {
            case 1:
                pw.print(prefix);
                pw.print("mNextAppTransitionPackage=");
                pw.println(this.mNextAppTransitionPackage);
                pw.print(prefix);
                pw.print("mNextAppTransitionEnter=0x");
                pw.print(Integer.toHexString(this.mNextAppTransitionEnter));
                pw.print(" mNextAppTransitionExit=0x");
                pw.println(Integer.toHexString(this.mNextAppTransitionExit));
                break;
            case 2:
                getDefaultNextAppTransitionStartRect(this.mTmpRect);
                pw.print(prefix);
                pw.print("mNextAppTransitionStartX=");
                pw.print(this.mTmpRect.left);
                pw.print(" mNextAppTransitionStartY=");
                pw.println(this.mTmpRect.top);
                pw.print(prefix);
                pw.print("mNextAppTransitionStartWidth=");
                pw.print(this.mTmpRect.width());
                pw.print(" mNextAppTransitionStartHeight=");
                pw.println(this.mTmpRect.height());
                break;
            case 3:
            case 4:
            case 5:
            case NEXT_TRANSIT_TYPE_THUMBNAIL_ASPECT_SCALE_DOWN /* 6 */:
                pw.print(prefix);
                pw.print("mDefaultNextAppTransitionAnimationSpec=");
                pw.println(this.mDefaultNextAppTransitionAnimationSpec);
                pw.print(prefix);
                pw.print("mNextAppTransitionAnimationsSpecs=");
                pw.println(this.mNextAppTransitionAnimationsSpecs);
                pw.print(prefix);
                pw.print("mNextAppTransitionScaleUp=");
                pw.println(this.mNextAppTransitionScaleUp);
                break;
            case NEXT_TRANSIT_TYPE_CUSTOM_IN_PLACE /* 7 */:
                pw.print(prefix);
                pw.print("mNextAppTransitionPackage=");
                pw.println(this.mNextAppTransitionPackage);
                pw.print(prefix);
                pw.print("mNextAppTransitionInPlace=0x");
                pw.print(Integer.toHexString(this.mNextAppTransitionInPlace));
                break;
        }
        if (this.mNextAppTransitionCallback != null) {
            pw.print(prefix);
            pw.print("mNextAppTransitionCallback=");
            pw.println(this.mNextAppTransitionCallback);
        }
        if (this.mLastUsedAppTransition != 0) {
            pw.print(prefix);
            pw.print("mLastUsedAppTransition=");
            pw.println(appTransitionToString(this.mLastUsedAppTransition));
            pw.print(prefix);
            pw.print("mLastOpeningApp=");
            pw.println(this.mLastOpeningApp);
            pw.print(prefix);
            pw.print("mLastClosingApp=");
            pw.println(this.mLastClosingApp);
            pw.print(prefix);
            pw.print("mLastChangingApp=");
            pw.println(this.mLastChangingApp);
        }
    }

    public void setCurrentUser(int newUserId) {
        this.mCurrentUserId = newUserId;
    }

    /* access modifiers changed from: package-private */
    public boolean prepareAppTransitionLocked(int transit, boolean alwaysKeepCurrent, int flags, boolean forceOverride) {
        int i;
        if (WindowManagerDebugConfig.DEBUG_APP_TRANSITIONS) {
            Slog.v(TAG, "Prepare app transition: transit=" + appTransitionToString(transit) + " " + this + " alwaysKeepCurrent=" + alwaysKeepCurrent + " displayId=" + this.mDisplayContent.getDisplayId() + " Callers=" + Debug.getCallers(5));
        }
        if (isKeyguardGoingAwayTransit(transit)) {
            this.mDisplayContent.mUnknownAppVisibilityController.clear();
        }
        boolean allowSetCrashing = !isKeyguardTransit(this.mNextAppTransition) && transit == 26;
        if (forceOverride || isKeyguardTransit(transit) || !isTransitionSet() || (i = this.mNextAppTransition) == 0 || allowSetCrashing) {
            setAppTransition(transit, flags);
        } else if (!alwaysKeepCurrent && !isKeyguardTransit(i) && this.mNextAppTransition != 26) {
            if (transit == 8 && isTransitionEqual(NEXT_TRANSIT_TYPE_OPEN_CROSS_PROFILE_APPS)) {
                setAppTransition(transit, flags);
            } else if (transit == NEXT_TRANSIT_TYPE_THUMBNAIL_ASPECT_SCALE_DOWN && isTransitionEqual(NEXT_TRANSIT_TYPE_CUSTOM_IN_PLACE)) {
                setAppTransition(transit, flags);
            } else if (isTaskTransit(transit) && isActivityTransit(this.mNextAppTransition)) {
                setAppTransition(transit, flags);
            }
        }
        boolean prepared = prepare();
        if (isTransitionSet()) {
            removeAppTransitionTimeoutCallbacks();
            this.mHandler.postDelayed(this.mHandleAppTransitionTimeoutRunnable, APP_TRANSITION_TIMEOUT_MS);
        }
        return prepared;
    }

    public static boolean isKeyguardGoingAwayTransit(int transit) {
        return transit == 20 || transit == 21;
    }

    private static boolean isKeyguardTransit(int transit) {
        return isKeyguardGoingAwayTransit(transit) || transit == 22 || transit == 23;
    }

    static boolean isTaskTransit(int transit) {
        return isTaskOpenTransit(transit) || transit == NEXT_TRANSIT_TYPE_OPEN_CROSS_PROFILE_APPS || transit == 11 || transit == 17;
    }

    private static boolean isTaskOpenTransit(int transit) {
        return transit == 8 || transit == 16 || transit == NEXT_TRANSIT_TYPE_REMOTE;
    }

    static boolean isActivityTransit(int transit) {
        return transit == NEXT_TRANSIT_TYPE_THUMBNAIL_ASPECT_SCALE_DOWN || transit == NEXT_TRANSIT_TYPE_CUSTOM_IN_PLACE || transit == 18;
    }

    static boolean isChangeTransit(int transit) {
        return transit == 27;
    }

    private boolean shouldScaleDownThumbnailTransition(int uiMode, int orientation) {
        return this.mGridLayoutRecentsEnabled || orientation == 1;
    }

    /* access modifiers changed from: private */
    /* renamed from: handleAppTransitionTimeout */
    public void lambda$new$0$AppTransition() {
        String str;
        synchronized (this.mService.mGlobalLock) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                DisplayContent dc = this.mDisplayContent;
                if (dc != null) {
                    if (isTransitionSet() || !dc.mOpeningApps.isEmpty() || !dc.mClosingApps.isEmpty() || !dc.mChangingApps.isEmpty()) {
                        Slog.v(TAG, "handleAppTransition. displayId=" + dc.getDisplayId() + " isTransitionSet()=" + dc.mAppTransition.isTransitionSet() + " mOpeningApps.size()=" + dc.mOpeningApps.size() + " mClosingApps.size()=" + dc.mClosingApps.size() + " mChangingApps.size()=" + dc.mChangingApps.size());
                        setTimeout();
                        Iterator<AppWindowToken> it = dc.mOpeningApps.iterator();
                        while (it.hasNext()) {
                            AppWindowToken appToken = it.next();
                            WindowState mainWindow = appToken.findMainWindow();
                            if (mainWindow != null) {
                                StringBuilder sb = new StringBuilder();
                                sb.append("handleAppTransition ");
                                sb.append(mainWindow);
                                sb.append(" please check last DrawState and nowSate = ");
                                if (mainWindow.mWinAnimator != null) {
                                    str = mainWindow.mWinAnimator.drawStateToString();
                                } else {
                                    str = "no WA";
                                }
                                sb.append(str);
                                Flog.i(310, sb.toString());
                            }
                            appToken.mPendingRelaunchCount = 0;
                            appToken.mFrozenBounds.clear();
                            appToken.mFrozenMergedConfig.clear();
                        }
                        this.mService.mWindowPlacerLocked.performSurfacePlacement();
                    }
                    WindowManagerService.resetPriorityAfterLockedSection();
                }
            } finally {
                WindowManagerService.resetPriorityAfterLockedSection();
            }
        }
    }

    /* access modifiers changed from: private */
    public static void doAnimationCallback(IRemoteCallback callback) {
        try {
            callback.sendResult((Bundle) null);
        } catch (RemoteException e) {
        }
    }

    /* access modifiers changed from: package-private */
    public void removeAppTransitionTimeoutCallbacks() {
        this.mHandler.removeCallbacks(this.mHandleAppTransitionTimeoutRunnable);
    }

    /* access modifiers changed from: package-private */
    public boolean isNextAppTransitCustom() {
        return this.mNextAppTransitionType == 1;
    }

    /* access modifiers changed from: package-private */
    public boolean isNextKeyguardGoingAwayTransit() {
        Slog.d(TAG, "mNextAppTransition=" + this.mNextAppTransition);
        return isKeyguardGoingAwayTransit(this.mNextAppTransition);
    }
}
