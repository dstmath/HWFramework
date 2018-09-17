package android.animation;

import android.speech.tts.TextToSpeech.Engine;
import android.view.View;
import android.view.View.OnAttachStateChangeListener;
import android.view.View.OnLayoutChangeListener;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.ViewTreeObserver;
import android.view.ViewTreeObserver.OnPreDrawListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class LayoutTransition {
    private static TimeInterpolator ACCEL_DECEL_INTERPOLATOR = null;
    public static final int APPEARING = 2;
    public static final int CHANGE_APPEARING = 0;
    public static final int CHANGE_DISAPPEARING = 1;
    public static final int CHANGING = 4;
    private static TimeInterpolator DECEL_INTERPOLATOR = null;
    private static long DEFAULT_DURATION = 0;
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
    private final HashMap<View, OnLayoutChangeListener> layoutChangeListenerMap;
    private boolean mAnimateParentHierarchy;
    private Animator mAppearingAnim;
    private long mAppearingDelay;
    private long mAppearingDuration;
    private TimeInterpolator mAppearingInterpolator;
    private Animator mChangingAnim;
    private Animator mChangingAppearingAnim;
    private long mChangingAppearingDelay;
    private long mChangingAppearingDuration;
    private TimeInterpolator mChangingAppearingInterpolator;
    private long mChangingAppearingStagger;
    private long mChangingDelay;
    private Animator mChangingDisappearingAnim;
    private long mChangingDisappearingDelay;
    private long mChangingDisappearingDuration;
    private TimeInterpolator mChangingDisappearingInterpolator;
    private long mChangingDisappearingStagger;
    private long mChangingDuration;
    private TimeInterpolator mChangingInterpolator;
    private long mChangingStagger;
    private Animator mDisappearingAnim;
    private long mDisappearingDelay;
    private long mDisappearingDuration;
    private TimeInterpolator mDisappearingInterpolator;
    private ArrayList<TransitionListener> mListeners;
    private int mTransitionTypes;
    private final HashMap<View, Animator> pendingAnimations;
    private long staggerDelay;

    /* renamed from: android.animation.LayoutTransition.1 */
    class AnonymousClass1 extends AnimatorListenerAdapter {
        final /* synthetic */ View val$child;

        AnonymousClass1(View val$child) {
            this.val$child = val$child;
        }

        public void onAnimationEnd(Animator animation) {
            LayoutTransition.this.pendingAnimations.remove(this.val$child);
        }
    }

    /* renamed from: android.animation.LayoutTransition.2 */
    class AnonymousClass2 implements OnLayoutChangeListener {
        final /* synthetic */ Animator val$anim;
        final /* synthetic */ int val$changeReason;
        final /* synthetic */ View val$child;
        final /* synthetic */ long val$duration;
        final /* synthetic */ ViewGroup val$parent;

        AnonymousClass2(Animator val$anim, int val$changeReason, long val$duration, View val$child, ViewGroup val$parent) {
            this.val$anim = val$anim;
            this.val$changeReason = val$changeReason;
            this.val$duration = val$duration;
            this.val$child = val$child;
            this.val$parent = val$parent;
        }

        public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
            this.val$anim.setupEndValues();
            if (this.val$anim instanceof ValueAnimator) {
                boolean valuesDiffer = false;
                PropertyValuesHolder[] oldValues = this.val$anim.getValues();
                for (int i = LayoutTransition.CHANGE_APPEARING; i < oldValues.length; i += LayoutTransition.FLAG_APPEARING) {
                    PropertyValuesHolder pvh = oldValues[i];
                    if (pvh.mKeyframes instanceof KeyframeSet) {
                        KeyframeSet keyframeSet = pvh.mKeyframes;
                        if (keyframeSet.mFirstKeyframe == null || keyframeSet.mLastKeyframe == null || !keyframeSet.mFirstKeyframe.getValue().equals(keyframeSet.mLastKeyframe.getValue())) {
                            valuesDiffer = true;
                        }
                    } else if (!pvh.mKeyframes.getValue(0.0f).equals(pvh.mKeyframes.getValue(Engine.DEFAULT_VOLUME))) {
                        valuesDiffer = true;
                    }
                }
                if (!valuesDiffer) {
                    return;
                }
            }
            long startDelay = 0;
            LayoutTransition layoutTransition;
            switch (this.val$changeReason) {
                case LayoutTransition.FLAG_DISAPPEARING /*2*/:
                    startDelay = LayoutTransition.this.mChangingAppearingDelay + LayoutTransition.this.staggerDelay;
                    layoutTransition = LayoutTransition.this;
                    layoutTransition.staggerDelay = layoutTransition.staggerDelay + LayoutTransition.this.mChangingAppearingStagger;
                    if (LayoutTransition.this.mChangingAppearingInterpolator != LayoutTransition.sChangingAppearingInterpolator) {
                        this.val$anim.setInterpolator(LayoutTransition.this.mChangingAppearingInterpolator);
                        break;
                    }
                    break;
                case LayoutTransition.DISAPPEARING /*3*/:
                    startDelay = LayoutTransition.this.mChangingDisappearingDelay + LayoutTransition.this.staggerDelay;
                    layoutTransition = LayoutTransition.this;
                    layoutTransition.staggerDelay = layoutTransition.staggerDelay + LayoutTransition.this.mChangingDisappearingStagger;
                    if (LayoutTransition.this.mChangingDisappearingInterpolator != LayoutTransition.sChangingDisappearingInterpolator) {
                        this.val$anim.setInterpolator(LayoutTransition.this.mChangingDisappearingInterpolator);
                        break;
                    }
                    break;
                case LayoutTransition.FLAG_CHANGE_APPEARING /*4*/:
                    startDelay = LayoutTransition.this.mChangingDelay + LayoutTransition.this.staggerDelay;
                    layoutTransition = LayoutTransition.this;
                    layoutTransition.staggerDelay = layoutTransition.staggerDelay + LayoutTransition.this.mChangingStagger;
                    if (LayoutTransition.this.mChangingInterpolator != LayoutTransition.sChangingInterpolator) {
                        this.val$anim.setInterpolator(LayoutTransition.this.mChangingInterpolator);
                        break;
                    }
                    break;
            }
            this.val$anim.setStartDelay(startDelay);
            this.val$anim.setDuration(this.val$duration);
            Animator prevAnimation = (Animator) LayoutTransition.this.currentChangingAnimations.get(this.val$child);
            if (prevAnimation != null) {
                prevAnimation.cancel();
            }
            if (((Animator) LayoutTransition.this.pendingAnimations.get(this.val$child)) != null) {
                LayoutTransition.this.pendingAnimations.remove(this.val$child);
            }
            LayoutTransition.this.currentChangingAnimations.put(this.val$child, this.val$anim);
            this.val$parent.requestTransitionStart(LayoutTransition.this);
            this.val$child.removeOnLayoutChangeListener(this);
            LayoutTransition.this.layoutChangeListenerMap.remove(this.val$child);
        }
    }

    /* renamed from: android.animation.LayoutTransition.3 */
    class AnonymousClass3 extends AnimatorListenerAdapter {
        final /* synthetic */ int val$changeReason;
        final /* synthetic */ View val$child;
        final /* synthetic */ OnLayoutChangeListener val$listener;
        final /* synthetic */ ViewGroup val$parent;

        AnonymousClass3(ViewGroup val$parent, View val$child, int val$changeReason, OnLayoutChangeListener val$listener) {
            this.val$parent = val$parent;
            this.val$child = val$child;
            this.val$changeReason = val$changeReason;
            this.val$listener = val$listener;
        }

        public void onAnimationStart(Animator animator) {
            if (LayoutTransition.this.hasListeners()) {
                for (TransitionListener listener : (ArrayList) LayoutTransition.this.mListeners.clone()) {
                    int i;
                    LayoutTransition layoutTransition = LayoutTransition.this;
                    ViewGroup viewGroup = this.val$parent;
                    View view = this.val$child;
                    if (this.val$changeReason == LayoutTransition.FLAG_DISAPPEARING) {
                        i = LayoutTransition.CHANGE_APPEARING;
                    } else if (this.val$changeReason == LayoutTransition.DISAPPEARING) {
                        i = LayoutTransition.FLAG_APPEARING;
                    } else {
                        i = LayoutTransition.FLAG_CHANGE_APPEARING;
                    }
                    listener.startTransition(layoutTransition, viewGroup, view, i);
                }
            }
        }

        public void onAnimationCancel(Animator animator) {
            this.val$child.removeOnLayoutChangeListener(this.val$listener);
            LayoutTransition.this.layoutChangeListenerMap.remove(this.val$child);
        }

        public void onAnimationEnd(Animator animator) {
            LayoutTransition.this.currentChangingAnimations.remove(this.val$child);
            if (LayoutTransition.this.hasListeners()) {
                for (TransitionListener listener : (ArrayList) LayoutTransition.this.mListeners.clone()) {
                    int i;
                    LayoutTransition layoutTransition = LayoutTransition.this;
                    ViewGroup viewGroup = this.val$parent;
                    View view = this.val$child;
                    if (this.val$changeReason == LayoutTransition.FLAG_DISAPPEARING) {
                        i = LayoutTransition.CHANGE_APPEARING;
                    } else if (this.val$changeReason == LayoutTransition.DISAPPEARING) {
                        i = LayoutTransition.FLAG_APPEARING;
                    } else {
                        i = LayoutTransition.FLAG_CHANGE_APPEARING;
                    }
                    listener.endTransition(layoutTransition, viewGroup, view, i);
                }
            }
        }
    }

    /* renamed from: android.animation.LayoutTransition.4 */
    class AnonymousClass4 extends AnimatorListenerAdapter {
        final /* synthetic */ View val$child;
        final /* synthetic */ ViewGroup val$parent;

        AnonymousClass4(View val$child, ViewGroup val$parent) {
            this.val$child = val$child;
            this.val$parent = val$parent;
        }

        public void onAnimationEnd(Animator anim) {
            LayoutTransition.this.currentAppearingAnimations.remove(this.val$child);
            if (LayoutTransition.this.hasListeners()) {
                for (TransitionListener listener : (ArrayList) LayoutTransition.this.mListeners.clone()) {
                    listener.endTransition(LayoutTransition.this, this.val$parent, this.val$child, LayoutTransition.FLAG_DISAPPEARING);
                }
            }
        }
    }

    /* renamed from: android.animation.LayoutTransition.5 */
    class AnonymousClass5 extends AnimatorListenerAdapter {
        final /* synthetic */ View val$child;
        final /* synthetic */ ViewGroup val$parent;
        final /* synthetic */ float val$preAnimAlpha;

        AnonymousClass5(View val$child, float val$preAnimAlpha, ViewGroup val$parent) {
            this.val$child = val$child;
            this.val$preAnimAlpha = val$preAnimAlpha;
            this.val$parent = val$parent;
        }

        public void onAnimationEnd(Animator anim) {
            LayoutTransition.this.currentDisappearingAnimations.remove(this.val$child);
            this.val$child.setAlpha(this.val$preAnimAlpha);
            if (LayoutTransition.this.hasListeners()) {
                for (TransitionListener listener : (ArrayList) LayoutTransition.this.mListeners.clone()) {
                    listener.endTransition(LayoutTransition.this, this.val$parent, this.val$child, LayoutTransition.DISAPPEARING);
                }
            }
        }
    }

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

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.animation.LayoutTransition.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.animation.LayoutTransition.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: android.animation.LayoutTransition.<clinit>():void");
    }

    public LayoutTransition() {
        this.mDisappearingAnim = null;
        this.mAppearingAnim = null;
        this.mChangingAppearingAnim = null;
        this.mChangingDisappearingAnim = null;
        this.mChangingAnim = null;
        this.mChangingAppearingDuration = DEFAULT_DURATION;
        this.mChangingDisappearingDuration = DEFAULT_DURATION;
        this.mChangingDuration = DEFAULT_DURATION;
        this.mAppearingDuration = DEFAULT_DURATION;
        this.mDisappearingDuration = DEFAULT_DURATION;
        this.mAppearingDelay = DEFAULT_DURATION;
        this.mDisappearingDelay = 0;
        this.mChangingAppearingDelay = 0;
        this.mChangingDisappearingDelay = DEFAULT_DURATION;
        this.mChangingDelay = 0;
        this.mChangingAppearingStagger = 0;
        this.mChangingDisappearingStagger = 0;
        this.mChangingStagger = 0;
        this.mAppearingInterpolator = sAppearingInterpolator;
        this.mDisappearingInterpolator = sDisappearingInterpolator;
        this.mChangingAppearingInterpolator = sChangingAppearingInterpolator;
        this.mChangingDisappearingInterpolator = sChangingDisappearingInterpolator;
        this.mChangingInterpolator = sChangingInterpolator;
        this.pendingAnimations = new HashMap();
        this.currentChangingAnimations = new LinkedHashMap();
        this.currentAppearingAnimations = new LinkedHashMap();
        this.currentDisappearingAnimations = new LinkedHashMap();
        this.layoutChangeListenerMap = new HashMap();
        this.mTransitionTypes = 15;
        this.mAnimateParentHierarchy = true;
        if (defaultChangeIn == null) {
            PropertyValuesHolder pvhLeft = PropertyValuesHolder.ofInt("left", CHANGE_APPEARING, FLAG_APPEARING);
            PropertyValuesHolder pvhTop = PropertyValuesHolder.ofInt("top", CHANGE_APPEARING, FLAG_APPEARING);
            PropertyValuesHolder pvhRight = PropertyValuesHolder.ofInt("right", CHANGE_APPEARING, FLAG_APPEARING);
            PropertyValuesHolder pvhBottom = PropertyValuesHolder.ofInt("bottom", CHANGE_APPEARING, FLAG_APPEARING);
            PropertyValuesHolder pvhScrollX = PropertyValuesHolder.ofInt("scrollX", CHANGE_APPEARING, FLAG_APPEARING);
            PropertyValuesHolder pvhScrollY = PropertyValuesHolder.ofInt("scrollY", CHANGE_APPEARING, FLAG_APPEARING);
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
            defaultFadeIn = ObjectAnimator.ofFloat(null, "alpha", 0.0f, Engine.DEFAULT_VOLUME);
            defaultFadeIn.setDuration(DEFAULT_DURATION);
            defaultFadeIn.setStartDelay(this.mAppearingDelay);
            defaultFadeIn.setInterpolator(this.mAppearingInterpolator);
            defaultFadeOut = ObjectAnimator.ofFloat(null, "alpha", Engine.DEFAULT_VOLUME, 0.0f);
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
            case CHANGE_APPEARING /*0*/:
                this.mTransitionTypes |= FLAG_CHANGE_APPEARING;
            case FLAG_APPEARING /*1*/:
                this.mTransitionTypes |= FLAG_CHANGE_DISAPPEARING;
            case FLAG_DISAPPEARING /*2*/:
                this.mTransitionTypes |= FLAG_APPEARING;
            case DISAPPEARING /*3*/:
                this.mTransitionTypes |= FLAG_DISAPPEARING;
            case FLAG_CHANGE_APPEARING /*4*/:
                this.mTransitionTypes |= FLAG_CHANGING;
            default:
        }
    }

    public void disableTransitionType(int transitionType) {
        switch (transitionType) {
            case CHANGE_APPEARING /*0*/:
                this.mTransitionTypes &= -5;
            case FLAG_APPEARING /*1*/:
                this.mTransitionTypes &= -9;
            case FLAG_DISAPPEARING /*2*/:
                this.mTransitionTypes &= -2;
            case DISAPPEARING /*3*/:
                this.mTransitionTypes &= -3;
            case FLAG_CHANGE_APPEARING /*4*/:
                this.mTransitionTypes &= -17;
            default:
        }
    }

    public boolean isTransitionTypeEnabled(int transitionType) {
        boolean z = true;
        switch (transitionType) {
            case CHANGE_APPEARING /*0*/:
                if ((this.mTransitionTypes & FLAG_CHANGE_APPEARING) != FLAG_CHANGE_APPEARING) {
                    z = false;
                }
                return z;
            case FLAG_APPEARING /*1*/:
                if ((this.mTransitionTypes & FLAG_CHANGE_DISAPPEARING) != FLAG_CHANGE_DISAPPEARING) {
                    z = false;
                }
                return z;
            case FLAG_DISAPPEARING /*2*/:
                if ((this.mTransitionTypes & FLAG_APPEARING) != FLAG_APPEARING) {
                    z = false;
                }
                return z;
            case DISAPPEARING /*3*/:
                if ((this.mTransitionTypes & FLAG_DISAPPEARING) != FLAG_DISAPPEARING) {
                    z = false;
                }
                return z;
            case FLAG_CHANGE_APPEARING /*4*/:
                if ((this.mTransitionTypes & FLAG_CHANGING) != FLAG_CHANGING) {
                    z = false;
                }
                return z;
            default:
                return false;
        }
    }

    public void setStartDelay(int transitionType, long delay) {
        switch (transitionType) {
            case CHANGE_APPEARING /*0*/:
                this.mChangingAppearingDelay = delay;
            case FLAG_APPEARING /*1*/:
                this.mChangingDisappearingDelay = delay;
            case FLAG_DISAPPEARING /*2*/:
                this.mAppearingDelay = delay;
            case DISAPPEARING /*3*/:
                this.mDisappearingDelay = delay;
            case FLAG_CHANGE_APPEARING /*4*/:
                this.mChangingDelay = delay;
            default:
        }
    }

    public long getStartDelay(int transitionType) {
        switch (transitionType) {
            case CHANGE_APPEARING /*0*/:
                return this.mChangingAppearingDelay;
            case FLAG_APPEARING /*1*/:
                return this.mChangingDisappearingDelay;
            case FLAG_DISAPPEARING /*2*/:
                return this.mAppearingDelay;
            case DISAPPEARING /*3*/:
                return this.mDisappearingDelay;
            case FLAG_CHANGE_APPEARING /*4*/:
                return this.mChangingDelay;
            default:
                return 0;
        }
    }

    public void setDuration(int transitionType, long duration) {
        switch (transitionType) {
            case CHANGE_APPEARING /*0*/:
                this.mChangingAppearingDuration = duration;
            case FLAG_APPEARING /*1*/:
                this.mChangingDisappearingDuration = duration;
            case FLAG_DISAPPEARING /*2*/:
                this.mAppearingDuration = duration;
            case DISAPPEARING /*3*/:
                this.mDisappearingDuration = duration;
            case FLAG_CHANGE_APPEARING /*4*/:
                this.mChangingDuration = duration;
            default:
        }
    }

    public long getDuration(int transitionType) {
        switch (transitionType) {
            case CHANGE_APPEARING /*0*/:
                return this.mChangingAppearingDuration;
            case FLAG_APPEARING /*1*/:
                return this.mChangingDisappearingDuration;
            case FLAG_DISAPPEARING /*2*/:
                return this.mAppearingDuration;
            case DISAPPEARING /*3*/:
                return this.mDisappearingDuration;
            case FLAG_CHANGE_APPEARING /*4*/:
                return this.mChangingDuration;
            default:
                return 0;
        }
    }

    public void setStagger(int transitionType, long duration) {
        switch (transitionType) {
            case CHANGE_APPEARING /*0*/:
                this.mChangingAppearingStagger = duration;
            case FLAG_APPEARING /*1*/:
                this.mChangingDisappearingStagger = duration;
            case FLAG_CHANGE_APPEARING /*4*/:
                this.mChangingStagger = duration;
            default:
        }
    }

    public long getStagger(int transitionType) {
        switch (transitionType) {
            case CHANGE_APPEARING /*0*/:
                return this.mChangingAppearingStagger;
            case FLAG_APPEARING /*1*/:
                return this.mChangingDisappearingStagger;
            case FLAG_CHANGE_APPEARING /*4*/:
                return this.mChangingStagger;
            default:
                return 0;
        }
    }

    public void setInterpolator(int transitionType, TimeInterpolator interpolator) {
        switch (transitionType) {
            case CHANGE_APPEARING /*0*/:
                this.mChangingAppearingInterpolator = interpolator;
            case FLAG_APPEARING /*1*/:
                this.mChangingDisappearingInterpolator = interpolator;
            case FLAG_DISAPPEARING /*2*/:
                this.mAppearingInterpolator = interpolator;
            case DISAPPEARING /*3*/:
                this.mDisappearingInterpolator = interpolator;
            case FLAG_CHANGE_APPEARING /*4*/:
                this.mChangingInterpolator = interpolator;
            default:
        }
    }

    public TimeInterpolator getInterpolator(int transitionType) {
        switch (transitionType) {
            case CHANGE_APPEARING /*0*/:
                return this.mChangingAppearingInterpolator;
            case FLAG_APPEARING /*1*/:
                return this.mChangingDisappearingInterpolator;
            case FLAG_DISAPPEARING /*2*/:
                return this.mAppearingInterpolator;
            case DISAPPEARING /*3*/:
                return this.mDisappearingInterpolator;
            case FLAG_CHANGE_APPEARING /*4*/:
                return this.mChangingInterpolator;
            default:
                return null;
        }
    }

    public void setAnimator(int transitionType, Animator animator) {
        switch (transitionType) {
            case CHANGE_APPEARING /*0*/:
                this.mChangingAppearingAnim = animator;
            case FLAG_APPEARING /*1*/:
                this.mChangingDisappearingAnim = animator;
            case FLAG_DISAPPEARING /*2*/:
                this.mAppearingAnim = animator;
            case DISAPPEARING /*3*/:
                this.mDisappearingAnim = animator;
            case FLAG_CHANGE_APPEARING /*4*/:
                this.mChangingAnim = animator;
            default:
        }
    }

    public Animator getAnimator(int transitionType) {
        switch (transitionType) {
            case CHANGE_APPEARING /*0*/:
                return this.mChangingAppearingAnim;
            case FLAG_APPEARING /*1*/:
                return this.mChangingDisappearingAnim;
            case FLAG_DISAPPEARING /*2*/:
                return this.mAppearingAnim;
            case DISAPPEARING /*3*/:
                return this.mDisappearingAnim;
            case FLAG_CHANGE_APPEARING /*4*/:
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
            case FLAG_DISAPPEARING /*2*/:
                baseAnimator = this.mChangingAppearingAnim;
                duration = this.mChangingAppearingDuration;
                parentAnimator = defaultChangeIn;
                break;
            case DISAPPEARING /*3*/:
                baseAnimator = this.mChangingDisappearingAnim;
                duration = this.mChangingDisappearingDuration;
                parentAnimator = defaultChangeOut;
                break;
            case FLAG_CHANGE_APPEARING /*4*/:
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
                for (int i = CHANGE_APPEARING; i < numChildren; i += FLAG_APPEARING) {
                    View child = parent.getChildAt(i);
                    if (child != newView) {
                        setupChangeAnimation(parent, changeReason, baseAnimator, duration, child);
                    }
                }
                if (this.mAnimateParentHierarchy) {
                    View view = parent;
                    while (view != null) {
                        ViewParent parentParent = view.getParent();
                        if (parentParent instanceof ViewGroup) {
                            setupChangeAnimation((ViewGroup) parentParent, changeReason, parentAnimator, duration, view);
                            view = (ViewGroup) parentParent;
                        } else {
                            view = null;
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
                Animator anim = baseAnimator.clone();
                anim.setTarget(child);
                anim.setupStartValues();
                Animator currentAnimation = (Animator) this.pendingAnimations.get(child);
                if (currentAnimation != null) {
                    currentAnimation.cancel();
                    this.pendingAnimations.remove(child);
                }
                this.pendingAnimations.put(child, anim);
                ValueAnimator pendingAnimRemover = ValueAnimator.ofFloat(0.0f, Engine.DEFAULT_VOLUME).setDuration(100 + duration);
                pendingAnimRemover.addListener(new AnonymousClass1(child));
                pendingAnimRemover.start();
                OnLayoutChangeListener listener = new AnonymousClass2(anim, changeReason, duration, child, parent);
                anim.addListener(new AnonymousClass3(parent, child, changeReason, listener));
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
            case CHANGE_APPEARING /*0*/:
            case FLAG_APPEARING /*1*/:
            case FLAG_CHANGE_APPEARING /*4*/:
                if (this.currentChangingAnimations.size() > 0) {
                    for (Animator anim : ((LinkedHashMap) this.currentChangingAnimations.clone()).values()) {
                        anim.cancel();
                    }
                    this.currentChangingAnimations.clear();
                }
            case FLAG_DISAPPEARING /*2*/:
                if (this.currentAppearingAnimations.size() > 0) {
                    for (Animator anim2 : ((LinkedHashMap) this.currentAppearingAnimations.clone()).values()) {
                        anim2.end();
                    }
                    this.currentAppearingAnimations.clear();
                }
            case DISAPPEARING /*3*/:
                if (this.currentDisappearingAnimations.size() > 0) {
                    for (Animator anim22 : ((LinkedHashMap) this.currentDisappearingAnimations.clone()).values()) {
                        anim22.end();
                    }
                    this.currentDisappearingAnimations.clear();
                }
            default:
        }
    }

    private void runAppearingTransition(ViewGroup parent, View child) {
        Animator currentAnimation = (Animator) this.currentDisappearingAnimations.get(child);
        if (currentAnimation != null) {
            currentAnimation.cancel();
        }
        if (this.mAppearingAnim == null) {
            if (hasListeners()) {
                for (TransitionListener listener : (ArrayList) this.mListeners.clone()) {
                    listener.endTransition(this, parent, child, FLAG_DISAPPEARING);
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
        anim.addListener(new AnonymousClass4(child, parent));
        this.currentAppearingAnimations.put(child, anim);
        anim.start();
    }

    private void runDisappearingTransition(ViewGroup parent, View child) {
        Animator currentAnimation = (Animator) this.currentAppearingAnimations.get(child);
        if (currentAnimation != null) {
            currentAnimation.cancel();
        }
        if (this.mDisappearingAnim == null) {
            if (hasListeners()) {
                for (TransitionListener listener : (ArrayList) this.mListeners.clone()) {
                    listener.endTransition(this, parent, child, DISAPPEARING);
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
        anim.addListener(new AnonymousClass5(child, child.getAlpha(), parent));
        if (anim instanceof ObjectAnimator) {
            ((ObjectAnimator) anim).setCurrentPlayTime(0);
        }
        this.currentDisappearingAnimations.put(child, anim);
        anim.start();
    }

    private void addChild(ViewGroup parent, View child, boolean changesLayout) {
        if (parent.getWindowVisibility() == 0) {
            if ((this.mTransitionTypes & FLAG_APPEARING) == FLAG_APPEARING) {
                cancel(DISAPPEARING);
            }
            if (changesLayout && (this.mTransitionTypes & FLAG_CHANGE_APPEARING) == FLAG_CHANGE_APPEARING) {
                cancel(CHANGE_APPEARING);
                cancel(FLAG_CHANGE_APPEARING);
            }
            if (hasListeners() && (this.mTransitionTypes & FLAG_APPEARING) == FLAG_APPEARING) {
                for (TransitionListener listener : (ArrayList) this.mListeners.clone()) {
                    listener.startTransition(this, parent, child, FLAG_DISAPPEARING);
                }
            }
            if (changesLayout && (this.mTransitionTypes & FLAG_CHANGE_APPEARING) == FLAG_CHANGE_APPEARING) {
                runChangeTransition(parent, child, FLAG_DISAPPEARING);
            }
            if ((this.mTransitionTypes & FLAG_APPEARING) == FLAG_APPEARING) {
                runAppearingTransition(parent, child);
            }
        }
    }

    private boolean hasListeners() {
        return this.mListeners != null && this.mListeners.size() > 0;
    }

    public void layoutChange(ViewGroup parent) {
        if (parent.getWindowVisibility() == 0 && (this.mTransitionTypes & FLAG_CHANGING) == FLAG_CHANGING && !isRunning()) {
            runChangeTransition(parent, null, FLAG_CHANGE_APPEARING);
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
        addChild(parent, child, oldVisibility == FLAG_CHANGE_DISAPPEARING);
    }

    private void removeChild(ViewGroup parent, View child, boolean changesLayout) {
        if (parent.getWindowVisibility() == 0) {
            if ((this.mTransitionTypes & FLAG_DISAPPEARING) == FLAG_DISAPPEARING) {
                cancel(FLAG_DISAPPEARING);
            }
            if (changesLayout && (this.mTransitionTypes & FLAG_CHANGE_DISAPPEARING) == FLAG_CHANGE_DISAPPEARING) {
                cancel(FLAG_APPEARING);
                cancel(FLAG_CHANGE_APPEARING);
            }
            if (hasListeners() && (this.mTransitionTypes & FLAG_DISAPPEARING) == FLAG_DISAPPEARING) {
                for (TransitionListener listener : (ArrayList) this.mListeners.clone()) {
                    listener.startTransition(this, parent, child, DISAPPEARING);
                }
            }
            if (changesLayout && (this.mTransitionTypes & FLAG_CHANGE_DISAPPEARING) == FLAG_CHANGE_DISAPPEARING) {
                runChangeTransition(parent, child, DISAPPEARING);
            }
            if ((this.mTransitionTypes & FLAG_DISAPPEARING) == FLAG_DISAPPEARING) {
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
        removeChild(parent, child, newVisibility == FLAG_CHANGE_DISAPPEARING);
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
