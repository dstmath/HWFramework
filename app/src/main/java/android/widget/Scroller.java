package android.widget;

import android.content.Context;
import android.util.BoostFramework;
import android.view.ViewConfiguration;
import android.view.animation.AnimationUtils;
import android.view.animation.Interpolator;
import com.android.internal.R;

public class Scroller {
    private static float DECELERATION_RATE = 0.0f;
    private static final int DEFAULT_DURATION = 250;
    private static final float END_TENSION = 1.0f;
    private static final int FLING_MODE = 1;
    private static final float INFLEXION = 0.35f;
    private static final int NB_SAMPLES = 100;
    private static final float P1 = 0.175f;
    private static final float P2 = 0.35000002f;
    private static final int SCROLL_MODE = 0;
    private static final float[] SPLINE_POSITION = null;
    private static final float[] SPLINE_TIME = null;
    private static final float START_TENSION = 0.5f;
    boolean bIsPerfBoostEnabled;
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
    private BoostFramework mPerf;
    private float mPhysicalCoeff;
    private final float mPpi;
    private long mStartTime;
    private int mStartX;
    private int mStartY;
    private float mVelocity;
    private int[] sBoostParamVal;
    private int sBoostTimeOut;
    private int scrollBoostTimeOut;

    static class ViscousFluidInterpolator implements Interpolator {
        private static final float VISCOUS_FLUID_NORMALIZE = 0.0f;
        private static final float VISCOUS_FLUID_OFFSET = 0.0f;
        private static final float VISCOUS_FLUID_SCALE = 8.0f;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.widget.Scroller.ViscousFluidInterpolator.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.widget.Scroller.ViscousFluidInterpolator.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 8 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 9 more
*/
            /*
            // Can't load method instructions.
            */
            throw new UnsupportedOperationException("Method not decompiled: android.widget.Scroller.ViscousFluidInterpolator.<clinit>():void");
        }

        ViscousFluidInterpolator() {
        }

        private static float viscousFluid(float x) {
            x *= VISCOUS_FLUID_SCALE;
            if (x < Scroller.END_TENSION) {
                return x - (Scroller.END_TENSION - ((float) Math.exp((double) (-x))));
            }
            return 0.36787945f + (0.63212055f * (Scroller.END_TENSION - ((float) Math.exp((double) (Scroller.END_TENSION - x)))));
        }

        public float getInterpolation(float input) {
            float interpolated = VISCOUS_FLUID_NORMALIZE * viscousFluid(input);
            if (interpolated > VISCOUS_FLUID_OFFSET) {
                return VISCOUS_FLUID_OFFSET + interpolated;
            }
            return interpolated;
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.widget.Scroller.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.widget.Scroller.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: android.widget.Scroller.<clinit>():void");
    }

    public Scroller(Context context) {
        this(context, null);
    }

    public Scroller(Context context, Interpolator interpolator) {
        this(context, interpolator, context.getApplicationInfo().targetSdkVersion >= 11);
    }

    public Scroller(Context context, Interpolator interpolator, boolean flywheel) {
        this.mFlingFriction = ViewConfiguration.getScrollFriction();
        this.mPerf = null;
        this.bIsPerfBoostEnabled = false;
        this.sBoostTimeOut = SCROLL_MODE;
        this.scrollBoostTimeOut = SCROLL_MODE;
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
        this.bIsPerfBoostEnabled = context.getResources().getBoolean(R.bool.config_enableCpuBoostForScroller);
        if (this.bIsPerfBoostEnabled) {
            this.sBoostTimeOut = context.getResources().getInteger(R.integer.scrollboost_timeout_param);
            this.sBoostParamVal = context.getResources().getIntArray(R.array.scrollboost_param_value);
        }
        if (this.bIsPerfBoostEnabled) {
            this.mPerf = new BoostFramework();
        }
    }

    public final void setFriction(float friction) {
        this.mDeceleration = computeDeceleration(friction);
        this.mFlingFriction = friction;
    }

    private float computeDeceleration(float friction) {
        return (this.mPpi * 386.0878f) * friction;
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
        return this.mMode == FLING_MODE ? this.mCurrVelocity : this.mVelocity - ((this.mDeceleration * ((float) timePassed())) / 2000.0f);
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
                case SCROLL_MODE /*0*/:
                    float x = this.mInterpolator.getInterpolation(((float) timePassed) * this.mDurationReciprocal);
                    this.mCurrX = this.mStartX + Math.round(this.mDeltaX * x);
                    this.mCurrY = this.mStartY + Math.round(this.mDeltaY * x);
                    break;
                case FLING_MODE /*1*/:
                    float t = ((float) timePassed) / ((float) this.mDuration);
                    int index = (int) (100.0f * t);
                    float distanceCoef = END_TENSION;
                    float velocityCoef = 0.0f;
                    if (index < NB_SAMPLES) {
                        float t_inf = ((float) index) / 100.0f;
                        float t_sup = ((float) (index + FLING_MODE)) / 100.0f;
                        float d_inf = SPLINE_POSITION[index];
                        velocityCoef = (SPLINE_POSITION[index + FLING_MODE] - d_inf) / (t_sup - t_inf);
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
        }
        this.mCurrX = this.mFinalX;
        this.mCurrY = this.mFinalY;
        this.mFinished = true;
        return true;
    }

    public void startScroll(int startX, int startY, int dx, int dy) {
        startScroll(startX, startY, dx, dy, DEFAULT_DURATION);
    }

    public void startScroll(int startX, int startY, int dx, int dy, int duration) {
        this.mMode = SCROLL_MODE;
        this.mFinished = false;
        this.mDuration = duration;
        this.mStartTime = AnimationUtils.currentAnimationTimeMillis();
        this.mStartX = startX;
        this.mStartY = startY;
        this.mFinalX = startX + dx;
        this.mFinalY = startY + dy;
        this.mDeltaX = (float) dx;
        this.mDeltaY = (float) dy;
        this.mDurationReciprocal = END_TENSION / ((float) this.mDuration);
        if (this.mPerf != null && duration != 0) {
            if (this.sBoostTimeOut == 0) {
                this.scrollBoostTimeOut = this.mDuration;
            } else {
                this.scrollBoostTimeOut = this.sBoostTimeOut;
            }
            this.mPerf.perfLockAcquire(this.scrollBoostTimeOut, this.sBoostParamVal);
        }
    }

    public void fling(int startX, int startY, int velocityX, int velocityY, int minX, int maxX, int minY, int maxY) {
        float coeffX;
        float coeffY;
        if (this.mFlywheel && !this.mFinished) {
            float oldVel = getCurrVelocity();
            float dx = (float) (this.mFinalX - this.mStartX);
            float dy = (float) (this.mFinalY - this.mStartY);
            float hyp = (float) Math.hypot((double) dx, (double) dy);
            float oldVelocityX = (dx / hyp) * oldVel;
            float oldVelocityY = (dy / hyp) * oldVel;
            if (Math.signum((float) velocityX) == Math.signum(oldVelocityX)) {
                if (Math.signum((float) velocityY) == Math.signum(oldVelocityY)) {
                    velocityX = (int) (((float) velocityX) + oldVelocityX);
                    velocityY = (int) (((float) velocityY) + oldVelocityY);
                }
            }
        }
        this.mMode = FLING_MODE;
        this.mFinished = false;
        float velocity = (float) Math.hypot((double) velocityX, (double) velocityY);
        this.mVelocity = velocity;
        this.mDuration = getSplineFlingDuration(velocity);
        this.mStartTime = AnimationUtils.currentAnimationTimeMillis();
        this.mStartX = startX;
        this.mStartY = startY;
        if (velocity == 0.0f) {
            coeffX = END_TENSION;
        } else {
            coeffX = ((float) velocityX) / velocity;
        }
        if (velocity == 0.0f) {
            coeffY = END_TENSION;
        } else {
            coeffY = ((float) velocityY) / velocity;
        }
        double totalDistance = getSplineFlingDistance(velocity);
        this.mDistance = (int) (((double) Math.signum(velocity)) * totalDistance);
        this.mMinX = minX;
        this.mMaxX = maxX;
        this.mMinY = minY;
        this.mMaxY = maxY;
        this.mFinalX = ((int) Math.round(((double) coeffX) * totalDistance)) + startX;
        this.mFinalX = Math.min(this.mFinalX, this.mMaxX);
        this.mFinalX = Math.max(this.mFinalX, this.mMinX);
        this.mFinalY = ((int) Math.round(((double) coeffY) * totalDistance)) + startY;
        this.mFinalY = Math.min(this.mFinalY, this.mMaxY);
        this.mFinalY = Math.max(this.mFinalY, this.mMinY);
    }

    private double getSplineDeceleration(float velocity) {
        return Math.log((double) ((Math.abs(velocity) * INFLEXION) / (this.mFlingFriction * this.mPhysicalCoeff)));
    }

    private int getSplineFlingDuration(float velocity) {
        return (int) (Math.exp(getSplineDeceleration(velocity) / (((double) DECELERATION_RATE) - 1.0d)) * 1000.0d);
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
        this.mDurationReciprocal = END_TENSION / ((float) this.mDuration);
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
        if (!this.mFinished && Math.signum(xvel) == Math.signum((float) (this.mFinalX - this.mStartX)) && Math.signum(yvel) == Math.signum((float) (this.mFinalY - this.mStartY))) {
            return true;
        }
        return false;
    }
}
