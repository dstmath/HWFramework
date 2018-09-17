package android.app;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.app.Activity.TranslucentConversionListener;
import android.app.SharedElementCallback.OnSharedElementsReadyListener;
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
    private Activity mActivity;
    private ObjectAnimator mBackgroundAnimator;
    private boolean mExitNotified;
    private Bundle mExitSharedElementBundle;
    private Handler mHandler;
    private HideSharedElementsCallback mHideSharedElementsCallback;
    private boolean mIsBackgroundReady;
    private boolean mIsCanceled;
    private boolean mIsExitStarted;
    private boolean mIsHidden;
    private Bundle mSharedElementBundle;
    private boolean mSharedElementNotified;
    private boolean mSharedElementsHidden;

    interface HideSharedElementsCallback {
        void hideSharedElements();
    }

    public ExitTransitionCoordinator(Activity activity, Window window, SharedElementCallback listener, ArrayList<String> names, ArrayList<String> accepted, ArrayList<View> mapped, boolean isReturning) {
        super(window, names, listener, isReturning);
        viewsReady(mapSharedElements(accepted, mapped));
        stripOffscreenViews();
        this.mIsBackgroundReady = isReturning ^ 1;
        this.mActivity = activity;
    }

    void setHideSharedElementsCallback(HideSharedElementsCallback callback) {
        this.mHideSharedElementsCallback = callback;
    }

    protected void onReceiveResult(int resultCode, Bundle resultData) {
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

    private void delayCancel() {
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
        if (!(this.mIsReturning || decorView == null)) {
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
        if (decorView == null || this.mExitSharedElementBundle == null || (this.mExitSharedElementBundle.isEmpty() ^ 1) == 0 || (this.mSharedElements.isEmpty() ^ 1) == 0 || getSharedElementTransition() == null) {
            sharedElementTransitionComplete();
        } else {
            startTransition(new Runnable() {
                public void run() {
                    ExitTransitionCoordinator.this.startSharedElementExit(decorView);
                }
            });
        }
    }

    private void startSharedElementExit(ViewGroup decorView) {
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
        OneShotPreDrawListener.add(decorView, new -$Lambda$C7h8n9V3Buzw6AAhwTUy-fEyco0(this, sharedElementSnapshots));
        lambda$-android_app_ActivityTransitionCoordinator_39166(4);
        scheduleGhostVisibilityChange(4);
        if (this.mListener != null) {
            this.mListener.onSharedElementEnd(this.mSharedElementNames, this.mSharedElements, sharedElementSnapshots);
        }
        TransitionManager.beginDelayedTransition(decorView, transition);
        scheduleGhostVisibilityChange(0);
        lambda$-android_app_ActivityTransitionCoordinator_39166(0);
        decorView.invalidate();
    }

    /* synthetic */ void lambda$-android_app_ExitTransitionCoordinator_6416(ArrayList sharedElementSnapshots) {
        setSharedElementState(this.mExitSharedElementBundle, sharedElementSnapshots);
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
        if (!this.mIsExitStarted) {
            ArrayList sharedElementNames;
            this.mIsExitStarted = true;
            pauseInput();
            ViewGroup decorView = getDecor();
            if (decorView != null) {
                decorView.suppressLayout(true);
            }
            this.mHandler = new Handler() {
                public void handleMessage(Message msg) {
                    ExitTransitionCoordinator.this.mIsCanceled = true;
                    ExitTransitionCoordinator.this.finish();
                }
            };
            delayCancel();
            moveSharedElementsToOverlay();
            if (decorView != null && decorView.getBackground() == null) {
                getWindow().setBackgroundDrawable(new ColorDrawable(0));
            }
            boolean targetsM = decorView == null || decorView.getContext().getApplicationInfo().targetSdkVersion >= 23;
            if (targetsM) {
                sharedElementNames = this.mSharedElementNames;
            } else {
                sharedElementNames = this.mAllSharedElementNames;
            }
            this.mActivity.convertToTranslucent(new TranslucentConversionListener() {
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

    private void startExitTransition() {
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

    private void fadeOutBackground() {
        if (this.mBackgroundAnimator == null) {
            ViewGroup decor = getDecor();
            if (decor != null) {
                Drawable background = decor.getBackground();
                if (background != null) {
                    Object background2 = background.mutate();
                    getWindow().setBackgroundDrawable(background2);
                    this.mBackgroundAnimator = ObjectAnimator.ofInt(background2, "alpha", 0);
                    this.mBackgroundAnimator.addListener(new AnimatorListenerAdapter() {
                        public void onAnimationEnd(Animator animation) {
                            ExitTransitionCoordinator.this.mBackgroundAnimator = null;
                            if (!ExitTransitionCoordinator.this.mIsCanceled) {
                                ExitTransitionCoordinator.this.mIsBackgroundReady = true;
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
        if (!(this.mTransitioningViews == null || (this.mTransitioningViews.isEmpty() ^ 1) == 0)) {
            viewsTransition = configureTransition(getViewsTransition(), true);
            ActivityTransitionCoordinator.removeExcludedViews(viewsTransition, this.mTransitioningViews);
            if (this.mTransitioningViews.isEmpty()) {
                viewsTransition = null;
            }
        }
        if (viewsTransition == null) {
            viewsTransitionComplete();
        } else {
            final ArrayList<View> transitioningViews = this.mTransitioningViews;
            viewsTransition.addListener(new ContinueTransitionListener(this) {
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
            sharedElementTransition.addListener(new ContinueTransitionListener(this) {
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

    private void beginTransitions() {
        Transition sharedElementTransition = getSharedElementExitTransition();
        Transition viewsTransition = getExitTransition();
        Transition transition = ActivityTransitionCoordinator.mergeTransitions(sharedElementTransition, viewsTransition);
        ViewGroup decorView = getDecor();
        if (transition == null || decorView == null) {
            transitionStarted();
            return;
        }
        lambda$-android_app_ActivityTransitionCoordinator_39166(4);
        scheduleGhostVisibilityChange(4);
        if (viewsTransition != null) {
            setTransitioningViewsVisiblity(0, false);
        }
        TransitionManager.beginDelayedTransition(decorView, transition);
        scheduleGhostVisibilityChange(0);
        lambda$-android_app_ActivityTransitionCoordinator_39166(0);
        if (viewsTransition != null) {
            setTransitioningViewsVisiblity(4, false);
        }
        decorView.invalidate();
    }

    protected boolean isReadyToNotify() {
        return (this.mSharedElementBundle == null || this.mResultReceiver == null) ? false : this.mIsBackgroundReady;
    }

    protected void sharedElementTransitionComplete() {
        this.mSharedElementBundle = this.mExitSharedElementBundle == null ? captureSharedElementState() : captureExitSharedElementsState();
        super.sharedElementTransitionComplete();
    }

    private Bundle captureExitSharedElementsState() {
        Bundle bundle = new Bundle();
        RectF bounds = new RectF();
        Matrix matrix = new Matrix();
        for (int i = 0; i < this.mSharedElements.size(); i++) {
            String name = (String) this.mSharedElementNames.get(i);
            Bundle sharedElementState = this.mExitSharedElementBundle.getBundle(name);
            if (sharedElementState != null) {
                bundle.putBundle(name, sharedElementState);
            } else {
                captureSharedElementState((View) this.mSharedElements.get(i), name, bundle, matrix, bounds);
            }
        }
        return bundle;
    }

    protected void onTransitionsComplete() {
        notifyComplete();
    }

    protected void notifyComplete() {
        Slog.d(TAG, "notifyComplete isReadyToNotify= " + isReadyToNotify() + " mSharedElementNotified= " + this.mSharedElementNotified + " mListener= " + this.mListener);
        if (!isReadyToNotify()) {
            return;
        }
        if (this.mSharedElementNotified) {
            notifyExitComplete();
            return;
        }
        this.mSharedElementNotified = true;
        delayCancel();
        if (this.mListener == null) {
            this.mResultReceiver.send(103, this.mSharedElementBundle);
            notifyExitComplete();
            return;
        }
        final ResultReceiver resultReceiver = this.mResultReceiver;
        final Bundle sharedElementBundle = this.mSharedElementBundle;
        this.mListener.onSharedElementsArrived(this.mSharedElementNames, this.mSharedElements, new OnSharedElementsReadyListener() {
            public void onSharedElementsReady() {
                resultReceiver.send(103, sharedElementBundle);
                ExitTransitionCoordinator.this.notifyExitComplete();
            }
        });
    }

    private void notifyExitComplete() {
        if (!this.mExitNotified && isViewsTransitionComplete()) {
            this.mExitNotified = true;
            this.mResultReceiver.send(104, null);
            this.mResultReceiver = null;
            ViewGroup decorView = getDecor();
            if (!(this.mIsReturning || decorView == null)) {
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

    private void finish() {
        stopCancel();
        if (this.mActivity != null) {
            this.mActivity.mActivityTransitionState.clear();
            this.mActivity.finish();
            this.mActivity.overridePendingTransition(0, 0);
            this.mActivity = null;
        }
        clearState();
    }

    protected void clearState() {
        this.mHandler = null;
        this.mSharedElementBundle = null;
        if (this.mBackgroundAnimator != null) {
            this.mBackgroundAnimator.cancel();
            this.mBackgroundAnimator = null;
        }
        this.mExitSharedElementBundle = null;
        super.clearState();
    }

    protected boolean moveSharedElementWithParent() {
        return this.mIsReturning ^ 1;
    }

    protected Transition getViewsTransition() {
        if (this.mIsReturning) {
            return getWindow().getReturnTransition();
        }
        return getWindow().getExitTransition();
    }

    protected Transition getSharedElementTransition() {
        if (this.mIsReturning) {
            return getWindow().getSharedElementReturnTransition();
        }
        return getWindow().getSharedElementExitTransition();
    }
}
