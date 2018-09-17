package android.view.animation;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.RectF;
import android.util.AttributeSet;
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
    private ArrayList<Animation> mAnimations = new ArrayList();
    private boolean mDirty;
    private int mFlags = 0;
    private boolean mHasAlpha;
    private long mLastEnd;
    private long[] mStoredOffsets;
    private Transformation mTempTransformation = new Transformation();

    public AnimationSet(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.AnimationSet);
        setFlag(16, a.getBoolean(1, true));
        init();
        if (context.getApplicationInfo().targetSdkVersion >= 14) {
            if (a.hasValue(0)) {
                this.mFlags |= 32;
            }
            if (a.hasValue(2)) {
                this.mFlags |= 2;
            }
            if (a.hasValue(3)) {
                this.mFlags |= 1;
            }
            if (a.hasValue(5)) {
                this.mFlags |= 4;
            }
            if (a.hasValue(4)) {
                this.mFlags |= 8;
            }
        }
        a.recycle();
    }

    public AnimationSet(boolean shareInterpolator) {
        setFlag(16, shareInterpolator);
        init();
    }

    protected AnimationSet clone() throws CloneNotSupportedException {
        AnimationSet animation = (AnimationSet) super.clone();
        animation.mTempTransformation = new Transformation();
        animation.mAnimations = new ArrayList();
        int count = this.mAnimations.size();
        ArrayList<Animation> animations = this.mAnimations;
        for (int i = 0; i < count; i++) {
            animation.mAnimations.add(((Animation) animations.get(i)).clone());
        }
        return animation;
    }

    private void setFlag(int mask, boolean value) {
        if (value) {
            this.mFlags |= mask;
        } else {
            this.mFlags &= ~mask;
        }
    }

    private void init() {
        this.mStartTime = 0;
    }

    public void setFillAfter(boolean fillAfter) {
        this.mFlags |= 1;
        super.setFillAfter(fillAfter);
    }

    public void setFillBefore(boolean fillBefore) {
        this.mFlags |= 2;
        super.setFillBefore(fillBefore);
    }

    public void setRepeatMode(int repeatMode) {
        this.mFlags |= 4;
        super.setRepeatMode(repeatMode);
    }

    public void setStartOffset(long startOffset) {
        this.mFlags |= 8;
        super.setStartOffset(startOffset);
    }

    public boolean hasAlpha() {
        if (this.mDirty) {
            this.mHasAlpha = false;
            this.mDirty = false;
            int count = this.mAnimations.size();
            ArrayList<Animation> animations = this.mAnimations;
            for (int i = 0; i < count; i++) {
                if (((Animation) animations.get(i)).hasAlpha()) {
                    this.mHasAlpha = true;
                    break;
                }
            }
        }
        return this.mHasAlpha;
    }

    public void setDuration(long durationMillis) {
        this.mFlags |= 32;
        super.setDuration(durationMillis);
        this.mLastEnd = this.mStartOffset + this.mDuration;
    }

    public void addAnimation(Animation a) {
        this.mAnimations.add(a);
        if (((this.mFlags & 64) == 0) && a.willChangeTransformationMatrix()) {
            this.mFlags |= 64;
        }
        if (((this.mFlags & 128) == 0) && a.willChangeBounds()) {
            this.mFlags |= 128;
        }
        if ((this.mFlags & 32) == 32) {
            this.mLastEnd = this.mStartOffset + this.mDuration;
        } else if (this.mAnimations.size() == 1) {
            this.mDuration = a.getStartOffset() + a.getDuration();
            this.mLastEnd = this.mStartOffset + this.mDuration;
        } else {
            this.mLastEnd = Math.max(this.mLastEnd, (this.mStartOffset + a.getStartOffset()) + a.getDuration());
            this.mDuration = this.mLastEnd - this.mStartOffset;
        }
        this.mDirty = true;
    }

    public void setStartTime(long startTimeMillis) {
        super.setStartTime(startTimeMillis);
        int count = this.mAnimations.size();
        ArrayList<Animation> animations = this.mAnimations;
        for (int i = 0; i < count; i++) {
            ((Animation) animations.get(i)).setStartTime(startTimeMillis);
        }
    }

    public long getStartTime() {
        long startTime = Long.MAX_VALUE;
        int count = this.mAnimations.size();
        ArrayList<Animation> animations = this.mAnimations;
        for (int i = 0; i < count; i++) {
            startTime = Math.min(startTime, ((Animation) animations.get(i)).getStartTime());
        }
        return startTime;
    }

    public void restrictDuration(long durationMillis) {
        super.restrictDuration(durationMillis);
        ArrayList<Animation> animations = this.mAnimations;
        int count = animations.size();
        for (int i = 0; i < count; i++) {
            ((Animation) animations.get(i)).restrictDuration(durationMillis);
        }
    }

    public long getDuration() {
        ArrayList<Animation> animations = this.mAnimations;
        int count = animations.size();
        long duration = 0;
        if ((this.mFlags & 32) == 32) {
            return this.mDuration;
        }
        for (int i = 0; i < count; i++) {
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
        region.inset(-1.0f, -1.0f);
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
        if (started && (this.mStarted ^ 1) != 0) {
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
        for (int i = 0; i < count; i++) {
            ((Animation) animations.get(i)).scaleCurrentDuration(scale);
        }
    }

    public void initialize(int width, int height, int parentWidth, int parentHeight) {
        super.initialize(width, height, parentWidth, parentHeight);
        boolean durationSet = (this.mFlags & 32) == 32;
        boolean fillAfterSet = (this.mFlags & 1) == 1;
        boolean fillBeforeSet = (this.mFlags & 2) == 2;
        boolean repeatModeSet = (this.mFlags & 4) == 4;
        boolean shareInterpolator = (this.mFlags & 16) == 16;
        boolean startOffsetSet = (this.mFlags & 8) == 8;
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
            if (storedOffsets == null || storedOffsets.length != count) {
                storedOffsets = new long[count];
                this.mStoredOffsets = storedOffsets;
            }
        } else if (storedOffsets != null) {
            this.mStoredOffsets = null;
            storedOffsets = null;
        }
        for (int i = 0; i < count; i++) {
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
            for (int i = 0; i < count; i++) {
                ((Animation) children.get(i)).setStartOffset(offsets[i]);
            }
        }
    }

    public List<Animation> getAnimations() {
        return this.mAnimations;
    }

    public boolean willChangeTransformationMatrix() {
        return (this.mFlags & 64) == 64;
    }

    public boolean willChangeBounds() {
        return (this.mFlags & 128) == 128;
    }
}
