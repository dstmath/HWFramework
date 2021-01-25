package com.android.server.multiwin.view;

import android.animation.PropertyValuesHolder;
import android.animation.TypeEvaluator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.Region;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.DragEvent;
import com.android.server.multiwin.HwMultiWinUtils;
import com.android.server.multiwin.animation.interpolator.FastOutSlowInInterpolator;
import com.android.server.multiwin.animation.interpolator.SharpCurveInterpolator;

public class HwMultiWinPushPendingDropView extends HwMultiWinHotAreaView {
    private static final boolean DBG = false;
    private static final long SHADOW_COMPLETE_SHOW_DURATION = 250;
    private static final long SHADOW_PUSH_DURATION = 250;
    private static final float SHADOW_SHOW_ALPHA = 0.55f;
    private static final float SHADOW_SHOW_ALPHA_NIGHT = 0.1f;
    private static final long SHADOW_SHOW_DELAY = 150;
    private static final long SHADOW_SHOW_DURATION = 250;
    private static final String TAG = "PushPendingDropView";
    private float mLastDragX = -1.0f;
    private float mLastDragY = -1.0f;
    private Rect mPushAcceptBound;
    private HwMultiWinClipImageView mPushTarget;
    private Drawable mShadow;
    private float mShadowAlpha;
    private Rect mShadowClipRect = new Rect();
    private Path mShadowPath;
    private ValueAnimator mShadowPushAnimator;
    private float mShadowRoundCorner;
    private float mShadowScaleX;
    private float mShadowScaleY;
    private float mShadowShowAlpha;
    private ValueAnimator mShadowShowAnimator;

    public HwMultiWinPushPendingDropView(Context context) {
        super(context);
    }

    public HwMultiWinPushPendingDropView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public HwMultiWinPushPendingDropView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public HwMultiWinPushPendingDropView(Context context, Rect pushAcceptBound, HwMultiWinClipImageView pushTarget, int splitMode, Point pendingDropSize) {
        super(context);
        this.mPushAcceptBound = pushAcceptBound;
        this.mPushTarget = pushTarget;
        this.mSplitMode = splitMode;
        this.mShadow = new ColorDrawable(Color.parseColor("#FFFFFFFF"));
        initShadowClipRect(pendingDropSize);
        this.mIsToDrawBorder = true;
        this.mShadowShowAlpha = HwMultiWinUtils.isInNightMode(context) ? 0.1f : SHADOW_SHOW_ALPHA;
    }

    private boolean isLeftRightSplit() {
        return this.mSplitMode == 1 || this.mSplitMode == 2;
    }

    private void initShadowClipRect(Point pendingDropSize) {
        HwMultiWinClipImageView hwMultiWinClipImageView;
        if (pendingDropSize == null || (hwMultiWinClipImageView = this.mPushTarget) == null) {
            Log.w(TAG, "initShadowClipRect failed, cause pendingDropSize or mPushTarget is null!");
            return;
        }
        int shadowLength = Math.round(2.0f * hwMultiWinClipImageView.getPrePushWidth()) - getContext().getResources().getDimensionPixelSize(34472582);
        int shadowWidth = isLeftRightSplit() ? shadowLength : this.mPushAcceptBound.width();
        int shadowHeight = isLeftRightSplit() ? this.mPushAcceptBound.height() : shadowLength;
        int pendingDropWidth = pendingDropSize.x;
        int pendingDropHeight = pendingDropSize.y;
        if (this.mSplitMode == 1 || this.mSplitMode == 3) {
            this.mShadowClipRect = new Rect(0, 0, shadowWidth, shadowHeight);
        } else if (this.mSplitMode == 2 || this.mSplitMode == 4) {
            this.mShadowClipRect = new Rect(pendingDropWidth - shadowWidth, pendingDropHeight - shadowHeight, pendingDropWidth, pendingDropHeight);
        } else {
            Log.w(TAG, "initShadowClipRect: no shadow to draw!");
            this.mShadowClipRect = new Rect();
        }
        float f = 1.0f;
        this.mShadowScaleX = isLeftRightSplit() ? 1.0f : this.mPushTarget.getFullScreenShotScaleFactor2();
        if (isLeftRightSplit()) {
            f = this.mPushTarget.getFullScreenShotScaleFactor2();
        }
        this.mShadowScaleY = f;
        this.mShadowRoundCorner = this.mRoundCornerRadius;
        this.mShadowPath = new Path();
    }

    /* access modifiers changed from: protected */
    @Override // com.android.server.multiwin.view.HwMultiWinHotAreaView, com.android.server.multiwin.view.HwMultiWinClipImageView, android.widget.ImageView, android.view.View
    public void onDraw(Canvas canvas) {
        this.mShadow.setBounds(this.mShadowClipRect);
        this.mShadow.setAlpha((int) (this.mShadowAlpha * 255.0f));
        this.mShadowPath.reset();
        float f = this.mShadowRoundCorner;
        this.mShadowPath.addRoundRect((float) this.mShadowClipRect.left, (float) this.mShadowClipRect.top, (float) this.mShadowClipRect.right, (float) this.mShadowClipRect.bottom, f, f, Path.Direction.CW);
        super.onDraw(canvas);
        canvas.save();
        canvas.scale(this.mShadowScaleX, this.mShadowScaleY, ((float) this.mShadowClipRect.width()) / 2.0f, ((float) this.mShadowClipRect.height()) / 2.0f);
        canvas.clipPath(this.mShadowPath, Region.Op.INTERSECT);
        this.mShadow.draw(canvas);
        drawOutlineBorder(canvas);
        canvas.restore();
    }

    /* access modifiers changed from: protected */
    @Override // com.android.server.multiwin.view.HwMultiWinClipImageView
    public void drawOutlineBorder(Canvas canvas) {
        if (this.mBorderPaint != null && this.mIsToDrawBorder) {
            this.mBorderPaint.setAlpha((int) (this.mShadowAlpha * 255.0f));
            canvas.drawPath(this.mShadowPath, this.mBorderPaint);
        }
    }

    /* access modifiers changed from: package-private */
    @Override // com.android.server.multiwin.view.HwMultiWinHotAreaView
    public void handleDragEntered(DragEvent dragEvent, int dragSurfaceAnimType) {
    }

    /* access modifiers changed from: package-private */
    @Override // com.android.server.multiwin.view.HwMultiWinHotAreaView
    public void handleDragLocation(DragEvent dragEvent) {
        super.handleDragLocation(dragEvent);
        int[] loc = new int[2];
        getLocationOnScreen(loc);
        float dragX = dragEvent.getX() + ((float) loc[0]);
        float dragY = dragEvent.getY() + ((float) loc[1]);
        if (isDragOutOfSwapAcceptBound(dragX, dragY)) {
            if (this.mSplitBarController != null) {
                this.mSplitBarController.hideSplitBarWithAnimation();
            }
            removeSelf();
        } else if (isDragIntoSwapAcceptBound(dragX, dragY)) {
            addSelf();
        }
        this.mLastDragX = dragX;
        this.mLastDragY = dragY;
    }

    private boolean isDragIntoSwapAcceptBound(float dragX, float dragY) {
        return !isInSwapAcceptBound(this.mLastDragX, this.mLastDragY) && isInSwapAcceptBound(dragX, dragY);
    }

    private boolean isDragOutOfSwapAcceptBound(float dragX, float dragY) {
        if (this.mLastDragX < 0.0f && this.mLastDragY < 0.0f) {
            return !isInSwapAcceptBound(dragX, dragY);
        }
        if (!isInSwapAcceptBound(this.mLastDragX, this.mLastDragY) || isInSwapAcceptBound(dragX, dragY)) {
            return false;
        }
        return true;
    }

    private boolean isInSwapAcceptBound(float x, float y) {
        Rect rect = this.mPushAcceptBound;
        if (rect == null) {
            Log.w(TAG, "isInSwapAcceptBound return false, cause mSwapAcceptBound is null");
            return false;
        } else if (x < ((float) rect.left) || x > ((float) this.mPushAcceptBound.right) || y < ((float) this.mPushAcceptBound.top) || y > ((float) this.mPushAcceptBound.bottom)) {
            return false;
        } else {
            return true;
        }
    }

    private boolean isNeedToPreserve() {
        return this.mNotchPos != -1;
    }

    /* access modifiers changed from: package-private */
    @Override // com.android.server.multiwin.view.HwMultiWinHotAreaView
    public void handleDragExited(DragEvent dragEvent) {
        super.handleDragExited(dragEvent);
        if (!isNeedToPreserve()) {
            removeSelf();
        }
    }

    /* access modifiers changed from: package-private */
    @Override // com.android.server.multiwin.view.HwMultiWinHotAreaView
    public void handleDrop(DragEvent dragEvent, int dragSurfaceAnimType) {
        super.handleDrop(dragEvent, 6);
        HwMultiWinClipImageView hwMultiWinClipImageView = this.mPushTarget;
        if (hwMultiWinClipImageView != null) {
            hwMultiWinClipImageView.playFinalPushAnimation(this.mSplitMode);
        }
        if (this.mSplitBarController != null) {
            this.mSplitBarController.showSplitBarWithAnimation();
        }
        playShadowPushAnimation();
        playShadowCompleteShowAnimation();
    }

    @SuppressLint({"NewApi"})
    public void playShadowShowAnimation() {
        ValueAnimator valueAnimator = this.mShadowShowAnimator;
        if (valueAnimator != null) {
            valueAnimator.cancel();
        }
        this.mShadowShowAnimator = ValueAnimator.ofPropertyValuesHolder(PropertyValuesHolder.ofFloat("shadowAlpha", 0.0f, this.mShadowShowAlpha));
        this.mShadowShowAnimator.setDuration(250L);
        this.mShadowShowAnimator.setStartDelay(SHADOW_SHOW_DELAY);
        this.mShadowShowAnimator.setInterpolator(new SharpCurveInterpolator());
        this.mShadowShowAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            /* class com.android.server.multiwin.view.HwMultiWinPushPendingDropView.AnonymousClass1 */

            @Override // android.animation.ValueAnimator.AnimatorUpdateListener
            public void onAnimationUpdate(ValueAnimator animation) {
                Object shadowAlphaObj = animation.getAnimatedValue("shadowAlpha");
                float shadowAlpha = 1.0f;
                if (shadowAlphaObj instanceof Float) {
                    shadowAlpha = ((Float) shadowAlphaObj).floatValue();
                }
                HwMultiWinPushPendingDropView.this.mShadowAlpha = shadowAlpha;
                HwMultiWinPushPendingDropView.this.invalidate();
            }
        });
        this.mShadowShowAnimator.start();
    }

    @SuppressLint({"NewApi"})
    public void playShadowCompleteShowAnimation() {
        if (!HwMultiWinUtils.isInNightMode(getContext())) {
            ValueAnimator valueAnimator = this.mShadowShowAnimator;
            if (valueAnimator != null) {
                valueAnimator.cancel();
            }
            this.mShadowShowAnimator = ValueAnimator.ofPropertyValuesHolder(PropertyValuesHolder.ofFloat("shadowAlpha", this.mShadowAlpha, 1.0f));
            this.mShadowShowAnimator.setDuration(250L);
            this.mShadowShowAnimator.setInterpolator(new SharpCurveInterpolator());
            this.mShadowShowAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                /* class com.android.server.multiwin.view.HwMultiWinPushPendingDropView.AnonymousClass2 */

                @Override // android.animation.ValueAnimator.AnimatorUpdateListener
                public void onAnimationUpdate(ValueAnimator animation) {
                    Object shadowAlphaObj = animation.getAnimatedValue("shadowAlpha");
                    float shadowAlpha = 1.0f;
                    if (shadowAlphaObj instanceof Float) {
                        shadowAlpha = ((Float) shadowAlphaObj).floatValue();
                    }
                    HwMultiWinPushPendingDropView.this.mShadowAlpha = shadowAlpha;
                    HwMultiWinPushPendingDropView.this.invalidate();
                }
            });
            this.mShadowShowAnimator.start();
        }
    }

    @SuppressLint({"NewApi"})
    public void playShadowPushAnimation() {
        ValueAnimator valueAnimator = this.mShadowPushAnimator;
        if (valueAnimator != null && valueAnimator.isRunning()) {
            this.mShadowPushAnimator.cancel();
        }
        this.mShadowPushAnimator = ValueAnimator.ofPropertyValuesHolder(PropertyValuesHolder.ofObject("shadowClipRect", new RectTypeEvaluator(), this.mShadowClipRect, new Rect(0, 0, getWidth(), getHeight())), PropertyValuesHolder.ofFloat("shadowScaleX", this.mShadowScaleX, 1.0f), PropertyValuesHolder.ofFloat("shadowScaleY", this.mShadowScaleY, 1.0f));
        this.mShadowPushAnimator.setDuration(250L);
        this.mShadowPushAnimator.setInterpolator(new FastOutSlowInInterpolator());
        this.mShadowPushAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            /* class com.android.server.multiwin.view.HwMultiWinPushPendingDropView.AnonymousClass3 */

            @Override // android.animation.ValueAnimator.AnimatorUpdateListener
            public void onAnimationUpdate(ValueAnimator animation) {
                Object shadowClipRectObj = animation.getAnimatedValue("shadowClipRect");
                if (shadowClipRectObj instanceof Rect) {
                    HwMultiWinPushPendingDropView.this.mShadowClipRect = (Rect) shadowClipRectObj;
                }
                Object shadowScaleXObj = animation.getAnimatedValue("shadowScaleX");
                if (shadowScaleXObj instanceof Float) {
                    HwMultiWinPushPendingDropView.this.mShadowScaleX = ((Float) shadowScaleXObj).floatValue();
                }
                Object shadowScaleYObj = animation.getAnimatedValue("shadowScaleY");
                if (shadowScaleYObj instanceof Float) {
                    HwMultiWinPushPendingDropView.this.mShadowScaleY = ((Float) shadowScaleYObj).floatValue();
                }
                HwMultiWinPushPendingDropView.this.invalidate();
            }
        });
        this.mShadowPushAnimator.start();
    }

    /* access modifiers changed from: private */
    public static class RectTypeEvaluator implements TypeEvaluator<Rect> {
        private RectTypeEvaluator() {
        }

        public Rect evaluate(float fraction, Rect startValue, Rect endValue) {
            int fromLeft = startValue.left;
            int fromTop = startValue.top;
            int fromRight = startValue.right;
            int fromBottom = startValue.bottom;
            return new Rect(Math.round(((float) fromLeft) + (((float) (endValue.left - fromLeft)) * fraction)), Math.round(((float) fromTop) + (((float) (endValue.top - fromTop)) * fraction)), Math.round(((float) fromRight) + (((float) (endValue.right - fromRight)) * fraction)), Math.round(((float) fromBottom) + (((float) (endValue.bottom - fromBottom)) * fraction)));
        }
    }
}
