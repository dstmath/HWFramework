package huawei.android.widget;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.TextView;
import huawei.android.widget.loader.ResLoaderUtil;

public class SubTabViewContainer extends HorizontalScrollView {
    private static final int ANIMATION_DURATION = 200;
    private ValueAnimator mScrollAnimator;
    private int mSubTabItemMargin;
    private final SlidingTabStrip mTabStrip;

    public static class SlidingTabStrip extends LinearLayout {
        private ValueAnimator mIndicatorAnimator;
        private int mIndicatorLeft = -1;
        private int mIndicatorRight = -1;
        private int mLayoutDirection = -1;
        private int mSelectedIndicatorHeight;
        private int mSelectedIndicatorMargin;
        private final Paint mSelectedIndicatorPaint;
        int mSelectedPosition = -1;
        float mSelectionOffset;
        private Drawable mUnderlineDrawable;

        SlidingTabStrip(Context context) {
            super(context);
            setWillNotDraw(false);
            this.mSelectedIndicatorPaint = new Paint();
            this.mUnderlineDrawable = getResources().getDrawable(ResLoaderUtil.getDrawableId(context, "subtab_underline"));
        }

        /* access modifiers changed from: package-private */
        public void setSelectedIndicatorColor(int color) {
            if (this.mSelectedIndicatorPaint.getColor() != color) {
                this.mUnderlineDrawable.setTint(color);
                this.mSelectedIndicatorPaint.setColor(color);
                postInvalidateOnAnimation();
            }
        }

        /* access modifiers changed from: package-private */
        public void setSelectedIndicatorHeight(int height) {
            if (this.mSelectedIndicatorHeight != height) {
                this.mSelectedIndicatorHeight = height;
                postInvalidateOnAnimation();
            }
        }

        /* access modifiers changed from: package-private */
        public void setSelectedIndicatorMargin(int margin) {
            if (this.mSelectedIndicatorMargin != margin) {
                this.mSelectedIndicatorMargin = margin;
                postInvalidateOnAnimation();
            }
        }

        /* access modifiers changed from: package-private */
        public boolean childrenNeedLayout() {
            int z = getChildCount();
            for (int i = 0; i < z; i++) {
                if (getChildAt(i).getWidth() <= 0) {
                    return true;
                }
            }
            return false;
        }

        /* access modifiers changed from: package-private */
        public void setIndicatorPositionFromTabPosition(int position, float positionOffset) {
            if (this.mIndicatorAnimator != null && this.mIndicatorAnimator.isRunning()) {
                this.mIndicatorAnimator.cancel();
            }
            this.mSelectedPosition = position;
            this.mSelectionOffset = positionOffset;
            updateIndicatorPosition();
        }

        /* access modifiers changed from: package-private */
        public float getIndicatorPosition() {
            return ((float) this.mSelectedPosition) + this.mSelectionOffset;
        }

        public void onRtlPropertiesChanged(int layoutDirection) {
            super.onRtlPropertiesChanged(layoutDirection);
            if (Build.VERSION.SDK_INT < 23 && this.mLayoutDirection != layoutDirection) {
                requestLayout();
                this.mLayoutDirection = layoutDirection;
            }
        }

        /* access modifiers changed from: protected */
        public void onLayout(boolean changed, int l, int t, int r, int b) {
            super.onLayout(changed, l, t, r, b);
            if (this.mIndicatorAnimator == null || !this.mIndicatorAnimator.isRunning()) {
                updateIndicatorPosition();
                return;
            }
            this.mIndicatorAnimator.cancel();
            animateIndicatorToPosition(this.mSelectedPosition, Math.round((1.0f - this.mIndicatorAnimator.getAnimatedFraction()) * ((float) this.mIndicatorAnimator.getDuration())));
        }

        private void updateIndicatorPosition() {
            int right;
            int left;
            View selectedTitle = getChildAt(this.mSelectedPosition);
            if (selectedTitle == null || selectedTitle.getWidth() <= 0) {
                left = -1;
                right = -1;
            } else {
                left = selectedTitle.getLeft();
                right = selectedTitle.getRight();
                if (this.mSelectionOffset > 0.0f && this.mSelectedPosition < getChildCount() - 1) {
                    View nextTitle = getChildAt(this.mSelectedPosition + 1);
                    int newLeft = nextTitle.getLeft();
                    int newRight = nextTitle.getRight();
                    left = (int) ((this.mSelectionOffset * ((float) newLeft)) + ((1.0f - this.mSelectionOffset) * ((float) left)));
                    right = (int) ((this.mSelectionOffset * ((float) newRight)) + ((1.0f - this.mSelectionOffset) * ((float) right)));
                }
            }
            setIndicatorPosition(left, right);
        }

        /* access modifiers changed from: package-private */
        public void setIndicatorPosition(int left, int right) {
            if (left != this.mIndicatorLeft || right != this.mIndicatorRight) {
                this.mIndicatorLeft = left;
                this.mIndicatorRight = right;
                postInvalidateOnAnimation();
            }
        }

        /* access modifiers changed from: package-private */
        public void animateIndicatorToPosition(int position, int duration) {
            if (this.mIndicatorAnimator != null && this.mIndicatorAnimator.isRunning()) {
                this.mIndicatorAnimator.cancel();
            }
            boolean z = true;
            if (getLayoutDirection() != 1) {
                z = false;
            }
            boolean z2 = z;
            View targetView = getChildAt(position);
            if (targetView == null) {
                updateIndicatorPosition();
                return;
            }
            int targetLeft = targetView.getLeft();
            int targetRight = targetView.getRight();
            int startLeft = this.mIndicatorLeft;
            int startRight = this.mIndicatorRight;
            if (startLeft == targetLeft && startRight == targetRight) {
                int i = position;
                int i2 = duration;
            } else {
                ValueAnimator valueAnimator = new ValueAnimator();
                this.mIndicatorAnimator = valueAnimator;
                ValueAnimator animator = valueAnimator;
                animator.setInterpolator(AnimationUtils.FAST_OUT_SLOW_IN_INTERPOLATOR);
                animator.setDuration((long) duration);
                animator.setFloatValues(new float[]{0.0f, 1.0f});
                final int i3 = startLeft;
                final int i4 = targetLeft;
                final int i5 = startRight;
                final int i6 = targetRight;
                AnonymousClass1 r0 = new ValueAnimator.AnimatorUpdateListener() {
                    public void onAnimationUpdate(ValueAnimator animator) {
                        float fraction = animator.getAnimatedFraction();
                        SlidingTabStrip.this.setIndicatorPosition(AnimationUtils.lerp(i3, i4, fraction), AnimationUtils.lerp(i5, i6, fraction));
                    }
                };
                animator.addUpdateListener(r0);
                final int i7 = position;
                animator.addListener(new AnimatorListenerAdapter() {
                    public void onAnimationEnd(Animator animator) {
                        SlidingTabStrip.this.mSelectedPosition = i7;
                        SlidingTabStrip.this.mSelectionOffset = 0.0f;
                    }
                });
                animator.start();
            }
        }

        public void draw(Canvas canvas) {
            super.draw(canvas);
            int marginBottom = 0;
            if (this.mSelectedPosition != -1) {
                marginBottom = ((TextView) getChildAt(this.mSelectedPosition)).getTotalPaddingBottom() - this.mSelectedIndicatorMargin;
            }
            if (this.mIndicatorLeft >= 0 && this.mIndicatorRight > this.mIndicatorLeft) {
                this.mUnderlineDrawable.setBounds(0, 0, this.mIndicatorRight - this.mIndicatorLeft, this.mSelectedIndicatorHeight);
                canvas.save();
                canvas.translate((float) this.mIndicatorLeft, (float) ((getHeight() - this.mSelectedIndicatorHeight) - marginBottom));
                this.mUnderlineDrawable.draw(canvas);
                canvas.restore();
            }
        }
    }

    public SubTabViewContainer(Context context) {
        this(context, null);
    }

    public SubTabViewContainer(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SubTabViewContainer(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setHorizontalScrollBarEnabled(false);
        this.mTabStrip = new SlidingTabStrip(context);
        super.addView(this.mTabStrip, 0, new FrameLayout.LayoutParams(-2, -1));
    }

    /* access modifiers changed from: protected */
    public void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        View child = getChildAt(0);
        if (canScroll()) {
            int tabPadding = ResLoaderUtil.getDimensionPixelSize(getContext(), "margin_l") - this.mSubTabItemMargin;
            child.setPadding(tabPadding, 0, tabPadding, 0);
            setHorizontalFadingEdgeEnabled(true);
            setFadingEdgeLength(ResLoaderUtil.getDimensionPixelSize(getContext(), "margin_l"));
            return;
        }
        setHorizontalFadingEdgeEnabled(false);
        child.setPadding(0, 0, 0, 0);
    }

    /* access modifiers changed from: protected */
    public float getRightFadingEdgeStrength() {
        return 1.0f;
    }

    /* access modifiers changed from: protected */
    public float getLeftFadingEdgeStrength() {
        return 1.0f;
    }

    /* access modifiers changed from: protected */
    public void setSubTabItemMargin(int itemMargin) {
        this.mSubTabItemMargin = itemMargin;
    }

    public SlidingTabStrip getmTabStrip() {
        return this.mTabStrip;
    }

    public void animateToTab(int newPosition) {
        if (newPosition != -1) {
            if (getWindowToken() == null || !isLaidOut() || this.mTabStrip.childrenNeedLayout()) {
                setScrollPosition(newPosition, 0.0f);
                return;
            }
            int startScrollX = getScrollX();
            int targetScrollX = calculateScrollXForTab(newPosition, 0.0f);
            if (startScrollX != targetScrollX) {
                ensureScrollAnimator();
                this.mScrollAnimator.setIntValues(new int[]{startScrollX, targetScrollX});
                this.mScrollAnimator.start();
            }
            this.mTabStrip.animateIndicatorToPosition(newPosition, ANIMATION_DURATION);
        }
    }

    private void ensureScrollAnimator() {
        if (this.mScrollAnimator == null) {
            this.mScrollAnimator = new ValueAnimator();
            this.mScrollAnimator.setInterpolator(AnimationUtils.FAST_OUT_SLOW_IN_INTERPOLATOR);
            this.mScrollAnimator.setDuration(200);
            this.mScrollAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                public void onAnimationUpdate(ValueAnimator animator) {
                    SubTabViewContainer.this.scrollTo(((Integer) animator.getAnimatedValue()).intValue(), 0);
                }
            });
        }
    }

    public void setScrollPosition(int position, float positionOffset) {
        setScrollPosition(position, positionOffset, true);
    }

    public void setScrollPosition(int position, float positionOffset, boolean updateIndicatorPosition) {
        int roundedPosition = Math.round(((float) position) + positionOffset);
        if (roundedPosition >= 0 && roundedPosition < this.mTabStrip.getChildCount()) {
            if (updateIndicatorPosition) {
                this.mTabStrip.setIndicatorPositionFromTabPosition(position, positionOffset);
            }
            if (this.mScrollAnimator != null && this.mScrollAnimator.isRunning()) {
                this.mScrollAnimator.cancel();
            }
            scrollTo(calculateScrollXForTab(position, positionOffset), 0);
        }
    }

    private int calculateScrollXForTab(int position, float positionOffset) {
        View nextChild;
        View selectedChild = this.mTabStrip.getChildAt(position);
        if (position + 1 < this.mTabStrip.getChildCount()) {
            nextChild = this.mTabStrip.getChildAt(position + 1);
        } else {
            nextChild = null;
        }
        int nextWidth = 0;
        int selectedWidth = selectedChild != null ? selectedChild.getWidth() : 0;
        if (nextChild != null) {
            nextWidth = nextChild.getWidth();
        }
        int scrollBase = 0;
        if (selectedChild != null) {
            scrollBase = (selectedChild.getLeft() + (selectedWidth / 2)) - (getWidth() / 2);
        }
        int scrollOffset = (int) (((((float) (selectedWidth + nextWidth)) * 0.5f) + ((float) (this.mSubTabItemMargin * 2))) * positionOffset);
        if (getLayoutDirection() == 0) {
            return scrollBase + scrollOffset;
        }
        return scrollBase - scrollOffset;
    }

    public boolean canScroll() {
        boolean z = false;
        View child = getChildAt(0);
        if (child == null) {
            return false;
        }
        if (getMeasuredWidth() < getPaddingStart() + child.getMeasuredWidth() + getPaddingEnd()) {
            z = true;
        }
        return z;
    }

    /* access modifiers changed from: package-private */
    public int dpToPx(int dps) {
        return Math.round(getResources().getDisplayMetrics().density * ((float) dps));
    }
}
