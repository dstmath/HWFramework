package huawei.android.widget;

import android.util.Log;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.HwSpringBackHelper;
import com.huawei.anim.dynamicanimation.DynamicAnimation;
import com.huawei.anim.dynamicanimation.SpringModelBase;
import com.huawei.anim.dynamicanimation.interpolator.FlingInterpolator;
import com.huawei.anim.dynamicanimation.interpolator.SpringInterpolator;
import com.huawei.anim.dynamicanimation.util.DynamicCurveRate;

public class HwSpringBackHelperImpl implements HwSpringBackHelper {
    private static final float CLOSE_ENOUGH = 0.001f;
    private static final float DEFAULT_DAMPING = 30.0f;
    private static final float DEFAULT_STIFFNESS = 228.0f;
    private static final float DEFAULT_THRESHOLD = 0.5f;
    private static final float DEFAULT_TIME_BASE = 1000.0f;
    private static final int FLING_MODE = 2;
    private static final int IDLE_MODE = 0;
    private static final float MAX_SCROLL_FACTOR = 0.5f;
    private static final int OVER_FLING_MODE = 3;
    private static final int SPRING_BACK_MODE = 1;
    private static final String TAG = "HwSpringBackHelper";
    private static final long USE_CURRENT_TIME = -1;
    private float mCurrVelocity;
    private int mCurrentPosition;
    private long mDuration;
    private int mFinalPosition;
    private FlingInterpolator mFlingInterpolator;
    private boolean mIsFinished = true;
    private int mMode = 0;
    private int mOverFlingDelta;
    private int mScrollMax;
    private int mScrollMin;
    private SpringInterpolator mSpringBackInterpolator = null;
    private HwSpringModel mSpringModel;
    private int mStart;
    private int mStartPosition;
    private long mStartTime;
    private View mTargetView;

    public boolean springBack(int startPosition, int minPosition, int maxPosition) {
        this.mMode = 1;
        this.mIsFinished = false;
        this.mStartTime = AnimationUtils.currentAnimationTimeMillis();
        this.mStartPosition = startPosition;
        int delta = 0;
        int i = this.mStartPosition;
        if (i < minPosition) {
            delta = i - minPosition;
            this.mFinalPosition = minPosition;
        } else if (i > maxPosition) {
            delta = i - maxPosition;
            this.mFinalPosition = maxPosition;
        } else {
            abortAnimation();
        }
        this.mSpringBackInterpolator = new SpringInterpolator(DynamicAnimation.SCROLL_Y, (float) DEFAULT_STIFFNESS, (float) DEFAULT_DAMPING, (float) delta);
        this.mDuration = (long) this.mSpringBackInterpolator.getDuration();
        return true ^ this.mIsFinished;
    }

    public void fling(View target, int startY, int velocityY, int minY, int maxY) {
        if (velocityY == 0) {
            abortAnimation();
            return;
        }
        this.mMode = 2;
        this.mFlingInterpolator = new FlingInterpolator((float) velocityY, 0.5f);
        this.mDuration = (long) this.mFlingInterpolator.getDuration();
        this.mIsFinished = false;
        this.mStartTime = AnimationUtils.currentAnimationTimeMillis();
        this.mCurrentPosition = startY;
        this.mStart = startY;
        this.mScrollMin = minY;
        this.mScrollMax = maxY;
        this.mTargetView = target;
        this.mCurrVelocity = (float) velocityY;
        this.mOverFlingDelta = 0;
    }

    public void overFling(View target, int endPosition) {
        this.mMode = 3;
        this.mCurrentPosition = endPosition;
        if (this.mTargetView == null) {
            if (target == null) {
                Log.e(TAG, "overFling: the target view is null.");
                abortAnimation();
                return;
            }
            this.mTargetView = target;
        }
        if (this.mCurrVelocity == 0.0f) {
            abortAnimation();
            return;
        }
        float startValue = (float) this.mOverFlingDelta;
        View view = this.mTargetView;
        if (view != null) {
            startValue += (float) view.getScrollY();
        }
        this.mSpringModel = new HwSpringModel(DEFAULT_STIFFNESS, DEFAULT_DAMPING, startValue, (float) endPosition, this.mCurrVelocity);
        this.mCurrentPosition = (int) startValue;
        this.mIsFinished = false;
    }

    public void overFling(float velocity, int beginPosition, int endPosition) {
        this.mMode = 3;
        this.mCurrentPosition = endPosition;
        if (velocity == 0.0f) {
            abortAnimation();
            return;
        }
        this.mSpringModel = new HwSpringModel(DEFAULT_STIFFNESS, DEFAULT_DAMPING, (float) beginPosition, (float) endPosition, velocity);
        this.mCurrentPosition = beginPosition;
        this.mCurrVelocity = velocity;
        this.mIsFinished = false;
        this.mStartTime = AnimationUtils.currentAnimationTimeMillis();
    }

    public boolean computeScrollOffset() {
        if (this.mIsFinished) {
            return false;
        }
        if (this.mMode == 3) {
            HwSpringModel hwSpringModel = this.mSpringModel;
            if (hwSpringModel != null) {
                this.mIsFinished = hwSpringModel.updateValues();
                this.mCurrentPosition = (int) this.mSpringModel.mCurrentValue;
                this.mCurrVelocity = this.mSpringModel.mVelocity;
            } else {
                Log.e(TAG, "computeScrollOffset mSpringModel is null");
                this.mIsFinished = true;
            }
            if (this.mIsFinished) {
                abortAnimation();
            }
            return !this.mIsFinished;
        } else if (this.mDuration <= 0) {
            abortAnimation();
            return false;
        } else {
            float progress = ((float) (AnimationUtils.currentAnimationTimeMillis() - this.mStartTime)) / ((float) this.mDuration);
            if (progress <= 1.0f) {
                this.mIsFinished = false;
                if (this.mMode == 2) {
                    this.mCurrentPosition = this.mStart + ((int) this.mFlingInterpolator.getInterpolateData(progress).getX());
                    this.mCurrVelocity = this.mFlingInterpolator.getInterpolateData(progress).getV();
                    int i = this.mCurrentPosition;
                    int i2 = this.mScrollMin;
                    if (i > i2 || this.mCurrVelocity >= 0.0f) {
                        int i3 = this.mCurrentPosition;
                        int i4 = this.mScrollMax;
                        if (i3 >= i4 && this.mCurrVelocity > 0.0f) {
                            this.mOverFlingDelta = i3 - i4;
                            overFling(this.mTargetView, i4);
                        }
                    } else {
                        this.mOverFlingDelta = i - i2;
                        overFling(this.mTargetView, i2);
                    }
                } else {
                    this.mCurrentPosition = (int) (((float) this.mStartPosition) - (this.mSpringBackInterpolator.getInterpolation(progress) * ((float) (this.mStartPosition - this.mFinalPosition))));
                }
            } else {
                abortAnimation();
            }
            return !this.mIsFinished;
        }
    }

    public int getCurrentOffset() {
        return this.mCurrentPosition;
    }

    public int getDynamicCurvedRateDelta(int viewHeight, int oldDelta, int currentPosition) {
        return (int) (((float) oldDelta) * new DynamicCurveRate(((float) viewHeight) * 0.5f).getRate((float) Math.abs(currentPosition)));
    }

    public boolean isFinished() {
        return this.mIsFinished;
    }

    public void abortAnimation() {
        this.mMode = 0;
        this.mCurrVelocity = 0.0f;
        this.mIsFinished = true;
    }

    /* access modifiers changed from: package-private */
    public class HwSpringModel extends SpringModelBase {
        private float mCurrentValue;
        private float mEndValue;
        private long mStartTime;
        private float mStartValue;
        private float mVelocity = 0.0f;

        HwSpringModel(float springConstant, float damping, float startValue, float endValue, float velocity) {
            super(springConstant, damping, HwSpringBackHelperImpl.CLOSE_ENOUGH);
            this.mStartValue = startValue;
            this.mCurrentValue = this.mStartValue;
            this.mEndValue = endValue;
            this.mVelocity = velocity;
            setValueThreshold(0.5f);
            snap(0.0f);
            setEndPosition(this.mEndValue - this.mStartValue, velocity, HwSpringBackHelperImpl.USE_CURRENT_TIME);
            this.mStartTime = AnimationUtils.currentAnimationTimeMillis();
        }

        /* access modifiers changed from: package-private */
        public boolean updateValues() {
            float deltaTime = ((float) (AnimationUtils.currentAnimationTimeMillis() - this.mStartTime)) / HwSpringBackHelperImpl.DEFAULT_TIME_BASE;
            this.mVelocity = getVelocity(deltaTime);
            float position = getPosition(deltaTime);
            float f = this.mStartValue;
            this.mCurrentValue = position + f;
            if (!isAtEquilibrium(this.mCurrentValue - f, this.mVelocity)) {
                return false;
            }
            this.mCurrentValue = getEndPosition() + this.mStartValue;
            this.mVelocity = 0.0f;
            return true;
        }
    }
}
