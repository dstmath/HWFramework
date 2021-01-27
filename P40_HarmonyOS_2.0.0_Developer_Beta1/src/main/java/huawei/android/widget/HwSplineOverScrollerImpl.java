package huawei.android.widget;

import android.content.Context;
import android.view.ViewConfiguration;
import android.view.animation.AnimationUtils;
import android.view.animation.Interpolator;
import android.widget.IHwSplineOverScroller;
import android.widget.OverScroller;

public class HwSplineOverScrollerImpl implements IHwSplineOverScroller {
    private static final int BALLISTIC_DURATION_MAX = 135;
    private static final float COMPARE_ZERO = 1.0E-6f;
    private static final float COMPENSATION_RATE = 0.1f;
    private static final int CUBIC_DURATION_MAX = 250;
    private static final float DECELERATION_RATE_ADJUST_FACTOR = 1.0f;
    private static final int DELTA_COMPENSATED_THRESHOLD = 5;
    private static final float DICHOTOMY_SIZE = 2.0f;
    private static final float DISTANCE_MODIFY = 1.8f;
    private static final float DISTANCE_MODIFY_DEFAULT = 1.0f;
    private static final float DURATION_ADJUST_FACTOR = 1000.0f;
    private static final float DURATION_MODIFY_DEFAULT = 1.0f;
    private static final float DURTION_EXP = 0.2f;
    private static final float DURTION_MODIFY = 2.2f;
    private static final float INFLEXION_DISTANCE = 0.36f;
    private static final float INFLEXION_DURTION = 0.8f;
    private static final float VELOCITY_ADJUST_FACTOR = 2.0f;
    private static final float VELOCITY_EXP = 0.45f;
    private static final int VELOCITY_MAX = 24000;
    private static final int VELOCITY_THRESHOLD = 5000;
    private int mAdjustBallisticDuration;
    private int mItemHeight;
    private double mLastDistance;
    private double mLastDistanceActual;
    private int mMaximumVelocity;
    private Interpolator mOvershootInterpolator;
    private OverScroller.SplineOverScroller mScroller;
    private Interpolator mSpringbackInterpolator;

    public HwSplineOverScrollerImpl(OverScroller.SplineOverScroller scroller, Context context) {
        this.mScroller = scroller;
        this.mSpringbackInterpolator = AnimationUtils.loadInterpolator(context, 34078720);
        this.mOvershootInterpolator = AnimationUtils.loadInterpolator(context, 34078721);
        this.mMaximumVelocity = ViewConfiguration.get(context).getScaledMaximumFlingVelocity();
    }

    public void resetLastDistanceValue(double lastDistance, double lastDistanceActual) {
        this.mLastDistance = lastDistance;
        this.mLastDistanceActual = lastDistanceActual;
    }

    public void setStableItemHeight(int height) {
        this.mItemHeight = height;
    }

    public double adjustDistance(double originalDistance) {
        double distance = originalDistance;
        if (Math.abs(this.mScroller.getSplineOverScrollerVelocity()) < VELOCITY_THRESHOLD || this.mItemHeight <= 0) {
            return distance;
        }
        int height = this.mItemHeight;
        if (Math.abs(distance - this.mLastDistance) > ((double) (((float) height) / 2.0f))) {
            long actualDelta = Math.round((distance - this.mLastDistanceActual) / ((double) height)) * ((long) height);
            this.mLastDistance = distance;
            double distance2 = this.mLastDistanceActual + ((double) actualDelta);
            this.mLastDistanceActual = distance2;
            return distance2;
        }
        double deltaTotal = this.mLastDistance - this.mLastDistanceActual;
        this.mLastDistance = distance;
        if (Math.abs(deltaTotal) > 5.0d) {
            distance -= 0.8999999761581421d * deltaTotal;
        }
        this.mLastDistanceActual = distance;
        return distance;
    }

    public double getBallisticDistance(double originalDistance, int start, int end, long duration, long currentTime) {
        return (double) (this.mOvershootInterpolator.getInterpolation(((float) currentTime) / ((float) duration)) * ((float) (end - start)));
    }

    public double getCubicDistance(double originalDistance, int start, int end, long duration, long currentTime) {
        return (double) (this.mSpringbackInterpolator.getInterpolation(((float) currentTime) / ((float) duration)) * ((float) (end - start)));
    }

    public int getBallisticDuration(int originalDuration) {
        return this.mAdjustBallisticDuration;
    }

    public int getCubicDuration(int originalDuration) {
        return CUBIC_DURATION_MAX;
    }

    public int adjustBallisticVelocity(int originalVelocity, float acceleration, int maxOver) {
        float ratio = (((float) Math.abs(originalVelocity)) * 1.0f) / ((float) this.mMaximumVelocity);
        int adjustVelocity = (int) (Math.sqrt((double) (Math.abs(acceleration) * 2.0f * ((float) maxOver))) * Math.pow((double) ratio, 0.44999998807907104d) * ((double) Math.signum((float) originalVelocity)));
        this.mAdjustBallisticDuration = (int) (Math.pow((double) ratio, 0.20000000298023224d) * 135.0d);
        return adjustVelocity;
    }

    private float getSplineFlingDurationModify(int velocity) {
        if (Math.abs(velocity) < VELOCITY_THRESHOLD) {
            return DURTION_MODIFY;
        }
        return 1.0f;
    }

    private float getSplineFlingDistanceModify(int velocity) {
        if (Math.abs(velocity) < VELOCITY_THRESHOLD) {
            return DISTANCE_MODIFY;
        }
        return 1.0f;
    }

    private int getAdjustVelocity(int velocity) {
        if (Math.abs(velocity) > VELOCITY_MAX) {
            return (int) (Math.signum((float) velocity) * 24000.0f);
        }
        return velocity;
    }

    private double getSplineDeceleration(float inflexion, int velocity, float flingFriction, float physicalCoeff) {
        return Math.log((double) ((((float) Math.abs(velocity)) * inflexion) / (flingFriction * physicalCoeff)));
    }

    public double getSplineFlingDistance(double orignDistance, int velocity, double decelerationRate, float flingFriction, float physicalCoeff) {
        if (Math.abs(flingFriction * physicalCoeff) <= COMPARE_ZERO) {
            return orignDistance;
        }
        int tempVelocity = getAdjustVelocity(velocity);
        return ((double) (flingFriction * getSplineFlingDistanceModify(tempVelocity) * physicalCoeff)) * Math.exp((decelerationRate / (decelerationRate - 1.0d)) * getSplineDeceleration(INFLEXION_DISTANCE, tempVelocity, flingFriction, physicalCoeff));
    }

    public int getSplineFlingDuration(int orignDuration, int velocity, double decelerationRate, float flingFriction, float physicalCoeff) {
        if (Math.abs(flingFriction * physicalCoeff) <= COMPARE_ZERO) {
            return orignDuration;
        }
        int tempVelocity = getAdjustVelocity(velocity);
        return (int) (((double) (DURATION_ADJUST_FACTOR * getSplineFlingDurationModify(tempVelocity))) * Math.exp(getSplineDeceleration(INFLEXION_DURTION, tempVelocity, flingFriction, physicalCoeff) / (decelerationRate - 1.0d)));
    }
}
