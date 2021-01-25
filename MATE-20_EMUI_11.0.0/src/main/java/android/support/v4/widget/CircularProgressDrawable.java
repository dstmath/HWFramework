package android.support.v4.widget;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.RestrictTo;
import android.support.v4.util.Preconditions;
import android.support.v4.view.animation.FastOutSlowInInterpolator;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class CircularProgressDrawable extends Drawable implements Animatable {
    private static final int ANIMATION_DURATION = 1332;
    private static final int ARROW_HEIGHT = 5;
    private static final int ARROW_HEIGHT_LARGE = 6;
    private static final int ARROW_WIDTH = 10;
    private static final int ARROW_WIDTH_LARGE = 12;
    private static final float CENTER_RADIUS = 7.5f;
    private static final float CENTER_RADIUS_LARGE = 11.0f;
    private static final int[] COLORS = {-16777216};
    private static final float COLOR_CHANGE_OFFSET = 0.75f;
    public static final int DEFAULT = 1;
    private static final float GROUP_FULL_ROTATION = 216.0f;
    public static final int LARGE = 0;
    private static final Interpolator LINEAR_INTERPOLATOR = new LinearInterpolator();
    private static final Interpolator MATERIAL_INTERPOLATOR = new FastOutSlowInInterpolator();
    private static final float MAX_PROGRESS_ARC = 0.8f;
    private static final float MIN_PROGRESS_ARC = 0.01f;
    private static final float RING_ROTATION = 0.20999998f;
    private static final float SHRINK_OFFSET = 0.5f;
    private static final float STROKE_WIDTH = 2.5f;
    private static final float STROKE_WIDTH_LARGE = 3.0f;
    private Animator mAnimator;
    private boolean mFinishing;
    private Resources mResources;
    private final Ring mRing = new Ring();
    private float mRotation;
    private float mRotationCount;

    @Retention(RetentionPolicy.SOURCE)
    @RestrictTo({RestrictTo.Scope.LIBRARY_GROUP})
    public @interface ProgressDrawableSize {
    }

    public CircularProgressDrawable(@NonNull Context context) {
        this.mResources = ((Context) Preconditions.checkNotNull(context)).getResources();
        this.mRing.setColors(COLORS);
        setStrokeWidth(STROKE_WIDTH);
        setupAnimators();
    }

    private void setSizeParameters(float centerRadius, float strokeWidth, float arrowWidth, float arrowHeight) {
        Ring ring = this.mRing;
        float screenDensity = this.mResources.getDisplayMetrics().density;
        ring.setStrokeWidth(strokeWidth * screenDensity);
        ring.setCenterRadius(centerRadius * screenDensity);
        ring.setColorIndex(0);
        ring.setArrowDimensions(arrowWidth * screenDensity, arrowHeight * screenDensity);
    }

    public void setStyle(int size) {
        if (size == 0) {
            setSizeParameters(CENTER_RADIUS_LARGE, STROKE_WIDTH_LARGE, 12.0f, 6.0f);
        } else {
            setSizeParameters(CENTER_RADIUS, STROKE_WIDTH, 10.0f, 5.0f);
        }
        invalidateSelf();
    }

    public float getStrokeWidth() {
        return this.mRing.getStrokeWidth();
    }

    public void setStrokeWidth(float strokeWidth) {
        this.mRing.setStrokeWidth(strokeWidth);
        invalidateSelf();
    }

    public float getCenterRadius() {
        return this.mRing.getCenterRadius();
    }

    public void setCenterRadius(float centerRadius) {
        this.mRing.setCenterRadius(centerRadius);
        invalidateSelf();
    }

    public void setStrokeCap(@NonNull Paint.Cap strokeCap) {
        this.mRing.setStrokeCap(strokeCap);
        invalidateSelf();
    }

    @NonNull
    public Paint.Cap getStrokeCap() {
        return this.mRing.getStrokeCap();
    }

    public float getArrowWidth() {
        return this.mRing.getArrowWidth();
    }

    public float getArrowHeight() {
        return this.mRing.getArrowHeight();
    }

    public void setArrowDimensions(float width, float height) {
        this.mRing.setArrowDimensions(width, height);
        invalidateSelf();
    }

    public boolean getArrowEnabled() {
        return this.mRing.getShowArrow();
    }

    public void setArrowEnabled(boolean show) {
        this.mRing.setShowArrow(show);
        invalidateSelf();
    }

    public float getArrowScale() {
        return this.mRing.getArrowScale();
    }

    public void setArrowScale(float scale) {
        this.mRing.setArrowScale(scale);
        invalidateSelf();
    }

    public float getStartTrim() {
        return this.mRing.getStartTrim();
    }

    public float getEndTrim() {
        return this.mRing.getEndTrim();
    }

    public void setStartEndTrim(float start, float end) {
        this.mRing.setStartTrim(start);
        this.mRing.setEndTrim(end);
        invalidateSelf();
    }

    public float getProgressRotation() {
        return this.mRing.getRotation();
    }

    public void setProgressRotation(float rotation) {
        this.mRing.setRotation(rotation);
        invalidateSelf();
    }

    public int getBackgroundColor() {
        return this.mRing.getBackgroundColor();
    }

    public void setBackgroundColor(int color) {
        this.mRing.setBackgroundColor(color);
        invalidateSelf();
    }

    @NonNull
    public int[] getColorSchemeColors() {
        return this.mRing.getColors();
    }

    public void setColorSchemeColors(@NonNull int... colors) {
        this.mRing.setColors(colors);
        this.mRing.setColorIndex(0);
        invalidateSelf();
    }

    @Override // android.graphics.drawable.Drawable
    public void draw(Canvas canvas) {
        Rect bounds = getBounds();
        canvas.save();
        canvas.rotate(this.mRotation, bounds.exactCenterX(), bounds.exactCenterY());
        this.mRing.draw(canvas, bounds);
        canvas.restore();
    }

    @Override // android.graphics.drawable.Drawable
    public void setAlpha(int alpha) {
        this.mRing.setAlpha(alpha);
        invalidateSelf();
    }

    @Override // android.graphics.drawable.Drawable
    public int getAlpha() {
        return this.mRing.getAlpha();
    }

    @Override // android.graphics.drawable.Drawable
    public void setColorFilter(ColorFilter colorFilter) {
        this.mRing.setColorFilter(colorFilter);
        invalidateSelf();
    }

    private void setRotation(float rotation) {
        this.mRotation = rotation;
    }

    private float getRotation() {
        return this.mRotation;
    }

    @Override // android.graphics.drawable.Drawable
    public int getOpacity() {
        return -3;
    }

    @Override // android.graphics.drawable.Animatable
    public boolean isRunning() {
        return this.mAnimator.isRunning();
    }

    @Override // android.graphics.drawable.Animatable
    public void start() {
        this.mAnimator.cancel();
        this.mRing.storeOriginals();
        if (this.mRing.getEndTrim() != this.mRing.getStartTrim()) {
            this.mFinishing = true;
            this.mAnimator.setDuration(666);
            this.mAnimator.start();
            return;
        }
        this.mRing.setColorIndex(0);
        this.mRing.resetOriginals();
        this.mAnimator.setDuration(1332);
        this.mAnimator.start();
    }

    @Override // android.graphics.drawable.Animatable
    public void stop() {
        this.mAnimator.cancel();
        setRotation(0.0f);
        this.mRing.setShowArrow(false);
        this.mRing.setColorIndex(0);
        this.mRing.resetOriginals();
        invalidateSelf();
    }

    private int evaluateColorChange(float fraction, int startValue, int endValue) {
        int startA = (startValue >> 24) & 255;
        int startR = (startValue >> 16) & 255;
        int startG = (startValue >> 8) & 255;
        int startB = startValue & 255;
        return ((((int) (((float) (((endValue >> 24) & 255) - startA)) * fraction)) + startA) << 24) | ((((int) (((float) (((endValue >> 16) & 255) - startR)) * fraction)) + startR) << 16) | ((((int) (((float) (((endValue >> 8) & 255) - startG)) * fraction)) + startG) << 8) | (((int) (((float) ((endValue & 255) - startB)) * fraction)) + startB);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateRingColor(float interpolatedTime, Ring ring) {
        if (interpolatedTime > COLOR_CHANGE_OFFSET) {
            ring.setColor(evaluateColorChange((interpolatedTime - COLOR_CHANGE_OFFSET) / 0.25f, ring.getStartingColor(), ring.getNextColor()));
        } else {
            ring.setColor(ring.getStartingColor());
        }
    }

    private void applyFinishTranslation(float interpolatedTime, Ring ring) {
        updateRingColor(interpolatedTime, ring);
        ring.setStartTrim(ring.getStartingStartTrim() + (((ring.getStartingEndTrim() - MIN_PROGRESS_ARC) - ring.getStartingStartTrim()) * interpolatedTime));
        ring.setEndTrim(ring.getStartingEndTrim());
        ring.setRotation(ring.getStartingRotation() + ((((float) (Math.floor((double) (ring.getStartingRotation() / MAX_PROGRESS_ARC)) + 1.0d)) - ring.getStartingRotation()) * interpolatedTime));
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void applyTransformation(float interpolatedTime, Ring ring, boolean lastFrame) {
        float startTrim;
        float startTrim2;
        if (this.mFinishing) {
            applyFinishTranslation(interpolatedTime, ring);
        } else if (interpolatedTime != 1.0f || lastFrame) {
            float startingRotation = ring.getStartingRotation();
            if (interpolatedTime < SHRINK_OFFSET) {
                float scaledTime = interpolatedTime / SHRINK_OFFSET;
                float startTrim3 = ring.getStartingStartTrim();
                startTrim2 = startTrim3;
                startTrim = (0.79f * MATERIAL_INTERPOLATOR.getInterpolation(scaledTime)) + MIN_PROGRESS_ARC + startTrim3;
            } else {
                float scaledTime2 = (interpolatedTime - SHRINK_OFFSET) / SHRINK_OFFSET;
                startTrim = ring.getStartingStartTrim() + 0.79f;
                startTrim2 = startTrim - ((0.79f * (1.0f - MATERIAL_INTERPOLATOR.getInterpolation(scaledTime2))) + MIN_PROGRESS_ARC);
            }
            float groupRotation = GROUP_FULL_ROTATION * (this.mRotationCount + interpolatedTime);
            ring.setStartTrim(startTrim2);
            ring.setEndTrim(startTrim);
            ring.setRotation((RING_ROTATION * interpolatedTime) + startingRotation);
            setRotation(groupRotation);
        }
    }

    private void setupAnimators() {
        final Ring ring = this.mRing;
        ValueAnimator animator = ValueAnimator.ofFloat(0.0f, 1.0f);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            /* class android.support.v4.widget.CircularProgressDrawable.AnonymousClass1 */

            @Override // android.animation.ValueAnimator.AnimatorUpdateListener
            public void onAnimationUpdate(ValueAnimator animation) {
                float interpolatedTime = ((Float) animation.getAnimatedValue()).floatValue();
                CircularProgressDrawable.this.updateRingColor(interpolatedTime, ring);
                CircularProgressDrawable.this.applyTransformation(interpolatedTime, ring, false);
                CircularProgressDrawable.this.invalidateSelf();
            }
        });
        animator.setRepeatCount(-1);
        animator.setRepeatMode(1);
        animator.setInterpolator(LINEAR_INTERPOLATOR);
        animator.addListener(new Animator.AnimatorListener() {
            /* class android.support.v4.widget.CircularProgressDrawable.AnonymousClass2 */

            @Override // android.animation.Animator.AnimatorListener
            public void onAnimationStart(Animator animator) {
                CircularProgressDrawable.this.mRotationCount = 0.0f;
            }

            @Override // android.animation.Animator.AnimatorListener
            public void onAnimationEnd(Animator animator) {
            }

            @Override // android.animation.Animator.AnimatorListener
            public void onAnimationCancel(Animator animation) {
            }

            @Override // android.animation.Animator.AnimatorListener
            public void onAnimationRepeat(Animator animator) {
                CircularProgressDrawable.this.applyTransformation(1.0f, ring, true);
                ring.storeOriginals();
                ring.goToNextColor();
                if (CircularProgressDrawable.this.mFinishing) {
                    CircularProgressDrawable.this.mFinishing = false;
                    animator.cancel();
                    animator.setDuration(1332);
                    animator.start();
                    ring.setShowArrow(false);
                    return;
                }
                CircularProgressDrawable.this.mRotationCount += 1.0f;
            }
        });
        this.mAnimator = animator;
    }

    /* access modifiers changed from: private */
    public static class Ring {
        int mAlpha = 255;
        Path mArrow;
        int mArrowHeight;
        final Paint mArrowPaint = new Paint();
        float mArrowScale = 1.0f;
        int mArrowWidth;
        final Paint mCirclePaint = new Paint();
        int mColorIndex;
        int[] mColors;
        int mCurrentColor;
        float mEndTrim = 0.0f;
        final Paint mPaint = new Paint();
        float mRingCenterRadius;
        float mRotation = 0.0f;
        boolean mShowArrow;
        float mStartTrim = 0.0f;
        float mStartingEndTrim;
        float mStartingRotation;
        float mStartingStartTrim;
        float mStrokeWidth = 5.0f;
        final RectF mTempBounds = new RectF();

        Ring() {
            this.mPaint.setStrokeCap(Paint.Cap.SQUARE);
            this.mPaint.setAntiAlias(true);
            this.mPaint.setStyle(Paint.Style.STROKE);
            this.mArrowPaint.setStyle(Paint.Style.FILL);
            this.mArrowPaint.setAntiAlias(true);
            this.mCirclePaint.setColor(0);
        }

        /* access modifiers changed from: package-private */
        public void setArrowDimensions(float width, float height) {
            this.mArrowWidth = (int) width;
            this.mArrowHeight = (int) height;
        }

        /* access modifiers changed from: package-private */
        public void setStrokeCap(Paint.Cap strokeCap) {
            this.mPaint.setStrokeCap(strokeCap);
        }

        /* access modifiers changed from: package-private */
        public Paint.Cap getStrokeCap() {
            return this.mPaint.getStrokeCap();
        }

        /* access modifiers changed from: package-private */
        public float getArrowWidth() {
            return (float) this.mArrowWidth;
        }

        /* access modifiers changed from: package-private */
        public float getArrowHeight() {
            return (float) this.mArrowHeight;
        }

        /* access modifiers changed from: package-private */
        public void draw(Canvas c, Rect bounds) {
            RectF arcBounds = this.mTempBounds;
            float arcRadius = this.mRingCenterRadius + (this.mStrokeWidth / 2.0f);
            if (this.mRingCenterRadius <= 0.0f) {
                arcRadius = (((float) Math.min(bounds.width(), bounds.height())) / 2.0f) - Math.max((((float) this.mArrowWidth) * this.mArrowScale) / 2.0f, this.mStrokeWidth / 2.0f);
            }
            arcBounds.set(((float) bounds.centerX()) - arcRadius, ((float) bounds.centerY()) - arcRadius, ((float) bounds.centerX()) + arcRadius, ((float) bounds.centerY()) + arcRadius);
            float startAngle = (this.mStartTrim + this.mRotation) * 360.0f;
            float sweepAngle = ((this.mEndTrim + this.mRotation) * 360.0f) - startAngle;
            this.mPaint.setColor(this.mCurrentColor);
            this.mPaint.setAlpha(this.mAlpha);
            float inset = this.mStrokeWidth / 2.0f;
            arcBounds.inset(inset, inset);
            c.drawCircle(arcBounds.centerX(), arcBounds.centerY(), arcBounds.width() / 2.0f, this.mCirclePaint);
            arcBounds.inset(-inset, -inset);
            c.drawArc(arcBounds, startAngle, sweepAngle, false, this.mPaint);
            drawTriangle(c, startAngle, sweepAngle, arcBounds);
        }

        /* access modifiers changed from: package-private */
        public void drawTriangle(Canvas c, float startAngle, float sweepAngle, RectF bounds) {
            if (this.mShowArrow) {
                if (this.mArrow == null) {
                    this.mArrow = new Path();
                    this.mArrow.setFillType(Path.FillType.EVEN_ODD);
                } else {
                    this.mArrow.reset();
                }
                this.mArrow.moveTo(0.0f, 0.0f);
                this.mArrow.lineTo(((float) this.mArrowWidth) * this.mArrowScale, 0.0f);
                this.mArrow.lineTo((((float) this.mArrowWidth) * this.mArrowScale) / 2.0f, ((float) this.mArrowHeight) * this.mArrowScale);
                this.mArrow.offset((bounds.centerX() + (Math.min(bounds.width(), bounds.height()) / 2.0f)) - ((((float) this.mArrowWidth) * this.mArrowScale) / 2.0f), bounds.centerY() + (this.mStrokeWidth / 2.0f));
                this.mArrow.close();
                this.mArrowPaint.setColor(this.mCurrentColor);
                this.mArrowPaint.setAlpha(this.mAlpha);
                c.save();
                c.rotate(startAngle + sweepAngle, bounds.centerX(), bounds.centerY());
                c.drawPath(this.mArrow, this.mArrowPaint);
                c.restore();
            }
        }

        /* access modifiers changed from: package-private */
        public void setColors(@NonNull int[] colors) {
            this.mColors = colors;
            setColorIndex(0);
        }

        /* access modifiers changed from: package-private */
        public int[] getColors() {
            return this.mColors;
        }

        /* access modifiers changed from: package-private */
        public void setColor(int color) {
            this.mCurrentColor = color;
        }

        /* access modifiers changed from: package-private */
        public void setBackgroundColor(int color) {
            this.mCirclePaint.setColor(color);
        }

        /* access modifiers changed from: package-private */
        public int getBackgroundColor() {
            return this.mCirclePaint.getColor();
        }

        /* access modifiers changed from: package-private */
        public void setColorIndex(int index) {
            this.mColorIndex = index;
            this.mCurrentColor = this.mColors[this.mColorIndex];
        }

        /* access modifiers changed from: package-private */
        public int getNextColor() {
            return this.mColors[getNextColorIndex()];
        }

        /* access modifiers changed from: package-private */
        public int getNextColorIndex() {
            return (this.mColorIndex + 1) % this.mColors.length;
        }

        /* access modifiers changed from: package-private */
        public void goToNextColor() {
            setColorIndex(getNextColorIndex());
        }

        /* access modifiers changed from: package-private */
        public void setColorFilter(ColorFilter filter) {
            this.mPaint.setColorFilter(filter);
        }

        /* access modifiers changed from: package-private */
        public void setAlpha(int alpha) {
            this.mAlpha = alpha;
        }

        /* access modifiers changed from: package-private */
        public int getAlpha() {
            return this.mAlpha;
        }

        /* access modifiers changed from: package-private */
        public void setStrokeWidth(float strokeWidth) {
            this.mStrokeWidth = strokeWidth;
            this.mPaint.setStrokeWidth(strokeWidth);
        }

        /* access modifiers changed from: package-private */
        public float getStrokeWidth() {
            return this.mStrokeWidth;
        }

        /* access modifiers changed from: package-private */
        public void setStartTrim(float startTrim) {
            this.mStartTrim = startTrim;
        }

        /* access modifiers changed from: package-private */
        public float getStartTrim() {
            return this.mStartTrim;
        }

        /* access modifiers changed from: package-private */
        public float getStartingStartTrim() {
            return this.mStartingStartTrim;
        }

        /* access modifiers changed from: package-private */
        public float getStartingEndTrim() {
            return this.mStartingEndTrim;
        }

        /* access modifiers changed from: package-private */
        public int getStartingColor() {
            return this.mColors[this.mColorIndex];
        }

        /* access modifiers changed from: package-private */
        public void setEndTrim(float endTrim) {
            this.mEndTrim = endTrim;
        }

        /* access modifiers changed from: package-private */
        public float getEndTrim() {
            return this.mEndTrim;
        }

        /* access modifiers changed from: package-private */
        public void setRotation(float rotation) {
            this.mRotation = rotation;
        }

        /* access modifiers changed from: package-private */
        public float getRotation() {
            return this.mRotation;
        }

        /* access modifiers changed from: package-private */
        public void setCenterRadius(float centerRadius) {
            this.mRingCenterRadius = centerRadius;
        }

        /* access modifiers changed from: package-private */
        public float getCenterRadius() {
            return this.mRingCenterRadius;
        }

        /* access modifiers changed from: package-private */
        public void setShowArrow(boolean show) {
            if (this.mShowArrow != show) {
                this.mShowArrow = show;
            }
        }

        /* access modifiers changed from: package-private */
        public boolean getShowArrow() {
            return this.mShowArrow;
        }

        /* access modifiers changed from: package-private */
        public void setArrowScale(float scale) {
            if (scale != this.mArrowScale) {
                this.mArrowScale = scale;
            }
        }

        /* access modifiers changed from: package-private */
        public float getArrowScale() {
            return this.mArrowScale;
        }

        /* access modifiers changed from: package-private */
        public float getStartingRotation() {
            return this.mStartingRotation;
        }

        /* access modifiers changed from: package-private */
        public void storeOriginals() {
            this.mStartingStartTrim = this.mStartTrim;
            this.mStartingEndTrim = this.mEndTrim;
            this.mStartingRotation = this.mRotation;
        }

        /* access modifiers changed from: package-private */
        public void resetOriginals() {
            this.mStartingStartTrim = 0.0f;
            this.mStartingEndTrim = 0.0f;
            this.mStartingRotation = 0.0f;
            setStartTrim(0.0f);
            setEndTrim(0.0f);
            setRotation(0.0f);
        }
    }
}
