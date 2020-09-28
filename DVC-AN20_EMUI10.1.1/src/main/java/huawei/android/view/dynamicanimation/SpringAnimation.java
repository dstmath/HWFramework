package huawei.android.view.dynamicanimation;

import android.os.Looper;
import android.util.AndroidRuntimeException;
import huawei.android.view.dynamicanimation.DynamicAnimation;

public final class SpringAnimation extends DynamicAnimation<SpringAnimation> {
    private static final int DELTA_HALF = 2;
    private static final float UNSET = Float.MAX_VALUE;
    private boolean mIsEndRequested = false;
    private float mPendingPosition = UNSET;
    private SpringForce mSpring = null;

    public SpringAnimation(FloatValueHolder floatValueHolder) {
        super(floatValueHolder);
    }

    public <K> SpringAnimation(K object, FloatPropertyCompat<K> property) {
        super(object, property);
    }

    public <K> SpringAnimation(K object, FloatPropertyCompat<K> property, float finalPosition) {
        super(object, property);
        this.mSpring = new SpringForce(finalPosition);
    }

    public SpringForce getSpring() {
        return this.mSpring;
    }

    public SpringAnimation setSpring(SpringForce force) {
        this.mSpring = force;
        return this;
    }

    @Override // huawei.android.view.dynamicanimation.DynamicAnimation
    public void start() {
        sanityCheck();
        this.mSpring.setValueThreshold((double) getValueThreshold());
        super.start();
    }

    public void animateToFinalPosition(float finalPosition) {
        if (isRunning()) {
            this.mPendingPosition = finalPosition;
            return;
        }
        if (this.mSpring == null) {
            this.mSpring = new SpringForce(finalPosition);
        }
        this.mSpring.setFinalPosition(finalPosition);
        start();
    }

    public void skipToEnd() {
        if (!canSkipToEnd()) {
            throw new UnsupportedOperationException("Spring animations can only come to an end when there is damping");
        } else if (Looper.myLooper() != Looper.getMainLooper()) {
            throw new AndroidRuntimeException("Animations may only be started on the main thread");
        } else if (this.mIsRunning) {
            this.mIsEndRequested = true;
        }
    }

    public boolean canSkipToEnd() {
        return this.mSpring.mDampingRatio > 0.0d;
    }

    private void sanityCheck() {
        SpringForce springForce = this.mSpring;
        if (springForce != null) {
            double finalPosition = (double) springForce.getFinalPosition();
            if (finalPosition > ((double) this.mMaxValue)) {
                throw new UnsupportedOperationException("Final position of the spring cannot be greater than the max value.");
            } else if (finalPosition < ((double) this.mMinValue)) {
                throw new UnsupportedOperationException("Final position of the spring cannot be less than the min value.");
            }
        } else {
            throw new UnsupportedOperationException("Incomplete SpringAnimation: Either final position or a spring force needs to be set.");
        }
    }

    /* access modifiers changed from: package-private */
    @Override // huawei.android.view.dynamicanimation.DynamicAnimation
    public boolean updateValueAndVelocity(long deltaT) {
        if (this.mIsEndRequested) {
            float f = this.mPendingPosition;
            if (f != UNSET) {
                this.mSpring.setFinalPosition(f);
                this.mPendingPosition = UNSET;
            }
            this.mValue = this.mSpring.getFinalPosition();
            this.mVelocity = 0.0f;
            this.mIsEndRequested = false;
            return true;
        }
        if (this.mPendingPosition != UNSET) {
            DynamicAnimation.MassState massState = this.mSpring.updateValues((double) this.mValue, (double) this.mVelocity, deltaT / 2);
            this.mSpring.setFinalPosition(this.mPendingPosition);
            this.mPendingPosition = UNSET;
            DynamicAnimation.MassState massState2 = this.mSpring.updateValues((double) massState.mValue, (double) massState.mVelocity, deltaT / 2);
            this.mValue = massState2.mValue;
            this.mVelocity = massState2.mVelocity;
        } else {
            DynamicAnimation.MassState massState3 = this.mSpring.updateValues((double) this.mValue, (double) this.mVelocity, deltaT);
            this.mValue = massState3.mValue;
            this.mVelocity = massState3.mVelocity;
        }
        this.mValue = this.mValue >= this.mMinValue ? this.mValue : this.mMinValue;
        this.mValue = this.mValue <= this.mMaxValue ? this.mValue : this.mMaxValue;
        if (!isAtEquilibrium(this.mValue, this.mVelocity)) {
            return false;
        }
        this.mValue = this.mSpring.getFinalPosition();
        this.mVelocity = 0.0f;
        return true;
    }

    /* access modifiers changed from: package-private */
    @Override // huawei.android.view.dynamicanimation.DynamicAnimation
    public float getAcceleration(float value, float velocity) {
        return this.mSpring.getAcceleration(value, velocity);
    }

    /* access modifiers changed from: package-private */
    @Override // huawei.android.view.dynamicanimation.DynamicAnimation
    public boolean isAtEquilibrium(float value, float velocity) {
        return this.mSpring.isAtEquilibrium(value, velocity);
    }

    /* access modifiers changed from: package-private */
    @Override // huawei.android.view.dynamicanimation.DynamicAnimation
    public void setValueThreshold(float threshold) {
    }
}
