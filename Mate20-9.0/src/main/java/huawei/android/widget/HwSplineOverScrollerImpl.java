package huawei.android.widget;

import android.content.Context;
import android.view.ViewConfiguration;
import android.view.animation.AnimationUtils;
import android.view.animation.Interpolator;
import android.widget.IHwSplineOverScroller;
import android.widget.OverScroller;

public class HwSplineOverScrollerImpl implements IHwSplineOverScroller {
    private static final int BALLISTIC_DURATION_MAX = 135;
    private static final float COMPENSATION_RATE = 0.1f;
    private static final int CUBIC_DURATION_MAX = 400;
    private static final int DELTA_COMPENSATED_THRESHOLD = 5;
    private static final float DISTANCE_MODIFY = 1.8f;
    private static final float DURTION_EXP = 0.2f;
    private static final float DURTION_MODIFY = 2.2f;
    private static final float INFLEXION_DISTANCE = 0.36f;
    private static final float INFLEXION_DURTION = 0.8f;
    private static final float VELOCITY_EXP = 0.45f;
    private static final int VELOCITY_MAX = 24000;
    private static final int VELOCITY_THRESHOLD = 5000;
    private int mAdjustBallisticDuration = 0;
    private int mItemHeight = 0;
    private double mLastDistance = 0.0d;
    private double mLastDistanceActual = 0.0d;
    private int mMaximumVelocity;
    private Interpolator mOvershootInterpolator;
    private OverScroller.SplineOverScroller mSos;
    private Interpolator mSpringbackInterpolator;

    public HwSplineOverScrollerImpl(OverScroller.SplineOverScroller sos, Context context) {
        this.mSos = sos;
        this.mSpringbackInterpolator = AnimationUtils.loadInterpolator(context, 34078720);
        this.mOvershootInterpolator = AnimationUtils.loadInterpolator(context, 34078721);
        this.mMaximumVelocity = ViewConfiguration.get(context).getScaledMaximumFlingVelocity();
    }

    public void resetLastDistanceValue(double lastDistance, double lastDistanceActual) {
        this.mLastDistance = lastDistance;
        this.mLastDistanceActual = lastDistanceActual;
    }

    public void setStableItemHeight(int h) {
        this.mItemHeight = h;
    }

    public double adjustDistance(double oirginalDistance) {
        double distance = oirginalDistance;
        if (Math.abs(this.mSos.getSplineOverScrollerVelocity()) < VELOCITY_THRESHOLD || this.mItemHeight <= 0) {
            return distance;
        }
        int h = this.mItemHeight;
        if (Math.abs(distance - this.mLastDistance) > ((double) h) / 2.0d) {
            long acutualDelta = Math.round((distance - this.mLastDistanceActual) / ((double) h)) * ((long) h);
            this.mLastDistance = distance;
            double distance2 = this.mLastDistanceActual + ((double) acutualDelta);
            this.mLastDistanceActual = distance2;
            distance = distance2;
        } else {
            double deltaTotal = this.mLastDistance - this.mLastDistanceActual;
            this.mLastDistance = distance;
            if (Math.abs(deltaTotal) > 5.0d) {
                distance -= 0.8999999761581421d * deltaTotal;
            }
            this.mLastDistanceActual = distance;
        }
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
        float ratio = (1.0f * ((float) Math.abs(originalVelocity))) / ((float) this.mMaximumVelocity);
        int adjustVelocity = (int) (Math.sqrt((double) (2.0f * Math.abs(acceleration) * ((float) maxOver))) * Math.pow((double) ratio, 0.44999998807907104d) * ((double) Math.signum((float) originalVelocity)));
        this.mAdjustBallisticDuration = (int) (135.0d * Math.pow((double) ratio, 0.20000000298023224d));
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

    public double getSplineFlingDistance(double orignDistance, int velocity, double decelerationRate, float flingFriction, float physicalCoeff) {
        int velocity2;
        if (Math.abs(velocity) > VELOCITY_MAX) {
            velocity2 = (int) (24000.0f * Math.signum((float) velocity));
        } else {
            velocity2 = velocity;
        }
        if (flingFriction * physicalCoeff != 0.0f) {
            return ((double) (flingFriction * getSplineFlingDistanceModify(velocity2) * physicalCoeff)) * Math.exp((decelerationRate / (decelerationRate - 1.0d)) * Math.log((double) ((INFLEXION_DISTANCE * ((float) Math.abs(velocity2))) / (flingFriction * physicalCoeff))));
        }
        return orignDistance;
    }

    public int getSplineFlingDuration(int orignDurtion, int velocity, double decelerationRate, float flingFriction, float physicalCoeff) {
        if (Math.abs(velocity) > VELOCITY_MAX) {
            velocity = (int) (24000.0f * Math.signum((float) velocity));
        }
        if (flingFriction * physicalCoeff != 0.0f) {
            return (int) (1000.0d * ((double) getSplineFlingDurationModify(velocity)) * Math.exp(Math.log((double) ((INFLEXION_DURTION * ((float) Math.abs(velocity))) / (flingFriction * physicalCoeff))) / (decelerationRate - 1.0d)));
        }
        return orignDurtion;
    }
}
