package android.app;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.app.ActivityTransitionCoordinator;
import android.app.SharedElementCallback;
import android.content.Intent;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.ResultReceiver;
import android.transition.Transition;
import android.transition.TransitionListenerAdapter;
import android.transition.TransitionManager;
import android.util.Slog;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import com.android.internal.view.OneShotPreDrawListener;
import java.util.ArrayList;

class ExitTransitionCoordinator extends ActivityTransitionCoordinator {
    private static final long MAX_WAIT_MS = 1000;
    private static final String TAG = "ExitTransitionCoordinator";
    /* access modifiers changed from: private */
    public Activity mActivity;
    /* access modifiers changed from: private */
    public ObjectAnimator mBackgroundAnimator;
    private boolean mExitNotified;
    private Bundle mExitSharedElementBundle;
    private Handler mHandler;
    private HideSharedElementsCallback mHideSharedElementsCallback;
    /* access modifiers changed from: private */
    public boolean mIsBackgroundReady;
    /* access modifiers changed from: private */
    public boolean mIsCanceled;
    private boolean mIsExitStarted;
    /* access modifiers changed from: private */
    public boolean mIsHidden;
    /* access modifiers changed from: private */
    public Bundle mSharedElementBundle;
    private boolean mSharedElementNotified;
    private boolean mSharedElementsHidden;

    interface HideSharedElementsCallback {
        void hideSharedElements();
    }

    public ExitTransitionCoordinator(Activity activity, Window window, SharedElementCallback listener, ArrayList<String> names, ArrayList<String> accepted, ArrayList<View> mapped, boolean isReturning) {
        super(window, names, listener, isReturning);
        viewsReady(mapSharedElements(accepted, mapped));
        stripOffscreenViews();
        this.mIsBackgroundReady = !isReturning;
        this.mActivity = activity;
    }

    /* access modifiers changed from: package-private */
    public void setHideSharedElementsCallback(HideSharedElementsCallback callback) {
        this.mHideSharedElementsCallback = callback;
    }

    /* access modifiers changed from: protected */
    public void onReceiveResult(int resultCode, Bundle resultData) {
        switch (resultCode) {
            case 100:
                stopCancel();
                this.mResultReceiver = (ResultReceiver) resultData.getParcelable("android:remoteReceiver");
                if (this.mIsCanceled) {
                    this.mResultReceiver.send(106, null);
                    this.mResultReceiver = null;
                    return;
                }
                notifyComplete();
                return;
            case 101:
                stopCancel();
                if (!this.mIsCanceled) {
                    hideSharedElements();
                    return;
                }
                return;
            case 105:
                this.mHandler.removeMessages(106);
                startExit();
                return;
            case 106:
                this.mIsCanceled = true;
                finish();
                return;
            case 107:
                this.mExitSharedElementBundle = resultData;
                sharedElementExitBack();
                return;
            default:
                return;
        }
    }

    private void stopCancel() {
        if (this.mHandler != null) {
            this.mHandler.removeMessages(106);
        }
    }

    /* access modifiers changed from: private */
    public void delayCancel() {
        if (this.mHandler != null) {
            this.mHandler.sendEmptyMessageDelayed(106, MAX_WAIT_MS);
        }
    }

    public void resetViews() {
        ViewGroup decorView = getDecor();
        if (decorView != null) {
            TransitionManager.endTransitions(decorView);
        }
        if (this.mTransitioningViews != null) {
            showViews(this.mTransitioningViews, true);
            setTransitioningViewsVisiblity(0, true);
        }
        showViews(this.mSharedElements, true);
        this.mIsHidden = true;
        if (!this.mIsReturning && decorView != null) {
            decorView.suppressLayout(false);
        }
        moveSharedElementsFromOverlay();
        clearState();
    }

    private void sharedElementExitBack() {
        final ViewGroup decorView = getDecor();
        if (decorView != null) {
            decorView.suppressLayout(true);
        }
        if (decorView == null || this.mExitSharedElementBundle == null || this.mExitSharedElementBundle.isEmpty() || this.mSharedElements.isEmpty() || getSharedElementTransition() == null) {
            sharedElementTransitionComplete();
        } else {
            startTransition(new Runnable() {
                public void run() {
                    ExitTransitionCoordinator.this.startSharedElementExit(decorView);
                }
            });
        }
    }

    /* access modifiers changed from: private */
    public void startSharedElementExit(ViewGroup decorView) {
        Transition transition = getSharedElementExitTransition();
        transition.addListener(new TransitionListenerAdapter() {
            public void onTransitionEnd(Transition transition) {
                transition.removeListener(this);
                if (ExitTransitionCoordinator.this.isViewsTransitionComplete()) {
                    ExitTransitionCoordinator.this.delayCancel();
                }
            }
        });
        ArrayList<View> sharedElementSnapshots = createSnapshots(this.mExitSharedElementBundle, this.mSharedElementNames);
        OneShotPreDrawListener.add(decorView, new Runnable(sharedElementSnapshots) {
            private final /* synthetic */ ArrayList f$1;

            {
                this.f$1 = r2;
            }

            public final void run() {
                ExitTransitionCoordinator.this.setSharedElementState(ExitTransitionCoordinator.this.mExitSharedElementBundle, this.f$1);
            }
        });
        setGhostVisibility(4);
        scheduleGhostVisibilityChange(4);
        if (this.mListener != null) {
            this.mListener.onSharedElementEnd(this.mSharedElementNames, this.mSharedElements, sharedElementSnapshots);
        }
        TransitionManager.beginDelayedTransition(decorView, transition);
        scheduleGhostVisibilityChange(0);
        setGhostVisibility(0);
        decorView.invalidate();
    }

    private void hideSharedElements() {
        moveSharedElementsFromOverlay();
        if (this.mHideSharedElementsCallback != null) {
            this.mHideSharedElementsCallback.hideSharedElements();
        }
        if (!this.mIsHidden) {
            hideViews(this.mSharedElements);
        }
        this.mSharedElementsHidden = true;
        finishIfNecessary();
    }

    public void startExit() {
        if (!this.mIsExitStarted) {
            backgroundAnimatorComplete();
            this.mIsExitStarted = true;
            pauseInput();
            ViewGroup decorView = getDecor();
            if (decorView != null) {
                decorView.suppressLayout(true);
            }
            moveSharedElementsToOverlay();
            startTransition(new Runnable() {
                public void run() {
                    if (ExitTransitionCoordinator.this.mActivity != null) {
                        ExitTransitionCoordinator.this.beginTransitions();
                    } else {
                        ExitTransitionCoordinator.this.startExitTransition();
                    }
                }
            });
        }
    }

    public void startExit(int resultCode, Intent data) {
        ArrayList<String> sharedElementNames;
        if (!this.mIsExitStarted) {
            boolean targetsM = true;
            this.mIsExitStarted = true;
            pauseInput();
            ViewGroup decorView = getDecor();
            if (decorView != null) {
                decorView.suppressLayout(true);
            }
            this.mHandler = new Handler() {
                public void handleMessage(Message msg) {
                    boolean unused = ExitTransitionCoordinator.this.mIsCanceled = true;
                    ExitTransitionCoordinator.this.finish();
                }
            };
            delayCancel();
            moveSharedElementsToOverlay();
            if (decorView != null && decorView.getBackground() == null) {
                getWindow().setBackgroundDrawable(new ColorDrawable(0));
            }
            if (decorView != null && decorView.getContext().getApplicationInfo().targetSdkVersion < 23) {
                targetsM = false;
            }
            if (targetsM) {
                sharedElementNames = this.mSharedElementNames;
            } else {
                sharedElementNames = this.mAllSharedElementNames;
            }
            this.mActivity.convertToTranslucent(new Activity.TranslucentConversionListener() {
                public void onTranslucentConversionComplete(boolean drawComplete) {
                    if (!ExitTransitionCoordinator.this.mIsCanceled) {
                        ExitTransitionCoordinator.this.fadeOutBackground();
                    }
                }
            }, ActivityOptions.makeSceneTransitionAnimation(this.mActivity, this, sharedElementNames, resultCode, data));
            startTransition(new Runnable() {
                public void run() {
                    ExitTransitionCoordinator.this.startExitTransition();
                }
            });
        }
    }

    public void stop() {
        if (this.mIsReturning && this.mActivity != null) {
            this.mActivity.convertToTranslucent(null, null);
            finish();
        }
    }

    /* access modifiers changed from: private */
    public void startExitTransition() {
        Transition transition = getExitTransition();
        ViewGroup decorView = getDecor();
        if (transition == null || decorView == null || this.mTransitioningViews == null) {
            transitionStarted();
            return;
        }
        setTransitioningViewsVisiblity(0, false);
        TransitionManager.beginDelayedTransition(decorView, transition);
        setTransitioningViewsVisiblity(4, false);
        decorView.invalidate();
    }

    /* access modifiers changed from: private */
    public void fadeOutBackground() {
        if (this.mBackgroundAnimator == null) {
            ViewGroup decor = getDecor();
            if (decor != null) {
                Drawable background = decor.getBackground();
                Drawable background2 = background;
                if (background != null) {
                    Drawable background3 = background2.mutate();
                    getWindow().setBackgroundDrawable(background3);
                    this.mBackgroundAnimator = ObjectAnimator.ofInt((Object) background3, "alpha", 0);
                    this.mBackgroundAnimator.addListener(new AnimatorListenerAdapter() {
                        public void onAnimationEnd(Animator animation) {
                            ObjectAnimator unused = ExitTransitionCoordinator.this.mBackgroundAnimator = null;
                            if (!ExitTransitionCoordinator.this.mIsCanceled) {
                                boolean unused2 = ExitTransitionCoordinator.this.mIsBackgroundReady = true;
                                ExitTransitionCoordinator.this.notifyComplete();
                            }
                            ExitTransitionCoordinator.this.backgroundAnimatorComplete();
                        }
                    });
                    this.mBackgroundAnimator.setDuration(getFadeDuration());
                    this.mBackgroundAnimator.start();
                    return;
                }
            }
            backgroundAnimatorComplete();
            this.mIsBackgroundReady = true;
        }
    }

    private Transition getExitTransition() {
        Transition viewsTransition = null;
        if (this.mTransitioningViews != null && !this.mTransitioningViews.isEmpty()) {
            viewsTransition = configureTransition(getViewsTransition(), true);
            removeExcludedViews(viewsTransition, this.mTransitioningViews);
            if (this.mTransitioningViews.isEmpty()) {
                viewsTransition = null;
            }
        }
        if (viewsTransition == null) {
            viewsTransitionComplete();
        } else {
            final ArrayList<View> transitioningViews = this.mTransitioningViews;
            viewsTransition.addListener(new ActivityTransitionCoordinator.ContinueTransitionListener() {
                public void onTransitionEnd(Transition transition) {
                    ExitTransitionCoordinator.this.viewsTransitionComplete();
                    if (ExitTransitionCoordinator.this.mIsHidden && transitioningViews != null) {
                        ExitTransitionCoordinator.this.showViews(transitioningViews, true);
                        ExitTransitionCoordinator.this.setTransitioningViewsVisiblity(0, true);
                    }
                    if (ExitTransitionCoordinator.this.mSharedElementBundle != null) {
                        ExitTransitionCoordinator.this.delayCancel();
                    }
                    super.onTransitionEnd(transition);
                }
            });
        }
        return viewsTransition;
    }

    private Transition getSharedElementExitTransition() {
        Transition sharedElementTransition = null;
        if (!this.mSharedElements.isEmpty()) {
            sharedElementTransition = configureTransition(getSharedElementTransition(), false);
        }
        if (sharedElementTransition == null) {
            sharedElementTransitionComplete();
        } else {
            sharedElementTransition.addListener(new ActivityTransitionCoordinator.ContinueTransitionListener() {
                public void onTransitionEnd(Transition transition) {
                    ExitTransitionCoordinator.this.sharedElementTransitionComplete();
                    if (ExitTransitionCoordinator.this.mIsHidden) {
                        ExitTransitionCoordinator.this.showViews(ExitTransitionCoordinator.this.mSharedElements, true);
                    }
                    super.onTransitionEnd(transition);
                }
            });
            ((View) this.mSharedElements.get(0)).invalidate();
        }
        return sharedElementTransition;
    }

    /* access modifiers changed from: private */
    public void beginTransitions() {
        Transition sharedElementTransition = getSharedElementExitTransition();
        Transition viewsTransition = getExitTransition();
        Transition transition = mergeTransitions(sharedElementTransition, viewsTransition);
        ViewGroup decorView = getDecor();
        if (transition == null || decorView == null) {
            transitionStarted();
            return;
        }
        setGhostVisibility(4);
        scheduleGhostVisibilityChange(4);
        if (viewsTransition != null) {
            setTransitioningViewsVisiblity(0, false);
        }
        TransitionManager.beginDelayedTransition(decorView, transition);
        scheduleGhostVisibilityChange(0);
        setGhostVisibility(0);
        if (viewsTransition != null) {
            setTransitioningViewsVisiblity(4, false);
        }
        decorView.invalidate();
    }

    /* access modifiers changed from: protected */
    public boolean isReadyToNotify() {
        return (this.mSharedElementBundle == null || this.mResultReceiver == null || !this.mIsBackgroundReady) ? false : true;
    }

    /* access modifiers changed from: protected */
    public void sharedElementTransitionComplete() {
        this.mSharedElementBundle = this.mExitSharedElementBundle == null ? captureSharedElementState() : captureExitSharedElementsState();
        super.sharedElementTransitionComplete();
    }

    private Bundle captureExitSharedElementsState() {
        Bundle bundle = new Bundle();
        RectF bounds = new RectF();
        Matrix matrix = new Matrix();
        int i = 0;
        while (true) {
            int i2 = i;
            if (i2 >= this.mSharedElements.size()) {
                return bundle;
            }
            String name = (String) this.mSharedElementNames.get(i2);
            Bundle sharedElementState = this.mExitSharedElementBundle.getBundle(name);
            if (sharedElementState != null) {
                bundle.putBundle(name, sharedElementState);
            } else {
                captureSharedElementState((View) this.mSharedElements.get(i2), name, bundle, matrix, bounds);
            }
            i = i2 + 1;
        }
    }

    /* access modifiers changed from: protected */
    public void onTransitionsComplete() {
        notifyComplete();
    }

    /* access modifiers changed from: protected */
    public void notifyComplete() {
        Slog.d(TAG, "notifyComplete isReadyToNotify= " + isReadyToNotify() + " mSharedElementNotified= " + this.mSharedElementNotified + " mListener= " + this.mListener);
        if (!isReadyToNotify()) {
            return;
        }
        if (!this.mSharedElementNotified) {
            this.mSharedElementNotified = true;
            delayCancel();
            if (this.mListener == null) {
                this.mResultReceiver.send(103, this.mSharedElementBundle);
                notifyExitComplete();
                return;
            }
            final ResultReceiver resultReceiver = this.mResultReceiver;
            final Bundle sharedElementBundle = this.mSharedElementBundle;
            this.mListener.onSharedElementsArrived(this.mSharedElementNames, this.mSharedElements, new SharedElementCallback.OnSharedElementsReadyListener() {
                public void onSharedElementsReady() {
                    resultReceiver.send(103, sharedElementBundle);
                    ExitTransitionCoordinator.this.notifyExitComplete();
                }
            });
            return;
        }
        notifyExitComplete();
    }

    /* access modifiers changed from: private */
    public void notifyExitComplete() {
        if (!this.mExitNotified && isViewsTransitionComplete()) {
            this.mExitNotified = true;
            this.mResultReceiver.send(104, null);
            this.mResultReceiver = null;
            ViewGroup decorView = getDecor();
            if (!this.mIsReturning && decorView != null) {
                decorView.suppressLayout(false);
            }
            finishIfNecessary();
        }
    }

    private void finishIfNecessary() {
        if (this.mIsReturning && this.mExitNotified && this.mActivity != null && (this.mSharedElements.isEmpty() || this.mSharedElementsHidden)) {
            finish();
        }
        if (!this.mIsReturning && this.mExitNotified) {
            this.mActivity = null;
        }
    }

    /* access modifiers changed from: private */
    public void finish() {
        stopCancel();
        if (this.mActivity != null) {
            this.mActivity.mActivityTransitionState.clear();
            this.mActivity.finish();
            this.mActivity.overridePendingTransition(0, 0);
            this.mActivity = null;
        }
        clearState();
    }

    /* access modifiers changed from: protected */
    public void clearState() {
        this.mHandler = null;
        this.mSharedElementBundle = null;
        if (this.mBackgroundAnimator != null) {
            this.mBackgroundAnimator.cancel();
            this.mBackgroundAnimator = null;
        }
        this.mExitSharedElementBundle = null;
        super.clearState();
    }

    /* access modifiers changed from: protected */
    public boolean moveSharedElementWithParent() {
        return !this.mIsReturning;
    }

    /* access modifiers changed from: protected */
    public Transition getViewsTransition() {
        if (this.mIsReturning) {
            return getWindow().getReturnTransition();
        }
        return getWindow().getExitTransition();
    }

    /* access modifiers changed from: protected */
    public Transition getSharedElementTransition() {
        if (this.mIsReturning) {
            return getWindow().getSharedElementReturnTransition();
        }
        return getWindow().getSharedElementExitTransition();
    }
}
