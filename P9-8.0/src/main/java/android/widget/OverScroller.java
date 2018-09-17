package android.widget;

import android.content.Context;
import android.hwcontrol.HwWidgetFactory;
import android.os.Process;
import android.os.SystemProperties;
import android.scrollerboost.ScrollerBoostManager;
import android.util.BoostFramework;
import android.util.Log;
import android.view.ViewConfiguration;
import android.view.animation.AnimationUtils;
import android.view.animation.Interpolator;
import com.android.internal.R;

public class OverScroller {
    private static final int DEFAULT_DURATION = 250;
    private static final int FLING_MODE = 1;
    private static final int SCROLL_MODE = 0;
    private final boolean mFlywheel;
    private Interpolator mInterpolator;
    private int mMode;
    private final SplineOverScroller mScrollerX;
    private final SplineOverScroller mScrollerY;

    public static class SplineOverScroller {
        private static final int BALLISTIC = 2;
        private static final int CUBIC = 1;
        private static float DECELERATION_RATE = ((float) (Math.log(0.78d) / Math.log(0.9d)));
        private static final float END_TENSION = 1.0f;
        private static final float GRAVITY = 2000.0f;
        private static final float INFLEXION = 0.35f;
        private static final int MAX_FLING_BOOST_TIME = 1000;
        private static final int NB_SAMPLES = 100;
        private static final float P1 = 0.175f;
        private static final float P2 = 0.35000002f;
        private static final int SPLINE = 0;
        private static final float[] SPLINE_POSITION = new float[101];
        private static final float[] SPLINE_TIME = new float[101];
        private static final float START_TENSION = 0.5f;
        private static final String SYSTEM_SERVER = "system_server";
        private static final int SYSTEM_SERVER_BOOST_TIME = 500;
        private int[] fBoostParamVal;
        private int fBoostTimeOut = 0;
        private int flingBoostTimeOut = 0;
        private float mCurrVelocity;
        private int mCurrentPosition;
        private float mDeceleration;
        private int mDuration;
        private int mFinal;
        private boolean mFinished = true;
        private float mFlingFriction = ViewConfiguration.getScrollFriction();
        private boolean mFlinging;
        public IHwSplineOverScroller mIHwSplineOverScrollerInner;
        private boolean mIsPerfBoostEnabled = false;
        private boolean mIsPerfLockAcquired = false;
        private boolean mIsSsystemServer;
        private int mOver;
        private BoostFramework mPerf = null;
        private float mPhysicalCoeff;
        private int mSplineDistance;
        private int mSplineDuration;
        private int mStart;
        private long mStartTime;
        private int mState = 0;
        private int mVelocity;

        static {
            float x_min = 0.0f;
            float y_min = 0.0f;
            for (int i = 0; i < 100; i++) {
                float x;
                float coef;
                float y;
                float alpha = ((float) i) / 100.0f;
                float x_max = 1.0f;
                while (true) {
                    x = x_min + ((x_max - x_min) / 2.0f);
                    coef = (3.0f * x) * (1.0f - x);
                    float tx = ((((1.0f - x) * P1) + (P2 * x)) * coef) + ((x * x) * x);
                    if (((double) Math.abs(tx - alpha)) < 1.0E-5d) {
                        break;
                    } else if (tx > alpha) {
                        x_max = x;
                    } else {
                        x_min = x;
                    }
                }
                SPLINE_POSITION[i] = ((((1.0f - x) * START_TENSION) + x) * coef) + ((x * x) * x);
                float y_max = 1.0f;
                while (true) {
                    y = y_min + ((y_max - y_min) / 2.0f);
                    coef = (3.0f * y) * (1.0f - y);
                    float dy = ((((1.0f - y) * START_TENSION) + y) * coef) + ((y * y) * y);
                    if (((double) Math.abs(dy - alpha)) < 1.0E-5d) {
                        break;
                    } else if (dy > alpha) {
                        y_max = y;
                    } else {
                        y_min = y;
                    }
                }
                SPLINE_TIME[i] = ((((1.0f - y) * P1) + (P2 * y)) * coef) + ((y * y) * y);
            }
            float[] fArr = SPLINE_POSITION;
            SPLINE_TIME[100] = 1.0f;
            fArr[100] = 1.0f;
        }

        void setFriction(float friction) {
            this.mFlingFriction = friction;
        }

        SplineOverScroller(Context context) {
            this.mPhysicalCoeff = (386.0878f * ((context.getResources().getDisplayMetrics().density * 160.0f) * (((float) SystemProperties.getInt("hw.lcd.density.scale", 1000)) / 1000.0f))) * 0.84f;
            this.mIHwSplineOverScrollerInner = HwWidgetFactory.getHwSplineOverScroller(this, context);
            this.mIsPerfBoostEnabled = context.getResources().getBoolean(R.bool.config_enableCpuBoostForOverScrollerFling);
            if (this.mIsPerfBoostEnabled) {
                this.fBoostTimeOut = context.getResources().getInteger(R.integer.flingboost_timeout_param);
                this.fBoostParamVal = context.getResources().getIntArray(R.array.flingboost_param_value);
                this.mPerf = new BoostFramework();
                this.mIsSsystemServer = SYSTEM_SERVER.equals(Process.getCmdlineForPid(Process.myPid()));
            }
        }

        void updateScroll(float q) {
            this.mCurrentPosition = this.mStart + Math.round(((float) (this.mFinal - this.mStart)) * q);
        }

        private static float getDeceleration(int velocity) {
            return velocity > 0 ? -2000.0f : GRAVITY;
        }

        private void adjustDuration(int start, int oldFinal, int newFinal) {
            float x = Math.abs(((float) (newFinal - start)) / ((float) (oldFinal - start)));
            int index = (int) (100.0f * x);
            if (index < 100) {
                float x_inf = ((float) index) / 100.0f;
                float x_sup = ((float) (index + 1)) / 100.0f;
                float t_inf = SPLINE_TIME[index];
                this.mDuration = (int) (((float) this.mDuration) * (t_inf + (((x - x_inf) / (x_sup - x_inf)) * (SPLINE_TIME[index + 1] - t_inf))));
            }
        }

        void startScroll(int start, int distance, int duration) {
            this.mFinished = false;
            this.mStart = start;
            this.mCurrentPosition = start;
            this.mFinal = start + distance;
            this.mStartTime = AnimationUtils.currentAnimationTimeMillis();
            this.mDuration = duration;
            this.mDeceleration = 0.0f;
            this.mVelocity = 0;
        }

        void finish() {
            if (this.mFlinging) {
                ScrollerBoostManager.getInstance().listFling(-1);
                this.mFlinging = false;
            }
            if (this.mIsPerfLockAcquired && this.mPerf != null) {
                this.mPerf.perfLockRelease();
                this.mIsPerfLockAcquired = false;
            }
            this.mCurrentPosition = this.mFinal;
            this.mFinished = true;
        }

        void setFinalPosition(int position) {
            this.mFinal = position;
            this.mFinished = false;
        }

        void extendDuration(int extend) {
            this.mDuration = ((int) (AnimationUtils.currentAnimationTimeMillis() - this.mStartTime)) + extend;
            this.mFinished = false;
        }

        boolean springback(int start, int min, int max) {
            this.mFinished = true;
            this.mFinal = start;
            this.mStart = start;
            this.mCurrentPosition = start;
            this.mVelocity = 0;
            this.mStartTime = AnimationUtils.currentAnimationTimeMillis();
            this.mDuration = 0;
            if (start < min) {
                startSpringback(start, min, 0);
            } else if (start > max) {
                startSpringback(start, max, 0);
            }
            return this.mFinished ^ 1;
        }

        private void startSpringback(int start, int end, int velocity) {
            this.mFinished = false;
            this.mState = 1;
            this.mStart = start;
            this.mCurrentPosition = start;
            this.mFinal = end;
            int delta = start - end;
            this.mDeceleration = getDeceleration(delta);
            this.mVelocity = -delta;
            this.mOver = Math.abs(delta);
            this.mDuration = (int) (Math.sqrt((((double) delta) * -2.0d) / ((double) this.mDeceleration)) * 1000.0d);
            this.mDuration = this.mIHwSplineOverScrollerInner.getCubicDuration(this.mDuration);
        }

        void fling(int start, int velocity, int min, int max, int over) {
            int i = 1000;
            this.mOver = over;
            this.mFinished = false;
            this.mVelocity = velocity;
            this.mCurrVelocity = (float) velocity;
            this.mSplineDuration = 0;
            this.mDuration = 0;
            this.mStartTime = AnimationUtils.currentAnimationTimeMillis();
            this.mStart = start;
            this.mCurrentPosition = start;
            this.mIHwSplineOverScrollerInner.resetLastDistanceValue(0.0d, 0.0d);
            if (start > max || start < min) {
                startAfterEdge(start, min, max, velocity);
                return;
            }
            this.mState = 0;
            double totalDistance = 0.0d;
            if (velocity != 0) {
                int splineFlingDuration = getSplineFlingDuration(velocity);
                this.mSplineDuration = splineFlingDuration;
                this.mDuration = splineFlingDuration;
                totalDistance = getSplineFlingDistance(velocity);
                if (this.mPerf != null) {
                    this.mIsPerfLockAcquired = true;
                    if (this.fBoostTimeOut == 0) {
                        if (this.mDuration <= 1000) {
                            i = this.mDuration;
                        }
                        this.flingBoostTimeOut = i;
                        this.flingBoostTimeOut = this.mIsSsystemServer ? 500 : this.flingBoostTimeOut;
                    } else {
                        this.flingBoostTimeOut = this.fBoostTimeOut;
                    }
                    this.mPerf.perfLockAcquire(this.flingBoostTimeOut, this.fBoostParamVal);
                }
                ScrollerBoostManager.getInstance().listFling(this.mDuration);
                this.mFlinging = true;
            }
            this.mSplineDistance = (int) (((double) Math.signum((float) velocity)) * totalDistance);
            this.mFinal = this.mSplineDistance + start;
            if (this.mFinal < min) {
                adjustDuration(this.mStart, this.mFinal, min);
                this.mFinal = min;
            }
            if (this.mFinal > max) {
                adjustDuration(this.mStart, this.mFinal, max);
                this.mFinal = max;
            }
        }

        private double getSplineDeceleration(int velocity) {
            return Math.log((double) ((((float) Math.abs(velocity)) * INFLEXION) / (this.mFlingFriction * this.mPhysicalCoeff)));
        }

        private double getSplineFlingDistance(int velocity) {
            return ((double) (this.mFlingFriction * this.mPhysicalCoeff)) * Math.exp((((double) DECELERATION_RATE) / (((double) DECELERATION_RATE) - 1.0d)) * getSplineDeceleration(velocity));
        }

        private int getSplineFlingDuration(int velocity) {
            return (int) (Math.exp(getSplineDeceleration(velocity) / (((double) DECELERATION_RATE) - 1.0d)) * 1000.0d);
        }

        private void fitOnBounceCurve(int start, int end, int velocity) {
            float totalDuration = (float) Math.sqrt((((double) ((((((float) velocity) * ((float) velocity)) / 2.0f) / Math.abs(this.mDeceleration)) + ((float) Math.abs(end - start)))) * 2.0d) / ((double) Math.abs(this.mDeceleration)));
            this.mStartTime -= (long) ((int) ((totalDuration - (((float) (-velocity)) / this.mDeceleration)) * 1000.0f));
            this.mStart = end;
            this.mCurrentPosition = end;
            this.mVelocity = (int) ((-this.mDeceleration) * totalDuration);
        }

        private void startBounceAfterEdge(int start, int end, int velocity) {
            int i;
            if (velocity == 0) {
                i = start - end;
            } else {
                i = velocity;
            }
            this.mDeceleration = getDeceleration(i);
            fitOnBounceCurve(start, end, velocity);
            onEdgeReached();
        }

        private void startAfterEdge(int start, int min, int max, int velocity) {
            if (start <= min || start >= max) {
                boolean positive = start > max;
                int edge = positive ? max : min;
                int overDistance = start - edge;
                if (overDistance * velocity >= 0) {
                    startBounceAfterEdge(start, edge, velocity);
                } else if (getSplineFlingDistance(velocity) > ((double) Math.abs(overDistance))) {
                    fling(start, velocity, positive ? min : start, positive ? start : max, this.mOver);
                } else {
                    startSpringback(start, edge, velocity);
                }
                return;
            }
            Log.e("OverScroller", "startAfterEdge called from a valid position");
            this.mFinished = true;
        }

        void notifyEdgeReached(int start, int end, int over) {
            if (this.mState == 0) {
                this.mOver = over;
                this.mStartTime = AnimationUtils.currentAnimationTimeMillis();
                startAfterEdge(start, end, end, (int) this.mCurrVelocity);
            }
        }

        private void onEdgeReached() {
            this.mVelocity = this.mIHwSplineOverScrollerInner.adjustBallisticVelocity(this.mVelocity, this.mDeceleration, this.mOver);
            float velocitySquared = ((float) this.mVelocity) * ((float) this.mVelocity);
            float distance = velocitySquared / (Math.abs(this.mDeceleration) * 2.0f);
            float sign = Math.signum((float) this.mVelocity);
            if (distance > ((float) this.mOver)) {
                this.mDeceleration = ((-sign) * velocitySquared) / (((float) this.mOver) * 2.0f);
                distance = (float) this.mOver;
            }
            this.mOver = (int) distance;
            this.mState = 2;
            int i = this.mStart;
            if (this.mVelocity <= 0) {
                distance = -distance;
            }
            this.mFinal = i + ((int) distance);
            this.mDuration = -((int) ((((float) this.mVelocity) * 1000.0f) / this.mDeceleration));
            this.mDuration = this.mIHwSplineOverScrollerInner.getBallisticDuration(this.mDuration);
        }

        boolean continueWhenFinished() {
            switch (this.mState) {
                case 0:
                    if (this.mDuration < this.mSplineDuration) {
                        int i = this.mFinal;
                        this.mStart = i;
                        this.mCurrentPosition = i;
                        this.mVelocity = (int) this.mCurrVelocity;
                        this.mDeceleration = getDeceleration(this.mVelocity);
                        this.mStartTime += (long) this.mDuration;
                        onEdgeReached();
                        break;
                    }
                    return false;
                case 1:
                    return false;
                case 2:
                    this.mStartTime += (long) this.mDuration;
                    startSpringback(this.mFinal, this.mStart, 0);
                    break;
            }
            update();
            return true;
        }

        boolean update() {
            long currentTime = AnimationUtils.currentAnimationTimeMillis() - this.mStartTime;
            if (currentTime == 0) {
                return this.mDuration > 0;
            } else if (currentTime > ((long) this.mDuration)) {
                return false;
            } else {
                double distance = 0.0d;
                float t;
                switch (this.mState) {
                    case 0:
                        t = ((float) currentTime) / ((float) this.mSplineDuration);
                        int index = (int) (100.0f * t);
                        float distanceCoef = 1.0f;
                        float velocityCoef = 0.0f;
                        if (index < 100) {
                            float t_inf = ((float) index) / 100.0f;
                            float t_sup = ((float) (index + 1)) / 100.0f;
                            float d_inf = SPLINE_POSITION[index];
                            velocityCoef = (SPLINE_POSITION[index + 1] - d_inf) / (t_sup - t_inf);
                            distanceCoef = d_inf + ((t - t_inf) * velocityCoef);
                        }
                        distance = (double) (((float) this.mSplineDistance) * distanceCoef);
                        this.mCurrVelocity = ((((float) this.mSplineDistance) * velocityCoef) / ((float) this.mSplineDuration)) * 1000.0f;
                        break;
                    case 1:
                        t = ((float) currentTime) / ((float) this.mDuration);
                        float t2 = t * t;
                        float sign = Math.signum((float) this.mVelocity);
                        distance = (double) ((((float) this.mOver) * sign) * ((3.0f * t2) - ((2.0f * t) * t2)));
                        this.mCurrVelocity = ((((float) this.mOver) * sign) * 6.0f) * ((-t) + t2);
                        distance = this.mIHwSplineOverScrollerInner.getCubicDistance(distance, this.mStart, this.mFinal, (long) this.mDuration, currentTime);
                        break;
                    case 2:
                        t = ((float) currentTime) / 1000.0f;
                        this.mCurrVelocity = ((float) this.mVelocity) + (this.mDeceleration * t);
                        distance = this.mIHwSplineOverScrollerInner.getBallisticDistance((double) ((((float) this.mVelocity) * t) + (((this.mDeceleration * t) * t) / 2.0f)), this.mStart, this.mFinal, (long) this.mDuration, currentTime);
                        break;
                }
                this.mCurrentPosition = this.mStart + ((int) Math.round(distance));
                return true;
            }
        }

        public int getSplineOverScrollerVelocity() {
            return this.mVelocity;
        }
    }

    public OverScroller(Context context) {
        this(context, null);
    }

    public OverScroller(Context context, Interpolator interpolator) {
        this(context, interpolator, true);
    }

    public OverScroller(Context context, Interpolator interpolator, boolean flywheel) {
        if (interpolator == null) {
            this.mInterpolator = new ViscousFluidInterpolator();
        } else {
            this.mInterpolator = interpolator;
        }
        this.mFlywheel = flywheel;
        this.mScrollerX = new SplineOverScroller(context);
        this.mScrollerY = new SplineOverScroller(context);
    }

    @Deprecated
    public OverScroller(Context context, Interpolator interpolator, float bounceCoefficientX, float bounceCoefficientY) {
        this(context, interpolator, true);
    }

    @Deprecated
    public OverScroller(Context context, Interpolator interpolator, float bounceCoefficientX, float bounceCoefficientY, boolean flywheel) {
        this(context, interpolator, flywheel);
    }

    void setInterpolator(Interpolator interpolator) {
        if (interpolator == null) {
            this.mInterpolator = new ViscousFluidInterpolator();
        } else {
            this.mInterpolator = interpolator;
        }
    }

    public final void setFriction(float friction) {
        this.mScrollerX.setFriction(friction);
        this.mScrollerY.setFriction(friction);
    }

    public final boolean isFinished() {
        return this.mScrollerX.mFinished ? this.mScrollerY.mFinished : false;
    }

    public final void forceFinished(boolean finished) {
        this.mScrollerX.mFinished = this.mScrollerY.mFinished = finished;
    }

    public final int getCurrX() {
        return this.mScrollerX.mCurrentPosition;
    }

    public final int getCurrY() {
        return this.mScrollerY.mCurrentPosition;
    }

    public float getCurrVelocity() {
        return (float) Math.hypot((double) this.mScrollerX.mCurrVelocity, (double) this.mScrollerY.mCurrVelocity);
    }

    public final int getStartX() {
        return this.mScrollerX.mStart;
    }

    public final int getStartY() {
        return this.mScrollerY.mStart;
    }

    public final int getFinalX() {
        return this.mScrollerX.mFinal;
    }

    public final int getFinalY() {
        return this.mScrollerY.mFinal;
    }

    @Deprecated
    public final int getDuration() {
        return Math.max(this.mScrollerX.mDuration, this.mScrollerY.mDuration);
    }

    @Deprecated
    public void extendDuration(int extend) {
        this.mScrollerX.extendDuration(extend);
        this.mScrollerY.extendDuration(extend);
    }

    @Deprecated
    public void setFinalX(int newX) {
        this.mScrollerX.setFinalPosition(newX);
    }

    @Deprecated
    public void setFinalY(int newY) {
        this.mScrollerY.setFinalPosition(newY);
    }

    public boolean computeScrollOffset() {
        if (isFinished()) {
            return false;
        }
        switch (this.mMode) {
            case 0:
                long elapsedTime = AnimationUtils.currentAnimationTimeMillis() - this.mScrollerX.mStartTime;
                int duration = this.mScrollerX.mDuration;
                if (elapsedTime >= ((long) duration)) {
                    abortAnimation();
                    break;
                }
                float q = this.mInterpolator.getInterpolation(((float) elapsedTime) / ((float) duration));
                this.mScrollerX.updateScroll(q);
                this.mScrollerY.updateScroll(q);
                break;
            case 1:
                if (!(this.mScrollerX.mFinished || this.mScrollerX.update() || this.mScrollerX.continueWhenFinished())) {
                    this.mScrollerX.finish();
                }
                if (!(this.mScrollerY.mFinished || this.mScrollerY.update() || this.mScrollerY.continueWhenFinished())) {
                    this.mScrollerY.finish();
                    break;
                }
        }
        return true;
    }

    public void startScroll(int startX, int startY, int dx, int dy) {
        startScroll(startX, startY, dx, dy, 250);
    }

    public void startScroll(int startX, int startY, int dx, int dy, int duration) {
        this.mMode = 0;
        this.mScrollerX.startScroll(startX, dx, duration);
        this.mScrollerY.startScroll(startY, dy, duration);
    }

    public boolean springBack(int startX, int startY, int minX, int maxX, int minY, int maxY) {
        this.mMode = 1;
        return !this.mScrollerX.springback(startX, minX, maxX) ? this.mScrollerY.springback(startY, minY, maxY) : true;
    }

    public void fling(int startX, int startY, int velocityX, int velocityY, int minX, int maxX, int minY, int maxY) {
        fling(startX, startY, velocityX, velocityY, minX, maxX, minY, maxY, 0, 0);
    }

    public void fling(int startX, int startY, int velocityX, int velocityY, int minX, int maxX, int minY, int maxY, int overX, int overY) {
        if (this.mFlywheel && (isFinished() ^ 1) != 0) {
            float oldVelocityX = this.mScrollerX.mCurrVelocity;
            float oldVelocityY = this.mScrollerY.mCurrVelocity;
            if (Math.signum((float) velocityX) == Math.signum(oldVelocityX) && Math.signum((float) velocityY) == Math.signum(oldVelocityY)) {
                velocityX = (int) (((float) velocityX) + oldVelocityX);
                velocityY = (int) (((float) velocityY) + oldVelocityY);
            }
        }
        this.mMode = 1;
        this.mScrollerX.fling(startX, velocityX, minX, maxX, overX);
        this.mScrollerY.fling(startY, velocityY, minY, maxY, overY);
    }

    public void notifyHorizontalEdgeReached(int startX, int finalX, int overX) {
        this.mScrollerX.notifyEdgeReached(startX, finalX, overX);
    }

    public void notifyVerticalEdgeReached(int startY, int finalY, int overY) {
        this.mScrollerY.notifyEdgeReached(startY, finalY, overY);
    }

    public boolean isOverScrolled() {
        if (!this.mScrollerX.mFinished && this.mScrollerX.mState != 0) {
            return true;
        }
        if (this.mScrollerY.mFinished) {
            return false;
        }
        if (this.mScrollerY.mState == 0) {
            return false;
        }
        return true;
    }

    public void abortAnimation() {
        this.mScrollerX.finish();
        this.mScrollerY.finish();
    }

    public int timePassed() {
        return (int) (AnimationUtils.currentAnimationTimeMillis() - Math.min(this.mScrollerX.mStartTime, this.mScrollerY.mStartTime));
    }

    public boolean isScrollingInDirection(float xvel, float yvel) {
        int dx = this.mScrollerX.mFinal - this.mScrollerX.mStart;
        int dy = this.mScrollerY.mFinal - this.mScrollerY.mStart;
        if (!isFinished() && Math.signum(xvel) == Math.signum((float) dx) && Math.signum(yvel) == Math.signum((float) dy)) {
            return true;
        }
        return false;
    }

    public IHwSplineOverScroller getIHwSplineOverScroller() {
        return this.mScrollerY.mIHwSplineOverScrollerInner;
    }
}
