package android.app;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.app.Activity.TranslucentConversionListener;
import android.app.SharedElementCallback.OnSharedElementsReadyListener;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.media.MediaFile;
import android.net.wifi.ScanResult.InformationElement;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.ResultReceiver;
import android.rms.HwSysResource;
import android.speech.tts.Voice;
import android.transition.Transition;
import android.transition.Transition.TransitionListenerAdapter;
import android.transition.TransitionManager;
import android.util.Slog;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver.OnPreDrawListener;
import java.util.ArrayList;

class ExitTransitionCoordinator extends ActivityTransitionCoordinator {
    private static final long MAX_WAIT_MS = 1000;
    private static final String TAG = "ExitTransitionCoordinator";
    private Activity mActivity;
    private ObjectAnimator mBackgroundAnimator;
    private boolean mExitNotified;
    private Bundle mExitSharedElementBundle;
    private Handler mHandler;
    private boolean mIsBackgroundReady;
    private boolean mIsCanceled;
    private boolean mIsExitStarted;
    private boolean mIsHidden;
    private Bundle mSharedElementBundle;
    private boolean mSharedElementNotified;
    private boolean mSharedElementsHidden;

    /* renamed from: android.app.ExitTransitionCoordinator.10 */
    class AnonymousClass10 extends ContinueTransitionListener {
        AnonymousClass10(ActivityTransitionCoordinator this$0_1) {
            super();
        }

        public void onTransitionEnd(Transition transition) {
            transition.removeListener(this);
            ExitTransitionCoordinator.this.sharedElementTransitionComplete();
            if (ExitTransitionCoordinator.this.mIsHidden) {
                ExitTransitionCoordinator.this.showViews(ExitTransitionCoordinator.this.mSharedElements, true);
            }
        }
    }

    /* renamed from: android.app.ExitTransitionCoordinator.11 */
    class AnonymousClass11 implements OnSharedElementsReadyListener {
        final /* synthetic */ ResultReceiver val$resultReceiver;
        final /* synthetic */ Bundle val$sharedElementBundle;

        AnonymousClass11(ResultReceiver val$resultReceiver, Bundle val$sharedElementBundle) {
            this.val$resultReceiver = val$resultReceiver;
            this.val$sharedElementBundle = val$sharedElementBundle;
        }

        public void onSharedElementsReady() {
            this.val$resultReceiver.send(MediaFile.FILE_TYPE_XML, this.val$sharedElementBundle);
            ExitTransitionCoordinator.this.notifyExitComplete();
        }
    }

    /* renamed from: android.app.ExitTransitionCoordinator.1 */
    class AnonymousClass1 implements Runnable {
        final /* synthetic */ ViewGroup val$decorView;

        AnonymousClass1(ViewGroup val$decorView) {
            this.val$decorView = val$decorView;
        }

        public void run() {
            ExitTransitionCoordinator.this.startSharedElementExit(this.val$decorView);
        }
    }

    /* renamed from: android.app.ExitTransitionCoordinator.3 */
    class AnonymousClass3 implements OnPreDrawListener {
        final /* synthetic */ ViewGroup val$decorView;
        final /* synthetic */ ArrayList val$sharedElementSnapshots;

        AnonymousClass3(ViewGroup val$decorView, ArrayList val$sharedElementSnapshots) {
            this.val$decorView = val$decorView;
            this.val$sharedElementSnapshots = val$sharedElementSnapshots;
        }

        public boolean onPreDraw() {
            this.val$decorView.getViewTreeObserver().removeOnPreDrawListener(this);
            ExitTransitionCoordinator.this.setSharedElementState(ExitTransitionCoordinator.this.mExitSharedElementBundle, this.val$sharedElementSnapshots);
            return true;
        }
    }

    /* renamed from: android.app.ExitTransitionCoordinator.9 */
    class AnonymousClass9 extends ContinueTransitionListener {
        final /* synthetic */ ArrayList val$transitioningViews;

        AnonymousClass9(ActivityTransitionCoordinator this$0_1, ArrayList val$transitioningViews) {
            this.val$transitioningViews = val$transitioningViews;
            super();
        }

        public void onTransitionEnd(Transition transition) {
            transition.removeListener(this);
            ExitTransitionCoordinator.this.viewsTransitionComplete();
            if (ExitTransitionCoordinator.this.mIsHidden && this.val$transitioningViews != null) {
                ExitTransitionCoordinator.this.showViews(this.val$transitioningViews, true);
                ExitTransitionCoordinator.this.setTransitioningViewsVisiblity(0, true);
            }
            if (ExitTransitionCoordinator.this.mSharedElementBundle != null) {
                ExitTransitionCoordinator.this.delayCancel();
            }
            super.onTransitionEnd(transition);
        }
    }

    public ExitTransitionCoordinator(Activity activity, ArrayList<String> names, ArrayList<String> accepted, ArrayList<View> mapped, boolean isReturning) {
        super(activity.getWindow(), names, getListener(activity, isReturning), isReturning);
        viewsReady(mapSharedElements(accepted, mapped));
        stripOffscreenViews();
        this.mIsBackgroundReady = !isReturning;
        this.mActivity = activity;
    }

    private static SharedElementCallback getListener(Activity activity, boolean isReturning) {
        return isReturning ? activity.mEnterTransitionListener : activity.mExitTransitionListener;
    }

    protected void onReceiveResult(int resultCode, Bundle resultData) {
        switch (resultCode) {
            case Voice.QUALITY_VERY_LOW /*100*/:
                stopCancel();
                this.mResultReceiver = (ResultReceiver) resultData.getParcelable("android:remoteReceiver");
                if (this.mIsCanceled) {
                    this.mResultReceiver.send(MediaFile.FILE_TYPE_MS_POWERPOINT, null);
                    this.mResultReceiver = null;
                    return;
                }
                notifyComplete();
            case HwSysResource.MAINSERVICES /*101*/:
                stopCancel();
                if (!this.mIsCanceled) {
                    hideSharedElements();
                }
            case MediaFile.FILE_TYPE_MS_EXCEL /*105*/:
                this.mHandler.removeMessages(MediaFile.FILE_TYPE_MS_POWERPOINT);
                startExit();
            case MediaFile.FILE_TYPE_MS_POWERPOINT /*106*/:
                this.mIsCanceled = true;
                finish();
            case InformationElement.EID_INTERWORKING /*107*/:
                this.mExitSharedElementBundle = resultData;
                sharedElementExitBack();
            default:
        }
    }

    private void stopCancel() {
        if (this.mHandler != null) {
            this.mHandler.removeMessages(MediaFile.FILE_TYPE_MS_POWERPOINT);
        }
    }

    private void delayCancel() {
        if (this.mHandler != null) {
            this.mHandler.sendEmptyMessageDelayed(MediaFile.FILE_TYPE_MS_POWERPOINT, MAX_WAIT_MS);
        }
    }

    public void resetViews() {
        if (this.mTransitioningViews != null) {
            showViews(this.mTransitioningViews, true);
            setTransitioningViewsVisiblity(0, true);
        }
        showViews(this.mSharedElements, true);
        this.mIsHidden = true;
        ViewGroup decorView = getDecor();
        if (!(this.mIsReturning || decorView == null)) {
            decorView.suppressLayout(false);
        }
        moveSharedElementsFromOverlay();
        clearState();
    }

    private void sharedElementExitBack() {
        ViewGroup decorView = getDecor();
        if (decorView != null) {
            decorView.suppressLayout(true);
        }
        if (decorView == null || this.mExitSharedElementBundle == null || this.mExitSharedElementBundle.isEmpty() || this.mSharedElements.isEmpty() || getSharedElementTransition() == null) {
            sharedElementTransitionComplete();
        } else {
            startTransition(new AnonymousClass1(decorView));
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
        decorView.getViewTreeObserver().addOnPreDrawListener(new AnonymousClass3(decorView, sharedElementSnapshots));
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
        if (!this.mIsHidden) {
            hideViews(this.mSharedElements);
        }
        this.mSharedElementsHidden = true;
        finishIfNecessary();
    }

    public void startExit() {
        if (!this.mIsExitStarted) {
            this.mIsExitStarted = true;
            pauseInput();
            ViewGroup decorView = getDecor();
            if (decorView != null) {
                decorView.suppressLayout(true);
            }
            moveSharedElementsToOverlay();
            startTransition(new Runnable() {
                public void run() {
                    ExitTransitionCoordinator.this.beginTransitions();
                }
            });
        }
    }

    public void startExit(int resultCode, Intent data) {
        boolean targetsM = true;
        if (!this.mIsExitStarted) {
            ArrayList<String> sharedElementNames;
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
                getWindow().setBackgroundDrawable(new ColorDrawable(Color.BLACK));
            }
            if (decorView != null && decorView.getContext().getApplicationInfo().targetSdkVersion < 23) {
                targetsM = false;
            }
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
                        }
                    });
                    this.mBackgroundAnimator.setDuration(getFadeDuration());
                    this.mBackgroundAnimator.start();
                    return;
                }
            }
            this.mIsBackgroundReady = true;
        }
    }

    private Transition getExitTransition() {
        Transition viewsTransition = null;
        if (!(this.mTransitioningViews == null || this.mTransitioningViews.isEmpty())) {
            viewsTransition = configureTransition(getViewsTransition(), true);
        }
        if (viewsTransition == null) {
            viewsTransitionComplete();
        } else {
            viewsTransition.addListener(new AnonymousClass9(this, this.mTransitioningViews));
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
            sharedElementTransition.addListener(new AnonymousClass10(this));
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
            this.mResultReceiver.send(MediaFile.FILE_TYPE_XML, this.mSharedElementBundle);
            notifyExitComplete();
            return;
        }
        this.mListener.onSharedElementsArrived(this.mSharedElementNames, this.mSharedElements, new AnonymousClass11(this.mResultReceiver, this.mSharedElementBundle));
    }

    private void notifyExitComplete() {
        if (!this.mExitNotified && isViewsTransitionComplete()) {
            this.mExitNotified = true;
            this.mResultReceiver.send(MediaFile.FILE_TYPE_MS_WORD, null);
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
        return !this.mIsReturning;
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
