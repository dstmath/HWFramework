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
    private ArrayList<Animation> mAnimations = new ArrayList<>();
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
                this.mFlags = 2 | this.mFlags;
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

    /* access modifiers changed from: protected */
    @Override // java.lang.Object, android.view.animation.Animation, android.view.animation.Animation
    public AnimationSet clone() throws CloneNotSupportedException {
        AnimationSet animation = (AnimationSet) super.clone();
        animation.mTempTransformation = new Transformation();
        animation.mAnimations = new ArrayList<>();
        int count = this.mAnimations.size();
        ArrayList<Animation> animations = this.mAnimations;
        for (int i = 0; i < count; i++) {
            animation.mAnimations.add(animations.get(i).clone());
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

    @Override // android.view.animation.Animation
    public void setFillAfter(boolean fillAfter) {
        this.mFlags |= 1;
        super.setFillAfter(fillAfter);
    }

    @Override // android.view.animation.Animation
    public void setFillBefore(boolean fillBefore) {
        this.mFlags |= 2;
        super.setFillBefore(fillBefore);
    }

    @Override // android.view.animation.Animation
    public void setRepeatMode(int repeatMode) {
        this.mFlags |= 4;
        super.setRepeatMode(repeatMode);
    }

    @Override // android.view.animation.Animation
    public void setStartOffset(long startOffset) {
        this.mFlags |= 8;
        super.setStartOffset(startOffset);
    }

    @Override // android.view.animation.Animation
    public boolean hasAlpha() {
        if (this.mDirty) {
            this.mHasAlpha = false;
            this.mDirty = false;
            int count = this.mAnimations.size();
            ArrayList<Animation> animations = this.mAnimations;
            int i = 0;
            while (true) {
                if (i >= count) {
                    break;
                } else if (animations.get(i).hasAlpha()) {
                    this.mHasAlpha = true;
                    break;
                } else {
                    i++;
                }
            }
        }
        return this.mHasAlpha;
    }

    @Override // android.view.animation.Animation
    public void setDuration(long durationMillis) {
        this.mFlags |= 32;
        super.setDuration(durationMillis);
        this.mLastEnd = this.mStartOffset + this.mDuration;
    }

    public void addAnimation(Animation a) {
        this.mAnimations.add(a);
        boolean changeBounds = false;
        if (((this.mFlags & 64) == 0) && a.willChangeTransformationMatrix()) {
            this.mFlags |= 64;
        }
        if ((this.mFlags & 128) == 0) {
            changeBounds = true;
        }
        if (changeBounds && a.willChangeBounds()) {
            this.mFlags |= 128;
        }
        if ((this.mFlags & 32) == 32) {
            this.mLastEnd = this.mStartOffset + this.mDuration;
        } else if (this.mAnimations.size() == 1) {
            this.mDuration = a.getStartOffset() + a.getDuration();
            this.mLastEnd = this.mStartOffset + this.mDuration;
        } else {
            this.mLastEnd = Math.max(this.mLastEnd, this.mStartOffset + a.getStartOffset() + a.getDuration());
            this.mDuration = this.mLastEnd - this.mStartOffset;
        }
        this.mDirty = true;
    }

    @Override // android.view.animation.Animation
    public void setStartTime(long startTimeMillis) {
        super.setStartTime(startTimeMillis);
        int count = this.mAnimations.size();
        ArrayList<Animation> animations = this.mAnimations;
        for (int i = 0; i < count; i++) {
            animations.get(i).setStartTime(startTimeMillis);
        }
    }

    @Override // android.view.animation.Animation
    public long getStartTime() {
        long startTime = Long.MAX_VALUE;
        int count = this.mAnimations.size();
        ArrayList<Animation> animations = this.mAnimations;
        for (int i = 0; i < count; i++) {
            startTime = Math.min(startTime, animations.get(i).getStartTime());
        }
        return startTime;
    }

    @Override // android.view.animation.Animation
    public void restrictDuration(long durationMillis) {
        super.restrictDuration(durationMillis);
        ArrayList<Animation> animations = this.mAnimations;
        int count = animations.size();
        for (int i = 0; i < count; i++) {
            animations.get(i).restrictDuration(durationMillis);
        }
    }

    @Override // android.view.animation.Animation
    public long getDuration() {
        ArrayList<Animation> animations = this.mAnimations;
        int count = animations.size();
        long duration = 0;
        if ((this.mFlags & 32) == 32) {
            return this.mDuration;
        }
        for (int i = 0; i < count; i++) {
            duration = Math.max(duration, animations.get(i).getDuration());
        }
        return duration;
    }

    @Override // android.view.animation.Animation
    public long computeDurationHint() {
        long duration = 0;
        int count = this.mAnimations.size();
        ArrayList<Animation> animations = this.mAnimations;
        for (int i = count - 1; i >= 0; i--) {
            long d = animations.get(i).computeDurationHint();
            if (d > duration) {
                duration = d;
            }
        }
        return duration;
    }

    @Override // android.view.animation.Animation
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
                Animation a = animations.get(i);
                if (!a.isFillEnabled() || a.getFillBefore() || a.getStartOffset() == 0) {
                    temp.clear();
                    Interpolator interpolator = a.mInterpolator;
                    float f = 0.0f;
                    if (interpolator != null) {
                        f = interpolator.getInterpolation(0.0f);
                    }
                    a.applyTransformation(f, temp);
                    previousTransformation.compose(temp);
                }
            }
        }
    }

    @Override // android.view.animation.Animation
    public boolean getTransformation(long currentTime, Transformation t) {
        int count = this.mAnimations.size();
        ArrayList<Animation> animations = this.mAnimations;
        Transformation temp = this.mTempTransformation;
        boolean more = false;
        boolean started = false;
        boolean ended = true;
        t.clear();
        int i = count - 1;
        while (true) {
            boolean z = true;
            if (i < 0) {
                break;
            }
            Animation a = animations.get(i);
            temp.clear();
            more = a.getTransformation(currentTime, temp, getScaleFactor()) || more;
            t.compose(temp);
            started = started || a.hasStarted();
            if (!a.hasEnded() || !ended) {
                z = false;
            }
            ended = z;
            i--;
        }
        if (started && !this.mStarted) {
            dispatchAnimationStart();
            this.mStarted = true;
        }
        if (ended != this.mEnded) {
            dispatchAnimationEnd();
            this.mEnded = ended;
        }
        return more;
    }

    @Override // android.view.animation.Animation
    public void scaleCurrentDuration(float scale) {
        ArrayList<Animation> animations = this.mAnimations;
        int count = animations.size();
        for (int i = 0; i < count; i++) {
            animations.get(i).scaleCurrentDuration(scale);
        }
    }

    /* JADX INFO: Multiple debug info for r14v1 long: [D('interpolator' android.view.animation.Interpolator), D('startOffset' long)] */
    /* JADX INFO: Multiple debug info for r14v2 long[]: [D('storedOffsets' long[]), D('startOffset' long)] */
    /* JADX INFO: Multiple debug info for r1v9 'interpolator'  android.view.animation.Interpolator: [D('interpolator' android.view.animation.Interpolator), D('durationSet' boolean)] */
    @Override // android.view.animation.Animation
    public void initialize(int width, int height, int parentWidth, int parentHeight) {
        boolean durationSet;
        Interpolator interpolator;
        Interpolator interpolator2;
        boolean fillAfterSet;
        super.initialize(width, height, parentWidth, parentHeight);
        boolean startOffsetSet = false;
        boolean durationSet2 = (this.mFlags & 32) == 32;
        boolean fillAfterSet2 = (this.mFlags & 1) == 1;
        boolean fillBeforeSet = (this.mFlags & 2) == 2;
        boolean repeatModeSet = (this.mFlags & 4) == 4;
        boolean shareInterpolator = (this.mFlags & 16) == 16;
        if ((this.mFlags & 8) == 8) {
            startOffsetSet = true;
        }
        if (shareInterpolator) {
            ensureInterpolator();
        }
        ArrayList<Animation> children = this.mAnimations;
        int count = children.size();
        long duration = this.mDuration;
        boolean fillAfter = this.mFillAfter;
        boolean fillBefore = this.mFillBefore;
        int repeatMode = this.mRepeatMode;
        Interpolator interpolator3 = this.mInterpolator;
        long startOffset = this.mStartOffset;
        long[] storedOffsets = this.mStoredOffsets;
        if (startOffsetSet) {
            if (storedOffsets == null || storedOffsets.length != count) {
                long[] jArr = new long[count];
                this.mStoredOffsets = jArr;
                storedOffsets = jArr;
            }
        } else if (storedOffsets != null) {
            this.mStoredOffsets = null;
            storedOffsets = null;
        }
        int i = 0;
        while (i < count) {
            Animation a = children.get(i);
            if (durationSet2) {
                a.setDuration(duration);
            }
            if (fillAfterSet2) {
                a.setFillAfter(fillAfter);
            }
            if (fillBeforeSet) {
                a.setFillBefore(fillBefore);
            }
            if (repeatModeSet) {
                a.setRepeatMode(repeatMode);
            }
            if (shareInterpolator) {
                durationSet = durationSet2;
                interpolator = interpolator3;
                a.setInterpolator(interpolator);
            } else {
                durationSet = durationSet2;
                interpolator = interpolator3;
            }
            if (startOffsetSet) {
                long offset = a.getStartOffset();
                interpolator2 = interpolator;
                fillAfterSet = fillAfterSet2;
                a.setStartOffset(offset + startOffset);
                storedOffsets[i] = offset;
            } else {
                interpolator2 = interpolator;
                fillAfterSet = fillAfterSet2;
            }
            a.initialize(width, height, parentWidth, parentHeight);
            i++;
            fillAfterSet2 = fillAfterSet;
            durationSet2 = durationSet;
            startOffsetSet = startOffsetSet;
            children = children;
            interpolator3 = interpolator2;
        }
    }

    @Override // android.view.animation.Animation
    public void reset() {
        super.reset();
        restoreChildrenStartOffset();
    }

    /* access modifiers changed from: package-private */
    public void restoreChildrenStartOffset() {
        long[] offsets = this.mStoredOffsets;
        if (offsets != null) {
            ArrayList<Animation> children = this.mAnimations;
            int count = children.size();
            for (int i = 0; i < count; i++) {
                children.get(i).setStartOffset(offsets[i]);
            }
        }
    }

    public List<Animation> getAnimations() {
        return this.mAnimations;
    }

    @Override // android.view.animation.Animation
    public boolean willChangeTransformationMatrix() {
        return (this.mFlags & 64) == 64;
    }

    @Override // android.view.animation.Animation
    public boolean willChangeBounds() {
        return (this.mFlags & 128) == 128;
    }
}
