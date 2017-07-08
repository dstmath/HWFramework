package android.view.animation;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.WindowManager.LayoutParams;
import com.android.internal.R;
import java.util.ArrayList;
import java.util.List;

public class AnimationSet extends Animation {
    private static final int PROPERTY_CHANGE_BOUNDS_MASK = 128;
    private static final int PROPERTY_DURATION_MASK = 32;
    private static final int PROPERTY_FILL_AFTER_MASK = 1;
    private static final int PROPERTY_FILL_BEFORE_MASK = 2;
    private static final int PROPERTY_MORPH_MATRIX_MASK = 64;
    private static final int PROPERTY_REPEAT_MODE_MASK = 4;
    private static final int PROPERTY_SHARE_INTERPOLATOR_MASK = 16;
    private static final int PROPERTY_START_OFFSET_MASK = 8;
    private ArrayList<Animation> mAnimations;
    private boolean mDirty;
    private int mFlags;
    private boolean mHasAlpha;
    private long mLastEnd;
    private long[] mStoredOffsets;
    private Transformation mTempTransformation;

    private void setFlag(int r1, boolean r2) {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.view.animation.AnimationSet.setFlag(int, boolean):void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException: Unknown instruction: not-int
	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:568)
	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:56)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:99)
	... 5 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: android.view.animation.AnimationSet.setFlag(int, boolean):void");
    }

    public AnimationSet(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mFlags = 0;
        this.mAnimations = new ArrayList();
        this.mTempTransformation = new Transformation();
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.AnimationSet);
        setFlag(PROPERTY_SHARE_INTERPOLATOR_MASK, a.getBoolean(PROPERTY_FILL_AFTER_MASK, true));
        init();
        if (context.getApplicationInfo().targetSdkVersion >= 14) {
            if (a.hasValue(0)) {
                this.mFlags |= PROPERTY_DURATION_MASK;
            }
            if (a.hasValue(PROPERTY_FILL_BEFORE_MASK)) {
                this.mFlags |= PROPERTY_FILL_BEFORE_MASK;
            }
            if (a.hasValue(3)) {
                this.mFlags |= PROPERTY_FILL_AFTER_MASK;
            }
            if (a.hasValue(5)) {
                this.mFlags |= PROPERTY_REPEAT_MODE_MASK;
            }
            if (a.hasValue(PROPERTY_REPEAT_MODE_MASK)) {
                this.mFlags |= PROPERTY_START_OFFSET_MASK;
            }
        }
        a.recycle();
    }

    public AnimationSet(boolean shareInterpolator) {
        this.mFlags = 0;
        this.mAnimations = new ArrayList();
        this.mTempTransformation = new Transformation();
        setFlag(PROPERTY_SHARE_INTERPOLATOR_MASK, shareInterpolator);
        init();
    }

    protected AnimationSet clone() throws CloneNotSupportedException {
        AnimationSet animation = (AnimationSet) super.clone();
        animation.mTempTransformation = new Transformation();
        animation.mAnimations = new ArrayList();
        int count = this.mAnimations.size();
        ArrayList<Animation> animations = this.mAnimations;
        for (int i = 0; i < count; i += PROPERTY_FILL_AFTER_MASK) {
            animation.mAnimations.add(((Animation) animations.get(i)).clone());
        }
        return animation;
    }

    private void init() {
        this.mStartTime = 0;
    }

    public void setFillAfter(boolean fillAfter) {
        this.mFlags |= PROPERTY_FILL_AFTER_MASK;
        super.setFillAfter(fillAfter);
    }

    public void setFillBefore(boolean fillBefore) {
        this.mFlags |= PROPERTY_FILL_BEFORE_MASK;
        super.setFillBefore(fillBefore);
    }

    public void setRepeatMode(int repeatMode) {
        this.mFlags |= PROPERTY_REPEAT_MODE_MASK;
        super.setRepeatMode(repeatMode);
    }

    public void setStartOffset(long startOffset) {
        this.mFlags |= PROPERTY_START_OFFSET_MASK;
        super.setStartOffset(startOffset);
    }

    public boolean hasAlpha() {
        if (this.mDirty) {
            this.mHasAlpha = false;
            this.mDirty = false;
            int count = this.mAnimations.size();
            ArrayList<Animation> animations = this.mAnimations;
            for (int i = 0; i < count; i += PROPERTY_FILL_AFTER_MASK) {
                if (((Animation) animations.get(i)).hasAlpha()) {
                    this.mHasAlpha = true;
                    break;
                }
            }
        }
        return this.mHasAlpha;
    }

    public void setDuration(long durationMillis) {
        this.mFlags |= PROPERTY_DURATION_MASK;
        super.setDuration(durationMillis);
        this.mLastEnd = this.mStartOffset + this.mDuration;
    }

    public void addAnimation(Animation a) {
        boolean noMatrix;
        boolean changeBounds = false;
        this.mAnimations.add(a);
        if ((this.mFlags & PROPERTY_MORPH_MATRIX_MASK) == 0) {
            noMatrix = true;
        } else {
            noMatrix = false;
        }
        if (noMatrix && a.willChangeTransformationMatrix()) {
            this.mFlags |= PROPERTY_MORPH_MATRIX_MASK;
        }
        if ((this.mFlags & PROPERTY_CHANGE_BOUNDS_MASK) == 0) {
            changeBounds = true;
        }
        if (changeBounds && a.willChangeBounds()) {
            this.mFlags |= PROPERTY_CHANGE_BOUNDS_MASK;
        }
        if ((this.mFlags & PROPERTY_DURATION_MASK) == PROPERTY_DURATION_MASK) {
            this.mLastEnd = this.mStartOffset + this.mDuration;
        } else if (this.mAnimations.size() == PROPERTY_FILL_AFTER_MASK) {
            this.mDuration = a.getStartOffset() + a.getDuration();
            this.mLastEnd = this.mStartOffset + this.mDuration;
        } else {
            this.mLastEnd = Math.max(this.mLastEnd, a.getStartOffset() + a.getDuration());
            this.mDuration = this.mLastEnd - this.mStartOffset;
        }
        this.mDirty = true;
    }

    public void setStartTime(long startTimeMillis) {
        super.setStartTime(startTimeMillis);
        int count = this.mAnimations.size();
        ArrayList<Animation> animations = this.mAnimations;
        for (int i = 0; i < count; i += PROPERTY_FILL_AFTER_MASK) {
            ((Animation) animations.get(i)).setStartTime(startTimeMillis);
        }
    }

    public long getStartTime() {
        long startTime = Long.MAX_VALUE;
        int count = this.mAnimations.size();
        ArrayList<Animation> animations = this.mAnimations;
        for (int i = 0; i < count; i += PROPERTY_FILL_AFTER_MASK) {
            startTime = Math.min(startTime, ((Animation) animations.get(i)).getStartTime());
        }
        return startTime;
    }

    public void restrictDuration(long durationMillis) {
        super.restrictDuration(durationMillis);
        ArrayList<Animation> animations = this.mAnimations;
        int count = animations.size();
        for (int i = 0; i < count; i += PROPERTY_FILL_AFTER_MASK) {
            ((Animation) animations.get(i)).restrictDuration(durationMillis);
        }
    }

    public long getDuration() {
        ArrayList<Animation> animations = this.mAnimations;
        int count = animations.size();
        long duration = 0;
        if ((this.mFlags & PROPERTY_DURATION_MASK) == PROPERTY_DURATION_MASK) {
            return this.mDuration;
        }
        for (int i = 0; i < count; i += PROPERTY_FILL_AFTER_MASK) {
            duration = Math.max(duration, ((Animation) animations.get(i)).getDuration());
        }
        return duration;
    }

    public long computeDurationHint() {
        long duration = 0;
        int count = this.mAnimations.size();
        ArrayList<Animation> animations = this.mAnimations;
        for (int i = count - 1; i >= 0; i--) {
            long d = ((Animation) animations.get(i)).computeDurationHint();
            if (d > duration) {
                duration = d;
            }
        }
        return duration;
    }

    public void initializeInvalidateRegion(int left, int top, int right, int bottom) {
        RectF region = this.mPreviousRegion;
        region.set((float) left, (float) top, (float) right, (float) bottom);
        region.inset(LayoutParams.BRIGHTNESS_OVERRIDE_NONE, LayoutParams.BRIGHTNESS_OVERRIDE_NONE);
        if (this.mFillBefore) {
            int count = this.mAnimations.size();
            ArrayList<Animation> animations = this.mAnimations;
            Transformation temp = this.mTempTransformation;
            Transformation previousTransformation = this.mPreviousTransformation;
            for (int i = count - 1; i >= 0; i--) {
                Animation a = (Animation) animations.get(i);
                if (!a.isFillEnabled() || a.getFillBefore() || a.getStartOffset() == 0) {
                    float interpolation;
                    temp.clear();
                    Interpolator interpolator = a.mInterpolator;
                    if (interpolator != null) {
                        interpolation = interpolator.getInterpolation(0.0f);
                    } else {
                        interpolation = 0.0f;
                    }
                    a.applyTransformation(interpolation, temp);
                    previousTransformation.compose(temp);
                }
            }
        }
    }

    public boolean getTransformation(long currentTime, Transformation t) {
        int count = this.mAnimations.size();
        ArrayList<Animation> animations = this.mAnimations;
        Transformation temp = this.mTempTransformation;
        boolean more = false;
        boolean started = false;
        boolean ended = true;
        t.clear();
        for (int i = count - 1; i >= 0; i--) {
            Animation a = (Animation) animations.get(i);
            temp.clear();
            if (a.getTransformation(currentTime, temp, getScaleFactor())) {
                more = true;
            }
            t.compose(temp);
            started = !started ? a.hasStarted() : true;
            if (!a.hasEnded()) {
                ended = false;
            }
        }
        if (started && !this.mStarted) {
            if (this.mListener != null) {
                this.mListener.onAnimationStart(this);
            }
            this.mStarted = true;
        }
        if (ended != this.mEnded) {
            if (this.mListener != null) {
                this.mListener.onAnimationEnd(this);
            }
            this.mEnded = ended;
        }
        return more;
    }

    public void scaleCurrentDuration(float scale) {
        ArrayList<Animation> animations = this.mAnimations;
        int count = animations.size();
        for (int i = 0; i < count; i += PROPERTY_FILL_AFTER_MASK) {
            ((Animation) animations.get(i)).scaleCurrentDuration(scale);
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void initialize(int width, int height, int parentWidth, int parentHeight) {
        super.initialize(width, height, parentWidth, parentHeight);
        boolean durationSet = (this.mFlags & PROPERTY_DURATION_MASK) == PROPERTY_DURATION_MASK;
        boolean fillAfterSet = (this.mFlags & PROPERTY_FILL_AFTER_MASK) == PROPERTY_FILL_AFTER_MASK;
        boolean fillBeforeSet = (this.mFlags & PROPERTY_FILL_BEFORE_MASK) == PROPERTY_FILL_BEFORE_MASK;
        boolean repeatModeSet = (this.mFlags & PROPERTY_REPEAT_MODE_MASK) == PROPERTY_REPEAT_MODE_MASK;
        boolean shareInterpolator = (this.mFlags & PROPERTY_SHARE_INTERPOLATOR_MASK) == PROPERTY_SHARE_INTERPOLATOR_MASK;
        boolean startOffsetSet = (this.mFlags & PROPERTY_START_OFFSET_MASK) == PROPERTY_START_OFFSET_MASK;
        if (shareInterpolator) {
            ensureInterpolator();
        }
        ArrayList<Animation> children = this.mAnimations;
        int count = children.size();
        long duration = this.mDuration;
        boolean fillAfter = this.mFillAfter;
        boolean fillBefore = this.mFillBefore;
        int repeatMode = this.mRepeatMode;
        Interpolator interpolator = this.mInterpolator;
        long startOffset = this.mStartOffset;
        long[] storedOffsets = this.mStoredOffsets;
        if (startOffsetSet) {
            if (storedOffsets != null) {
                int length = storedOffsets.length;
            }
            storedOffsets = new long[count];
            this.mStoredOffsets = storedOffsets;
        } else if (storedOffsets != null) {
            this.mStoredOffsets = null;
            storedOffsets = null;
        }
        for (int i = 0; i < count; i += PROPERTY_FILL_AFTER_MASK) {
            Animation a = (Animation) children.get(i);
            if (durationSet) {
                a.setDuration(duration);
            }
            if (fillAfterSet) {
                a.setFillAfter(fillAfter);
            }
            if (fillBeforeSet) {
                a.setFillBefore(fillBefore);
            }
            if (repeatModeSet) {
                a.setRepeatMode(repeatMode);
            }
            if (shareInterpolator) {
                a.setInterpolator(interpolator);
            }
            if (startOffsetSet) {
                long offset = a.getStartOffset();
                a.setStartOffset(offset + startOffset);
                storedOffsets[i] = offset;
            }
            a.initialize(width, height, parentWidth, parentHeight);
        }
    }

    public void reset() {
        super.reset();
        restoreChildrenStartOffset();
    }

    void restoreChildrenStartOffset() {
        long[] offsets = this.mStoredOffsets;
        if (offsets != null) {
            ArrayList<Animation> children = this.mAnimations;
            int count = children.size();
            for (int i = 0; i < count; i += PROPERTY_FILL_AFTER_MASK) {
                ((Animation) children.get(i)).setStartOffset(offsets[i]);
            }
        }
    }

    public List<Animation> getAnimations() {
        return this.mAnimations;
    }

    public boolean willChangeTransformationMatrix() {
        return (this.mFlags & PROPERTY_MORPH_MATRIX_MASK) == PROPERTY_MORPH_MATRIX_MASK;
    }

    public boolean willChangeBounds() {
        return (this.mFlags & PROPERTY_CHANGE_BOUNDS_MASK) == PROPERTY_CHANGE_BOUNDS_MASK;
    }
}
