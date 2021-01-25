package com.android.server.multiwin.view;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.ImageView;
import com.android.server.multiwin.animation.interpolator.FastOutSlowInInterpolator;
import com.android.server.multiwin.animation.interpolator.SharpCurveInterpolator;
import com.huawei.android.app.HwActivityManager;

public class HwMultiWinClipImageView extends ImageView {
    private static final float FINAL_PUSH_SCALE_FACTOR_NO_SPLIT_BAR = 0.5f;
    private static final long FULL_SCREEN_SHOT_ANIM_DURATION = 350;
    private static final float FULL_SCREEN_SHOT_SCALE_FACTOR_1 = 0.7f;
    private static final float FULL_SCREEN_SHOT_SCALE_FACTOR_2 = 0.95f;
    private static final float ICON_BASE_SCALE_RATIO = 1.0f;
    protected static final float NUM_TWO_CONST = 2.0f;
    private static final long PRE_PUSH_DURATION = 250;
    private static final float PRE_PUSH_SCALE_FACTOR_1 = 0.5f;
    private static final float PRE_PUSH_SCALE_FACTOR_2 = 0.95f;
    private static final long ROUND_CORNER_DISMISS_DURATION = 150;
    private static final long SWAP_ANIM_DURATION = 250;
    private static final long SWAP_BLUR_COVER_ALPHA_ANIM_DURATION = 150;
    private static final String TAG = "HwMultiWinClipImageView";
    private View mAppFullView;
    protected Paint mBorderPaint;
    private ValueAnimator mFinalPushAnimator;
    private ValueAnimator mFullScreenShotDismissAnimator;
    private boolean mHasBeenResizedWithoutNavBar = false;
    private boolean mHasRemovedSelf = true;
    private Drawable mIcon;
    private ValueAnimator mIconAnimator;
    private int mInSwapMode;
    private boolean mIsLandScape = false;
    protected boolean mIsToDrawBorder = true;
    private int mLastSwapAcceptSplitMode = 0;
    private int mOriginalInSwapMode;
    private ValueAnimator mPrePushAnimator;
    private Path mRoundCornerClipPath = new Path();
    private RectF mRoundCornerClipRect = new RectF();
    protected float mRoundCornerRadius;
    private ValueAnimator mSwapAnimator;
    private View mSwapBlurCover;
    private ValueAnimator mSwapBlurCoverAlphaAnimator;

    public HwMultiWinClipImageView(Context context) {
        super(context);
        init(context);
    }

    public HwMultiWinClipImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public HwMultiWinClipImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        float f;
        if (HwActivityManager.IS_PHONE) {
            f = 0.0f;
        } else {
            f = (float) context.getResources().getDimensionPixelSize(34472572);
        }
        this.mRoundCornerRadius = f;
        int borderWidth = context.getResources().getDimensionPixelSize(34472568);
        if (borderWidth > 0) {
            this.mBorderPaint = new Paint();
            this.mBorderPaint.setColor(context.getResources().getColor(33882975));
            this.mBorderPaint.setStyle(Paint.Style.STROKE);
            this.mBorderPaint.setStrokeWidth(((float) borderWidth) * 2.0f);
            this.mBorderPaint.setStrokeCap(Paint.Cap.ROUND);
        }
    }

    public void disableBorder() {
        this.mIsToDrawBorder = false;
    }

    public void setInSwapMode(int inSwapMode) {
        this.mInSwapMode = inSwapMode;
        this.mOriginalInSwapMode = inSwapMode;
    }

    public int getOriginalInSwapMode() {
        return this.mOriginalInSwapMode;
    }

    private ViewGroup getParentViewGroup() {
        ViewParent parent = getParent();
        if (parent instanceof ViewGroup) {
            return (ViewGroup) parent;
        }
        return null;
    }

    private int getSwapToMiddleTranslation() {
        int parentMid;
        int mid;
        ViewGroup viewGroup = getParentViewGroup();
        if (viewGroup == null) {
            return 0;
        }
        if (isLandScape()) {
            parentMid = (int) (((float) (viewGroup.getLeft() + viewGroup.getRight())) / 2.0f);
        } else {
            parentMid = (int) (((float) (viewGroup.getTop() + viewGroup.getBottom())) / 2.0f);
        }
        if (isLandScape()) {
            mid = (int) (((float) (getLeft() + getRight())) / 2.0f);
        } else {
            mid = (int) (((float) (getTop() + getBottom())) / 2.0f);
        }
        return parentMid - mid;
    }

    private boolean isOriginalInLeftOrTopSwapMode() {
        int i = this.mOriginalInSwapMode;
        return i == 1 || i == 3;
    }

    private float getSwapToScale(int currentSwapAcceptSplitMode) {
        float swapToScale = isLandScape() ? getScaleX() : getScaleY();
        ViewGroup viewGroup = getParentViewGroup();
        if (viewGroup == null) {
            return swapToScale;
        }
        if (currentSwapAcceptSplitMode != 5) {
            return 1.0f;
        }
        return ((float) (isLandScape() ? viewGroup.getWidth() : viewGroup.getHeight())) / ((float) (isLandScape() ? getWidth() : getHeight()));
    }

    private float getSwapToTranslation(float fromTranslation, int currentSwapAcceptSplitMode, float splitSwapSize) {
        float toTranslation;
        boolean isSwapToMiddle = false;
        int i = 3;
        boolean isSwapToRightOrBottom = currentSwapAcceptSplitMode == 1 || currentSwapAcceptSplitMode == 3;
        if (currentSwapAcceptSplitMode == 5) {
            isSwapToMiddle = true;
        }
        if (currentSwapAcceptSplitMode != 5) {
            int i2 = this.mInSwapMode;
            if (i2 != 5 && currentSwapAcceptSplitMode != i2) {
                toTranslation = fromTranslation;
            } else if (isOriginalInLeftOrTopSwapMode() && isSwapToRightOrBottom) {
                toTranslation = splitSwapSize;
            } else if (isOriginalInLeftOrTopSwapMode() && !isSwapToRightOrBottom) {
                toTranslation = 0.0f;
            } else if (isOriginalInLeftOrTopSwapMode() || !isSwapToRightOrBottom) {
                toTranslation = -splitSwapSize;
            } else {
                toTranslation = 0.0f;
            }
        } else {
            toTranslation = (float) getSwapToMiddleTranslation();
        }
        if (isSwapToRightOrBottom) {
            this.mInSwapMode = isLandScape() ? 2 : 4;
        } else {
            if (isLandScape()) {
                i = 1;
            }
            this.mInSwapMode = i;
        }
        if (isSwapToMiddle) {
            this.mInSwapMode = 5;
        }
        return toTranslation;
    }

    public void setSwapBlurCover(View swapBlurCover) {
        this.mSwapBlurCover = swapBlurCover;
    }

    public View getSwapBlurCover() {
        return this.mSwapBlurCover;
    }

    @SuppressLint({"NewApi"})
    public void playSwapAnimation(int currentSwapAcceptSplitMode, float splitSwapSize, AnimatorListenerAdapter swapAnimationListenerAdapter) {
        if (this.mLastSwapAcceptSplitMode != currentSwapAcceptSplitMode) {
            final Animator swapAnimator = createSwapAnimation(currentSwapAcceptSplitMode, splitSwapSize, swapAnimationListenerAdapter);
            final Animator swapBlurCoverAlphaAnimator = createSwapBlurCoverAlphaAnimation(currentSwapAcceptSplitMode);
            if (currentSwapAcceptSplitMode != 5) {
                swapAnimator.addListener(new AnimatorListenerAdapter() {
                    /* class com.android.server.multiwin.view.HwMultiWinClipImageView.AnonymousClass1 */
                    private boolean mIsCanceled = false;

                    @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
                    public void onAnimationCancel(Animator animation) {
                        this.mIsCanceled = true;
                    }

                    @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
                    public void onAnimationEnd(Animator animation) {
                        Animator animator = swapBlurCoverAlphaAnimator;
                        if (animator != null && !this.mIsCanceled) {
                            animator.start();
                        }
                    }
                });
                swapAnimator.start();
            } else {
                swapBlurCoverAlphaAnimator.addListener(new AnimatorListenerAdapter() {
                    /* class com.android.server.multiwin.view.HwMultiWinClipImageView.AnonymousClass2 */
                    private boolean mIsCanceled = false;

                    @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
                    public void onAnimationCancel(Animator animation) {
                        this.mIsCanceled = true;
                    }

                    @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
                    public void onAnimationEnd(Animator animation) {
                        Animator animator = swapAnimator;
                        if (animator != null && !this.mIsCanceled) {
                            animator.start();
                        }
                    }
                });
                swapBlurCoverAlphaAnimator.start();
                playIconShowAnimation();
            }
            this.mLastSwapAcceptSplitMode = currentSwapAcceptSplitMode;
        }
    }

    @SuppressLint({"NewApi"})
    private Animator createSwapAnimation(int currentSwapAcceptSplitMode, float splitSwapSize, AnimatorListenerAdapter swapAnimationListenerAdapter) {
        ValueAnimator valueAnimator = this.mSwapAnimator;
        if (valueAnimator != null && valueAnimator.isRunning()) {
            this.mSwapAnimator.cancel();
        }
        float fromTranslation = isLandScape() ? getTranslationX() : getTranslationY();
        float toTranslation = getSwapToTranslation(fromTranslation, currentSwapAcceptSplitMode, splitSwapSize);
        float fromScale = isLandScape() ? getScaleX() : getScaleY();
        float toScale = getSwapToScale(currentSwapAcceptSplitMode);
        Log.d(TAG, "playSwapAnimation: mInSwapMode = " + this.mInSwapMode + ", currentSwapAcceptSplitMode = " + currentSwapAcceptSplitMode + ", fromTranslation = " + fromTranslation + ", toTranslation = " + toTranslation + ", fromScale = " + fromScale + ", toScale = " + toScale);
        this.mSwapAnimator = ValueAnimator.ofPropertyValuesHolder(PropertyValuesHolder.ofFloat("translation", fromTranslation, toTranslation), PropertyValuesHolder.ofFloat("scale", fromScale, toScale));
        this.mSwapAnimator.setDuration(250L);
        this.mSwapAnimator.setInterpolator(new FastOutSlowInInterpolator());
        this.mSwapAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            /* class com.android.server.multiwin.view.HwMultiWinClipImageView.AnonymousClass3 */

            @Override // android.animation.ValueAnimator.AnimatorUpdateListener
            public void onAnimationUpdate(ValueAnimator animation) {
                HwMultiWinClipImageView.this.updateSwapTranslation(animation.getAnimatedValue("translation"));
                HwMultiWinClipImageView.this.updateSwapScale(animation.getAnimatedValue("scale"));
                if (HwMultiWinClipImageView.this.mIcon != null && ((float) HwMultiWinClipImageView.this.mIcon.getAlpha()) > 0.0f) {
                    HwMultiWinClipImageView.this.invalidate();
                }
            }
        });
        this.mSwapAnimator.addListener(swapAnimationListenerAdapter);
        return this.mSwapAnimator;
    }

    @SuppressLint({"NewApi"})
    private Animator createSwapBlurCoverAlphaAnimation(int currentSwapAcceptSplitMode) {
        View view = this.mSwapBlurCover;
        float toBlurCoverAlpha = 0.0f;
        float fromBlurCoverAlpha = view != null ? view.getAlpha() : 0.0f;
        if (currentSwapAcceptSplitMode != 5) {
            toBlurCoverAlpha = 1.0f;
        }
        ValueAnimator valueAnimator = this.mSwapBlurCoverAlphaAnimator;
        if (valueAnimator != null && valueAnimator.isRunning()) {
            this.mSwapBlurCoverAlphaAnimator.cancel();
        }
        this.mSwapBlurCoverAlphaAnimator = ValueAnimator.ofPropertyValuesHolder(PropertyValuesHolder.ofFloat("blurCoverAlpha", fromBlurCoverAlpha, toBlurCoverAlpha));
        this.mSwapBlurCoverAlphaAnimator.setDuration(150L);
        this.mSwapBlurCoverAlphaAnimator.setInterpolator(new SharpCurveInterpolator());
        this.mSwapBlurCoverAlphaAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            /* class com.android.server.multiwin.view.HwMultiWinClipImageView.AnonymousClass4 */

            @Override // android.animation.ValueAnimator.AnimatorUpdateListener
            public void onAnimationUpdate(ValueAnimator animation) {
                HwMultiWinClipImageView.this.updateBlurCoverAlpha(animation.getAnimatedValue("blurCoverAlpha"));
            }
        });
        return this.mSwapBlurCoverAlphaAnimator;
    }

    private boolean isNeedToSyncSwapBlurCover() {
        View view = this.mSwapBlurCover;
        return view != null && view.getVisibility() == 0;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateBlurCoverAlpha(Object blurCoverAlphaObject) {
        if (this.mSwapBlurCover != null && (blurCoverAlphaObject instanceof Float)) {
            this.mSwapBlurCover.setAlpha(((Float) blurCoverAlphaObject).floatValue());
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateSwapScale(Object scaleObj) {
        if (scaleObj instanceof Float) {
            float scale = ((Float) scaleObj).floatValue();
            if (isLandScape()) {
                setScaleX(scale);
            } else {
                setScaleY(scale);
            }
            if (isNeedToSyncSwapBlurCover()) {
                if (isLandScape()) {
                    this.mSwapBlurCover.setScaleX(scale);
                } else {
                    this.mSwapBlurCover.setScaleY(scale);
                }
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateSwapTranslation(Object translationObj) {
        if (translationObj instanceof Float) {
            float translation = ((Float) translationObj).floatValue();
            if (isLandScape()) {
                setTranslationX(translation);
            } else {
                setTranslationY(translation);
            }
            if (isNeedToSyncSwapBlurCover()) {
                if (isLandScape()) {
                    this.mSwapBlurCover.setTranslationX(translation);
                } else {
                    this.mSwapBlurCover.setTranslationY(translation);
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    public float getPrePushWidth() {
        return getPrePushWidthByScaleFactor(getPushScaleFactorWithSplitBar(0.5f));
    }

    public float getPrePushWidthByScaleFactor(float scaleFactor) {
        if (isLandScape()) {
            return (((float) getWidth()) * (1.0f - scaleFactor)) / 2.0f;
        }
        return (((float) getHeight()) * (1.0f - scaleFactor)) / 2.0f;
    }

    public void playPrePushAnimation(int currentPushAcceptSplitMode) {
        Log.d(TAG, "play pre push animation now: currentPushAcceptSplitMode = " + currentPushAcceptSplitMode);
        float prePushScaleFactor = getPushScaleFactorWithSplitBar(0.5f);
        float prePushWidth = getPrePushWidthByScaleFactor(prePushScaleFactor);
        float mainScaleFactor = currentPushAcceptSplitMode != 5 ? prePushScaleFactor : 0.7f;
        float toScaleX = isLandScape() ? mainScaleFactor : 0.95f;
        float toScaleY = isLandScape() ? 0.95f : mainScaleFactor;
        if (currentPushAcceptSplitMode == 1 || currentPushAcceptSplitMode == 3) {
            playPrePushAnimationInternal(prePushWidth, 250, toScaleX, toScaleY);
        } else if (currentPushAcceptSplitMode == 2 || currentPushAcceptSplitMode == 4) {
            playPrePushAnimationInternal(-prePushWidth, 250, toScaleX, toScaleY);
        } else if (currentPushAcceptSplitMode == 5) {
            playPrePushAnimationInternal(0.0f, 250, toScaleX, toScaleY);
        }
    }

    public float getFullScreenShotScaleFactor2() {
        return 0.95f;
    }

    public void playFinalPushAnimation(int currentPushAcceptSplitMode) {
        Log.d(TAG, "play final push animation now: currentPushAcceptSplitMode = " + currentPushAcceptSplitMode);
        float finalScaleFactor = getPushScaleFactorWithSplitBar(0.5f);
        float toScaleX = isLandScape() ? finalScaleFactor : 1.0f;
        float toScaleY = isLandScape() ? 1.0f : finalScaleFactor;
        float finalPushWidth = getFinalPushTranslation(finalScaleFactor);
        if (currentPushAcceptSplitMode == 1 || currentPushAcceptSplitMode == 3) {
            playFinalPushAnimationInternal(finalPushWidth, toScaleX, toScaleY, 250);
        } else if (currentPushAcceptSplitMode == 2 || currentPushAcceptSplitMode == 4) {
            playFinalPushAnimationInternal(-finalPushWidth, toScaleX, toScaleY, 250);
        } else if (currentPushAcceptSplitMode == 5) {
            playFinalPushAnimationInternal(0.0f, 1.0f, 1.0f, 250);
        }
    }

    private float getPushScaleFactorWithSplitBar(float scaleFactorWithNoSplitBar) {
        int totalLength = isLandScape() ? getWidth() : getHeight();
        if (((float) totalLength) > 0.0f) {
            return ((((float) totalLength) * scaleFactorWithNoSplitBar) - (((float) getContext().getResources().getDimensionPixelSize(34472582)) / 2.0f)) / ((float) totalLength);
        }
        Log.w(TAG, "getPushScaleFactorWithSplitBar failed, cause totalLength is less than 0!");
        return 1.0f;
    }

    public float getFinalPushTranslation(float finalScaleFactor) {
        if (isLandScape()) {
            return (((float) getWidth()) * (1.0f - finalScaleFactor)) / 2.0f;
        }
        return (((float) getHeight()) * (1.0f - finalScaleFactor)) / 2.0f;
    }

    public void setIsLandScape(boolean isLandScape) {
        this.mIsLandScape = isLandScape;
    }

    /* access modifiers changed from: protected */
    public boolean isLandScape() {
        return this.mIsLandScape;
    }

    @SuppressLint({"NewApi"})
    public void playFullScreenShotDismissAnimation() {
        if (this.mFullScreenShotDismissAnimator == null) {
            this.mFullScreenShotDismissAnimator = ValueAnimator.ofPropertyValuesHolder(PropertyValuesHolder.ofFloat("alpha", 1.0f, 0.0f));
            this.mFullScreenShotDismissAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                /* class com.android.server.multiwin.view.HwMultiWinClipImageView.AnonymousClass5 */

                @Override // android.animation.ValueAnimator.AnimatorUpdateListener
                public void onAnimationUpdate(ValueAnimator animation) {
                    Object alphaObj = animation.getAnimatedValue("alpha");
                    float alpha = 1.0f;
                    if (alphaObj instanceof Float) {
                        alpha = ((Float) alphaObj).floatValue();
                    }
                    HwMultiWinClipImageView.this.setAlpha(alpha);
                }
            });
            this.mFullScreenShotDismissAnimator.addListener(new AnimatorListenerAdapter() {
                /* class com.android.server.multiwin.view.HwMultiWinClipImageView.AnonymousClass6 */

                @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
                public void onAnimationEnd(Animator animation) {
                    HwMultiWinClipImageView.this.removeSelf();
                }
            });
            this.mFullScreenShotDismissAnimator.setDuration(350L);
            this.mFullScreenShotDismissAnimator.setInterpolator(new FastOutSlowInInterpolator());
            this.mFullScreenShotDismissAnimator.start();
        }
    }

    public void setIcon(Drawable icon) {
        this.mIcon = icon;
        this.mIcon.setAlpha(0);
    }

    public void setAppFullView(View appFullView) {
        this.mAppFullView = appFullView;
    }

    @SuppressLint({"NewApi"})
    public void playRoundCornerDismissAnimation() {
        ValueAnimator animator = ValueAnimator.ofPropertyValuesHolder(PropertyValuesHolder.ofFloat("roundCornerRadius", this.mRoundCornerRadius, 0.0f));
        animator.setDuration(150L);
        animator.setInterpolator(new FastOutSlowInInterpolator());
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            /* class com.android.server.multiwin.view.HwMultiWinClipImageView.AnonymousClass7 */

            @Override // android.animation.ValueAnimator.AnimatorUpdateListener
            public void onAnimationUpdate(ValueAnimator animation) {
                Object radiusObj = animation.getAnimatedValue("roundCornerRadius");
                if (radiusObj instanceof Float) {
                    HwMultiWinClipImageView.this.mRoundCornerRadius = ((Float) radiusObj).floatValue();
                    HwMultiWinClipImageView.this.invalidate();
                }
            }
        });
        animator.start();
    }

    @SuppressLint({"NewApi"})
    public void playIconShowAnimation() {
        Drawable drawable;
        ValueAnimator valueAnimator = this.mIconAnimator;
        if ((valueAnimator == null || !valueAnimator.isRunning()) && (drawable = this.mIcon) != null) {
            this.mIconAnimator = ValueAnimator.ofPropertyValuesHolder(PropertyValuesHolder.ofFloat("iconAlpha", ((float) drawable.getAlpha()) / 255.0f, 1.0f));
            this.mIconAnimator.setDuration(350L);
            this.mIconAnimator.setInterpolator(new SharpCurveInterpolator());
            this.mIconAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                /* class com.android.server.multiwin.view.HwMultiWinClipImageView.AnonymousClass8 */

                @Override // android.animation.ValueAnimator.AnimatorUpdateListener
                public void onAnimationUpdate(ValueAnimator animation) {
                    if (HwMultiWinClipImageView.this.mIcon != null) {
                        Object alphaObj = animation.getAnimatedValue("iconAlpha");
                        float alpha = 1.0f;
                        if (alphaObj instanceof Float) {
                            alpha = ((Float) alphaObj).floatValue();
                        }
                        HwMultiWinClipImageView.this.mIcon.setAlpha((int) (255.0f * alpha));
                        HwMultiWinClipImageView.this.invalidate();
                    }
                }
            });
            this.mIconAnimator.start();
        }
    }

    private void clipForRoundCorner(Canvas canvas) {
        this.mRoundCornerClipRect.set(0.0f, 0.0f, (float) getWidth(), (float) getHeight());
        float scaleRatio = getScaleY() / getScaleX();
        this.mRoundCornerClipPath.reset();
        Path path = this.mRoundCornerClipPath;
        RectF rectF = this.mRoundCornerClipRect;
        float f = this.mRoundCornerRadius;
        path.addRoundRect(rectF, f * scaleRatio, f, Path.Direction.CW);
        canvas.clipPath(this.mRoundCornerClipPath);
    }

    /* access modifiers changed from: protected */
    public void drawOutlineBorder(Canvas canvas) {
        Paint paint = this.mBorderPaint;
        if (paint != null && this.mIsToDrawBorder) {
            canvas.drawPath(this.mRoundCornerClipPath, paint);
        }
    }

    /* access modifiers changed from: protected */
    @Override // android.widget.ImageView, android.view.View
    public void onDraw(Canvas canvas) {
        canvas.save();
        clipForRoundCorner(canvas);
        super.onDraw(canvas);
        if (!(this instanceof HwMultiWinPushPendingDropView) && !(this instanceof HwMultiWinNotchPendingDropView)) {
            drawOutlineBorder(canvas);
        }
        Drawable drawable = this.mIcon;
        if (drawable != null && drawable.getAlpha() > 0) {
            canvas.save();
            float scaleX = 1.0f / getScaleX();
            float scaleY = 1.0f / getScaleY();
            canvas.translate((((float) getWidth()) - (((float) this.mIcon.getBounds().width()) * scaleX)) / 2.0f, (((float) getHeight()) - (((float) this.mIcon.getBounds().height()) * scaleY)) / 2.0f);
            canvas.scale(scaleX, scaleY);
            this.mIcon.draw(canvas);
            canvas.restore();
        }
        canvas.restore();
    }

    @SuppressLint({"NewApi"})
    public void playPrePushAnimationInternal(float toTranslation, long duration, float toScaleX, float toScaleY) {
        ValueAnimator valueAnimator = this.mPrePushAnimator;
        if (valueAnimator != null && valueAnimator.isRunning()) {
            this.mPrePushAnimator.cancel();
        }
        float fromTranslation = isLandScape() ? getTranslationX() : getTranslationY();
        Log.d(TAG, "playPushAnimation: , fromTranslation = " + fromTranslation + ", toTranslation = " + toTranslation);
        this.mPrePushAnimator = ValueAnimator.ofPropertyValuesHolder(PropertyValuesHolder.ofFloat("translation", fromTranslation, toTranslation), PropertyValuesHolder.ofFloat("scaleX", getScaleX(), toScaleX), PropertyValuesHolder.ofFloat("scaleY", getScaleY(), toScaleY));
        this.mPrePushAnimator.setDuration(duration);
        this.mPrePushAnimator.setInterpolator(new FastOutSlowInInterpolator());
        this.mPrePushAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            /* class com.android.server.multiwin.view.HwMultiWinClipImageView.AnonymousClass9 */

            @Override // android.animation.ValueAnimator.AnimatorUpdateListener
            public void onAnimationUpdate(ValueAnimator animation) {
                HwMultiWinClipImageView.this.updatePushTranslation(animation.getAnimatedValue("translation"));
                HwMultiWinClipImageView.this.updatePushScaleX(animation.getAnimatedValue("scaleX"));
                HwMultiWinClipImageView.this.updatePushScaleY(animation.getAnimatedValue("scaleY"));
                HwMultiWinClipImageView.this.invalidate();
            }
        });
        this.mPrePushAnimator.start();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updatePushScaleX(Object scaleObjX) {
        if (scaleObjX instanceof Float) {
            float scaleX = ((Float) scaleObjX).floatValue();
            setScaleX(scaleX);
            if (isNeedToSyncAppFullView()) {
                this.mAppFullView.setScaleX(scaleX);
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updatePushScaleY(Object scaleObjY) {
        if (scaleObjY instanceof Float) {
            float scaleY = ((Float) scaleObjY).floatValue();
            setScaleY(scaleY);
            if (isNeedToSyncAppFullView()) {
                this.mAppFullView.setScaleY(scaleY);
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updatePushTranslation(Object translationObj) {
        if (translationObj instanceof Float) {
            float translation = ((Float) translationObj).floatValue();
            if (isLandScape()) {
                setTranslationX(translation);
            } else {
                setTranslationY(translation);
            }
            if (isNeedToSyncAppFullView()) {
                if (isLandScape()) {
                    this.mAppFullView.setTranslationX(translation);
                } else {
                    this.mAppFullView.setTranslationY(translation);
                }
            }
        }
    }

    @SuppressLint({"NewApi"})
    public void playFinalPushAnimationInternal(float toTranslation, float toScaleX, float toScaleY, long duration) {
        ValueAnimator valueAnimator = this.mFinalPushAnimator;
        if (valueAnimator != null && valueAnimator.isRunning()) {
            this.mFinalPushAnimator.cancel();
        }
        float fromTranslation = isLandScape() ? getTranslationX() : getTranslationY();
        Log.d(TAG, "playFinalPushAnimationInternal: , fromTranslation = " + fromTranslation + ", toTranslation = " + toTranslation);
        this.mFinalPushAnimator = ValueAnimator.ofPropertyValuesHolder(PropertyValuesHolder.ofFloat("translation", fromTranslation, toTranslation), PropertyValuesHolder.ofFloat("scaleX", getScaleX(), toScaleX), PropertyValuesHolder.ofFloat("scaleY", getScaleY(), toScaleY));
        this.mFinalPushAnimator.setDuration(duration);
        this.mFinalPushAnimator.setInterpolator(new FastOutSlowInInterpolator());
        this.mFinalPushAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            /* class com.android.server.multiwin.view.HwMultiWinClipImageView.AnonymousClass10 */

            @Override // android.animation.ValueAnimator.AnimatorUpdateListener
            public void onAnimationUpdate(ValueAnimator animation) {
                HwMultiWinClipImageView.this.updatePushTranslation(animation.getAnimatedValue("translation"));
                HwMultiWinClipImageView.this.updatePushScaleX(animation.getAnimatedValue("scaleX"));
                HwMultiWinClipImageView.this.updatePushScaleY(animation.getAnimatedValue("scaleY"));
                HwMultiWinClipImageView.this.invalidate();
            }
        });
        this.mFinalPushAnimator.start();
    }

    public boolean isHasBeenResizedWithoutNavBar() {
        return this.mHasBeenResizedWithoutNavBar;
    }

    public void setHasBeenResizedWithoutNavBar(boolean hasBeenResizedWithNavBar) {
        this.mHasBeenResizedWithoutNavBar = hasBeenResizedWithNavBar;
    }

    private boolean isNeedToSyncAppFullView() {
        View view = this.mAppFullView;
        return view != null && view.getVisibility() == 0;
    }

    /* access modifiers changed from: protected */
    public void removeSelf() {
        if (!this.mHasRemovedSelf) {
            ViewParent viewParent = getParent();
            if (viewParent instanceof ViewGroup) {
                ViewGroup viewGroup = (ViewGroup) viewParent;
                viewGroup.removeView(this);
                this.mHasRemovedSelf = true;
                Log.d(TAG, "remove self = " + this + ", parent = " + viewGroup);
            }
        }
    }

    /* access modifiers changed from: protected */
    public void addSelf() {
        if (this.mHasRemovedSelf) {
            ViewParent viewParent = getParent();
            if (viewParent instanceof ViewGroup) {
                ViewGroup viewGroup = (ViewGroup) viewParent;
                setVisibility(0);
                viewGroup.addView(this);
                this.mHasRemovedSelf = false;
                Log.d(TAG, "add self = " + this + ", parent = " + viewGroup);
            }
        }
    }

    public void setHasRemovedSelf(boolean hasRemovedSelf) {
        this.mHasRemovedSelf = hasRemovedSelf;
    }
}
