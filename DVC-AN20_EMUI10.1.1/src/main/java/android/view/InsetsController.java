package android.view;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.TypeEvaluator;
import android.graphics.Insets;
import android.graphics.Rect;
import android.os.RemoteException;
import android.util.ArraySet;
import android.util.Log;
import android.util.Pair;
import android.util.Property;
import android.util.SparseArray;
import android.view.WindowInsets;
import android.view.WindowInsetsAnimationListener;
import android.view.animation.Interpolator;
import android.view.animation.PathInterpolator;
import com.android.internal.annotations.VisibleForTesting;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.function.Supplier;

public class InsetsController implements WindowInsetsController {
    private static final int ANIMATION_DURATION_HIDE_MS = 340;
    private static final int ANIMATION_DURATION_SHOW_MS = 275;
    private static final int DIRECTION_HIDE = 2;
    private static final int DIRECTION_NONE = 0;
    private static final int DIRECTION_SHOW = 1;
    private static final Interpolator INTERPOLATOR = new PathInterpolator(0.4f, 0.0f, 0.2f, 1.0f);
    private static TypeEvaluator<Insets> sEvaluator = $$Lambda$InsetsController$Cj7UJrCkdHvJAZ_cYKrXuTMsjz8.INSTANCE;
    private final String TAG = "InsetsControllerImpl";
    private final Runnable mAnimCallback;
    private boolean mAnimCallbackScheduled;
    private final ArrayList<InsetsAnimationControlImpl> mAnimationControls = new ArrayList<>();
    @AnimationDirection
    private int mAnimationDirection;
    private final Rect mFrame = new Rect();
    private WindowInsets mLastInsets;
    private final Rect mLastLegacyContentInsets = new Rect();
    private int mLastLegacySoftInputMode;
    private final Rect mLastLegacyStableInsets = new Rect();
    private int mPendingTypesToShow;
    private final SparseArray<InsetsSourceConsumer> mSourceConsumers = new SparseArray<>();
    private final InsetsState mState = new InsetsState();
    private final SparseArray<InsetsSourceControl> mTmpControlArray = new SparseArray<>();
    private final ArrayList<InsetsAnimationControlImpl> mTmpFinishedControls = new ArrayList<>();
    private final InsetsState mTmpState = new InsetsState();
    private final ViewRootImpl mViewRoot;

    private @interface AnimationDirection {
    }

    private static class InsetsProperty extends Property<WindowInsetsAnimationController, Insets> {
        InsetsProperty() {
            super(Insets.class, "Insets");
        }

        public Insets get(WindowInsetsAnimationController object) {
            return object.getCurrentInsets();
        }

        public void set(WindowInsetsAnimationController object, Insets value) {
            object.changeInsets(value);
        }
    }

    public InsetsController(ViewRootImpl viewRoot) {
        this.mViewRoot = viewRoot;
        this.mAnimCallback = new Runnable() {
            /* class android.view.$$Lambda$InsetsController$HI9QZ2HvGm6iykcWONz2KPG61Q */

            public final void run() {
                InsetsController.this.lambda$new$1$InsetsController();
            }
        };
    }

    public /* synthetic */ void lambda$new$1$InsetsController() {
        this.mAnimCallbackScheduled = false;
        if (!this.mAnimationControls.isEmpty()) {
            this.mTmpFinishedControls.clear();
            InsetsState state = new InsetsState(this.mState, true);
            for (int i = this.mAnimationControls.size() - 1; i >= 0; i--) {
                InsetsAnimationControlImpl control = this.mAnimationControls.get(i);
                if (this.mAnimationControls.get(i).applyChangeInsets(state)) {
                    this.mTmpFinishedControls.add(control);
                }
            }
            this.mViewRoot.mView.dispatchWindowInsetsAnimationProgress(state.calculateInsets(this.mFrame, this.mLastInsets.isRound(), this.mLastInsets.shouldAlwaysConsumeSystemBars(), this.mLastInsets.getDisplayCutout(), this.mLastLegacyContentInsets, this.mLastLegacyStableInsets, this.mLastLegacySoftInputMode, null));
            for (int i2 = this.mTmpFinishedControls.size() - 1; i2 >= 0; i2--) {
                dispatchAnimationFinished(this.mTmpFinishedControls.get(i2).getAnimation());
            }
        }
    }

    @VisibleForTesting
    public void onFrameChanged(Rect frame) {
        if (!this.mFrame.equals(frame)) {
            this.mViewRoot.notifyInsetsChanged();
            this.mFrame.set(frame);
        }
    }

    public InsetsState getState() {
        return this.mState;
    }

    /* access modifiers changed from: package-private */
    public boolean onStateChanged(InsetsState state) {
        if (this.mState.equals(state)) {
            return false;
        }
        this.mState.set(state);
        this.mTmpState.set(state, true);
        applyLocalVisibilityOverride();
        this.mViewRoot.notifyInsetsChanged();
        if (!this.mState.equals(this.mTmpState)) {
            sendStateToWindowManager();
        }
        return true;
    }

    @VisibleForTesting
    public WindowInsets calculateInsets(boolean isScreenRound, boolean alwaysConsumeSystemBars, DisplayCutout cutout, Rect legacyContentInsets, Rect legacyStableInsets, int legacySoftInputMode) {
        this.mLastLegacyContentInsets.set(legacyContentInsets);
        this.mLastLegacyStableInsets.set(legacyStableInsets);
        this.mLastLegacySoftInputMode = legacySoftInputMode;
        this.mLastInsets = this.mState.calculateInsets(this.mFrame, isScreenRound, alwaysConsumeSystemBars, cutout, legacyContentInsets, legacyStableInsets, legacySoftInputMode, null);
        return this.mLastInsets;
    }

    public void onControlsChanged(InsetsSourceControl[] activeControls) {
        if (activeControls != null) {
            for (InsetsSourceControl activeControl : activeControls) {
                if (activeControl != null) {
                    this.mTmpControlArray.put(activeControl.getType(), activeControl);
                }
            }
        }
        for (int i = this.mSourceConsumers.size() - 1; i >= 0; i--) {
            InsetsSourceConsumer consumer = this.mSourceConsumers.valueAt(i);
            consumer.setControl(this.mTmpControlArray.get(consumer.getType()));
        }
        for (int i2 = this.mTmpControlArray.size() - 1; i2 >= 0; i2--) {
            InsetsSourceControl control = this.mTmpControlArray.valueAt(i2);
            getSourceConsumer(control.getType()).setControl(control);
        }
        this.mTmpControlArray.clear();
    }

    @Override // android.view.WindowInsetsController
    public void show(int types) {
        show(types, false);
    }

    private void show(int types, boolean fromIme) {
        int typesReady = 0;
        ArraySet<Integer> internalTypes = InsetsState.toInternalType(types);
        for (int i = internalTypes.size() - 1; i >= 0; i--) {
            InsetsSourceConsumer consumer = getSourceConsumer(internalTypes.valueAt(i).intValue());
            if (this.mAnimationDirection == 2) {
                cancelExistingAnimation();
            } else if (consumer.isVisible()) {
                int i2 = this.mAnimationDirection;
                if (i2 != 0) {
                    if (i2 == 2) {
                    }
                }
            }
            typesReady |= InsetsState.toPublicType(consumer.getType());
        }
        applyAnimation(typesReady, true, fromIme);
    }

    @Override // android.view.WindowInsetsController
    public void hide(int types) {
        int typesReady = 0;
        ArraySet<Integer> internalTypes = InsetsState.toInternalType(types);
        for (int i = internalTypes.size() - 1; i >= 0; i--) {
            InsetsSourceConsumer consumer = getSourceConsumer(internalTypes.valueAt(i).intValue());
            if (this.mAnimationDirection == 1) {
                cancelExistingAnimation();
            } else if (!consumer.isVisible()) {
                int i2 = this.mAnimationDirection;
                if (i2 != 0) {
                    if (i2 == 2) {
                    }
                }
            }
            typesReady |= InsetsState.toPublicType(consumer.getType());
        }
        applyAnimation(typesReady, false, false);
    }

    @Override // android.view.WindowInsetsController
    public void controlWindowInsetsAnimation(int types, WindowInsetsAnimationControlListener listener) {
        controlWindowInsetsAnimation(types, listener, false);
    }

    private void controlWindowInsetsAnimation(int types, WindowInsetsAnimationControlListener listener, boolean fromIme) {
        if (!this.mState.getDisplayFrame().equals(this.mFrame)) {
            listener.onCancelled();
        } else {
            controlAnimationUnchecked(types, listener, this.mFrame, fromIme);
        }
    }

    private void controlAnimationUnchecked(int types, WindowInsetsAnimationControlListener listener, Rect frame, boolean fromIme) {
        if (types != 0) {
            cancelExistingControllers(types);
            InsetsState insetsState = this.mState;
            ArraySet<Integer> internalTypes = InsetsState.toInternalType(types);
            SparseArray<InsetsSourceConsumer> consumers = new SparseArray<>();
            Pair<Integer, Boolean> typesReadyPair = collectConsumers(fromIme, internalTypes, consumers);
            int typesReady = typesReadyPair.first.intValue();
            if (!typesReadyPair.second.booleanValue()) {
                this.mPendingTypesToShow = typesReady;
                return;
            }
            int typesReady2 = collectPendingConsumers(typesReady, consumers);
            if (typesReady2 == 0) {
                listener.onCancelled();
                return;
            }
            this.mAnimationControls.add(new InsetsAnimationControlImpl(consumers, frame, this.mState, listener, typesReady2, new Supplier() {
                /* class android.view.$$Lambda$InsetsController$n9dGLDW5oKSxT73i9ZlnIPWSzms */

                @Override // java.util.function.Supplier
                public final Object get() {
                    return InsetsController.this.lambda$controlAnimationUnchecked$2$InsetsController();
                }
            }, this));
        }
    }

    public /* synthetic */ SyncRtSurfaceTransactionApplier lambda$controlAnimationUnchecked$2$InsetsController() {
        return new SyncRtSurfaceTransactionApplier(this.mViewRoot.mView);
    }

    private Pair<Integer, Boolean> collectConsumers(boolean fromIme, ArraySet<Integer> internalTypes, SparseArray<InsetsSourceConsumer> consumers) {
        int i;
        int typesReady = 0;
        boolean isReady = true;
        for (int i2 = internalTypes.size() - 1; i2 >= 0; i2--) {
            InsetsSourceConsumer consumer = getSourceConsumer(internalTypes.valueAt(i2).intValue());
            if (consumer.getControl() != null) {
                if (!consumer.isVisible()) {
                    int requestShow = consumer.requestShow(fromIme);
                    if (requestShow == 0) {
                        typesReady |= InsetsState.toPublicType(consumer.getType());
                    } else if (requestShow == 1) {
                        isReady = false;
                    } else if (requestShow == 2 && (i = this.mPendingTypesToShow) != 0) {
                        this.mPendingTypesToShow = i & (~InsetsState.toPublicType(10));
                    }
                } else {
                    consumer.notifyHidden();
                    typesReady |= InsetsState.toPublicType(consumer.getType());
                }
                consumers.put(consumer.getType(), consumer);
            }
        }
        return new Pair<>(Integer.valueOf(typesReady), Boolean.valueOf(isReady));
    }

    private int collectPendingConsumers(int typesReady, SparseArray<InsetsSourceConsumer> consumers) {
        int i = this.mPendingTypesToShow;
        if (i != 0) {
            typesReady |= i;
            InsetsState insetsState = this.mState;
            ArraySet<Integer> internalTypes = InsetsState.toInternalType(i);
            for (int i2 = internalTypes.size() - 1; i2 >= 0; i2--) {
                InsetsSourceConsumer consumer = getSourceConsumer(internalTypes.valueAt(i2).intValue());
                consumers.put(consumer.getType(), consumer);
            }
            this.mPendingTypesToShow = 0;
        }
        return typesReady;
    }

    private void cancelExistingControllers(int types) {
        for (int i = this.mAnimationControls.size() - 1; i >= 0; i--) {
            InsetsAnimationControlImpl control = this.mAnimationControls.get(i);
            if ((control.getTypes() & types) != 0) {
                cancelAnimation(control);
            }
        }
    }

    @VisibleForTesting
    public void notifyFinished(InsetsAnimationControlImpl controller, int shownTypes) {
        this.mAnimationControls.remove(controller);
        hideDirectly(controller.getTypes() & (~shownTypes));
        showDirectly(controller.getTypes() & shownTypes);
    }

    /* access modifiers changed from: package-private */
    public void notifyControlRevoked(InsetsSourceConsumer consumer) {
        for (int i = this.mAnimationControls.size() - 1; i >= 0; i--) {
            InsetsAnimationControlImpl control = this.mAnimationControls.get(i);
            if ((control.getTypes() & InsetsState.toPublicType(consumer.getType())) != 0) {
                cancelAnimation(control);
            }
        }
    }

    private void cancelAnimation(InsetsAnimationControlImpl control) {
        control.onCancelled();
        this.mAnimationControls.remove(control);
    }

    private void applyLocalVisibilityOverride() {
        for (int i = this.mSourceConsumers.size() - 1; i >= 0; i--) {
            this.mSourceConsumers.valueAt(i).applyLocalVisibilityOverride();
        }
    }

    @VisibleForTesting
    public InsetsSourceConsumer getSourceConsumer(int type) {
        InsetsSourceConsumer controller = this.mSourceConsumers.get(type);
        if (controller != null) {
            return controller;
        }
        InsetsSourceConsumer controller2 = createConsumerOfType(type);
        this.mSourceConsumers.put(type, controller2);
        return controller2;
    }

    @VisibleForTesting
    public void notifyVisibilityChanged() {
        this.mViewRoot.notifyInsetsChanged();
        sendStateToWindowManager();
    }

    public void onWindowFocusGained() {
        getSourceConsumer(10).onWindowFocusGained();
    }

    public void onWindowFocusLost() {
        getSourceConsumer(10).onWindowFocusLost();
    }

    /* access modifiers changed from: package-private */
    public ViewRootImpl getViewRoot() {
        return this.mViewRoot;
    }

    @VisibleForTesting
    public void applyImeVisibility(boolean setVisible) {
        if (setVisible) {
            show(2, true);
        } else {
            hide(2);
        }
    }

    private InsetsSourceConsumer createConsumerOfType(int type) {
        if (type == 10) {
            return new ImeInsetsSourceConsumer(this.mState, $$Lambda$9vBfnQOmNnsc9WU80IIatZHQGKc.INSTANCE, this);
        }
        return new InsetsSourceConsumer(type, this.mState, $$Lambda$9vBfnQOmNnsc9WU80IIatZHQGKc.INSTANCE, this);
    }

    private void sendStateToWindowManager() {
        InsetsState tmpState = new InsetsState();
        for (int i = this.mSourceConsumers.size() - 1; i >= 0; i--) {
            InsetsSourceConsumer consumer = this.mSourceConsumers.valueAt(i);
            if (consumer.getControl() != null) {
                tmpState.addSource(this.mState.getSource(consumer.getType()));
            }
        }
        try {
            this.mViewRoot.mWindowSession.insetsModified(this.mViewRoot.mWindow, tmpState);
        } catch (RemoteException e) {
            Log.e("InsetsControllerImpl", "Failed to call insetsModified", e);
        }
    }

    private void applyAnimation(final int types, final boolean show, boolean fromIme) {
        if (types != 0) {
            controlAnimationUnchecked(types, new WindowInsetsAnimationControlListener() {
                /* class android.view.InsetsController.AnonymousClass1 */
                private ObjectAnimator mAnimator;
                private WindowInsetsAnimationController mController;

                @Override // android.view.WindowInsetsAnimationControlListener
                public void onReady(WindowInsetsAnimationController controller, int types) {
                    long j;
                    this.mController = controller;
                    if (show) {
                        InsetsController.this.showDirectly(types);
                    } else {
                        InsetsController.this.hideDirectly(types);
                    }
                    InsetsProperty insetsProperty = new InsetsProperty();
                    TypeEvaluator typeEvaluator = InsetsController.sEvaluator;
                    Insets[] insetsArr = new Insets[2];
                    insetsArr[0] = show ? controller.getHiddenStateInsets() : controller.getShownStateInsets();
                    insetsArr[1] = show ? controller.getShownStateInsets() : controller.getHiddenStateInsets();
                    this.mAnimator = ObjectAnimator.ofObject(controller, insetsProperty, typeEvaluator, insetsArr);
                    ObjectAnimator objectAnimator = this.mAnimator;
                    if (show) {
                        j = 275;
                    } else {
                        j = 340;
                    }
                    objectAnimator.setDuration(j);
                    this.mAnimator.setInterpolator(InsetsController.INTERPOLATOR);
                    this.mAnimator.addListener(new AnimatorListenerAdapter() {
                        /* class android.view.InsetsController.AnonymousClass1.AnonymousClass1 */

                        @Override // android.animation.Animator.AnimatorListener, android.animation.AnimatorListenerAdapter
                        public void onAnimationEnd(Animator animation) {
                            AnonymousClass1.this.onAnimationFinish();
                        }
                    });
                    this.mAnimator.start();
                }

                @Override // android.view.WindowInsetsAnimationControlListener
                public void onCancelled() {
                    this.mAnimator.cancel();
                }

                /* access modifiers changed from: private */
                /* access modifiers changed from: public */
                private void onAnimationFinish() {
                    int i = 0;
                    InsetsController.this.mAnimationDirection = 0;
                    WindowInsetsAnimationController windowInsetsAnimationController = this.mController;
                    if (show) {
                        i = types;
                    }
                    windowInsetsAnimationController.finish(i);
                }
            }, this.mState.getDisplayFrame(), fromIme);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void hideDirectly(int types) {
        ArraySet<Integer> internalTypes = InsetsState.toInternalType(types);
        for (int i = internalTypes.size() - 1; i >= 0; i--) {
            getSourceConsumer(internalTypes.valueAt(i).intValue()).hide();
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void showDirectly(int types) {
        ArraySet<Integer> internalTypes = InsetsState.toInternalType(types);
        for (int i = internalTypes.size() - 1; i >= 0; i--) {
            getSourceConsumer(internalTypes.valueAt(i).intValue()).show();
        }
    }

    @VisibleForTesting
    public void cancelExistingAnimation() {
        cancelExistingControllers(WindowInsets.Type.all());
    }

    /* access modifiers changed from: package-private */
    public void dump(String prefix, PrintWriter pw) {
        pw.println(prefix);
        pw.println("InsetsController:");
        InsetsState insetsState = this.mState;
        insetsState.dump(prefix + "  ", pw);
    }

    @VisibleForTesting
    public void dispatchAnimationStarted(WindowInsetsAnimationListener.InsetsAnimation animation) {
        this.mViewRoot.mView.dispatchWindowInsetsAnimationStarted(animation);
    }

    @VisibleForTesting
    public void dispatchAnimationFinished(WindowInsetsAnimationListener.InsetsAnimation animation) {
        this.mViewRoot.mView.dispatchWindowInsetsAnimationFinished(animation);
    }

    @VisibleForTesting
    public void scheduleApplyChangeInsets() {
        if (!this.mAnimCallbackScheduled) {
            this.mViewRoot.mChoreographer.postCallback(2, this.mAnimCallback, null);
            this.mAnimCallbackScheduled = true;
        }
    }
}
