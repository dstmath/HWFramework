package huawei.android.widget;

import android.content.Context;
import android.view.ViewConfiguration;
import android.view.animation.AnimationUtils;
import android.view.animation.Interpolator;
import android.widget.IHwSplineOverScroller;
import android.widget.OverScroller.SplineOverScroller;

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
    private SplineOverScroller mSos;
    private Interpolator mSpringbackInterpolator;

    public HwSplineOverScrollerImpl(SplineOverScroller sos, Context context) {
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
            return oirginalDistance;
        }
        int h = this.mItemHeight;
        if (Math.abs(oirginalDistance - this.mLastDistance) > ((double) h) / 2.0d) {
            long acutualDelta = Math.round((oirginalDistance - this.mLastDistanceActual) / ((double) h)) * ((long) h);
            this.mLastDistance = oirginalDistance;
            distance = this.mLastDistanceActual + ((double) acutualDelta);
            this.mLastDistanceActual = distance;
        } else {
            double deltaTotal = this.mLastDistance - this.mLastDistanceActual;
            this.mLastDistance = oirginalDistance;
            if (Math.abs(deltaTotal) > 5.0d) {
                distance = oirginalDistance - (0.8999999761581421d * deltaTotal);
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
        float ratio = (((float) Math.abs(originalVelocity)) * 1.0f) / ((float) this.mMaximumVelocity);
        int adjustVelocity = (int) ((Math.sqrt((double) ((Math.abs(acceleration) * 2.0f) * ((float) maxOver))) * Math.pow((double) ratio, 0.44999998807907104d)) * ((double) Math.signum((float) originalVelocity)));
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

    public double getSplineFlingDistance(double orignDistance, int velocity, double decelerationRate, float flingFriction, float physicalCoeff) {
        if (Math.abs(velocity) > VELOCITY_MAX) {
            velocity = (int) (Math.signum((float) velocity) * 24000.0f);
        }
        if (flingFriction * physicalCoeff == 0.0f) {
            return orignDistance;
        }
        return ((double) ((flingFriction * getSplineFlingDistanceModify(velocity)) * physicalCoeff)) * Math.exp((decelerationRate / (decelerationRate - 1.0d)) * Math.log((double) ((((float) Math.abs(velocity)) * INFLEXION_DISTANCE) / (flingFriction * physicalCoeff))));
    }

    public int getSplineFlingDuration(int orignDurtion, int velocity, double decelerationRate, float flingFriction, float physicalCoeff) {
        if (Math.abs(velocity) > VELOCITY_MAX) {
            velocity = (int) (Math.signum((float) velocity) * 24000.0f);
        }
        if (flingFriction * physicalCoeff == 0.0f) {
            return orignDurtion;
        }
        return (int) ((((double) getSplineFlingDurationModify(velocity)) * 1000.0d) * Math.exp(Math.log((double) ((((float) Math.abs(velocity)) * 0.8f) / (flingFriction * physicalCoeff))) / (decelerationRate - 1.0d)));
    }
}
