package android.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.hwcontrol.HwWidgetFactory;
import android.view.animation.AnimationUtils;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import com.android.internal.R;

public class EdgeEffect {
    private static final double ANGLE = 0.5235987755982988d;
    private static final float COS = ((float) Math.cos(ANGLE));
    private static final float EPSILON = 0.001f;
    private static final float MAX_ALPHA = 0.5f;
    private static final float MAX_GLOW_SCALE = 2.0f;
    private static final int MAX_VELOCITY = 10000;
    private static final int MIN_VELOCITY = 100;
    private static final int PULL_DECAY_TIME = 2000;
    private static final float PULL_DISTANCE_ALPHA_GLOW_FACTOR = 0.8f;
    private static final float PULL_GLOW_BEGIN = 0.0f;
    private static final int PULL_TIME = 167;
    private static final int RECEDE_TIME = 600;
    private static final float SIN = ((float) Math.sin(ANGLE));
    private static final int STATE_ABSORB = 2;
    private static final int STATE_IDLE = 0;
    private static final int STATE_PULL = 1;
    private static final int STATE_PULL_DECAY = 4;
    private static final int STATE_RECEDE = 3;
    private static final String TAG = "EdgeEffect";
    private static final int VELOCITY_GLOW_FACTOR = 6;
    private float mBaseGlowScale;
    private final Rect mBounds = new Rect();
    private float mDisplacement = MAX_ALPHA;
    private float mDuration;
    private float mGlowAlpha;
    private float mGlowAlphaFinish;
    private float mGlowAlphaStart;
    private float mGlowScaleY;
    private float mGlowScaleYFinish;
    private float mGlowScaleYStart;
    private final Interpolator mInterpolator;
    private boolean mIsHwTheme = false;
    private final Paint mPaint = new Paint();
    private float mPullDistance;
    private float mRadius;
    private long mStartTime;
    private int mState = 0;
    private float mTargetDisplacement = MAX_ALPHA;

    public EdgeEffect(Context context) {
        this.mPaint.setAntiAlias(true);
        TypedArray a = context.obtainStyledAttributes(R.styleable.EdgeEffect);
        int themeColor = a.getColor(0, -10066330);
        a.recycle();
        this.mIsHwTheme = HwWidgetFactory.checkIsHwTheme(context, null);
        this.mPaint.setColor((16777215 & themeColor) | 855638016);
        this.mPaint.setStyle(Style.FILL);
        this.mPaint.setXfermode(new PorterDuffXfermode(Mode.SRC_ATOP));
        this.mInterpolator = new DecelerateInterpolator();
    }

    public void setSize(int width, int height) {
        float f = 1.0f;
        float r = (((float) width) * 0.75f) / SIN;
        float h = r - (COS * r);
        float or = (((float) height) * 0.75f) / SIN;
        float oh = or - (COS * or);
        this.mRadius = r;
        if (h > 0.0f) {
            f = Math.min(oh / h, 1.0f);
        }
        this.mBaseGlowScale = f;
        this.mBounds.set(this.mBounds.left, this.mBounds.top, width, (int) Math.min((float) height, h));
    }

    public boolean isFinished() {
        return this.mState == 0;
    }

    public void finish() {
        this.mState = 0;
    }

    public void onPull(float deltaDistance) {
        onPull(deltaDistance, MAX_ALPHA);
    }

    public void onPull(float deltaDistance, float displacement) {
        long now = AnimationUtils.currentAnimationTimeMillis();
        this.mTargetDisplacement = displacement;
        if (this.mState != 4 || ((float) (now - this.mStartTime)) >= this.mDuration) {
            if (this.mState != 1) {
                this.mGlowScaleY = Math.max(0.0f, this.mGlowScaleY);
            }
            this.mState = 1;
            this.mStartTime = now;
            this.mDuration = 167.0f;
            this.mPullDistance += deltaDistance;
            float min = Math.min(MAX_ALPHA, this.mGlowAlpha + (PULL_DISTANCE_ALPHA_GLOW_FACTOR * Math.abs(deltaDistance)));
            this.mGlowAlphaStart = min;
            this.mGlowAlpha = min;
            if (this.mPullDistance == 0.0f) {
                this.mGlowScaleYStart = 0.0f;
                this.mGlowScaleY = 0.0f;
            } else {
                float scale = (float) (Math.max(0.0d, (1.0d - (1.0d / Math.sqrt((double) (Math.abs(this.mPullDistance) * ((float) this.mBounds.height()))))) - 0.3d) / 0.7d);
                this.mGlowScaleYStart = scale;
                this.mGlowScaleY = scale;
            }
            this.mGlowAlphaFinish = this.mGlowAlpha;
            this.mGlowScaleYFinish = this.mGlowScaleY;
        }
    }

    public void onRelease() {
        this.mPullDistance = 0.0f;
        if (this.mState == 1 || this.mState == 4) {
            this.mState = 3;
            this.mGlowAlphaStart = this.mGlowAlpha;
            this.mGlowScaleYStart = this.mGlowScaleY;
            this.mGlowAlphaFinish = 0.0f;
            this.mGlowScaleYFinish = 0.0f;
            this.mStartTime = AnimationUtils.currentAnimationTimeMillis();
            this.mDuration = 600.0f;
        }
    }

    public void onAbsorb(int velocity) {
        this.mState = 2;
        velocity = Math.min(Math.max(100, Math.abs(velocity)), 10000);
        this.mStartTime = AnimationUtils.currentAnimationTimeMillis();
        this.mDuration = (((float) velocity) * 0.02f) + 0.15f;
        this.mGlowAlphaStart = 0.3f;
        this.mGlowScaleYStart = Math.max(this.mGlowScaleY, 0.0f);
        this.mGlowScaleYFinish = Math.min(((((float) ((velocity / 100) * velocity)) * 1.5E-4f) / MAX_GLOW_SCALE) + 0.025f, 1.0f);
        this.mGlowAlphaFinish = Math.max(this.mGlowAlphaStart, Math.min(((float) (velocity * 6)) * 1.0E-5f, MAX_ALPHA));
        this.mTargetDisplacement = MAX_ALPHA;
    }

    public void setColor(int color) {
        this.mPaint.setColor(color);
    }

    public int getColor() {
        return this.mPaint.getColor();
    }

    public boolean draw(Canvas canvas) {
        if (this.mIsHwTheme) {
            return false;
        }
        update();
        int count = canvas.save();
        float centerX = (float) this.mBounds.centerX();
        float centerY = ((float) this.mBounds.height()) - this.mRadius;
        canvas.scale(1.0f, Math.min(this.mGlowScaleY, 1.0f) * this.mBaseGlowScale, centerX, 0.0f);
        float translateX = (((float) this.mBounds.width()) * (Math.max(0.0f, Math.min(this.mDisplacement, 1.0f)) - MAX_ALPHA)) / MAX_GLOW_SCALE;
        canvas.clipRect(this.mBounds);
        canvas.translate(translateX, 0.0f);
        this.mPaint.setAlpha((int) (this.mGlowAlpha * 255.0f));
        canvas.drawCircle(centerX, centerY, this.mRadius, this.mPaint);
        canvas.restoreToCount(count);
        boolean oneLastFrame = false;
        if (this.mState == 3 && this.mGlowScaleY == 0.0f) {
            this.mState = 0;
            oneLastFrame = true;
        }
        if (this.mState != 0) {
            oneLastFrame = true;
        }
        return oneLastFrame;
    }

    public int getMaxHeight() {
        return (int) ((((float) this.mBounds.height()) * MAX_GLOW_SCALE) + MAX_ALPHA);
    }

    private void update() {
        float t = Math.min(((float) (AnimationUtils.currentAnimationTimeMillis() - this.mStartTime)) / this.mDuration, 1.0f);
        float interp = this.mInterpolator.getInterpolation(t);
        this.mGlowAlpha = this.mGlowAlphaStart + ((this.mGlowAlphaFinish - this.mGlowAlphaStart) * interp);
        this.mGlowScaleY = this.mGlowScaleYStart + ((this.mGlowScaleYFinish - this.mGlowScaleYStart) * interp);
        this.mDisplacement = (this.mDisplacement + this.mTargetDisplacement) / MAX_GLOW_SCALE;
        if (t >= 0.999f) {
            switch (this.mState) {
                case 1:
                    this.mState = 4;
                    this.mStartTime = AnimationUtils.currentAnimationTimeMillis();
                    this.mDuration = 2000.0f;
                    this.mGlowAlphaStart = this.mGlowAlpha;
                    this.mGlowScaleYStart = this.mGlowScaleY;
                    this.mGlowAlphaFinish = 0.0f;
                    this.mGlowScaleYFinish = 0.0f;
                    return;
                case 2:
                    this.mState = 3;
                    this.mStartTime = AnimationUtils.currentAnimationTimeMillis();
                    this.mDuration = 600.0f;
                    this.mGlowAlphaStart = this.mGlowAlpha;
                    this.mGlowScaleYStart = this.mGlowScaleY;
                    this.mGlowAlphaFinish = 0.0f;
                    this.mGlowScaleYFinish = 0.0f;
                    return;
                case 3:
                    this.mState = 0;
                    return;
                case 4:
                    this.mState = 3;
                    return;
                default:
                    return;
            }
        }
    }
}
