package android.graphics.drawable;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.animation.TimeInterpolator;
import android.animation.ValueAnimator;
import android.app.ActivityThread;
import android.app.Application;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Insets;
import android.graphics.Outline;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.drawable.Animatable2;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.VectorDrawable;
import android.util.ArrayMap;
import android.util.AttributeSet;
import android.util.IntArray;
import android.util.Log;
import android.util.LongArray;
import android.util.PathParser;
import android.util.Property;
import android.view.Choreographer;
import android.view.DisplayListCanvas;
import android.view.RenderNode;
import android.view.RenderNodeAnimatorSetHelper;
import com.android.internal.R;
import com.android.internal.util.VirtualRefBasePtr;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Iterator;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public class AnimatedVectorDrawable extends Drawable implements Animatable2 {
    private static final String ANIMATED_VECTOR = "animated-vector";
    private static final boolean DBG_ANIMATION_VECTOR_DRAWABLE = false;
    private static final String LOGTAG = "AnimatedVectorDrawable";
    private static final String TARGET = "target";
    /* access modifiers changed from: private */
    public AnimatedVectorDrawableState mAnimatedVectorState;
    /* access modifiers changed from: private */
    public ArrayList<Animatable2.AnimationCallback> mAnimationCallbacks;
    private Animator.AnimatorListener mAnimatorListener;
    private VectorDrawableAnimator mAnimatorSet;
    private AnimatorSet mAnimatorSetFromXml;
    private final Drawable.Callback mCallback;
    private boolean mMutated;
    private Resources mRes;

    private static class AnimatedVectorDrawableState extends Drawable.ConstantState {
        ArrayList<Animator> mAnimators;
        int mChangingConfigurations;
        ArrayList<PendingAnimator> mPendingAnims;
        /* access modifiers changed from: private */
        public final boolean mShouldIgnoreInvalidAnim = AnimatedVectorDrawable.shouldIgnoreInvalidAnimation();
        ArrayMap<Animator, String> mTargetNameMap;
        VectorDrawable mVectorDrawable;

        private static class PendingAnimator {
            public final int animResId;
            public final float pathErrorScale;
            public final String target;

            public PendingAnimator(int animResId2, float pathErrorScale2, String target2) {
                this.animResId = animResId2;
                this.pathErrorScale = pathErrorScale2;
                this.target = target2;
            }

            public Animator newInstance(Resources res, Resources.Theme theme) {
                return AnimatorInflater.loadAnimator(res, theme, this.animResId, this.pathErrorScale);
            }
        }

        public AnimatedVectorDrawableState(AnimatedVectorDrawableState copy, Drawable.Callback owner, Resources res) {
            if (copy != null) {
                this.mChangingConfigurations = copy.mChangingConfigurations;
                if (copy.mVectorDrawable != null) {
                    Drawable.ConstantState cs = copy.mVectorDrawable.getConstantState();
                    if (res != null) {
                        this.mVectorDrawable = (VectorDrawable) cs.newDrawable(res);
                    } else {
                        this.mVectorDrawable = (VectorDrawable) cs.newDrawable();
                    }
                    this.mVectorDrawable = (VectorDrawable) this.mVectorDrawable.mutate();
                    this.mVectorDrawable.setCallback(owner);
                    this.mVectorDrawable.setLayoutDirection(copy.mVectorDrawable.getLayoutDirection());
                    this.mVectorDrawable.setBounds(copy.mVectorDrawable.getBounds());
                    this.mVectorDrawable.setAllowCaching(false);
                }
                if (copy.mAnimators != null) {
                    this.mAnimators = new ArrayList<>(copy.mAnimators);
                }
                if (copy.mTargetNameMap != null) {
                    this.mTargetNameMap = new ArrayMap<>(copy.mTargetNameMap);
                }
                if (copy.mPendingAnims != null) {
                    this.mPendingAnims = new ArrayList<>(copy.mPendingAnims);
                    return;
                }
                return;
            }
            this.mVectorDrawable = new VectorDrawable();
        }

        public boolean canApplyTheme() {
            return (this.mVectorDrawable != null && this.mVectorDrawable.canApplyTheme()) || this.mPendingAnims != null || super.canApplyTheme();
        }

        public Drawable newDrawable() {
            return new AnimatedVectorDrawable(this, null);
        }

        public Drawable newDrawable(Resources res) {
            return new AnimatedVectorDrawable(this, res);
        }

        public int getChangingConfigurations() {
            return this.mChangingConfigurations;
        }

        public void addPendingAnimator(int resId, float pathErrorScale, String target) {
            if (this.mPendingAnims == null) {
                this.mPendingAnims = new ArrayList<>(1);
            }
            this.mPendingAnims.add(new PendingAnimator(resId, pathErrorScale, target));
        }

        public void addTargetAnimator(String targetName, Animator animator) {
            if (this.mAnimators == null) {
                this.mAnimators = new ArrayList<>(1);
                this.mTargetNameMap = new ArrayMap<>(1);
            }
            this.mAnimators.add(animator);
            this.mTargetNameMap.put(animator, targetName);
        }

        public void prepareLocalAnimators(AnimatorSet animatorSet, Resources res) {
            if (this.mPendingAnims != null) {
                if (res != null) {
                    inflatePendingAnimators(res, null);
                } else {
                    Log.e(AnimatedVectorDrawable.LOGTAG, "Failed to load animators. Either the AnimatedVectorDrawable must be created using a Resources object or applyTheme() must be called with a non-null Theme object.");
                }
                this.mPendingAnims = null;
            }
            int count = this.mAnimators == null ? 0 : this.mAnimators.size();
            if (count > 0) {
                AnimatorSet.Builder builder = animatorSet.play(prepareLocalAnimator(0));
                for (int i = 1; i < count; i++) {
                    builder.with(prepareLocalAnimator(i));
                }
            }
        }

        private Animator prepareLocalAnimator(int index) {
            Animator animator = this.mAnimators.get(index);
            Animator localAnimator = animator.clone();
            String targetName = this.mTargetNameMap.get(animator);
            Object target = this.mVectorDrawable.getTargetByName(targetName);
            if (!this.mShouldIgnoreInvalidAnim) {
                if (target == null) {
                    throw new IllegalStateException("Target with the name \"" + targetName + "\" cannot be found in the VectorDrawable to be animated.");
                } else if (!(target instanceof VectorDrawable.VectorDrawableState) && !(target instanceof VectorDrawable.VObject)) {
                    throw new UnsupportedOperationException("Target should be either VGroup, VPath, or ConstantState, " + target.getClass() + " is not supported");
                }
            }
            localAnimator.setTarget(target);
            return localAnimator;
        }

        public void inflatePendingAnimators(Resources res, Resources.Theme t) {
            ArrayList<PendingAnimator> pendingAnims = this.mPendingAnims;
            if (pendingAnims != null) {
                this.mPendingAnims = null;
                int count = pendingAnims.size();
                for (int i = 0; i < count; i++) {
                    PendingAnimator pendingAnimator = pendingAnims.get(i);
                    Animator animator = pendingAnimator.newInstance(res, t);
                    AnimatedVectorDrawable.updateAnimatorProperty(animator, pendingAnimator.target, this.mVectorDrawable, this.mShouldIgnoreInvalidAnim);
                    addTargetAnimator(pendingAnimator.target, animator);
                }
            }
        }
    }

    private interface VectorDrawableAnimator {
        boolean canReverse();

        void end();

        void init(AnimatorSet animatorSet);

        boolean isInfinite();

        boolean isRunning();

        boolean isStarted();

        void onDraw(Canvas canvas);

        void pause();

        void removeListener(Animator.AnimatorListener animatorListener);

        void reset();

        void resume();

        void reverse();

        void setListener(Animator.AnimatorListener animatorListener);

        void start();
    }

    public static class VectorDrawableAnimatorRT implements VectorDrawableAnimator {
        private static final int END_ANIMATION = 4;
        private static final int MAX_SAMPLE_POINTS = 300;
        private static final int RESET_ANIMATION = 3;
        private static final int REVERSE_ANIMATION = 2;
        private static final int START_ANIMATION = 1;
        private boolean mContainsSequentialAnimators = false;
        private final AnimatedVectorDrawable mDrawable;
        private boolean mInitialized = false;
        private boolean mIsInfinite = false;
        private boolean mIsReversible = false;
        private int mLastListenerId = 0;
        private WeakReference<RenderNode> mLastSeenTarget = null;
        /* access modifiers changed from: private */
        public Animator.AnimatorListener mListener = null;
        /* access modifiers changed from: private */
        public final IntArray mPendingAnimationActions = new IntArray();
        private long mSetPtr = 0;
        private final VirtualRefBasePtr mSetRefBasePtr;
        private final LongArray mStartDelays = new LongArray();
        private boolean mStarted = false;
        private PropertyValuesHolder.PropertyValues mTmpValues = new PropertyValuesHolder.PropertyValues();

        VectorDrawableAnimatorRT(AnimatedVectorDrawable drawable) {
            this.mDrawable = drawable;
            this.mSetPtr = AnimatedVectorDrawable.nCreateAnimatorSet();
            this.mSetRefBasePtr = new VirtualRefBasePtr(this.mSetPtr);
        }

        public void init(AnimatorSet set) {
            if (!this.mInitialized) {
                parseAnimatorSet(set, 0);
                AnimatedVectorDrawable.nSetVectorDrawableTarget(this.mSetPtr, this.mDrawable.mAnimatedVectorState.mVectorDrawable.getNativeTree());
                this.mInitialized = true;
                this.mIsInfinite = set.getTotalDuration() == -1;
                this.mIsReversible = true;
                if (this.mContainsSequentialAnimators) {
                    this.mIsReversible = false;
                } else {
                    for (int i = 0; i < this.mStartDelays.size(); i++) {
                        if (this.mStartDelays.get(i) > 0) {
                            this.mIsReversible = false;
                            return;
                        }
                    }
                }
                return;
            }
            throw new UnsupportedOperationException("VectorDrawableAnimator cannot be re-initialized");
        }

        private void parseAnimatorSet(AnimatorSet set, long startTime) {
            ArrayList<Animator> animators = set.getChildAnimations();
            boolean playTogether = set.shouldPlayTogether();
            for (int i = 0; i < animators.size(); i++) {
                Animator animator = animators.get(i);
                if (animator instanceof AnimatorSet) {
                    parseAnimatorSet((AnimatorSet) animator, startTime);
                } else if (animator instanceof ObjectAnimator) {
                    createRTAnimator((ObjectAnimator) animator, startTime);
                }
                if (!playTogether) {
                    startTime += animator.getTotalDuration();
                    this.mContainsSequentialAnimators = true;
                }
            }
        }

        private void createRTAnimator(ObjectAnimator animator, long startTime) {
            PropertyValuesHolder[] values = animator.getValues();
            Object target = animator.getTarget();
            if (target instanceof VectorDrawable.VGroup) {
                createRTAnimatorForGroup(values, animator, (VectorDrawable.VGroup) target, startTime);
            } else if (target instanceof VectorDrawable.VPath) {
                for (PropertyValuesHolder propertyValues : values) {
                    propertyValues.getPropertyValues(this.mTmpValues);
                    if ((this.mTmpValues.endValue instanceof PathParser.PathData) && this.mTmpValues.propertyName.equals("pathData")) {
                        createRTAnimatorForPath(animator, (VectorDrawable.VPath) target, startTime);
                    } else if (target instanceof VectorDrawable.VFullPath) {
                        createRTAnimatorForFullPath(animator, (VectorDrawable.VFullPath) target, startTime);
                    } else if (!this.mDrawable.mAnimatedVectorState.mShouldIgnoreInvalidAnim) {
                        throw new IllegalArgumentException("ClipPath only supports PathData property");
                    }
                }
            } else if ((target instanceof VectorDrawable.VectorDrawableState) != 0) {
                createRTAnimatorForRootGroup(values, animator, (VectorDrawable.VectorDrawableState) target, startTime);
            }
        }

        private void createRTAnimatorForGroup(PropertyValuesHolder[] values, ObjectAnimator animator, VectorDrawable.VGroup target, long startTime) {
            PropertyValuesHolder[] propertyValuesHolderArr = values;
            long nativePtr = target.getNativePtr();
            int i = 0;
            while (true) {
                int i2 = i;
                if (i2 < propertyValuesHolderArr.length) {
                    propertyValuesHolderArr[i2].getPropertyValues(this.mTmpValues);
                    int propertyId = VectorDrawable.VGroup.getPropertyIndex(this.mTmpValues.propertyName);
                    if ((this.mTmpValues.type == Float.class || this.mTmpValues.type == Float.TYPE) && propertyId >= 0) {
                        long propertyPtr = AnimatedVectorDrawable.nCreateGroupPropertyHolder(nativePtr, propertyId, ((Float) this.mTmpValues.startValue).floatValue(), ((Float) this.mTmpValues.endValue).floatValue());
                        if (this.mTmpValues.dataSource != null) {
                            float[] dataPoints = createFloatDataPoints(this.mTmpValues.dataSource, animator.getDuration());
                            AnimatedVectorDrawable.nSetPropertyHolderData(propertyPtr, dataPoints, dataPoints.length);
                        }
                        createNativeChildAnimator(propertyPtr, startTime, animator);
                    }
                    i = i2 + 1;
                } else {
                    return;
                }
            }
        }

        private void createRTAnimatorForPath(ObjectAnimator animator, VectorDrawable.VPath target, long startTime) {
            createNativeChildAnimator(AnimatedVectorDrawable.nCreatePathDataPropertyHolder(target.getNativePtr(), ((PathParser.PathData) this.mTmpValues.startValue).getNativePtr(), ((PathParser.PathData) this.mTmpValues.endValue).getNativePtr()), startTime, animator);
        }

        private void createRTAnimatorForFullPath(ObjectAnimator animator, VectorDrawable.VFullPath target, long startTime) {
            long propertyPtr;
            int propertyId = target.getPropertyIndex(this.mTmpValues.propertyName);
            long nativePtr = target.getNativePtr();
            if (this.mTmpValues.type == Float.class || this.mTmpValues.type == Float.TYPE) {
                if (propertyId >= 0) {
                    propertyPtr = AnimatedVectorDrawable.nCreatePathPropertyHolder(nativePtr, propertyId, ((Float) this.mTmpValues.startValue).floatValue(), ((Float) this.mTmpValues.endValue).floatValue());
                    if (this.mTmpValues.dataSource != null) {
                        float[] dataPoints = createFloatDataPoints(this.mTmpValues.dataSource, animator.getDuration());
                        AnimatedVectorDrawable.nSetPropertyHolderData(propertyPtr, dataPoints, dataPoints.length);
                    }
                } else if (!this.mDrawable.mAnimatedVectorState.mShouldIgnoreInvalidAnim) {
                    throw new IllegalArgumentException("Property: " + this.mTmpValues.propertyName + " is not supported for FullPath");
                } else {
                    return;
                }
            } else if (this.mTmpValues.type == Integer.class || this.mTmpValues.type == Integer.TYPE) {
                propertyPtr = AnimatedVectorDrawable.nCreatePathColorPropertyHolder(nativePtr, propertyId, ((Integer) this.mTmpValues.startValue).intValue(), ((Integer) this.mTmpValues.endValue).intValue());
                if (this.mTmpValues.dataSource != null) {
                    int[] dataPoints2 = createIntDataPoints(this.mTmpValues.dataSource, animator.getDuration());
                    AnimatedVectorDrawable.nSetPropertyHolderData(propertyPtr, dataPoints2, dataPoints2.length);
                }
            } else if (!this.mDrawable.mAnimatedVectorState.mShouldIgnoreInvalidAnim) {
                throw new UnsupportedOperationException("Unsupported type: " + this.mTmpValues.type + ". Only float, int or PathData value is supported for Paths.");
            } else {
                return;
            }
            createNativeChildAnimator(propertyPtr, startTime, animator);
        }

        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r3v7, resolved type: java.lang.Object} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v15, resolved type: java.lang.Float} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r3v9, resolved type: java.lang.Object} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v8, resolved type: java.lang.Float} */
        /* JADX WARNING: Multi-variable type inference failed */
        private void createRTAnimatorForRootGroup(PropertyValuesHolder[] values, ObjectAnimator animator, VectorDrawable.VectorDrawableState target, long startTime) {
            PropertyValuesHolder[] propertyValuesHolderArr = values;
            long nativePtr = target.getNativeRenderer();
            if (animator.getPropertyName().equals("alpha")) {
                Float startValue = null;
                Float endValue = null;
                int i = 0;
                while (true) {
                    if (i >= propertyValuesHolderArr.length) {
                        break;
                    }
                    propertyValuesHolderArr[i].getPropertyValues(this.mTmpValues);
                    if (this.mTmpValues.propertyName.equals("alpha")) {
                        startValue = this.mTmpValues.startValue;
                        endValue = this.mTmpValues.endValue;
                        break;
                    }
                    i++;
                }
                Float startValue2 = startValue;
                Float endValue2 = endValue;
                if (startValue2 != null || endValue2 != null) {
                    long propertyPtr = AnimatedVectorDrawable.nCreateRootAlphaPropertyHolder(nativePtr, startValue2.floatValue(), endValue2.floatValue());
                    if (this.mTmpValues.dataSource != null) {
                        float[] dataPoints = createFloatDataPoints(this.mTmpValues.dataSource, animator.getDuration());
                        AnimatedVectorDrawable.nSetPropertyHolderData(propertyPtr, dataPoints, dataPoints.length);
                    }
                    createNativeChildAnimator(propertyPtr, startTime, animator);
                } else if (!this.mDrawable.mAnimatedVectorState.mShouldIgnoreInvalidAnim) {
                    throw new UnsupportedOperationException("No alpha values are specified");
                }
            } else if (!this.mDrawable.mAnimatedVectorState.mShouldIgnoreInvalidAnim) {
                throw new UnsupportedOperationException("Only alpha is supported for root group");
            }
        }

        private static int getFrameCount(long duration) {
            int numAnimFrames = Math.max(2, (int) Math.ceil(((double) duration) / ((double) ((int) (Choreographer.getInstance().getFrameIntervalNanos() / 1000000)))));
            if (numAnimFrames <= 300) {
                return numAnimFrames;
            }
            Log.w(AnimatedVectorDrawable.LOGTAG, "Duration for the animation is too long :" + duration + ", the animation will subsample the keyframe or path data.");
            return 300;
        }

        private static float[] createFloatDataPoints(PropertyValuesHolder.PropertyValues.DataSource dataSource, long duration) {
            int numAnimFrames = getFrameCount(duration);
            float[] values = new float[numAnimFrames];
            float lastFrame = (float) (numAnimFrames - 1);
            for (int i = 0; i < numAnimFrames; i++) {
                values[i] = ((Float) dataSource.getValueAtFraction(((float) i) / lastFrame)).floatValue();
            }
            return values;
        }

        private static int[] createIntDataPoints(PropertyValuesHolder.PropertyValues.DataSource dataSource, long duration) {
            int numAnimFrames = getFrameCount(duration);
            int[] values = new int[numAnimFrames];
            float lastFrame = (float) (numAnimFrames - 1);
            for (int i = 0; i < numAnimFrames; i++) {
                values[i] = ((Integer) dataSource.getValueAtFraction(((float) i) / lastFrame)).intValue();
            }
            return values;
        }

        private void createNativeChildAnimator(long propertyPtr, long extraDelay, ObjectAnimator animator) {
            long duration = animator.getDuration();
            int repeatCount = animator.getRepeatCount();
            TimeInterpolator interpolator = animator.getInterpolator();
            long nativeInterpolator = RenderNodeAnimatorSetHelper.createNativeInterpolator(interpolator, duration);
            long startDelay = (long) (((float) (extraDelay + animator.getStartDelay())) * ValueAnimator.getDurationScale());
            long duration2 = (long) (((float) duration) * ValueAnimator.getDurationScale());
            this.mStartDelays.add(startDelay);
            long j = startDelay;
            TimeInterpolator timeInterpolator = interpolator;
            AnimatedVectorDrawable.nAddAnimator(this.mSetPtr, propertyPtr, nativeInterpolator, startDelay, duration2, repeatCount, animator.getRepeatMode());
        }

        /* access modifiers changed from: protected */
        public void recordLastSeenTarget(DisplayListCanvas canvas) {
            RenderNode node = RenderNodeAnimatorSetHelper.getTarget(canvas);
            this.mLastSeenTarget = new WeakReference<>(node);
            if ((this.mInitialized || this.mPendingAnimationActions.size() > 0) && useTarget(node)) {
                for (int i = 0; i < this.mPendingAnimationActions.size(); i++) {
                    handlePendingAction(this.mPendingAnimationActions.get(i));
                }
                this.mPendingAnimationActions.clear();
            }
        }

        private void handlePendingAction(int pendingAnimationAction) {
            if (pendingAnimationAction == 1) {
                startAnimation();
            } else if (pendingAnimationAction == 2) {
                reverseAnimation();
            } else if (pendingAnimationAction == 3) {
                resetAnimation();
            } else if (pendingAnimationAction == 4) {
                endAnimation();
            } else {
                throw new UnsupportedOperationException("Animation action " + pendingAnimationAction + "is not supported");
            }
        }

        private boolean useLastSeenTarget() {
            if (this.mLastSeenTarget != null) {
                return useTarget((RenderNode) this.mLastSeenTarget.get());
            }
            return false;
        }

        private boolean useTarget(RenderNode target) {
            if (target == null || !target.isAttached()) {
                return false;
            }
            target.registerVectorDrawableAnimator(this);
            return true;
        }

        private void invalidateOwningView() {
            this.mDrawable.invalidateSelf();
        }

        private void addPendingAction(int pendingAnimationAction) {
            invalidateOwningView();
            this.mPendingAnimationActions.add(pendingAnimationAction);
        }

        public void start() {
            if (this.mInitialized) {
                if (useLastSeenTarget()) {
                    startAnimation();
                } else {
                    addPendingAction(1);
                }
            }
        }

        public void end() {
            if (this.mInitialized) {
                if (useLastSeenTarget()) {
                    endAnimation();
                } else {
                    addPendingAction(4);
                }
            }
        }

        public void reset() {
            if (this.mInitialized) {
                if (useLastSeenTarget()) {
                    resetAnimation();
                } else {
                    addPendingAction(3);
                }
            }
        }

        public void reverse() {
            if (this.mIsReversible && this.mInitialized) {
                if (useLastSeenTarget()) {
                    reverseAnimation();
                } else {
                    addPendingAction(2);
                }
            }
        }

        private void startAnimation() {
            this.mStarted = true;
            long j = this.mSetPtr;
            int i = this.mLastListenerId + 1;
            this.mLastListenerId = i;
            AnimatedVectorDrawable.nStart(j, this, i);
            invalidateOwningView();
            if (this.mListener != null) {
                this.mListener.onAnimationStart(null);
            }
        }

        private void endAnimation() {
            AnimatedVectorDrawable.nEnd(this.mSetPtr);
            invalidateOwningView();
        }

        private void resetAnimation() {
            AnimatedVectorDrawable.nReset(this.mSetPtr);
            invalidateOwningView();
        }

        private void reverseAnimation() {
            this.mStarted = true;
            long j = this.mSetPtr;
            int i = this.mLastListenerId + 1;
            this.mLastListenerId = i;
            AnimatedVectorDrawable.nReverse(j, this, i);
            invalidateOwningView();
            if (this.mListener != null) {
                this.mListener.onAnimationStart(null);
            }
        }

        public long getAnimatorNativePtr() {
            return this.mSetPtr;
        }

        public boolean canReverse() {
            return this.mIsReversible;
        }

        public boolean isStarted() {
            return this.mStarted;
        }

        public boolean isRunning() {
            if (!this.mInitialized) {
                return false;
            }
            return this.mStarted;
        }

        public void setListener(Animator.AnimatorListener listener) {
            this.mListener = listener;
        }

        public void removeListener(Animator.AnimatorListener listener) {
            this.mListener = null;
        }

        public void onDraw(Canvas canvas) {
            if (canvas.isHardwareAccelerated()) {
                recordLastSeenTarget((DisplayListCanvas) canvas);
            }
        }

        public boolean isInfinite() {
            return this.mIsInfinite;
        }

        public void pause() {
        }

        public void resume() {
        }

        private void onAnimationEnd(int listenerId) {
            if (listenerId == this.mLastListenerId) {
                this.mStarted = false;
                invalidateOwningView();
                if (this.mListener != null) {
                    this.mListener.onAnimationEnd(null);
                }
            }
        }

        private static void callOnFinished(VectorDrawableAnimatorRT set, int id) {
            set.onAnimationEnd(id);
        }

        /* access modifiers changed from: private */
        public void transferPendingActions(VectorDrawableAnimator animatorSet) {
            for (int i = 0; i < this.mPendingAnimationActions.size(); i++) {
                int pendingAction = this.mPendingAnimationActions.get(i);
                if (pendingAction == 1) {
                    animatorSet.start();
                } else if (pendingAction == 4) {
                    animatorSet.end();
                } else if (pendingAction == 2) {
                    animatorSet.reverse();
                } else if (pendingAction == 3) {
                    animatorSet.reset();
                } else {
                    throw new UnsupportedOperationException("Animation action " + pendingAction + "is not supported");
                }
            }
            this.mPendingAnimationActions.clear();
        }
    }

    private static class VectorDrawableAnimatorUI implements VectorDrawableAnimator {
        private final Drawable mDrawable;
        private boolean mIsInfinite = false;
        private ArrayList<Animator.AnimatorListener> mListenerArray = null;
        private AnimatorSet mSet = null;

        VectorDrawableAnimatorUI(AnimatedVectorDrawable drawable) {
            this.mDrawable = drawable;
        }

        public void init(AnimatorSet set) {
            if (this.mSet == null) {
                this.mSet = set.clone();
                int i = 0;
                this.mIsInfinite = this.mSet.getTotalDuration() == -1;
                if (this.mListenerArray != null && !this.mListenerArray.isEmpty()) {
                    while (true) {
                        int i2 = i;
                        if (i2 < this.mListenerArray.size()) {
                            this.mSet.addListener(this.mListenerArray.get(i2));
                            i = i2 + 1;
                        } else {
                            this.mListenerArray.clear();
                            this.mListenerArray = null;
                            return;
                        }
                    }
                }
            } else {
                throw new UnsupportedOperationException("VectorDrawableAnimator cannot be re-initialized");
            }
        }

        public void start() {
            if (this.mSet != null && !this.mSet.isStarted()) {
                this.mSet.start();
                invalidateOwningView();
            }
        }

        public void end() {
            if (this.mSet != null) {
                this.mSet.end();
            }
        }

        public void reset() {
            if (this.mSet != null) {
                start();
                this.mSet.cancel();
            }
        }

        public void reverse() {
            if (this.mSet != null) {
                this.mSet.reverse();
                invalidateOwningView();
            }
        }

        public boolean canReverse() {
            return this.mSet != null && this.mSet.canReverse();
        }

        public void setListener(Animator.AnimatorListener listener) {
            if (this.mSet == null) {
                if (this.mListenerArray == null) {
                    this.mListenerArray = new ArrayList<>();
                }
                this.mListenerArray.add(listener);
                return;
            }
            this.mSet.addListener(listener);
        }

        public void removeListener(Animator.AnimatorListener listener) {
            if (this.mSet != null) {
                this.mSet.removeListener(listener);
            } else if (this.mListenerArray != null) {
                this.mListenerArray.remove(listener);
            }
        }

        public void onDraw(Canvas canvas) {
            if (this.mSet != null && this.mSet.isStarted()) {
                invalidateOwningView();
            }
        }

        public boolean isStarted() {
            return this.mSet != null && this.mSet.isStarted();
        }

        public boolean isRunning() {
            return this.mSet != null && this.mSet.isRunning();
        }

        public boolean isInfinite() {
            return this.mIsInfinite;
        }

        public void pause() {
            if (this.mSet != null) {
                this.mSet.pause();
            }
        }

        public void resume() {
            if (this.mSet != null) {
                this.mSet.resume();
            }
        }

        private void invalidateOwningView() {
            this.mDrawable.invalidateSelf();
        }
    }

    /* access modifiers changed from: private */
    public static native void nAddAnimator(long j, long j2, long j3, long j4, long j5, int i, int i2);

    /* access modifiers changed from: private */
    public static native long nCreateAnimatorSet();

    /* access modifiers changed from: private */
    public static native long nCreateGroupPropertyHolder(long j, int i, float f, float f2);

    /* access modifiers changed from: private */
    public static native long nCreatePathColorPropertyHolder(long j, int i, int i2, int i3);

    /* access modifiers changed from: private */
    public static native long nCreatePathDataPropertyHolder(long j, long j2, long j3);

    /* access modifiers changed from: private */
    public static native long nCreatePathPropertyHolder(long j, int i, float f, float f2);

    /* access modifiers changed from: private */
    public static native long nCreateRootAlphaPropertyHolder(long j, float f, float f2);

    /* access modifiers changed from: private */
    public static native void nEnd(long j);

    /* access modifiers changed from: private */
    public static native void nReset(long j);

    /* access modifiers changed from: private */
    public static native void nReverse(long j, VectorDrawableAnimatorRT vectorDrawableAnimatorRT, int i);

    /* access modifiers changed from: private */
    public static native void nSetPropertyHolderData(long j, float[] fArr, int i);

    /* access modifiers changed from: private */
    public static native void nSetPropertyHolderData(long j, int[] iArr, int i);

    /* access modifiers changed from: private */
    public static native void nSetVectorDrawableTarget(long j, long j2);

    /* access modifiers changed from: private */
    public static native void nStart(long j, VectorDrawableAnimatorRT vectorDrawableAnimatorRT, int i);

    public AnimatedVectorDrawable() {
        this(null, null);
    }

    private AnimatedVectorDrawable(AnimatedVectorDrawableState state, Resources res) {
        this.mAnimatorSetFromXml = null;
        this.mAnimationCallbacks = null;
        this.mAnimatorListener = null;
        this.mCallback = new Drawable.Callback() {
            public void invalidateDrawable(Drawable who) {
                AnimatedVectorDrawable.this.invalidateSelf();
            }

            public void scheduleDrawable(Drawable who, Runnable what, long when) {
                AnimatedVectorDrawable.this.scheduleSelf(what, when);
            }

            public void unscheduleDrawable(Drawable who, Runnable what) {
                AnimatedVectorDrawable.this.unscheduleSelf(what);
            }
        };
        this.mAnimatedVectorState = new AnimatedVectorDrawableState(state, this.mCallback, res);
        this.mAnimatorSet = new VectorDrawableAnimatorRT(this);
        this.mRes = res;
    }

    public Drawable mutate() {
        if (!this.mMutated && super.mutate() == this) {
            this.mAnimatedVectorState = new AnimatedVectorDrawableState(this.mAnimatedVectorState, this.mCallback, this.mRes);
            this.mMutated = true;
        }
        return this;
    }

    public void clearMutated() {
        super.clearMutated();
        if (this.mAnimatedVectorState.mVectorDrawable != null) {
            this.mAnimatedVectorState.mVectorDrawable.clearMutated();
        }
        this.mMutated = false;
    }

    /* access modifiers changed from: private */
    public static boolean shouldIgnoreInvalidAnimation() {
        Application app = ActivityThread.currentApplication();
        if (app == null || app.getApplicationInfo() == null || app.getApplicationInfo().targetSdkVersion < 24) {
            return true;
        }
        return false;
    }

    public Drawable.ConstantState getConstantState() {
        this.mAnimatedVectorState.mChangingConfigurations = getChangingConfigurations();
        return this.mAnimatedVectorState;
    }

    public int getChangingConfigurations() {
        return super.getChangingConfigurations() | this.mAnimatedVectorState.getChangingConfigurations();
    }

    public void draw(Canvas canvas) {
        if ((this.mAnimatorSet instanceof VectorDrawableAnimatorRT) && !this.mAnimatorSet.isRunning() && ((VectorDrawableAnimatorRT) this.mAnimatorSet).mPendingAnimationActions.size() > 0) {
            fallbackOntoUI();
        }
        this.mAnimatorSet.onDraw(canvas);
        this.mAnimatedVectorState.mVectorDrawable.draw(canvas);
    }

    /* access modifiers changed from: protected */
    public void onBoundsChange(Rect bounds) {
        this.mAnimatedVectorState.mVectorDrawable.setBounds(bounds);
    }

    /* access modifiers changed from: protected */
    public boolean onStateChange(int[] state) {
        return this.mAnimatedVectorState.mVectorDrawable.setState(state);
    }

    /* access modifiers changed from: protected */
    public boolean onLevelChange(int level) {
        return this.mAnimatedVectorState.mVectorDrawable.setLevel(level);
    }

    public boolean onLayoutDirectionChanged(int layoutDirection) {
        return this.mAnimatedVectorState.mVectorDrawable.setLayoutDirection(layoutDirection);
    }

    public int getAlpha() {
        return this.mAnimatedVectorState.mVectorDrawable.getAlpha();
    }

    public void setAlpha(int alpha) {
        this.mAnimatedVectorState.mVectorDrawable.setAlpha(alpha);
    }

    public void setColorFilter(ColorFilter colorFilter) {
        this.mAnimatedVectorState.mVectorDrawable.setColorFilter(colorFilter);
    }

    public ColorFilter getColorFilter() {
        return this.mAnimatedVectorState.mVectorDrawable.getColorFilter();
    }

    public void setTintList(ColorStateList tint) {
        this.mAnimatedVectorState.mVectorDrawable.setTintList(tint);
    }

    public void setHotspot(float x, float y) {
        this.mAnimatedVectorState.mVectorDrawable.setHotspot(x, y);
    }

    public void setHotspotBounds(int left, int top, int right, int bottom) {
        this.mAnimatedVectorState.mVectorDrawable.setHotspotBounds(left, top, right, bottom);
    }

    public void setTintMode(PorterDuff.Mode tintMode) {
        this.mAnimatedVectorState.mVectorDrawable.setTintMode(tintMode);
    }

    public boolean setVisible(boolean visible, boolean restart) {
        if (this.mAnimatorSet.isInfinite() && this.mAnimatorSet.isStarted()) {
            if (visible) {
                this.mAnimatorSet.resume();
            } else {
                this.mAnimatorSet.pause();
            }
        }
        this.mAnimatedVectorState.mVectorDrawable.setVisible(visible, restart);
        return super.setVisible(visible, restart);
    }

    public boolean isStateful() {
        return this.mAnimatedVectorState.mVectorDrawable.isStateful();
    }

    public int getOpacity() {
        return -3;
    }

    public int getIntrinsicWidth() {
        return this.mAnimatedVectorState.mVectorDrawable.getIntrinsicWidth();
    }

    public int getIntrinsicHeight() {
        return this.mAnimatedVectorState.mVectorDrawable.getIntrinsicHeight();
    }

    public void getOutline(Outline outline) {
        this.mAnimatedVectorState.mVectorDrawable.getOutline(outline);
    }

    public Insets getOpticalInsets() {
        return this.mAnimatedVectorState.mVectorDrawable.getOpticalInsets();
    }

    public void inflate(Resources res, XmlPullParser parser, AttributeSet attrs, Resources.Theme theme) throws XmlPullParserException, IOException {
        Resources resources;
        Resources resources2 = res;
        AttributeSet attributeSet = attrs;
        Resources.Theme theme2 = theme;
        AnimatedVectorDrawableState state = this.mAnimatedVectorState;
        int eventType = parser.getEventType();
        float pathErrorScale = 1.0f;
        int innerDepth = parser.getDepth() + 1;
        while (true) {
            resources = null;
            if (eventType != 1 && (parser.getDepth() >= innerDepth || eventType != 3)) {
                if (eventType == 2) {
                    String tagName = parser.getName();
                    if (ANIMATED_VECTOR.equals(tagName)) {
                        TypedArray a = obtainAttributes(resources2, theme2, attributeSet, R.styleable.AnimatedVectorDrawable);
                        int drawableRes = a.getResourceId(0, 0);
                        if (drawableRes != 0) {
                            VectorDrawable vectorDrawable = (VectorDrawable) resources2.getDrawable(drawableRes, theme2).mutate();
                            vectorDrawable.setAllowCaching(false);
                            vectorDrawable.setCallback(this.mCallback);
                            pathErrorScale = vectorDrawable.getPixelSize();
                            if (state.mVectorDrawable != null) {
                                state.mVectorDrawable.setCallback(null);
                            }
                            state.mVectorDrawable = vectorDrawable;
                        }
                        a.recycle();
                    } else if (TARGET.equals(tagName)) {
                        TypedArray a2 = obtainAttributes(resources2, theme2, attributeSet, R.styleable.AnimatedVectorDrawableTarget);
                        String target = a2.getString(0);
                        int animResId = a2.getResourceId(1, 0);
                        if (animResId != 0) {
                            if (theme2 != null) {
                                Animator animator = AnimatorInflater.loadAnimator(resources2, theme2, animResId, pathErrorScale);
                                updateAnimatorProperty(animator, target, state.mVectorDrawable, state.mShouldIgnoreInvalidAnim);
                                state.addTargetAnimator(target, animator);
                            } else {
                                state.addPendingAnimator(animResId, pathErrorScale, target);
                            }
                        }
                        a2.recycle();
                    }
                }
                eventType = parser.next();
            }
        }
        if (state.mPendingAnims != null) {
            resources = resources2;
        }
        this.mRes = resources;
    }

    /* access modifiers changed from: private */
    public static void updateAnimatorProperty(Animator animator, String targetName, VectorDrawable vectorDrawable, boolean ignoreInvalidAnim) {
        if (animator instanceof ObjectAnimator) {
            PropertyValuesHolder[] holders = ((ObjectAnimator) animator).getValues();
            for (PropertyValuesHolder pvh : holders) {
                String propertyName = pvh.getPropertyName();
                Object targetNameObj = vectorDrawable.getTargetByName(targetName);
                Property property = null;
                if (targetNameObj instanceof VectorDrawable.VObject) {
                    property = ((VectorDrawable.VObject) targetNameObj).getProperty(propertyName);
                } else if (targetNameObj instanceof VectorDrawable.VectorDrawableState) {
                    property = ((VectorDrawable.VectorDrawableState) targetNameObj).getProperty(propertyName);
                }
                if (property != null) {
                    if (containsSameValueType(pvh, property)) {
                        pvh.setProperty(property);
                    } else if (!ignoreInvalidAnim) {
                        throw new RuntimeException("Wrong valueType for Property: " + propertyName + ".  Expected type: " + property.getType().toString() + ". Actual type defined in resources: " + pvh.getValueType().toString());
                    }
                }
            }
        } else if (animator instanceof AnimatorSet) {
            Iterator<Animator> it = ((AnimatorSet) animator).getChildAnimations().iterator();
            while (it.hasNext()) {
                updateAnimatorProperty(it.next(), targetName, vectorDrawable, ignoreInvalidAnim);
            }
        }
    }

    private static boolean containsSameValueType(PropertyValuesHolder holder, Property property) {
        Class type1 = holder.getValueType();
        Class type2 = property.getType();
        boolean z = false;
        if (type1 == Float.TYPE || type1 == Float.class) {
            if (type2 == Float.TYPE || type2 == Float.class) {
                z = true;
            }
            return z;
        } else if (type1 == Integer.TYPE || type1 == Integer.class) {
            if (type2 == Integer.TYPE || type2 == Integer.class) {
                z = true;
            }
            return z;
        } else {
            if (type1 == type2) {
                z = true;
            }
            return z;
        }
    }

    public void forceAnimationOnUI() {
        if (!(this.mAnimatorSet instanceof VectorDrawableAnimatorRT)) {
            return;
        }
        if (!((VectorDrawableAnimatorRT) this.mAnimatorSet).isRunning()) {
            fallbackOntoUI();
            return;
        }
        throw new UnsupportedOperationException("Cannot force Animated Vector Drawable to run on UI thread when the animation has started on RenderThread.");
    }

    private void fallbackOntoUI() {
        if (this.mAnimatorSet instanceof VectorDrawableAnimatorRT) {
            VectorDrawableAnimatorRT oldAnim = (VectorDrawableAnimatorRT) this.mAnimatorSet;
            this.mAnimatorSet = new VectorDrawableAnimatorUI(this);
            if (this.mAnimatorSetFromXml != null) {
                this.mAnimatorSet.init(this.mAnimatorSetFromXml);
            }
            if (oldAnim.mListener != null) {
                this.mAnimatorSet.setListener(oldAnim.mListener);
            }
            oldAnim.transferPendingActions(this.mAnimatorSet);
        }
    }

    public boolean canApplyTheme() {
        return (this.mAnimatedVectorState != null && this.mAnimatedVectorState.canApplyTheme()) || super.canApplyTheme();
    }

    public void applyTheme(Resources.Theme t) {
        super.applyTheme(t);
        VectorDrawable vectorDrawable = this.mAnimatedVectorState.mVectorDrawable;
        if (vectorDrawable != null && vectorDrawable.canApplyTheme()) {
            vectorDrawable.applyTheme(t);
        }
        if (t != null) {
            this.mAnimatedVectorState.inflatePendingAnimators(t.getResources(), t);
        }
        if (this.mAnimatedVectorState.mPendingAnims == null) {
            this.mRes = null;
        }
    }

    public void setPathAnimFraction(float fraction) {
        ensureAnimatorSet();
        setFractionToAnim(fraction, this.mAnimatorSetFromXml);
        invalidateSelf();
    }

    /* access modifiers changed from: package-private */
    public void setFractionToAnim(float fraction, Animator animator) {
        if (animator instanceof ObjectAnimator) {
            ((ObjectAnimator) animator).setPathAnimFraction(fraction);
        }
        if (animator instanceof AnimatorSet) {
            ArrayList<Animator> animChildren = ((AnimatorSet) animator).getChildAnimations();
            int size = animChildren.size();
            for (int i = 0; i < size; i++) {
                setFractionToAnim(fraction, animChildren.get(i));
            }
        }
    }

    public boolean isRunning() {
        return this.mAnimatorSet.isRunning();
    }

    public void reset() {
        ensureAnimatorSet();
        this.mAnimatorSet.reset();
    }

    public void start() {
        ensureAnimatorSet();
        this.mAnimatorSet.start();
    }

    private void ensureAnimatorSet() {
        if (this.mAnimatorSetFromXml == null) {
            this.mAnimatorSetFromXml = new AnimatorSet();
            this.mAnimatedVectorState.prepareLocalAnimators(this.mAnimatorSetFromXml, this.mRes);
            this.mAnimatorSet.init(this.mAnimatorSetFromXml);
            this.mRes = null;
        }
    }

    public void stop() {
        this.mAnimatorSet.end();
    }

    public void reverse() {
        ensureAnimatorSet();
        if (!canReverse()) {
            Log.w(LOGTAG, "AnimatedVectorDrawable can't reverse()");
        } else {
            this.mAnimatorSet.reverse();
        }
    }

    public boolean canReverse() {
        return this.mAnimatorSet.canReverse();
    }

    public void registerAnimationCallback(Animatable2.AnimationCallback callback) {
        if (callback != null) {
            if (this.mAnimationCallbacks == null) {
                this.mAnimationCallbacks = new ArrayList<>();
            }
            this.mAnimationCallbacks.add(callback);
            if (this.mAnimatorListener == null) {
                this.mAnimatorListener = new AnimatorListenerAdapter() {
                    public void onAnimationStart(Animator animation) {
                        ArrayList<Animatable2.AnimationCallback> tmpCallbacks = new ArrayList<>(AnimatedVectorDrawable.this.mAnimationCallbacks);
                        int size = tmpCallbacks.size();
                        for (int i = 0; i < size; i++) {
                            tmpCallbacks.get(i).onAnimationStart(AnimatedVectorDrawable.this);
                        }
                    }

                    public void onAnimationEnd(Animator animation) {
                        ArrayList<Animatable2.AnimationCallback> tmpCallbacks = new ArrayList<>(AnimatedVectorDrawable.this.mAnimationCallbacks);
                        int size = tmpCallbacks.size();
                        for (int i = 0; i < size; i++) {
                            tmpCallbacks.get(i).onAnimationEnd(AnimatedVectorDrawable.this);
                        }
                    }
                };
            }
            this.mAnimatorSet.setListener(this.mAnimatorListener);
        }
    }

    private void removeAnimatorSetListener() {
        if (this.mAnimatorListener != null) {
            this.mAnimatorSet.removeListener(this.mAnimatorListener);
            this.mAnimatorListener = null;
        }
    }

    public boolean unregisterAnimationCallback(Animatable2.AnimationCallback callback) {
        if (this.mAnimationCallbacks == null || callback == null) {
            return false;
        }
        boolean removed = this.mAnimationCallbacks.remove(callback);
        if (this.mAnimationCallbacks.size() == 0) {
            removeAnimatorSetListener();
        }
        return removed;
    }

    public void clearAnimationCallbacks() {
        removeAnimatorSetListener();
        if (this.mAnimationCallbacks != null) {
            this.mAnimationCallbacks.clear();
        }
    }
}
