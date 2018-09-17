package com.android.server.wm;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Path;
import android.graphics.Rect;
import android.os.IBinder;
import android.os.IRemoteCallback;
import android.os.RemoteException;
import android.util.ArraySet;
import android.util.Slog;
import android.util.SparseArray;
import android.view.AppTransitionAnimationSpec;
import android.view.IAppTransitionAnimationSpecsFuture;
import android.view.WindowManager.LayoutParams;
import android.view.WindowManagerInternal.AppTransitionListener;
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
    private static final long APP_TRANSITION_TIMEOUT_MS = 5000;
    private static final int CLIP_REVEAL_TRANSLATION_Y_DP = 8;
    static final int DEFAULT_APP_TRANSITION_DURATION = 250;
    static final int HW_APP_TRANSITION_ALPHA_DURATION = 150;
    static final int HW_APP_TRANSITION_SCALE_ENTER_DURATION = 250;
    static final int HW_APP_TRANSITION_SCALE_EXIT_DURATION = 300;
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
    private static final String TAG = null;
    private static final int THUMBNAIL_APP_TRANSITION_DURATION = 336;
    private static final Interpolator THUMBNAIL_DOCK_INTERPOLATOR = null;
    private static final int THUMBNAIL_TRANSITION_ENTER_SCALE_DOWN = 2;
    private static final int THUMBNAIL_TRANSITION_ENTER_SCALE_UP = 0;
    private static final int THUMBNAIL_TRANSITION_EXIT_SCALE_DOWN = 3;
    private static final int THUMBNAIL_TRANSITION_EXIT_SCALE_UP = 1;
    static final Interpolator TOUCH_RESPONSE_INTERPOLATOR = null;
    public static final int TRANSIT_ACTIVITY_CLOSE = 7;
    public static final int TRANSIT_ACTIVITY_OPEN = 6;
    public static final int TRANSIT_ACTIVITY_RELAUNCH = 18;
    public static final int TRANSIT_DOCK_TASK_FROM_RECENTS = 19;
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
    private final Interpolator mAppAlphaInterpolator;
    private final Interpolator mAppScaleInterpolator;
    private int mAppTransitionState;
    private final Interpolator mClipHorizontalInterpolator;
    private final int mClipRevealTranslationY;
    private final int mConfigShortAnimTime;
    private final Context mContext;
    private int mCurrentUserId;
    private final Interpolator mDecelerateInterpolator;
    private final ExecutorService mDefaultExecutor;
    private AppTransitionAnimationSpec mDefaultNextAppTransitionAnimationSpec;
    private final Interpolator mFastOutLinearInInterpolator;
    private final Interpolator mFastOutSlowInInterpolator;
    private int mLastClipRevealMaxTranslation;
    private long mLastClipRevealTransitionDuration;
    private boolean mLastHadClipReveal;
    private final Interpolator mLinearOutSlowInInterpolator;
    private final ArrayList<AppTransitionListener> mListeners;
    private int mNextAppTransition;
    private final SparseArray<AppTransitionAnimationSpec> mNextAppTransitionAnimationsSpecs;
    private IAppTransitionAnimationSpecsFuture mNextAppTransitionAnimationsSpecsFuture;
    private boolean mNextAppTransitionAnimationsSpecsPending;
    private IRemoteCallback mNextAppTransitionCallback;
    private int mNextAppTransitionEnter;
    private int mNextAppTransitionExit;
    private IRemoteCallback mNextAppTransitionFutureCallback;
    private int mNextAppTransitionInPlace;
    private Rect mNextAppTransitionInsets;
    private String mNextAppTransitionPackage;
    private boolean mNextAppTransitionScaleUp;
    private int mNextAppTransitionType;
    private boolean mProlongedAnimationsEnded;
    private final WindowManagerService mService;
    private final Interpolator mThumbnailFadeInInterpolator;
    private final Interpolator mThumbnailFadeOutInterpolator;
    private Rect mTmpFromClipRect;
    private final Rect mTmpRect;
    private Rect mTmpToClipRect;
    private Rect mToScaleRect;

    /* renamed from: com.android.server.wm.AppTransition.3 */
    class AnonymousClass3 implements AnimationListener {
        final /* synthetic */ IRemoteCallback val$callback;

        AnonymousClass3(IRemoteCallback val$callback) {
            this.val$callback = val$callback;
        }

        public void onAnimationStart(Animation animation) {
        }

        public void onAnimationEnd(Animation animation) {
            AppTransition.this.mService.mH.obtainMessage(26, this.val$callback).sendToTarget();
        }

        public void onAnimationRepeat(Animation animation) {
        }
    }

    /* renamed from: com.android.server.wm.AppTransition.4 */
    class AnonymousClass4 implements Runnable {
        final /* synthetic */ IAppTransitionAnimationSpecsFuture val$future;

        AnonymousClass4(IAppTransitionAnimationSpecsFuture val$future) {
            this.val$future = val$future;
        }

        public void run() {
            AppTransitionAnimationSpec[] specs = null;
            try {
                specs = this.val$future.get();
            } catch (RemoteException e) {
                Slog.w(AppTransition.TAG, "Failed to fetch app transition specs: " + e);
            }
            synchronized (AppTransition.this.mService.mWindowMap) {
                AppTransition.this.mNextAppTransitionAnimationsSpecsPending = false;
                AppTransition.this.overridePendingAppTransitionMultiThumb(specs, AppTransition.this.mNextAppTransitionFutureCallback, null, AppTransition.this.mNextAppTransitionScaleUp);
                AppTransition.this.mNextAppTransitionFutureCallback = null;
                if (specs != null) {
                    AppTransition.this.mService.prolongAnimationsFromSpecs(specs, AppTransition.this.mNextAppTransitionScaleUp);
                }
            }
            AppTransition.this.mService.requestTraversal();
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.wm.AppTransition.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.wm.AppTransition.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.wm.AppTransition.<clinit>():void");
    }

    public AppTransition(Context context, WindowManagerService service) {
        this.mNextAppTransition = TRANSIT_UNSET;
        this.mNextAppTransitionType = TRANSIT_NONE;
        this.mNextAppTransitionAnimationsSpecs = new SparseArray();
        this.mNextAppTransitionInsets = new Rect();
        this.mTmpFromClipRect = new Rect();
        this.mTmpToClipRect = new Rect();
        this.mTmpRect = new Rect();
        this.mAppTransitionState = TRANSIT_NONE;
        this.mToScaleRect = new Rect();
        this.mClipHorizontalInterpolator = new PathInterpolator(0.0f, 0.0f, 0.4f, 1.0f);
        this.mCurrentUserId = TRANSIT_NONE;
        this.mLastClipRevealTransitionDuration = 250;
        this.mListeners = new ArrayList();
        this.mDefaultExecutor = Executors.newSingleThreadExecutor();
        this.mContext = context;
        this.mService = service;
        this.mLinearOutSlowInInterpolator = AnimationUtils.loadInterpolator(context, 17563662);
        this.mFastOutLinearInInterpolator = AnimationUtils.loadInterpolator(context, 17563663);
        this.mFastOutSlowInInterpolator = AnimationUtils.loadInterpolator(context, 17563661);
        this.mConfigShortAnimTime = context.getResources().getInteger(17694720);
        this.mDecelerateInterpolator = AnimationUtils.loadInterpolator(context, 17563651);
        this.mAppScaleInterpolator = new PathInterpolator(0.3f, 0.15f, 0.1f, 0.85f);
        this.mAppAlphaInterpolator = new PathInterpolator(0.3f, 0.15f, 0.1f, 0.85f);
        this.mThumbnailFadeInInterpolator = new Interpolator() {
            public float getInterpolation(float input) {
                if (input < AppTransition.RECENTS_THUMBNAIL_FADEOUT_FRACTION) {
                    return 0.0f;
                }
                return AppTransition.this.mFastOutLinearInInterpolator.getInterpolation((input - AppTransition.RECENTS_THUMBNAIL_FADEOUT_FRACTION) / AppTransition.RECENTS_THUMBNAIL_FADEOUT_FRACTION);
            }
        };
        this.mThumbnailFadeOutInterpolator = new Interpolator() {
            public float getInterpolation(float input) {
                if (input >= AppTransition.RECENTS_THUMBNAIL_FADEOUT_FRACTION) {
                    return 1.0f;
                }
                return AppTransition.this.mLinearOutSlowInInterpolator.getInterpolation(input / AppTransition.RECENTS_THUMBNAIL_FADEOUT_FRACTION);
            }
        };
        this.mClipRevealTranslationY = (int) (this.mContext.getResources().getDisplayMetrics().density * 8.0f);
    }

    boolean isTransitionSet() {
        return this.mNextAppTransition != TRANSIT_UNSET;
    }

    boolean isTransitionEqual(int transit) {
        return this.mNextAppTransition == transit;
    }

    int getAppTransition() {
        return this.mNextAppTransition;
    }

    private void setAppTransition(int transit) {
        this.mNextAppTransition = transit;
    }

    boolean isReady() {
        if (this.mAppTransitionState == THUMBNAIL_TRANSITION_EXIT_SCALE_UP || this.mAppTransitionState == THUMBNAIL_TRANSITION_EXIT_SCALE_DOWN) {
            return true;
        }
        return false;
    }

    void setReady() {
        this.mAppTransitionState = THUMBNAIL_TRANSITION_EXIT_SCALE_UP;
        fetchAppTransitionSpecsFromFuture();
    }

    boolean isRunning() {
        return this.mAppTransitionState == THUMBNAIL_TRANSITION_ENTER_SCALE_DOWN;
    }

    void setIdle() {
        this.mAppTransitionState = TRANSIT_NONE;
    }

    boolean isTimeout() {
        return this.mAppTransitionState == THUMBNAIL_TRANSITION_EXIT_SCALE_DOWN;
    }

    void setTimeout() {
        this.mAppTransitionState = THUMBNAIL_TRANSITION_EXIT_SCALE_DOWN;
    }

    Bitmap getAppTransitionThumbnailHeader(int taskId) {
        AppTransitionAnimationSpec spec = (AppTransitionAnimationSpec) this.mNextAppTransitionAnimationsSpecs.get(taskId);
        if (spec == null) {
            spec = this.mDefaultNextAppTransitionAnimationSpec;
        }
        if (spec != null) {
            return spec.bitmap;
        }
        return null;
    }

    boolean isNextThumbnailTransitionAspectScaled() {
        if (this.mNextAppTransitionType == NEXT_TRANSIT_TYPE_THUMBNAIL_ASPECT_SCALE_UP || this.mNextAppTransitionType == TRANSIT_ACTIVITY_OPEN) {
            return true;
        }
        return false;
    }

    boolean isNextThumbnailTransitionScaleUp() {
        return this.mNextAppTransitionScaleUp;
    }

    boolean isNextAppTransitionThumbnailUp() {
        if (this.mNextAppTransitionType == THUMBNAIL_TRANSITION_EXIT_SCALE_DOWN || this.mNextAppTransitionType == NEXT_TRANSIT_TYPE_THUMBNAIL_ASPECT_SCALE_UP) {
            return true;
        }
        return false;
    }

    boolean isNextAppTransitionThumbnailDown() {
        if (this.mNextAppTransitionType == NEXT_TRANSIT_TYPE_THUMBNAIL_SCALE_DOWN || this.mNextAppTransitionType == TRANSIT_ACTIVITY_OPEN) {
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
        this.mAppTransitionState = TRANSIT_NONE;
        notifyAppTransitionPendingLocked();
        this.mLastHadClipReveal = false;
        this.mLastClipRevealMaxTranslation = TRANSIT_NONE;
        this.mLastClipRevealTransitionDuration = 250;
        return true;
    }

    void goodToGo(AppWindowAnimator topOpeningAppAnimator, AppWindowAnimator topClosingAppAnimator, ArraySet<AppWindowToken> openingApps, ArraySet<AppWindowToken> arraySet) {
        IBinder iBinder;
        IBinder iBinder2;
        Animation animation;
        Animation animation2 = null;
        this.mNextAppTransition = TRANSIT_UNSET;
        this.mAppTransitionState = THUMBNAIL_TRANSITION_ENTER_SCALE_DOWN;
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
        notifyAppTransitionStartingLocked(iBinder, iBinder2, animation, animation2);
        this.mService.getDefaultDisplayContentLocked().getDockedDividerController().notifyAppTransitionStarting();
        if (this.mNextAppTransition == TRANSIT_DOCK_TASK_FROM_RECENTS && !this.mProlongedAnimationsEnded) {
            for (int i = openingApps.size() + TRANSIT_UNSET; i >= 0; i += TRANSIT_UNSET) {
                ((AppWindowToken) openingApps.valueAt(i)).mAppAnimator.startProlongAnimation(THUMBNAIL_TRANSITION_ENTER_SCALE_DOWN);
            }
        }
    }

    void notifyProlongedAnimationsEnded() {
        this.mProlongedAnimationsEnded = true;
    }

    void clear() {
        this.mNextAppTransitionType = TRANSIT_NONE;
        this.mNextAppTransitionPackage = null;
        this.mNextAppTransitionAnimationsSpecs.clear();
        this.mNextAppTransitionAnimationsSpecsFuture = null;
        this.mDefaultNextAppTransitionAnimationSpec = null;
        this.mAnimationFinishedCallback = null;
        this.mProlongedAnimationsEnded = false;
    }

    void freeze() {
        setAppTransition(TRANSIT_UNSET);
        clear();
        setReady();
        notifyAppTransitionCancelledLocked();
    }

    void registerListenerLocked(AppTransitionListener listener) {
        this.mListeners.add(listener);
    }

    public void notifyAppTransitionFinishedLocked(IBinder token) {
        for (int i = TRANSIT_NONE; i < this.mListeners.size(); i += THUMBNAIL_TRANSITION_EXIT_SCALE_UP) {
            ((AppTransitionListener) this.mListeners.get(i)).onAppTransitionFinishedLocked(token);
        }
    }

    private void notifyAppTransitionPendingLocked() {
        for (int i = TRANSIT_NONE; i < this.mListeners.size(); i += THUMBNAIL_TRANSITION_EXIT_SCALE_UP) {
            ((AppTransitionListener) this.mListeners.get(i)).onAppTransitionPendingLocked();
        }
    }

    private void notifyAppTransitionCancelledLocked() {
        for (int i = TRANSIT_NONE; i < this.mListeners.size(); i += THUMBNAIL_TRANSITION_EXIT_SCALE_UP) {
            ((AppTransitionListener) this.mListeners.get(i)).onAppTransitionCancelledLocked();
        }
    }

    private void notifyAppTransitionStartingLocked(IBinder openToken, IBinder closeToken, Animation openAnimation, Animation closeAnimation) {
        for (int i = TRANSIT_NONE; i < this.mListeners.size(); i += THUMBNAIL_TRANSITION_EXIT_SCALE_UP) {
            ((AppTransitionListener) this.mListeners.get(i)).onAppTransitionStartingLocked(openToken, closeToken, openAnimation, closeAnimation);
        }
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
        int anim = TRANSIT_NONE;
        Context context = this.mContext;
        if (animAttr >= 0) {
            Entry ent = getCachedAnimations(lp);
            if (ent != null) {
                context = ent.context;
                anim = ent.array.getResourceId(animAttr, TRANSIT_NONE);
            }
            ent = HwServiceFactory.getHwAppTransition().overrideAnimation(lp, animAttr, this.mContext, ent, this);
            if (ent != null) {
                context = ent.context;
                anim = ent.array.getResourceId(animAttr, TRANSIT_NONE);
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
        int anim = TRANSIT_NONE;
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

    private Animation createHwExitAnimation(Rect containingFrame) {
        int appWidth = containingFrame.width();
        float scaleW = ((float) this.mToScaleRect.width()) / ((float) appWidth);
        float scaleH = ((float) this.mToScaleRect.height()) / ((float) containingFrame.height());
        AlphaAnimation alphaAnim = new AlphaAnimation(1.0f, 0.0f);
        alphaAnim.setFillAfter(true);
        alphaAnim.setFillBefore(true);
        alphaAnim.setFillEnabled(true);
        alphaAnim.setDuration(150);
        alphaAnim.setStartOffset(150);
        alphaAnim.setInterpolator(this.mAppAlphaInterpolator);
        ScaleAnimation scaleAnimation = new ScaleAnimation(1.0f, scaleW, 1.0f, scaleH, computePivot(this.mToScaleRect.left, scaleW), computePivot(this.mToScaleRect.top, scaleH));
        scaleAnimation.setFillAfter(true);
        scaleAnimation.setFillBefore(true);
        scaleAnimation.setFillEnabled(true);
        scaleAnimation.setInterpolator(this.mAppScaleInterpolator);
        scaleAnimation.setDuration(300);
        AnimationSet animSet = new AnimationSet(false);
        animSet.addAnimation(alphaAnim);
        animSet.addAnimation(scaleAnimation);
        animSet.setZAdjustment(THUMBNAIL_TRANSITION_EXIT_SCALE_UP);
        return animSet;
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
            scale.setInterpolator(this.mAppScaleInterpolator);
            scale.setDuration(250);
            Animation alpha = new AlphaAnimation(0.0f, 1.0f);
            alpha.setInterpolator(this.mAppAlphaInterpolator);
            alpha.setDuration(150);
            Animation set = new AnimationSet(false);
            set.addAnimation(scale);
            set.addAnimation(alpha);
            set.setDetachWallpaper(true);
            a = set;
        } else if (transit == TRANSIT_WALLPAPER_INTRA_OPEN || transit == TRANSIT_WALLPAPER_INTRA_CLOSE) {
            a = new AlphaAnimation(1.0f, 0.0f);
            a.setDetachWallpaper(true);
        } else {
            a = new AlphaAnimation(1.0f, 1.0f);
        }
        switch (transit) {
            case TRANSIT_ACTIVITY_OPEN /*6*/:
            case TRANSIT_ACTIVITY_CLOSE /*7*/:
                duration = (long) this.mConfigShortAnimTime;
                break;
            default:
                duration = 250;
                break;
        }
        if (!enter) {
            a.setDuration(duration);
        }
        a.setFillAfter(true);
        a.setInterpolator(this.mDecelerateInterpolator);
        a.initialize(appWidth, appHeight, appWidth, appHeight);
        return a;
    }

    private void getDefaultNextAppTransitionStartRect(Rect rect) {
        if (this.mDefaultNextAppTransitionAnimationSpec == null || this.mDefaultNextAppTransitionAnimationSpec.rect == null) {
            Slog.wtf(TAG, "Starting rect for app requested, but none available", new Throwable());
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
            Slog.wtf(TAG, "Starting rect for task: " + taskId + " requested, but not available", new Throwable());
            rect.setEmpty();
            return;
        }
        rect.set(spec.rect);
    }

    private void putDefaultNextAppTransitionCoordinates(int left, int top, int width, int height, Bitmap bitmap) {
        this.mDefaultNextAppTransitionAnimationSpec = new AppTransitionAnimationSpec(TRANSIT_UNSET, bitmap, new Rect(left, top, left + width, top + height));
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
            int translationX = TRANSIT_NONE;
            int translationYCorrection = translationY;
            int centerX = this.mTmpRect.centerX();
            int centerY = this.mTmpRect.centerY();
            int halfWidth = this.mTmpRect.width() / THUMBNAIL_TRANSITION_ENTER_SCALE_DOWN;
            int halfHeight = this.mTmpRect.height() / THUMBNAIL_TRANSITION_ENTER_SCALE_DOWN;
            int clipStartX = (centerX - halfWidth) - appFrame.left;
            int clipStartY = (centerY - halfHeight) - appFrame.top;
            boolean cutOff = false;
            if (appFrame.top > centerY - halfHeight) {
                translationY = (centerY - halfHeight) - appFrame.top;
                translationYCorrection = TRANSIT_NONE;
                clipStartY = TRANSIT_NONE;
                cutOff = true;
            }
            if (appFrame.left > centerX - halfWidth) {
                translationX = (centerX - halfWidth) - appFrame.left;
                clipStartX = TRANSIT_NONE;
                cutOff = true;
            }
            if (appFrame.right < centerX + halfWidth) {
                translationX = (centerX + halfWidth) - appFrame.right;
                clipStartX = appWidth - this.mTmpRect.width();
                cutOff = true;
            }
            duration = calculateClipRevealTransitionDuration(cutOff, (float) translationX, (float) translationY, displayFrame);
            Animation clipRectLRAnimation = new ClipRectLRAnimation(clipStartX, this.mTmpRect.width() + clipStartX, TRANSIT_NONE, appWidth);
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
            Animation clipAnimTB = new ClipRectTBAnimation(clipStartY, this.mTmpRect.height() + clipStartY, TRANSIT_NONE, appHeight, translationYCorrection, TRANSIT_NONE, this.mLinearOutSlowInInterpolator);
            clipAnimTB.setInterpolator(TOUCH_RESPONSE_INTERPOLATOR);
            clipAnimTB.setDuration(duration);
            long alphaDuration = duration / 4;
            AlphaAnimation alpha = new AlphaAnimation(RECENTS_THUMBNAIL_FADEOUT_FRACTION, 1.0f);
            alpha.setDuration(alphaDuration);
            alpha.setInterpolator(this.mLinearOutSlowInInterpolator);
            clipRectLRAnimation = new AnimationSet(false);
            clipRectLRAnimation.addAnimation(clipRectLRAnimation);
            clipRectLRAnimation.addAnimation(clipAnimTB);
            clipRectLRAnimation.addAnimation(clipRectLRAnimation);
            clipRectLRAnimation.addAnimation(alpha);
            clipRectLRAnimation.setZAdjustment(THUMBNAIL_TRANSITION_EXIT_SCALE_UP);
            clipRectLRAnimation.initialize(appWidth, appHeight, appWidth, appHeight);
            anim = clipRectLRAnimation;
            this.mLastHadClipReveal = true;
            this.mLastClipRevealTransitionDuration = duration;
            this.mLastClipRevealMaxTranslation = cutOff ? Math.max(Math.abs(translationY), Math.abs(translationX)) : TRANSIT_NONE;
        } else {
            switch (transit) {
                case TRANSIT_ACTIVITY_OPEN /*6*/:
                case TRANSIT_ACTIVITY_CLOSE /*7*/:
                    duration = (long) this.mConfigShortAnimTime;
                    break;
                default:
                    duration = 250;
                    break;
            }
            if (transit == TRANSIT_WALLPAPER_INTRA_OPEN || transit == TRANSIT_WALLPAPER_INTRA_CLOSE) {
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
            case TRANSIT_ACTIVITY_OPEN /*6*/:
            case TRANSIT_ACTIVITY_CLOSE /*7*/:
                duration = this.mConfigShortAnimTime;
                break;
            default:
                duration = HW_APP_TRANSITION_SCALE_ENTER_DURATION;
                break;
        }
        return prepareThumbnailAnimationWithDuration(a, appWidth, appHeight, (long) duration, this.mDecelerateInterpolator);
    }

    int getThumbnailTransitionState(boolean enter) {
        if (enter) {
            if (this.mNextAppTransitionScaleUp) {
                return TRANSIT_NONE;
            }
            return THUMBNAIL_TRANSITION_ENTER_SCALE_DOWN;
        } else if (this.mNextAppTransitionScaleUp) {
            return THUMBNAIL_TRANSITION_EXIT_SCALE_UP;
        } else {
            return THUMBNAIL_TRANSITION_EXIT_SCALE_DOWN;
        }
    }

    Animation createThumbnailAspectScaleAnimationLocked(Rect appRect, Rect contentInsets, Bitmap thumbnailHeader, int taskId, int uiMode, int orientation) {
        float fromX;
        float fromY;
        float toX;
        float toY;
        float pivotX;
        float pivotY;
        Animation a;
        int thumbWidthI = thumbnailHeader.getWidth();
        float thumbWidth = (float) (thumbWidthI > 0 ? thumbWidthI : THUMBNAIL_TRANSITION_EXIT_SCALE_UP);
        int thumbHeightI = thumbnailHeader.getHeight();
        int appWidth = appRect.width();
        float scaleW = ((float) appWidth) / thumbWidth;
        getNextAppTransitionStartRect(taskId, this.mTmpRect);
        if (isTvUiMode(uiMode) || orientation == THUMBNAIL_TRANSITION_EXIT_SCALE_UP) {
            fromX = (float) this.mTmpRect.left;
            fromY = (float) this.mTmpRect.top;
            toX = (((float) (this.mTmpRect.width() / THUMBNAIL_TRANSITION_ENTER_SCALE_DOWN)) * (scaleW - 1.0f)) + ((float) appRect.left);
            toY = (((float) (appRect.height() / THUMBNAIL_TRANSITION_ENTER_SCALE_DOWN)) * (1.0f - (1.0f / scaleW))) + ((float) appRect.top);
            pivotX = (float) (this.mTmpRect.width() / THUMBNAIL_TRANSITION_ENTER_SCALE_DOWN);
            pivotY = ((float) (appRect.height() / THUMBNAIL_TRANSITION_ENTER_SCALE_DOWN)) / scaleW;
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
            alphaAnimation.setInterpolator(this.mNextAppTransition == TRANSIT_DOCK_TASK_FROM_RECENTS ? THUMBNAIL_DOCK_INTERPOLATOR : this.mThumbnailFadeOutInterpolator);
            if (this.mNextAppTransition == TRANSIT_DOCK_TASK_FROM_RECENTS) {
                j = duration / 2;
            } else {
                j = duration;
            }
            alphaAnimation.setDuration(j);
            translate = createCurvedMotion(fromX, toX, fromY, toY);
            translate.setInterpolator(interpolator);
            translate.setDuration(duration);
            this.mTmpFromClipRect.set(TRANSIT_NONE, TRANSIT_NONE, thumbWidthI, thumbHeightI);
            this.mTmpToClipRect.set(appRect);
            this.mTmpToClipRect.offsetTo(TRANSIT_NONE, TRANSIT_NONE);
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
            alphaAnimation.addAnimation(alphaAnimation);
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
            alphaAnimation.addAnimation(alphaAnimation);
            alphaAnimation.addAnimation(translate);
            a = alphaAnimation;
        }
        return prepareThumbnailAnimationWithDuration(a, appWidth, appRect.height(), 0, null);
    }

    private Animation createCurvedMotion(float fromX, float toX, float fromY, float toY) {
        if (Math.abs(toX - fromX) < 1.0f || this.mNextAppTransition != TRANSIT_DOCK_TASK_FROM_RECENTS) {
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
        if (this.mNextAppTransition == TRANSIT_DOCK_TASK_FROM_RECENTS) {
            return 453;
        }
        return 336;
    }

    private Interpolator getAspectScaleInterpolator() {
        if (this.mNextAppTransition == TRANSIT_DOCK_TASK_FROM_RECENTS) {
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
        float thumbWidth = (float) (thumbWidthI > 0 ? thumbWidthI : THUMBNAIL_TRANSITION_EXIT_SCALE_UP);
        int thumbHeightI = this.mTmpRect.height();
        float thumbHeight = (float) (thumbHeightI > 0 ? thumbHeightI : THUMBNAIL_TRANSITION_EXIT_SCALE_UP);
        int thumbStartX = this.mTmpRect.left - containingFrame.left;
        int thumbStartY = this.mTmpRect.top - containingFrame.top;
        switch (thumbTransitState) {
            case TRANSIT_NONE /*0*/:
            case THUMBNAIL_TRANSITION_EXIT_SCALE_DOWN /*3*/:
                boolean scaleUp = thumbTransitState == 0;
                if (!freeform || !scaleUp) {
                    if (!freeform) {
                        Animation animationSet = new AnimationSet(true);
                        this.mTmpFromClipRect.set(containingFrame);
                        this.mTmpToClipRect.set(containingFrame);
                        this.mTmpFromClipRect.offsetTo(TRANSIT_NONE, TRANSIT_NONE);
                        this.mTmpToClipRect.offsetTo(TRANSIT_NONE, TRANSIT_NONE);
                        this.mTmpFromClipRect.inset(contentInsets);
                        this.mNextAppTransitionInsets.set(contentInsets);
                        Animation clipAnim;
                        Animation translateAnim;
                        if (isTvUiMode(uiMode) || orientation == THUMBNAIL_TRANSITION_EXIT_SCALE_UP) {
                            float f;
                            float scale = thumbWidth / ((float) ((appWidth - contentInsets.left) - contentInsets.right));
                            this.mTmpFromClipRect.bottom = this.mTmpFromClipRect.top + ((int) (thumbHeight / scale));
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
                        animationSet.setZAdjustment(THUMBNAIL_TRANSITION_EXIT_SCALE_UP);
                        break;
                    }
                    a = createAspectScaledThumbnailExitFreeformAnimationLocked(containingFrame, surfaceInsets, taskId);
                    break;
                }
                a = createAspectScaledThumbnailEnterFreeformAnimationLocked(containingFrame, surfaceInsets, taskId);
                break;
                break;
            case THUMBNAIL_TRANSITION_EXIT_SCALE_UP /*1*/:
                if (transit != TRANSIT_WALLPAPER_INTRA_OPEN) {
                    a = new AlphaAnimation(1.0f, 1.0f);
                    break;
                }
                a = new AlphaAnimation(1.0f, 0.0f);
                break;
            case THUMBNAIL_TRANSITION_ENTER_SCALE_DOWN /*2*/:
                if (transit != TRANSIT_WALLPAPER_INTRA_OPEN) {
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
        int surfaceInsetsH = surfaceInsets == null ? TRANSIT_NONE : surfaceInsets.left + surfaceInsets.right;
        int surfaceInsetsV = surfaceInsets == null ? TRANSIT_NONE : surfaceInsets.top + surfaceInsets.bottom;
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
        int sourceHCenter = sourceFrame.left + (sourceFrame.width() / THUMBNAIL_TRANSITION_ENTER_SCALE_DOWN);
        int sourceVCenter = sourceFrame.top + (sourceFrame.height() / THUMBNAIL_TRANSITION_ENTER_SCALE_DOWN);
        int destHCenter = destFrame.left + (destFrame.width() / THUMBNAIL_TRANSITION_ENTER_SCALE_DOWN);
        int destVCenter = destFrame.top + (destFrame.height() / THUMBNAIL_TRANSITION_ENTER_SCALE_DOWN);
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
            animationSet.setAnimationListener(new AnonymousClass3(callback));
        }
        return animationSet;
    }

    Animation createThumbnailScaleAnimationLocked(int appWidth, int appHeight, int transit, Bitmap thumbnailHeader) {
        Animation a;
        getDefaultNextAppTransitionStartRect(this.mTmpRect);
        int thumbWidthI = thumbnailHeader.getWidth();
        if (thumbWidthI <= 0) {
            thumbWidthI = THUMBNAIL_TRANSITION_EXIT_SCALE_UP;
        }
        float thumbWidth = (float) thumbWidthI;
        int thumbHeightI = thumbnailHeader.getHeight();
        if (thumbHeightI <= 0) {
            thumbHeightI = THUMBNAIL_TRANSITION_EXIT_SCALE_UP;
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
        int thumbWidthI;
        int thumbHeightI;
        Animation a;
        int appWidth = containingFrame.width();
        int appHeight = containingFrame.height();
        Bitmap thumbnailHeader = getAppTransitionThumbnailHeader(taskId);
        getDefaultNextAppTransitionStartRect(this.mTmpRect);
        if (thumbnailHeader != null) {
            thumbWidthI = thumbnailHeader.getWidth();
        } else {
            thumbWidthI = appWidth;
        }
        if (thumbWidthI <= 0) {
            thumbWidthI = THUMBNAIL_TRANSITION_EXIT_SCALE_UP;
        }
        float thumbWidth = (float) thumbWidthI;
        if (thumbnailHeader != null) {
            thumbHeightI = thumbnailHeader.getHeight();
        } else {
            thumbHeightI = appHeight;
        }
        if (thumbHeightI <= 0) {
            thumbHeightI = THUMBNAIL_TRANSITION_EXIT_SCALE_UP;
        }
        float thumbHeight = (float) thumbHeightI;
        float scaleW;
        float scaleH;
        switch (thumbTransitState) {
            case TRANSIT_NONE /*0*/:
                scaleW = thumbWidth / ((float) appWidth);
                scaleH = thumbHeight / ((float) appHeight);
                a = new ScaleAnimation(scaleW, 1.0f, scaleH, 1.0f, computePivot(this.mTmpRect.left, scaleW), computePivot(this.mTmpRect.top, scaleH));
                break;
            case THUMBNAIL_TRANSITION_EXIT_SCALE_UP /*1*/:
                if (transit != TRANSIT_WALLPAPER_INTRA_OPEN) {
                    a = new AlphaAnimation(1.0f, 1.0f);
                    break;
                }
                a = new AlphaAnimation(1.0f, 0.0f);
                break;
            case THUMBNAIL_TRANSITION_ENTER_SCALE_DOWN /*2*/:
                a = new AlphaAnimation(1.0f, 1.0f);
                break;
            case THUMBNAIL_TRANSITION_EXIT_SCALE_DOWN /*3*/:
                scaleW = thumbWidth / ((float) appWidth);
                scaleH = thumbHeight / ((float) appHeight);
                Animation scale = new ScaleAnimation(1.0f, scaleW, 1.0f, scaleH, computePivot(this.mTmpRect.left, scaleW), computePivot(this.mTmpRect.top, scaleH));
                Animation alpha = new AlphaAnimation(1.0f, 0.0f);
                Animation animationSet = new AnimationSet(true);
                animationSet.addAnimation(scale);
                animationSet.addAnimation(alpha);
                animationSet.setZAdjustment(THUMBNAIL_TRANSITION_EXIT_SCALE_UP);
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
        this.mTmpToClipRect.set(TRANSIT_NONE, TRANSIT_NONE, containingFrame.width(), containingFrame.height());
        AnimationSet set = new AnimationSet(true);
        float fromWidth = (float) this.mTmpFromClipRect.width();
        float toWidth = (float) this.mTmpToClipRect.width();
        float fromHeight = (float) this.mTmpFromClipRect.height();
        float toHeight = (float) ((this.mTmpToClipRect.height() - contentInsets.top) - contentInsets.bottom);
        int translateAdjustment = TRANSIT_NONE;
        if (fromWidth > toWidth || fromHeight > toHeight) {
            set.addAnimation(new ScaleAnimation(fromWidth / toWidth, 1.0f, fromHeight / toHeight, 1.0f));
            translateAdjustment = (int) ((((float) contentInsets.top) * fromHeight) / toHeight);
        } else {
            set.addAnimation(new ClipRectAnimation(this.mTmpFromClipRect, this.mTmpToClipRect));
        }
        set.addAnimation(new TranslateAnimation((float) (left - containingFrame.left), 0.0f, (float) ((top - containingFrame.top) - translateAdjustment), 0.0f));
        set.setDuration(250);
        set.setZAdjustment(THUMBNAIL_TRANSITION_EXIT_SCALE_UP);
        return set;
    }

    boolean canSkipFirstFrame() {
        if (this.mNextAppTransitionType == THUMBNAIL_TRANSITION_EXIT_SCALE_UP || this.mNextAppTransitionType == TRANSIT_ACTIVITY_CLOSE) {
            return false;
        }
        return this.mNextAppTransitionType != TRANSIT_TASK_OPEN;
    }

    Animation loadAnimation(LayoutParams lp, int transit, boolean enter, int uiMode, int orientation, Rect frame, Rect displayFrame, Rect insets, Rect surfaceInsets, boolean isVoiceInteraction, boolean freeform, int taskId) {
        int i;
        if (isVoiceInteraction && (transit == TRANSIT_ACTIVITY_OPEN || transit == TRANSIT_TASK_OPEN || transit == TRANSIT_TASK_TO_FRONT)) {
            if (enter) {
                i = 17432735;
            } else {
                i = 17432736;
            }
            return loadAnimationRes(lp, i);
        } else if (isVoiceInteraction && (transit == TRANSIT_ACTIVITY_CLOSE || transit == TRANSIT_TASK_CLOSE || transit == TRANSIT_TASK_TO_BACK)) {
            if (enter) {
                i = 17432733;
            } else {
                i = 17432734;
            }
            return loadAnimationRes(lp, i);
        } else if (transit == TRANSIT_ACTIVITY_RELAUNCH) {
            return createRelaunchAnimation(frame, insets);
        } else {
            if (this.mNextAppTransitionType == THUMBNAIL_TRANSITION_EXIT_SCALE_UP) {
                return loadAnimationRes(this.mNextAppTransitionPackage, enter ? this.mNextAppTransitionEnter : this.mNextAppTransitionExit);
            } else if (this.mNextAppTransitionType == TRANSIT_ACTIVITY_CLOSE) {
                return loadAnimationRes(this.mNextAppTransitionPackage, this.mNextAppTransitionInPlace);
            } else if (this.mNextAppTransitionType == TRANSIT_TASK_OPEN) {
                return createClipRevealAnimationLocked(transit, enter, frame, displayFrame);
            } else {
                if (this.mNextAppTransitionType == THUMBNAIL_TRANSITION_ENTER_SCALE_DOWN) {
                    return createScaleUpAnimationLocked(transit, enter, frame);
                }
                if (this.mNextAppTransitionType == THUMBNAIL_TRANSITION_EXIT_SCALE_DOWN || this.mNextAppTransitionType == NEXT_TRANSIT_TYPE_THUMBNAIL_SCALE_DOWN) {
                    this.mNextAppTransitionScaleUp = this.mNextAppTransitionType == THUMBNAIL_TRANSITION_EXIT_SCALE_DOWN;
                    return createThumbnailEnterExitAnimationLocked(getThumbnailTransitionState(enter), frame, transit, taskId);
                } else if (this.mNextAppTransitionType == NEXT_TRANSIT_TYPE_THUMBNAIL_ASPECT_SCALE_UP || this.mNextAppTransitionType == TRANSIT_ACTIVITY_OPEN) {
                    this.mNextAppTransitionScaleUp = this.mNextAppTransitionType == NEXT_TRANSIT_TYPE_THUMBNAIL_ASPECT_SCALE_UP;
                    return createAspectScaledThumbnailEnterExitAnimationLocked(getThumbnailTransitionState(enter), uiMode, orientation, transit, frame, insets, surfaceInsets, freeform, taskId);
                } else if ((transit != TRANSIT_WALLPAPER_OPEN && transit != TRANSIT_WALLPAPER_INTRA_OPEN) || enter || this.mToScaleRect.isEmpty()) {
                    int animAttr = TRANSIT_NONE;
                    switch (transit) {
                        case TRANSIT_ACTIVITY_OPEN /*6*/:
                            if (!enter) {
                                animAttr = NEXT_TRANSIT_TYPE_THUMBNAIL_ASPECT_SCALE_UP;
                                break;
                            }
                            animAttr = NEXT_TRANSIT_TYPE_THUMBNAIL_SCALE_DOWN;
                            break;
                        case TRANSIT_ACTIVITY_CLOSE /*7*/:
                            if (!enter) {
                                animAttr = TRANSIT_ACTIVITY_CLOSE;
                                break;
                            }
                            animAttr = TRANSIT_ACTIVITY_OPEN;
                            break;
                        case TRANSIT_TASK_OPEN /*8*/:
                        case TRANSIT_DOCK_TASK_FROM_RECENTS /*19*/:
                            if (!enter) {
                                animAttr = TRANSIT_TASK_CLOSE;
                                break;
                            }
                            animAttr = TRANSIT_TASK_OPEN;
                            break;
                        case TRANSIT_TASK_CLOSE /*9*/:
                            if (!enter) {
                                animAttr = TRANSIT_TASK_TO_BACK;
                                break;
                            }
                            animAttr = TRANSIT_TASK_TO_FRONT;
                            break;
                        case TRANSIT_TASK_TO_FRONT /*10*/:
                            if (!enter) {
                                animAttr = TRANSIT_WALLPAPER_OPEN;
                                break;
                            }
                            animAttr = TRANSIT_WALLPAPER_CLOSE;
                            break;
                        case TRANSIT_TASK_TO_BACK /*11*/:
                            if (!enter) {
                                animAttr = TRANSIT_WALLPAPER_INTRA_CLOSE;
                                break;
                            }
                            animAttr = TRANSIT_WALLPAPER_INTRA_OPEN;
                            break;
                        case TRANSIT_WALLPAPER_CLOSE /*12*/:
                            if (!enter) {
                                animAttr = TRANSIT_DOCK_TASK_FROM_RECENTS;
                                break;
                            }
                            animAttr = TRANSIT_ACTIVITY_RELAUNCH;
                            break;
                        case TRANSIT_WALLPAPER_OPEN /*13*/:
                            if (!enter) {
                                animAttr = TRANSIT_TASK_IN_PLACE;
                                break;
                            }
                            animAttr = TRANSIT_TASK_OPEN_BEHIND;
                            break;
                        case TRANSIT_WALLPAPER_INTRA_OPEN /*14*/:
                            if (!enter) {
                                animAttr = 21;
                                break;
                            }
                            animAttr = 20;
                            break;
                        case TRANSIT_WALLPAPER_INTRA_CLOSE /*15*/:
                            if (!enter) {
                                animAttr = 23;
                                break;
                            }
                            animAttr = 22;
                            break;
                        case TRANSIT_TASK_OPEN_BEHIND /*16*/:
                            if (!enter) {
                                animAttr = 24;
                                break;
                            }
                            animAttr = 25;
                            break;
                    }
                    return animAttr != 0 ? loadAnimationAttr(lp, animAttr) : null;
                } else {
                    Animation createHwExitAnimation = createHwExitAnimation(frame);
                    this.mToScaleRect = new Rect();
                    return createHwExitAnimation;
                }
            }
        }
    }

    int getAppStackClipMode() {
        if (this.mNextAppTransition == TRANSIT_ACTIVITY_RELAUNCH || this.mNextAppTransition == TRANSIT_DOCK_TASK_FROM_RECENTS || this.mNextAppTransitionType == TRANSIT_TASK_OPEN) {
            return THUMBNAIL_TRANSITION_ENTER_SCALE_DOWN;
        }
        return TRANSIT_NONE;
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
            this.mNextAppTransitionType = THUMBNAIL_TRANSITION_EXIT_SCALE_UP;
            this.mNextAppTransitionPackage = packageName;
            this.mNextAppTransitionEnter = enterAnim;
            this.mNextAppTransitionExit = exitAnim;
            postAnimationCallback();
            this.mNextAppTransitionCallback = startedCallback;
            return;
        }
        postAnimationCallback();
    }

    void setExitPosition(int startX, int startY, int width, int height) {
        this.mToScaleRect.left = startX;
        this.mToScaleRect.top = startY;
        this.mToScaleRect.right = startX + width;
        this.mToScaleRect.bottom = startY + height;
    }

    void overridePendingAppTransitionScaleUp(int startX, int startY, int startWidth, int startHeight) {
        if (isTransitionSet()) {
            clear();
            this.mNextAppTransitionType = THUMBNAIL_TRANSITION_ENTER_SCALE_DOWN;
            putDefaultNextAppTransitionCoordinates(startX, startY, startWidth, startHeight, null);
            postAnimationCallback();
        }
    }

    void overridePendingAppTransitionClipReveal(int startX, int startY, int startWidth, int startHeight) {
        if (isTransitionSet()) {
            clear();
            this.mNextAppTransitionType = TRANSIT_TASK_OPEN;
            putDefaultNextAppTransitionCoordinates(startX, startY, startWidth, startHeight, null);
            postAnimationCallback();
        }
    }

    void overridePendingAppTransitionThumb(Bitmap srcThumb, int startX, int startY, IRemoteCallback startedCallback, boolean scaleUp) {
        if (isTransitionSet()) {
            int i;
            clear();
            if (scaleUp) {
                i = THUMBNAIL_TRANSITION_EXIT_SCALE_DOWN;
            } else {
                i = NEXT_TRANSIT_TYPE_THUMBNAIL_SCALE_DOWN;
            }
            this.mNextAppTransitionType = i;
            this.mNextAppTransitionScaleUp = scaleUp;
            putDefaultNextAppTransitionCoordinates(startX, startY, TRANSIT_NONE, TRANSIT_NONE, srcThumb);
            postAnimationCallback();
            this.mNextAppTransitionCallback = startedCallback;
            return;
        }
        postAnimationCallback();
    }

    void overridePendingAppTransitionAspectScaledThumb(Bitmap srcThumb, int startX, int startY, int targetWidth, int targetHeight, IRemoteCallback startedCallback, boolean scaleUp) {
        if (isTransitionSet()) {
            int i;
            clear();
            if (scaleUp) {
                i = NEXT_TRANSIT_TYPE_THUMBNAIL_ASPECT_SCALE_UP;
            } else {
                i = TRANSIT_ACTIVITY_OPEN;
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
                i = NEXT_TRANSIT_TYPE_THUMBNAIL_ASPECT_SCALE_UP;
            } else {
                i = TRANSIT_ACTIVITY_OPEN;
            }
            this.mNextAppTransitionType = i;
            this.mNextAppTransitionScaleUp = scaleUp;
            if (specs != null) {
                for (int i2 = TRANSIT_NONE; i2 < specs.length; i2 += THUMBNAIL_TRANSITION_EXIT_SCALE_UP) {
                    AppTransitionAnimationSpec spec = specs[i2];
                    if (spec != null) {
                        this.mNextAppTransitionAnimationsSpecs.put(spec.taskId, spec);
                        if (i2 == 0) {
                            Rect rect = spec.rect;
                            putDefaultNextAppTransitionCoordinates(rect.left, rect.top, rect.width(), rect.height(), spec.bitmap);
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
                i = NEXT_TRANSIT_TYPE_THUMBNAIL_ASPECT_SCALE_UP;
            } else {
                i = TRANSIT_ACTIVITY_OPEN;
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
            this.mNextAppTransitionType = TRANSIT_ACTIVITY_CLOSE;
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
            this.mDefaultExecutor.execute(new AnonymousClass4(future));
        }
    }

    public String toString() {
        return "mNextAppTransition=" + appTransitionToString(this.mNextAppTransition);
    }

    public static String appTransitionToString(int transition) {
        switch (transition) {
            case TRANSIT_UNSET /*-1*/:
                return "TRANSIT_UNSET";
            case TRANSIT_NONE /*0*/:
                return "TRANSIT_NONE";
            case TRANSIT_ACTIVITY_OPEN /*6*/:
                return "TRANSIT_ACTIVITY_OPEN";
            case TRANSIT_ACTIVITY_CLOSE /*7*/:
                return "TRANSIT_ACTIVITY_CLOSE";
            case TRANSIT_TASK_OPEN /*8*/:
                return "TRANSIT_TASK_OPEN";
            case TRANSIT_TASK_CLOSE /*9*/:
                return "TRANSIT_TASK_CLOSE";
            case TRANSIT_TASK_TO_FRONT /*10*/:
                return "TRANSIT_TASK_TO_FRONT";
            case TRANSIT_TASK_TO_BACK /*11*/:
                return "TRANSIT_TASK_TO_BACK";
            case TRANSIT_WALLPAPER_CLOSE /*12*/:
                return "TRANSIT_WALLPAPER_CLOSE";
            case TRANSIT_WALLPAPER_OPEN /*13*/:
                return "TRANSIT_WALLPAPER_OPEN";
            case TRANSIT_WALLPAPER_INTRA_OPEN /*14*/:
                return "TRANSIT_WALLPAPER_INTRA_OPEN";
            case TRANSIT_WALLPAPER_INTRA_CLOSE /*15*/:
                return "TRANSIT_WALLPAPER_INTRA_CLOSE";
            case TRANSIT_TASK_OPEN_BEHIND /*16*/:
                return "TRANSIT_TASK_OPEN_BEHIND";
            case TRANSIT_ACTIVITY_RELAUNCH /*18*/:
                return "TRANSIT_ACTIVITY_RELAUNCH";
            case TRANSIT_DOCK_TASK_FROM_RECENTS /*19*/:
                return "TRANSIT_DOCK_TASK_FROM_RECENTS";
            default:
                return "<UNKNOWN>";
        }
    }

    private String appStateToString() {
        switch (this.mAppTransitionState) {
            case TRANSIT_NONE /*0*/:
                return "APP_STATE_IDLE";
            case THUMBNAIL_TRANSITION_EXIT_SCALE_UP /*1*/:
                return "APP_STATE_READY";
            case THUMBNAIL_TRANSITION_ENTER_SCALE_DOWN /*2*/:
                return "APP_STATE_RUNNING";
            case THUMBNAIL_TRANSITION_EXIT_SCALE_DOWN /*3*/:
                return "APP_STATE_TIMEOUT";
            default:
                return "unknown state=" + this.mAppTransitionState;
        }
    }

    private String transitTypeToString() {
        switch (this.mNextAppTransitionType) {
            case TRANSIT_NONE /*0*/:
                return "NEXT_TRANSIT_TYPE_NONE";
            case THUMBNAIL_TRANSITION_EXIT_SCALE_UP /*1*/:
                return "NEXT_TRANSIT_TYPE_CUSTOM";
            case THUMBNAIL_TRANSITION_ENTER_SCALE_DOWN /*2*/:
                return "NEXT_TRANSIT_TYPE_SCALE_UP";
            case THUMBNAIL_TRANSITION_EXIT_SCALE_DOWN /*3*/:
                return "NEXT_TRANSIT_TYPE_THUMBNAIL_SCALE_UP";
            case NEXT_TRANSIT_TYPE_THUMBNAIL_SCALE_DOWN /*4*/:
                return "NEXT_TRANSIT_TYPE_THUMBNAIL_SCALE_DOWN";
            case NEXT_TRANSIT_TYPE_THUMBNAIL_ASPECT_SCALE_UP /*5*/:
                return "NEXT_TRANSIT_TYPE_THUMBNAIL_ASPECT_SCALE_UP";
            case TRANSIT_ACTIVITY_OPEN /*6*/:
                return "NEXT_TRANSIT_TYPE_THUMBNAIL_ASPECT_SCALE_DOWN";
            case TRANSIT_ACTIVITY_CLOSE /*7*/:
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
            case THUMBNAIL_TRANSITION_EXIT_SCALE_UP /*1*/:
                pw.print(prefix);
                pw.print("mNextAppTransitionPackage=");
                pw.println(this.mNextAppTransitionPackage);
                pw.print(prefix);
                pw.print("mNextAppTransitionEnter=0x");
                pw.print(Integer.toHexString(this.mNextAppTransitionEnter));
                pw.print(" mNextAppTransitionExit=0x");
                pw.println(Integer.toHexString(this.mNextAppTransitionExit));
                break;
            case THUMBNAIL_TRANSITION_ENTER_SCALE_DOWN /*2*/:
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
            case THUMBNAIL_TRANSITION_EXIT_SCALE_DOWN /*3*/:
            case NEXT_TRANSIT_TYPE_THUMBNAIL_SCALE_DOWN /*4*/:
            case NEXT_TRANSIT_TYPE_THUMBNAIL_ASPECT_SCALE_UP /*5*/:
            case TRANSIT_ACTIVITY_OPEN /*6*/:
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
            case TRANSIT_ACTIVITY_CLOSE /*7*/:
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
    }

    public void setCurrentUser(int newUserId) {
        this.mCurrentUserId = newUserId;
    }

    boolean prepareAppTransitionLocked(int transit, boolean alwaysKeepCurrent) {
        if (!isTransitionSet() || this.mNextAppTransition == 0) {
            setAppTransition(transit);
        } else if (!alwaysKeepCurrent) {
            if (transit == TRANSIT_TASK_OPEN && isTransitionEqual(TRANSIT_TASK_CLOSE)) {
                setAppTransition(transit);
            } else if (transit == TRANSIT_ACTIVITY_OPEN && isTransitionEqual(TRANSIT_ACTIVITY_CLOSE)) {
                setAppTransition(transit);
            }
        }
        boolean prepared = prepare();
        if (isTransitionSet()) {
            this.mService.mH.removeMessages(TRANSIT_WALLPAPER_OPEN);
            this.mService.mH.sendEmptyMessageDelayed(TRANSIT_WALLPAPER_OPEN, APP_TRANSITION_TIMEOUT_MS);
        }
        return prepared;
    }

    private boolean isTvUiMode(int uiMode) {
        return (uiMode & NEXT_TRANSIT_TYPE_THUMBNAIL_SCALE_DOWN) > 0;
    }
}
