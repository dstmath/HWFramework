package android.app;

import android.os.Bundle;
import android.os.ResultReceiver;
import android.transition.Transition;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver.OnPreDrawListener;
import android.view.Window;
import java.lang.ref.WeakReference;
import java.util.ArrayList;

class ActivityTransitionState {
    private static final String ENTERING_SHARED_ELEMENTS = "android:enteringSharedElements";
    private static final String EXITING_MAPPED_FROM = "android:exitingMappedFrom";
    private static final String EXITING_MAPPED_TO = "android:exitingMappedTo";
    private ExitTransitionCoordinator mCalledExitCoordinator;
    private ActivityOptions mEnterActivityOptions;
    private EnterTransitionCoordinator mEnterTransitionCoordinator;
    private ArrayList<String> mEnteringNames;
    private SparseArray<WeakReference<ExitTransitionCoordinator>> mExitTransitionCoordinators;
    private int mExitTransitionCoordinatorsKey;
    private ArrayList<String> mExitingFrom;
    private ArrayList<String> mExitingTo;
    private ArrayList<View> mExitingToView;
    private boolean mHasExited;
    private boolean mIsEnterPostponed;
    private boolean mIsEnterTriggered;
    private ExitTransitionCoordinator mReturnExitCoordinator;

    /* renamed from: android.app.ActivityTransitionState.2 */
    class AnonymousClass2 implements OnPreDrawListener {
        final /* synthetic */ Activity val$activity;
        final /* synthetic */ ViewGroup val$finalDecor;

        AnonymousClass2(ViewGroup val$finalDecor, Activity val$activity) {
            this.val$finalDecor = val$finalDecor;
            this.val$activity = val$activity;
        }

        public boolean onPreDraw() {
            this.val$finalDecor.getViewTreeObserver().removeOnPreDrawListener(this);
            if (ActivityTransitionState.this.mReturnExitCoordinator != null) {
                ActivityTransitionState.this.mReturnExitCoordinator.startExit(this.val$activity.mResultCode, this.val$activity.mResultData);
            }
            return true;
        }
    }

    public ActivityTransitionState() {
        this.mExitTransitionCoordinatorsKey = 1;
    }

    public int addExitTransitionCoordinator(ExitTransitionCoordinator exitTransitionCoordinator) {
        if (this.mExitTransitionCoordinators == null) {
            this.mExitTransitionCoordinators = new SparseArray();
        }
        WeakReference<ExitTransitionCoordinator> ref = new WeakReference(exitTransitionCoordinator);
        for (int i = this.mExitTransitionCoordinators.size() - 1; i >= 0; i--) {
            if (((WeakReference) this.mExitTransitionCoordinators.valueAt(i)).get() == null) {
                this.mExitTransitionCoordinators.removeAt(i);
            }
        }
        int newKey = this.mExitTransitionCoordinatorsKey;
        this.mExitTransitionCoordinatorsKey = newKey + 1;
        this.mExitTransitionCoordinators.append(newKey, ref);
        return newKey;
    }

    public void readState(Bundle bundle) {
        if (bundle != null) {
            if (this.mEnterTransitionCoordinator == null || this.mEnterTransitionCoordinator.isReturning()) {
                this.mEnteringNames = bundle.getStringArrayList(ENTERING_SHARED_ELEMENTS);
            }
            if (this.mEnterTransitionCoordinator == null) {
                this.mExitingFrom = bundle.getStringArrayList(EXITING_MAPPED_FROM);
                this.mExitingTo = bundle.getStringArrayList(EXITING_MAPPED_TO);
            }
        }
    }

    public void saveState(Bundle bundle) {
        if (this.mEnteringNames != null) {
            bundle.putStringArrayList(ENTERING_SHARED_ELEMENTS, this.mEnteringNames);
        }
        if (this.mExitingFrom != null) {
            bundle.putStringArrayList(EXITING_MAPPED_FROM, this.mExitingFrom);
            bundle.putStringArrayList(EXITING_MAPPED_TO, this.mExitingTo);
        }
    }

    public void setEnterActivityOptions(Activity activity, ActivityOptions options) {
        Window window = activity.getWindow();
        if (window != null) {
            window.getDecorView();
            if (window.hasFeature(13) && options != null && this.mEnterActivityOptions == null && this.mEnterTransitionCoordinator == null && options.getAnimationType() == 5) {
                this.mEnterActivityOptions = options;
                this.mIsEnterTriggered = false;
                if (this.mEnterActivityOptions.isReturning()) {
                    restoreExitedViews();
                    int result = this.mEnterActivityOptions.getResultCode();
                    if (result != 0) {
                        activity.onActivityReenter(result, this.mEnterActivityOptions.getResultData());
                    }
                }
            }
        }
    }

    public void enterReady(Activity activity) {
        if (this.mEnterActivityOptions != null && !this.mIsEnterTriggered) {
            this.mIsEnterTriggered = true;
            this.mHasExited = false;
            ArrayList<String> sharedElementNames = this.mEnterActivityOptions.getSharedElementNames();
            ResultReceiver resultReceiver = this.mEnterActivityOptions.getResultReceiver();
            if (this.mEnterActivityOptions.isReturning()) {
                restoreExitedViews();
                activity.getWindow().getDecorView().setVisibility(0);
            }
            this.mEnterTransitionCoordinator = new EnterTransitionCoordinator(activity, resultReceiver, sharedElementNames, this.mEnterActivityOptions.isReturning());
            if (!this.mIsEnterPostponed) {
                startEnter();
            }
        }
    }

    public void postponeEnterTransition() {
        this.mIsEnterPostponed = true;
    }

    public void startPostponedEnterTransition() {
        if (this.mIsEnterPostponed) {
            this.mIsEnterPostponed = false;
            if (this.mEnterTransitionCoordinator != null) {
                startEnter();
            }
        }
    }

    private void startEnter() {
        if (!this.mEnterTransitionCoordinator.isReturning()) {
            this.mEnterTransitionCoordinator.namedViewsReady(null, null);
            this.mEnteringNames = this.mEnterTransitionCoordinator.getAllSharedElementNames();
        } else if (this.mExitingToView != null) {
            this.mEnterTransitionCoordinator.viewInstancesReady(this.mExitingFrom, this.mExitingTo, this.mExitingToView);
        } else {
            this.mEnterTransitionCoordinator.namedViewsReady(this.mExitingFrom, this.mExitingTo);
        }
        this.mExitingFrom = null;
        this.mExitingTo = null;
        this.mExitingToView = null;
        this.mEnterActivityOptions = null;
    }

    public void onStop() {
        restoreExitedViews();
        if (this.mEnterTransitionCoordinator != null) {
            this.mEnterTransitionCoordinator.stop();
            this.mEnterTransitionCoordinator = null;
        }
        if (this.mReturnExitCoordinator != null) {
            this.mReturnExitCoordinator.stop();
            this.mReturnExitCoordinator = null;
        }
    }

    public void onResume(Activity activity, boolean isTopOfTask) {
        if (isTopOfTask || this.mEnterTransitionCoordinator == null) {
            restoreExitedViews();
            restoreReenteringViews();
            return;
        }
        activity.mHandler.postDelayed(new Runnable() {
            public void run() {
                if (ActivityTransitionState.this.mEnterTransitionCoordinator == null || ActivityTransitionState.this.mEnterTransitionCoordinator.isWaitingForRemoteExit()) {
                    ActivityTransitionState.this.restoreExitedViews();
                    ActivityTransitionState.this.restoreReenteringViews();
                }
            }
        }, 1000);
    }

    public void clear() {
        this.mEnteringNames = null;
        this.mExitingFrom = null;
        this.mExitingTo = null;
        this.mExitingToView = null;
        this.mCalledExitCoordinator = null;
        this.mEnterTransitionCoordinator = null;
        this.mEnterActivityOptions = null;
        this.mExitTransitionCoordinators = null;
    }

    private void restoreExitedViews() {
        if (this.mCalledExitCoordinator != null) {
            this.mCalledExitCoordinator.resetViews();
            this.mCalledExitCoordinator = null;
        }
    }

    private void restoreReenteringViews() {
        if (this.mEnterTransitionCoordinator != null && this.mEnterTransitionCoordinator.isReturning()) {
            this.mEnterTransitionCoordinator.forceViewsToAppear();
            this.mExitingFrom = null;
            this.mExitingTo = null;
            this.mExitingToView = null;
        }
    }

    public boolean startExitBackTransition(Activity activity) {
        if (this.mEnteringNames == null || this.mCalledExitCoordinator != null) {
            return false;
        }
        if (!this.mHasExited) {
            this.mHasExited = true;
            Transition transition = null;
            View view = null;
            boolean z = false;
            if (this.mEnterTransitionCoordinator != null) {
                transition = this.mEnterTransitionCoordinator.getEnterViewsTransition();
                view = this.mEnterTransitionCoordinator.getDecor();
                z = this.mEnterTransitionCoordinator.cancelEnter();
                this.mEnterTransitionCoordinator = null;
                if (!(transition == null || view == null)) {
                    transition.pause(view);
                }
            }
            this.mReturnExitCoordinator = new ExitTransitionCoordinator(activity, this.mEnteringNames, null, null, true);
            if (!(transition == null || view == null)) {
                transition.resume(view);
            }
            if (!z || view == null) {
                this.mReturnExitCoordinator.startExit(activity.mResultCode, activity.mResultData);
            } else {
                view.getViewTreeObserver().addOnPreDrawListener(new AnonymousClass2(view, activity));
            }
        }
        return true;
    }

    public void startExitOutTransition(Activity activity, Bundle options) {
        if (activity.getWindow().hasFeature(13)) {
            ActivityOptions activityOptions = new ActivityOptions(options);
            this.mEnterTransitionCoordinator = null;
            if (activityOptions.getAnimationType() == 5) {
                int index = this.mExitTransitionCoordinators.indexOfKey(activityOptions.getExitCoordinatorKey());
                if (index >= 0) {
                    this.mCalledExitCoordinator = (ExitTransitionCoordinator) ((WeakReference) this.mExitTransitionCoordinators.valueAt(index)).get();
                    this.mExitTransitionCoordinators.removeAt(index);
                    if (this.mCalledExitCoordinator != null) {
                        this.mExitingFrom = this.mCalledExitCoordinator.getAcceptedNames();
                        this.mExitingTo = this.mCalledExitCoordinator.getMappedNames();
                        this.mExitingToView = this.mCalledExitCoordinator.copyMappedViews();
                        this.mCalledExitCoordinator.startExit();
                    }
                }
            }
        }
    }
}
