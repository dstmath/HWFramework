package android.app;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.app.SharedElementCallback.OnSharedElementsReadyListener;
import android.graphics.drawable.Drawable;
import android.media.MediaFile;
import android.net.wifi.ScanResult.InformationElement;
import android.os.Bundle;
import android.os.Process;
import android.os.ResultReceiver;
import android.rms.HwSysResource;
import android.speech.tts.TextToSpeech.Engine;
import android.text.TextUtils;
import android.transition.Transition;
import android.transition.Transition.TransitionListenerAdapter;
import android.transition.TransitionManager;
import android.util.ArrayMap;
import android.util.Property;
import android.util.Slog;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroupOverlay;
import android.view.ViewTreeObserver.OnPreDrawListener;
import android.view.Window;
import java.util.ArrayList;

class EnterTransitionCoordinator extends ActivityTransitionCoordinator {
    private static final int MIN_ANIMATION_FRAMES = 2;
    private static final String TAG = "EnterTransitionCoordinator";
    private Activity mActivity;
    private boolean mAreViewsReady;
    private ObjectAnimator mBackgroundAnimator;
    private Transition mEnterViewsTransition;
    private boolean mHasStopped;
    private boolean mIsCanceled;
    private boolean mIsExitTransitionComplete;
    private boolean mIsReadyForTransition;
    private boolean mIsViewsTransitionStarted;
    private boolean mSharedElementTransitionStarted;
    private Bundle mSharedElementsBundle;
    private OnPreDrawListener mViewsReadyListener;
    private boolean mWasOpaque;

    /* renamed from: android.app.EnterTransitionCoordinator.10 */
    class AnonymousClass10 extends AnimatorListenerAdapter {
        final /* synthetic */ ViewGroup val$decorView;
        final /* synthetic */ ArrayList val$rejectedSnapshots;

        AnonymousClass10(ViewGroup val$decorView, ArrayList val$rejectedSnapshots) {
            this.val$decorView = val$decorView;
            this.val$rejectedSnapshots = val$rejectedSnapshots;
        }

        public void onAnimationEnd(Animator animation) {
            ViewGroupOverlay overlay = this.val$decorView.getOverlay();
            int numRejected = this.val$rejectedSnapshots.size();
            for (int i = 0; i < numRejected; i++) {
                overlay.remove((View) this.val$rejectedSnapshots.get(i));
            }
        }
    }

    /* renamed from: android.app.EnterTransitionCoordinator.1 */
    class AnonymousClass1 implements OnPreDrawListener {
        final /* synthetic */ View val$decorView;

        AnonymousClass1(View val$decorView) {
            this.val$decorView = val$decorView;
        }

        public boolean onPreDraw() {
            if (EnterTransitionCoordinator.this.mIsReadyForTransition) {
                this.val$decorView.getViewTreeObserver().removeOnPreDrawListener(this);
            }
            return EnterTransitionCoordinator.this.mIsReadyForTransition;
        }
    }

    /* renamed from: android.app.EnterTransitionCoordinator.2 */
    class AnonymousClass2 implements OnPreDrawListener {
        final /* synthetic */ ViewGroup val$decor;
        final /* synthetic */ ArrayMap val$sharedElements;

        AnonymousClass2(ViewGroup val$decor, ArrayMap val$sharedElements) {
            this.val$decor = val$decor;
            this.val$sharedElements = val$sharedElements;
        }

        public boolean onPreDraw() {
            EnterTransitionCoordinator.this.mViewsReadyListener = null;
            this.val$decor.getViewTreeObserver().removeOnPreDrawListener(this);
            EnterTransitionCoordinator.this.viewsReady(this.val$sharedElements);
            return true;
        }
    }

    /* renamed from: android.app.EnterTransitionCoordinator.3 */
    class AnonymousClass3 implements OnPreDrawListener {
        final /* synthetic */ View val$decorView;

        AnonymousClass3(View val$decorView) {
            this.val$decorView = val$decorView;
        }

        public boolean onPreDraw() {
            this.val$decorView.getViewTreeObserver().removeOnPreDrawListener(this);
            if (EnterTransitionCoordinator.this.mResultReceiver != null) {
                Bundle state = EnterTransitionCoordinator.this.captureSharedElementState();
                EnterTransitionCoordinator.this.moveSharedElementsToOverlay();
                EnterTransitionCoordinator.this.mResultReceiver.send(InformationElement.EID_INTERWORKING, state);
            }
            return true;
        }
    }

    /* renamed from: android.app.EnterTransitionCoordinator.5 */
    class AnonymousClass5 implements OnSharedElementsReadyListener {
        final /* synthetic */ Bundle val$sharedElementState;

        /* renamed from: android.app.EnterTransitionCoordinator.5.1 */
        class AnonymousClass1 implements OnPreDrawListener {
            final /* synthetic */ View val$decorView;
            final /* synthetic */ Bundle val$sharedElementState;

            /* renamed from: android.app.EnterTransitionCoordinator.5.1.1 */
            class AnonymousClass1 implements Runnable {
                final /* synthetic */ Bundle val$sharedElementState;

                AnonymousClass1(Bundle val$sharedElementState) {
                    this.val$sharedElementState = val$sharedElementState;
                }

                public void run() {
                    EnterTransitionCoordinator.this.startSharedElementTransition(this.val$sharedElementState);
                }
            }

            AnonymousClass1(View val$decorView, Bundle val$sharedElementState) {
                this.val$decorView = val$decorView;
                this.val$sharedElementState = val$sharedElementState;
            }

            public boolean onPreDraw() {
                this.val$decorView.getViewTreeObserver().removeOnPreDrawListener(this);
                EnterTransitionCoordinator.this.startTransition(new AnonymousClass1(this.val$sharedElementState));
                return false;
            }
        }

        AnonymousClass5(Bundle val$sharedElementState) {
            this.val$sharedElementState = val$sharedElementState;
        }

        public void onSharedElementsReady() {
            View decorView = EnterTransitionCoordinator.this.getDecor();
            if (decorView != null) {
                decorView.getViewTreeObserver().addOnPreDrawListener(new AnonymousClass1(decorView, this.val$sharedElementState));
                decorView.invalidate();
            }
        }
    }

    /* renamed from: android.app.EnterTransitionCoordinator.7 */
    class AnonymousClass7 extends ContinueTransitionListener {
        final /* synthetic */ ArrayList val$transitioningViews;

        AnonymousClass7(ActivityTransitionCoordinator this$0_1, ArrayList val$transitioningViews) {
            this.val$transitioningViews = val$transitioningViews;
            super();
        }

        public void onTransitionStart(Transition transition) {
            EnterTransitionCoordinator.this.mEnterViewsTransition = transition;
            if (this.val$transitioningViews != null) {
                EnterTransitionCoordinator.this.showViews(this.val$transitioningViews, false);
            }
            super.onTransitionStart(transition);
        }

        public void onTransitionEnd(Transition transition) {
            EnterTransitionCoordinator.this.mEnterViewsTransition = null;
            transition.removeListener(this);
            EnterTransitionCoordinator.this.viewsTransitionComplete();
            super.onTransitionEnd(transition);
        }
    }

    public EnterTransitionCoordinator(Activity activity, ResultReceiver resultReceiver, ArrayList<String> sharedElementNames, boolean isReturning) {
        super(activity.getWindow(), sharedElementNames, getListener(activity, isReturning), isReturning);
        this.mActivity = activity;
        setResultReceiver(resultReceiver);
        prepareEnter();
        Bundle resultReceiverBundle = new Bundle();
        resultReceiverBundle.putParcelable("android:remoteReceiver", this);
        this.mResultReceiver.send(100, resultReceiverBundle);
        View decorView = getDecor();
        if (decorView != null) {
            decorView.getViewTreeObserver().addOnPreDrawListener(new AnonymousClass1(decorView));
        }
    }

    public void viewInstancesReady(ArrayList<String> accepted, ArrayList<String> localNames, ArrayList<View> localViews) {
        boolean remap = false;
        for (int i = 0; i < localViews.size(); i++) {
            View view = (View) localViews.get(i);
            if (!TextUtils.equals(view.getTransitionName(), (CharSequence) localNames.get(i)) || !view.isAttachedToWindow()) {
                remap = true;
                break;
            }
        }
        if (remap) {
            triggerViewsReady(mapNamedElements(accepted, localNames));
        } else {
            triggerViewsReady(mapSharedElements(accepted, localViews));
        }
    }

    public void namedViewsReady(ArrayList<String> accepted, ArrayList<String> localNames) {
        triggerViewsReady(mapNamedElements(accepted, localNames));
    }

    public Transition getEnterViewsTransition() {
        return this.mEnterViewsTransition;
    }

    protected void viewsReady(ArrayMap<String, View> sharedElements) {
        super.viewsReady(sharedElements);
        this.mIsReadyForTransition = true;
        hideViews(this.mSharedElements);
        if (!(getViewsTransition() == null || this.mTransitioningViews == null)) {
            hideViews(this.mTransitioningViews);
        }
        if (this.mIsReturning) {
            sendSharedElementDestination();
        } else {
            moveSharedElementsToOverlay();
        }
        if (this.mSharedElementsBundle != null) {
            onTakeSharedElements();
        }
    }

    private void triggerViewsReady(ArrayMap<String, View> sharedElements) {
        if (!this.mAreViewsReady) {
            this.mAreViewsReady = true;
            ViewGroup decor = getDecor();
            if (decor == null || (decor.isAttachedToWindow() && (sharedElements.isEmpty() || !((View) sharedElements.valueAt(0)).isLayoutRequested()))) {
                viewsReady(sharedElements);
            } else {
                this.mViewsReadyListener = new AnonymousClass2(decor, sharedElements);
                decor.getViewTreeObserver().addOnPreDrawListener(this.mViewsReadyListener);
                decor.invalidate();
            }
        }
    }

    private ArrayMap<String, View> mapNamedElements(ArrayList<String> accepted, ArrayList<String> localNames) {
        ArrayMap<String, View> sharedElements = new ArrayMap();
        ViewGroup decorView = getDecor();
        if (decorView != null) {
            decorView.findNamedViews(sharedElements);
        }
        if (accepted != null) {
            for (int i = 0; i < localNames.size(); i++) {
                String localName = (String) localNames.get(i);
                String acceptedName = (String) accepted.get(i);
                if (!(localName == null || localName.equals(acceptedName))) {
                    View view = (View) sharedElements.remove(localName);
                    if (view != null) {
                        sharedElements.put(acceptedName, view);
                    }
                }
            }
        }
        return sharedElements;
    }

    private void sendSharedElementDestination() {
        boolean z;
        View decorView = getDecor();
        if (allowOverlappingTransitions() && getEnterViewsTransition() != null) {
            z = false;
        } else if (decorView == null) {
            z = true;
        } else {
            z = !decorView.isLayoutRequested();
            if (z) {
                for (int i = 0; i < this.mSharedElements.size(); i++) {
                    if (((View) this.mSharedElements.get(i)).isLayoutRequested()) {
                        z = false;
                        break;
                    }
                }
            }
        }
        if (z) {
            Bundle state = captureSharedElementState();
            moveSharedElementsToOverlay();
            this.mResultReceiver.send(InformationElement.EID_INTERWORKING, state);
        } else if (decorView != null) {
            decorView.getViewTreeObserver().addOnPreDrawListener(new AnonymousClass3(decorView));
        }
        if (allowOverlappingTransitions()) {
            startEnterTransitionOnly();
        }
    }

    private static SharedElementCallback getListener(Activity activity, boolean isReturning) {
        return isReturning ? activity.mExitTransitionListener : activity.mEnterTransitionListener;
    }

    protected void onReceiveResult(int resultCode, Bundle resultData) {
        switch (resultCode) {
            case MediaFile.FILE_TYPE_XML /*103*/:
                if (!this.mIsCanceled) {
                    this.mSharedElementsBundle = resultData;
                    onTakeSharedElements();
                }
            case MediaFile.FILE_TYPE_MS_WORD /*104*/:
                if (!this.mIsCanceled) {
                    this.mIsExitTransitionComplete = true;
                    if (this.mSharedElementTransitionStarted) {
                        onRemoteExitTransitionComplete();
                    }
                }
            case MediaFile.FILE_TYPE_MS_POWERPOINT /*106*/:
                cancel();
            default:
        }
    }

    public boolean isWaitingForRemoteExit() {
        return this.mIsReturning && this.mResultReceiver != null;
    }

    public void forceViewsToAppear() {
        if (this.mIsReturning) {
            if (this.mIsReadyForTransition) {
                if (!this.mSharedElementTransitionStarted) {
                    moveSharedElementsFromOverlay();
                    this.mSharedElementTransitionStarted = true;
                    showViews(this.mSharedElements, true);
                    this.mSharedElements.clear();
                    sharedElementTransitionComplete();
                }
                if (!this.mIsViewsTransitionStarted) {
                    this.mIsViewsTransitionStarted = true;
                    showViews(this.mTransitioningViews, true);
                    setTransitioningViewsVisiblity(0, true);
                    this.mTransitioningViews.clear();
                    viewsTransitionComplete();
                }
                cancelPendingTransitions();
            } else {
                this.mIsReadyForTransition = true;
                ViewGroup decor = getDecor();
                if (!(decor == null || this.mViewsReadyListener == null)) {
                    decor.getViewTreeObserver().removeOnPreDrawListener(this.mViewsReadyListener);
                    this.mViewsReadyListener = null;
                }
                showViews(this.mTransitioningViews, true);
                setTransitioningViewsVisiblity(0, true);
                this.mSharedElements.clear();
                this.mAllSharedElementNames.clear();
                this.mTransitioningViews.clear();
                this.mIsReadyForTransition = true;
                viewsTransitionComplete();
                sharedElementTransitionComplete();
            }
            this.mAreViewsReady = true;
            if (this.mResultReceiver != null) {
                this.mResultReceiver.send(MediaFile.FILE_TYPE_MS_POWERPOINT, null);
                this.mResultReceiver = null;
            }
        }
    }

    private void cancel() {
        if (!this.mIsCanceled) {
            this.mIsCanceled = true;
            if (getViewsTransition() == null || this.mIsViewsTransitionStarted) {
                showViews(this.mSharedElements, true);
            } else if (this.mTransitioningViews != null) {
                this.mTransitioningViews.addAll(this.mSharedElements);
            }
            moveSharedElementsFromOverlay();
            this.mSharedElementNames.clear();
            this.mSharedElements.clear();
            this.mAllSharedElementNames.clear();
            startSharedElementTransition(null);
            onRemoteExitTransitionComplete();
        }
    }

    public boolean isReturning() {
        return this.mIsReturning;
    }

    protected void prepareEnter() {
        ViewGroup decorView = getDecor();
        if (this.mActivity != null && decorView != null) {
            this.mActivity.overridePendingTransition(0, 0);
            if (this.mIsReturning) {
                this.mActivity = null;
            } else {
                this.mWasOpaque = this.mActivity.convertToTranslucent(null, null);
                Drawable background = decorView.getBackground();
                if (background != null) {
                    getWindow().setBackgroundDrawable(null);
                    background = background.mutate();
                    background.setAlpha(0);
                    getWindow().setBackgroundDrawable(background);
                }
            }
        }
    }

    protected Transition getViewsTransition() {
        Window window = getWindow();
        if (window == null) {
            return null;
        }
        if (this.mIsReturning) {
            return window.getReenterTransition();
        }
        return window.getEnterTransition();
    }

    protected Transition getSharedElementTransition() {
        Window window = getWindow();
        if (window == null) {
            return null;
        }
        if (this.mIsReturning) {
            return window.getSharedElementReenterTransition();
        }
        return window.getSharedElementEnterTransition();
    }

    private void startSharedElementTransition(Bundle sharedElementState) {
        ViewGroup decorView = getDecor();
        if (decorView != null) {
            ArrayList<String> rejectedNames = new ArrayList(this.mAllSharedElementNames);
            rejectedNames.removeAll(this.mSharedElementNames);
            ArrayList<View> rejectedSnapshots = createSnapshots(sharedElementState, rejectedNames);
            if (this.mListener != null) {
                this.mListener.onRejectSharedElements(rejectedSnapshots);
            }
            removeNullViews(rejectedSnapshots);
            startRejectedAnimations(rejectedSnapshots);
            ArrayList<View> sharedElementSnapshots = createSnapshots(sharedElementState, this.mSharedElementNames);
            showViews(this.mSharedElements, true);
            scheduleSetSharedElementEnd(sharedElementSnapshots);
            ArrayList<SharedElementOriginalState> originalImageViewState = setSharedElementState(sharedElementState, sharedElementSnapshots);
            requestLayoutForSharedElements();
            boolean startEnterTransition = allowOverlappingTransitions() && !this.mIsReturning;
            setGhostVisibility(4);
            scheduleGhostVisibilityChange(4);
            pauseInput();
            Transition transition = beginTransition(decorView, startEnterTransition, true);
            scheduleGhostVisibilityChange(0);
            setGhostVisibility(0);
            if (startEnterTransition) {
                startEnterTransition(transition);
            }
            ActivityTransitionCoordinator.setOriginalSharedElementState(this.mSharedElements, originalImageViewState);
            if (this.mResultReceiver != null) {
                decorView.postOnAnimation(new Runnable() {
                    int mAnimations;

                    public void run() {
                        int i = this.mAnimations;
                        this.mAnimations = i + 1;
                        if (i < EnterTransitionCoordinator.MIN_ANIMATION_FRAMES) {
                            View decorView = EnterTransitionCoordinator.this.getDecor();
                            if (decorView != null) {
                                decorView.postOnAnimation(this);
                            }
                        } else if (EnterTransitionCoordinator.this.mResultReceiver != null) {
                            EnterTransitionCoordinator.this.mResultReceiver.send(HwSysResource.MAINSERVICES, null);
                            EnterTransitionCoordinator.this.mResultReceiver = null;
                        }
                    }
                });
            }
        }
    }

    private static void removeNullViews(ArrayList<View> views) {
        if (views != null) {
            for (int i = views.size() - 1; i >= 0; i--) {
                if (views.get(i) == null) {
                    views.remove(i);
                }
            }
        }
    }

    private void onTakeSharedElements() {
        Slog.d(TAG, "onTakeSharedElements mIsReadyForTransition= " + this.mIsReadyForTransition + " mSharedElementsBundle= " + this.mSharedElementsBundle + " mListener= " + this.mListener);
        if (this.mIsReadyForTransition && this.mSharedElementsBundle != null) {
            Bundle sharedElementState = this.mSharedElementsBundle;
            this.mSharedElementsBundle = null;
            OnSharedElementsReadyListener listener = new AnonymousClass5(sharedElementState);
            if (this.mListener == null) {
                listener.onSharedElementsReady();
            } else {
                this.mListener.onSharedElementsArrived(this.mSharedElementNames, this.mSharedElements, listener);
            }
        }
    }

    private void requestLayoutForSharedElements() {
        int numSharedElements = this.mSharedElements.size();
        for (int i = 0; i < numSharedElements; i++) {
            ((View) this.mSharedElements.get(i)).requestLayout();
        }
    }

    private Transition beginTransition(ViewGroup decorView, boolean startEnterTransition, boolean startSharedElementTransition) {
        Transition transition = null;
        if (startSharedElementTransition) {
            if (!this.mSharedElementNames.isEmpty()) {
                transition = configureTransition(getSharedElementTransition(), false);
            }
            if (transition == null) {
                sharedElementTransitionStarted();
                sharedElementTransitionComplete();
            } else {
                transition.addListener(new TransitionListenerAdapter() {
                    public void onTransitionStart(Transition transition) {
                        EnterTransitionCoordinator.this.sharedElementTransitionStarted();
                    }

                    public void onTransitionEnd(Transition transition) {
                        transition.removeListener(this);
                        EnterTransitionCoordinator.this.sharedElementTransitionComplete();
                    }
                });
            }
        }
        Transition transition2 = null;
        if (startEnterTransition) {
            this.mIsViewsTransitionStarted = true;
            if (!(this.mTransitioningViews == null || this.mTransitioningViews.isEmpty())) {
                transition2 = configureTransition(getViewsTransition(), true);
                if (!(transition2 == null || this.mIsReturning)) {
                    stripOffscreenViews();
                }
            }
            if (transition2 == null) {
                viewsTransitionComplete();
            } else {
                transition2.addListener(new AnonymousClass7(this, this.mTransitioningViews));
            }
        }
        Transition transition3 = ActivityTransitionCoordinator.mergeTransitions(transition, transition2);
        if (transition3 != null) {
            transition3.addListener(new ContinueTransitionListener());
            if (startEnterTransition) {
                setTransitioningViewsVisiblity(4, false);
            }
            TransitionManager.beginDelayedTransition(decorView, transition3);
            if (startEnterTransition) {
                setTransitioningViewsVisiblity(0, false);
            }
            decorView.invalidate();
        } else {
            transitionStarted();
        }
        return transition3;
    }

    protected void onTransitionsComplete() {
        moveSharedElementsFromOverlay();
        ViewGroup decorView = getDecor();
        if (decorView != null) {
            decorView.sendAccessibilityEvent(Process.PROC_CHAR);
        }
    }

    private void sharedElementTransitionStarted() {
        this.mSharedElementTransitionStarted = true;
        if (this.mIsExitTransitionComplete) {
            send(MediaFile.FILE_TYPE_MS_WORD, null);
        }
    }

    private void startEnterTransition(Transition transition) {
        ViewGroup decorView = getDecor();
        if (!this.mIsReturning && decorView != null) {
            Drawable background = decorView.getBackground();
            if (background != null) {
                Object background2 = background.mutate();
                getWindow().setBackgroundDrawable(background2);
                this.mBackgroundAnimator = ObjectAnimator.ofInt(background2, "alpha", Process.PROC_TERM_MASK);
                this.mBackgroundAnimator.setDuration(getFadeDuration());
                this.mBackgroundAnimator.addListener(new AnimatorListenerAdapter() {
                    public void onAnimationEnd(Animator animation) {
                        EnterTransitionCoordinator.this.makeOpaque();
                    }
                });
                this.mBackgroundAnimator.start();
            } else if (transition != null) {
                transition.addListener(new TransitionListenerAdapter() {
                    public void onTransitionEnd(Transition transition) {
                        transition.removeListener(this);
                        EnterTransitionCoordinator.this.makeOpaque();
                    }
                });
            } else {
                makeOpaque();
            }
        }
    }

    public void stop() {
        if (this.mBackgroundAnimator != null) {
            this.mBackgroundAnimator.end();
            this.mBackgroundAnimator = null;
        } else if (this.mWasOpaque) {
            ViewGroup decorView = getDecor();
            if (decorView != null) {
                Drawable drawable = decorView.getBackground();
                if (drawable != null) {
                    drawable.setAlpha(1);
                }
            }
        }
        makeOpaque();
        this.mIsCanceled = true;
        this.mResultReceiver = null;
        this.mActivity = null;
        moveSharedElementsFromOverlay();
        if (this.mTransitioningViews != null) {
            showViews(this.mTransitioningViews, true);
            setTransitioningViewsVisiblity(0, true);
        }
        showViews(this.mSharedElements, true);
        clearState();
    }

    public boolean cancelEnter() {
        setGhostVisibility(4);
        this.mHasStopped = true;
        this.mIsCanceled = true;
        clearState();
        return super.cancelPendingTransitions();
    }

    protected void clearState() {
        this.mSharedElementsBundle = null;
        this.mEnterViewsTransition = null;
        this.mResultReceiver = null;
        if (this.mBackgroundAnimator != null) {
            this.mBackgroundAnimator.cancel();
            this.mBackgroundAnimator = null;
        }
        super.clearState();
    }

    private void makeOpaque() {
        if (!this.mHasStopped && this.mActivity != null) {
            if (this.mWasOpaque) {
                this.mActivity.convertFromTranslucent();
            }
            this.mActivity = null;
        }
    }

    private boolean allowOverlappingTransitions() {
        if (this.mIsReturning) {
            return getWindow().getAllowReturnTransitionOverlap();
        }
        return getWindow().getAllowEnterTransitionOverlap();
    }

    private void startRejectedAnimations(ArrayList<View> rejectedSnapshots) {
        if (rejectedSnapshots != null && !rejectedSnapshots.isEmpty()) {
            ViewGroup decorView = getDecor();
            if (decorView != null) {
                ViewGroupOverlay overlay = decorView.getOverlay();
                ObjectAnimator animator = null;
                int numRejected = rejectedSnapshots.size();
                for (int i = 0; i < numRejected; i++) {
                    Object snapshot = (View) rejectedSnapshots.get(i);
                    overlay.add(snapshot);
                    Property property = View.ALPHA;
                    float[] fArr = new float[MIN_ANIMATION_FRAMES];
                    fArr[0] = Engine.DEFAULT_VOLUME;
                    fArr[1] = 0.0f;
                    animator = ObjectAnimator.ofFloat(snapshot, property, fArr);
                    animator.start();
                }
                animator.addListener(new AnonymousClass10(decorView, rejectedSnapshots));
            }
        }
    }

    protected void onRemoteExitTransitionComplete() {
        if (!allowOverlappingTransitions()) {
            startEnterTransitionOnly();
        }
    }

    private void startEnterTransitionOnly() {
        startTransition(new Runnable() {
            public void run() {
                ViewGroup decorView = EnterTransitionCoordinator.this.getDecor();
                if (decorView != null) {
                    EnterTransitionCoordinator.this.startEnterTransition(EnterTransitionCoordinator.this.beginTransition(decorView, true, false));
                }
            }
        });
    }
}
