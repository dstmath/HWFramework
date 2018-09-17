package android.view;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.TimeInterpolator;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.view.WindowManager.LayoutParams;
import java.util.ArrayList;
import java.util.HashMap;

public class ViewPropertyAnimator {
    static final int ALPHA = 2048;
    static final int NONE = 0;
    static final int ROTATION = 32;
    static final int ROTATION_X = 64;
    static final int ROTATION_Y = 128;
    static final int SCALE_X = 8;
    static final int SCALE_Y = 16;
    private static final int TRANSFORM_MASK = 2047;
    static final int TRANSLATION_X = 1;
    static final int TRANSLATION_Y = 2;
    static final int TRANSLATION_Z = 4;
    static final int X = 256;
    static final int Y = 512;
    static final int Z = 1024;
    private Runnable mAnimationStarter;
    private HashMap<Animator, Runnable> mAnimatorCleanupMap;
    private AnimatorEventListener mAnimatorEventListener;
    private HashMap<Animator, PropertyBundle> mAnimatorMap;
    private HashMap<Animator, Runnable> mAnimatorOnEndMap;
    private HashMap<Animator, Runnable> mAnimatorOnStartMap;
    private HashMap<Animator, Runnable> mAnimatorSetupMap;
    private long mDuration;
    private boolean mDurationSet;
    private TimeInterpolator mInterpolator;
    private boolean mInterpolatorSet;
    private AnimatorListener mListener;
    ArrayList<NameValuesHolder> mPendingAnimations;
    private Runnable mPendingCleanupAction;
    private Runnable mPendingOnEndAction;
    private Runnable mPendingOnStartAction;
    private Runnable mPendingSetupAction;
    private ViewPropertyAnimatorRT mRTBackend;
    private long mStartDelay;
    private boolean mStartDelaySet;
    private ValueAnimator mTempValueAnimator;
    private AnimatorUpdateListener mUpdateListener;
    final View mView;

    /* renamed from: android.view.ViewPropertyAnimator.3 */
    class AnonymousClass3 implements Runnable {
        final /* synthetic */ int val$currentLayerType;

        AnonymousClass3(int val$currentLayerType) {
            this.val$currentLayerType = val$currentLayerType;
        }

        public void run() {
            ViewPropertyAnimator.this.mView.setLayerType(this.val$currentLayerType, null);
        }
    }

    private class AnimatorEventListener implements AnimatorListener, AnimatorUpdateListener {
        private AnimatorEventListener() {
        }

        public void onAnimationStart(Animator animation) {
            Runnable r;
            if (ViewPropertyAnimator.this.mAnimatorSetupMap != null) {
                r = (Runnable) ViewPropertyAnimator.this.mAnimatorSetupMap.get(animation);
                if (r != null) {
                    r.run();
                }
                ViewPropertyAnimator.this.mAnimatorSetupMap.remove(animation);
            }
            if (ViewPropertyAnimator.this.mAnimatorOnStartMap != null) {
                r = (Runnable) ViewPropertyAnimator.this.mAnimatorOnStartMap.get(animation);
                if (r != null) {
                    r.run();
                }
                ViewPropertyAnimator.this.mAnimatorOnStartMap.remove(animation);
            }
            if (ViewPropertyAnimator.this.mListener != null) {
                ViewPropertyAnimator.this.mListener.onAnimationStart(animation);
            }
        }

        public void onAnimationCancel(Animator animation) {
            if (ViewPropertyAnimator.this.mListener != null) {
                ViewPropertyAnimator.this.mListener.onAnimationCancel(animation);
            }
            if (ViewPropertyAnimator.this.mAnimatorOnEndMap != null) {
                ViewPropertyAnimator.this.mAnimatorOnEndMap.remove(animation);
            }
        }

        public void onAnimationRepeat(Animator animation) {
            if (ViewPropertyAnimator.this.mListener != null) {
                ViewPropertyAnimator.this.mListener.onAnimationRepeat(animation);
            }
        }

        public void onAnimationEnd(Animator animation) {
            Runnable r;
            ViewPropertyAnimator.this.mView.setHasTransientState(false);
            if (ViewPropertyAnimator.this.mAnimatorCleanupMap != null) {
                r = (Runnable) ViewPropertyAnimator.this.mAnimatorCleanupMap.get(animation);
                if (r != null) {
                    r.run();
                }
                ViewPropertyAnimator.this.mAnimatorCleanupMap.remove(animation);
            }
            if (ViewPropertyAnimator.this.mListener != null) {
                ViewPropertyAnimator.this.mListener.onAnimationEnd(animation);
            }
            if (ViewPropertyAnimator.this.mAnimatorOnEndMap != null) {
                r = (Runnable) ViewPropertyAnimator.this.mAnimatorOnEndMap.get(animation);
                if (r != null) {
                    r.run();
                }
                ViewPropertyAnimator.this.mAnimatorOnEndMap.remove(animation);
            }
            ViewPropertyAnimator.this.mAnimatorMap.remove(animation);
        }

        public void onAnimationUpdate(ValueAnimator animation) {
            PropertyBundle propertyBundle = (PropertyBundle) ViewPropertyAnimator.this.mAnimatorMap.get(animation);
            if (propertyBundle != null) {
                boolean hardwareAccelerated = ViewPropertyAnimator.this.mView.isHardwareAccelerated();
                boolean z = false;
                if (!hardwareAccelerated) {
                    ViewPropertyAnimator.this.mView.invalidateParentCaches();
                }
                float fraction = animation.getAnimatedFraction();
                int propertyMask = propertyBundle.mPropertyMask;
                if ((propertyMask & ViewPropertyAnimator.TRANSFORM_MASK) != 0) {
                    ViewPropertyAnimator.this.mView.invalidateViewProperty(hardwareAccelerated, false);
                }
                ArrayList<NameValuesHolder> valueList = propertyBundle.mNameValuesHolder;
                if (valueList != null) {
                    int count = valueList.size();
                    for (int i = ViewPropertyAnimator.NONE; i < count; i += ViewPropertyAnimator.TRANSLATION_X) {
                        NameValuesHolder values = (NameValuesHolder) valueList.get(i);
                        float value = values.mFromValue + (values.mDeltaValue * fraction);
                        if (values.mNameConstant == ViewPropertyAnimator.ALPHA) {
                            z = ViewPropertyAnimator.this.mView.setAlphaNoInvalidation(value);
                        } else {
                            ViewPropertyAnimator.this.setValue(values.mNameConstant, value);
                        }
                    }
                }
                if (!((propertyMask & ViewPropertyAnimator.TRANSFORM_MASK) == 0 || hardwareAccelerated)) {
                    View view = ViewPropertyAnimator.this.mView;
                    view.mPrivateFlags |= ViewPropertyAnimator.ROTATION;
                }
                if (z) {
                    ViewPropertyAnimator.this.mView.invalidate(true);
                } else {
                    ViewPropertyAnimator.this.mView.invalidateViewProperty(false, false);
                }
                if (ViewPropertyAnimator.this.mUpdateListener != null) {
                    ViewPropertyAnimator.this.mUpdateListener.onAnimationUpdate(animation);
                }
            }
        }
    }

    static class NameValuesHolder {
        float mDeltaValue;
        float mFromValue;
        int mNameConstant;

        NameValuesHolder(int nameConstant, float fromValue, float deltaValue) {
            this.mNameConstant = nameConstant;
            this.mFromValue = fromValue;
            this.mDeltaValue = deltaValue;
        }
    }

    private static class PropertyBundle {
        ArrayList<NameValuesHolder> mNameValuesHolder;
        int mPropertyMask;

        boolean cancel(int r1) {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.view.ViewPropertyAnimator.PropertyBundle.cancel(int):boolean
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException: Unknown instruction: not-int
	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:568)
	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:56)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:99)
	... 8 more
*/
            /*
            // Can't load method instructions.
            */
            throw new UnsupportedOperationException("Method not decompiled: android.view.ViewPropertyAnimator.PropertyBundle.cancel(int):boolean");
        }

        PropertyBundle(int propertyMask, ArrayList<NameValuesHolder> nameValuesHolder) {
            this.mPropertyMask = propertyMask;
            this.mNameValuesHolder = nameValuesHolder;
        }
    }

    ViewPropertyAnimator(View view) {
        this.mDurationSet = false;
        this.mStartDelay = 0;
        this.mStartDelaySet = false;
        this.mInterpolatorSet = false;
        this.mListener = null;
        this.mUpdateListener = null;
        this.mAnimatorEventListener = new AnimatorEventListener();
        this.mPendingAnimations = new ArrayList();
        this.mAnimationStarter = new Runnable() {
            public void run() {
                ViewPropertyAnimator.this.startAnimation();
            }
        };
        this.mAnimatorMap = new HashMap();
        this.mView = view;
        view.ensureTransformationInfo();
    }

    public ViewPropertyAnimator setDuration(long duration) {
        if (duration < 0) {
            throw new IllegalArgumentException("Animators cannot have negative duration: " + duration);
        }
        this.mDurationSet = true;
        this.mDuration = duration;
        return this;
    }

    public long getDuration() {
        if (this.mDurationSet) {
            return this.mDuration;
        }
        if (this.mTempValueAnimator == null) {
            this.mTempValueAnimator = new ValueAnimator();
        }
        return this.mTempValueAnimator.getDuration();
    }

    public long getStartDelay() {
        if (this.mStartDelaySet) {
            return this.mStartDelay;
        }
        return 0;
    }

    public ViewPropertyAnimator setStartDelay(long startDelay) {
        if (startDelay < 0) {
            throw new IllegalArgumentException("Animators cannot have negative start delay: " + startDelay);
        }
        this.mStartDelaySet = true;
        this.mStartDelay = startDelay;
        return this;
    }

    public ViewPropertyAnimator setInterpolator(TimeInterpolator interpolator) {
        this.mInterpolatorSet = true;
        this.mInterpolator = interpolator;
        return this;
    }

    public TimeInterpolator getInterpolator() {
        if (this.mInterpolatorSet) {
            return this.mInterpolator;
        }
        if (this.mTempValueAnimator == null) {
            this.mTempValueAnimator = new ValueAnimator();
        }
        return this.mTempValueAnimator.getInterpolator();
    }

    public ViewPropertyAnimator setListener(AnimatorListener listener) {
        this.mListener = listener;
        return this;
    }

    AnimatorListener getListener() {
        return this.mListener;
    }

    public ViewPropertyAnimator setUpdateListener(AnimatorUpdateListener listener) {
        this.mUpdateListener = listener;
        return this;
    }

    AnimatorUpdateListener getUpdateListener() {
        return this.mUpdateListener;
    }

    public void start() {
        this.mView.removeCallbacks(this.mAnimationStarter);
        startAnimation();
    }

    public void cancel() {
        if (this.mAnimatorMap.size() > 0) {
            for (Animator runningAnim : ((HashMap) this.mAnimatorMap.clone()).keySet()) {
                runningAnim.cancel();
            }
        }
        this.mPendingAnimations.clear();
        this.mPendingSetupAction = null;
        this.mPendingCleanupAction = null;
        this.mPendingOnStartAction = null;
        this.mPendingOnEndAction = null;
        this.mView.removeCallbacks(this.mAnimationStarter);
        if (this.mRTBackend != null) {
            this.mRTBackend.cancelAll();
        }
    }

    public ViewPropertyAnimator x(float value) {
        animateProperty(X, value);
        return this;
    }

    public ViewPropertyAnimator xBy(float value) {
        animatePropertyBy(X, value);
        return this;
    }

    public ViewPropertyAnimator y(float value) {
        animateProperty(Y, value);
        return this;
    }

    public ViewPropertyAnimator yBy(float value) {
        animatePropertyBy(Y, value);
        return this;
    }

    public ViewPropertyAnimator z(float value) {
        animateProperty(Z, value);
        return this;
    }

    public ViewPropertyAnimator zBy(float value) {
        animatePropertyBy(Z, value);
        return this;
    }

    public ViewPropertyAnimator rotation(float value) {
        animateProperty(ROTATION, value);
        return this;
    }

    public ViewPropertyAnimator rotationBy(float value) {
        animatePropertyBy(ROTATION, value);
        return this;
    }

    public ViewPropertyAnimator rotationX(float value) {
        animateProperty(ROTATION_X, value);
        return this;
    }

    public ViewPropertyAnimator rotationXBy(float value) {
        animatePropertyBy(ROTATION_X, value);
        return this;
    }

    public ViewPropertyAnimator rotationY(float value) {
        animateProperty(ROTATION_Y, value);
        return this;
    }

    public ViewPropertyAnimator rotationYBy(float value) {
        animatePropertyBy(ROTATION_Y, value);
        return this;
    }

    public ViewPropertyAnimator translationX(float value) {
        animateProperty(TRANSLATION_X, value);
        return this;
    }

    public ViewPropertyAnimator translationXBy(float value) {
        animatePropertyBy(TRANSLATION_X, value);
        return this;
    }

    public ViewPropertyAnimator translationY(float value) {
        animateProperty(TRANSLATION_Y, value);
        return this;
    }

    public ViewPropertyAnimator translationYBy(float value) {
        animatePropertyBy(TRANSLATION_Y, value);
        return this;
    }

    public ViewPropertyAnimator translationZ(float value) {
        animateProperty(TRANSLATION_Z, value);
        return this;
    }

    public ViewPropertyAnimator translationZBy(float value) {
        animatePropertyBy(TRANSLATION_Z, value);
        return this;
    }

    public ViewPropertyAnimator scaleX(float value) {
        animateProperty(SCALE_X, value);
        return this;
    }

    public ViewPropertyAnimator scaleXBy(float value) {
        animatePropertyBy(SCALE_X, value);
        return this;
    }

    public ViewPropertyAnimator scaleY(float value) {
        animateProperty(SCALE_Y, value);
        return this;
    }

    public ViewPropertyAnimator scaleYBy(float value) {
        animatePropertyBy(SCALE_Y, value);
        return this;
    }

    public ViewPropertyAnimator alpha(float value) {
        animateProperty(ALPHA, value);
        return this;
    }

    public ViewPropertyAnimator alphaBy(float value) {
        animatePropertyBy(ALPHA, value);
        return this;
    }

    public ViewPropertyAnimator withLayer() {
        this.mPendingSetupAction = new Runnable() {
            public void run() {
                ViewPropertyAnimator.this.mView.setLayerType(ViewPropertyAnimator.TRANSLATION_Y, null);
                if (ViewPropertyAnimator.this.mView.isAttachedToWindow()) {
                    ViewPropertyAnimator.this.mView.buildLayer();
                }
            }
        };
        this.mPendingCleanupAction = new AnonymousClass3(this.mView.getLayerType());
        if (this.mAnimatorSetupMap == null) {
            this.mAnimatorSetupMap = new HashMap();
        }
        if (this.mAnimatorCleanupMap == null) {
            this.mAnimatorCleanupMap = new HashMap();
        }
        return this;
    }

    public ViewPropertyAnimator withStartAction(Runnable runnable) {
        this.mPendingOnStartAction = runnable;
        if (runnable != null && this.mAnimatorOnStartMap == null) {
            this.mAnimatorOnStartMap = new HashMap();
        }
        return this;
    }

    public ViewPropertyAnimator withEndAction(Runnable runnable) {
        this.mPendingOnEndAction = runnable;
        if (runnable != null && this.mAnimatorOnEndMap == null) {
            this.mAnimatorOnEndMap = new HashMap();
        }
        return this;
    }

    boolean hasActions() {
        if (this.mPendingSetupAction == null && this.mPendingCleanupAction == null && this.mPendingOnStartAction == null && this.mPendingOnEndAction == null) {
            return false;
        }
        return true;
    }

    private void startAnimation() {
        if (this.mRTBackend == null || !this.mRTBackend.startAnimation(this)) {
            this.mView.setHasTransientState(true);
            float[] fArr = new float[TRANSLATION_X];
            fArr[NONE] = LayoutParams.BRIGHTNESS_OVERRIDE_FULL;
            ValueAnimator animator = ValueAnimator.ofFloat(fArr);
            ArrayList<NameValuesHolder> nameValueList = (ArrayList) this.mPendingAnimations.clone();
            this.mPendingAnimations.clear();
            int propertyMask = NONE;
            int propertyCount = nameValueList.size();
            for (int i = NONE; i < propertyCount; i += TRANSLATION_X) {
                propertyMask |= ((NameValuesHolder) nameValueList.get(i)).mNameConstant;
            }
            this.mAnimatorMap.put(animator, new PropertyBundle(propertyMask, nameValueList));
            if (this.mPendingSetupAction != null) {
                this.mAnimatorSetupMap.put(animator, this.mPendingSetupAction);
                this.mPendingSetupAction = null;
            }
            if (this.mPendingCleanupAction != null) {
                this.mAnimatorCleanupMap.put(animator, this.mPendingCleanupAction);
                this.mPendingCleanupAction = null;
            }
            if (this.mPendingOnStartAction != null) {
                this.mAnimatorOnStartMap.put(animator, this.mPendingOnStartAction);
                this.mPendingOnStartAction = null;
            }
            if (this.mPendingOnEndAction != null) {
                this.mAnimatorOnEndMap.put(animator, this.mPendingOnEndAction);
                this.mPendingOnEndAction = null;
            }
            animator.addUpdateListener(this.mAnimatorEventListener);
            animator.addListener(this.mAnimatorEventListener);
            if (this.mStartDelaySet) {
                animator.setStartDelay(this.mStartDelay);
            }
            if (this.mDurationSet) {
                animator.setDuration(this.mDuration);
            }
            if (this.mInterpolatorSet) {
                animator.setInterpolator(this.mInterpolator);
            }
            animator.start();
        }
    }

    private void animateProperty(int constantName, float toValue) {
        float fromValue = getValue(constantName);
        animatePropertyBy(constantName, fromValue, toValue - fromValue);
    }

    private void animatePropertyBy(int constantName, float byValue) {
        animatePropertyBy(constantName, getValue(constantName), byValue);
    }

    private void animatePropertyBy(int constantName, float startValue, float byValue) {
        if (this.mAnimatorMap.size() > 0) {
            Animator animatorToCancel = null;
            for (Animator runningAnim : this.mAnimatorMap.keySet()) {
                PropertyBundle bundle = (PropertyBundle) this.mAnimatorMap.get(runningAnim);
                if (bundle.cancel(constantName) && bundle.mPropertyMask == 0) {
                    animatorToCancel = runningAnim;
                    break;
                }
            }
            if (animatorToCancel != null) {
                animatorToCancel.cancel();
            }
        }
        this.mPendingAnimations.add(new NameValuesHolder(constantName, startValue, byValue));
        this.mView.removeCallbacks(this.mAnimationStarter);
        this.mView.postOnAnimation(this.mAnimationStarter);
    }

    private void setValue(int propertyConstant, float value) {
        TransformationInfo info = this.mView.mTransformationInfo;
        RenderNode renderNode = this.mView.mRenderNode;
        switch (propertyConstant) {
            case TRANSLATION_X /*1*/:
                renderNode.setTranslationX(value);
            case TRANSLATION_Y /*2*/:
                renderNode.setTranslationY(value);
            case TRANSLATION_Z /*4*/:
                renderNode.setTranslationZ(value);
            case SCALE_X /*8*/:
                renderNode.setScaleX(value);
            case SCALE_Y /*16*/:
                renderNode.setScaleY(value);
            case ROTATION /*32*/:
                renderNode.setRotation(value);
            case ROTATION_X /*64*/:
                renderNode.setRotationX(value);
            case ROTATION_Y /*128*/:
                renderNode.setRotationY(value);
            case X /*256*/:
                renderNode.setTranslationX(value - ((float) this.mView.mLeft));
            case Y /*512*/:
                renderNode.setTranslationY(value - ((float) this.mView.mTop));
            case Z /*1024*/:
                renderNode.setTranslationZ(value - renderNode.getElevation());
            case ALPHA /*2048*/:
                info.mAlpha = value;
                renderNode.setAlpha(value);
            default:
        }
    }

    private float getValue(int propertyConstant) {
        RenderNode node = this.mView.mRenderNode;
        switch (propertyConstant) {
            case TRANSLATION_X /*1*/:
                return node.getTranslationX();
            case TRANSLATION_Y /*2*/:
                return node.getTranslationY();
            case TRANSLATION_Z /*4*/:
                return node.getTranslationZ();
            case SCALE_X /*8*/:
                return node.getScaleX();
            case SCALE_Y /*16*/:
                return node.getScaleY();
            case ROTATION /*32*/:
                return node.getRotation();
            case ROTATION_X /*64*/:
                return node.getRotationX();
            case ROTATION_Y /*128*/:
                return node.getRotationY();
            case X /*256*/:
                return ((float) this.mView.mLeft) + node.getTranslationX();
            case Y /*512*/:
                return ((float) this.mView.mTop) + node.getTranslationY();
            case Z /*1024*/:
                return node.getElevation() + node.getTranslationZ();
            case ALPHA /*2048*/:
                return this.mView.mTransformationInfo.mAlpha;
            default:
                return 0.0f;
        }
    }
}
