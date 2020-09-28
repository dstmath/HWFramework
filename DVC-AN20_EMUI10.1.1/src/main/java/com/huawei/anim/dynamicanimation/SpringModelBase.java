package com.huawei.anim.dynamicanimation;

import android.os.SystemClock;

public class SpringModelBase extends PhysicalModelBase {
    public static final float DEFAULT_DAMPING = 15.0f;
    private static final float DEFAULT_ESTIMATE_DURATION = 500.0f;
    public static final float DEFAULT_MASS = 1.0f;
    public static final float DEFAULT_STIFFNESS = 800.0f;
    public static final float DEFAULT_VALUE_THRESHOLD = 0.001f;
    private static final int DIST_NUM = 16;
    public static final float MAXIMUM_DAMPING = 99.0f;
    public static final float MAXIMUM_MASS = 1.0f;
    public static final float MAXIMUM_STIFFNESS = 999.0f;
    private static final float MAX_ITERATION_NUM = 999.0f;
    public static final float MINIMUM_DAMPING = 1.0f;
    public static final float MINIMUM_MASS = 1.0f;
    public static final float MINIMUM_STIFFNESS = 1.0f;
    private float mDamping = 15.0f;
    private float mMass = 1.0f;
    private Solution mSolution;
    private float mStiffness = 800.0f;

    public SpringModelBase(float stiffness, float damping, float valueThreshold) {
        super.setValueThreshold(valueThreshold);
        this.mMass = 1.0f;
        this.mStiffness = Math.min(Math.max(1.0f, stiffness), 999.0f);
        this.mDamping = Math.min(Math.max(1.0f, damping), 99.0f);
        this.mSolution = null;
        this.mStartPosition = 0.0f;
        this.mEndPosition = 0.0f;
        this.mStartVelocity = 0.0f;
        this.mStartTime = 0;
    }

    public SpringModelBase(float stiffness, float damping) {
        super.setValueThreshold(0.001f);
        this.mMass = 1.0f;
        this.mStiffness = Math.min(Math.max(1.0f, stiffness), 999.0f);
        this.mDamping = Math.min(Math.max(1.0f, damping), 99.0f);
        this.mSolution = null;
        this.mStartPosition = 0.0f;
        this.mEndPosition = 0.0f;
        this.mStartVelocity = 0.0f;
        this.mStartTime = 0;
    }

    /* access modifiers changed from: private */
    public abstract class Solution {
        protected float mDDX;
        protected float mDX;
        protected float mDuration;
        protected float mX;
        private float[] mXDist;

        /* access modifiers changed from: protected */
        public abstract void doEstimateDuration();

        /* access modifiers changed from: protected */
        public abstract float estimateDuration();

        /* access modifiers changed from: protected */
        public abstract float getDDX(float f);

        /* access modifiers changed from: protected */
        public abstract float getDX(float f);

        /* access modifiers changed from: protected */
        public abstract float getFirstExtremumX();

        /* access modifiers changed from: protected */
        public abstract float getMaxAbsX();

        /* access modifiers changed from: protected */
        public abstract float getX(float f);

        protected Solution() {
            this.mXDist = new float[17];
            this.mX = 0.0f;
            this.mDX = 0.0f;
            this.mDDX = 0.0f;
            this.mDuration = 0.0f;
        }

        protected Solution(float x0, float dx0, float ddx0) {
            this.mXDist = new float[17];
            this.mX = x0;
            this.mDX = dx0;
            this.mDDX = ddx0;
            this.mDuration = 0.0f;
        }

        private float getStartTForX(float x, float startT, float endT) {
            float delta = (endT - startT) / 16.0f;
            boolean bIncremental = getDX((endT + startT) / 2.0f) > 0.0f;
            for (int i = 1; i < 17; i++) {
                float[] fArr = this.mXDist;
                float xRange = fArr[i] - fArr[i - 1];
                if (!bIncremental || fArr[i] < x) {
                    if (!bIncremental) {
                        float[] fArr2 = this.mXDist;
                        if (fArr2[i] <= x) {
                            if (xRange == 0.0f) {
                                return (((float) (i - 1)) * delta) + startT;
                            }
                            return ((((float) i) - ((fArr2[i] - x) / xRange)) * delta) + startT;
                        }
                    }
                } else if (xRange == 0.0f) {
                    return (((float) (i - 1)) * delta) + startT;
                } else {
                    return ((((float) (i - 1)) + ((x - fArr[i - 1]) / xRange)) * delta) + startT;
                }
            }
            return endT;
        }

        /* access modifiers changed from: protected */
        public float doIterate(float startT, float endT) {
            float deltaT = (endT - startT) / 16.0f;
            float xT = SpringModelBase.this.mValueThreshold;
            for (int i = 0; i < 17; i++) {
                this.mXDist[i] = getX((((float) i) * deltaT) + startT);
            }
            boolean found = false;
            int i2 = 1;
            while (true) {
                if (i2 >= 17) {
                    break;
                } else if ((this.mXDist[i2 - 1] - SpringModelBase.this.mValueThreshold) * (this.mXDist[i2] - SpringModelBase.this.mValueThreshold) < 0.0f) {
                    xT = SpringModelBase.this.mValueThreshold;
                    found = true;
                    break;
                } else if ((this.mXDist[i2 - 1] + SpringModelBase.this.mValueThreshold) * (this.mXDist[i2] + SpringModelBase.this.mValueThreshold) < 0.0f) {
                    xT = -SpringModelBase.this.mValueThreshold;
                    found = true;
                    break;
                } else {
                    i2++;
                }
            }
            if (!found) {
                return startT;
            }
            float startT2 = getStartTForX(xT, startT, endT);
            while (Math.abs(getX(startT2)) < SpringModelBase.this.mValueThreshold && endT - startT2 >= 0.0625f) {
                endT = startT2;
                float deltaT2 = (endT - startT) / 16.0f;
                for (int i3 = 0; i3 < 17; i3++) {
                    this.mXDist[i3] = getX((((float) i3) * deltaT2) + startT);
                }
                startT2 = getStartTForX(xT, startT, endT);
            }
            float i4 = 0.0f;
            float xi = getX(startT2);
            float dxi = getDX(startT2);
            while (true) {
                if (!SpringModelBase.this.almostGreaterThan(Math.abs(xi), SpringModelBase.this.mValueThreshold, 0.0f)) {
                    break;
                }
                float i5 = 1.0f + i4;
                if (i4 >= 999.0f) {
                    i4 = i5;
                    break;
                }
                startT2 -= xi / dxi;
                xi = getX(startT2);
                dxi = getDX(startT2);
                i4 = i5;
            }
            return getIterate(i4, startT2);
        }

        private float getIterate(float i, float startT) {
            if (i <= 999.0f) {
                return startT;
            }
            return -1.0f;
        }
    }

    /* access modifiers changed from: private */
    public class Solution0 extends Solution {
        private Solution0() {
            super();
        }

        @Override // com.huawei.anim.dynamicanimation.SpringModelBase.Solution
        public float getX(float dt) {
            return this.mX;
        }

        @Override // com.huawei.anim.dynamicanimation.SpringModelBase.Solution
        public float getDX(float dt) {
            return this.mDX;
        }

        @Override // com.huawei.anim.dynamicanimation.SpringModelBase.Solution
        public float getDDX(float dt) {
            return this.mDDX;
        }

        /* access modifiers changed from: protected */
        @Override // com.huawei.anim.dynamicanimation.SpringModelBase.Solution
        public float estimateDuration() {
            return 0.0f;
        }

        /* access modifiers changed from: protected */
        @Override // com.huawei.anim.dynamicanimation.SpringModelBase.Solution
        public void doEstimateDuration() {
        }

        /* access modifiers changed from: protected */
        @Override // com.huawei.anim.dynamicanimation.SpringModelBase.Solution
        public float getMaxAbsX() {
            return 0.0f;
        }

        /* access modifiers changed from: protected */
        @Override // com.huawei.anim.dynamicanimation.SpringModelBase.Solution
        public float getFirstExtremumX() {
            return 0.0f;
        }
    }

    /* access modifiers changed from: private */
    public class Solution1 extends Solution {
        float mC1;
        float mC2;
        float mR;

        public Solution1(float c1, float c2, float r) {
            super();
            this.mC1 = c1;
            this.mC2 = c2;
            this.mR = r;
            doEstimateDuration();
        }

        @Override // com.huawei.anim.dynamicanimation.SpringModelBase.Solution
        public float getX(float t) {
            this.mX = (float) (((double) (this.mC1 + (this.mC2 * t))) * Math.pow(2.718281828459045d, (double) (this.mR * t)));
            return this.mX;
        }

        @Override // com.huawei.anim.dynamicanimation.SpringModelBase.Solution
        public float getDX(float t) {
            float pow = (float) Math.pow(2.718281828459045d, (double) (this.mR * t));
            float f = this.mR;
            float f2 = this.mC1;
            float f3 = this.mC2;
            this.mDX = (f * (f2 + (f3 * t)) * pow) + (f3 * pow);
            return this.mDX;
        }

        @Override // com.huawei.anim.dynamicanimation.SpringModelBase.Solution
        public float getDDX(float t) {
            float pow = (float) Math.pow(2.718281828459045d, (double) (this.mR * t));
            float f = this.mR;
            float f2 = this.mC1;
            float f3 = this.mC2;
            this.mDDX = (f * f * (f2 + (f3 * t)) * pow) + (f3 * 2.0f * f * pow);
            return this.mDDX;
        }

        /* access modifiers changed from: protected */
        @Override // com.huawei.anim.dynamicanimation.SpringModelBase.Solution
        public final void doEstimateDuration() {
            float ti;
            float f = this.mC2;
            float tInflexion = (-(((f * 2.0f) / this.mR) + this.mC1)) / f;
            if (tInflexion < 0.0f || Float.isInfinite(tInflexion) || Float.isNaN(tInflexion)) {
                ti = 0.0f;
            } else {
                ti = tInflexion;
                float xi = getX(ti);
                int i = 0;
                while (SpringModelBase.this.almostLessThan(Math.abs(xi), SpringModelBase.this.mValueThreshold, 0.0f)) {
                    i++;
                    if (((float) i) > 999.0f) {
                        break;
                    }
                    ti = (0.0f + ti) / 2.0f;
                    xi = getX(ti);
                }
                if (((float) i) > 999.0f) {
                    this.mDuration = ti;
                    return;
                }
            }
            float xi2 = getX(ti);
            float dxi = getDX(ti);
            int i2 = 0;
            while (SpringModelBase.this.almostGreaterThan(Math.abs(xi2), SpringModelBase.this.mValueThreshold, 0.0f)) {
                i2++;
                if (((float) i2) > 999.0f) {
                    break;
                }
                ti -= xi2 / dxi;
                if (ti < 0.0f || Float.isNaN(ti) || Float.isInfinite(ti)) {
                    this.mDuration = 0.0f;
                    return;
                } else {
                    xi2 = getX(ti);
                    dxi = getDX(ti);
                }
            }
            if (((float) i2) > 999.0f) {
                this.mDuration = -1.0f;
            } else {
                this.mDuration = ti;
            }
        }

        /* access modifiers changed from: protected */
        @Override // com.huawei.anim.dynamicanimation.SpringModelBase.Solution
        public float estimateDuration() {
            return this.mDuration;
        }

        /* access modifiers changed from: protected */
        @Override // com.huawei.anim.dynamicanimation.SpringModelBase.Solution
        public float getMaxAbsX() {
            return Math.abs(getFirstExtremumX());
        }

        /* access modifiers changed from: protected */
        @Override // com.huawei.anim.dynamicanimation.SpringModelBase.Solution
        public float getFirstExtremumX() {
            float f = this.mC2;
            float tExtremum = (-((f / this.mR) + this.mC1)) / f;
            if (tExtremum < 0.0f || Float.isInfinite(tExtremum)) {
                tExtremum = 0.0f;
            }
            return getX(tExtremum);
        }
    }

    /* access modifiers changed from: private */
    public class Solution2 extends Solution {
        float mC1;
        float mC2;
        float mR1;
        float mR2;

        public Solution2(float c1, float c2, float r1, float r2) {
            super();
            this.mC1 = c1;
            this.mC2 = c2;
            this.mR1 = r1;
            this.mR2 = r2;
            doEstimateDuration();
        }

        @Override // com.huawei.anim.dynamicanimation.SpringModelBase.Solution
        public float getX(float t) {
            this.mX = (this.mC1 * ((float) Math.pow(2.718281828459045d, (double) (this.mR1 * t)))) + (this.mC2 * ((float) Math.pow(2.718281828459045d, (double) (this.mR2 * t))));
            return this.mX;
        }

        @Override // com.huawei.anim.dynamicanimation.SpringModelBase.Solution
        public float getDX(float t) {
            float f = this.mC1;
            float f2 = this.mR1;
            float pow = f * f2 * ((float) Math.pow(2.718281828459045d, (double) (f2 * t)));
            float f3 = this.mC2;
            float f4 = this.mR2;
            this.mDX = pow + (f3 * f4 * ((float) Math.pow(2.718281828459045d, (double) (f4 * t))));
            return this.mDX;
        }

        @Override // com.huawei.anim.dynamicanimation.SpringModelBase.Solution
        public float getDDX(float t) {
            float f = this.mC1;
            float f2 = this.mR1;
            float pow = f * f2 * f2 * ((float) Math.pow(2.718281828459045d, (double) (f2 * t)));
            float f3 = this.mC2;
            float f4 = this.mR2;
            this.mDDX = pow + (f3 * f4 * f4 * ((float) Math.pow(2.718281828459045d, (double) (f4 * t))));
            return this.mDDX;
        }

        /* access modifiers changed from: protected */
        @Override // com.huawei.anim.dynamicanimation.SpringModelBase.Solution
        public final void doEstimateDuration() {
            float ti;
            float f = this.mC1;
            float f2 = this.mR1;
            float f3 = this.mR2;
            float tInflexion = (((float) Math.log((double) Math.abs((f * f2) * f2))) - ((float) Math.log((double) Math.abs(((-this.mC2) * f3) * f3)))) / (this.mR2 - this.mR1);
            if (tInflexion < 0.0f || Float.isInfinite(tInflexion) || Float.isNaN(tInflexion)) {
                ti = 0.0f;
            } else {
                ti = tInflexion;
                float xi = getX(ti);
                int i = 0;
                while (SpringModelBase.this.almostLessThan(Math.abs(xi), SpringModelBase.this.mValueThreshold, 0.0f)) {
                    i++;
                    if (((float) i) > 999.0f) {
                        break;
                    }
                    ti = (0.0f + ti) / 2.0f;
                    xi = getX(ti);
                }
                if (((float) i) > 999.0f) {
                    this.mDuration = ti;
                    return;
                }
            }
            float xi2 = getX(ti);
            float dxi = getDX(ti);
            int i2 = 0;
            while (SpringModelBase.this.almostGreaterThan(Math.abs(xi2), SpringModelBase.this.mValueThreshold, 0.0f)) {
                i2++;
                if (((float) i2) > 999.0f) {
                    break;
                }
                ti -= xi2 / dxi;
                if (ti < 0.0f || Float.isNaN(ti) || Float.isInfinite(ti)) {
                    this.mDuration = 0.0f;
                    return;
                } else {
                    xi2 = getX(ti);
                    dxi = getDX(ti);
                }
            }
            if (((float) i2) > 999.0f) {
                this.mDuration = -1.0f;
            } else {
                this.mDuration = ti;
            }
        }

        /* access modifiers changed from: protected */
        @Override // com.huawei.anim.dynamicanimation.SpringModelBase.Solution
        public float estimateDuration() {
            return this.mDuration;
        }

        /* access modifiers changed from: protected */
        @Override // com.huawei.anim.dynamicanimation.SpringModelBase.Solution
        public float getMaxAbsX() {
            return Math.abs(getFirstExtremumX());
        }

        /* access modifiers changed from: protected */
        @Override // com.huawei.anim.dynamicanimation.SpringModelBase.Solution
        public float getFirstExtremumX() {
            float tExtremum = (((float) Math.log((double) Math.abs(this.mC1 * this.mR1))) - ((float) Math.log((double) Math.abs((-this.mC2) * this.mR2)))) / (this.mR2 - this.mR1);
            if (tExtremum < 0.0f || Float.isInfinite(tExtremum)) {
                tExtremum = 0.0f;
            }
            return getX(tExtremum);
        }
    }

    /* access modifiers changed from: private */
    public class Solution3 extends Solution {
        float mC1;
        float mC2;
        float mR;
        float mW;

        public Solution3(float c1, float c2, float w, float r) {
            super();
            this.mC1 = c1;
            this.mC2 = c2;
            this.mW = w;
            this.mR = r;
            doEstimateDuration();
        }

        @Override // com.huawei.anim.dynamicanimation.SpringModelBase.Solution
        public float getX(float t) {
            this.mX = ((float) Math.pow(2.718281828459045d, (double) (this.mR * t))) * ((this.mC1 * ((float) Math.cos((double) (this.mW * t)))) + (this.mC2 * ((float) Math.sin((double) (this.mW * t)))));
            return this.mX;
        }

        @Override // com.huawei.anim.dynamicanimation.SpringModelBase.Solution
        public float getDX(float t) {
            float power = (float) Math.pow(2.718281828459045d, (double) (this.mR * t));
            float cos0 = (float) Math.cos((double) (this.mW * t));
            float sin0 = (float) Math.sin((double) (this.mW * t));
            float f = this.mC2;
            float f2 = this.mW;
            float f3 = this.mC1;
            this.mDX = ((((f * f2) * cos0) - ((f2 * f3) * sin0)) * power) + (this.mR * power * ((f * sin0) + (f3 * cos0)));
            return this.mDX;
        }

        @Override // com.huawei.anim.dynamicanimation.SpringModelBase.Solution
        public float getDDX(float t) {
            float power = (float) Math.pow(2.718281828459045d, (double) (this.mR * t));
            float cos0 = (float) Math.cos((double) (this.mW * t));
            float sin0 = (float) Math.sin((double) (this.mW * t));
            float f = this.mR;
            float f2 = this.mC2;
            float f3 = this.mW;
            float f4 = this.mC1;
            this.mDDX = (f * power * (((f2 * f3) * cos0) - ((f4 * f3) * sin0))) + ((((((-f2) * f3) * f3) * sin0) - (((f4 * f3) * f3) * cos0)) * power) + (f * f * power * ((f2 * sin0) + (f4 * cos0))) + (f * power * (((f2 * f3) * cos0) - ((f4 * f3) * sin0)));
            return this.mDDX;
        }

        /* access modifiers changed from: protected */
        @Override // com.huawei.anim.dynamicanimation.SpringModelBase.Solution
        public final void doEstimateDuration() {
            int i;
            float result;
            int i2 = -1082130432;
            float dampingRatio = (float) Math.sqrt((double) ((SpringModelBase.this.mDamping * SpringModelBase.this.mDamping) / ((SpringModelBase.this.mMass * 4.0f) * SpringModelBase.this.mStiffness)));
            float naturalFreq = (float) Math.sqrt((double) (SpringModelBase.this.mStiffness / SpringModelBase.this.mMass));
            float dampedFreq = ((float) Math.sqrt((double) (1.0f - (dampingRatio * dampingRatio)))) * naturalFreq;
            float halfDampedPeriod = (6.2831855f / dampedFreq) / 2.0f;
            float phiX = (float) Math.atan((double) (this.mC2 / this.mC1));
            if (Float.isNaN(phiX)) {
                this.mDuration = 0.0f;
                return;
            }
            float tx0 = ((((float) Math.acos(0.0d)) + phiX) % 3.1415927f) / this.mW;
            float dx0 = getDX(tx0);
            float ti = (((((float) Math.acos(0.0d)) + ((float) Math.atan((double) (dampedFreq / (dampingRatio * naturalFreq))))) + phiX) % 3.1415927f) / dampedFreq;
            int i3 = 0;
            float t0 = 0.0f;
            while (true) {
                if (!SpringModelBase.this.almostGreaterThan(Math.abs(dx0), SpringModelBase.this.mVelocityThreshold, 0.0f)) {
                    i = i3;
                    break;
                }
                i = i3 + 1;
                if (((float) i3) >= 999.0f) {
                    break;
                }
                tx0 += halfDampedPeriod;
                dx0 = getDX(tx0);
                t0 += halfDampedPeriod;
                ti += halfDampedPeriod;
                i3 = i;
                i2 = i2;
                dampingRatio = dampingRatio;
            }
            if (((float) i) >= 999.0f) {
                this.mDuration = -1.0f;
                return;
            }
            if ((t0 <= ti && ti < tx0) || t0 == tx0) {
                result = doIterate(ti, ti + halfDampedPeriod);
            } else if (t0 >= tx0 || tx0 >= ti) {
                result = i2;
            } else {
                result = doIterate(Math.max(0.0f, ti - halfDampedPeriod), ti);
            }
            this.mDuration = result;
        }

        /* access modifiers changed from: protected */
        @Override // com.huawei.anim.dynamicanimation.SpringModelBase.Solution
        public float estimateDuration() {
            return this.mDuration;
        }

        /* access modifiers changed from: protected */
        @Override // com.huawei.anim.dynamicanimation.SpringModelBase.Solution
        public float getMaxAbsX() {
            float dampingRatio = (float) Math.sqrt((double) ((SpringModelBase.this.mDamping * SpringModelBase.this.mDamping) / ((SpringModelBase.this.mMass * 4.0f) * SpringModelBase.this.mStiffness)));
            float naturalFreq = (float) Math.sqrt((double) (SpringModelBase.this.mStiffness / SpringModelBase.this.mMass));
            float dampedFreq = (float) (((double) naturalFreq) * Math.sqrt((double) (1.0f - (dampingRatio * dampingRatio))));
            double d = 3.141592653589793d;
            float initPhi = (float) (((Math.acos(0.0d) + ((double) ((float) Math.atan((double) (dampedFreq / (dampingRatio * naturalFreq)))))) + ((double) ((float) Math.atan((double) (this.mC2 / this.mC1))))) % 3.141592653589793d);
            float resultMax = Math.abs(getX(initPhi / dampedFreq));
            int i = 0;
            while (true) {
                float tExtremum = (float) (((double) initPhi) + ((((double) i) * d) / ((double) dampedFreq)));
                float resultCandidate = Math.abs(getX(tExtremum));
                if (resultMax < resultCandidate) {
                    resultMax = resultCandidate;
                }
                if (tExtremum >= estimateDuration()) {
                    break;
                }
                i++;
                if (((float) i) >= 999.0f) {
                    break;
                }
                d = 3.141592653589793d;
            }
            if (((float) i) >= 999.0f) {
                return -1.0f;
            }
            return resultMax;
        }

        /* access modifiers changed from: protected */
        @Override // com.huawei.anim.dynamicanimation.SpringModelBase.Solution
        public float getFirstExtremumX() {
            float dampingRatio = (float) Math.sqrt((double) ((SpringModelBase.this.mDamping * SpringModelBase.this.mDamping) / ((SpringModelBase.this.mMass * 4.0f) * SpringModelBase.this.mStiffness)));
            float naturalFreq = (float) Math.sqrt((double) (SpringModelBase.this.mStiffness / SpringModelBase.this.mMass));
            float dampedFreq = (float) (((double) naturalFreq) * Math.sqrt((double) (1.0f - (dampingRatio * dampingRatio))));
            return getX((float) ((((Math.acos(0.0d) + ((double) ((float) Math.atan((double) (dampedFreq / (dampingRatio * naturalFreq)))))) + ((double) ((float) Math.atan((double) (this.mC2 / this.mC1))))) % 3.141592653589793d) / ((double) dampedFreq)));
        }
    }

    private boolean almostEqual(float a, float b, float epsilon) {
        return a > b - epsilon && a < b + epsilon;
    }

    private boolean almostZero(float a, float epsilon) {
        return almostEqual(a, 0.0f, epsilon);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean almostLessThan(float a, float b, float epsilon) {
        return a < b - epsilon;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean almostGreaterThan(float a, float b, float epsilon) {
        return a > b - epsilon;
    }

    public Solution solve(float initial, float velocity) {
        float c = this.mDamping;
        float m = this.mMass;
        float k = this.mStiffness;
        float cmk = (c * c) - ((m * 4.0f) * k);
        int compare = Float.compare(c * c, m * 4.0f * k);
        if (compare == 0) {
            float r = (-c) / (2.0f * m);
            return new Solution1(initial, velocity - (r * initial), r);
        } else if (compare > 0) {
            float r1 = (float) ((((double) (-c)) - Math.sqrt((double) cmk)) / ((double) (m * 2.0f)));
            float r2 = (float) ((((double) (-c)) + Math.sqrt((double) cmk)) / ((double) (2.0f * m)));
            float c2 = (velocity - (r1 * initial)) / (r2 - r1);
            return new Solution2(initial - c2, c2, r1, r2);
        } else {
            float w = (float) (Math.sqrt((double) (((4.0f * m) * k) - (c * c))) / ((double) (m * 2.0f)));
            float r3 = (-c) / (2.0f * m);
            return new Solution3(initial, (velocity - (r3 * initial)) / w, w, r3);
        }
    }

    @Override // com.huawei.anim.dynamicanimation.IPhysicalModel, com.huawei.anim.dynamicanimation.PhysicalModelBase
    public float getX(float dt) {
        if (dt < 0.0f) {
            dt = (float) (((double) (SystemClock.elapsedRealtime() - this.mStartTime)) / 1000.0d);
        }
        if (this.mSolution != null) {
            return this.mEndPosition + this.mSolution.getX(dt);
        }
        return 0.0f;
    }

    @Override // com.huawei.anim.dynamicanimation.IPhysicalModel, com.huawei.anim.dynamicanimation.PhysicalModelBase
    public float getX() {
        return getX(-1.0f);
    }

    @Override // com.huawei.anim.dynamicanimation.IPhysicalModel, com.huawei.anim.dynamicanimation.PhysicalModelBase
    public float getDX(float dt) {
        if (dt < 0.0f) {
            dt = (float) (((double) (SystemClock.elapsedRealtime() - this.mStartTime)) / 1000.0d);
        }
        Solution solution = this.mSolution;
        if (solution != null) {
            return solution.getDX(dt);
        }
        return 0.0f;
    }

    @Override // com.huawei.anim.dynamicanimation.IPhysicalModel, com.huawei.anim.dynamicanimation.PhysicalModelBase
    public float getDX() {
        return getDX(-1.0f);
    }

    @Override // com.huawei.anim.dynamicanimation.IPhysicalModel, com.huawei.anim.dynamicanimation.PhysicalModelBase
    public float getDDX(float dt) {
        if (dt < 0.0f) {
            dt = (float) (((double) (SystemClock.elapsedRealtime() - this.mStartTime)) / 1000.0d);
        }
        Solution solution = this.mSolution;
        if (solution != null) {
            return solution.getDDX(dt);
        }
        return 0.0f;
    }

    @Override // com.huawei.anim.dynamicanimation.IPhysicalModel, com.huawei.anim.dynamicanimation.PhysicalModelBase
    public float getDDX() {
        return getDDX(-1.0f);
    }

    public SpringModelBase setEndPosition(float x, float velocity, long t) {
        float x2 = Math.min(99999.0f, Math.max(-99999.0f, x));
        float velocity2 = Math.min(99999.0f, Math.max(-99999.0f, velocity));
        if (t <= 0) {
            t = SystemClock.elapsedRealtime();
        }
        if (x2 == this.mEndPosition && almostZero(velocity2, this.mValueThreshold)) {
            return this;
        }
        float position = this.mEndPosition;
        if (this.mSolution != null) {
            if (almostZero(velocity2, this.mValueThreshold)) {
                velocity2 = this.mSolution.getDX(((float) (t - this.mStartTime)) / 1000.0f);
            }
            float position2 = this.mSolution.getX(((float) (t - this.mStartTime)) / 1000.0f);
            if (almostZero(velocity2, this.mValueThreshold)) {
                velocity2 = 0.0f;
            }
            if (almostZero(position2, this.mValueThreshold)) {
                position2 = 0.0f;
            }
            position = position2 + this.mEndPosition;
            if (almostZero(position - x2, this.mValueThreshold) && almostZero(velocity2, this.mValueThreshold)) {
                return this;
            }
        }
        this.mEndPosition = x2;
        this.mStartPosition = position;
        this.mStartVelocity = velocity2;
        this.mSolution = solve(position - this.mEndPosition, velocity2);
        this.mStartTime = t;
        return this;
    }

    @Override // com.huawei.anim.dynamicanimation.IPhysicalModel, com.huawei.anim.dynamicanimation.PhysicalModelBase
    public boolean isAtEquilibrium(float dt) {
        if (dt < 0.0f) {
            dt = ((float) SystemClock.elapsedRealtime()) - (getStartTime() / 1000.0f);
        }
        return almostEqual(getX(dt), this.mEndPosition, this.mValueThreshold) && almostZero(getDX(dt), this.mValueThreshold);
    }

    @Override // com.huawei.anim.dynamicanimation.IPhysicalModel, com.huawei.anim.dynamicanimation.PhysicalModelBase
    public boolean isAtEquilibrium() {
        return isAtEquilibrium(-1.0f);
    }

    @Override // com.huawei.anim.dynamicanimation.IPhysicalModel, com.huawei.anim.dynamicanimation.PhysicalModelBase
    public boolean isAtEquilibrium(float value, float velocity) {
        return ((double) Math.abs(velocity)) < ((double) this.mVelocityThreshold) && ((double) Math.abs(value - this.mEndPosition)) < ((double) this.mValueThreshold);
    }

    public SpringModelBase snap(float x) {
        float x2 = Math.min(0.0f, Math.max(0.0f, x));
        this.mStartTime = SystemClock.elapsedRealtime();
        this.mStartPosition = 0.0f;
        this.mEndPosition = x2;
        this.mStartVelocity = 0.0f;
        this.mSolution = new Solution0();
        return this;
    }

    public SpringModelBase reconfigure(float mass, float stiffness, float damping, float valueThreshold) {
        super.setValueThreshold(valueThreshold);
        this.mMass = Math.min(Math.max(1.0f, mass), 1.0f);
        this.mStiffness = Math.min(Math.max(1.0f, stiffness), 999.0f);
        this.mDamping = Math.min(Math.max(1.0f, damping), 99.0f);
        this.mStartPosition = getX(-1.0f);
        this.mStartVelocity = getDX(-1.0f);
        this.mSolution = solve(this.mStartPosition - this.mEndPosition, this.mStartVelocity);
        this.mStartTime = SystemClock.elapsedRealtime();
        return this;
    }

    public float getStiffness() {
        return this.mStiffness;
    }

    public float getDamping() {
        return this.mDamping;
    }

    public void setMass(float m) {
        reconfigure(m, this.mStiffness, this.mDamping, this.mValueThreshold);
    }

    public SpringModelBase setStiffness(float c) {
        return reconfigure(this.mMass, c, this.mDamping, this.mValueThreshold);
    }

    public SpringModelBase setDamping(float d) {
        return reconfigure(this.mMass, this.mStiffness, d, this.mValueThreshold);
    }

    @Override // com.huawei.anim.dynamicanimation.IPhysicalModel, com.huawei.anim.dynamicanimation.PhysicalModelBase, com.huawei.anim.dynamicanimation.PhysicalModelBase
    public SpringModelBase setValueThreshold(float v) {
        return reconfigure(this.mMass, this.mStiffness, this.mDamping, v);
    }

    @Override // com.huawei.anim.dynamicanimation.IPhysicalModel, com.huawei.anim.dynamicanimation.PhysicalModelBase
    public float getEstimatedDuration() {
        float estimateDuration = this.mSolution.estimateDuration();
        if (Float.compare(estimateDuration, -1.0f) == 0) {
            return DEFAULT_ESTIMATE_DURATION;
        }
        return 1000.0f * estimateDuration;
    }

    @Override // com.huawei.anim.dynamicanimation.IPhysicalModel, com.huawei.anim.dynamicanimation.PhysicalModelBase
    public float getMaxAbsX() {
        Solution solution = this.mSolution;
        if (solution != null) {
            return solution.getMaxAbsX();
        }
        return 0.0f;
    }

    public float getFirstExtremumX() {
        Solution solution = this.mSolution;
        if (solution != null) {
            return solution.getFirstExtremumX();
        }
        return 0.0f;
    }
}
