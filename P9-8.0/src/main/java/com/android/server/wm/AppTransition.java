package com.android.server.wm;

import android.content.Context;
import android.graphics.GraphicBuffer;
import android.graphics.Path;
import android.graphics.Rect;
import android.os.Binder;
import android.os.Debug;
import android.os.IBinder;
import android.os.IRemoteCallback;
import android.os.RemoteException;
import android.os.SystemProperties;
import android.util.ArraySet;
import android.util.Flog;
import android.util.Slog;
import android.util.SparseArray;
import android.view.AppTransitionAnimationSpec;
import android.view.IAppTransitionAnimationSpecsFuture;
import android.view.WindowManager.LayoutParams;
import android.view.WindowManagerInternal.AppTransitionListener;
import android.view.WindowManagerPolicy;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationSet;
import android.view.animation.AnimationUtils;
import android.view.animation.ClipRectAnimation;
import android.view.animation.Interpolator;
import android.view.animation.PathInterpolator;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;
import com.android.internal.R;
import com.android.internal.util.DumpUtils.Dump;
import com.android.server.AttributeCache;
import com.android.server.AttributeCache.Entry;
import com.android.server.HwServiceFactory;
import com.android.server.usb.UsbAudioDevice;
import com.android.server.wm.animation.ClipRectLRAnimation;
import com.android.server.wm.animation.ClipRectTBAnimation;
import com.android.server.wm.animation.CurvedTranslateAnimation;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AppTransition implements Dump {
    private static final int APP_STATE_IDLE = 0;
    private static final int APP_STATE_READY = 1;
    private static final int APP_STATE_RUNNING = 2;
    private static final int APP_STATE_TIMEOUT = 3;
    private static final long APP_TRANSITION_GETSPECSFUTURE_TIMEOUT_MS = 5000;
    private static final long APP_TRANSITION_TIMEOUT_MS = 5000;
    private static final int CLIP_REVEAL_TRANSLATION_Y_DP = 8;
    static final int DEFAULT_APP_TRANSITION_DURATION = 250;
    private static final int MAX_CLIP_REVEAL_TRANSITION_DURATION = 420;
    private static final int NEXT_TRANSIT_TYPE_CLIP_REVEAL = 8;
    private static final int NEXT_TRANSIT_TYPE_CUSTOM = 1;
    private static final int NEXT_TRANSIT_TYPE_CUSTOM_IN_PLACE = 7;
    private static final int NEXT_TRANSIT_TYPE_NONE = 0;
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
    static final Interpolator TOUCH_RESPONSE_INTERPOLATOR = new PathInterpolator(0.3f, 0.0f, 0.1f, 1.0f);
    public static final int TRANSIT_ACTIVITY_CLOSE = 7;
    public static final int TRANSIT_ACTIVITY_OPEN = 6;
    public static final int TRANSIT_ACTIVITY_RELAUNCH = 18;
    public static final int TRANSIT_DOCK_TASK_FROM_RECENTS = 19;
    public static final int TRANSIT_FLAG_KEYGUARD_GOING_AWAY_NO_ANIMATION = 2;
    public static final int TRANSIT_FLAG_KEYGUARD_GOING_AWAY_TO_SHADE = 1;
    public static final int TRANSIT_FLAG_KEYGUARD_GOING_AWAY_WITH_WALLPAPER = 4;
    public static final int TRANSIT_KEYGUARD_GOING_AWAY = 20;
    public static final int TRANSIT_KEYGUARD_GOING_AWAY_ON_WALLPAPER = 21;
    public static final int TRANSIT_KEYGUARD_OCCLUDE = 22;
    public static final int TRANSIT_KEYGUARD_UNOCCLUDE = 23;
    public static final int TRANSIT_NONE = 0;
    public static final int TRANSIT_TASK_CLOSE = 9;
    public static final int TRANSIT_TASK_IN_PLACE = 17;
    public static final int TRANSIT_TASK_OPEN = 8;
    public static final int TRANSIT_TASK_OPEN_BEHIND = 16;
    public static final int TRANSIT_TASK_TO_BACK = 11;
    public static final int TRANSIT_TASK_TO_FRONT = 10;
    public static final int TRANSIT_UNSET = -1;
    public static final int TRANSIT_WALLPAPER_CLOSE = 12;
    public static final int TRANSIT_WALLPAPER_INTRA_CLOSE = 15;
    public static final int TRANSIT_WALLPAPER_INTRA_OPEN = 14;
    public static final int TRANSIT_WALLPAPER_OPEN = 13;
    private IRemoteCallback mAnimationFinishedCallback;
    private int mAppTransitionState = 0;
    private final Interpolator mClipHorizontalInterpolator = new PathInterpolator(0.0f, 0.0f, 0.4f, 1.0f);
    private final int mClipRevealTranslationY;
    private final int mConfigShortAnimTime;
    private final Context mContext;
    private int mCurrentUserId = 0;
    private final Interpolator mDecelerateInterpolator;
    private final ExecutorService mDefaultExecutor = Executors.newSingleThreadExecutor();
    private AppTransitionAnimationSpec mDefaultNextAppTransitionAnimationSpec;
    private final Interpolator mFastOutLinearInInterpolator;
    private final Interpolator mFastOutSlowInInterpolator;
    private final boolean mGridLayoutRecentsEnabled;
    private int mLastClipRevealMaxTranslation;
    private long mLastClipRevealTransitionDuration = 250;
    private String mLastClosingApp;
    private boolean mLastHadClipReveal;
    private String mLastOpeningApp;
    private int mLastUsedAppTransition = -1;
    private final Interpolator mLinearOutSlowInInterpolator;
    private final ArrayList<AppTransitionListener> mListeners = new ArrayList();
    private int mNextAppTransition = -1;
    private final SparseArray<AppTransitionAnimationSpec> mNextAppTransitionAnimationsSpecs = new SparseArray();
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
    private boolean mProlongedAnimationsEnded;
    private final WindowManagerService mService;
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
        this.mClipRevealTranslationY = (int) (this.mContext.getResources().getDisplayMetrics().density * 8.0f);
        this.mGridLayoutRecentsEnabled = SystemProperties.getBoolean("ro.recents.grid", false);
    }

    boolean isTransitionSet() {
        return this.mNextAppTransition != -1;
    }

    boolean isTransitionEqual(int transit) {
        return this.mNextAppTransition == transit;
    }

    int getAppTransition() {
        return this.mNextAppTransition;
    }

    private void setAppTransition(int transit, int flags) {
        if (transit != this.mNextAppTransition) {
            Flog.i(307, "set app transition: transit=" + appTransitionToString(transit) + " " + this + " Callers=" + Debug.getCallers(3));
        }
        this.mNextAppTransition = transit;
        this.mNextAppTransitionFlags |= flags;
        setLastAppTransition(-1, null, null);
        updateBooster();
    }

    void setLastAppTransition(int transit, AppWindowToken openingApp, AppWindowToken closingApp) {
        this.mLastUsedAppTransition = transit;
        this.mLastOpeningApp = "" + openingApp;
        this.mLastClosingApp = "" + closingApp;
    }

    boolean isReady() {
        if (this.mAppTransitionState == 1 || this.mAppTransitionState == 3) {
            return true;
        }
        return false;
    }

    void setReady() {
        setAppTransitionState(1);
        fetchAppTransitionSpecsFromFuture();
    }

    boolean isRunning() {
        return this.mAppTransitionState == 2;
    }

    void setIdle() {
        setAppTransitionState(0);
    }

    boolean isTimeout() {
        return this.mAppTransitionState == 3;
    }

    void setTimeout() {
        setAppTransitionState(3);
    }

    GraphicBuffer getAppTransitionThumbnailHeader(int taskId) {
        AppTransitionAnimationSpec spec = (AppTransitionAnimationSpec) this.mNextAppTransitionAnimationsSpecs.get(taskId);
        if (spec == null) {
            spec = this.mDefaultNextAppTransitionAnimationSpec;
        }
        if (spec != null) {
            return spec.buffer;
        }
        return null;
    }

    boolean isNextThumbnailTransitionAspectScaled() {
        if (this.mNextAppTransitionType == 5 || this.mNextAppTransitionType == 6) {
            return true;
        }
        return false;
    }

    boolean isNextThumbnailTransitionScaleUp() {
        return this.mNextAppTransitionScaleUp;
    }

    boolean isNextAppTransitionThumbnailUp() {
        if (this.mNextAppTransitionType == 3 || this.mNextAppTransitionType == 5) {
            return true;
        }
        return false;
    }

    boolean isNextAppTransitionThumbnailDown() {
        if (this.mNextAppTransitionType == 4 || this.mNextAppTransitionType == 6) {
            return true;
        }
        return false;
    }

    boolean isFetchingAppTransitionsSpecs() {
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

    int goodToGo(int transit, AppWindowAnimator topOpeningAppAnimator, AppWindowAnimator topClosingAppAnimator, ArraySet<AppWindowToken> openingApps, ArraySet<AppWindowToken> arraySet) {
        IBinder iBinder;
        IBinder iBinder2;
        Animation animation;
        Animation animation2 = null;
        this.mNextAppTransition = -1;
        this.mNextAppTransitionFlags = 0;
        setAppTransitionState(2);
        if (topOpeningAppAnimator != null) {
            iBinder = topOpeningAppAnimator.mAppToken.token;
        } else {
            iBinder = null;
        }
        if (topClosingAppAnimator != null) {
            iBinder2 = topClosingAppAnimator.mAppToken.token;
        } else {
            iBinder2 = null;
        }
        if (topOpeningAppAnimator != null) {
            animation = topOpeningAppAnimator.animation;
        } else {
            animation = null;
        }
        if (topClosingAppAnimator != null) {
            animation2 = topClosingAppAnimator.animation;
        }
        int redoLayout = notifyAppTransitionStartingLocked(transit, iBinder, iBinder2, animation, animation2);
        this.mService.getDefaultDisplayContentLocked().getDockedDividerController().notifyAppTransitionStarting(openingApps, transit);
        if (transit == 19 && (this.mProlongedAnimationsEnded ^ 1) != 0) {
            for (int i = openingApps.size() - 1; i >= 0; i--) {
                ((AppWindowToken) openingApps.valueAt(i)).mAppAnimator.startProlongAnimation(2);
            }
        }
        return redoLayout;
    }

    void notifyProlongedAnimationsEnded() {
        this.mProlongedAnimationsEnded = true;
    }

    void clear() {
        this.mNextAppTransitionType = 0;
        this.mNextAppTransitionPackage = null;
        this.mNextAppTransitionAnimationsSpecs.clear();
        this.mNextAppTransitionAnimationsSpecsFuture = null;
        this.mDefaultNextAppTransitionAnimationSpec = null;
        this.mAnimationFinishedCallback = null;
        this.mProlongedAnimationsEnded = false;
    }

    void freeze() {
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

    private void updateBooster() {
        boolean z = true;
        WindowManagerThreadPriorityBooster windowManagerThreadPriorityBooster = WindowManagerService.sThreadPriorityBooster;
        if (!(this.mNextAppTransition != -1 || this.mAppTransitionState == 1 || this.mAppTransitionState == 2)) {
            z = false;
        }
        windowManagerThreadPriorityBooster.setAppTransitionRunning(z);
    }

    void registerListenerLocked(AppTransitionListener listener) {
        this.mListeners.add(listener);
    }

    public void notifyAppTransitionFinishedLocked(IBinder token) {
        for (int i = 0; i < this.mListeners.size(); i++) {
            ((AppTransitionListener) this.mListeners.get(i)).onAppTransitionFinishedLocked(token);
        }
    }

    private void notifyAppTransitionPendingLocked() {
        for (int i = 0; i < this.mListeners.size(); i++) {
            ((AppTransitionListener) this.mListeners.get(i)).onAppTransitionPendingLocked();
        }
    }

    private void notifyAppTransitionCancelledLocked(int transit) {
        for (int i = 0; i < this.mListeners.size(); i++) {
            ((AppTransitionListener) this.mListeners.get(i)).onAppTransitionCancelledLocked(transit);
        }
    }

    private int notifyAppTransitionStartingLocked(int transit, IBinder openToken, IBinder closeToken, Animation openAnimation, Animation closeAnimation) {
        int redoLayout = 0;
        for (int i = 0; i < this.mListeners.size(); i++) {
            redoLayout |= ((AppTransitionListener) this.mListeners.get(i)).onAppTransitionStartingLocked(transit, openToken, closeToken, openAnimation, closeAnimation);
        }
        return redoLayout;
    }

    private Entry getCachedAnimations(LayoutParams lp) {
        if (lp == null || lp.windowAnimations == 0) {
            return null;
        }
        String packageName = lp.packageName != null ? lp.packageName : "android";
        int resId = lp.windowAnimations;
        if ((UsbAudioDevice.kAudioDeviceMetaMask & resId) == 16777216) {
            packageName = "android";
        }
        return AttributeCache.instance().get(packageName, resId, R.styleable.WindowAnimation, this.mCurrentUserId);
    }

    protected Entry getCachedAnimations(String packageName, int resId) {
        if (packageName == null) {
            return null;
        }
        if ((UsbAudioDevice.kAudioDeviceMetaMask & resId) == 16777216) {
            packageName = "android";
        }
        return AttributeCache.instance().get(packageName, resId, R.styleable.WindowAnimation, this.mCurrentUserId);
    }

    Animation loadAnimationAttr(LayoutParams lp, int animAttr) {
        int anim = 0;
        Context context = this.mContext;
        if (animAttr >= 0) {
            Entry ent = getCachedAnimations(lp);
            if (ent != null) {
                context = ent.context;
                anim = ent.array.getResourceId(animAttr, 0);
            }
            ent = HwServiceFactory.getHwAppTransition().overrideAnimation(lp, animAttr, this.mContext, ent, this);
            if (ent != null) {
                context = ent.context;
                anim = ent.array.getResourceId(animAttr, 0);
            }
        }
        if (anim != 0) {
            return AnimationUtils.loadAnimation(context, anim);
        }
        return null;
    }

    Animation loadAnimationRes(LayoutParams lp, int resId) {
        Context context = this.mContext;
        if (resId < 0) {
            return null;
        }
        Entry ent = getCachedAnimations(lp);
        if (ent != null) {
            context = ent.context;
        }
        return AnimationUtils.loadAnimation(context, resId);
    }

    private Animation loadAnimationRes(String packageName, int resId) {
        int anim = 0;
        Context context = this.mContext;
        if (resId >= 0) {
            Entry ent = getCachedAnimations(packageName, resId);
            if (ent != null) {
                context = ent.context;
                anim = resId;
            }
        }
        if (anim != 0) {
            return AnimationUtils.loadAnimation(context, anim);
        }
        return null;
    }

    private static float computePivot(int startPos, float finalScale) {
        float denom = finalScale - 1.0f;
        if (Math.abs(denom) < 1.0E-4f) {
            return (float) startPos;
        }
        return ((float) (-startPos)) / denom;
    }

    private Animation createScaleUpAnimationLocked(int transit, boolean enter, Rect containingFrame) {
        Animation a;
        long duration;
        getDefaultNextAppTransitionStartRect(this.mTmpRect);
        int appWidth = containingFrame.width();
        int appHeight = containingFrame.height();
        if (enter) {
            float scaleW = ((float) this.mTmpRect.width()) / ((float) appWidth);
            float scaleH = ((float) this.mTmpRect.height()) / ((float) appHeight);
            Animation scale = new ScaleAnimation(scaleW, 1.0f, scaleH, 1.0f, computePivot(this.mTmpRect.left, scaleW), computePivot(this.mTmpRect.top, scaleH));
            scale.setInterpolator(this.mDecelerateInterpolator);
            Animation alpha = new AlphaAnimation(0.0f, 1.0f);
            alpha.setInterpolator(this.mThumbnailFadeOutInterpolator);
            Animation set = new AnimationSet(false);
            set.addAnimation(scale);
            set.addAnimation(alpha);
            set.setDetachWallpaper(true);
            a = set;
        } else if (transit == 14 || transit == 15) {
            a = new AlphaAnimation(1.0f, 0.0f);
            a.setDetachWallpaper(true);
        } else {
            a = new AlphaAnimation(1.0f, 1.0f);
        }
        switch (transit) {
            case 6:
            case 7:
                duration = (long) this.mConfigShortAnimTime;
                break;
            default:
                duration = 250;
                break;
        }
        a.setDuration(duration);
        a.setFillAfter(true);
        a.setInterpolator(this.mDecelerateInterpolator);
        a.initialize(appWidth, appHeight, appWidth, appHeight);
        return a;
    }

    private void getDefaultNextAppTransitionStartRect(Rect rect) {
        if (this.mDefaultNextAppTransitionAnimationSpec == null || this.mDefaultNextAppTransitionAnimationSpec.rect == null) {
            Slog.e(TAG, "Starting rect for app requested, but none available", new Throwable());
            rect.setEmpty();
            return;
        }
        rect.set(this.mDefaultNextAppTransitionAnimationSpec.rect);
    }

    void getNextAppTransitionStartRect(int taskId, Rect rect) {
        AppTransitionAnimationSpec spec = (AppTransitionAnimationSpec) this.mNextAppTransitionAnimationsSpecs.get(taskId);
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

    long getLastClipRevealTransitionDuration() {
        return this.mLastClipRevealTransitionDuration;
    }

    int getLastClipRevealMaxTranslation() {
        return this.mLastClipRevealMaxTranslation;
    }

    boolean hadClipRevealAnimation() {
        return this.mLastHadClipReveal;
    }

    private long calculateClipRevealTransitionDuration(boolean cutOff, float translationX, float translationY, Rect displayFrame) {
        if (cutOff) {
            return (long) ((170.0f * Math.max(Math.abs(translationX) / ((float) displayFrame.width()), Math.abs(translationY) / ((float) displayFrame.height()))) + 250.0f);
        }
        return 250;
    }

    private Animation createClipRevealAnimationLocked(int transit, boolean enter, Rect appFrame, Rect displayFrame) {
        Animation anim;
        long duration;
        if (enter) {
            Interpolator interpolator;
            int appWidth = appFrame.width();
            int appHeight = appFrame.height();
            getDefaultNextAppTransitionStartRect(this.mTmpRect);
            float t = 0.0f;
            if (appHeight > 0) {
                t = ((float) this.mTmpRect.top) / ((float) displayFrame.height());
            }
            int translationY = this.mClipRevealTranslationY + ((int) ((((float) displayFrame.height()) / 7.0f) * t));
            int translationX = 0;
            int translationYCorrection = translationY;
            int centerX = this.mTmpRect.centerX();
            int centerY = this.mTmpRect.centerY();
            int halfWidth = this.mTmpRect.width() / 2;
            int halfHeight = this.mTmpRect.height() / 2;
            int clipStartX = (centerX - halfWidth) - appFrame.left;
            int clipStartY = (centerY - halfHeight) - appFrame.top;
            boolean cutOff = false;
            if (appFrame.top > centerY - halfHeight) {
                translationY = (centerY - halfHeight) - appFrame.top;
                translationYCorrection = 0;
                clipStartY = 0;
                cutOff = true;
            }
            if (appFrame.left > centerX - halfWidth) {
                translationX = (centerX - halfWidth) - appFrame.left;
                clipStartX = 0;
                cutOff = true;
            }
            if (appFrame.right < centerX + halfWidth) {
                translationX = (centerX + halfWidth) - appFrame.right;
                clipStartX = appWidth - this.mTmpRect.width();
                cutOff = true;
            }
            duration = calculateClipRevealTransitionDuration(cutOff, (float) translationX, (float) translationY, displayFrame);
            Animation clipRectLRAnimation = new ClipRectLRAnimation(clipStartX, this.mTmpRect.width() + clipStartX, 0, appWidth);
            clipRectLRAnimation.setInterpolator(this.mClipHorizontalInterpolator);
            clipRectLRAnimation.setDuration((long) (((float) duration) / 2.5f));
            clipRectLRAnimation = new TranslateAnimation((float) translationX, 0.0f, (float) translationY, 0.0f);
            if (cutOff) {
                interpolator = TOUCH_RESPONSE_INTERPOLATOR;
            } else {
                interpolator = this.mLinearOutSlowInInterpolator;
            }
            clipRectLRAnimation.setInterpolator(interpolator);
            clipRectLRAnimation.setDuration(duration);
            Animation clipAnimTB = new ClipRectTBAnimation(clipStartY, this.mTmpRect.height() + clipStartY, 0, appHeight, translationYCorrection, 0, this.mLinearOutSlowInInterpolator);
            clipAnimTB.setInterpolator(TOUCH_RESPONSE_INTERPOLATOR);
            clipAnimTB.setDuration(duration);
            long alphaDuration = duration / 4;
            AlphaAnimation alpha = new AlphaAnimation(0.5f, 1.0f);
            alpha.setDuration(alphaDuration);
            alpha.setInterpolator(this.mLinearOutSlowInInterpolator);
            clipRectLRAnimation = new AnimationSet(false);
            clipRectLRAnimation.addAnimation(clipRectLRAnimation);
            clipRectLRAnimation.addAnimation(clipAnimTB);
            clipRectLRAnimation.addAnimation(clipRectLRAnimation);
            clipRectLRAnimation.addAnimation(alpha);
            clipRectLRAnimation.setZAdjustment(1);
            clipRectLRAnimation.initialize(appWidth, appHeight, appWidth, appHeight);
            anim = clipRectLRAnimation;
            this.mLastHadClipReveal = true;
            this.mLastClipRevealTransitionDuration = duration;
            this.mLastClipRevealMaxTranslation = cutOff ? Math.max(Math.abs(translationY), Math.abs(translationX)) : 0;
        } else {
            switch (transit) {
                case 6:
                case 7:
                    duration = (long) this.mConfigShortAnimTime;
                    break;
                default:
                    duration = 250;
                    break;
            }
            if (transit == 14 || transit == 15) {
                anim = new AlphaAnimation(1.0f, 0.0f);
                anim.setDetachWallpaper(true);
            } else {
                anim = new AlphaAnimation(1.0f, 1.0f);
            }
            anim.setInterpolator(this.mDecelerateInterpolator);
            anim.setDuration(duration);
            anim.setFillAfter(true);
        }
        return anim;
    }

    Animation prepareThumbnailAnimationWithDuration(Animation a, int appWidth, int appHeight, long duration, Interpolator interpolator) {
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

    Animation prepareThumbnailAnimation(Animation a, int appWidth, int appHeight, int transit) {
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

    int getThumbnailTransitionState(boolean enter) {
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

    Animation createThumbnailAspectScaleAnimationLocked(Rect appRect, Rect contentInsets, GraphicBuffer thumbnailHeader, int taskId, int uiMode, int orientation) {
        float fromX;
        float fromY;
        float toX;
        float toY;
        float pivotX;
        float pivotY;
        Animation a;
        int thumbWidthI = thumbnailHeader.getWidth();
        float thumbWidth = (float) (thumbWidthI > 0 ? thumbWidthI : 1);
        int thumbHeightI = thumbnailHeader.getHeight();
        int appWidth = appRect.width();
        float scaleW = ((float) appWidth) / thumbWidth;
        getNextAppTransitionStartRect(taskId, this.mTmpRect);
        if (shouldScaleDownThumbnailTransition(uiMode, orientation)) {
            fromX = (float) this.mTmpRect.left;
            fromY = (float) this.mTmpRect.top;
            toX = (((float) (this.mTmpRect.width() / 2)) * (scaleW - 1.0f)) + ((float) appRect.left);
            toY = (((float) (appRect.height() / 2)) * (1.0f - (1.0f / scaleW))) + ((float) appRect.top);
            pivotX = (float) (this.mTmpRect.width() / 2);
            pivotY = ((float) (appRect.height() / 2)) / scaleW;
            if (this.mGridLayoutRecentsEnabled) {
                fromY -= (float) thumbHeightI;
                toY -= ((float) thumbHeightI) * scaleW;
            }
        } else {
            pivotX = 0.0f;
            pivotY = 0.0f;
            fromX = (float) this.mTmpRect.left;
            fromY = (float) this.mTmpRect.top;
            toX = (float) appRect.left;
            toY = (float) appRect.top;
        }
        long duration = getAspectScaleDuration();
        Interpolator interpolator = getAspectScaleInterpolator();
        Animation alphaAnimation;
        Animation translate;
        if (this.mNextAppTransitionScaleUp) {
            long j;
            Animation scale = new ScaleAnimation(1.0f, scaleW, 1.0f, scaleW, pivotX, pivotY);
            scale.setInterpolator(interpolator);
            scale.setDuration(duration);
            alphaAnimation = new AlphaAnimation(1.0f, 0.0f);
            alphaAnimation.setInterpolator(this.mNextAppTransition == 19 ? THUMBNAIL_DOCK_INTERPOLATOR : this.mThumbnailFadeOutInterpolator);
            if (this.mNextAppTransition == 19) {
                j = duration / 2;
            } else {
                j = duration;
            }
            alphaAnimation.setDuration(j);
            translate = createCurvedMotion(fromX, toX, fromY, toY);
            translate.setInterpolator(interpolator);
            translate.setDuration(duration);
            this.mTmpFromClipRect.set(0, 0, thumbWidthI, thumbHeightI);
            this.mTmpToClipRect.set(appRect);
            this.mTmpToClipRect.offsetTo(0, 0);
            this.mTmpToClipRect.right = (int) (((float) this.mTmpToClipRect.right) / scaleW);
            this.mTmpToClipRect.bottom = (int) (((float) this.mTmpToClipRect.bottom) / scaleW);
            if (contentInsets != null) {
                this.mTmpToClipRect.inset((int) (((float) (-contentInsets.left)) * scaleW), (int) (((float) (-contentInsets.top)) * scaleW), (int) (((float) (-contentInsets.right)) * scaleW), (int) (((float) (-contentInsets.bottom)) * scaleW));
            }
            alphaAnimation = new ClipRectAnimation(this.mTmpFromClipRect, this.mTmpToClipRect);
            alphaAnimation.setInterpolator(interpolator);
            alphaAnimation.setDuration(duration);
            alphaAnimation = new AnimationSet(false);
            alphaAnimation.addAnimation(scale);
            if (!this.mGridLayoutRecentsEnabled) {
                alphaAnimation.addAnimation(alphaAnimation);
            }
            alphaAnimation.addAnimation(translate);
            alphaAnimation.addAnimation(alphaAnimation);
            a = alphaAnimation;
        } else {
            Animation scaleAnimation = new ScaleAnimation(scaleW, 1.0f, scaleW, 1.0f, pivotX, pivotY);
            scaleAnimation.setInterpolator(interpolator);
            scaleAnimation.setDuration(duration);
            alphaAnimation = new AlphaAnimation(0.0f, 1.0f);
            alphaAnimation.setInterpolator(this.mThumbnailFadeInInterpolator);
            alphaAnimation.setDuration(duration);
            translate = createCurvedMotion(toX, fromX, toY, fromY);
            translate.setInterpolator(interpolator);
            translate.setDuration(duration);
            alphaAnimation = new AnimationSet(false);
            alphaAnimation.addAnimation(scaleAnimation);
            if (!this.mGridLayoutRecentsEnabled) {
                alphaAnimation.addAnimation(alphaAnimation);
            }
            alphaAnimation.addAnimation(translate);
            a = alphaAnimation;
        }
        return prepareThumbnailAnimationWithDuration(a, appWidth, appRect.height(), 0, null);
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

    Animation createAspectScaledThumbnailEnterExitAnimationLocked(int thumbTransitState, int uiMode, int orientation, int transit, Rect containingFrame, Rect contentInsets, Rect surfaceInsets, boolean freeform, int taskId) {
        Animation a;
        int appWidth = containingFrame.width();
        int appHeight = containingFrame.height();
        getDefaultNextAppTransitionStartRect(this.mTmpRect);
        int thumbWidthI = this.mTmpRect.width();
        float thumbWidth = (float) (thumbWidthI > 0 ? thumbWidthI : 1);
        int thumbHeightI = this.mTmpRect.height();
        float thumbHeight = (float) (thumbHeightI > 0 ? thumbHeightI : 1);
        int thumbStartX = (this.mTmpRect.left - containingFrame.left) - contentInsets.left;
        int thumbStartY = this.mTmpRect.top - containingFrame.top;
        switch (thumbTransitState) {
            case 0:
            case 3:
                boolean scaleUp = thumbTransitState == 0;
                if (!freeform || !scaleUp) {
                    if (!freeform) {
                        Animation animationSet = new AnimationSet(true);
                        this.mTmpFromClipRect.set(containingFrame);
                        this.mTmpToClipRect.set(containingFrame);
                        this.mTmpFromClipRect.offsetTo(0, 0);
                        this.mTmpToClipRect.offsetTo(0, 0);
                        this.mTmpFromClipRect.inset(contentInsets);
                        this.mNextAppTransitionInsets.set(contentInsets);
                        Animation clipAnim;
                        Animation translateAnim;
                        if (shouldScaleDownThumbnailTransition(uiMode, orientation)) {
                            float f;
                            float scale = thumbWidth / ((float) ((appWidth - contentInsets.left) - contentInsets.right));
                            if (!this.mGridLayoutRecentsEnabled) {
                                this.mTmpFromClipRect.bottom = this.mTmpFromClipRect.top + ((int) (thumbHeight / scale));
                            }
                            this.mNextAppTransitionInsets.set(contentInsets);
                            float f2 = scaleUp ? scale : 1.0f;
                            float f3 = scaleUp ? 1.0f : scale;
                            float f4 = scaleUp ? scale : 1.0f;
                            if (scaleUp) {
                                f = 1.0f;
                            } else {
                                f = scale;
                            }
                            Animation scaleAnim = new ScaleAnimation(f2, f3, f4, f, ((float) containingFrame.width()) / 2.0f, (((float) containingFrame.height()) / 2.0f) + ((float) contentInsets.top));
                            float startX = ((float) (this.mTmpRect.left - containingFrame.left)) - ((((float) containingFrame.width()) / 2.0f) - ((((float) containingFrame.width()) / 2.0f) * scale));
                            float startY = ((float) (this.mTmpRect.top - containingFrame.top)) - ((((float) containingFrame.height()) / 2.0f) - ((((float) containingFrame.height()) / 2.0f) * scale));
                            if (scaleUp) {
                                clipAnim = new ClipRectAnimation(this.mTmpFromClipRect, this.mTmpToClipRect);
                            } else {
                                clipAnim = new ClipRectAnimation(this.mTmpToClipRect, this.mTmpFromClipRect);
                            }
                            if (scaleUp) {
                                translateAnim = createCurvedMotion(startX, 0.0f, startY - ((float) contentInsets.top), 0.0f);
                            } else {
                                translateAnim = createCurvedMotion(0.0f, startX, 0.0f, startY - ((float) contentInsets.top));
                            }
                            animationSet.addAnimation(clipAnim);
                            animationSet.addAnimation(scaleAnim);
                            animationSet.addAnimation(translateAnim);
                        } else {
                            this.mTmpFromClipRect.bottom = this.mTmpFromClipRect.top + thumbHeightI;
                            this.mTmpFromClipRect.right = this.mTmpFromClipRect.left + thumbWidthI;
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
                            animationSet.addAnimation(clipAnim);
                            animationSet.addAnimation(translateAnim);
                        }
                        a = animationSet;
                        animationSet.setZAdjustment(1);
                        break;
                    }
                    a = createAspectScaledThumbnailExitFreeformAnimationLocked(containingFrame, surfaceInsets, taskId);
                    break;
                }
                a = createAspectScaledThumbnailEnterFreeformAnimationLocked(containingFrame, surfaceInsets, taskId);
                break;
                break;
            case 1:
                if (transit != 14) {
                    a = new AlphaAnimation(1.0f, 1.0f);
                    break;
                }
                a = new AlphaAnimation(1.0f, 0.0f);
                break;
            case 2:
                if (transit != 14) {
                    a = new AlphaAnimation(1.0f, 1.0f);
                    break;
                }
                a = new AlphaAnimation(0.0f, 1.0f);
                break;
            default:
                throw new RuntimeException("Invalid thumbnail transition state");
        }
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
        float sourceWidth = (float) sourceFrame.width();
        float sourceHeight = (float) sourceFrame.height();
        float destWidth = (float) destFrame.width();
        float destHeight = (float) destFrame.height();
        float scaleH = enter ? sourceWidth / destWidth : destWidth / sourceWidth;
        float scaleV = enter ? sourceHeight / destHeight : destHeight / sourceHeight;
        AnimationSet animationSet = new AnimationSet(true);
        int surfaceInsetsH = surfaceInsets == null ? 0 : surfaceInsets.left + surfaceInsets.right;
        int surfaceInsetsV = surfaceInsets == null ? 0 : surfaceInsets.top + surfaceInsets.bottom;
        if (!enter) {
            destWidth = sourceWidth;
        }
        float scaleHCenter = (((float) surfaceInsetsH) + destWidth) / 2.0f;
        if (!enter) {
            destHeight = sourceHeight;
        }
        float scaleVCenter = (((float) surfaceInsetsV) + destHeight) / 2.0f;
        if (enter) {
            scale = new ScaleAnimation(scaleH, 1.0f, scaleV, 1.0f, scaleHCenter, scaleVCenter);
        } else {
            ScaleAnimation scaleAnimation = new ScaleAnimation(1.0f, scaleH, 1.0f, scaleV, scaleHCenter, scaleVCenter);
        }
        int sourceHCenter = sourceFrame.left + (sourceFrame.width() / 2);
        int sourceVCenter = sourceFrame.top + (sourceFrame.height() / 2);
        int destHCenter = destFrame.left + (destFrame.width() / 2);
        int destVCenter = destFrame.top + (destFrame.height() / 2);
        int fromX = enter ? sourceHCenter - destHCenter : destHCenter - sourceHCenter;
        int fromY = enter ? sourceVCenter - destVCenter : destVCenter - sourceVCenter;
        Animation translateAnimation;
        if (enter) {
            translateAnimation = new TranslateAnimation((float) fromX, 0.0f, (float) fromY, 0.0f);
        } else {
            translateAnimation = new TranslateAnimation(0.0f, (float) fromX, 0.0f, (float) fromY);
        }
        animationSet.addAnimation(scale);
        animationSet.addAnimation(translation);
        IRemoteCallback callback = this.mAnimationFinishedCallback;
        if (callback != null) {
            final IRemoteCallback iRemoteCallback = callback;
            animationSet.setAnimationListener(new AnimationListener() {
                public void onAnimationStart(Animation animation) {
                }

                public void onAnimationEnd(Animation animation) {
                    AppTransition.this.mService.mH.obtainMessage(26, iRemoteCallback).sendToTarget();
                }

                public void onAnimationRepeat(Animation animation) {
                }
            });
        }
        return animationSet;
    }

    Animation createThumbnailScaleAnimationLocked(int appWidth, int appHeight, int transit, GraphicBuffer thumbnailHeader) {
        Animation a;
        getDefaultNextAppTransitionStartRect(this.mTmpRect);
        int thumbWidthI = thumbnailHeader.getWidth();
        if (thumbWidthI <= 0) {
            thumbWidthI = 1;
        }
        float thumbWidth = (float) thumbWidthI;
        int thumbHeightI = thumbnailHeader.getHeight();
        if (thumbHeightI <= 0) {
            thumbHeightI = 1;
        }
        float thumbHeight = (float) thumbHeightI;
        float scaleW;
        float scaleH;
        if (this.mNextAppTransitionScaleUp) {
            scaleW = ((float) appWidth) / thumbWidth;
            scaleH = ((float) appHeight) / thumbHeight;
            Animation scale = new ScaleAnimation(1.0f, scaleW, 1.0f, scaleH, computePivot(this.mTmpRect.left, 1.0f / scaleW), computePivot(this.mTmpRect.top, 1.0f / scaleH));
            scale.setInterpolator(this.mDecelerateInterpolator);
            Animation alpha = new AlphaAnimation(1.0f, 0.0f);
            alpha.setInterpolator(this.mThumbnailFadeOutInterpolator);
            Animation set = new AnimationSet(false);
            set.addAnimation(scale);
            set.addAnimation(alpha);
            a = set;
        } else {
            scaleW = ((float) appWidth) / thumbWidth;
            scaleH = ((float) appHeight) / thumbHeight;
            a = new ScaleAnimation(scaleW, 1.0f, scaleH, 1.0f, computePivot(this.mTmpRect.left, 1.0f / scaleW), computePivot(this.mTmpRect.top, 1.0f / scaleH));
        }
        return prepareThumbnailAnimation(a, appWidth, appHeight, transit);
    }

    Animation createThumbnailEnterExitAnimationLocked(int thumbTransitState, Rect containingFrame, int transit, int taskId) {
        Animation a;
        int appWidth = containingFrame.width();
        int appHeight = containingFrame.height();
        GraphicBuffer thumbnailHeader = getAppTransitionThumbnailHeader(taskId);
        getDefaultNextAppTransitionStartRect(this.mTmpRect);
        int thumbWidthI = thumbnailHeader != null ? thumbnailHeader.getWidth() : appWidth;
        if (thumbWidthI <= 0) {
            thumbWidthI = 1;
        }
        float thumbWidth = (float) thumbWidthI;
        int thumbHeightI = thumbnailHeader != null ? thumbnailHeader.getHeight() : appHeight;
        if (thumbHeightI <= 0) {
            thumbHeightI = 1;
        }
        float thumbHeight = (float) thumbHeightI;
        float scaleW;
        float scaleH;
        switch (thumbTransitState) {
            case 0:
                scaleW = thumbWidth / ((float) appWidth);
                scaleH = thumbHeight / ((float) appHeight);
                a = new ScaleAnimation(scaleW, 1.0f, scaleH, 1.0f, computePivot(this.mTmpRect.left, scaleW), computePivot(this.mTmpRect.top, scaleH));
                break;
            case 1:
                if (transit != 14) {
                    a = new AlphaAnimation(1.0f, 1.0f);
                    break;
                }
                a = new AlphaAnimation(1.0f, 0.0f);
                break;
            case 2:
                a = new AlphaAnimation(1.0f, 1.0f);
                break;
            case 3:
                scaleW = thumbWidth / ((float) appWidth);
                scaleH = thumbHeight / ((float) appHeight);
                Animation scale = new ScaleAnimation(1.0f, scaleW, 1.0f, scaleH, computePivot(this.mTmpRect.left, scaleW), computePivot(this.mTmpRect.top, scaleH));
                Animation alpha = new AlphaAnimation(1.0f, 0.0f);
                Animation animationSet = new AnimationSet(true);
                animationSet.addAnimation(scale);
                animationSet.addAnimation(alpha);
                animationSet.setZAdjustment(1);
                a = animationSet;
                break;
            default:
                throw new RuntimeException("Invalid thumbnail transition state");
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
        set.setDuration(250);
        set.setZAdjustment(1);
        return set;
    }

    boolean canSkipFirstFrame() {
        if (this.mNextAppTransitionType == 1 || this.mNextAppTransitionType == 7 || this.mNextAppTransitionType == 8) {
            return false;
        }
        return this.mNextAppTransition != 20;
    }

    Animation loadAnimation(LayoutParams lp, int transit, boolean enter, int uiMode, int orientation, Rect frame, Rect displayFrame, Rect insets, Rect surfaceInsets, boolean isVoiceInteraction, boolean freeform, int taskId) {
        if (isKeyguardGoingAwayTransit(transit) && enter) {
            return loadKeyguardExitAnimation(transit);
        }
        if (transit == 22) {
            return null;
        }
        if (transit == 23 && (enter ^ 1) != 0) {
            return loadAnimationRes(lp, 17432753);
        }
        int i;
        if (isVoiceInteraction && (transit == 6 || transit == 8 || transit == 10)) {
            if (enter) {
                i = 17432740;
            } else {
                i = 17432741;
            }
            return loadAnimationRes(lp, i);
        } else if (isVoiceInteraction && (transit == 7 || transit == 9 || transit == 11)) {
            if (enter) {
                i = 17432738;
            } else {
                i = 17432739;
            }
            return loadAnimationRes(lp, i);
        } else if (transit == 18) {
            return createRelaunchAnimation(frame, insets);
        } else {
            if (this.mNextAppTransitionType == 1) {
                return loadAnimationRes(this.mNextAppTransitionPackage, enter ? this.mNextAppTransitionEnter : this.mNextAppTransitionExit);
            } else if (this.mNextAppTransitionType == 7) {
                return loadAnimationRes(this.mNextAppTransitionPackage, this.mNextAppTransitionInPlace);
            } else if (this.mNextAppTransitionType == 8) {
                return createClipRevealAnimationLocked(transit, enter, frame, displayFrame);
            } else {
                if (this.mNextAppTransitionType == 2) {
                    return createScaleUpAnimationLocked(transit, enter, frame);
                }
                if (this.mNextAppTransitionType == 3 || this.mNextAppTransitionType == 4) {
                    this.mNextAppTransitionScaleUp = this.mNextAppTransitionType == 3;
                    return createThumbnailEnterExitAnimationLocked(getThumbnailTransitionState(enter), frame, transit, taskId);
                } else if (this.mNextAppTransitionType == 5 || this.mNextAppTransitionType == 6) {
                    this.mNextAppTransitionScaleUp = this.mNextAppTransitionType == 5;
                    return createAspectScaledThumbnailEnterExitAnimationLocked(getThumbnailTransitionState(enter), uiMode, orientation, transit, frame, insets, surfaceInsets, freeform, taskId);
                } else {
                    int animAttr = 0;
                    switch (transit) {
                        case 6:
                            if (!enter) {
                                animAttr = 5;
                                break;
                            }
                            animAttr = 4;
                            break;
                        case 7:
                            if (!enter) {
                                animAttr = 7;
                                break;
                            }
                            animAttr = 6;
                            break;
                        case 8:
                        case 19:
                            if (!enter) {
                                animAttr = 9;
                                break;
                            }
                            animAttr = 8;
                            break;
                        case 9:
                            if (!enter) {
                                animAttr = 11;
                                break;
                            }
                            animAttr = 10;
                            break;
                        case 10:
                            if (!enter) {
                                animAttr = 13;
                                break;
                            }
                            animAttr = 12;
                            break;
                        case 11:
                            if (!enter) {
                                animAttr = 15;
                                break;
                            }
                            animAttr = 14;
                            break;
                        case 12:
                            if (!this.mService.mPolicy.isKeyguardShowingOrOccluded()) {
                                if (!enter) {
                                    animAttr = 19;
                                    break;
                                }
                                animAttr = 18;
                                break;
                            }
                            break;
                        case 13:
                            if (!enter) {
                                animAttr = 17;
                                break;
                            }
                            animAttr = 16;
                            break;
                        case 14:
                            if (!enter) {
                                animAttr = 21;
                                break;
                            }
                            animAttr = 20;
                            break;
                        case 15:
                            if (!enter) {
                                animAttr = 23;
                                break;
                            }
                            animAttr = 22;
                            break;
                        case 16:
                            if (!enter) {
                                animAttr = 24;
                                break;
                            }
                            animAttr = 25;
                            break;
                    }
                    return animAttr != 0 ? loadAnimationAttr(lp, animAttr) : null;
                }
            }
        }
    }

    private Animation loadKeyguardExitAnimation(int transit) {
        boolean z = false;
        if ((this.mNextAppTransitionFlags & 2) != 0) {
            return null;
        }
        boolean toShade = (this.mNextAppTransitionFlags & 1) != 0;
        WindowManagerPolicy windowManagerPolicy = this.mService.mPolicy;
        if (transit == 21) {
            z = true;
        }
        return windowManagerPolicy.createHiddenByKeyguardExit(z, toShade);
    }

    int getAppStackClipMode() {
        if (this.mNextAppTransition == 20 || this.mNextAppTransition == 21) {
            return 1;
        }
        int i;
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

    void postAnimationCallback() {
        if (this.mNextAppTransitionCallback != null) {
            this.mService.mH.sendMessage(this.mService.mH.obtainMessage(26, this.mNextAppTransitionCallback));
            this.mNextAppTransitionCallback = null;
        }
    }

    void overridePendingAppTransition(String packageName, int enterAnim, int exitAnim, IRemoteCallback startedCallback) {
        if (isTransitionSet()) {
            clear();
            this.mNextAppTransitionType = 1;
            this.mNextAppTransitionPackage = packageName;
            this.mNextAppTransitionEnter = enterAnim;
            this.mNextAppTransitionExit = exitAnim;
            postAnimationCallback();
            this.mNextAppTransitionCallback = startedCallback;
            return;
        }
        postAnimationCallback();
    }

    void overridePendingAppTransitionScaleUp(int startX, int startY, int startWidth, int startHeight) {
        if (isTransitionSet()) {
            clear();
            this.mNextAppTransitionType = 2;
            putDefaultNextAppTransitionCoordinates(startX, startY, startWidth, startHeight, null);
            postAnimationCallback();
        }
    }

    void overridePendingAppTransitionClipReveal(int startX, int startY, int startWidth, int startHeight) {
        if (isTransitionSet()) {
            clear();
            this.mNextAppTransitionType = 8;
            putDefaultNextAppTransitionCoordinates(startX, startY, startWidth, startHeight, null);
            postAnimationCallback();
        }
    }

    void overridePendingAppTransitionThumb(GraphicBuffer srcThumb, int startX, int startY, IRemoteCallback startedCallback, boolean scaleUp) {
        if (isTransitionSet()) {
            int i;
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
            return;
        }
        postAnimationCallback();
    }

    void overridePendingAppTransitionAspectScaledThumb(GraphicBuffer srcThumb, int startX, int startY, int targetWidth, int targetHeight, IRemoteCallback startedCallback, boolean scaleUp) {
        if (isTransitionSet()) {
            int i;
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
            return;
        }
        postAnimationCallback();
    }

    public void overridePendingAppTransitionMultiThumb(AppTransitionAnimationSpec[] specs, IRemoteCallback onAnimationStartedCallback, IRemoteCallback onAnimationFinishedCallback, boolean scaleUp) {
        if (isTransitionSet()) {
            int i;
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
            return;
        }
        postAnimationCallback();
    }

    void overridePendingAppTransitionMultiThumbFuture(IAppTransitionAnimationSpecsFuture specsFuture, IRemoteCallback callback, boolean scaleUp) {
        if (isTransitionSet()) {
            int i;
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

    void overrideInPlaceAppTransition(String packageName, int anim) {
        if (isTransitionSet()) {
            clear();
            this.mNextAppTransitionType = 7;
            this.mNextAppTransitionPackage = packageName;
            this.mNextAppTransitionInPlace = anim;
            return;
        }
        postAnimationCallback();
    }

    private void fetchAppTransitionSpecsFromFuture() {
        if (this.mNextAppTransitionAnimationsSpecsFuture != null) {
            this.mNextAppTransitionAnimationsSpecsPending = true;
            IAppTransitionAnimationSpecsFuture future = this.mNextAppTransitionAnimationsSpecsFuture;
            this.mNextAppTransitionAnimationsSpecsFuture = null;
            this.mDefaultExecutor.execute(new -$Lambda$g_wW2PVcCyEszs2IV6ABAnl_2r4(this, future));
        }
    }

    /* synthetic */ void lambda$-com_android_server_wm_AppTransition_89645(IAppTransitionAnimationSpecsFuture future) {
        this.mService.mH.removeMessages(102);
        this.mService.mH.sendEmptyMessageDelayed(102, 5000);
        AppTransitionAnimationSpec[] specs = null;
        try {
            Binder.allowBlocking(future.asBinder());
            specs = future.get();
        } catch (RemoteException e) {
            Slog.w(TAG, "Failed to fetch app transition specs: " + e);
        }
        this.mService.mH.removeMessages(102);
        synchronized (this.mService.mWindowMap) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                this.mNextAppTransitionAnimationsSpecsPending = false;
                overridePendingAppTransitionMultiThumb(specs, this.mNextAppTransitionFutureCallback, null, this.mNextAppTransitionScaleUp);
                this.mNextAppTransitionFutureCallback = null;
                if (specs != null) {
                    this.mService.prolongAnimationsFromSpecs(specs, this.mNextAppTransitionScaleUp);
                }
            } finally {
                WindowManagerService.resetPriorityAfterLockedSection();
            }
        }
        this.mService.requestTraversal();
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
            case 19:
                return "TRANSIT_DOCK_TASK_FROM_RECENTS";
            case 20:
                return "TRANSIT_KEYGUARD_GOING_AWAY";
            case 21:
                return "TRANSIT_KEYGUARD_GOING_AWAY_ON_WALLPAPER";
            case 22:
                return "TRANSIT_KEYGUARD_OCCLUDE";
            case 23:
                return "TRANSIT_KEYGUARD_UNOCCLUDE";
            default:
                return "<UNKNOWN>";
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
            default:
                return "unknown type=" + this.mNextAppTransitionType;
        }
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

    boolean prepareAppTransitionLocked(int transit, boolean alwaysKeepCurrent, int flags, boolean forceOverride) {
        if (isKeyguardGoingAwayTransit(transit)) {
            Slog.v(TAG, "clear UnknownApp for KeyguardGoingAwayTransit");
            this.mService.mUnknownAppVisibilityController.clear();
        }
        if (forceOverride || isKeyguardTransit(transit) || (isTransitionSet() ^ 1) != 0 || this.mNextAppTransition == 0) {
            setAppTransition(transit, flags);
        } else if (!(alwaysKeepCurrent || (isKeyguardTransit(transit) ^ 1) == 0)) {
            if (transit == 8 && isTransitionEqual(9)) {
                setAppTransition(transit, flags);
            } else if (transit == 6 && isTransitionEqual(7)) {
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
        if (transit == 20 || transit == 21) {
            return true;
        }
        return false;
    }

    private static boolean isKeyguardTransit(int transit) {
        if (isKeyguardGoingAwayTransit(transit) || transit == 22 || transit == 23) {
            return true;
        }
        return false;
    }

    private boolean shouldScaleDownThumbnailTransition(int uiMode, int orientation) {
        if (this.mGridLayoutRecentsEnabled || orientation == 1) {
            return true;
        }
        return false;
    }
}
