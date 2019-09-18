package android.widget;

import android.content.Context;
import android.view.ViewConfiguration;
import android.view.animation.AnimationUtils;
import android.view.animation.Interpolator;

public class Scroller {
    private static float DECELERATION_RATE = ((float) (Math.log(0.78d) / Math.log(0.9d)));
    private static final int DEFAULT_DURATION = 250;
    private static final float END_TENSION = 1.0f;
    private static final int FLING_MODE = 1;
    private static final float INFLEXION = 0.35f;
    private static final int NB_SAMPLES = 100;
    private static final float P1 = 0.175f;
    private static final float P2 = 0.35000002f;
    private static final int SCROLL_MODE = 0;
    private static final float[] SPLINE_POSITION = new float[101];
    private static final float[] SPLINE_TIME = new float[101];
    private static final float START_TENSION = 0.5f;
    private float mCurrVelocity;
    private int mCurrX;
    private int mCurrY;
    private float mDeceleration;
    private float mDeltaX;
    private float mDeltaY;
    private int mDistance;
    private int mDuration;
    private float mDurationReciprocal;
    private int mFinalX;
    private int mFinalY;
    private boolean mFinished;
    private float mFlingFriction;
    private boolean mFlywheel;
    private final Interpolator mInterpolator;
    private int mMaxX;
    private int mMaxY;
    private int mMinX;
    private int mMinY;
    private int mMode;
    private float mPhysicalCoeff;
    private final float mPpi;
    private long mStartTime;
    private int mStartX;
    private int mStartY;
    private float mVelocity;

    static class ViscousFluidInterpolator implements Interpolator {
        private static final float VISCOUS_FLUID_NORMALIZE = (1.0f / viscousFluid(1.0f));
        private static final float VISCOUS_FLUID_OFFSET = (1.0f - (VISCOUS_FLUID_NORMALIZE * viscousFluid(1.0f)));
        private static final float VISCOUS_FLUID_SCALE = 8.0f;

        ViscousFluidInterpolator() {
        }

        private static float viscousFluid(float x) {
            float x2 = x * VISCOUS_FLUID_SCALE;
            if (x2 < 1.0f) {
                return x2 - (1.0f - ((float) Math.exp((double) (-x2))));
            }
            return 0.36787945f + ((1.0f - 0.36787945f) * (1.0f - ((float) Math.exp((double) (1.0f - x2)))));
        }

        public float getInterpolation(float input) {
            float interpolated = VISCOUS_FLUID_NORMALIZE * viscousFluid(input);
            if (interpolated > 0.0f) {
                return VISCOUS_FLUID_OFFSET + interpolated;
            }
            return interpolated;
        }
    }

    static {
        float f;
        float x;
        float f2;
        float coef;
        float y;
        float coef2;
        float y_min;
        float x_min = 0.0f;
        float y_min2 = 0.0f;
        int i = 0;
        while (i < 100) {
            float alpha = ((float) i) / 100.0f;
            float x_min2 = x_min;
            float x_max = 1.0f;
            while (true) {
                f = 2.0f;
                x = ((x_max - x_min2) / 2.0f) + x_min2;
                f2 = 3.0f;
                coef = 3.0f * x * (1.0f - x);
                float tx = ((((1.0f - x) * P1) + (x * P2)) * coef) + (x * x * x);
                if (((double) Math.abs(tx - alpha)) < 1.0E-5d) {
                    break;
                } else if (tx > alpha) {
                    x_max = x;
                } else {
                    x_min2 = x;
                }
            }
            SPLINE_POSITION[i] = ((((1.0f - x) * START_TENSION) + x) * coef) + (x * x * x);
            float f3 = coef;
            float y_min3 = y_min2;
            float y_max = 1.0f;
            while (true) {
                y = ((y_max - y_min3) / f) + y_min3;
                coef2 = f2 * y * (1.0f - y);
                float dy = ((((1.0f - y) * START_TENSION) + y) * coef2) + (y * y * y);
                y_min = y_min3;
                if (((double) Math.abs(dy - alpha)) < 1.0E-5d) {
                    break;
                }
                if (dy > alpha) {
                    y_max = y;
                    y_min3 = y_min;
                } else {
                    y_min3 = y;
                }
                f = 2.0f;
                f2 = 3.0f;
            }
            SPLINE_TIME[i] = ((((1.0f - y) * P1) + (P2 * y)) * coef2) + (y * y * y);
            i++;
            x_min = x_min2;
            y_min2 = y_min;
        }
        float[] fArr = SPLINE_POSITION;
        SPLINE_TIME[100] = 1.0f;
        fArr[100] = 1.0f;
    }

    public Scroller(Context context) {
        this(context, null);
    }

    /* JADX INFO: this call moved to the top of the method (can break code semantics) */
    public Scroller(Context context, Interpolator interpolator) {
        this(context, interpolator, context.getApplicationInfo().targetSdkVersion >= 11);
    }

    public Scroller(Context context, Interpolator interpolator, boolean flywheel) {
        this.mFlingFriction = ViewConfiguration.getScrollFriction();
        this.mFinished = true;
        if (interpolator == null) {
            this.mInterpolator = new ViscousFluidInterpolator();
        } else {
            this.mInterpolator = interpolator;
        }
        this.mPpi = context.getResources().getDisplayMetrics().density * 160.0f;
        this.mDeceleration = computeDeceleration(ViewConfiguration.getScrollFriction());
        this.mFlywheel = flywheel;
        this.mPhysicalCoeff = computeDeceleration(0.84f);
    }

    public final void setFriction(float friction) {
        this.mDeceleration = computeDeceleration(friction);
        this.mFlingFriction = friction;
    }

    private float computeDeceleration(float friction) {
        return 386.0878f * this.mPpi * friction;
    }

    public final boolean isFinished() {
        return this.mFinished;
    }

    public final void forceFinished(boolean finished) {
        this.mFinished = finished;
    }

    public final int getDuration() {
        return this.mDuration;
    }

    public final int getCurrX() {
        return this.mCurrX;
    }

    public final int getCurrY() {
        return this.mCurrY;
    }

    public float getCurrVelocity() {
        return this.mMode == 1 ? this.mCurrVelocity : this.mVelocity - ((this.mDeceleration * ((float) timePassed())) / 2000.0f);
    }

    public final int getStartX() {
        return this.mStartX;
    }

    public final int getStartY() {
        return this.mStartY;
    }

    public final int getFinalX() {
        return this.mFinalX;
    }

    public final int getFinalY() {
        return this.mFinalY;
    }

    public boolean computeScrollOffset() {
        if (this.mFinished) {
            return false;
        }
        int timePassed = (int) (AnimationUtils.currentAnimationTimeMillis() - this.mStartTime);
        if (timePassed < this.mDuration) {
            switch (this.mMode) {
                case 0:
                    float x = this.mInterpolator.getInterpolation(((float) timePassed) * this.mDurationReciprocal);
                    this.mCurrX = this.mStartX + Math.round(this.mDeltaX * x);
                    this.mCurrY = this.mStartY + Math.round(this.mDeltaY * x);
                    break;
                case 1:
                    float t = ((float) timePassed) / ((float) this.mDuration);
                    int index = (int) (100.0f * t);
                    float distanceCoef = 1.0f;
                    float velocityCoef = 0.0f;
                    if (index < 100) {
                        float t_inf = ((float) index) / 100.0f;
                        float d_inf = SPLINE_POSITION[index];
                        velocityCoef = (SPLINE_POSITION[index + 1] - d_inf) / ((((float) (index + 1)) / 100.0f) - t_inf);
                        distanceCoef = d_inf + ((t - t_inf) * velocityCoef);
                    }
                    this.mCurrVelocity = ((((float) this.mDistance) * velocityCoef) / ((float) this.mDuration)) * 1000.0f;
                    this.mCurrX = this.mStartX + Math.round(((float) (this.mFinalX - this.mStartX)) * distanceCoef);
                    this.mCurrX = Math.min(this.mCurrX, this.mMaxX);
                    this.mCurrX = Math.max(this.mCurrX, this.mMinX);
                    this.mCurrY = this.mStartY + Math.round(((float) (this.mFinalY - this.mStartY)) * distanceCoef);
                    this.mCurrY = Math.min(this.mCurrY, this.mMaxY);
                    this.mCurrY = Math.max(this.mCurrY, this.mMinY);
                    if (this.mCurrX == this.mFinalX && this.mCurrY == this.mFinalY) {
                        this.mFinished = true;
                        break;
                    }
            }
        } else {
            this.mCurrX = this.mFinalX;
            this.mCurrY = this.mFinalY;
            this.mFinished = true;
        }
        return true;
    }

    public void startScroll(int startX, int startY, int dx, int dy) {
        startScroll(startX, startY, dx, dy, 250);
    }

    public void startScroll(int startX, int startY, int dx, int dy, int duration) {
        this.mMode = 0;
        this.mFinished = false;
        this.mDuration = duration;
        this.mStartTime = AnimationUtils.currentAnimationTimeMillis();
        this.mStartX = startX;
        this.mStartY = startY;
        this.mFinalX = startX + dx;
        this.mFinalY = startY + dy;
        this.mDeltaX = (float) dx;
        this.mDeltaY = (float) dy;
        this.mDurationReciprocal = 1.0f / ((float) this.mDuration);
    }

    public void fling(int startX, int startY, int velocityX, int velocityY, int minX, int maxX, int minY, int maxY) {
        int i = startX;
        int i2 = startY;
        int velocityX2 = velocityX;
        int velocityY2 = velocityY;
        if (this.mFlywheel && !this.mFinished) {
            float oldVel = getCurrVelocity();
            float dx = (float) (this.mFinalX - this.mStartX);
            float dy = (float) (this.mFinalY - this.mStartY);
            float hyp = (float) Math.hypot((double) dx, (double) dy);
            float oldVelocityX = (dx / hyp) * oldVel;
            float oldVelocityY = (dy / hyp) * oldVel;
            if (Math.signum((float) velocityX2) == Math.signum(oldVelocityX) && Math.signum((float) velocityY2) == Math.signum(oldVelocityY)) {
                velocityX2 = (int) (((float) velocityX2) + oldVelocityX);
                velocityY2 = (int) (((float) velocityY2) + oldVelocityY);
            }
        }
        this.mMode = 1;
        this.mFinished = false;
        float velocity = (float) Math.hypot((double) velocityX2, (double) velocityY2);
        this.mVelocity = velocity;
        this.mDuration = getSplineFlingDuration(velocity);
        this.mStartTime = AnimationUtils.currentAnimationTimeMillis();
        this.mStartX = i;
        this.mStartY = i2;
        float f = 1.0f;
        float coeffX = velocity == 0.0f ? 1.0f : ((float) velocityX2) / velocity;
        if (velocity != 0.0f) {
            f = ((float) velocityY2) / velocity;
        }
        float coeffY = f;
        double totalDistance = getSplineFlingDistance(velocity);
        this.mDistance = (int) (((double) Math.signum(velocity)) * totalDistance);
        this.mMinX = minX;
        this.mMaxX = maxX;
        this.mMinY = minY;
        this.mMaxY = maxY;
        this.mFinalX = ((int) Math.round(((double) coeffX) * totalDistance)) + i;
        this.mFinalX = Math.min(this.mFinalX, this.mMaxX);
        this.mFinalX = Math.max(this.mFinalX, this.mMinX);
        this.mFinalY = ((int) Math.round(((double) coeffY) * totalDistance)) + i2;
        this.mFinalY = Math.min(this.mFinalY, this.mMaxY);
        this.mFinalY = Math.max(this.mFinalY, this.mMinY);
    }

    private double getSplineDeceleration(float velocity) {
        return Math.log((double) ((INFLEXION * Math.abs(velocity)) / (this.mFlingFriction * this.mPhysicalCoeff)));
    }

    private int getSplineFlingDuration(float velocity) {
        return (int) (1000.0d * Math.exp(getSplineDeceleration(velocity) / (((double) DECELERATION_RATE) - 1.0d)));
    }

    private double getSplineFlingDistance(float velocity) {
        return ((double) (this.mFlingFriction * this.mPhysicalCoeff)) * Math.exp((((double) DECELERATION_RATE) / (((double) DECELERATION_RATE) - 1.0d)) * getSplineDeceleration(velocity));
    }

    public void abortAnimation() {
        this.mCurrX = this.mFinalX;
        this.mCurrY = this.mFinalY;
        this.mFinished = true;
    }

    public void extendDuration(int extend) {
        this.mDuration = timePassed() + extend;
        this.mDurationReciprocal = 1.0f / ((float) this.mDuration);
        this.mFinished = false;
    }

    public int timePassed() {
        return (int) (AnimationUtils.currentAnimationTimeMillis() - this.mStartTime);
    }

    public void setFinalX(int newX) {
        this.mFinalX = newX;
        this.mDeltaX = (float) (this.mFinalX - this.mStartX);
        this.mFinished = false;
    }

    public void setFinalY(int newY) {
        this.mFinalY = newY;
        this.mDeltaY = (float) (this.mFinalY - this.mStartY);
        this.mFinished = false;
    }

    public boolean isScrollingInDirection(float xvel, float yvel) {
        return !this.mFinished && Math.signum(xvel) == Math.signum((float) (this.mFinalX - this.mStartX)) && Math.signum(yvel) == Math.signum((float) (this.mFinalY - this.mStartY));
    }
}
