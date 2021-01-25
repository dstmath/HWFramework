package huawei.android.view.dynamicanimation;

import huawei.android.view.dynamicanimation.DynamicAnimation;

public final class SpringForce implements Force {
    public static final float DAMPING_RATIO_HIGH_BOUNCY = 0.2f;
    public static final float DAMPING_RATIO_LOW_BOUNCY = 0.75f;
    public static final float DAMPING_RATIO_MEDIUM_BOUNCY = 0.5f;
    public static final float DAMPING_RATIO_NO_BOUNCY = 1.0f;
    private static final double DOUBLE_MINIMUM_VALUE = 1.0E-6d;
    private static final int DOUBLE_NATURE_FREQ_MULTIPLY = 2;
    public static final float STIFFNESS_HIGH = 10000.0f;
    public static final float STIFFNESS_LOW = 200.0f;
    public static final float STIFFNESS_MEDIUM = 1500.0f;
    public static final float STIFFNESS_VERY_LOW = 50.0f;
    private static final double TIME_ELAPSED_UNIT = 1000.0d;
    private static final double UNSET = Double.MAX_VALUE;
    private static final double VELOCITY_THRESHOLD_MULTIPLIER = 62.5d;
    private double mDampedFreq;
    double mDampingRatio = 0.5d;
    private double mFinalPosition = UNSET;
    private double mGammaMinus;
    private double mGammaPlus;
    private boolean mIsInitialized = false;
    private final DynamicAnimation.MassState mMassState = new DynamicAnimation.MassState();
    double mNaturalFreq = Math.sqrt(1500.0d);
    private double mValueThreshold;
    private double mVelocityThreshold;

    public SpringForce() {
    }

    public SpringForce(float finalPosition) {
        this.mFinalPosition = (double) finalPosition;
    }

    public SpringForce setStiffness(float stiffness) {
        if (stiffness > 0.0f) {
            this.mNaturalFreq = Math.sqrt((double) stiffness);
            this.mIsInitialized = false;
            return this;
        }
        throw new IllegalArgumentException("Spring stiffness constant must be positive.");
    }

    public float getStiffness() {
        double d = this.mNaturalFreq;
        return (float) (d * d);
    }

    public SpringForce setDampingRatio(float dampingRatio) {
        if (dampingRatio >= 0.0f) {
            this.mDampingRatio = (double) dampingRatio;
            this.mIsInitialized = false;
            return this;
        }
        throw new IllegalArgumentException("Damping ratio must be non-negative");
    }

    public float getDampingRatio() {
        return (float) this.mDampingRatio;
    }

    public SpringForce setFinalPosition(float finalPosition) {
        this.mFinalPosition = (double) finalPosition;
        return this;
    }

    public float getFinalPosition() {
        return (float) this.mFinalPosition;
    }

    @Override // huawei.android.view.dynamicanimation.Force
    public float getAcceleration(float lastDisplacement, float lastVelocity) {
        double d = this.mNaturalFreq;
        return (float) (((-(d * d)) * ((double) (lastDisplacement - getFinalPosition()))) - (((double) lastVelocity) * ((d * 2.0d) * this.mDampingRatio)));
    }

    @Override // huawei.android.view.dynamicanimation.Force
    public boolean isAtEquilibrium(float value, float velocity) {
        if (((double) Math.abs(velocity)) >= this.mVelocityThreshold || ((double) Math.abs(value - getFinalPosition())) >= this.mValueThreshold) {
            return false;
        }
        return true;
    }

    private void init() {
        if (!this.mIsInitialized) {
            if (Math.abs(this.mFinalPosition - UNSET) >= DOUBLE_MINIMUM_VALUE) {
                double d = this.mDampingRatio;
                if (d > 1.0d) {
                    double dampingRatioMultiNaturalFreq = (-d) * this.mNaturalFreq;
                    double sqrtDampingRatioMultiNaturalFreq = Math.sqrt((d * d) - 1.0d);
                    double d2 = this.mNaturalFreq;
                    this.mGammaPlus = (d2 * sqrtDampingRatioMultiNaturalFreq) + dampingRatioMultiNaturalFreq;
                    this.mGammaMinus = dampingRatioMultiNaturalFreq - (d2 * sqrtDampingRatioMultiNaturalFreq);
                } else if (d >= 0.0d && d < 1.0d) {
                    this.mDampedFreq = this.mNaturalFreq * Math.sqrt(1.0d - (d * d));
                }
                this.mIsInitialized = true;
                return;
            }
            throw new IllegalStateException("Error: Final position of the spring must be set before the animation starts");
        }
    }

    /* access modifiers changed from: package-private */
    public DynamicAnimation.MassState updateValues(double lastDisplacement, double lastVelocity, long timeElapsed) {
        double sinCoeff;
        double cosCoeff;
        init();
        double deltaT = ((double) timeElapsed) / TIME_ELAPSED_UNIT;
        double localLastDisplacement = lastDisplacement - this.mFinalPosition;
        double displacement = this.mDampingRatio;
        if (displacement > 1.0d) {
            double d = this.mGammaMinus;
            double d2 = this.mGammaPlus;
            double coeffA = localLastDisplacement - (((d * localLastDisplacement) - lastVelocity) / (d - d2));
            double coeffB = ((d * localLastDisplacement) - lastVelocity) / (d - d2);
            double displacement2 = (Math.pow(2.718281828459045d, d * deltaT) * coeffA) + (Math.pow(2.718281828459045d, this.mGammaPlus * deltaT) * coeffB);
            double d3 = this.mGammaMinus;
            double pow = coeffA * d3 * Math.pow(2.718281828459045d, d3 * deltaT);
            double d4 = this.mGammaPlus;
            sinCoeff = displacement2;
            cosCoeff = pow + (coeffB * d4 * Math.pow(2.718281828459045d, d4 * deltaT));
        } else if (Math.abs(displacement - 1.0d) < DOUBLE_MINIMUM_VALUE) {
            double d5 = this.mNaturalFreq;
            double coeffB2 = lastVelocity + (d5 * localLastDisplacement);
            sinCoeff = Math.pow(2.718281828459045d, (-d5) * deltaT) * (localLastDisplacement + (coeffB2 * deltaT));
            double pow2 = (localLastDisplacement + (coeffB2 * deltaT)) * Math.pow(2.718281828459045d, (-this.mNaturalFreq) * deltaT);
            double d6 = this.mNaturalFreq;
            cosCoeff = (pow2 * (-d6)) + (Math.pow(2.718281828459045d, (-d6) * deltaT) * coeffB2);
        } else {
            double d7 = 1.0d / this.mDampedFreq;
            double d8 = this.mDampingRatio;
            double d9 = this.mNaturalFreq;
            double sinCoeff2 = d7 * ((d8 * d9 * localLastDisplacement) + lastVelocity);
            double displacement3 = Math.pow(2.718281828459045d, (-d8) * d9 * deltaT) * ((Math.cos(this.mDampedFreq * deltaT) * localLastDisplacement) + (Math.sin(this.mDampedFreq * deltaT) * sinCoeff2));
            double d10 = this.mNaturalFreq;
            double localLastDisplacement2 = this.mDampingRatio;
            double d11 = (-d10) * displacement3 * localLastDisplacement2;
            double pow3 = Math.pow(2.718281828459045d, (-localLastDisplacement2) * d10 * deltaT);
            double d12 = this.mDampedFreq;
            double sin = (-d12) * localLastDisplacement * Math.sin(d12 * deltaT);
            double d13 = this.mDampedFreq;
            cosCoeff = d11 + (pow3 * (sin + (d13 * sinCoeff2 * Math.cos(d13 * deltaT))));
            sinCoeff = displacement3;
        }
        DynamicAnimation.MassState massState = this.mMassState;
        massState.mValue = (float) (this.mFinalPosition + sinCoeff);
        massState.mVelocity = (float) cosCoeff;
        return massState;
    }

    /* access modifiers changed from: package-private */
    public void setValueThreshold(double threshold) {
        this.mValueThreshold = Math.abs(threshold);
        this.mVelocityThreshold = this.mValueThreshold * VELOCITY_THRESHOLD_MULTIPLIER;
    }
}
