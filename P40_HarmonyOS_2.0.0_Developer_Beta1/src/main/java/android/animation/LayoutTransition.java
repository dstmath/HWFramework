package android.animation;

import android.annotation.UnsupportedAppUsage;
import android.rms.AppAssociate;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.ViewTreeObserver;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
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
    private static TimeInterpolator sAppearingInterpolator;
    private static TimeInterpolator sChangingAppearingInterpolator;
    private static TimeInterpolator sChangingDisappearingInterpolator;
    private static TimeInterpolator sChangingInterpolator;
    private static TimeInterpolator sDisappearingInterpolator;
    private final LinkedHashMap<View, Animator> currentAppearingAnimations;
    private final LinkedHashMap<View, Animator> currentChangingAnimations;
    private final LinkedHashMap<View, Animator> currentDisappearingAnimations;
    private final HashMap<View, View.OnLayoutChangeListener> layoutChangeListenerMap;
    private boolean mAnimateParentHierarchy;
    private Animator mAppearingAnim = null;
    private long mAppearingDelay;
    private long mAppearingDuration;
    private TimeInterpolator mAppearingInterpolator;
    private Animator mChangingAnim = null;
    private Animator mChangingAppearingAnim = null;
    private long mChangingAppearingDelay;
    private long mChangingAppearingDuration;
    private TimeInterpolator mChangingAppearingInterpolator;
    private long mChangingAppearingStagger;
    private long mChangingDelay;
    private Animator mChangingDisappearingAnim = null;
    private long mChangingDisappearingDelay;
    private long mChangingDisappearingDuration;
    private TimeInterpolator mChangingDisappearingInterpolator;
    private long mChangingDisappearingStagger;
    private long mChangingDuration;
    private TimeInterpolator mChangingInterpolator;
    private long mChangingStagger;
    private Animator mDisappearingAnim = null;
    private long mDisappearingDelay;
    private long mDisappearingDuration;
    private TimeInterpolator mDisappearingInterpolator;
    private ArrayList<TransitionListener> mListeners;
    private int mTransitionTypes;
    private final HashMap<View, Animator> pendingAnimations;
    private long staggerDelay;

    public interface TransitionListener {
        void endTransition(LayoutTransition layoutTransition, ViewGroup viewGroup, View view, int i);

        void startTransition(LayoutTransition layoutTransition, ViewGroup viewGroup, View view, int i);
    }

    static /* synthetic */ long access$214(LayoutTransition x0, long x1) {
        long j = x0.staggerDelay + x1;
        x0.staggerDelay = j;
        return j;
    }

    static {
        TimeInterpolator timeInterpolator = ACCEL_DECEL_INTERPOLATOR;
        sAppearingInterpolator = timeInterpolator;
        sDisappearingInterpolator = timeInterpolator;
        TimeInterpolator timeInterpolator2 = DECEL_INTERPOLATOR;
        sChangingAppearingInterpolator = timeInterpolator2;
        sChangingDisappearingInterpolator = timeInterpolator2;
        sChangingInterpolator = timeInterpolator2;
    }

    public LayoutTransition() {
        long j = DEFAULT_DURATION;
        this.mChangingAppearingDuration = j;
        this.mChangingDisappearingDuration = j;
        this.mChangingDuration = j;
        this.mAppearingDuration = j;
        this.mDisappearingDuration = j;
        this.mAppearingDelay = j;
        this.mDisappearingDelay = 0;
        this.mChangingAppearingDelay = 0;
        this.mChangingDisappearingDelay = j;
        this.mChangingDelay = 0;
        this.mChangingAppearingStagger = 0;
        this.mChangingDisappearingStagger = 0;
        this.mChangingStagger = 0;
        this.mAppearingInterpolator = sAppearingInterpolator;
        this.mDisappearingInterpolator = sDisappearingInterpolator;
        this.mChangingAppearingInterpolator = sChangingAppearingInterpolator;
        this.mChangingDisappearingInterpolator = sChangingDisappearingInterpolator;
        this.mChangingInterpolator = sChangingInterpolator;
        this.pendingAnimations = new HashMap<>();
        this.currentChangingAnimations = new LinkedHashMap<>();
        this.currentAppearingAnimations = new LinkedHashMap<>();
        this.currentDisappearingAnimations = new LinkedHashMap<>();
        this.layoutChangeListenerMap = new HashMap<>();
        this.mTransitionTypes = 15;
        this.mAnimateParentHierarchy = true;
        if (defaultChangeIn == null) {
            defaultChangeIn = ObjectAnimator.ofPropertyValuesHolder(null, PropertyValuesHolder.ofInt("left", 0, 1), PropertyValuesHolder.ofInt("top", 0, 1), PropertyValuesHolder.ofInt("right", 0, 1), PropertyValuesHolder.ofInt("bottom", 0, 1), PropertyValuesHolder.ofInt("scrollX", 0, 1), PropertyValuesHolder.ofInt("scrollY", 0, 1));
            defaultChangeIn.setDuration(DEFAULT_DURATION);
            defaultChangeIn.setStartDelay(this.mChangingAppearingDelay);
            defaultChangeIn.setInterpolator(this.mChangingAppearingInterpolator);
            defaultChangeOut = defaultChangeIn.clone();
            defaultChangeOut.setStartDelay(this.mChangingDisappearingDelay);
            defaultChangeOut.setInterpolator(this.mChangingDisappearingInterpolator);
            defaultChange = defaultChangeIn.clone();
            defaultChange.setStartDelay(this.mChangingDelay);
            defaultChange.setInterpolator(this.mChangingInterpolator);
            defaultFadeIn = ObjectAnimator.ofFloat((Object) null, AppAssociate.ASSOC_WINDOW_ALPHA, 0.0f, 1.0f);
            defaultFadeIn.setDuration(DEFAULT_DURATION);
            defaultFadeIn.setStartDelay(this.mAppearingDelay);
            defaultFadeIn.setInterpolator(this.mAppearingInterpolator);
            defaultFadeOut = ObjectAnimator.ofFloat((Object) null, AppAssociate.ASSOC_WINDOW_ALPHA, 1.0f, 0.0f);
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
        if (transitionType == 0) {
            this.mTransitionTypes = 4 | this.mTransitionTypes;
        } else if (transitionType == 1) {
            this.mTransitionTypes |= 8;
        } else if (transitionType == 2) {
            this.mTransitionTypes |= 1;
        } else if (transitionType == 3) {
            this.mTransitionTypes |= 2;
        } else if (transitionType == 4) {
            this.mTransitionTypes |= 16;
        }
    }

    public void disableTransitionType(int transitionType) {
        if (transitionType == 0) {
            this.mTransitionTypes &= -5;
        } else if (transitionType == 1) {
            this.mTransitionTypes &= -9;
        } else if (transitionType == 2) {
            this.mTransitionTypes &= -2;
        } else if (transitionType == 3) {
            this.mTransitionTypes &= -3;
        } else if (transitionType == 4) {
            this.mTransitionTypes &= -17;
        }
    }

    public boolean isTransitionTypeEnabled(int transitionType) {
        return transitionType != 0 ? transitionType != 1 ? transitionType != 2 ? transitionType != 3 ? transitionType == 4 && (this.mTransitionTypes & 16) == 16 : (this.mTransitionTypes & 2) == 2 : (this.mTransitionTypes & 1) == 1 : (this.mTransitionTypes & 8) == 8 : (this.mTransitionTypes & 4) == 4;
    }

    public void setStartDelay(int transitionType, long delay) {
        if (transitionType == 0) {
            this.mChangingAppearingDelay = delay;
        } else if (transitionType == 1) {
            this.mChangingDisappearingDelay = delay;
        } else if (transitionType == 2) {
            this.mAppearingDelay = delay;
        } else if (transitionType == 3) {
            this.mDisappearingDelay = delay;
        } else if (transitionType == 4) {
            this.mChangingDelay = delay;
        }
    }

    public long getStartDelay(int transitionType) {
        if (transitionType == 0) {
            return this.mChangingAppearingDelay;
        }
        if (transitionType == 1) {
            return this.mChangingDisappearingDelay;
        }
        if (transitionType == 2) {
            return this.mAppearingDelay;
        }
        if (transitionType == 3) {
            return this.mDisappearingDelay;
        }
        if (transitionType != 4) {
            return 0;
        }
        return this.mChangingDelay;
    }

    public void setDuration(int transitionType, long duration) {
        if (transitionType == 0) {
            this.mChangingAppearingDuration = duration;
        } else if (transitionType == 1) {
            this.mChangingDisappearingDuration = duration;
        } else if (transitionType == 2) {
            this.mAppearingDuration = duration;
        } else if (transitionType == 3) {
            this.mDisappearingDuration = duration;
        } else if (transitionType == 4) {
            this.mChangingDuration = duration;
        }
    }

    public long getDuration(int transitionType) {
        if (transitionType == 0) {
            return this.mChangingAppearingDuration;
        }
        if (transitionType == 1) {
            return this.mChangingDisappearingDuration;
        }
        if (transitionType == 2) {
            return this.mAppearingDuration;
        }
        if (transitionType == 3) {
            return this.mDisappearingDuration;
        }
        if (transitionType != 4) {
            return 0;
        }
        return this.mChangingDuration;
    }

    public void setStagger(int transitionType, long duration) {
        if (transitionType == 0) {
            this.mChangingAppearingStagger = duration;
        } else if (transitionType == 1) {
            this.mChangingDisappearingStagger = duration;
        } else if (transitionType == 4) {
            this.mChangingStagger = duration;
        }
    }

    public long getStagger(int transitionType) {
        if (transitionType == 0) {
            return this.mChangingAppearingStagger;
        }
        if (transitionType == 1) {
            return this.mChangingDisappearingStagger;
        }
        if (transitionType != 4) {
            return 0;
        }
        return this.mChangingStagger;
    }

    public void setInterpolator(int transitionType, TimeInterpolator interpolator) {
        if (transitionType == 0) {
            this.mChangingAppearingInterpolator = interpolator;
        } else if (transitionType == 1) {
            this.mChangingDisappearingInterpolator = interpolator;
        } else if (transitionType == 2) {
            this.mAppearingInterpolator = interpolator;
        } else if (transitionType == 3) {
            this.mDisappearingInterpolator = interpolator;
        } else if (transitionType == 4) {
            this.mChangingInterpolator = interpolator;
        }
    }

    public TimeInterpolator getInterpolator(int transitionType) {
        if (transitionType == 0) {
            return this.mChangingAppearingInterpolator;
        }
        if (transitionType == 1) {
            return this.mChangingDisappearingInterpolator;
        }
        if (transitionType == 2) {
            return this.mAppearingInterpolator;
        }
        if (transitionType == 3) {
            return this.mDisappearingInterpolator;
        }
        if (transitionType != 4) {
            return null;
        }
        return this.mChangingInterpolator;
    }

    public void setAnimator(int transitionType, Animator animator) {
        if (transitionType == 0) {
            this.mChangingAppearingAnim = animator;
        } else if (transitionType == 1) {
            this.mChangingDisappearingAnim = animator;
        } else if (transitionType == 2) {
            this.mAppearingAnim = animator;
        } else if (transitionType == 3) {
            this.mDisappearingAnim = animator;
        } else if (transitionType == 4) {
            this.mChangingAnim = animator;
        }
    }

    public Animator getAnimator(int transitionType) {
        if (transitionType == 0) {
            return this.mChangingAppearingAnim;
        }
        if (transitionType == 1) {
            return this.mChangingDisappearingAnim;
        }
        if (transitionType == 2) {
            return this.mAppearingAnim;
        }
        if (transitionType == 3) {
            return this.mDisappearingAnim;
        }
        if (transitionType != 4) {
            return null;
        }
        return this.mChangingAnim;
    }

    private void runChangeTransition(ViewGroup parent, View newView, int changeReason) {
        long duration;
        Animator parentAnimator;
        Animator baseAnimator;
        int i;
        if (changeReason == 2) {
            Animator baseAnimator2 = this.mChangingAppearingAnim;
            long duration2 = this.mChangingAppearingDuration;
            baseAnimator = baseAnimator2;
            parentAnimator = defaultChangeIn;
            duration = duration2;
        } else if (changeReason == 3) {
            Animator baseAnimator3 = this.mChangingDisappearingAnim;
            long duration3 = this.mChangingDisappearingDuration;
            baseAnimator = baseAnimator3;
            parentAnimator = defaultChangeOut;
            duration = duration3;
        } else if (changeReason != 4) {
            baseAnimator = null;
            parentAnimator = null;
            duration = 0;
        } else {
            Animator baseAnimator4 = this.mChangingAnim;
            long duration4 = this.mChangingDuration;
            baseAnimator = baseAnimator4;
            parentAnimator = defaultChange;
            duration = duration4;
        }
        if (baseAnimator != null) {
            this.staggerDelay = 0;
            ViewTreeObserver observer = parent.getViewTreeObserver();
            if (observer.isAlive()) {
                int numChildren = parent.getChildCount();
                int i2 = 0;
                while (i2 < numChildren) {
                    View child = parent.getChildAt(i2);
                    if (child != newView) {
                        i = i2;
                        setupChangeAnimation(parent, changeReason, baseAnimator, duration, child);
                    } else {
                        i = i2;
                    }
                    i2 = i + 1;
                }
                if (this.mAnimateParentHierarchy) {
                    ViewGroup tempParent = parent;
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

    private void setupChangeAnimation(final ViewGroup parent, final int changeReason, Animator baseAnimator, final long duration, final View child) {
        if (this.layoutChangeListenerMap.get(child) == null) {
            if (child.getWidth() != 0 || child.getHeight() != 0) {
                final Animator anim = baseAnimator.clone();
                anim.setTarget(child);
                anim.setupStartValues();
                Animator currentAnimation = this.pendingAnimations.get(child);
                if (currentAnimation != null) {
                    currentAnimation.cancel();
                    this.pendingAnimations.remove(child);
                }
                this.pendingAnimations.put(child, anim);
                ValueAnimator pendingAnimRemover = ValueAnimator.ofFloat(0.0f, 1.0f).setDuration(duration + 100);
                pendingAnimRemover.addListener(new AnimatorListenerAdapter() {
                    /* class android.animation.LayoutTransition.AnonymousClass1 */

                    @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
                    public void onAnimationEnd(Animator animation) {
                        LayoutTransition.this.pendingAnimations.remove(child);
                    }
                });
                pendingAnimRemover.start();
                final View.OnLayoutChangeListener listener = new View.OnLayoutChangeListener() {
                    /* class android.animation.LayoutTransition.AnonymousClass2 */

                    @Override // android.view.View.OnLayoutChangeListener
                    public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                        anim.setupEndValues();
                        Animator animator = anim;
                        if (animator instanceof ValueAnimator) {
                            boolean valuesDiffer = false;
                            PropertyValuesHolder[] oldValues = ((ValueAnimator) animator).getValues();
                            for (PropertyValuesHolder pvh : oldValues) {
                                if (pvh.mKeyframes instanceof KeyframeSet) {
                                    KeyframeSet keyframeSet = (KeyframeSet) pvh.mKeyframes;
                                    if (keyframeSet.mFirstKeyframe == null || keyframeSet.mLastKeyframe == null || !keyframeSet.mFirstKeyframe.getValue().equals(keyframeSet.mLastKeyframe.getValue())) {
                                        valuesDiffer = true;
                                    }
                                } else if (!pvh.mKeyframes.getValue(0.0f).equals(pvh.mKeyframes.getValue(1.0f))) {
                                    valuesDiffer = true;
                                }
                            }
                            if (!valuesDiffer) {
                                return;
                            }
                        }
                        long startDelay = 0;
                        int i = changeReason;
                        if (i == 2) {
                            startDelay = LayoutTransition.this.mChangingAppearingDelay + LayoutTransition.this.staggerDelay;
                            LayoutTransition layoutTransition = LayoutTransition.this;
                            LayoutTransition.access$214(layoutTransition, layoutTransition.mChangingAppearingStagger);
                            if (LayoutTransition.this.mChangingAppearingInterpolator != LayoutTransition.sChangingAppearingInterpolator) {
                                anim.setInterpolator(LayoutTransition.this.mChangingAppearingInterpolator);
                            }
                        } else if (i == 3) {
                            startDelay = LayoutTransition.this.mChangingDisappearingDelay + LayoutTransition.this.staggerDelay;
                            LayoutTransition layoutTransition2 = LayoutTransition.this;
                            LayoutTransition.access$214(layoutTransition2, layoutTransition2.mChangingDisappearingStagger);
                            if (LayoutTransition.this.mChangingDisappearingInterpolator != LayoutTransition.sChangingDisappearingInterpolator) {
                                anim.setInterpolator(LayoutTransition.this.mChangingDisappearingInterpolator);
                            }
                        } else if (i == 4) {
                            startDelay = LayoutTransition.this.mChangingDelay + LayoutTransition.this.staggerDelay;
                            LayoutTransition layoutTransition3 = LayoutTransition.this;
                            LayoutTransition.access$214(layoutTransition3, layoutTransition3.mChangingStagger);
                            if (LayoutTransition.this.mChangingInterpolator != LayoutTransition.sChangingInterpolator) {
                                anim.setInterpolator(LayoutTransition.this.mChangingInterpolator);
                            }
                        }
                        anim.setStartDelay(startDelay);
                        anim.setDuration(duration);
                        Animator prevAnimation = (Animator) LayoutTransition.this.currentChangingAnimations.get(child);
                        if (prevAnimation != null) {
                            prevAnimation.cancel();
                        }
                        if (((Animator) LayoutTransition.this.pendingAnimations.get(child)) != null) {
                            LayoutTransition.this.pendingAnimations.remove(child);
                        }
                        LayoutTransition.this.currentChangingAnimations.put(child, anim);
                        parent.requestTransitionStart(LayoutTransition.this);
                        child.removeOnLayoutChangeListener(this);
                        LayoutTransition.this.layoutChangeListenerMap.remove(child);
                    }
                };
                anim.addListener(new AnimatorListenerAdapter() {
                    /* class android.animation.LayoutTransition.AnonymousClass3 */

                    @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
                    public void onAnimationStart(Animator animator) {
                        int i;
                        if (LayoutTransition.this.hasListeners()) {
                            Iterator<TransitionListener> it = ((ArrayList) LayoutTransition.this.mListeners.clone()).iterator();
                            while (it.hasNext()) {
                                TransitionListener listener = it.next();
                                LayoutTransition layoutTransition = LayoutTransition.this;
                                ViewGroup viewGroup = parent;
                                View view = child;
                                int i2 = changeReason;
                                if (i2 == 2) {
                                    i = 0;
                                } else {
                                    i = i2 == 3 ? 1 : 4;
                                }
                                listener.startTransition(layoutTransition, viewGroup, view, i);
                            }
                        }
                    }

                    @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
                    public void onAnimationCancel(Animator animator) {
                        child.removeOnLayoutChangeListener(listener);
                        LayoutTransition.this.layoutChangeListenerMap.remove(child);
                    }

                    @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
                    public void onAnimationEnd(Animator animator) {
                        int i;
                        LayoutTransition.this.currentChangingAnimations.remove(child);
                        if (LayoutTransition.this.hasListeners()) {
                            Iterator<TransitionListener> it = ((ArrayList) LayoutTransition.this.mListeners.clone()).iterator();
                            while (it.hasNext()) {
                                TransitionListener listener = it.next();
                                LayoutTransition layoutTransition = LayoutTransition.this;
                                ViewGroup viewGroup = parent;
                                View view = child;
                                int i2 = changeReason;
                                if (i2 == 2) {
                                    i = 0;
                                } else {
                                    i = i2 == 3 ? 1 : 4;
                                }
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
        return this.currentChangingAnimations.size() > 0 || this.currentAppearingAnimations.size() > 0 || this.currentDisappearingAnimations.size() > 0;
    }

    @UnsupportedAppUsage(maxTargetSdk = 28)
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
            for (Animator anim3 : ((LinkedHashMap) this.currentDisappearingAnimations.clone()).values()) {
                anim3.end();
            }
            this.currentDisappearingAnimations.clear();
        }
    }

    @UnsupportedAppUsage(maxTargetSdk = 28)
    public void cancel(int transitionType) {
        if (!(transitionType == 0 || transitionType == 1)) {
            if (transitionType != 2) {
                if (transitionType != 3) {
                    if (transitionType != 4) {
                        return;
                    }
                } else if (this.currentDisappearingAnimations.size() > 0) {
                    for (Animator anim : ((LinkedHashMap) this.currentDisappearingAnimations.clone()).values()) {
                        anim.end();
                    }
                    this.currentDisappearingAnimations.clear();
                    return;
                } else {
                    return;
                }
            } else if (this.currentAppearingAnimations.size() > 0) {
                for (Animator anim2 : ((LinkedHashMap) this.currentAppearingAnimations.clone()).values()) {
                    anim2.end();
                }
                this.currentAppearingAnimations.clear();
                return;
            } else {
                return;
            }
        }
        if (this.currentChangingAnimations.size() > 0) {
            for (Animator anim3 : ((LinkedHashMap) this.currentChangingAnimations.clone()).values()) {
                anim3.cancel();
            }
            this.currentChangingAnimations.clear();
        }
    }

    private void runAppearingTransition(final ViewGroup parent, final View child) {
        Animator currentAnimation = this.currentDisappearingAnimations.get(child);
        if (currentAnimation != null) {
            currentAnimation.cancel();
        }
        Animator animator = this.mAppearingAnim;
        if (animator != null) {
            Animator anim = animator.clone();
            anim.setTarget(child);
            anim.setStartDelay(this.mAppearingDelay);
            anim.setDuration(this.mAppearingDuration);
            TimeInterpolator timeInterpolator = this.mAppearingInterpolator;
            if (timeInterpolator != sAppearingInterpolator) {
                anim.setInterpolator(timeInterpolator);
            }
            if (anim instanceof ObjectAnimator) {
                ((ObjectAnimator) anim).setCurrentPlayTime(0);
            }
            anim.addListener(new AnimatorListenerAdapter() {
                /* class android.animation.LayoutTransition.AnonymousClass4 */

                @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
                public void onAnimationEnd(Animator anim) {
                    LayoutTransition.this.currentAppearingAnimations.remove(child);
                    if (LayoutTransition.this.hasListeners()) {
                        Iterator<TransitionListener> it = ((ArrayList) LayoutTransition.this.mListeners.clone()).iterator();
                        while (it.hasNext()) {
                            it.next().endTransition(LayoutTransition.this, parent, child, 2);
                        }
                    }
                }
            });
            this.currentAppearingAnimations.put(child, anim);
            anim.start();
        } else if (hasListeners()) {
            Iterator<TransitionListener> it = ((ArrayList) this.mListeners.clone()).iterator();
            while (it.hasNext()) {
                it.next().endTransition(this, parent, child, 2);
            }
        }
    }

    private void runDisappearingTransition(final ViewGroup parent, final View child) {
        Animator currentAnimation = this.currentAppearingAnimations.get(child);
        if (currentAnimation != null) {
            currentAnimation.cancel();
        }
        Animator animator = this.mDisappearingAnim;
        if (animator != null) {
            Animator anim = animator.clone();
            anim.setStartDelay(this.mDisappearingDelay);
            anim.setDuration(this.mDisappearingDuration);
            TimeInterpolator timeInterpolator = this.mDisappearingInterpolator;
            if (timeInterpolator != sDisappearingInterpolator) {
                anim.setInterpolator(timeInterpolator);
            }
            anim.setTarget(child);
            final float preAnimAlpha = child.getAlpha();
            anim.addListener(new AnimatorListenerAdapter() {
                /* class android.animation.LayoutTransition.AnonymousClass5 */

                @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
                public void onAnimationEnd(Animator anim) {
                    LayoutTransition.this.currentDisappearingAnimations.remove(child);
                    child.setAlpha(preAnimAlpha);
                    if (LayoutTransition.this.hasListeners()) {
                        Iterator<TransitionListener> it = ((ArrayList) LayoutTransition.this.mListeners.clone()).iterator();
                        while (it.hasNext()) {
                            it.next().endTransition(LayoutTransition.this, parent, child, 3);
                        }
                    }
                }
            });
            if (anim instanceof ObjectAnimator) {
                ((ObjectAnimator) anim).setCurrentPlayTime(0);
            }
            this.currentDisappearingAnimations.put(child, anim);
            anim.start();
        } else if (hasListeners()) {
            Iterator<TransitionListener> it = ((ArrayList) this.mListeners.clone()).iterator();
            while (it.hasNext()) {
                it.next().endTransition(this, parent, child, 3);
            }
        }
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
                Iterator<TransitionListener> it = ((ArrayList) this.mListeners.clone()).iterator();
                while (it.hasNext()) {
                    it.next().startTransition(this, parent, child, 2);
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

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean hasListeners() {
        ArrayList<TransitionListener> arrayList = this.mListeners;
        return arrayList != null && arrayList.size() > 0;
    }

    public void layoutChange(ViewGroup parent) {
        if (parent.getWindowVisibility() == 0 && (this.mTransitionTypes & 16) == 16 && !isRunning()) {
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
                Iterator<TransitionListener> it = ((ArrayList) this.mListeners.clone()).iterator();
                while (it.hasNext()) {
                    it.next().startTransition(this, parent, child, 3);
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
            this.mListeners = new ArrayList<>();
        }
        this.mListeners.add(listener);
    }

    public void removeTransitionListener(TransitionListener listener) {
        ArrayList<TransitionListener> arrayList = this.mListeners;
        if (arrayList != null) {
            arrayList.remove(listener);
        }
    }

    public List<TransitionListener> getTransitionListeners() {
        return this.mListeners;
    }

    /* access modifiers changed from: private */
    public static final class CleanupCallback implements ViewTreeObserver.OnPreDrawListener, View.OnAttachStateChangeListener {
        final Map<View, View.OnLayoutChangeListener> layoutChangeListenerMap;
        final ViewGroup parent;

        CleanupCallback(Map<View, View.OnLayoutChangeListener> listenerMap, ViewGroup parent2) {
            this.layoutChangeListenerMap = listenerMap;
            this.parent = parent2;
        }

        private void cleanup() {
            this.parent.getViewTreeObserver().removeOnPreDrawListener(this);
            this.parent.removeOnAttachStateChangeListener(this);
            if (this.layoutChangeListenerMap.size() > 0) {
                for (View view : this.layoutChangeListenerMap.keySet()) {
                    view.removeOnLayoutChangeListener(this.layoutChangeListenerMap.get(view));
                }
                this.layoutChangeListenerMap.clear();
            }
        }

        @Override // android.view.View.OnAttachStateChangeListener
        public void onViewAttachedToWindow(View v) {
        }

        @Override // android.view.View.OnAttachStateChangeListener
        public void onViewDetachedFromWindow(View v) {
            cleanup();
        }

        @Override // android.view.ViewTreeObserver.OnPreDrawListener
        public boolean onPreDraw() {
            cleanup();
            return true;
        }
    }
}
