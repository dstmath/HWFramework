package com.android.server.wm;

import android.animation.TimeInterpolator;
import android.app.ActivityManager;
import android.content.Context;
import android.content.res.ResourceId;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.GraphicBuffer;
import android.graphics.Path;
import android.graphics.Picture;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Binder;
import android.os.Debug;
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
import android.view.animation.LinearInterpolator;
import android.view.animation.PathInterpolator;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;
import com.android.internal.R;
import com.android.internal.util.DumpUtils;
import com.android.server.AttributeCache;
import com.android.server.HwServiceFactory;
import com.android.server.backup.internal.BackupHandler;
import com.android.server.pm.PackageManagerService;
import com.android.server.policy.WindowManagerPolicy;
import com.android.server.wm.WindowManagerInternal;
import com.android.server.wm.WindowManagerService;
import com.android.server.wm.animation.ClipRectLRAnimation;
import com.android.server.wm.animation.ClipRectTBAnimation;
import com.android.server.wm.animation.CurvedTranslateAnimation;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AppTransition implements DumpUtils.Dump {
    private static final boolean APP_LOW_PERF_ANIM = SystemProperties.getBoolean("ro.feature.appstart.low_perf_anim", false);
    private static final int APP_STATE_IDLE = 0;
    private static final int APP_STATE_READY = 1;
    private static final int APP_STATE_RUNNING = 2;
    private static final int APP_STATE_TIMEOUT = 3;
    private static final long APP_TRANSITION_GETSPECSFUTURE_TIMEOUT_MS = 5000;
    private static final long APP_TRANSITION_TIMEOUT_MS = 5000;
    private static final int CLIP_REVEAL_TRANSLATION_Y_DP = 8;
    static final int DEFAULT_APP_TRANSITION_DURATION = 250;
    private static final float LAUNCHER_ENTER_ALPHA_TIME_RATIO = 0.2f;
    private static final float LAUNCHER_ENTER_HIDE_TIME_RATIO = 0.3f;
    private static final float LAUNCHER_ENTER_SCALE_TIME_RATIO = 0.7f;
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
    private static final String TAG = "WindowManager";
    private static final int THUMBNAIL_APP_TRANSITION_DURATION = 200;
    private static final Interpolator THUMBNAIL_DOCK_INTERPOLATOR = new PathInterpolator(0.85f, 0.0f, 1.0f, 1.0f);
    private static final int THUMBNAIL_TRANSITION_ENTER_SCALE_DOWN = 2;
    private static final int THUMBNAIL_TRANSITION_ENTER_SCALE_UP = 0;
    private static final int THUMBNAIL_TRANSITION_EXIT_SCALE_DOWN = 3;
    private static final int THUMBNAIL_TRANSITION_EXIT_SCALE_UP = 1;
    static final Interpolator TOUCH_RESPONSE_INTERPOLATOR = new PathInterpolator(LAUNCHER_ENTER_HIDE_TIME_RATIO, 0.0f, 0.1f, 1.0f);
    private float LAZY_MODE_COMP_FACTOR = 0.125f;
    private float LAZY_MODE_WIN_SCALE_FACTOR = 0.75f;
    private TimeInterpolator[] mAlphaInterpolators = {this.mConstantInterpolator, new PathInterpolator(0.4f, 0.0f, 0.6f, 1.0f), this.mConstantInterpolator};
    private IRemoteCallback mAnimationFinishedCallback;
    private int mAppTransitionState = 0;
    private final Interpolator mClipHorizontalInterpolator = new PathInterpolator(0.0f, 0.0f, 0.4f, 1.0f);
    private final int mClipRevealTranslationY;
    private final int mConfigShortAnimTime;
    private TimeInterpolator mConstantInterpolator = new TimeInterpolator() {
        public float getInterpolation(float input) {
            return 1.0f;
        }
    };
    private final Context mContext;
    private int mCurrentUserId = 0;
    private final Interpolator mDecelerateInterpolator;
    private final ExecutorService mDefaultExecutor = Executors.newSingleThreadExecutor();
    private AppTransitionAnimationSpec mDefaultNextAppTransitionAnimationSpec;
    /* access modifiers changed from: private */
    public final Interpolator mFastOutLinearInInterpolator;
    private final Interpolator mFastOutSlowInInterpolator;
    private final boolean mGridLayoutRecentsEnabled;
    public boolean mIgnoreShowRecentApps = false;
    private int mLastClipRevealMaxTranslation;
    private long mLastClipRevealTransitionDuration = 250;
    private String mLastClosingApp;
    private boolean mLastHadClipReveal;
    private String mLastOpeningApp;
    private int mLastUsedAppTransition = -1;
    /* access modifiers changed from: private */
    public final Interpolator mLinearOutSlowInInterpolator;
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
    /* access modifiers changed from: private */
    public final WindowManagerService mService;
    private TimeInterpolator[] mSizeBigInterpolators = {new PathInterpolator(0.44f, 0.43f, LAUNCHER_ENTER_SCALE_TIME_RATIO, 0.75f), new PathInterpolator(0.13f, 0.79f, LAUNCHER_ENTER_HIDE_TIME_RATIO, 1.0f)};
    private TimeInterpolator[] mSizeSmallInterpolators = {new PathInterpolator(0.41f, 0.38f, LAUNCHER_ENTER_SCALE_TIME_RATIO, 0.71f), new PathInterpolator(0.16f, 0.64f, 0.33f, 1.0f)};
    private final Interpolator mThumbnailFadeInInterpolator;
    private final Interpolator mThumbnailFadeOutInterpolator;
    private Rect mTmpFromClipRect = new Rect();
    private final Rect mTmpRect = new Rect();
    private Rect mTmpToClipRect = new Rect();

    public AppTransition(Context context, WindowManagerService service) {
        this.mContext = context;
        this.mService = service;
        this.mLinearOutSlowInInterpolator = AnimationUtils.loadInterpolator(context, 17563662);
        this.mFastOutLinearInInterpolator = AnimationUtils.loadInterpolator(context, 17563663);
        this.mFastOutSlowInInterpolator = AnimationUtils.loadInterpolator(context, 17563661);
        this.mConfigShortAnimTime = context.getResources().getInteger(17694720);
        this.mDecelerateInterpolator = AnimationUtils.loadInterpolator(context, 17563651);
        this.mThumbnailFadeInInterpolator = new Interpolator() {
            public float getInterpolation(float input) {
                if (input < 0.5f) {
                    return 0.0f;
                }
                return AppTransition.this.mFastOutLinearInInterpolator.getInterpolation((input - 0.5f) / 0.5f);
            }
        };
        this.mThumbnailFadeOutInterpolator = new Interpolator() {
            public float getInterpolation(float input) {
                if (input >= 0.5f) {
                    return 1.0f;
                }
                return AppTransition.this.mLinearOutSlowInInterpolator.getInterpolation(input / 0.5f);
            }
        };
        this.mClipRevealTranslationY = (int) (8.0f * this.mContext.getResources().getDisplayMetrics().density);
        this.mGridLayoutRecentsEnabled = SystemProperties.getBoolean("ro.recents.grid", false);
        this.mLowRamRecentsEnabled = ActivityManager.isLowRamDeviceStatic();
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
        if (transit != this.mNextAppTransition) {
            Flog.i(310, "set app transition from " + appTransitionToString(transit) + " to " + appTransitionToString(this.mNextAppTransition));
        }
        this.mNextAppTransition = transit;
        this.mNextAppTransitionFlags |= flags;
        setLastAppTransition(-1, null, null);
        updateBooster();
    }

    /* access modifiers changed from: package-private */
    public void setLastAppTransition(int transit, AppWindowToken openingApp, AppWindowToken closingApp) {
        this.mLastUsedAppTransition = transit;
        this.mLastOpeningApp = BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS + openingApp;
        this.mLastClosingApp = BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS + closingApp;
    }

    /* access modifiers changed from: package-private */
    public boolean isReady() {
        return this.mAppTransitionState == 1 || this.mAppTransitionState == 3;
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
        return this.mNextAppTransitionType == 5 || this.mNextAppTransitionType == 6;
    }

    /* access modifiers changed from: package-private */
    public boolean isNextThumbnailTransitionScaleUp() {
        return this.mNextAppTransitionScaleUp;
    }

    /* access modifiers changed from: package-private */
    public boolean isNextAppTransitionThumbnailUp() {
        return this.mNextAppTransitionType == 3 || this.mNextAppTransitionType == 5;
    }

    /* access modifiers changed from: package-private */
    public boolean isNextAppTransitionThumbnailDown() {
        return this.mNextAppTransitionType == 4 || this.mNextAppTransitionType == 6;
    }

    /* access modifiers changed from: package-private */
    public boolean isNextAppTransitionOpenCrossProfileApps() {
        return this.mNextAppTransitionType == 9;
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
        this.mLastClipRevealTransitionDuration = 250;
        return true;
    }

    /* access modifiers changed from: package-private */
    public int goodToGo(int transit, AppWindowToken topOpeningApp, AppWindowToken topClosingApp, ArraySet<AppWindowToken> openingApps, ArraySet<AppWindowToken> arraySet) {
        AnimationAdapter animationAdapter;
        IBinder iBinder;
        long uptimeMillis;
        AppWindowToken appWindowToken = topOpeningApp;
        AppWindowToken appWindowToken2 = topClosingApp;
        this.mNextAppTransition = -1;
        this.mNextAppTransitionFlags = 0;
        setAppTransitionState(2);
        IBinder iBinder2 = null;
        if (appWindowToken != null) {
            animationAdapter = topOpeningApp.getAnimation();
        } else {
            animationAdapter = null;
        }
        AnimationAdapter topOpeningAnim = animationAdapter;
        if (appWindowToken != null) {
            iBinder = appWindowToken.token;
        } else {
            iBinder = null;
        }
        if (appWindowToken2 != null) {
            iBinder2 = appWindowToken2.token;
        }
        IBinder iBinder3 = iBinder2;
        long durationHint = topOpeningAnim != null ? topOpeningAnim.getDurationHint() : 0;
        if (topOpeningAnim != null) {
            uptimeMillis = topOpeningAnim.getStatusBarTransitionsStartTime();
        } else {
            uptimeMillis = SystemClock.uptimeMillis();
        }
        int redoLayout = notifyAppTransitionStartingLocked(transit, iBinder, iBinder3, durationHint, uptimeMillis, 120);
        this.mService.getDefaultDisplayContentLocked().getDockedDividerController().notifyAppTransitionStarting(openingApps, transit);
        this.mIgnoreShowRecentApps = false;
        if (this.mRemoteAnimationController != null) {
            this.mRemoteAnimationController.goodToGo();
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
        if (this.mNextAppTransition == 20 && !this.mService.mOpeningApps.isEmpty()) {
            this.mIgnoreShowRecentApps = true;
            Slog.v(TAG, "freeze set mIgnoreShowRecentApps " + this.mIgnoreShowRecentApps);
        }
        int transit = this.mNextAppTransition;
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
        boolean recentsAnimRunning = this.mService.getRecentsAnimationController() != null;
        if (this.mNextAppTransition != -1 || this.mAppTransitionState == 1 || this.mAppTransitionState == 2 || recentsAnimRunning) {
            return true;
        }
        return false;
    }

    /* access modifiers changed from: package-private */
    public void registerListenerLocked(WindowManagerInternal.AppTransitionListener listener) {
        this.mListeners.add(listener);
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

    private int notifyAppTransitionStartingLocked(int transit, IBinder openToken, IBinder closeToken, long duration, long statusBarAnimationStartTime, long statusBarAnimationDuration) {
        int redoLayout = 0;
        for (int i = 0; i < this.mListeners.size(); i++) {
            redoLayout |= this.mListeners.get(i).onAppTransitionStartingLocked(transit, openToken, closeToken, duration, statusBarAnimationStartTime, statusBarAnimationDuration);
        }
        return redoLayout;
    }

    private AttributeCache.Entry getCachedAnimations(WindowManager.LayoutParams lp) {
        if (lp == null || lp.windowAnimations == 0) {
            return null;
        }
        String packageName = lp.packageName != null ? lp.packageName : PackageManagerService.PLATFORM_PACKAGE_NAME;
        int resId = lp.windowAnimations;
        if ((-16777216 & resId) == 16777216) {
            packageName = PackageManagerService.PLATFORM_PACKAGE_NAME;
        }
        return AttributeCache.instance().get(packageName, resId, R.styleable.WindowAnimation, this.mCurrentUserId);
    }

    /* access modifiers changed from: protected */
    public AttributeCache.Entry getCachedAnimations(String packageName, int resId) {
        if (packageName == null) {
            return null;
        }
        if ((-16777216 & resId) == 16777216) {
            packageName = PackageManagerService.PLATFORM_PACKAGE_NAME;
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
            return AnimationUtils.loadAnimation(context, resId2);
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
        return AnimationUtils.loadAnimation(context, resId);
    }

    private Animation loadAnimationRes(String packageName, int resId) {
        if (ResourceId.isValid(resId)) {
            AttributeCache.Entry ent = getCachedAnimations(packageName, resId);
            if (ent != null) {
                return AnimationUtils.loadAnimation(ent.context, resId);
            }
        }
        return null;
    }

    private int updateToTranslucentAnimIfNeeded(int anim, int transit) {
        if (transit == 24 && anim == 17432591) {
            return 17432594;
        }
        if (transit == 25 && anim == 17432590) {
            return 17432593;
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

    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r4v1, resolved type: android.view.animation.AlphaAnimation} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r4v2, resolved type: android.view.animation.AlphaAnimation} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r4v3, resolved type: android.view.animation.AlphaAnimation} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r6v5, resolved type: android.view.animation.AnimationSet} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r4v5, resolved type: android.view.animation.AlphaAnimation} */
    /* JADX WARNING: Multi-variable type inference failed */
    private Animation createScaleUpAnimationLocked(int transit, boolean enter, Rect containingFrame) {
        Animation alpha;
        long duration;
        int i = transit;
        getDefaultNextAppTransitionStartRect(this.mTmpRect);
        int appWidth = containingFrame.width();
        int appHeight = containingFrame.height();
        if (enter) {
            float scaleW = ((float) this.mTmpRect.width()) / ((float) appWidth);
            float scaleH = ((float) this.mTmpRect.height()) / ((float) appHeight);
            ScaleAnimation scaleAnimation = new ScaleAnimation(scaleW, 1.0f, scaleH, 1.0f, computePivot(this.mTmpRect.left, scaleW), computePivot(this.mTmpRect.top, scaleH));
            scaleAnimation.setInterpolator(this.mDecelerateInterpolator);
            Animation alpha2 = new AlphaAnimation(0.0f, 1.0f);
            alpha2.setInterpolator(this.mThumbnailFadeOutInterpolator);
            AnimationSet set = new AnimationSet(false);
            set.addAnimation(scaleAnimation);
            if (!APP_LOW_PERF_ANIM) {
                set.addAnimation(alpha2);
            }
            set.setDetachWallpaper(true);
            alpha = set;
        } else if (i == 14 || i == 15) {
            alpha = new AlphaAnimation(1.0f, 0.0f);
            alpha.setDetachWallpaper(true);
        } else {
            alpha = new AlphaAnimation(1.0f, 1.0f);
        }
        switch (i) {
            case 6:
            case 7:
                duration = (long) this.mConfigShortAnimTime;
                break;
            default:
                duration = 250;
                break;
        }
        if (APP_LOW_PERF_ANIM && 12 == i) {
            duration = 150;
        }
        alpha.setDuration(duration);
        alpha.setFillAfter(true);
        alpha.setInterpolator(this.mDecelerateInterpolator);
        alpha.initialize(appWidth, appHeight, appWidth, appHeight);
        return alpha;
    }

    private static float computeFloatPivot(float startPos, float finalScale) {
        float denom = finalScale - 1.0f;
        if (Math.abs(denom) < 1.0E-4f) {
            return startPos;
        }
        return (-startPos) / denom;
    }

    private float[] adjustPivotsInLazyMode(float originPivotX, float originPivotY, int iconWidth, int iconHeight, WindowState win) {
        int lazyMode = this.mService.getLazyMode();
        float[] pivots = {originPivotX, originPivotY};
        if (lazyMode != 0) {
            Rect bounds = win.getBounds();
            if (bounds == null) {
                Slog.w(TAG, "bounds is null! return.");
                return pivots;
            }
            Slog.d(TAG, "app exit to launcher, lazymode bounds = " + bounds);
            float winStartX = 0.0f;
            float winStartY = (1.0f - this.LAZY_MODE_WIN_SCALE_FACTOR) * ((float) bounds.height());
            if (lazyMode == 2) {
                winStartX = (1.0f - this.LAZY_MODE_WIN_SCALE_FACTOR) * ((float) bounds.width());
            }
            float compensationX = this.LAZY_MODE_COMP_FACTOR * ((float) iconWidth);
            float compensationY = this.LAZY_MODE_COMP_FACTOR * ((float) iconHeight);
            if (lazyMode == 1) {
                compensationX = -compensationX;
            }
            pivots[0] = ((this.LAZY_MODE_WIN_SCALE_FACTOR * originPivotX) + winStartX) - compensationX;
            pivots[1] = ((this.LAZY_MODE_WIN_SCALE_FACTOR * originPivotY) + winStartY) - compensationY;
        } else {
            int i = iconWidth;
            int i2 = iconHeight;
        }
        return pivots;
    }

    /* access modifiers changed from: package-private */
    public Animation createAppExitToIconAnimation(AppWindowToken atoken, int containingHeight, int iconWidth, int iconHeight, float originPivotX, float originPivotY, Bitmap icon) {
        int finalIconHeight;
        int finalIconHeight2;
        TimeInterpolator[] sizeXInterpolators;
        TimeInterpolator[] sizeYInterpolators;
        int finalIconWidth = iconWidth;
        int i = iconHeight;
        Bitmap bitmap = icon;
        if (atoken == null) {
            Slog.w(TAG, "create app exit animation find no app window token!");
            return null;
        }
        WindowState window = atoken.findMainWindow();
        if (window == null) {
            Slog.w(TAG, "create app exit animation find no app main window!");
            return null;
        }
        float[] pivots = adjustPivotsInLazyMode(originPivotX, originPivotY, finalIconWidth, i, window);
        float pivotXAdj = pivots[0];
        float pivotYAdj = pivots[1];
        Rect winDecorFrame = window.mDecorFrame;
        if (winDecorFrame == null) {
            Slog.w(TAG, "create app exit animation find no app window frame!");
            return null;
        }
        Rect winDisplayFrame = window.mDisplayFrame;
        if (winDisplayFrame == null) {
            Slog.w(TAG, "create app exit animation find no app window displayFrame!");
            return null;
        }
        int winWidth = winDecorFrame.right - winDecorFrame.left;
        int winHeight = ((containingHeight - winDecorFrame.top) - winDisplayFrame.top) - (containingHeight - winDisplayFrame.bottom);
        if (winWidth <= 0) {
            float f = pivotXAdj;
            float f2 = pivotYAdj;
            Rect rect = winDecorFrame;
            int i2 = i;
            Bitmap bitmap2 = bitmap;
            WindowState windowState = window;
            Rect rect2 = winDisplayFrame;
            float pivotXAdj2 = winHeight;
            int winHeight2 = finalIconWidth;
        } else if (winHeight <= 0) {
            float[] fArr = pivots;
            float f3 = pivotXAdj;
            float f4 = pivotYAdj;
            Rect rect3 = winDecorFrame;
            int i3 = i;
            Bitmap bitmap3 = bitmap;
            WindowState windowState2 = window;
            Rect rect4 = winDisplayFrame;
            float pivotXAdj3 = winHeight;
            int winHeight3 = finalIconWidth;
        } else {
            int winWidth2 = winWidth < 0 ? -winWidth : winWidth;
            int winHeight4 = winHeight < 0 ? -winHeight : winHeight;
            boolean isHorizontal = winWidth2 > winHeight4;
            float middleYRatio = 0.44f;
            float middleXRatio = isHorizontal ? 0.54f : 0.44f;
            if (!isHorizontal) {
                middleYRatio = 0.54f;
            }
            float[] fArr2 = pivots;
            Rect rect5 = winDisplayFrame;
            float middleX = 1.0f - ((((float) (winWidth2 - finalIconWidth)) * middleXRatio) / ((float) winWidth2));
            float middleY = 1.0f - ((((float) (winHeight4 - i)) * middleYRatio) / ((float) winHeight4));
            int finalIconHeight3 = i;
            int finalIconWidth2 = finalIconWidth;
            boolean isHorizontal2 = isHorizontal;
            if (this.mService.mExitFlag == 1) {
                finalIconHeight = (int) (((float) i) * 0.4f);
                finalIconHeight2 = (int) (((float) finalIconWidth) * 0.4f);
            } else {
                finalIconHeight = finalIconHeight3;
                finalIconHeight2 = finalIconWidth2;
            }
            float toX = ((float) finalIconHeight2) / ((float) winWidth2);
            float middleY2 = middleY;
            float toY = ((float) finalIconHeight) / ((float) winHeight4);
            float middleX2 = middleX;
            Slog.d(TAG, "now set the app exit scale animation for: " + window + ", [winWidth, winHeight] = [" + winWidth2 + ", " + winHeight4 + "][originPivotX, originPivotY] = [" + originPivotX + ", " + originPivotY + "][fromX, fromY] = [" + 1.0f + ", " + 1.0f + "][toX, toY] = [" + toX + ", " + toY + "]");
            float iconLeft = pivotXAdj - (((float) finalIconHeight2) / 2.0f);
            float iconTop = pivotYAdj - (((float) finalIconHeight) / 2.0f);
            float f5 = pivotXAdj;
            float pivotX = computeFloatPivot(iconLeft, toX) + 0.5f;
            float pivotY = computeFloatPivot(iconTop, toY) + 0.5f;
            float f6 = pivotYAdj;
            Rect winFrame = window.mFrame;
            if (winFrame == null) {
                float f7 = iconLeft;
                int i4 = finalIconHeight2;
                Slog.w(TAG, "create app exit animation find no app window frame!");
                return null;
            }
            int i5 = finalIconHeight2;
            int offsetY = winDecorFrame.top > winFrame.top ? winDecorFrame.top : winFrame.top;
            float pivotY2 = pivotY - (((float) offsetY) * toY);
            Rect rect6 = winFrame;
            Rect rect7 = winDecorFrame;
            StringBuilder sb = new StringBuilder();
            int i6 = offsetY;
            sb.append("Retrieved [pivotX, pivotY] = [");
            sb.append(pivotX);
            sb.append(", ");
            sb.append(pivotY2);
            sb.append("]");
            Slog.d(TAG, sb.toString());
            long duration = 350;
            if (APP_LOW_PERF_ANIM) {
                duration = 200;
            }
            float f8 = iconTop;
            AnimationSet appExitToIconAnimation = new AnimationSet(false);
            AlphaAnimation alphaAnim = new AlphaAnimation(0.0f, 1.0f);
            alphaAnim.setDuration(duration);
            alphaAnim.setFillEnabled(true);
            alphaAnim.setFillBefore(true);
            alphaAnim.setFillAfter(true);
            float[] alphaOutValues = {1.0f, 1.0f, 0.0f, 0.0f};
            int i7 = finalIconHeight;
            int winHeight5 = winHeight4;
            WindowState window2 = window;
            PhaseInterpolator alphaInterpolator = new PhaseInterpolator(new float[]{0.0f, 0.16f, 0.32f, 1.0f}, alphaOutValues, this.mAlphaInterpolators);
            alphaAnim.setInterpolator(alphaInterpolator);
            PhaseInterpolator phaseInterpolator = alphaInterpolator;
            float[] scaleInValues = {0.0f, 0.16f, 1.0f};
            float[] fArr3 = alphaOutValues;
            float[] alphaOutValues2 = {1.0f, middleX2, toX};
            float[] scaleOutValuesY = {1.0f, middleY2, toY};
            if (isHorizontal2) {
                sizeXInterpolators = this.mSizeBigInterpolators;
            } else {
                sizeXInterpolators = this.mSizeSmallInterpolators;
            }
            if (isHorizontal2) {
                sizeYInterpolators = this.mSizeSmallInterpolators;
            } else {
                sizeYInterpolators = this.mSizeBigInterpolators;
            }
            float f9 = toX;
            PhaseInterpolator interpolatorX = new PhaseInterpolator(scaleInValues, alphaOutValues2, sizeXInterpolators);
            TimeInterpolator[] timeInterpolatorArr = sizeXInterpolators;
            PhaseInterpolator interpolatorY = new PhaseInterpolator(scaleInValues, scaleOutValuesY, sizeYInterpolators);
            float f10 = pivotX;
            float f11 = pivotY2;
            ScaleAnimation animX = new ScaleAnimation(0.0f, 1.0f, 1.0f, 1.0f, f10, f11);
            float[] fArr4 = scaleInValues;
            TimeInterpolator[] timeInterpolatorArr2 = sizeYInterpolators;
            ScaleAnimation animX2 = animX;
            animX2.setFillEnabled(true);
            animX2.setFillBefore(true);
            animX2.setFillAfter(true);
            animX2.setDuration(duration);
            animX2.setInterpolator(interpolatorX);
            ScaleAnimation scaleAnimation = new ScaleAnimation(1.0f, 1.0f, 0.0f, 1.0f, f10, f11);
            float f12 = pivotX;
            scaleAnimation.setFillEnabled(true);
            scaleAnimation.setFillBefore(true);
            scaleAnimation.setFillAfter(true);
            scaleAnimation.setDuration(duration);
            scaleAnimation.setInterpolator(interpolatorY);
            appExitToIconAnimation.addAnimation(alphaAnim);
            appExitToIconAnimation.addAnimation(animX2);
            appExitToIconAnimation.addAnimation(scaleAnimation);
            appExitToIconAnimation.setZAdjustment(1);
            PhaseInterpolator phaseInterpolator2 = interpolatorY;
            int winHeight6 = winHeight5;
            float roundx = ((float) Math.min(winWidth2, winHeight6)) * LAUNCHER_ENTER_ALPHA_TIME_RATIO;
            ScaleAnimation scaleAnimation2 = animX2;
            int i8 = winHeight6;
            WindowState window3 = window2;
            int i9 = winWidth2;
            window3.mWinAnimator.setWindowClipFlag(1);
            float roundy = roundx;
            window3.mWinAnimator.setWindowClipRound(roundx, roundy);
            float f13 = roundx;
            if (!atoken.mShouldDrawIcon) {
                window3.mWinAnimator.setWindowClipFlag(2);
            }
            Bitmap bitmap4 = icon;
            if (bitmap4 != null) {
                WindowState windowState3 = window3;
                float f14 = roundy;
                window3.mWinAnimator.setWindowClipIcon(iconWidth, iconHeight, bitmap4);
            } else {
                WindowState windowState4 = window3;
                float f15 = roundy;
                int i10 = iconWidth;
                float roundy2 = iconHeight;
            }
            return appExitToIconAnimation;
        }
        return null;
    }

    /* access modifiers changed from: package-private */
    public Animation createLauncherEnterAnimation(AppWindowToken atoken, int containingHeight, int iconWidth, int iconHeight, float originPivotX, float originPivotY) {
        float pivotX;
        int i = iconWidth;
        int i2 = iconHeight;
        if (atoken == null) {
            Slog.w(TAG, "create launcher enter animation find no app window token!");
            return null;
        }
        WindowState window = atoken.findMainWindow();
        if (window == null) {
            Slog.w(TAG, "create launcher enter animation find no app main window!");
            return null;
        }
        Rect winDecorFrame = window.mDecorFrame;
        Rect winVisibleFrame = window.mVisibleFrame;
        if (winDecorFrame == null) {
            Rect rect = winDecorFrame;
        } else if (winVisibleFrame == null) {
            WindowState windowState = window;
            Rect rect2 = winDecorFrame;
        } else {
            int winWidth = winDecorFrame.right - winDecorFrame.left;
            int winHeight = containingHeight - winDecorFrame.top;
            if (winWidth <= 0) {
                Rect rect3 = winDecorFrame;
            } else if (winHeight <= 0) {
                WindowState windowState2 = window;
                Rect rect4 = winDecorFrame;
            } else {
                int winWidth2 = winWidth < 0 ? -winWidth : winWidth;
                int winHeight2 = winHeight < 0 ? -winHeight : winHeight;
                float toX = ((float) i) / ((float) winWidth2);
                float toY = ((float) i2) / ((float) winHeight2);
                float iconTop = originPivotY - ((float) (i2 / 2));
                float pivotX2 = computePivot((int) (originPivotX - ((float) (i / 2))), toX);
                float pivotY = computePivot((int) iconTop, toY);
                float f = iconTop;
                Rect winFrame = window.mFrame;
                if (winFrame == null) {
                    WindowState windowState3 = window;
                    Slog.w(TAG, "create launcher enter animation find no app window frame!");
                    return null;
                }
                int offsetY = winDecorFrame.top > winFrame.top ? winDecorFrame.top : winFrame.top;
                float pivotY2 = pivotY - (((float) offsetY) * toY);
                if (originPivotX < 0.0f || originPivotY < 0.0f) {
                    pivotX = ((float) winWidth2) / 2.0f;
                    Rect rect5 = winFrame;
                    pivotY2 = ((float) winHeight2) / 2.0f;
                } else {
                    Rect rect6 = winFrame;
                    pivotX = pivotX2;
                }
                long duration = 350;
                if (this.mService.mExitIconWidth < 0 || this.mService.mExitIconHeight < 0 || this.mService.mExitIconBitmap == null) {
                    duration = 200;
                }
                long duration2 = duration;
                AnimationSet launcherEnterAnimation = new AnimationSet(false);
                ScaleAnimation scaleAnimation = new ScaleAnimation(0.93f, 1.0f, 0.93f, 1.0f, pivotX, pivotY2);
                scaleAnimation.setFillEnabled(true);
                scaleAnimation.setFillBefore(true);
                scaleAnimation.setFillAfter(true);
                int i3 = offsetY;
                float f2 = pivotX;
                scaleAnimation.setStartOffset((long) (((float) duration2) * LAUNCHER_ENTER_HIDE_TIME_RATIO));
                scaleAnimation.setDuration((long) (((float) duration2) * LAUNCHER_ENTER_SCALE_TIME_RATIO));
                float f3 = pivotY2;
                Rect rect7 = winDecorFrame;
                scaleAnimation.setInterpolator(new PathInterpolator(LAUNCHER_ENTER_ALPHA_TIME_RATIO, 0.0f, 0.1f, 1.0f));
                launcherEnterAnimation.addAnimation(scaleAnimation);
                AlphaAnimation alphaAnimation = new AlphaAnimation(0.0f, 1.0f);
                alphaAnimation.setFillEnabled(true);
                alphaAnimation.setFillBefore(true);
                alphaAnimation.setFillAfter(true);
                alphaAnimation.setDuration((long) (((float) duration2) * LAUNCHER_ENTER_ALPHA_TIME_RATIO));
                alphaAnimation.setStartOffset((long) (((float) duration2) * LAUNCHER_ENTER_HIDE_TIME_RATIO));
                alphaAnimation.setInterpolator(new LinearInterpolator());
                launcherEnterAnimation.addAnimation(alphaAnimation);
                launcherEnterAnimation.setDetachWallpaper(true);
                launcherEnterAnimation.setZAdjustment(0);
                return launcherEnterAnimation;
            }
            return null;
        }
        Slog.w(TAG, "create launcher enter animation find no app window frame!");
        return null;
    }

    private void getDefaultNextAppTransitionStartRect(Rect rect) {
        if (this.mDefaultNextAppTransitionAnimationSpec == null || this.mDefaultNextAppTransitionAnimationSpec.rect == null) {
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
            return 250;
        }
        return (long) (250.0f + (170.0f * Math.max(Math.abs(translationX) / ((float) displayFrame.width()), Math.abs(translationY) / ((float) displayFrame.height()))));
    }

    /* JADX WARNING: type inference failed for: r5v3, types: [android.view.animation.Animation] */
    /* JADX WARNING: type inference failed for: r7v17, types: [com.android.server.wm.animation.ClipRectTBAnimation, android.view.animation.Animation] */
    /* JADX WARNING: Multi-variable type inference failed */
    /* JADX WARNING: Unknown variable types count: 1 */
    private Animation createClipRevealAnimationLocked(int transit, boolean enter, Rect appFrame, Rect displayFrame) {
        long duration;
        boolean z;
        Animation anim;
        int translationYCorrection;
        int clipStartY;
        int translationYCorrection2;
        Interpolator interpolator;
        int i;
        int i2 = transit;
        Rect rect = appFrame;
        if (enter) {
            int appWidth = appFrame.width();
            int appHeight = appFrame.height();
            getDefaultNextAppTransitionStartRect(this.mTmpRect);
            float t = 0.0f;
            if (appHeight > 0) {
                t = ((float) this.mTmpRect.top) / ((float) displayFrame.height());
            }
            int translationY = this.mClipRevealTranslationY + ((int) ((((float) displayFrame.height()) / 7.0f) * t));
            int translationX = 0;
            int translationYCorrection3 = translationY;
            int centerX = this.mTmpRect.centerX();
            int centerY = this.mTmpRect.centerY();
            int halfWidth = this.mTmpRect.width() / 2;
            int halfHeight = this.mTmpRect.height() / 2;
            int clipStartX = (centerX - halfWidth) - rect.left;
            int clipStartY2 = (centerY - halfHeight) - rect.top;
            boolean cutOff = false;
            if (rect.top > centerY - halfHeight) {
                cutOff = true;
                translationYCorrection = 0;
                translationYCorrection2 = (centerY - halfHeight) - rect.top;
                clipStartY = 0;
            } else {
                translationYCorrection2 = translationY;
                translationYCorrection = translationYCorrection3;
                clipStartY = clipStartY2;
            }
            if (rect.left > centerX - halfWidth) {
                translationX = (centerX - halfWidth) - rect.left;
                clipStartX = 0;
                cutOff = true;
            }
            if (rect.right < centerX + halfWidth) {
                translationX = (centerX + halfWidth) - rect.right;
                clipStartX = appWidth - this.mTmpRect.width();
                cutOff = true;
            }
            int clipStartX2 = clipStartX;
            boolean cutOff2 = cutOff;
            int translationX2 = translationX;
            long duration2 = calculateClipRevealTransitionDuration(cutOff2, (float) translationX2, (float) translationYCorrection2, displayFrame);
            ? clipRectLRAnimation = new ClipRectLRAnimation(clipStartX2, this.mTmpRect.width() + clipStartX2, 0, appWidth);
            clipRectLRAnimation.setInterpolator(this.mClipHorizontalInterpolator);
            int clipStartX3 = clipStartX2;
            clipRectLRAnimation.setDuration((long) (((float) duration2) / 2.5f));
            TranslateAnimation translate = new TranslateAnimation((float) translationX2, 0.0f, (float) translationYCorrection2, 0.0f);
            if (cutOff2) {
                interpolator = TOUCH_RESPONSE_INTERPOLATOR;
            } else {
                interpolator = this.mLinearOutSlowInInterpolator;
            }
            translate.setInterpolator(interpolator);
            translate.setDuration(duration2);
            int i3 = clipStartX3;
            int translationX3 = translationX2;
            boolean cutOff3 = cutOff2;
            int appHeight2 = appHeight;
            ? clipRectTBAnimation = new ClipRectTBAnimation(clipStartY, clipStartY + this.mTmpRect.height(), 0, appHeight, translationYCorrection, 0, this.mLinearOutSlowInInterpolator);
            clipRectTBAnimation.setInterpolator(TOUCH_RESPONSE_INTERPOLATOR);
            long duration3 = duration2;
            clipRectTBAnimation.setDuration(duration3);
            AlphaAnimation alpha = new AlphaAnimation(0.5f, 1.0f);
            alpha.setDuration(duration3 / 4);
            alpha.setInterpolator(this.mLinearOutSlowInInterpolator);
            AnimationSet set = new AnimationSet(false);
            set.addAnimation(clipRectLRAnimation);
            set.addAnimation(clipRectTBAnimation);
            set.addAnimation(translate);
            set.addAnimation(alpha);
            set.setZAdjustment(1);
            set.initialize(appWidth, appHeight2, appWidth, appHeight2);
            Animation anim2 = set;
            this.mLastHadClipReveal = true;
            this.mLastClipRevealTransitionDuration = duration3;
            if (cutOff3) {
                int i4 = appHeight2;
                TranslateAnimation translateAnimation = translate;
                i = Math.max(Math.abs(translationYCorrection2), Math.abs(translationX3));
            } else {
                TranslateAnimation translateAnimation2 = translate;
                int appHeight3 = translationX3;
                i = 0;
            }
            this.mLastClipRevealMaxTranslation = i;
            return anim2;
        }
        switch (i2) {
            case 6:
            case 7:
                duration = (long) this.mConfigShortAnimTime;
                break;
            default:
                duration = 250;
                break;
        }
        if (i2 == 14 || i2 == 15) {
            anim = new AlphaAnimation(1.0f, 0.0f);
            z = true;
            anim.setDetachWallpaper(true);
        } else {
            anim = new AlphaAnimation(1.0f, 1.0f);
            z = true;
        }
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
        switch (transit) {
            case 6:
            case 7:
                duration = this.mConfigShortAnimTime;
                break;
            default:
                duration = DEFAULT_APP_TRANSITION_DURATION;
                break;
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
        int thumbnailSize = this.mService.mContext.getResources().getDimensionPixelSize(17104986);
        Drawable drawable = this.mService.mContext.getDrawable(thumbnailDrawableRes);
        drawable.setBounds((width - thumbnailSize) / 2, (height - thumbnailSize) / 2, (width + thumbnailSize) / 2, (height + thumbnailSize) / 2);
        drawable.setTint(this.mContext.getColor(17170443));
        drawable.draw(canvas);
        picture.endRecording();
        return Bitmap.createBitmap(picture).createGraphicBufferHandle();
    }

    /* access modifiers changed from: package-private */
    public Animation createCrossProfileAppsThumbnailAnimationLocked(Rect appRect) {
        return prepareThumbnailAnimationWithDuration(loadAnimationRes(PackageManagerService.PLATFORM_PACKAGE_NAME, 17432611), appRect.width(), appRect.height(), 0, null);
    }

    /* access modifiers changed from: package-private */
    public Animation createThumbnailAspectScaleAnimationLocked(Rect appRect, Rect contentInsets, GraphicBuffer thumbnailHeader, int taskId, int uiMode, int orientation) {
        float pivotY;
        float pivotX;
        float toY;
        float fromY;
        float fromX;
        float fromY2;
        int appWidth;
        AnimationSet set;
        float fromY3;
        long j;
        float fromY4;
        Rect rect = appRect;
        Rect rect2 = contentInsets;
        int thumbWidthI = thumbnailHeader.getWidth();
        float thumbWidth = thumbWidthI > 0 ? (float) thumbWidthI : 1.0f;
        int thumbHeightI = thumbnailHeader.getHeight();
        int appWidth2 = appRect.width();
        float scaleW = ((float) appWidth2) / thumbWidth;
        getNextAppTransitionStartRect(taskId, this.mTmpRect);
        if (shouldScaleDownThumbnailTransition(uiMode, orientation)) {
            float f = (float) this.mTmpRect.left;
            float fromY5 = (float) this.mTmpRect.top;
            fromY = (((float) (this.mTmpRect.width() / 2)) * (scaleW - 1.0f)) + ((float) rect.left);
            toY = (((float) (appRect.height() / 2)) * (1.0f - (1.0f / scaleW))) + ((float) rect.top);
            float width = (float) (this.mTmpRect.width() / 2);
            float pivotX2 = ((float) (appRect.height() / 2)) / scaleW;
            if (this.mGridLayoutRecentsEnabled) {
                fromY5 -= (float) thumbHeightI;
                toY -= ((float) thumbHeightI) * scaleW;
            }
            fromY2 = fromY5;
            pivotY = pivotX2;
            fromX = f;
            pivotX = width;
        } else {
            fromX = (float) this.mTmpRect.left;
            pivotX = 0.0f;
            pivotY = 0.0f;
            fromY2 = (float) this.mTmpRect.top;
            fromY = (float) rect.left;
            toY = (float) rect.top;
        }
        float pivotY2 = toY;
        long duration = getAspectScaleDuration();
        Interpolator interpolator = getAspectScaleInterpolator();
        long duration2 = duration;
        if (this.mNextAppTransitionScaleUp) {
            ScaleAnimation scaleAnimation = new ScaleAnimation(1.0f, scaleW, 1.0f, scaleW, pivotX, pivotY);
            scaleAnimation.setInterpolator(interpolator);
            long duration3 = duration2;
            scaleAnimation.setDuration(duration3);
            float f2 = thumbWidth;
            appWidth = appWidth2;
            Animation alpha = new AlphaAnimation(1.0f, 0.0f);
            alpha.setInterpolator(this.mNextAppTransition == 19 ? THUMBNAIL_DOCK_INTERPOLATOR : this.mThumbnailFadeOutInterpolator);
            if (this.mNextAppTransition == 19) {
                j = duration3 / 2;
            } else {
                j = duration3;
            }
            alpha.setDuration(j);
            Animation translate = createCurvedMotion(fromX, fromY, fromY2, pivotY2);
            translate.setInterpolator(interpolator);
            translate.setDuration(duration3);
            this.mTmpFromClipRect.set(0, 0, thumbWidthI, thumbHeightI);
            this.mTmpToClipRect.set(appRect);
            this.mTmpToClipRect.offsetTo(0, 0);
            this.mTmpToClipRect.right = (int) (((float) this.mTmpToClipRect.right) / scaleW);
            this.mTmpToClipRect.bottom = (int) (((float) this.mTmpToClipRect.bottom) / scaleW);
            Rect rect3 = contentInsets;
            if (rect3 != null) {
                int i = thumbWidthI;
                int i2 = thumbHeightI;
                fromY4 = fromY2;
                this.mTmpToClipRect.inset((int) (((float) (-rect3.left)) * scaleW), (int) (((float) (-rect3.top)) * scaleW), (int) (((float) (-rect3.right)) * scaleW), (int) (((float) (-rect3.bottom)) * scaleW));
            } else {
                fromY4 = fromY2;
                int i3 = thumbWidthI;
                int i4 = thumbHeightI;
            }
            Animation clipAnim = new ClipRectAnimation(this.mTmpFromClipRect, this.mTmpToClipRect);
            clipAnim.setInterpolator(interpolator);
            clipAnim.setDuration(duration3);
            AnimationSet set2 = new AnimationSet(false);
            set2.addAnimation(scaleAnimation);
            if (!this.mGridLayoutRecentsEnabled) {
                set2.addAnimation(alpha);
            }
            set2.addAnimation(translate);
            set2.addAnimation(clipAnim);
            set = set2;
            long j2 = duration3;
            fromY3 = fromY4;
        } else {
            int i5 = thumbWidthI;
            float f3 = thumbWidth;
            int i6 = thumbHeightI;
            appWidth = appWidth2;
            long duration4 = duration2;
            ScaleAnimation scaleAnimation2 = new ScaleAnimation(scaleW, 1.0f, scaleW, 1.0f, pivotX, pivotY);
            scaleAnimation2.setInterpolator(interpolator);
            scaleAnimation2.setDuration(duration4);
            Animation alpha2 = new AlphaAnimation(0.0f, 1.0f);
            alpha2.setInterpolator(this.mThumbnailFadeInInterpolator);
            alpha2.setDuration(duration4);
            fromY3 = fromY2;
            Animation translate2 = createCurvedMotion(fromY, fromX, pivotY2, fromY3);
            translate2.setInterpolator(interpolator);
            translate2.setDuration(duration4);
            set = new AnimationSet(false);
            set.addAnimation(scaleAnimation2);
            if (!this.mGridLayoutRecentsEnabled) {
                set.addAnimation(alpha2);
            }
            set.addAnimation(translate2);
        }
        float f4 = pivotY2;
        float f5 = fromY3;
        float f6 = fromX;
        float f7 = fromY;
        Interpolator interpolator2 = interpolator;
        return prepareThumbnailAnimationWithDuration(set, appWidth, appRect.height(), 0, null);
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

    /* access modifiers changed from: package-private */
    public Animation createAspectScaledThumbnailEnterExitAnimationLocked(int thumbTransitState, int uiMode, int orientation, int transit, Rect containingFrame, Rect contentInsets, Rect surfaceInsets, Rect stableInsets, boolean freeform, int taskId) {
        Animation a;
        Animation clipAnim;
        Animation translateAnim;
        Animation animation;
        Animation translateAnim2;
        Animation a2;
        int i = transit;
        Rect rect = containingFrame;
        Rect rect2 = contentInsets;
        Rect rect3 = surfaceInsets;
        Rect rect4 = stableInsets;
        int i2 = taskId;
        int appWidth = containingFrame.width();
        int appHeight = containingFrame.height();
        getDefaultNextAppTransitionStartRect(this.mTmpRect);
        int thumbWidthI = this.mTmpRect.width();
        float thumbWidth = thumbWidthI > 0 ? (float) thumbWidthI : 1.0f;
        int thumbHeightI = this.mTmpRect.height();
        float thumbHeight = thumbHeightI > 0 ? (float) thumbHeightI : 1.0f;
        int thumbStartX = (this.mTmpRect.left - rect.left) - rect2.left;
        int thumbStartY = this.mTmpRect.top - rect.top;
        switch (thumbTransitState) {
            case 0:
            case 3:
                boolean scaleUp = thumbTransitState == 0;
                if (!freeform || !scaleUp) {
                    if (!freeform) {
                        AnimationSet set = new AnimationSet(true);
                        this.mTmpFromClipRect.set(rect);
                        this.mTmpToClipRect.set(rect);
                        this.mTmpFromClipRect.offsetTo(0, 0);
                        this.mTmpToClipRect.offsetTo(0, 0);
                        this.mTmpFromClipRect.inset(rect2);
                        this.mNextAppTransitionInsets.set(rect2);
                        if (shouldScaleDownThumbnailTransition(uiMode, orientation)) {
                            float scale = thumbWidth / ((float) ((appWidth - rect2.left) - rect2.right));
                            if (!this.mGridLayoutRecentsEnabled) {
                                this.mTmpFromClipRect.bottom = this.mTmpFromClipRect.top + ((int) (thumbHeight / scale));
                            }
                            this.mNextAppTransitionInsets.set(rect2);
                            ScaleAnimation scaleAnimation = new ScaleAnimation(scaleUp ? scale : 1.0f, scaleUp ? 1.0f : scale, scaleUp ? scale : 1.0f, scaleUp ? 1.0f : scale, ((float) containingFrame.width()) / 2.0f, (((float) containingFrame.height()) / 2.0f) + ((float) rect2.top));
                            float targetX = (float) (this.mTmpRect.left - rect.left);
                            float x = (((float) containingFrame.width()) / 2.0f) - ((((float) containingFrame.width()) / 2.0f) * scale);
                            float targetY = (float) (this.mTmpRect.top - rect.top);
                            float y = (((float) containingFrame.height()) / 2.0f) - ((((float) containingFrame.height()) / 2.0f) * scale);
                            if (!this.mLowRamRecentsEnabled || rect2.top != 0 || !scaleUp) {
                            } else {
                                float f = scale;
                                this.mTmpFromClipRect.top += rect4.top;
                                y += (float) rect4.top;
                            }
                            float scale2 = targetX - x;
                            float startY = targetY - y;
                            if (scaleUp) {
                                float f2 = targetX;
                                float f3 = x;
                                animation = new ClipRectAnimation(this.mTmpFromClipRect, this.mTmpToClipRect);
                            } else {
                                float f4 = x;
                                animation = new ClipRectAnimation(this.mTmpToClipRect, this.mTmpFromClipRect);
                            }
                            Animation clipAnim2 = animation;
                            if (scaleUp) {
                                translateAnim2 = createCurvedMotion(scale2, 0.0f, startY - ((float) rect2.top), 0.0f);
                            } else {
                                translateAnim2 = createCurvedMotion(0.0f, scale2, 0.0f, startY - ((float) rect2.top));
                            }
                            set.addAnimation(clipAnim2);
                            set.addAnimation(scaleAnimation);
                            set.addAnimation(translateAnim2);
                        } else {
                            this.mTmpFromClipRect.bottom = this.mTmpFromClipRect.top + thumbHeightI;
                            this.mTmpFromClipRect.right = this.mTmpFromClipRect.left + thumbWidthI;
                            if (scaleUp) {
                                clipAnim = new ClipRectAnimation(this.mTmpFromClipRect, this.mTmpToClipRect);
                            } else {
                                clipAnim = new ClipRectAnimation(this.mTmpToClipRect, this.mTmpFromClipRect);
                            }
                            if (scaleUp) {
                                translateAnim = createCurvedMotion((float) thumbStartX, 0.0f, (float) (thumbStartY - rect2.top), 0.0f);
                            } else {
                                translateAnim = createCurvedMotion(0.0f, (float) thumbStartX, 0.0f, (float) (thumbStartY - rect2.top));
                            }
                            set.addAnimation(clipAnim);
                            set.addAnimation(translateAnim);
                        }
                        a = set;
                        a.setZAdjustment(1);
                        break;
                    } else {
                        a2 = createAspectScaledThumbnailExitFreeformAnimationLocked(rect, rect3, i2);
                    }
                } else {
                    a2 = createAspectScaledThumbnailEnterFreeformAnimationLocked(rect, rect3, i2);
                }
                a = a2;
                break;
            case 1:
                if (i != 14) {
                    a = new AlphaAnimation(1.0f, 1.0f);
                    Animation animation2 = a;
                    break;
                } else {
                    a = new AlphaAnimation(1.0f, 0.0f);
                    Animation animation3 = a;
                    break;
                }
            case 2:
                if (i != 14) {
                    a = new AlphaAnimation(1.0f, 1.0f);
                    Animation animation4 = a;
                    break;
                } else {
                    a = new AlphaAnimation(0.0f, 1.0f);
                    Animation animation5 = a;
                    break;
                }
            default:
                int i3 = thumbHeightI;
                int i4 = thumbStartX;
                int i5 = thumbWidthI;
                throw new RuntimeException("Invalid thumbnail transition state");
        }
        int i6 = thumbHeightI;
        int i7 = thumbStartX;
        int i8 = thumbWidthI;
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
        Rect rect = sourceFrame;
        Rect rect2 = destFrame;
        Rect rect3 = surfaceInsets;
        float sourceWidth = (float) sourceFrame.width();
        float sourceHeight = (float) sourceFrame.height();
        float destWidth = (float) destFrame.width();
        float destHeight = (float) destFrame.height();
        float scaleH = enter ? sourceWidth / destWidth : destWidth / sourceWidth;
        float scaleV = enter ? sourceHeight / destHeight : destHeight / sourceHeight;
        AnimationSet set = new AnimationSet(true);
        int i = 0;
        int surfaceInsetsH = rect3 == null ? 0 : rect3.left + rect3.right;
        if (rect3 != null) {
            i = rect3.top + rect3.bottom;
        }
        int surfaceInsetsV = i;
        float scaleHCenter = ((enter ? destWidth : sourceWidth) + ((float) surfaceInsetsH)) / 2.0f;
        float scaleVCenter = ((enter ? destHeight : sourceHeight) + ((float) surfaceInsetsV)) / 2.0f;
        if (enter) {
            int i2 = surfaceInsetsV;
            int i3 = surfaceInsetsH;
            scale = new ScaleAnimation(scaleH, 1.0f, scaleV, 1.0f, scaleHCenter, scaleVCenter);
        } else {
            int i4 = surfaceInsetsH;
            scale = new ScaleAnimation(1.0f, scaleH, 1.0f, scaleV, scaleHCenter, scaleVCenter);
        }
        int sourceHCenter = rect.left + (sourceFrame.width() / 2);
        int sourceVCenter = rect.top + (sourceFrame.height() / 2);
        int destHCenter = rect2.left + (destFrame.width() / 2);
        int destVCenter = rect2.top + (destFrame.height() / 2);
        int fromX = enter ? sourceHCenter - destHCenter : destHCenter - sourceHCenter;
        int fromY = enter ? sourceVCenter - destVCenter : destVCenter - sourceVCenter;
        int i5 = destVCenter;
        if (enter) {
            float f = scaleVCenter;
            float f2 = sourceWidth;
            float f3 = sourceHeight;
            translation = new TranslateAnimation((float) fromX, 0.0f, (float) fromY, 0.0f);
        } else {
            float f4 = sourceWidth;
            float f5 = sourceHeight;
            translation = new TranslateAnimation(0.0f, (float) fromX, 0.0f, (float) fromY);
        }
        set.addAnimation(scale);
        set.addAnimation(translation);
        final IRemoteCallback callback = this.mAnimationFinishedCallback;
        if (callback != null) {
            set.setAnimationListener(new Animation.AnimationListener() {
                public void onAnimationStart(Animation animation) {
                }

                public void onAnimationEnd(Animation animation) {
                    AppTransition.this.mService.mH.obtainMessage(26, callback).sendToTarget();
                }

                public void onAnimationRepeat(Animation animation) {
                }
            });
        }
        return set;
    }

    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r10v6, resolved type: android.view.animation.AnimationSet} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r4v4, resolved type: android.view.animation.ScaleAnimation} */
    /* access modifiers changed from: package-private */
    /* JADX WARNING: Multi-variable type inference failed */
    public Animation createThumbnailScaleAnimationLocked(int appWidth, int appHeight, int transit, GraphicBuffer thumbnailHeader) {
        ScaleAnimation a;
        int i = appWidth;
        int i2 = appHeight;
        getDefaultNextAppTransitionStartRect(this.mTmpRect);
        int thumbWidthI = thumbnailHeader.getWidth();
        float thumbWidth = thumbWidthI > 0 ? (float) thumbWidthI : 1.0f;
        int thumbHeightI = thumbnailHeader.getHeight();
        float thumbHeight = thumbHeightI > 0 ? (float) thumbHeightI : 1.0f;
        if (this.mNextAppTransitionScaleUp) {
            float scaleW = ((float) i) / thumbWidth;
            float scaleH = ((float) i2) / thumbHeight;
            ScaleAnimation scaleAnimation = new ScaleAnimation(1.0f, scaleW, 1.0f, scaleH, computePivot(this.mTmpRect.left, 1.0f / scaleW), computePivot(this.mTmpRect.top, 1.0f / scaleH));
            scaleAnimation.setInterpolator(this.mDecelerateInterpolator);
            Animation alpha = new AlphaAnimation(1.0f, 0.0f);
            alpha.setInterpolator(this.mThumbnailFadeOutInterpolator);
            AnimationSet set = new AnimationSet(false);
            set.addAnimation(scaleAnimation);
            set.addAnimation(alpha);
            a = set;
        } else {
            float scaleW2 = ((float) i) / thumbWidth;
            float scaleH2 = ((float) i2) / thumbHeight;
            a = new ScaleAnimation(scaleW2, 1.0f, scaleH2, 1.0f, computePivot(this.mTmpRect.left, 1.0f / scaleW2), computePivot(this.mTmpRect.top, 1.0f / scaleH2));
        }
        return prepareThumbnailAnimation(a, i, i2, transit);
    }

    /* access modifiers changed from: package-private */
    public Animation createThumbnailEnterExitAnimationLocked(int thumbTransitState, Rect containingFrame, int transit, int taskId) {
        Animation a;
        int i = transit;
        int appWidth = containingFrame.width();
        int appHeight = containingFrame.height();
        GraphicBuffer thumbnailHeader = getAppTransitionThumbnailHeader(taskId);
        getDefaultNextAppTransitionStartRect(this.mTmpRect);
        int thumbWidthI = thumbnailHeader != null ? thumbnailHeader.getWidth() : appWidth;
        float thumbWidth = thumbWidthI > 0 ? (float) thumbWidthI : 1.0f;
        int thumbHeightI = thumbnailHeader != null ? thumbnailHeader.getHeight() : appHeight;
        float thumbHeight = thumbHeightI > 0 ? (float) thumbHeightI : 1.0f;
        switch (thumbTransitState) {
            case 0:
                float scaleW = thumbWidth / ((float) appWidth);
                float scaleH = thumbHeight / ((float) appHeight);
                ScaleAnimation scaleAnimation = new ScaleAnimation(scaleW, 1.0f, scaleH, 1.0f, computePivot(this.mTmpRect.left, scaleW), computePivot(this.mTmpRect.top, scaleH));
                a = scaleAnimation;
                break;
            case 1:
                if (i != 14) {
                    a = new AlphaAnimation(1.0f, 1.0f);
                    break;
                } else {
                    a = new AlphaAnimation(1.0f, 0.0f);
                    break;
                }
            case 2:
                a = new AlphaAnimation(1.0f, 1.0f);
                break;
            case 3:
                float scaleW2 = thumbWidth / ((float) appWidth);
                float scaleH2 = thumbHeight / ((float) appHeight);
                ScaleAnimation scaleAnimation2 = new ScaleAnimation(1.0f, scaleW2, 1.0f, scaleH2, computePivot(this.mTmpRect.left, scaleW2), computePivot(this.mTmpRect.top, scaleH2));
                AnimationSet set = new AnimationSet(true);
                set.addAnimation(scaleAnimation2);
                set.addAnimation(new AlphaAnimation(1.0f, 0.0f));
                set.setZAdjustment(1);
                a = set;
                break;
            default:
                throw new RuntimeException("Invalid thumbnail transition state");
        }
        return prepareThumbnailAnimation(a, appWidth, appHeight, i);
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
        set.setDuration(250);
        set.setZAdjustment(1);
        return set;
    }

    /* access modifiers changed from: package-private */
    public boolean canSkipFirstFrame() {
        return (this.mNextAppTransitionType == 1 || this.mNextAppTransitionType == 7 || this.mNextAppTransitionType == 8 || this.mNextAppTransition == 20) ? false : true;
    }

    /* access modifiers changed from: package-private */
    public RemoteAnimationController getRemoteAnimationController() {
        return this.mRemoteAnimationController;
    }

    /* access modifiers changed from: package-private */
    /* JADX WARNING: Code restructure failed: missing block: B:116:0x0301, code lost:
        if (r14 == false) goto L_0x0304;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:117:0x0304, code lost:
        r4 = 7;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:118:0x0305, code lost:
        r1 = r4;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:119:0x0307, code lost:
        if (r14 == false) goto L_0x030c;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:120:0x0309, code lost:
        r2 = 4;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:121:0x030c, code lost:
        r1 = r2;
     */
    public Animation loadAnimation(WindowManager.LayoutParams lp, int transit, boolean enter, int uiMode, int orientation, Rect frame, Rect displayFrame, Rect insets, Rect surfaceInsets, Rect stableInsets, boolean isVoiceInteraction, boolean freeform, int taskId) {
        Animation a;
        int i;
        int i2;
        int i3;
        int i4;
        int i5;
        Animation a2;
        int i6;
        int i7;
        WindowManager.LayoutParams layoutParams = lp;
        int i8 = transit;
        boolean z = enter;
        Rect rect = frame;
        if (isKeyguardGoingAwayTransit(transit) && z) {
            a = loadKeyguardExitAnimation(i8);
        } else if (i8 == 22) {
            a = null;
        } else if (i8 == 23 && !z) {
            a = loadAnimationRes(layoutParams, 17432764);
        } else if (i8 == 26) {
            a = null;
        } else {
            int i9 = 6;
            if (!isVoiceInteraction || !(i8 == 6 || i8 == 8 || i8 == 10)) {
                int i10 = 9;
                if (isVoiceInteraction && (i8 == 7 || i8 == 9 || i8 == 11)) {
                    if (z) {
                        i6 = 17432749;
                    } else {
                        i6 = 17432750;
                    }
                    a = loadAnimationRes(layoutParams, i6);
                    if (WindowManagerDebugConfig.DEBUG_APP_TRANSITIONS) {
                        Slog.v(TAG, "applyAnimation voice: anim=" + a + " transit=" + appTransitionToString(transit) + " isEntrance=" + z + " Callers=" + Debug.getCallers(3));
                    }
                } else if (i8 == 18) {
                    a = createRelaunchAnimation(rect, insets);
                    if (WindowManagerDebugConfig.DEBUG_APP_TRANSITIONS) {
                        Slog.v(TAG, "applyAnimation: anim=" + a + " nextAppTransition=" + this.mNextAppTransition + " transit=" + appTransitionToString(transit) + " Callers=" + Debug.getCallers(3));
                    }
                } else {
                    Rect rect2 = insets;
                    if (this.mNextAppTransitionType == 1) {
                        a = loadAnimationRes(this.mNextAppTransitionPackage, z ? this.mNextAppTransitionEnter : this.mNextAppTransitionExit);
                        if (WindowManagerDebugConfig.DEBUG_APP_TRANSITIONS) {
                            Slog.v(TAG, "applyAnimation: anim=" + a + " nextAppTransition=ANIM_CUSTOM transit=" + appTransitionToString(transit) + " isEntrance=" + z + " Callers=" + Debug.getCallers(3));
                        }
                    } else if (this.mNextAppTransitionType == 7) {
                        a = loadAnimationRes(this.mNextAppTransitionPackage, this.mNextAppTransitionInPlace);
                        if (WindowManagerDebugConfig.DEBUG_APP_TRANSITIONS) {
                            Slog.v(TAG, "applyAnimation: anim=" + a + " nextAppTransition=ANIM_CUSTOM_IN_PLACE transit=" + appTransitionToString(transit) + " Callers=" + Debug.getCallers(3));
                        }
                    } else {
                        if (this.mNextAppTransitionType == 8) {
                            a2 = createClipRevealAnimationLocked(i8, z, rect, displayFrame);
                            if (WindowManagerDebugConfig.DEBUG_APP_TRANSITIONS) {
                                Slog.v(TAG, "applyAnimation: anim=" + a2 + " nextAppTransition=ANIM_CLIP_REVEAL transit=" + appTransitionToString(transit) + " Callers=" + Debug.getCallers(3));
                            }
                        } else {
                            Rect rect3 = displayFrame;
                            if (this.mNextAppTransitionType == 2) {
                                a2 = createScaleUpAnimationLocked(i8, z, rect);
                                if (WindowManagerDebugConfig.DEBUG_APP_TRANSITIONS) {
                                    Slog.v(TAG, "applyAnimation: anim=" + a2 + " nextAppTransition=ANIM_SCALE_UP transit=" + appTransitionToString(transit) + " isEntrance=" + z + " Callers=" + Debug.getCallers(3));
                                }
                            } else if (this.mNextAppTransitionType == 3 || this.mNextAppTransitionType == 4) {
                                this.mNextAppTransitionScaleUp = this.mNextAppTransitionType == 3;
                                Animation a3 = createThumbnailEnterExitAnimationLocked(getThumbnailTransitionState(z), rect, i8, taskId);
                                if (!WindowManagerDebugConfig.DEBUG_APP_TRANSITIONS) {
                                    return a3;
                                }
                                String animName = this.mNextAppTransitionScaleUp ? "ANIM_THUMBNAIL_SCALE_UP" : "ANIM_THUMBNAIL_SCALE_DOWN";
                                Slog.v(TAG, "applyAnimation: anim=" + a3 + " nextAppTransition=" + animName + " transit=" + appTransitionToString(transit) + " isEntrance=" + z + " Callers=" + Debug.getCallers(3));
                                return a3;
                            } else {
                                int i11 = 5;
                                if (this.mNextAppTransitionType == 5 || this.mNextAppTransitionType == 6) {
                                    this.mNextAppTransitionScaleUp = this.mNextAppTransitionType == 5;
                                    a = createAspectScaledThumbnailEnterExitAnimationLocked(getThumbnailTransitionState(z), uiMode, orientation, i8, rect, rect2, surfaceInsets, stableInsets, freeform, taskId);
                                    if (WindowManagerDebugConfig.DEBUG_APP_TRANSITIONS) {
                                        String animName2 = this.mNextAppTransitionScaleUp ? "ANIM_THUMBNAIL_ASPECT_SCALE_UP" : "ANIM_THUMBNAIL_ASPECT_SCALE_DOWN";
                                        Slog.v(TAG, "applyAnimation: anim=" + a + " nextAppTransition=" + animName2 + " transit=" + appTransitionToString(transit) + " isEntrance=" + z + " Callers=" + Debug.getCallers(3));
                                    }
                                } else if (this.mNextAppTransitionType != 9 || !z) {
                                    int animAttr = 0;
                                    int i12 = 19;
                                    if (i8 != 19) {
                                        switch (i8) {
                                            case 6:
                                                break;
                                            case 7:
                                                break;
                                            case 8:
                                                break;
                                            case 9:
                                                animAttr = z ? 10 : 11;
                                                break;
                                            case 10:
                                                if (z) {
                                                    i = 12;
                                                } else {
                                                    i = 13;
                                                }
                                                animAttr = i;
                                                break;
                                            case 11:
                                                if (z) {
                                                    i2 = 14;
                                                } else {
                                                    i2 = 15;
                                                }
                                                animAttr = i2;
                                                break;
                                            case 12:
                                                if (!this.mService.mPolicy.isKeyguardShowingOrOccluded()) {
                                                    if (z) {
                                                        i12 = 18;
                                                    }
                                                    animAttr = i12;
                                                    break;
                                                }
                                                break;
                                            case 13:
                                                if (z) {
                                                    i3 = 16;
                                                } else {
                                                    i3 = 17;
                                                }
                                                animAttr = i3;
                                                break;
                                            case 14:
                                                if (z) {
                                                    i4 = 20;
                                                } else {
                                                    i4 = 21;
                                                }
                                                animAttr = i4;
                                                break;
                                            case 15:
                                                animAttr = z ? 22 : 23;
                                                break;
                                            case 16:
                                                if (z) {
                                                    i5 = 25;
                                                } else {
                                                    i5 = 24;
                                                }
                                                animAttr = i5;
                                                break;
                                            default:
                                                switch (i8) {
                                                    case 24:
                                                        break;
                                                    case WindowManagerService.H.SHOW_STRICT_MODE_VIOLATION /*25*/:
                                                        break;
                                                }
                                        }
                                    }
                                    if (z) {
                                        i10 = 8;
                                    }
                                    animAttr = i10;
                                    Animation a4 = animAttr != 0 ? loadAnimationAttr(layoutParams, animAttr, i8) : null;
                                    if (WindowManagerDebugConfig.DEBUG_APP_TRANSITIONS) {
                                        Slog.v(TAG, "applyAnimation: anim=" + a4 + " animAttr=0x" + Integer.toHexString(animAttr) + " transit=" + appTransitionToString(transit) + " isEntrance=" + z + " Callers=" + Debug.getCallers(3));
                                    }
                                    int animAttr2 = taskId;
                                    return a4;
                                } else {
                                    a2 = loadAnimationRes(PackageManagerService.PLATFORM_PACKAGE_NAME, 17432741);
                                    Slog.v(TAG, "applyAnimation NEXT_TRANSIT_TYPE_OPEN_CROSS_PROFILE_APPS: anim=" + a2 + " transit=" + appTransitionToString(transit) + " isEntrance=true Callers=" + Debug.getCallers(3));
                                }
                            }
                        }
                        a = a2;
                    }
                }
            } else {
                if (z) {
                    i7 = 17432751;
                } else {
                    i7 = 17432752;
                }
                a = loadAnimationRes(layoutParams, i7);
                if (WindowManagerDebugConfig.DEBUG_APP_TRANSITIONS) {
                    Slog.v(TAG, "applyAnimation voice: anim=" + a + " transit=" + appTransitionToString(transit) + " isEntrance=" + z + " Callers=" + Debug.getCallers(3));
                }
            }
        }
        int i13 = taskId;
        return a;
    }

    private Animation loadKeyguardExitAnimation(int transit) {
        if ((this.mNextAppTransitionFlags & 2) != 0) {
            return null;
        }
        boolean z = true;
        boolean toShade = (this.mNextAppTransitionFlags & 1) != 0;
        WindowManagerPolicy windowManagerPolicy = this.mService.mPolicy;
        if (transit != 21) {
            z = false;
        }
        return windowManagerPolicy.createHiddenByKeyguardExit(z, toShade);
    }

    /* access modifiers changed from: package-private */
    public int getAppStackClipMode() {
        int i;
        if (this.mNextAppTransition == 20 || this.mNextAppTransition == 21) {
            return 1;
        }
        if (this.mNextAppTransition == 18 || this.mNextAppTransition == 19 || this.mNextAppTransitionType == 8) {
            i = 2;
        } else {
            i = 0;
        }
        return i;
    }

    public int getTransitFlags() {
        return this.mNextAppTransitionFlags;
    }

    /* access modifiers changed from: package-private */
    public void postAnimationCallback() {
        if (this.mNextAppTransitionCallback != null) {
            this.mService.mH.sendMessage(this.mService.mH.obtainMessage(26, this.mNextAppTransitionCallback));
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
                i = 6;
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
                i = 6;
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
                i = 6;
            }
            this.mNextAppTransitionType = i;
            this.mNextAppTransitionAnimationsSpecsFuture = specsFuture;
            this.mNextAppTransitionScaleUp = scaleUp;
            this.mNextAppTransitionFutureCallback = callback;
        }
    }

    /* access modifiers changed from: package-private */
    public void overridePendingAppTransitionRemote(RemoteAnimationAdapter remoteAnimationAdapter) {
        if (isTransitionSet()) {
            clear();
            this.mNextAppTransitionType = 10;
            this.mRemoteAnimationController = new RemoteAnimationController(this.mService, remoteAnimationAdapter, this.mService.mH);
        }
    }

    /* access modifiers changed from: package-private */
    public void overrideInPlaceAppTransition(String packageName, int anim) {
        if (canOverridePendingAppTransition()) {
            clear();
            this.mNextAppTransitionType = 7;
            this.mNextAppTransitionPackage = packageName;
            this.mNextAppTransitionInPlace = anim;
        }
    }

    /* access modifiers changed from: package-private */
    public void overridePendingAppTransitionStartCrossProfileApps() {
        if (canOverridePendingAppTransition()) {
            clear();
            this.mNextAppTransitionType = 9;
            postAnimationCallback();
        }
    }

    private boolean canOverridePendingAppTransition() {
        return isTransitionSet() && this.mNextAppTransitionType != 10;
    }

    private void fetchAppTransitionSpecsFromFuture() {
        if (this.mNextAppTransitionAnimationsSpecsFuture != null) {
            this.mNextAppTransitionAnimationsSpecsPending = true;
            IAppTransitionAnimationSpecsFuture future = this.mNextAppTransitionAnimationsSpecsFuture;
            this.mNextAppTransitionAnimationsSpecsFuture = null;
            this.mDefaultExecutor.execute(new Runnable(future) {
                private final /* synthetic */ IAppTransitionAnimationSpecsFuture f$1;

                {
                    this.f$1 = r2;
                }

                public final void run() {
                    AppTransition.lambda$fetchAppTransitionSpecsFromFuture$0(AppTransition.this, this.f$1);
                }
            });
        }
    }

    public static /* synthetic */ void lambda$fetchAppTransitionSpecsFromFuture$0(AppTransition appTransition, IAppTransitionAnimationSpecsFuture future) {
        appTransition.mService.mH.removeMessages(102);
        appTransition.mService.mH.sendEmptyMessageDelayed(102, 5000);
        AppTransitionAnimationSpec[] specs = null;
        try {
            Binder.allowBlocking(future.asBinder());
            specs = future.get();
        } catch (RemoteException e) {
            Slog.w(TAG, "Failed to fetch app transition specs: " + e);
        }
        appTransition.mService.mH.removeMessages(102);
        synchronized (appTransition.mService.mWindowMap) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                appTransition.mNextAppTransitionAnimationsSpecsPending = false;
                appTransition.overridePendingAppTransitionMultiThumb(specs, appTransition.mNextAppTransitionFutureCallback, null, appTransition.mNextAppTransitionScaleUp);
                appTransition.mNextAppTransitionFutureCallback = null;
            } catch (Throwable th) {
                while (true) {
                    WindowManagerService.resetPriorityAfterLockedSection();
                    throw th;
                }
            }
        }
        WindowManagerService.resetPriorityAfterLockedSection();
        appTransition.mService.requestTraversal();
    }

    public String toString() {
        return "mNextAppTransition=" + appTransitionToString(this.mNextAppTransition);
    }

    public static String appTransitionToString(int transition) {
        switch (transition) {
            case -1:
                return "TRANSIT_UNSET";
            case 0:
                return "TRANSIT_NONE";
            case 6:
                return "TRANSIT_ACTIVITY_OPEN";
            case 7:
                return "TRANSIT_ACTIVITY_CLOSE";
            case 8:
                return "TRANSIT_TASK_OPEN";
            case 9:
                return "TRANSIT_TASK_CLOSE";
            case 10:
                return "TRANSIT_TASK_TO_FRONT";
            case 11:
                return "TRANSIT_TASK_TO_BACK";
            case 12:
                return "TRANSIT_WALLPAPER_CLOSE";
            case 13:
                return "TRANSIT_WALLPAPER_OPEN";
            case 14:
                return "TRANSIT_WALLPAPER_INTRA_OPEN";
            case 15:
                return "TRANSIT_WALLPAPER_INTRA_CLOSE";
            case 16:
                return "TRANSIT_TASK_OPEN_BEHIND";
            case 18:
                return "TRANSIT_ACTIVITY_RELAUNCH";
            case WindowManagerService.H.REPORT_WINDOWS_CHANGE /*19*/:
                return "TRANSIT_DOCK_TASK_FROM_RECENTS";
            case 20:
                return "TRANSIT_KEYGUARD_GOING_AWAY";
            case BackupHandler.MSG_OP_COMPLETE:
                return "TRANSIT_KEYGUARD_GOING_AWAY_ON_WALLPAPER";
            case WindowManagerService.H.REPORT_HARD_KEYBOARD_STATUS_CHANGE /*22*/:
                return "TRANSIT_KEYGUARD_OCCLUDE";
            case WindowManagerService.H.BOOT_TIMEOUT /*23*/:
                return "TRANSIT_KEYGUARD_UNOCCLUDE";
            case 24:
                return "TRANSIT_TRANSLUCENT_ACTIVITY_OPEN";
            case WindowManagerService.H.SHOW_STRICT_MODE_VIOLATION /*25*/:
                return "TRANSIT_TRANSLUCENT_ACTIVITY_CLOSE";
            case WindowManagerService.H.DO_ANIMATION_CALLBACK /*26*/:
                return "TRANSIT_CRASHING_ACTIVITY_CLOSE";
            default:
                return "<UNKNOWN: " + transition + ">";
        }
    }

    private String appStateToString() {
        switch (this.mAppTransitionState) {
            case 0:
                return "APP_STATE_IDLE";
            case 1:
                return "APP_STATE_READY";
            case 2:
                return "APP_STATE_RUNNING";
            case 3:
                return "APP_STATE_TIMEOUT";
            default:
                return "unknown state=" + this.mAppTransitionState;
        }
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
            case 6:
                return "NEXT_TRANSIT_TYPE_THUMBNAIL_ASPECT_SCALE_DOWN";
            case 7:
                return "NEXT_TRANSIT_TYPE_CUSTOM_IN_PLACE";
            case 9:
                return "NEXT_TRANSIT_TYPE_OPEN_CROSS_PROFILE_APPS";
            default:
                return "unknown type=" + this.mNextAppTransitionType;
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
            case 6:
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
            case 7:
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
        }
        pw.print(prefix);
        pw.print("mNextAppTransitionAnimationsSpecsPending= ");
        pw.println(this.mNextAppTransitionAnimationsSpecsPending);
    }

    public void setCurrentUser(int newUserId) {
        this.mCurrentUserId = newUserId;
    }

    /* access modifiers changed from: package-private */
    public boolean prepareAppTransitionLocked(int transit, boolean alwaysKeepCurrent, int flags, boolean forceOverride) {
        if (WindowManagerDebugConfig.DEBUG_APP_TRANSITIONS) {
            Slog.v(TAG, "Prepare app transition: transit=" + appTransitionToString(transit) + " " + this + " alwaysKeepCurrent=" + alwaysKeepCurrent + " Callers=" + Debug.getCallers(3));
        }
        if (isKeyguardGoingAwayTransit(transit)) {
            this.mService.mUnknownAppVisibilityController.clear();
        }
        boolean allowSetCrashing = !isKeyguardTransit(this.mNextAppTransition) && transit == 26;
        if (forceOverride || isKeyguardTransit(transit) || !isTransitionSet() || this.mNextAppTransition == 0 || allowSetCrashing) {
            setAppTransition(transit, flags);
        } else if (!alwaysKeepCurrent && !isKeyguardTransit(this.mNextAppTransition) && this.mNextAppTransition != 26) {
            if (transit == 8 && isTransitionEqual(9)) {
                setAppTransition(transit, flags);
            } else if (transit == 6 && isTransitionEqual(7)) {
                setAppTransition(transit, flags);
            } else if (isTaskTransit(transit) && isActivityTransit(this.mNextAppTransition)) {
                setAppTransition(transit, flags);
            }
        }
        boolean prepared = prepare();
        if (isTransitionSet()) {
            this.mService.mH.removeMessages(13);
            this.mService.mH.sendEmptyMessageDelayed(13, 5000);
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
        return isTaskOpenTransit(transit) || transit == 9 || transit == 11 || transit == 17;
    }

    private static boolean isTaskOpenTransit(int transit) {
        return transit == 8 || transit == 16 || transit == 10;
    }

    static boolean isActivityTransit(int transit) {
        return transit == 6 || transit == 7 || transit == 18;
    }

    private boolean shouldScaleDownThumbnailTransition(int uiMode, int orientation) {
        return this.mGridLayoutRecentsEnabled || orientation == 1;
    }
}
