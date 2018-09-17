package com.android.internal.widget;

import android.content.res.Resources;
import android.os.SystemClock;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.WindowManager.LayoutParams;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AnimationUtils;
import android.view.animation.Interpolator;
import android.widget.AbsListView;
import huawei.cust.HwCfgFilePolicy;

public abstract class AutoScrollHelper implements OnTouchListener {
    private static final int DEFAULT_ACTIVATION_DELAY = 0;
    private static final int DEFAULT_EDGE_TYPE = 1;
    private static final float DEFAULT_MAXIMUM_EDGE = Float.MAX_VALUE;
    private static final int DEFAULT_MAXIMUM_VELOCITY_DIPS = 1575;
    private static final int DEFAULT_MINIMUM_VELOCITY_DIPS = 315;
    private static final int DEFAULT_RAMP_DOWN_DURATION = 500;
    private static final int DEFAULT_RAMP_UP_DURATION = 500;
    private static final float DEFAULT_RELATIVE_EDGE = 0.2f;
    private static final float DEFAULT_RELATIVE_VELOCITY = 1.0f;
    public static final int EDGE_TYPE_INSIDE = 0;
    public static final int EDGE_TYPE_INSIDE_EXTEND = 1;
    public static final int EDGE_TYPE_OUTSIDE = 2;
    private static final int HORIZONTAL = 0;
    public static final float NO_MAX = Float.MAX_VALUE;
    public static final float NO_MIN = 0.0f;
    public static final float RELATIVE_UNSPECIFIED = 0.0f;
    private static final int VERTICAL = 1;
    private int mActivationDelay;
    private boolean mAlreadyDelayed;
    private boolean mAnimating;
    private final Interpolator mEdgeInterpolator;
    private int mEdgeType;
    private boolean mEnabled;
    private boolean mExclusive;
    private float[] mMaximumEdges;
    private float[] mMaximumVelocity;
    private float[] mMinimumVelocity;
    private boolean mNeedsCancel;
    private boolean mNeedsReset;
    private float[] mRelativeEdges;
    private float[] mRelativeVelocity;
    private Runnable mRunnable;
    private final ClampedScroller mScroller;
    private final View mTarget;

    public static class AbsListViewAutoScroller extends AutoScrollHelper {
        private final AbsListView mTarget;

        public AbsListViewAutoScroller(AbsListView target) {
            super(target);
            this.mTarget = target;
        }

        public void scrollTargetBy(int deltaX, int deltaY) {
            this.mTarget.scrollListBy(deltaY);
        }

        public boolean canTargetScrollHorizontally(int direction) {
            return false;
        }

        public boolean canTargetScrollVertically(int direction) {
            AbsListView target = this.mTarget;
            int itemCount = target.getCount();
            if (itemCount == 0) {
                return false;
            }
            int childCount = target.getChildCount();
            int firstPosition = target.getFirstVisiblePosition();
            int lastPosition = firstPosition + childCount;
            if (direction > 0) {
                if (lastPosition >= itemCount && target.getChildAt(childCount - 1).getBottom() <= target.getHeight()) {
                    return false;
                }
            } else if (direction >= 0) {
                return false;
            } else {
                if (firstPosition <= 0) {
                    View firstView = target.getChildAt(AutoScrollHelper.HORIZONTAL);
                    if (firstView == null || firstView.getTop() >= 0) {
                        return false;
                    }
                }
            }
            return true;
        }
    }

    private static class ClampedScroller {
        private long mDeltaTime;
        private int mDeltaX;
        private int mDeltaY;
        private int mEffectiveRampDown;
        private int mRampDownDuration;
        private int mRampUpDuration;
        private long mStartTime;
        private long mStopTime;
        private float mStopValue;
        private float mTargetVelocityX;
        private float mTargetVelocityY;

        public ClampedScroller() {
            this.mStartTime = Long.MIN_VALUE;
            this.mStopTime = -1;
            this.mDeltaTime = 0;
            this.mDeltaX = AutoScrollHelper.HORIZONTAL;
            this.mDeltaY = AutoScrollHelper.HORIZONTAL;
        }

        public void setRampUpDuration(int durationMillis) {
            this.mRampUpDuration = durationMillis;
        }

        public void setRampDownDuration(int durationMillis) {
            this.mRampDownDuration = durationMillis;
        }

        public void start() {
            this.mStartTime = AnimationUtils.currentAnimationTimeMillis();
            this.mStopTime = -1;
            this.mDeltaTime = this.mStartTime;
            this.mStopValue = 0.5f;
            this.mDeltaX = AutoScrollHelper.HORIZONTAL;
            this.mDeltaY = AutoScrollHelper.HORIZONTAL;
        }

        public void requestStop() {
            long currentTime = AnimationUtils.currentAnimationTimeMillis();
            this.mEffectiveRampDown = AutoScrollHelper.constrain((int) (currentTime - this.mStartTime), (int) AutoScrollHelper.HORIZONTAL, this.mRampDownDuration);
            this.mStopValue = getValueAt(currentTime);
            this.mStopTime = currentTime;
        }

        public boolean isFinished() {
            if (this.mStopTime <= 0 || AnimationUtils.currentAnimationTimeMillis() <= this.mStopTime + ((long) this.mEffectiveRampDown)) {
                return false;
            }
            return true;
        }

        private float getValueAt(long currentTime) {
            if (currentTime < this.mStartTime) {
                return AutoScrollHelper.RELATIVE_UNSPECIFIED;
            }
            if (this.mStopTime < 0 || currentTime < this.mStopTime) {
                return AutoScrollHelper.constrain(((float) (currentTime - this.mStartTime)) / ((float) this.mRampUpDuration), (float) AutoScrollHelper.RELATIVE_UNSPECIFIED, (float) AutoScrollHelper.DEFAULT_RELATIVE_VELOCITY) * 0.5f;
            }
            return (AutoScrollHelper.DEFAULT_RELATIVE_VELOCITY - this.mStopValue) + (this.mStopValue * AutoScrollHelper.constrain(((float) (currentTime - this.mStopTime)) / ((float) this.mEffectiveRampDown), (float) AutoScrollHelper.RELATIVE_UNSPECIFIED, (float) AutoScrollHelper.DEFAULT_RELATIVE_VELOCITY));
        }

        private float interpolateValue(float value) {
            return ((-4.0f * value) * value) + (4.0f * value);
        }

        public void computeScrollDelta() {
            if (this.mDeltaTime == 0) {
                throw new RuntimeException("Cannot compute scroll delta before calling start()");
            }
            long currentTime = AnimationUtils.currentAnimationTimeMillis();
            float scale = interpolateValue(getValueAt(currentTime));
            long elapsedSinceDelta = currentTime - this.mDeltaTime;
            this.mDeltaTime = currentTime;
            this.mDeltaX = (int) ((((float) elapsedSinceDelta) * scale) * this.mTargetVelocityX);
            this.mDeltaY = (int) ((((float) elapsedSinceDelta) * scale) * this.mTargetVelocityY);
        }

        public void setTargetVelocity(float x, float y) {
            this.mTargetVelocityX = x;
            this.mTargetVelocityY = y;
        }

        public int getHorizontalDirection() {
            return (int) (this.mTargetVelocityX / Math.abs(this.mTargetVelocityX));
        }

        public int getVerticalDirection() {
            return (int) (this.mTargetVelocityY / Math.abs(this.mTargetVelocityY));
        }

        public int getDeltaX() {
            return this.mDeltaX;
        }

        public int getDeltaY() {
            return this.mDeltaY;
        }
    }

    private class ScrollAnimationRunnable implements Runnable {
        private ScrollAnimationRunnable() {
        }

        public void run() {
            if (AutoScrollHelper.this.mAnimating) {
                if (AutoScrollHelper.this.mNeedsReset) {
                    AutoScrollHelper.this.mNeedsReset = false;
                    AutoScrollHelper.this.mScroller.start();
                }
                ClampedScroller scroller = AutoScrollHelper.this.mScroller;
                if (scroller.isFinished() || !AutoScrollHelper.this.shouldAnimate()) {
                    AutoScrollHelper.this.mAnimating = false;
                    return;
                }
                if (AutoScrollHelper.this.mNeedsCancel) {
                    AutoScrollHelper.this.mNeedsCancel = false;
                    AutoScrollHelper.this.cancelTargetTouch();
                }
                scroller.computeScrollDelta();
                AutoScrollHelper.this.scrollTargetBy(scroller.getDeltaX(), scroller.getDeltaY());
                AutoScrollHelper.this.mTarget.postOnAnimation(this);
            }
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.internal.widget.AutoScrollHelper.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.internal.widget.AutoScrollHelper.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.internal.widget.AutoScrollHelper.<clinit>():void");
    }

    public abstract boolean canTargetScrollHorizontally(int i);

    public abstract boolean canTargetScrollVertically(int i);

    public abstract void scrollTargetBy(int i, int i2);

    public AutoScrollHelper(View target) {
        this.mScroller = new ClampedScroller();
        this.mEdgeInterpolator = new AccelerateInterpolator();
        this.mRelativeEdges = new float[]{RELATIVE_UNSPECIFIED, RELATIVE_UNSPECIFIED};
        this.mMaximumEdges = new float[]{NO_MAX, NO_MAX};
        this.mRelativeVelocity = new float[]{RELATIVE_UNSPECIFIED, RELATIVE_UNSPECIFIED};
        this.mMinimumVelocity = new float[]{RELATIVE_UNSPECIFIED, RELATIVE_UNSPECIFIED};
        this.mMaximumVelocity = new float[]{NO_MAX, NO_MAX};
        this.mTarget = target;
        DisplayMetrics metrics = Resources.getSystem().getDisplayMetrics();
        int maxVelocity = (int) ((metrics.density * 1575.0f) + 0.5f);
        int minVelocity = (int) ((metrics.density * 315.0f) + 0.5f);
        setMaximumVelocity((float) maxVelocity, (float) maxVelocity);
        setMinimumVelocity((float) minVelocity, (float) minVelocity);
        setEdgeType(VERTICAL);
        setMaximumEdges(NO_MAX, NO_MAX);
        setRelativeEdges(DEFAULT_RELATIVE_EDGE, DEFAULT_RELATIVE_EDGE);
        setRelativeVelocity(DEFAULT_RELATIVE_VELOCITY, DEFAULT_RELATIVE_VELOCITY);
        setActivationDelay(DEFAULT_ACTIVATION_DELAY);
        setRampUpDuration(DEFAULT_RAMP_UP_DURATION);
        setRampDownDuration(DEFAULT_RAMP_UP_DURATION);
    }

    public AutoScrollHelper setEnabled(boolean enabled) {
        if (this.mEnabled && !enabled) {
            requestStop();
        }
        this.mEnabled = enabled;
        return this;
    }

    public boolean isEnabled() {
        return this.mEnabled;
    }

    public AutoScrollHelper setExclusive(boolean exclusive) {
        this.mExclusive = exclusive;
        return this;
    }

    public boolean isExclusive() {
        return this.mExclusive;
    }

    public AutoScrollHelper setMaximumVelocity(float horizontalMax, float verticalMax) {
        this.mMaximumVelocity[HORIZONTAL] = horizontalMax / 1000.0f;
        this.mMaximumVelocity[VERTICAL] = verticalMax / 1000.0f;
        return this;
    }

    public AutoScrollHelper setMinimumVelocity(float horizontalMin, float verticalMin) {
        this.mMinimumVelocity[HORIZONTAL] = horizontalMin / 1000.0f;
        this.mMinimumVelocity[VERTICAL] = verticalMin / 1000.0f;
        return this;
    }

    public AutoScrollHelper setRelativeVelocity(float horizontal, float vertical) {
        this.mRelativeVelocity[HORIZONTAL] = horizontal / 1000.0f;
        this.mRelativeVelocity[VERTICAL] = vertical / 1000.0f;
        return this;
    }

    public AutoScrollHelper setEdgeType(int type) {
        this.mEdgeType = type;
        return this;
    }

    public AutoScrollHelper setRelativeEdges(float horizontal, float vertical) {
        this.mRelativeEdges[HORIZONTAL] = horizontal;
        this.mRelativeEdges[VERTICAL] = vertical;
        return this;
    }

    public AutoScrollHelper setMaximumEdges(float horizontalMax, float verticalMax) {
        this.mMaximumEdges[HORIZONTAL] = horizontalMax;
        this.mMaximumEdges[VERTICAL] = verticalMax;
        return this;
    }

    public AutoScrollHelper setActivationDelay(int delayMillis) {
        this.mActivationDelay = delayMillis;
        return this;
    }

    public AutoScrollHelper setRampUpDuration(int durationMillis) {
        this.mScroller.setRampUpDuration(durationMillis);
        return this;
    }

    public AutoScrollHelper setRampDownDuration(int durationMillis) {
        this.mScroller.setRampDownDuration(durationMillis);
        return this;
    }

    public boolean onTouch(View v, MotionEvent event) {
        boolean z = false;
        if (!this.mEnabled) {
            return false;
        }
        switch (event.getActionMasked()) {
            case HORIZONTAL /*0*/:
                this.mNeedsCancel = true;
                this.mAlreadyDelayed = false;
                break;
            case VERTICAL /*1*/:
            case HwCfgFilePolicy.BASE /*3*/:
                requestStop();
                break;
            case EDGE_TYPE_OUTSIDE /*2*/:
                break;
        }
        this.mScroller.setTargetVelocity(computeTargetVelocity(HORIZONTAL, event.getX(), (float) v.getWidth(), (float) this.mTarget.getWidth()), computeTargetVelocity(VERTICAL, event.getY(), (float) v.getHeight(), (float) this.mTarget.getHeight()));
        if (!this.mAnimating && shouldAnimate()) {
            startAnimating();
        }
        if (this.mExclusive) {
            z = this.mAnimating;
        }
        return z;
    }

    private boolean shouldAnimate() {
        ClampedScroller scroller = this.mScroller;
        int verticalDirection = scroller.getVerticalDirection();
        int horizontalDirection = scroller.getHorizontalDirection();
        if (verticalDirection != 0 && canTargetScrollVertically(verticalDirection)) {
            return true;
        }
        if (horizontalDirection != 0) {
            return canTargetScrollHorizontally(horizontalDirection);
        }
        return false;
    }

    private void startAnimating() {
        if (this.mRunnable == null) {
            this.mRunnable = new ScrollAnimationRunnable();
        }
        this.mAnimating = true;
        this.mNeedsReset = true;
        if (this.mAlreadyDelayed || this.mActivationDelay <= 0) {
            this.mRunnable.run();
        } else {
            this.mTarget.postOnAnimationDelayed(this.mRunnable, (long) this.mActivationDelay);
        }
        this.mAlreadyDelayed = true;
    }

    private void requestStop() {
        if (this.mNeedsReset) {
            this.mAnimating = false;
        } else {
            this.mScroller.requestStop();
        }
    }

    private float computeTargetVelocity(int direction, float coordinate, float srcSize, float dstSize) {
        float value = getEdgeValue(this.mRelativeEdges[direction], srcSize, this.mMaximumEdges[direction], coordinate);
        if (value == RELATIVE_UNSPECIFIED) {
            return RELATIVE_UNSPECIFIED;
        }
        float relativeVelocity = this.mRelativeVelocity[direction];
        float minimumVelocity = this.mMinimumVelocity[direction];
        float maximumVelocity = this.mMaximumVelocity[direction];
        float targetVelocity = relativeVelocity * dstSize;
        if (value > RELATIVE_UNSPECIFIED) {
            return constrain(value * targetVelocity, minimumVelocity, maximumVelocity);
        }
        return -constrain((-value) * targetVelocity, minimumVelocity, maximumVelocity);
    }

    private float getEdgeValue(float relativeValue, float size, float maxValue, float current) {
        float interpolated;
        float edgeSize = constrain(relativeValue * size, (float) RELATIVE_UNSPECIFIED, maxValue);
        float value = constrainEdgeValue(size - current, edgeSize) - constrainEdgeValue(current, edgeSize);
        if (value < RELATIVE_UNSPECIFIED) {
            interpolated = -this.mEdgeInterpolator.getInterpolation(-value);
        } else if (value <= RELATIVE_UNSPECIFIED) {
            return RELATIVE_UNSPECIFIED;
        } else {
            interpolated = this.mEdgeInterpolator.getInterpolation(value);
        }
        return constrain(interpolated, (float) LayoutParams.BRIGHTNESS_OVERRIDE_NONE, (float) DEFAULT_RELATIVE_VELOCITY);
    }

    private float constrainEdgeValue(float current, float leading) {
        if (leading == RELATIVE_UNSPECIFIED) {
            return RELATIVE_UNSPECIFIED;
        }
        switch (this.mEdgeType) {
            case HORIZONTAL /*0*/:
            case VERTICAL /*1*/:
                if (current < leading) {
                    if (current >= RELATIVE_UNSPECIFIED) {
                        return DEFAULT_RELATIVE_VELOCITY - (current / leading);
                    }
                    return (this.mAnimating && this.mEdgeType == VERTICAL) ? DEFAULT_RELATIVE_VELOCITY : RELATIVE_UNSPECIFIED;
                }
                break;
            case EDGE_TYPE_OUTSIDE /*2*/:
                if (current < RELATIVE_UNSPECIFIED) {
                    return current / (-leading);
                }
                break;
        }
    }

    private static int constrain(int value, int min, int max) {
        if (value > max) {
            return max;
        }
        if (value < min) {
            return min;
        }
        return value;
    }

    private static float constrain(float value, float min, float max) {
        if (value > max) {
            return max;
        }
        if (value < min) {
            return min;
        }
        return value;
    }

    private void cancelTargetTouch() {
        long eventTime = SystemClock.uptimeMillis();
        MotionEvent cancel = MotionEvent.obtain(eventTime, eventTime, 3, RELATIVE_UNSPECIFIED, RELATIVE_UNSPECIFIED, HORIZONTAL);
        this.mTarget.onTouchEvent(cancel);
        cancel.recycle();
    }
}
