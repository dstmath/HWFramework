package android.widget;

import android.content.Context;
import android.os.SystemProperties;
import android.scrollerboost.ScrollerBoostManager;
import android.util.Log;
import android.view.ViewConfiguration;
import android.view.animation.AnimationUtils;
import android.view.animation.Interpolator;
import android.widget.Scroller;
import com.huawei.emui.hiexperience.hwperf.speedloader.HwPerfSpeedLoader;
import com.huawei.featurelayer.HwFeatureLoader;
import com.huawei.featurelayer.featureframework.IFeatureFramework;
import com.huawei.featurelayer.systemfeature.HwWidget.IHwSplineOverScrollerEx;
import com.huawei.uifirst.smartslide.SmartSlideOverScroller;

public class OverScroller {
    private static final int DEFAULT_DURATION = 600;
    private static final float DEFAULT_MULTIPLE_FLING_LENGTH_THRESHOLD = 125.0f;
    private static final int FLING_MODE = 1;
    private static final String LOG_TAG = "OverScrollerOptimization";
    private static final int SCROLL_MODE = 0;
    /* access modifiers changed from: private */
    public static boolean SMART_SLIDE_PROPERTIES = SystemProperties.getBoolean("uifirst_listview_optimization_enable", false);
    private static boolean mIsApplicationEnable = true;
    /* access modifiers changed from: private */
    public static boolean mIsEnable = false;
    /* access modifiers changed from: private */
    public static SmartSlideOverScroller mSmartSlideOverScroller;
    /* access modifiers changed from: private */
    public static int sVelocityToSplineOverScroller;
    private final boolean mFlywheel;
    private HwPerfSpeedLoader mHwPerfSpeedLoader;
    private Interpolator mInterpolator;
    private int mMode;
    private final SplineOverScroller mScrollerX;
    private final SplineOverScroller mScrollerY;

    public static class SplineOverScroller {
        private static final int BALLISTIC = 2;
        private static final int CUBIC = 1;
        private static final double CURVE_CUT_THRESHOLD = 1.0d;
        private static float DECELERATION_RATE = ((float) (Math.log(0.78d) / Math.log(0.9d)));
        private static final float END_TENSION = 1.0f;
        private static final float GRAVITY = 2000.0f;
        private static final float INFLEXION = 0.35f;
        private static final int NB_SAMPLES = 100;
        private static final float P1 = 0.175f;
        private static final float P2 = 0.35000002f;
        private static final int SPLINE = 0;
        private static final float[] SPLINE_POSITION = new float[101];
        private static final float[] SPLINE_TIME = new float[101];
        private static final float START_TENSION = 0.5f;
        /* access modifiers changed from: private */
        public float mCurrVelocity;
        /* access modifiers changed from: private */
        public int mCurrentPosition;
        private long mCurrentTimeHolder = 0;
        private float mDeceleration;
        /* access modifiers changed from: private */
        public int mDuration;
        /* access modifiers changed from: private */
        public int mFinal;
        /* access modifiers changed from: private */
        public boolean mFinished = true;
        private float mFlingFriction = ViewConfiguration.getScrollFriction();
        private boolean mFlinging;
        public IHwSplineOverScrollerEx mIHwSplineOverScrollerInner;
        private int mOver;
        private float mPhysicalCoeff;
        private int mSplineDistance;
        private double mSplineDistanceDiff;
        private double mSplineDistanceHolder = 0.0d;
        private int mSplineDuration;
        /* access modifiers changed from: private */
        public int mStart;
        /* access modifiers changed from: private */
        public long mStartTime;
        /* access modifiers changed from: private */
        public int mState = 0;
        private int mVelocity;

        class HwSplineOverScrollerExDummy implements IHwSplineOverScrollerEx {
            public HwSplineOverScrollerExDummy() {
            }

            public void initSplineOverScrollerImpl(Object sos, Context context) {
            }

            public void resetLastDistanceValue(double lastDistance, double lastDistanceActual) {
            }

            public void setStableItemHeight(int h) {
            }

            public double adjustDistance(double oirginalDistance) {
                return oirginalDistance;
            }

            public double getBallisticDistance(double originalDistance, int start, int end, long duration, long currentTime) {
                return originalDistance;
            }

            public double getCubicDistance(double originalDistance, int start, int end, long duration, long currentTime) {
                return originalDistance;
            }

            public int getBallisticDuration(int originalDuration) {
                return originalDuration;
            }

            public int getCubicDuration(int originalDuration) {
                return originalDuration;
            }

            public int adjustBallisticVelocity(int originalVelocity, float acceleration, int maxOver) {
                return originalVelocity;
            }

            public double getSplineFlingDistance(double orignDistance, int velocity, double decelerationRate, float flingFriction, float physicalCoeff) {
                return orignDistance;
            }

            public int getSplineFlingDuration(int orignDurtion, int velocity, double decelerationRate, float flingFriction, float physicalCoeff) {
                return orignDurtion;
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

        /* access modifiers changed from: package-private */
        public void setFriction(float friction) {
            this.mFlingFriction = friction;
        }

        SplineOverScroller(Context context) {
            this.mPhysicalCoeff = 386.0878f * context.getResources().getDisplayMetrics().density * 160.0f * 0.84f;
            initHwSplineOverScroller(this, context);
        }

        private void initHwSplineOverScroller(Object obj, Context context) {
            IFeatureFramework ff = HwFeatureLoader.SystemFeature.getFeatureFramework();
            if (ff == null) {
                this.mIHwSplineOverScrollerInner = new HwSplineOverScrollerExDummy();
                return;
            }
            this.mIHwSplineOverScrollerInner = ff.loadFeature("com.huawei.featurelayer.systemfeature.HwWidget", "com.huawei.featurelayer.systemfeature.HwWidget.IHwSplineOverScrollerEx");
            if (this.mIHwSplineOverScrollerInner != null) {
                this.mIHwSplineOverScrollerInner.initSplineOverScrollerImpl(obj, context);
            } else {
                this.mIHwSplineOverScrollerInner = new HwSplineOverScrollerExDummy();
            }
        }

        /* access modifiers changed from: package-private */
        public void updateScroll(float q) {
            this.mCurrentPosition = this.mStart + Math.round(((float) (this.mFinal - this.mStart)) * q);
        }

        private static float getDeceleration(int velocity) {
            if (velocity > 0) {
                return -2000.0f;
            }
            return GRAVITY;
        }

        private void adjustDuration(int start, int oldFinal, int newFinal) {
            int oldDistance = oldFinal - start;
            int newDistance = newFinal - start;
            if (OverScroller.SMART_SLIDE_PROPERTIES) {
                this.mDuration = OverScroller.mSmartSlideOverScroller.getAdjustDuratuion(newDistance, this.mSplineDuration, this.mSplineDistance, this.mSplineDistanceDiff);
                return;
            }
            float x = Math.abs(((float) newDistance) / ((float) oldDistance));
            int index = (int) (100.0f * x);
            if (index < 100) {
                float x_inf = ((float) index) / 100.0f;
                float t_inf = SPLINE_TIME[index];
                float t_sup = SPLINE_TIME[index + 1];
                this.mDuration = (int) (((float) this.mDuration) * ((((x - x_inf) / ((((float) (index + 1)) / 100.0f) - x_inf)) * (t_sup - t_inf)) + t_inf));
            }
        }

        /* access modifiers changed from: package-private */
        public void startScroll(int start, int distance, int duration) {
            this.mFinished = false;
            this.mStart = start;
            this.mCurrentPosition = start;
            this.mFinal = start + distance;
            this.mStartTime = AnimationUtils.currentAnimationTimeMillis();
            this.mDuration = duration;
            this.mDeceleration = 0.0f;
            this.mVelocity = 0;
        }

        /* access modifiers changed from: package-private */
        public void finish() {
            if (this.mFlinging) {
                ScrollerBoostManager.getInstance().finishListFling(this.mCurrVelocity);
                this.mFlinging = false;
            }
            this.mCurrentPosition = this.mFinal;
            this.mFinished = true;
        }

        /* access modifiers changed from: package-private */
        public void setFinalPosition(int position) {
            this.mFinal = position;
            this.mFinished = false;
        }

        /* access modifiers changed from: package-private */
        public void extendDuration(int extend) {
            this.mDuration = ((int) (AnimationUtils.currentAnimationTimeMillis() - this.mStartTime)) + extend;
            this.mFinished = false;
        }

        /* access modifiers changed from: package-private */
        public boolean springback(int start, int min, int max) {
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
            return true ^ this.mFinished;
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
            this.mDuration = (int) (1000.0d * Math.sqrt((-2.0d * ((double) delta)) / ((double) this.mDeceleration)));
            this.mDuration = this.mIHwSplineOverScrollerInner.getCubicDuration(this.mDuration);
        }

        /* access modifiers changed from: package-private */
        public void fling(int start, int velocity, int min, int max, int over) {
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
                this.mSplineDistanceHolder = 0.0d;
                this.mCurrentTimeHolder = 0;
                if (OverScroller.SMART_SLIDE_PROPERTIES && OverScroller.mIsEnable) {
                    this.mSplineDistanceDiff = OverScroller.mSmartSlideOverScroller.getDistanceDiff(velocity);
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
            return Math.log((double) ((INFLEXION * ((float) Math.abs(velocity))) / (this.mFlingFriction * this.mPhysicalCoeff)));
        }

        private double getSplineFlingDistance(int velocity) {
            if (OverScroller.SMART_SLIDE_PROPERTIES && OverScroller.mIsEnable) {
                return OverScroller.mSmartSlideOverScroller.getSplineFlingDistance(velocity);
            }
            return ((double) (this.mFlingFriction * this.mPhysicalCoeff)) * Math.exp((((double) DECELERATION_RATE) / (((double) DECELERATION_RATE) - CURVE_CUT_THRESHOLD)) * getSplineDeceleration(velocity));
        }

        private int getSplineFlingDuration(int velocity) {
            if (!OverScroller.SMART_SLIDE_PROPERTIES || !OverScroller.mIsEnable) {
                return (int) (1000.0d * Math.exp(getSplineDeceleration(velocity) / (((double) DECELERATION_RATE) - CURVE_CUT_THRESHOLD)));
            }
            return OverScroller.mSmartSlideOverScroller.getSplineFlingDuration(velocity);
        }

        private void fitOnBounceCurve(int start, int end, int velocity) {
            float durationToApex = ((float) (-velocity)) / this.mDeceleration;
            float totalDuration = (float) Math.sqrt((2.0d * ((double) ((((((float) velocity) * ((float) velocity)) / 2.0f) / Math.abs(this.mDeceleration)) + ((float) Math.abs(end - start))))) / ((double) Math.abs(this.mDeceleration)));
            this.mStartTime -= (long) ((int) (1000.0f * (totalDuration - durationToApex)));
            this.mStart = end;
            this.mCurrentPosition = end;
            this.mVelocity = (int) ((-this.mDeceleration) * totalDuration);
        }

        private void startBounceAfterEdge(int start, int end, int velocity) {
            this.mDeceleration = getDeceleration(velocity == 0 ? start - end : velocity);
            fitOnBounceCurve(start, end, velocity);
            onEdgeReached();
        }

        private void startAfterEdge(int start, int min, int max, int velocity) {
            int i = start;
            int i2 = max;
            int i3 = velocity;
            boolean keepIncreasing = true;
            int i4 = min;
            if (i <= i4 || i >= i2) {
                boolean positive = i > i2;
                int edge = positive ? i2 : i4;
                int overDistance = i - edge;
                if (overDistance * i3 < 0) {
                    keepIncreasing = false;
                }
                if (keepIncreasing) {
                    startBounceAfterEdge(i, edge, i3);
                } else if (getSplineFlingDistance(i3) > ((double) Math.abs(overDistance))) {
                    fling(i, i3, positive ? i4 : i, positive ? i : i2, this.mOver);
                } else {
                    startSpringback(i, edge, i3);
                }
                return;
            }
            Log.e("OverScroller", "startAfterEdge called from a valid position");
            this.mFinished = true;
        }

        /* access modifiers changed from: package-private */
        public void notifyEdgeReached(int start, int end, int over) {
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
                this.mDeceleration = ((-sign) * velocitySquared) / (2.0f * ((float) this.mOver));
                distance = (float) this.mOver;
            }
            this.mOver = (int) distance;
            this.mState = 2;
            this.mFinal = this.mStart + ((int) (this.mVelocity > 0 ? distance : -distance));
            this.mDuration = -((int) ((1000.0f * ((float) this.mVelocity)) / this.mDeceleration));
            this.mDuration = this.mIHwSplineOverScrollerInner.getBallisticDuration(this.mDuration);
        }

        /* access modifiers changed from: package-private */
        public boolean continueWhenFinished() {
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
                    } else {
                        return false;
                    }
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

        /* access modifiers changed from: package-private */
        public boolean update() {
            double distance;
            long currentTime = AnimationUtils.currentAnimationTimeMillis() - this.mStartTime;
            boolean z = false;
            if (currentTime == 0) {
                if (this.mDuration > 0) {
                    z = true;
                }
                return z;
            } else if (currentTime > ((long) this.mDuration)) {
                return false;
            } else {
                double distance2 = 0.0d;
                switch (this.mState) {
                    case 0:
                        if (!OverScroller.SMART_SLIDE_PROPERTIES || !OverScroller.mIsEnable) {
                            float t = ((float) currentTime) / ((float) this.mSplineDuration);
                            int index = (int) (100.0f * t);
                            float distanceCoef = 1.0f;
                            float velocityCoef = 0.0f;
                            if (index < 100) {
                                float t_inf = ((float) index) / 100.0f;
                                float d_inf = SPLINE_POSITION[index];
                                velocityCoef = (SPLINE_POSITION[index + 1] - d_inf) / ((((float) (index + 1)) / 100.0f) - t_inf);
                                distanceCoef = d_inf + ((t - t_inf) * velocityCoef);
                            }
                            double distance3 = (double) (((float) this.mSplineDistance) * distanceCoef);
                            this.mCurrVelocity = ((((float) this.mSplineDistance) * velocityCoef) / ((float) this.mSplineDuration)) * 1000.0f;
                            distance = distance3;
                            break;
                        } else {
                            distance = OverScroller.mSmartSlideOverScroller.getUpdateDistance(currentTime, this.mSplineDuration, this.mSplineDistance, this.mSplineDistanceDiff);
                            this.mCurrVelocity = OverScroller.mSmartSlideOverScroller.getUpdateVelocity(currentTime, this.mSplineDuration, OverScroller.sVelocityToSplineOverScroller);
                            if (Math.abs(distance - this.mSplineDistanceHolder) >= CURVE_CUT_THRESHOLD || currentTime == this.mCurrentTimeHolder) {
                                this.mSplineDistanceHolder = distance;
                                this.mCurrentTimeHolder = currentTime;
                                break;
                            } else {
                                this.mSplineDistanceHolder = 0.0d;
                                this.mCurrentTimeHolder = 0;
                                this.mCurrentPosition = this.mStart + ((int) Math.round(distance));
                                this.mFinal = this.mCurrentPosition;
                                return false;
                            }
                        }
                        break;
                    case 1:
                        float t2 = ((float) currentTime) / ((float) this.mDuration);
                        float t22 = t2 * t2;
                        float sign = Math.signum((float) this.mVelocity);
                        double distance4 = (double) (((float) this.mOver) * sign * ((3.0f * t22) - ((2.0f * t2) * t22)));
                        this.mCurrVelocity = ((float) this.mOver) * sign * 6.0f * ((-t2) + t22);
                        double d = distance4;
                        distance2 = this.mIHwSplineOverScrollerInner.getCubicDistance(distance4, this.mStart, this.mFinal, (long) this.mDuration, currentTime);
                        break;
                    case 2:
                        float t3 = ((float) currentTime) / 1000.0f;
                        this.mCurrVelocity = ((float) this.mVelocity) + (this.mDeceleration * t3);
                        double distance5 = (double) ((((float) this.mVelocity) * t3) + (((this.mDeceleration * t3) * t3) / 2.0f));
                        double d2 = distance5;
                        distance2 = this.mIHwSplineOverScrollerInner.getBallisticDistance(distance5, this.mStart, this.mFinal, (long) this.mDuration, currentTime);
                        break;
                }
                distance = distance2;
                this.mCurrentPosition = this.mStart + ((int) Math.round(distance));
                return true;
            }
        }

        public int getSplineOverScrollerVelocity() {
            return this.mVelocity;
        }
    }

    public void registerVelocityListener(HwPerfSpeedLoader hwPerfSpeedLoader) {
        this.mHwPerfSpeedLoader = hwPerfSpeedLoader;
    }

    public OverScroller(Context context) {
        this(context, null);
    }

    public OverScroller(Context context, Interpolator interpolator) {
        this(context, interpolator, true);
    }

    public OverScroller(Context context, Interpolator interpolator, boolean flywheel) {
        if (interpolator == null) {
            this.mInterpolator = new Scroller.ViscousFluidInterpolator();
        } else {
            this.mInterpolator = interpolator;
        }
        if (SMART_SLIDE_PROPERTIES) {
            if (mSmartSlideOverScroller == null) {
                mSmartSlideOverScroller = new SmartSlideOverScroller(context);
            }
            mIsApplicationEnable = mSmartSlideOverScroller.getAppEnable();
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

    /* access modifiers changed from: package-private */
    public void setInterpolator(Interpolator interpolator) {
        if (interpolator == null) {
            this.mInterpolator = new Scroller.ViscousFluidInterpolator();
        } else {
            this.mInterpolator = interpolator;
        }
    }

    public final void setFriction(float friction) {
        this.mScrollerX.setFriction(friction);
        this.mScrollerY.setFriction(friction);
    }

    public final boolean isFinished() {
        return this.mScrollerX.mFinished && this.mScrollerY.mFinished;
    }

    public final void forceFinished(boolean finished) {
        boolean unused = this.mScrollerX.mFinished = this.mScrollerY.mFinished = finished;
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
                } else {
                    float q = this.mInterpolator.getInterpolation(((float) elapsedTime) / ((float) duration));
                    this.mScrollerX.updateScroll(q);
                    this.mScrollerY.updateScroll(q);
                    break;
                }
            case 1:
                if (this.mHwPerfSpeedLoader != null) {
                    this.mHwPerfSpeedLoader.onFlingRunning(getCurrVelocity());
                }
                if (!this.mScrollerX.mFinished && !this.mScrollerX.update() && !this.mScrollerX.continueWhenFinished()) {
                    this.mScrollerX.finish();
                }
                if (!this.mScrollerY.mFinished && !this.mScrollerY.update() && !this.mScrollerY.continueWhenFinished()) {
                    this.mScrollerY.finish();
                    break;
                }
        }
        return true;
    }

    public void startScroll(int startX, int startY, int dx, int dy) {
        startScroll(startX, startY, dx, dy, 600);
    }

    public void startScroll(int startX, int startY, int dx, int dy, int duration) {
        this.mMode = 0;
        this.mScrollerX.startScroll(startX, dx, duration);
        this.mScrollerY.startScroll(startY, dy, duration);
    }

    public boolean springBack(int startX, int startY, int minX, int maxX, int minY, int maxY) {
        this.mMode = 1;
        boolean spingbackX = this.mScrollerX.springback(startX, minX, maxX);
        boolean spingbackY = this.mScrollerY.springback(startY, minY, maxY);
        if (spingbackX || spingbackY) {
            return true;
        }
        return false;
    }

    public void fling(int startX, int startY, int velocityX, int velocityY, int minX, int maxX, int minY, int maxY) {
        if (!SMART_SLIDE_PROPERTIES || !mIsApplicationEnable) {
            mIsEnable = false;
            fling(startX, startY, velocityX, velocityY, minX, maxX, minY, maxY, 0, 0);
        } else {
            mIsEnable = true;
            fling(startX, startY, velocityX, velocityY, minX, maxX, minY, maxY, 0, 0, DEFAULT_MULTIPLE_FLING_LENGTH_THRESHOLD);
        }
        if (this.mHwPerfSpeedLoader != null) {
            this.mHwPerfSpeedLoader.onFlingStart();
        }
    }

    public float getScreenPPI() {
        return mSmartSlideOverScroller.getScreenPPI();
    }

    public void fling(int startX, int startY, int velocityX, int velocityY, int minX, int maxX, int minY, int maxY, int overX, int overY, float slidingDistance) {
        float oldVelocityX = this.mScrollerX.mCurrVelocity;
        float oldVelocityY = this.mScrollerY.mCurrVelocity;
        mIsEnable = true;
        int i = velocityX;
        float f = oldVelocityX;
        int multipleVelocity = mSmartSlideOverScroller.fling(startX, startY, i, velocityY, oldVelocityX, oldVelocityY, minX, maxX, minY, maxY, 0, 0, slidingDistance);
        this.mMode = 1;
        sVelocityToSplineOverScroller = velocityY;
        this.mScrollerX.fling(startX, i, minX, maxX, overX);
        this.mScrollerY.fling(startY, multipleVelocity, minY, maxY, overY);
    }

    public void fling(int startX, int startY, int velocityX, int velocityY, int minX, int maxX, int minY, int maxY, int overX, int overY) {
        int velocityX2;
        int i = velocityX;
        int velocityY2 = velocityY;
        if (!SMART_SLIDE_PROPERTIES || !mIsApplicationEnable) {
            mIsEnable = false;
            if (this.mFlywheel && !isFinished()) {
                float oldVelocityX = this.mScrollerX.mCurrVelocity;
                float oldVelocityY = this.mScrollerY.mCurrVelocity;
                if (Math.signum((float) i) == Math.signum(oldVelocityX) && Math.signum((float) velocityY2) == Math.signum(oldVelocityY)) {
                    velocityX2 = (int) (((float) i) + oldVelocityX);
                    velocityY2 = (int) (((float) velocityY2) + oldVelocityY);
                    this.mMode = 1;
                    this.mScrollerX.fling(startX, velocityX2, minX, maxX, overX);
                    this.mScrollerY.fling(startY, velocityY2, minY, maxY, overY);
                    return;
                }
            }
            velocityX2 = i;
            this.mMode = 1;
            this.mScrollerX.fling(startX, velocityX2, minX, maxX, overX);
            this.mScrollerY.fling(startY, velocityY2, minY, maxY, overY);
            return;
        }
        mIsEnable = true;
        fling(startX, startY, i, velocityY2, minX, maxX, minY, maxY, overX, overY, DEFAULT_MULTIPLE_FLING_LENGTH_THRESHOLD);
    }

    public void notifyHorizontalEdgeReached(int startX, int finalX, int overX) {
        this.mScrollerX.notifyEdgeReached(startX, finalX, overX);
    }

    public void notifyVerticalEdgeReached(int startY, int finalY, int overY) {
        this.mScrollerY.notifyEdgeReached(startY, finalY, overY);
    }

    public boolean isOverScrolled() {
        return (!this.mScrollerX.mFinished && this.mScrollerX.mState != 0) || (!this.mScrollerY.mFinished && this.mScrollerY.mState != 0);
    }

    public void abortAnimation() {
        this.mScrollerX.finish();
        this.mScrollerY.finish();
    }

    public int timePassed() {
        return (int) (AnimationUtils.currentAnimationTimeMillis() - Math.min(this.mScrollerX.mStartTime, this.mScrollerY.mStartTime));
    }

    public boolean isScrollingInDirection(float xvel, float yvel) {
        return !isFinished() && Math.signum(xvel) == Math.signum((float) (this.mScrollerX.mFinal - this.mScrollerX.mStart)) && Math.signum(yvel) == Math.signum((float) (this.mScrollerY.mFinal - this.mScrollerY.mStart));
    }

    public IHwSplineOverScrollerEx getIHwSplineOverScroller() {
        return this.mScrollerY.mIHwSplineOverScrollerInner;
    }
}
