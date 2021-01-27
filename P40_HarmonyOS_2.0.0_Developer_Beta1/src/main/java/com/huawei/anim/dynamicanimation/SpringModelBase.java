package com.huawei.anim.dynamicanimation;

import android.os.SystemClock;
import java.math.BigDecimal;

public class SpringModelBase extends PhysicalModelBase {
    public static final float DEFAULT_DAMPING = 15.0f;
    public static final float DEFAULT_MASS = 1.0f;
    public static final float DEFAULT_STIFFNESS = 800.0f;
    public static final float DEFAULT_VALUE_THRESHOLD = new BigDecimal("1").divide(new BigDecimal("1000")).floatValue();
    public static final float MAXIMUM_DAMPING = 99.0f;
    public static final float MAXIMUM_MASS = 1.0f;
    public static final float MAXIMUM_STIFFNESS = 999.0f;
    public static final float MINIMUM_DAMPING = 1.0f;
    public static final float MINIMUM_MASS = 1.0f;
    public static final float MINIMUM_STIFFNESS = 1.0f;
    private static final float a = 999.0f;
    private static final int b = 16;
    private static final float c = 500.0f;
    private static final float d = -1.0f;
    private static final double e = 1000.0d;
    private static final float f = 1000.0f;
    private static final int g = 4;
    private static final int h = 2;
    private static final int i = -1;
    private float j = 1.0f;
    private float k = 800.0f;
    private float l = 15.0f;
    private a m;

    public SpringModelBase(float f2, float f3, float f4) {
        super.setValueThreshold(f4);
        this.j = 1.0f;
        this.k = Math.min(Math.max(1.0f, f2), 999.0f);
        this.l = Math.min(Math.max(1.0f, f3), 99.0f);
        this.m = null;
        this.mStartPosition = 0.0f;
        this.mEndPosition = 0.0f;
        this.mStartVelocity = 0.0f;
        this.mStartTime = 0;
    }

    /* access modifiers changed from: private */
    public abstract class a {
        protected float a = 0.0f;
        protected float b = 0.0f;
        protected float c = 0.0f;
        protected float d = 0.0f;
        private float[] f = new float[17];

        /* access modifiers changed from: protected */
        public abstract float a();

        /* access modifiers changed from: protected */
        public abstract float a(float f2);

        /* access modifiers changed from: protected */
        public abstract float b(float f2);

        /* access modifiers changed from: protected */
        public abstract void b();

        /* access modifiers changed from: protected */
        public abstract float c();

        /* access modifiers changed from: protected */
        public abstract float c(float f2);

        /* access modifiers changed from: protected */
        public abstract float d();

        protected a() {
        }

        /* access modifiers changed from: protected */
        public void a(float f2, float f3) {
            int i = 0;
            if (f3 >= 0.0f && !Float.isInfinite(f3) && !Float.isNaN(f3)) {
                float a2 = a(f3);
                float f4 = f3;
                int i2 = 0;
                while (SpringModelBase.this.b(Math.abs(a2), SpringModelBase.this.mValueThreshold, 0.0f)) {
                    i2++;
                    if (((float) i2) > 999.0f) {
                        break;
                    }
                    f4 = (f4 + f2) / 2.0f;
                    a2 = a(f4);
                }
                if (((float) i2) > 999.0f) {
                    this.d = f4;
                    return;
                }
                f2 = f4;
            }
            float a3 = a(f2);
            float b2 = b(f2);
            while (SpringModelBase.this.c(Math.abs(a3), SpringModelBase.this.mValueThreshold, 0.0f)) {
                i++;
                if (((float) i) > 999.0f) {
                    break;
                }
                f2 -= a3 / b2;
                if (f2 < 0.0f || Float.isNaN(f2) || Float.isInfinite(f2)) {
                    this.d = 0.0f;
                    return;
                } else {
                    a3 = a(f2);
                    b2 = b(f2);
                }
            }
            if (((float) i) > 999.0f) {
                this.d = -1.0f;
            } else {
                this.d = f2;
            }
        }

        private float a(float f2, float f3, float f4) {
            float f5 = (f4 - f3) / 16.0f;
            boolean z = b(new BigDecimal((double) (f4 + f3)).divide(new BigDecimal("2")).floatValue()) > 0.0f;
            for (int i = 1; i < 17; i++) {
                float[] fArr = this.f;
                int i2 = i - 1;
                float f6 = fArr[i] - fArr[i2];
                if (!z || fArr[i] < f2) {
                    if (!z) {
                        float[] fArr2 = this.f;
                        if (fArr2[i] <= f2) {
                            if (f6 == 0.0f) {
                                return f3 + (((float) i2) * f5);
                            }
                            return f3 + ((((float) i) - ((fArr2[i] - f2) / f6)) * f5);
                        }
                    }
                } else if (f6 == 0.0f) {
                    return f3 + (((float) i2) * f5);
                } else {
                    return f3 + ((((float) i2) + ((f2 - fArr[i2]) / f6)) * f5);
                }
            }
            return f4;
        }

        /* access modifiers changed from: protected */
        public float b(float f2, float f3) {
            float f4 = (f3 - f2) / 16.0f;
            float f5 = SpringModelBase.this.mValueThreshold;
            for (int i = 0; i < 17; i++) {
                this.f[i] = a((((float) i) * f4) + f2);
            }
            boolean z = true;
            int i2 = 1;
            while (true) {
                if (i2 >= 17) {
                    z = false;
                    break;
                }
                int i3 = i2 - 1;
                if ((this.f[i3] - SpringModelBase.this.mValueThreshold) * (this.f[i2] - SpringModelBase.this.mValueThreshold) < 0.0f) {
                    f5 = SpringModelBase.this.mValueThreshold;
                    break;
                } else if ((this.f[i3] + SpringModelBase.this.mValueThreshold) * (this.f[i2] + SpringModelBase.this.mValueThreshold) < 0.0f) {
                    f5 = -SpringModelBase.this.mValueThreshold;
                    break;
                } else {
                    i2++;
                }
            }
            if (!z) {
                return f2;
            }
            float a2 = a(f5, f2, f3);
            while (true) {
                f3 = a2;
                if (Math.abs(a(f3)) >= SpringModelBase.this.mValueThreshold || f3 - f3 < PhysicalModelBase.VELOCITY_THRESHOLD_MULTIPLIER / SpringModelBase.f) {
                    break;
                }
                float f6 = (f3 - f2) / 16.0f;
                for (int i4 = 0; i4 < 17; i4++) {
                    this.f[i4] = a((((float) i4) * f6) + f2);
                }
                a2 = a(f5, f2, f3);
            }
            float a3 = a(f3);
            float b2 = b(f3);
            float f7 = f3;
            float f8 = 0.0f;
            while (true) {
                if (!SpringModelBase.this.c(Math.abs(a3), SpringModelBase.this.mValueThreshold, 0.0f)) {
                    break;
                }
                float f9 = 1.0f + f8;
                if (f8 >= 999.0f) {
                    f8 = f9;
                    break;
                }
                f7 -= a3 / b2;
                a3 = a(f7);
                b2 = b(f7);
                f8 = f9;
            }
            return c(f8, f7);
        }

        private float c(float f2, float f3) {
            if (f2 <= 999.0f) {
                return f3;
            }
            return -1.0f;
        }
    }

    /* access modifiers changed from: private */
    public class b extends a {
        private b() {
            super();
        }

        @Override // com.huawei.anim.dynamicanimation.SpringModelBase.a
        public float a(float f2) {
            return this.a;
        }

        @Override // com.huawei.anim.dynamicanimation.SpringModelBase.a
        public float b(float f2) {
            return this.b;
        }

        @Override // com.huawei.anim.dynamicanimation.SpringModelBase.a
        public float c(float f2) {
            return this.c;
        }

        /* access modifiers changed from: protected */
        @Override // com.huawei.anim.dynamicanimation.SpringModelBase.a
        public float a() {
            return 0.0f;
        }

        /* access modifiers changed from: protected */
        @Override // com.huawei.anim.dynamicanimation.SpringModelBase.a
        public void b() {
        }

        /* access modifiers changed from: protected */
        @Override // com.huawei.anim.dynamicanimation.SpringModelBase.a
        public float c() {
            return 0.0f;
        }

        /* access modifiers changed from: protected */
        @Override // com.huawei.anim.dynamicanimation.SpringModelBase.a
        public float d() {
            return 0.0f;
        }
    }

    /* access modifiers changed from: private */
    public class c extends a {
        float f;
        float g;
        float h;

        c(float f2, float f3, float f4) {
            super();
            this.f = f2;
            this.g = f3;
            this.h = f4;
            b();
        }

        @Override // com.huawei.anim.dynamicanimation.SpringModelBase.a
        public float a(float f2) {
            this.a = (float) (((double) (this.f + (this.g * f2))) * Math.pow(2.718281828459045d, (double) (this.h * f2)));
            return this.a;
        }

        @Override // com.huawei.anim.dynamicanimation.SpringModelBase.a
        public float b(float f2) {
            float pow = (float) Math.pow(2.718281828459045d, (double) (this.h * f2));
            float f3 = this.h;
            float f4 = this.f;
            float f5 = this.g;
            this.b = (f3 * (f4 + (f2 * f5)) * pow) + (f5 * pow);
            return this.b;
        }

        @Override // com.huawei.anim.dynamicanimation.SpringModelBase.a
        public float c(float f2) {
            float pow = (float) Math.pow(2.718281828459045d, (double) (this.h * f2));
            float f3 = this.h;
            float f4 = this.f;
            float f5 = this.g;
            this.c = (f3 * f3 * (f4 + (f2 * f5)) * pow) + (f5 * 2.0f * f3 * pow);
            return this.c;
        }

        /* access modifiers changed from: protected */
        @Override // com.huawei.anim.dynamicanimation.SpringModelBase.a
        public final void b() {
            float f2 = this.g;
            a(0.0f, (-(((2.0f * f2) / this.h) + this.f)) / f2);
        }

        /* access modifiers changed from: protected */
        @Override // com.huawei.anim.dynamicanimation.SpringModelBase.a
        public float a() {
            return this.d;
        }

        /* access modifiers changed from: protected */
        @Override // com.huawei.anim.dynamicanimation.SpringModelBase.a
        public float c() {
            return Math.abs(d());
        }

        /* access modifiers changed from: protected */
        @Override // com.huawei.anim.dynamicanimation.SpringModelBase.a
        public float d() {
            float f2 = this.g;
            float f3 = (-((f2 / this.h) + this.f)) / f2;
            float f4 = 0.0f;
            if (f3 >= 0.0f && !Float.isInfinite(f3)) {
                f4 = f3;
            }
            return a(f4);
        }
    }

    /* access modifiers changed from: private */
    public class d extends a {
        float f;
        float g;
        float h;
        float i;

        d(float f2, float f3, float f4, float f5) {
            super();
            this.f = f2;
            this.g = f3;
            this.h = f4;
            this.i = f5;
            b();
        }

        @Override // com.huawei.anim.dynamicanimation.SpringModelBase.a
        public float a(float f2) {
            this.a = (this.f * ((float) Math.pow(2.718281828459045d, (double) (this.h * f2)))) + (this.g * ((float) Math.pow(2.718281828459045d, (double) (this.i * f2))));
            return this.a;
        }

        @Override // com.huawei.anim.dynamicanimation.SpringModelBase.a
        public float b(float f2) {
            float f3 = this.f;
            float f4 = this.h;
            float pow = f3 * f4 * ((float) Math.pow(2.718281828459045d, (double) (f4 * f2)));
            float f5 = this.g;
            float f6 = this.i;
            this.b = pow + (f5 * f6 * ((float) Math.pow(2.718281828459045d, (double) (f6 * f2))));
            return this.b;
        }

        @Override // com.huawei.anim.dynamicanimation.SpringModelBase.a
        public float c(float f2) {
            float f3 = this.f;
            float f4 = this.h;
            float pow = f3 * f4 * f4 * ((float) Math.pow(2.718281828459045d, (double) (f4 * f2)));
            float f5 = this.g;
            float f6 = this.i;
            this.c = pow + (f5 * f6 * f6 * ((float) Math.pow(2.718281828459045d, (double) (f6 * f2))));
            return this.c;
        }

        /* access modifiers changed from: protected */
        @Override // com.huawei.anim.dynamicanimation.SpringModelBase.a
        public final void b() {
            float f2 = this.f;
            float f3 = this.h;
            float f4 = this.i;
            a(0.0f, (((float) Math.log((double) Math.abs((f2 * f3) * f3))) - ((float) Math.log((double) Math.abs(((-this.g) * f4) * f4)))) / (this.i - this.h));
        }

        /* access modifiers changed from: protected */
        @Override // com.huawei.anim.dynamicanimation.SpringModelBase.a
        public float a() {
            return this.d;
        }

        /* access modifiers changed from: protected */
        @Override // com.huawei.anim.dynamicanimation.SpringModelBase.a
        public float c() {
            return Math.abs(d());
        }

        /* access modifiers changed from: protected */
        @Override // com.huawei.anim.dynamicanimation.SpringModelBase.a
        public float d() {
            float log = (((float) Math.log((double) Math.abs(this.f * this.h))) - ((float) Math.log((double) Math.abs((-this.g) * this.i)))) / (this.i - this.h);
            if (log < 0.0f || Float.isInfinite(log)) {
                log = 0.0f;
            }
            return a(log);
        }
    }

    /* access modifiers changed from: private */
    public class e extends a {
        float f;
        float g;
        float h;
        float i;

        e(float f2, float f3, float f4, float f5) {
            super();
            this.f = f2;
            this.g = f3;
            this.h = f4;
            this.i = f5;
            b();
        }

        @Override // com.huawei.anim.dynamicanimation.SpringModelBase.a
        public float a(float f2) {
            this.a = ((float) Math.pow(2.718281828459045d, (double) (this.i * f2))) * ((this.f * ((float) Math.cos((double) (this.h * f2)))) + (this.g * ((float) Math.sin((double) (this.h * f2)))));
            return this.a;
        }

        @Override // com.huawei.anim.dynamicanimation.SpringModelBase.a
        public float b(float f2) {
            float pow = (float) Math.pow(2.718281828459045d, (double) (this.i * f2));
            float cos = (float) Math.cos((double) (this.h * f2));
            float sin = (float) Math.sin((double) (this.h * f2));
            float f3 = this.g;
            float f4 = this.h;
            float f5 = this.f;
            this.b = ((((f3 * f4) * cos) - ((f4 * f5) * sin)) * pow) + (this.i * pow * ((f3 * sin) + (f5 * cos)));
            return this.b;
        }

        @Override // com.huawei.anim.dynamicanimation.SpringModelBase.a
        public float c(float f2) {
            float pow = (float) Math.pow(2.718281828459045d, (double) (this.i * f2));
            float cos = (float) Math.cos((double) (this.h * f2));
            float sin = (float) Math.sin((double) (this.h * f2));
            float f3 = this.i;
            float f4 = this.g;
            float f5 = this.h;
            float f6 = this.f;
            this.c = (f3 * pow * (((f4 * f5) * cos) - ((f6 * f5) * sin))) + ((((((-f4) * f5) * f5) * sin) - (((f6 * f5) * f5) * cos)) * pow) + (f3 * f3 * pow * ((f4 * sin) + (f6 * cos))) + (f3 * pow * (((f4 * f5) * cos) - ((f6 * f5) * sin)));
            return this.c;
        }

        /* access modifiers changed from: protected */
        @Override // com.huawei.anim.dynamicanimation.SpringModelBase.a
        public final void b() {
            float sqrt = (float) Math.sqrt((double) ((SpringModelBase.this.l * SpringModelBase.this.l) / ((SpringModelBase.this.j * 4.0f) * SpringModelBase.this.k)));
            float sqrt2 = (float) Math.sqrt((double) (SpringModelBase.this.k / SpringModelBase.this.j));
            float sqrt3 = ((float) Math.sqrt((double) (1.0f - (sqrt * sqrt)))) * sqrt2;
            float f2 = (6.2831855f / sqrt3) / 2.0f;
            float atan = (float) Math.atan((double) (this.g / this.f));
            if (Float.isNaN(atan)) {
                this.d = 0.0f;
                return;
            }
            float acos = ((((float) Math.acos(0.0d)) + atan) % 3.1415927f) / this.h;
            float b = b(acos);
            float acos2 = (((((float) Math.acos(0.0d)) + ((float) Math.atan((double) (sqrt3 / (sqrt * sqrt2))))) + atan) % 3.1415927f) / sqrt3;
            int i2 = 0;
            float f3 = 0.0f;
            while (true) {
                if (!SpringModelBase.this.c(Math.abs(b), SpringModelBase.this.mVelocityThreshold, 0.0f)) {
                    break;
                }
                int i3 = i2 + 1;
                if (((float) i2) >= 999.0f) {
                    i2 = i3;
                    break;
                }
                acos += f2;
                b = b(acos);
                f3 += f2;
                acos2 += f2;
                i2 = i3;
            }
            float f4 = -1.0f;
            if (((float) i2) >= 999.0f) {
                this.d = -1.0f;
                return;
            }
            if ((f3 <= acos2 && acos2 < acos) || f3 == acos) {
                f4 = b(acos2, f2 + acos2);
            } else if (f3 < acos && acos < acos2) {
                f4 = b(Math.max(0.0f, acos2 - f2), acos2);
            }
            this.d = f4;
        }

        /* access modifiers changed from: protected */
        @Override // com.huawei.anim.dynamicanimation.SpringModelBase.a
        public float a() {
            return this.d;
        }

        /* access modifiers changed from: protected */
        @Override // com.huawei.anim.dynamicanimation.SpringModelBase.a
        public float c() {
            float sqrt = (float) Math.sqrt((double) ((SpringModelBase.this.l * SpringModelBase.this.l) / ((SpringModelBase.this.j * 4.0f) * SpringModelBase.this.k)));
            float sqrt2 = (float) Math.sqrt((double) (SpringModelBase.this.k / SpringModelBase.this.j));
            float sqrt3 = (float) (((double) sqrt2) * Math.sqrt((double) (1.0f - (sqrt * sqrt))));
            float acos = (float) (((Math.acos(0.0d) + ((double) ((float) Math.atan((double) (sqrt3 / (sqrt * sqrt2)))))) + ((double) ((float) Math.atan((double) (this.g / this.f))))) % 3.141592653589793d);
            float abs = Math.abs(a(acos / sqrt3));
            int i2 = 0;
            do {
                float f2 = (float) (((double) acos) + ((((double) i2) * 3.141592653589793d) / ((double) sqrt3)));
                float abs2 = Math.abs(a(f2));
                if (abs < abs2) {
                    abs = abs2;
                }
                if (f2 >= a()) {
                    break;
                }
                i2++;
            } while (((float) i2) < 999.0f);
            if (((float) i2) >= 999.0f) {
                return -1.0f;
            }
            return abs;
        }

        /* access modifiers changed from: protected */
        @Override // com.huawei.anim.dynamicanimation.SpringModelBase.a
        public float d() {
            float sqrt = (float) Math.sqrt((double) ((SpringModelBase.this.l * SpringModelBase.this.l) / ((SpringModelBase.this.j * 4.0f) * SpringModelBase.this.k)));
            float sqrt2 = (float) Math.sqrt((double) (SpringModelBase.this.k / SpringModelBase.this.j));
            float sqrt3 = (float) (((double) sqrt2) * Math.sqrt((double) (1.0f - (sqrt * sqrt))));
            return a((float) ((((Math.acos(0.0d) + ((double) ((float) Math.atan((double) (sqrt3 / (sqrt * sqrt2)))))) + ((double) ((float) Math.atan((double) (this.g / this.f))))) % 3.141592653589793d) / ((double) sqrt3)));
        }
    }

    private boolean a(float f2, float f3, float f4) {
        return f2 > f3 - f4 && f2 < f3 + f4;
    }

    private boolean a(float f2, float f3) {
        return a(f2, 0.0f, f3);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean b(float f2, float f3, float f4) {
        return f2 < f3 - f4;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean c(float f2, float f3, float f4) {
        return f2 > f3 - f4;
    }

    public a solve(float f2, float f3) {
        float f4 = this.l;
        float f5 = this.j;
        float f6 = f4 * f4;
        float f7 = 4.0f * f5 * this.k;
        float f8 = f6 - f7;
        int compare = Float.compare(f6, f7);
        if (compare == 0) {
            float f9 = (-f4) / (f5 * 2.0f);
            return new c(f2, f3 - (f9 * f2), f9);
        } else if (compare > 0) {
            double d2 = (double) (-f4);
            double d3 = (double) f8;
            double d4 = (double) (f5 * 2.0f);
            float sqrt = (float) ((d2 - Math.sqrt(d3)) / d4);
            float sqrt2 = (float) ((d2 + Math.sqrt(d3)) / d4);
            float f10 = (f3 - (sqrt * f2)) / (sqrt2 - sqrt);
            return new d(f2 - f10, f10, sqrt, sqrt2);
        } else {
            float f11 = f5 * 2.0f;
            float sqrt3 = (float) (Math.sqrt((double) (f7 - f6)) / ((double) f11));
            float f12 = (-f4) / f11;
            return new e(f2, (f3 - (f12 * f2)) / sqrt3, sqrt3, f12);
        }
    }

    @Override // com.huawei.anim.dynamicanimation.PhysicalModelBase, com.huawei.anim.dynamicanimation.PhysicalModel
    public float getPosition(float f2) {
        if (f2 < 0.0f) {
            f2 = (float) (((double) (SystemClock.elapsedRealtime() - this.mStartTime)) / e);
        }
        if (this.m != null) {
            return this.mEndPosition + this.m.a(f2);
        }
        return 0.0f;
    }

    @Override // com.huawei.anim.dynamicanimation.PhysicalModelBase, com.huawei.anim.dynamicanimation.PhysicalModel
    public float getPosition() {
        return getPosition(-1.0f);
    }

    @Override // com.huawei.anim.dynamicanimation.PhysicalModelBase, com.huawei.anim.dynamicanimation.PhysicalModel
    public float getVelocity(float f2) {
        if (f2 < 0.0f) {
            f2 = (float) (((double) (SystemClock.elapsedRealtime() - this.mStartTime)) / e);
        }
        a aVar = this.m;
        if (aVar != null) {
            return aVar.b(f2);
        }
        return 0.0f;
    }

    @Override // com.huawei.anim.dynamicanimation.PhysicalModelBase, com.huawei.anim.dynamicanimation.PhysicalModel
    public float getVelocity() {
        return getVelocity(-1.0f);
    }

    @Override // com.huawei.anim.dynamicanimation.PhysicalModelBase, com.huawei.anim.dynamicanimation.PhysicalModel
    public float getAcceleration(float f2) {
        if (f2 < 0.0f) {
            f2 = (float) (((double) (SystemClock.elapsedRealtime() - this.mStartTime)) / e);
        }
        a aVar = this.m;
        if (aVar != null) {
            return aVar.c(f2);
        }
        return 0.0f;
    }

    @Override // com.huawei.anim.dynamicanimation.PhysicalModelBase, com.huawei.anim.dynamicanimation.PhysicalModel
    public float getAcceleration() {
        return getAcceleration(-1.0f);
    }

    public SpringModelBase setEndPosition(float f2, float f3, long j2) {
        float min = Math.min(99999.0f, Math.max(-99999.0f, f2));
        float min2 = Math.min(99999.0f, Math.max(-99999.0f, f3));
        if (j2 <= 0) {
            j2 = SystemClock.elapsedRealtime();
        }
        if (min == this.mEndPosition && a(min2, this.mValueThreshold)) {
            return this;
        }
        float f4 = this.mEndPosition;
        if (this.m != null) {
            if (a(min2, this.mValueThreshold)) {
                min2 = this.m.b(((float) (j2 - this.mStartTime)) / f);
            }
            float a2 = this.m.a(((float) (j2 - this.mStartTime)) / f);
            if (a(min2, this.mValueThreshold)) {
                min2 = 0.0f;
            }
            if (a(a2, this.mValueThreshold)) {
                a2 = 0.0f;
            }
            f4 = a2 + this.mEndPosition;
            if (a(f4 - min, this.mValueThreshold) && a(min2, this.mValueThreshold)) {
                return this;
            }
        }
        this.mEndPosition = min;
        this.mStartPosition = f4;
        this.mStartVelocity = min2;
        this.m = solve(f4 - this.mEndPosition, min2);
        this.mStartTime = j2;
        return this;
    }

    public SpringModelBase setEndValue(float f2, float f3) {
        if (f2 == this.mEndPosition && a(f3, this.mValueThreshold)) {
            return this;
        }
        this.mStartTime = SystemClock.elapsedRealtime();
        this.mStartPosition = 0.0f;
        this.mEndPosition = f2;
        this.mStartVelocity = f3;
        this.m = solve(this.mStartPosition - this.mEndPosition, f3);
        return this;
    }

    @Override // com.huawei.anim.dynamicanimation.PhysicalModelBase, com.huawei.anim.dynamicanimation.PhysicalModel
    public boolean isAtEquilibrium(float f2) {
        if (f2 < 0.0f) {
            f2 = ((float) SystemClock.elapsedRealtime()) - (getStartTime() / f);
        }
        return a(getPosition(f2), this.mEndPosition, this.mValueThreshold) && a(getVelocity(f2), this.mValueThreshold);
    }

    @Override // com.huawei.anim.dynamicanimation.PhysicalModelBase, com.huawei.anim.dynamicanimation.PhysicalModel
    public boolean isAtEquilibrium() {
        return isAtEquilibrium(-1.0f);
    }

    @Override // com.huawei.anim.dynamicanimation.PhysicalModelBase, com.huawei.anim.dynamicanimation.PhysicalModel
    public boolean isAtEquilibrium(float f2, float f3) {
        return Math.abs(f3) < this.mVelocityThreshold && Math.abs(f2 - this.mEndPosition) < this.mValueThreshold;
    }

    public SpringModelBase snap(float f2) {
        float min = Math.min(0.0f, Math.max(0.0f, f2));
        this.mStartTime = SystemClock.elapsedRealtime();
        this.mStartPosition = 0.0f;
        this.mEndPosition = min;
        this.mStartVelocity = 0.0f;
        this.m = new b();
        return this;
    }

    public SpringModelBase reconfigure(float f2, float f3, float f4, float f5) {
        super.setValueThreshold(f5);
        this.j = Math.min(Math.max(1.0f, f2), 1.0f);
        this.k = Math.min(Math.max(1.0f, f3), 999.0f);
        this.l = Math.min(Math.max(1.0f, f4), 99.0f);
        this.mStartPosition = getPosition(-1.0f);
        this.mStartVelocity = getVelocity(-1.0f);
        this.m = solve(this.mStartPosition - this.mEndPosition, this.mStartVelocity);
        this.mStartTime = SystemClock.elapsedRealtime();
        return this;
    }

    public float getStiffness() {
        return this.k;
    }

    public float getDamping() {
        return this.l;
    }

    public void setMass(float f2) {
        reconfigure(f2, this.k, this.l, this.mValueThreshold);
    }

    public SpringModelBase setStiffness(float f2) {
        return reconfigure(this.j, f2, this.l, this.mValueThreshold);
    }

    public SpringModelBase setDamping(float f2) {
        return reconfigure(this.j, this.k, f2, this.mValueThreshold);
    }

    @Override // com.huawei.anim.dynamicanimation.PhysicalModelBase, com.huawei.anim.dynamicanimation.PhysicalModel
    public SpringModelBase setValueThreshold(float f2) {
        return reconfigure(this.j, this.k, this.l, f2);
    }

    @Override // com.huawei.anim.dynamicanimation.PhysicalModelBase, com.huawei.anim.dynamicanimation.PhysicalModel
    public float getEstimatedDuration() {
        float a2 = this.m.a();
        if (Float.compare(a2, -1.0f) == 0) {
            return c;
        }
        return a2 * f;
    }

    @Override // com.huawei.anim.dynamicanimation.PhysicalModelBase, com.huawei.anim.dynamicanimation.PhysicalModel
    public float getMaxAbsX() {
        a aVar = this.m;
        if (aVar != null) {
            return aVar.c();
        }
        return 0.0f;
    }

    public float getFirstExtremumX() {
        a aVar = this.m;
        if (aVar != null) {
            return aVar.d();
        }
        return 0.0f;
    }
}
