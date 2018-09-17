package android.animation;

import android.hardware.camera2.params.TonemapCurve;
import android.view.View;
import android.view.View.OnAttachStateChangeListener;
import android.view.View.OnLayoutChangeListener;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.ViewTreeObserver;
import android.view.ViewTreeObserver.OnPreDrawListener;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class LayoutTransition {
    private static TimeInterpolator ACCEL_DECEL_INTERPOLATOR = new AccelerateDecelerateInterpolator();
    public static final int APPEARING = 2;
    public static final int CHANGE_APPEARING = 0;
    public static final int CHANGE_DISAPPEARING = 1;
    public static final int CHANGING = 4;
    private static TimeInterpolator DECEL_INTERPOLATOR = new DecelerateInterpolator();
    private static long DEFAULT_DURATION = 300;
    public static final int DISAPPEARING = 3;
    private static final int FLAG_APPEARING = 1;
    private static final int FLAG_CHANGE_APPEARING = 4;
    private static final int FLAG_CHANGE_DISAPPEARING = 8;
    private static final int FLAG_CHANGING = 16;
    private static final int FLAG_DISAPPEARING = 2;
    private static ObjectAnimator defaultChange;
    private static ObjectAnimator defaultChangeIn;
    private static ObjectAnimator defaultChangeOut;
    private static ObjectAnimator defaultFadeIn;
    private static ObjectAnimator defaultFadeOut;
    private static TimeInterpolator sAppearingInterpolator = ACCEL_DECEL_INTERPOLATOR;
    private static TimeInterpolator sChangingAppearingInterpolator = DECEL_INTERPOLATOR;
    private static TimeInterpolator sChangingDisappearingInterpolator = DECEL_INTERPOLATOR;
    private static TimeInterpolator sChangingInterpolator = DECEL_INTERPOLATOR;
    private static TimeInterpolator sDisappearingInterpolator = ACCEL_DECEL_INTERPOLATOR;
    private final LinkedHashMap<View, Animator> currentAppearingAnimations = new LinkedHashMap();
    private final LinkedHashMap<View, Animator> currentChangingAnimations = new LinkedHashMap();
    private final LinkedHashMap<View, Animator> currentDisappearingAnimations = new LinkedHashMap();
    private final HashMap<View, OnLayoutChangeListener> layoutChangeListenerMap = new HashMap();
    private boolean mAnimateParentHierarchy = true;
    private Animator mAppearingAnim = null;
    private long mAppearingDelay = DEFAULT_DURATION;
    private long mAppearingDuration = DEFAULT_DURATION;
    private TimeInterpolator mAppearingInterpolator = sAppearingInterpolator;
    private Animator mChangingAnim = null;
    private Animator mChangingAppearingAnim = null;
    private long mChangingAppearingDelay = 0;
    private long mChangingAppearingDuration = DEFAULT_DURATION;
    private TimeInterpolator mChangingAppearingInterpolator = sChangingAppearingInterpolator;
    private long mChangingAppearingStagger = 0;
    private long mChangingDelay = 0;
    private Animator mChangingDisappearingAnim = null;
    private long mChangingDisappearingDelay = DEFAULT_DURATION;
    private long mChangingDisappearingDuration = DEFAULT_DURATION;
    private TimeInterpolator mChangingDisappearingInterpolator = sChangingDisappearingInterpolator;
    private long mChangingDisappearingStagger = 0;
    private long mChangingDuration = DEFAULT_DURATION;
    private TimeInterpolator mChangingInterpolator = sChangingInterpolator;
    private long mChangingStagger = 0;
    private Animator mDisappearingAnim = null;
    private long mDisappearingDelay = 0;
    private long mDisappearingDuration = DEFAULT_DURATION;
    private TimeInterpolator mDisappearingInterpolator = sDisappearingInterpolator;
    private ArrayList<TransitionListener> mListeners;
    private int mTransitionTypes = 15;
    private final HashMap<View, Animator> pendingAnimations = new HashMap();
    private long staggerDelay;

    private static final class CleanupCallback implements OnPreDrawListener, OnAttachStateChangeListener {
        final Map<View, OnLayoutChangeListener> layoutChangeListenerMap;
        final ViewGroup parent;

        CleanupCallback(Map<View, OnLayoutChangeListener> listenerMap, ViewGroup parent) {
            this.layoutChangeListenerMap = listenerMap;
            this.parent = parent;
        }

        private void cleanup() {
            this.parent.getViewTreeObserver().removeOnPreDrawListener(this);
            this.parent.removeOnAttachStateChangeListener(this);
            if (this.layoutChangeListenerMap.size() > 0) {
                for (View view : this.layoutChangeListenerMap.keySet()) {
                    view.removeOnLayoutChangeListener((OnLayoutChangeListener) this.layoutChangeListenerMap.get(view));
                }
                this.layoutChangeListenerMap.clear();
            }
        }

        public void onViewAttachedToWindow(View v) {
        }

        public void onViewDetachedFromWindow(View v) {
            cleanup();
        }

        public boolean onPreDraw() {
            cleanup();
            return true;
        }
    }

    public interface TransitionListener {
        void endTransition(LayoutTransition layoutTransition, ViewGroup viewGroup, View view, int i);

        void startTransition(LayoutTransition layoutTransition, ViewGroup viewGroup, View view, int i);
    }

    public LayoutTransition() {
        if (defaultChangeIn == null) {
            PropertyValuesHolder pvhLeft = PropertyValuesHolder.ofInt("left", 0, 1);
            PropertyValuesHolder pvhTop = PropertyValuesHolder.ofInt("top", 0, 1);
            PropertyValuesHolder pvhRight = PropertyValuesHolder.ofInt("right", 0, 1);
            PropertyValuesHolder pvhBottom = PropertyValuesHolder.ofInt("bottom", 0, 1);
            PropertyValuesHolder pvhScrollX = PropertyValuesHolder.ofInt("scrollX", 0, 1);
            PropertyValuesHolder pvhScrollY = PropertyValuesHolder.ofInt("scrollY", 0, 1);
            defaultChangeIn = ObjectAnimator.ofPropertyValuesHolder(null, pvhLeft, pvhTop, pvhRight, pvhBottom, pvhScrollX, pvhScrollY);
            defaultChangeIn.setDuration(DEFAULT_DURATION);
            defaultChangeIn.setStartDelay(this.mChangingAppearingDelay);
            defaultChangeIn.setInterpolator(this.mChangingAppearingInterpolator);
            defaultChangeOut = defaultChangeIn.clone();
            defaultChangeOut.setStartDelay(this.mChangingDisappearingDelay);
            defaultChangeOut.setInterpolator(this.mChangingDisappearingInterpolator);
            defaultChange = defaultChangeIn.clone();
            defaultChange.setStartDelay(this.mChangingDelay);
            defaultChange.setInterpolator(this.mChangingInterpolator);
            defaultFadeIn = ObjectAnimator.ofFloat(null, "alpha", TonemapCurve.LEVEL_BLACK, 1.0f);
            defaultFadeIn.setDuration(DEFAULT_DURATION);
            defaultFadeIn.setStartDelay(this.mAppearingDelay);
            defaultFadeIn.setInterpolator(this.mAppearingInterpolator);
            defaultFadeOut = ObjectAnimator.ofFloat(null, "alpha", 1.0f, TonemapCurve.LEVEL_BLACK);
            defaultFadeOut.setDuration(DEFAULT_DURATION);
            defaultFadeOut.setStartDelay(this.mDisappearingDelay);
            defaultFadeOut.setInterpolator(this.mDisappearingInterpolator);
        }
        this.mChangingAppearingAnim = defaultChangeIn;
        this.mChangingDisappearingAnim = defaultChangeOut;
        this.mChangingAnim = defaultChange;
        this.mAppearingAnim = defaultFadeIn;
        this.mDisappearingAnim = defaultFadeOut;
    }

    public void setDuration(long duration) {
        this.mChangingAppearingDuration = duration;
        this.mChangingDisappearingDuration = duration;
        this.mChangingDuration = duration;
        this.mAppearingDuration = duration;
        this.mDisappearingDuration = duration;
    }

    public void enableTransitionType(int transitionType) {
        switch (transitionType) {
            case 0:
                this.mTransitionTypes |= 4;
                return;
            case 1:
                this.mTransitionTypes |= 8;
                return;
            case 2:
                this.mTransitionTypes |= 1;
                return;
            case 3:
                this.mTransitionTypes |= 2;
                return;
            case 4:
                this.mTransitionTypes |= 16;
                return;
            default:
                return;
        }
    }

    public void disableTransitionType(int transitionType) {
        switch (transitionType) {
            case 0:
                this.mTransitionTypes &= -5;
                return;
            case 1:
                this.mTransitionTypes &= -9;
                return;
            case 2:
                this.mTransitionTypes &= -2;
                return;
            case 3:
                this.mTransitionTypes &= -3;
                return;
            case 4:
                this.mTransitionTypes &= -17;
                return;
            default:
                return;
        }
    }

    public boolean isTransitionTypeEnabled(int transitionType) {
        boolean z = true;
        switch (transitionType) {
            case 0:
                if ((this.mTransitionTypes & 4) != 4) {
                    z = false;
                }
                return z;
            case 1:
                if ((this.mTransitionTypes & 8) != 8) {
                    z = false;
                }
                return z;
            case 2:
                if ((this.mTransitionTypes & 1) != 1) {
                    z = false;
                }
                return z;
            case 3:
                if ((this.mTransitionTypes & 2) != 2) {
                    z = false;
                }
                return z;
            case 4:
                if ((this.mTransitionTypes & 16) != 16) {
                    z = false;
                }
                return z;
            default:
                return false;
        }
    }

    public void setStartDelay(int transitionType, long delay) {
        switch (transitionType) {
            case 0:
                this.mChangingAppearingDelay = delay;
                return;
            case 1:
                this.mChangingDisappearingDelay = delay;
                return;
            case 2:
                this.mAppearingDelay = delay;
                return;
            case 3:
                this.mDisappearingDelay = delay;
                return;
            case 4:
                this.mChangingDelay = delay;
                return;
            default:
                return;
        }
    }

    public long getStartDelay(int transitionType) {
        switch (transitionType) {
            case 0:
                return this.mChangingAppearingDelay;
            case 1:
                return this.mChangingDisappearingDelay;
            case 2:
                return this.mAppearingDelay;
            case 3:
                return this.mDisappearingDelay;
            case 4:
                return this.mChangingDelay;
            default:
                return 0;
        }
    }

    public void setDuration(int transitionType, long duration) {
        switch (transitionType) {
            case 0:
                this.mChangingAppearingDuration = duration;
                return;
            case 1:
                this.mChangingDisappearingDuration = duration;
                return;
            case 2:
                this.mAppearingDuration = duration;
                return;
            case 3:
                this.mDisappearingDuration = duration;
                return;
            case 4:
                this.mChangingDuration = duration;
                return;
            default:
                return;
        }
    }

    public long getDuration(int transitionType) {
        switch (transitionType) {
            case 0:
                return this.mChangingAppearingDuration;
            case 1:
                return this.mChangingDisappearingDuration;
            case 2:
                return this.mAppearingDuration;
            case 3:
                return this.mDisappearingDuration;
            case 4:
                return this.mChangingDuration;
            default:
                return 0;
        }
    }

    public void setStagger(int transitionType, long duration) {
        switch (transitionType) {
            case 0:
                this.mChangingAppearingStagger = duration;
                return;
            case 1:
                this.mChangingDisappearingStagger = duration;
                return;
            case 4:
                this.mChangingStagger = duration;
                return;
            default:
                return;
        }
    }

    public long getStagger(int transitionType) {
        switch (transitionType) {
            case 0:
                return this.mChangingAppearingStagger;
            case 1:
                return this.mChangingDisappearingStagger;
            case 4:
                return this.mChangingStagger;
            default:
                return 0;
        }
    }

    public void setInterpolator(int transitionType, TimeInterpolator interpolator) {
        switch (transitionType) {
            case 0:
                this.mChangingAppearingInterpolator = interpolator;
                return;
            case 1:
                this.mChangingDisappearingInterpolator = interpolator;
                return;
            case 2:
                this.mAppearingInterpolator = interpolator;
                return;
            case 3:
                this.mDisappearingInterpolator = interpolator;
                return;
            case 4:
                this.mChangingInterpolator = interpolator;
                return;
            default:
                return;
        }
    }

    public TimeInterpolator getInterpolator(int transitionType) {
        switch (transitionType) {
            case 0:
                return this.mChangingAppearingInterpolator;
            case 1:
                return this.mChangingDisappearingInterpolator;
            case 2:
                return this.mAppearingInterpolator;
            case 3:
                return this.mDisappearingInterpolator;
            case 4:
                return this.mChangingInterpolator;
            default:
                return null;
        }
    }

    public void setAnimator(int transitionType, Animator animator) {
        switch (transitionType) {
            case 0:
                this.mChangingAppearingAnim = animator;
                return;
            case 1:
                this.mChangingDisappearingAnim = animator;
                return;
            case 2:
                this.mAppearingAnim = animator;
                return;
            case 3:
                this.mDisappearingAnim = animator;
                return;
            case 4:
                this.mChangingAnim = animator;
                return;
            default:
                return;
        }
    }

    public Animator getAnimator(int transitionType) {
        switch (transitionType) {
            case 0:
                return this.mChangingAppearingAnim;
            case 1:
                return this.mChangingDisappearingAnim;
            case 2:
                return this.mAppearingAnim;
            case 3:
                return this.mDisappearingAnim;
            case 4:
                return this.mChangingAnim;
            default:
                return null;
        }
    }

    private void runChangeTransition(ViewGroup parent, View newView, int changeReason) {
        long duration;
        Animator baseAnimator = null;
        Animator parentAnimator = null;
        switch (changeReason) {
            case 2:
                baseAnimator = this.mChangingAppearingAnim;
                duration = this.mChangingAppearingDuration;
                parentAnimator = defaultChangeIn;
                break;
            case 3:
                baseAnimator = this.mChangingDisappearingAnim;
                duration = this.mChangingDisappearingDuration;
                parentAnimator = defaultChangeOut;
                break;
            case 4:
                baseAnimator = this.mChangingAnim;
                duration = this.mChangingDuration;
                parentAnimator = defaultChange;
                break;
            default:
                duration = 0;
                break;
        }
        if (baseAnimator != null) {
            this.staggerDelay = 0;
            ViewTreeObserver observer = parent.getViewTreeObserver();
            if (observer.isAlive()) {
                int numChildren = parent.getChildCount();
                for (int i = 0; i < numChildren; i++) {
                    View child = parent.getChildAt(i);
                    if (child != newView) {
                        setupChangeAnimation(parent, changeReason, baseAnimator, duration, child);
                    }
                }
                if (this.mAnimateParentHierarchy) {
                    View tempParent = parent;
                    while (tempParent != null) {
                        ViewParent parentParent = tempParent.getParent();
                        if (parentParent instanceof ViewGroup) {
                            setupChangeAnimation((ViewGroup) parentParent, changeReason, parentAnimator, duration, tempParent);
                            tempParent = (ViewGroup) parentParent;
                        } else {
                            tempParent = null;
                        }
                    }
                }
                CleanupCallback callback = new CleanupCallback(this.layoutChangeListenerMap, parent);
                observer.addOnPreDrawListener(callback);
                parent.addOnAttachStateChangeListener(callback);
            }
        }
    }

    public void setAnimateParentHierarchy(boolean animateParentHierarchy) {
        this.mAnimateParentHierarchy = animateParentHierarchy;
    }

    private void setupChangeAnimation(ViewGroup parent, int changeReason, Animator baseAnimator, long duration, View child) {
        if (this.layoutChangeListenerMap.get(child) == null) {
            if (child.getWidth() != 0 || child.getHeight() != 0) {
                final Animator anim = baseAnimator.clone();
                anim.setTarget(child);
                anim.setupStartValues();
                Animator currentAnimation = (Animator) this.pendingAnimations.get(child);
                if (currentAnimation != null) {
                    currentAnimation.cancel();
                    this.pendingAnimations.remove(child);
                }
                this.pendingAnimations.put(child, anim);
                ValueAnimator pendingAnimRemover = ValueAnimator.ofFloat(TonemapCurve.LEVEL_BLACK, 1.0f).setDuration(100 + duration);
                final View view = child;
                pendingAnimRemover.addListener(new AnimatorListenerAdapter() {
                    public void onAnimationEnd(Animator animation) {
                        LayoutTransition.this.pendingAnimations.remove(view);
                    }
                });
                pendingAnimRemover.start();
                final int i = changeReason;
                final long j = duration;
                final View view2 = child;
                final ViewGroup viewGroup = parent;
                OnLayoutChangeListener listener = new OnLayoutChangeListener() {
                    public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                        anim.setupEndValues();
                        if (anim instanceof ValueAnimator) {
                            boolean valuesDiffer = false;
                            PropertyValuesHolder[] oldValues = anim.getValues();
                            for (PropertyValuesHolder pvh : oldValues) {
                                if (pvh.mKeyframes instanceof KeyframeSet) {
                                    KeyframeSet keyframeSet = pvh.mKeyframes;
                                    if (keyframeSet.mFirstKeyframe == null || keyframeSet.mLastKeyframe == null || (keyframeSet.mFirstKeyframe.getValue().equals(keyframeSet.mLastKeyframe.getValue()) ^ 1) != 0) {
                                        valuesDiffer = true;
                                    }
                                } else if (!pvh.mKeyframes.getValue(TonemapCurve.LEVEL_BLACK).equals(pvh.mKeyframes.getValue(1.0f))) {
                                    valuesDiffer = true;
                                }
                            }
                            if (!valuesDiffer) {
                                return;
                            }
                        }
                        long startDelay = 0;
                        LayoutTransition layoutTransition;
                        switch (i) {
                            case 2:
                                startDelay = LayoutTransition.this.mChangingAppearingDelay + LayoutTransition.this.staggerDelay;
                                layoutTransition = LayoutTransition.this;
                                layoutTransition.staggerDelay = layoutTransition.staggerDelay + LayoutTransition.this.mChangingAppearingStagger;
                                if (LayoutTransition.this.mChangingAppearingInterpolator != LayoutTransition.sChangingAppearingInterpolator) {
                                    anim.setInterpolator(LayoutTransition.this.mChangingAppearingInterpolator);
                                    break;
                                }
                                break;
                            case 3:
                                startDelay = LayoutTransition.this.mChangingDisappearingDelay + LayoutTransition.this.staggerDelay;
                                layoutTransition = LayoutTransition.this;
                                layoutTransition.staggerDelay = layoutTransition.staggerDelay + LayoutTransition.this.mChangingDisappearingStagger;
                                if (LayoutTransition.this.mChangingDisappearingInterpolator != LayoutTransition.sChangingDisappearingInterpolator) {
                                    anim.setInterpolator(LayoutTransition.this.mChangingDisappearingInterpolator);
                                    break;
                                }
                                break;
                            case 4:
                                startDelay = LayoutTransition.this.mChangingDelay + LayoutTransition.this.staggerDelay;
                                layoutTransition = LayoutTransition.this;
                                layoutTransition.staggerDelay = layoutTransition.staggerDelay + LayoutTransition.this.mChangingStagger;
                                if (LayoutTransition.this.mChangingInterpolator != LayoutTransition.sChangingInterpolator) {
                                    anim.setInterpolator(LayoutTransition.this.mChangingInterpolator);
                                    break;
                                }
                                break;
                        }
                        anim.setStartDelay(startDelay);
                        anim.setDuration(j);
                        Animator prevAnimation = (Animator) LayoutTransition.this.currentChangingAnimations.get(view2);
                        if (prevAnimation != null) {
                            prevAnimation.cancel();
                        }
                        if (((Animator) LayoutTransition.this.pendingAnimations.get(view2)) != null) {
                            LayoutTransition.this.pendingAnimations.remove(view2);
                        }
                        LayoutTransition.this.currentChangingAnimations.put(view2, anim);
                        viewGroup.requestTransitionStart(LayoutTransition.this);
                        view2.removeOnLayoutChangeListener(this);
                        LayoutTransition.this.layoutChangeListenerMap.remove(view2);
                    }
                };
                final ViewGroup viewGroup2 = parent;
                view2 = child;
                final int i2 = changeReason;
                final OnLayoutChangeListener onLayoutChangeListener = listener;
                anim.addListener(new AnimatorListenerAdapter() {
                    public void onAnimationStart(Animator animator) {
                        if (LayoutTransition.this.hasListeners()) {
                            for (TransitionListener listener : (ArrayList) LayoutTransition.this.mListeners.clone()) {
                                LayoutTransition layoutTransition = LayoutTransition.this;
                                ViewGroup viewGroup = viewGroup2;
                                View view = view2;
                                int i = i2 == 2 ? 0 : i2 == 3 ? 1 : 4;
                                listener.startTransition(layoutTransition, viewGroup, view, i);
                            }
                        }
                    }

                    public void onAnimationCancel(Animator animator) {
                        view2.removeOnLayoutChangeListener(onLayoutChangeListener);
                        LayoutTransition.this.layoutChangeListenerMap.remove(view2);
                    }

                    public void onAnimationEnd(Animator animator) {
                        LayoutTransition.this.currentChangingAnimations.remove(view2);
                        if (LayoutTransition.this.hasListeners()) {
                            for (TransitionListener listener : (ArrayList) LayoutTransition.this.mListeners.clone()) {
                                LayoutTransition layoutTransition = LayoutTransition.this;
                                ViewGroup viewGroup = viewGroup2;
                                View view = view2;
                                int i = i2 == 2 ? 0 : i2 == 3 ? 1 : 4;
                                listener.endTransition(layoutTransition, viewGroup, view, i);
                            }
                        }
                    }
                });
                child.addOnLayoutChangeListener(listener);
                this.layoutChangeListenerMap.put(child, listener);
            }
        }
    }

    public void startChangingAnimations() {
        for (Animator anim : ((LinkedHashMap) this.currentChangingAnimations.clone()).values()) {
            if (anim instanceof ObjectAnimator) {
                ((ObjectAnimator) anim).setCurrentPlayTime(0);
            }
            anim.start();
        }
    }

    public void endChangingAnimations() {
        for (Animator anim : ((LinkedHashMap) this.currentChangingAnimations.clone()).values()) {
            anim.start();
            anim.end();
        }
        this.currentChangingAnimations.clear();
    }

    public boolean isChangingLayout() {
        return this.currentChangingAnimations.size() > 0;
    }

    public boolean isRunning() {
        if (this.currentChangingAnimations.size() > 0 || this.currentAppearingAnimations.size() > 0 || this.currentDisappearingAnimations.size() > 0) {
            return true;
        }
        return false;
    }

    public void cancel() {
        if (this.currentChangingAnimations.size() > 0) {
            for (Animator anim : ((LinkedHashMap) this.currentChangingAnimations.clone()).values()) {
                anim.cancel();
            }
            this.currentChangingAnimations.clear();
        }
        if (this.currentAppearingAnimations.size() > 0) {
            for (Animator anim2 : ((LinkedHashMap) this.currentAppearingAnimations.clone()).values()) {
                anim2.end();
            }
            this.currentAppearingAnimations.clear();
        }
        if (this.currentDisappearingAnimations.size() > 0) {
            for (Animator anim22 : ((LinkedHashMap) this.currentDisappearingAnimations.clone()).values()) {
                anim22.end();
            }
            this.currentDisappearingAnimations.clear();
        }
    }

    public void cancel(int transitionType) {
        switch (transitionType) {
            case 0:
            case 1:
            case 4:
                if (this.currentChangingAnimations.size() > 0) {
                    for (Animator anim : ((LinkedHashMap) this.currentChangingAnimations.clone()).values()) {
                        anim.cancel();
                    }
                    this.currentChangingAnimations.clear();
                    return;
                }
                return;
            case 2:
                if (this.currentAppearingAnimations.size() > 0) {
                    for (Animator anim2 : ((LinkedHashMap) this.currentAppearingAnimations.clone()).values()) {
                        anim2.end();
                    }
                    this.currentAppearingAnimations.clear();
                    return;
                }
                return;
            case 3:
                if (this.currentDisappearingAnimations.size() > 0) {
                    for (Animator anim22 : ((LinkedHashMap) this.currentDisappearingAnimations.clone()).values()) {
                        anim22.end();
                    }
                    this.currentDisappearingAnimations.clear();
                    return;
                }
                return;
            default:
                return;
        }
    }

    private void runAppearingTransition(final ViewGroup parent, final View child) {
        Animator currentAnimation = (Animator) this.currentDisappearingAnimations.get(child);
        if (currentAnimation != null) {
            currentAnimation.cancel();
        }
        if (this.mAppearingAnim == null) {
            if (hasListeners()) {
                for (TransitionListener listener : (ArrayList) this.mListeners.clone()) {
                    listener.endTransition(this, parent, child, 2);
                }
            }
            return;
        }
        Animator anim = this.mAppearingAnim.clone();
        anim.setTarget(child);
        anim.setStartDelay(this.mAppearingDelay);
        anim.setDuration(this.mAppearingDuration);
        if (this.mAppearingInterpolator != sAppearingInterpolator) {
            anim.setInterpolator(this.mAppearingInterpolator);
        }
        if (anim instanceof ObjectAnimator) {
            ((ObjectAnimator) anim).setCurrentPlayTime(0);
        }
        anim.addListener(new AnimatorListenerAdapter() {
            public void onAnimationEnd(Animator anim) {
                LayoutTransition.this.currentAppearingAnimations.remove(child);
                if (LayoutTransition.this.hasListeners()) {
                    for (TransitionListener listener : (ArrayList) LayoutTransition.this.mListeners.clone()) {
                        listener.endTransition(LayoutTransition.this, parent, child, 2);
                    }
                }
            }
        });
        this.currentAppearingAnimations.put(child, anim);
        anim.start();
    }

    private void runDisappearingTransition(final ViewGroup parent, final View child) {
        Animator currentAnimation = (Animator) this.currentAppearingAnimations.get(child);
        if (currentAnimation != null) {
            currentAnimation.cancel();
        }
        if (this.mDisappearingAnim == null) {
            if (hasListeners()) {
                for (TransitionListener listener : (ArrayList) this.mListeners.clone()) {
                    listener.endTransition(this, parent, child, 3);
                }
            }
            return;
        }
        Animator anim = this.mDisappearingAnim.clone();
        anim.setStartDelay(this.mDisappearingDelay);
        anim.setDuration(this.mDisappearingDuration);
        if (this.mDisappearingInterpolator != sDisappearingInterpolator) {
            anim.setInterpolator(this.mDisappearingInterpolator);
        }
        anim.setTarget(child);
        final float preAnimAlpha = child.getAlpha();
        anim.addListener(new AnimatorListenerAdapter() {
            public void onAnimationEnd(Animator anim) {
                LayoutTransition.this.currentDisappearingAnimations.remove(child);
                child.setAlpha(preAnimAlpha);
                if (LayoutTransition.this.hasListeners()) {
                    for (TransitionListener listener : (ArrayList) LayoutTransition.this.mListeners.clone()) {
                        listener.endTransition(LayoutTransition.this, parent, child, 3);
                    }
                }
            }
        });
        if (anim instanceof ObjectAnimator) {
            ((ObjectAnimator) anim).setCurrentPlayTime(0);
        }
        this.currentDisappearingAnimations.put(child, anim);
        anim.start();
    }

    private void addChild(ViewGroup parent, View child, boolean changesLayout) {
        if (parent.getWindowVisibility() == 0) {
            if ((this.mTransitionTypes & 1) == 1) {
                cancel(3);
            }
            if (changesLayout && (this.mTransitionTypes & 4) == 4) {
                cancel(0);
                cancel(4);
            }
            if (hasListeners() && (this.mTransitionTypes & 1) == 1) {
                for (TransitionListener listener : (ArrayList) this.mListeners.clone()) {
                    listener.startTransition(this, parent, child, 2);
                }
            }
            if (changesLayout && (this.mTransitionTypes & 4) == 4) {
                runChangeTransition(parent, child, 2);
            }
            if ((this.mTransitionTypes & 1) == 1) {
                runAppearingTransition(parent, child);
            }
        }
    }

    private boolean hasListeners() {
        return this.mListeners != null && this.mListeners.size() > 0;
    }

    public void layoutChange(ViewGroup parent) {
        if (parent.getWindowVisibility() == 0 && (this.mTransitionTypes & 16) == 16 && (isRunning() ^ 1) != 0) {
            runChangeTransition(parent, null, 4);
        }
    }

    public void addChild(ViewGroup parent, View child) {
        addChild(parent, child, true);
    }

    @Deprecated
    public void showChild(ViewGroup parent, View child) {
        addChild(parent, child, true);
    }

    public void showChild(ViewGroup parent, View child, int oldVisibility) {
        addChild(parent, child, oldVisibility == 8);
    }

    private void removeChild(ViewGroup parent, View child, boolean changesLayout) {
        if (parent.getWindowVisibility() == 0) {
            if ((this.mTransitionTypes & 2) == 2) {
                cancel(2);
            }
            if (changesLayout && (this.mTransitionTypes & 8) == 8) {
                cancel(1);
                cancel(4);
            }
            if (hasListeners() && (this.mTransitionTypes & 2) == 2) {
                for (TransitionListener listener : (ArrayList) this.mListeners.clone()) {
                    listener.startTransition(this, parent, child, 3);
                }
            }
            if (changesLayout && (this.mTransitionTypes & 8) == 8) {
                runChangeTransition(parent, child, 3);
            }
            if ((this.mTransitionTypes & 2) == 2) {
                runDisappearingTransition(parent, child);
            }
        }
    }

    public void removeChild(ViewGroup parent, View child) {
        removeChild(parent, child, true);
    }

    @Deprecated
    public void hideChild(ViewGroup parent, View child) {
        removeChild(parent, child, true);
    }

    public void hideChild(ViewGroup parent, View child, int newVisibility) {
        removeChild(parent, child, newVisibility == 8);
    }

    public void addTransitionListener(TransitionListener listener) {
        if (this.mListeners == null) {
            this.mListeners = new ArrayList();
        }
        this.mListeners.add(listener);
    }

    public void removeTransitionListener(TransitionListener listener) {
        if (this.mListeners != null) {
            this.mListeners.remove(listener);
        }
    }

    public List<TransitionListener> getTransitionListeners() {
        return this.mListeners;
    }
}
