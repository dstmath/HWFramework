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
    private static final int DOUBLE_INT = 2;
    private static final int INVALID_VALUE = -1;
    private static final float OFFSET_SCALE = 0.5f;
    private float mMarginL;
    private ValueAnimator mScrollAnimator;
    private int mSubTabItemMargin;
    private final SlidingTabStrip mTabStrip;

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
        Object obj = Integer.valueOf(ResLoaderUtil.getDimensionPixelSize(getContext(), "margin_l"));
        if (obj instanceof Float) {
            this.mMarginL = ((Float) obj).floatValue();
        }
    }

    /* access modifiers changed from: protected */
    @Override // android.widget.HorizontalScrollView, android.widget.FrameLayout, android.view.View, android.view.ViewGroup
    public void onLayout(boolean isChanged, int left, int top, int right, int bottom) {
        super.onLayout(isChanged, left, top, right, bottom);
        View child = getChildAt(0);
        if (canScroll()) {
            int tabPadding = (int) (this.mMarginL - ((float) this.mSubTabItemMargin));
            child.setPadding(tabPadding, 0, tabPadding, 0);
            setHorizontalFadingEdgeEnabled(true);
            setFadingEdgeLength((int) this.mMarginL);
            return;
        }
        setHorizontalFadingEdgeEnabled(false);
        child.setPadding(0, 0, 0, 0);
    }

    /* access modifiers changed from: protected */
    @Override // android.widget.HorizontalScrollView, android.view.View
    public float getRightFadingEdgeStrength() {
        return 1.0f;
    }

    /* access modifiers changed from: protected */
    @Override // android.widget.HorizontalScrollView, android.view.View
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
            if (targetScrollX != startScrollX) {
                ensureScrollAnimator();
                this.mScrollAnimator.setIntValues(startScrollX, targetScrollX);
                this.mScrollAnimator.start();
            }
            this.mTabStrip.animateIndicatorToPosition(newPosition, ANIMATION_DURATION);
        }
    }

    private void ensureScrollAnimator() {
        if (this.mScrollAnimator == null) {
            this.mScrollAnimator = new ValueAnimator();
            this.mScrollAnimator.setInterpolator(AnimationUtils.FAST_OUT_SLOW_IN_INTERPOLATOR);
            this.mScrollAnimator.setDuration(200L);
            this.mScrollAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                /* class huawei.android.widget.SubTabViewContainer.AnonymousClass1 */

                @Override // android.animation.ValueAnimator.AnimatorUpdateListener
                public void onAnimationUpdate(ValueAnimator animator) {
                    if (animator != null) {
                        int value = 0;
                        Object obj = animator.getAnimatedValue();
                        if (obj instanceof Integer) {
                            value = ((Integer) obj).intValue();
                        }
                        SubTabViewContainer.this.scrollTo(value, 0);
                    }
                }
            });
        }
    }

    public void setScrollPosition(int position, float positionOffset) {
        setScrollPosition(position, positionOffset, true);
    }

    public void setScrollPosition(int position, float positionOffset, boolean isUpdateIndicatorPosition) {
        int roundedPosition = Math.round(((float) position) + positionOffset);
        if (roundedPosition >= 0 && roundedPosition < this.mTabStrip.getChildCount()) {
            if (isUpdateIndicatorPosition) {
                this.mTabStrip.setIndicatorPositionFromTabPosition(position, positionOffset);
            }
            ValueAnimator valueAnimator = this.mScrollAnimator;
            if (valueAnimator != null && valueAnimator.isRunning()) {
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
        int scrollOffset = (int) (((((float) (selectedWidth + nextWidth)) * OFFSET_SCALE) + ((float) (this.mSubTabItemMargin * 2))) * positionOffset);
        if (getLayoutDirection() == 0) {
            return scrollBase + scrollOffset;
        }
        return scrollBase - scrollOffset;
    }

    public boolean canScroll() {
        View child = getChildAt(0);
        if (child == null) {
            return false;
        }
        if (getMeasuredWidth() < getPaddingStart() + child.getMeasuredWidth() + getPaddingEnd()) {
            return true;
        }
        return false;
    }

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
            int childCount = getChildCount();
            for (int i = 0; i < childCount; i++) {
                if (getChildAt(i).getWidth() <= 0) {
                    return true;
                }
            }
            return false;
        }

        /* access modifiers changed from: package-private */
        public void setIndicatorPositionFromTabPosition(int position, float positionOffset) {
            ValueAnimator valueAnimator = this.mIndicatorAnimator;
            if (valueAnimator != null && valueAnimator.isRunning()) {
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

        @Override // android.widget.LinearLayout, android.view.View
        public void onRtlPropertiesChanged(int layoutDirection) {
            super.onRtlPropertiesChanged(layoutDirection);
            if (Build.VERSION.SDK_INT < 23 && this.mLayoutDirection != layoutDirection) {
                requestLayout();
                this.mLayoutDirection = layoutDirection;
            }
        }

        /* access modifiers changed from: protected */
        @Override // android.widget.LinearLayout, android.view.View, android.view.ViewGroup
        public void onLayout(boolean isChanged, int left, int top, int right, int bottom) {
            super.onLayout(isChanged, left, top, right, bottom);
            ValueAnimator valueAnimator = this.mIndicatorAnimator;
            if (valueAnimator == null || !valueAnimator.isRunning()) {
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
                    float f = this.mSelectionOffset;
                    left = (int) ((((float) newLeft) * f) + ((1.0f - f) * ((float) left)));
                    right = (int) ((((float) newRight) * f) + ((1.0f - f) * ((float) right)));
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
        public void animateIndicatorToPosition(final int position, int duration) {
            ValueAnimator valueAnimator = this.mIndicatorAnimator;
            if (valueAnimator != null && valueAnimator.isRunning()) {
                this.mIndicatorAnimator.cancel();
            }
            boolean z = true;
            if (getLayoutDirection() != 1) {
                z = false;
            }
            View targetView = getChildAt(position);
            if (targetView == null) {
                updateIndicatorPosition();
                return;
            }
            final int targetLeft = targetView.getLeft();
            final int targetRight = targetView.getRight();
            final int startLeft = this.mIndicatorLeft;
            final int startRight = this.mIndicatorRight;
            if (startLeft != targetLeft || startRight != targetRight) {
                ValueAnimator animator = new ValueAnimator();
                this.mIndicatorAnimator = animator;
                animator.setInterpolator(AnimationUtils.FAST_OUT_SLOW_IN_INTERPOLATOR);
                animator.setDuration((long) duration);
                animator.setFloatValues(0.0f, 1.0f);
                animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    /* class huawei.android.widget.SubTabViewContainer.SlidingTabStrip.AnonymousClass1 */

                    @Override // android.animation.ValueAnimator.AnimatorUpdateListener
                    public void onAnimationUpdate(ValueAnimator animator) {
                        float fraction = animator.getAnimatedFraction();
                        SlidingTabStrip.this.setIndicatorPosition(AnimationUtils.lerp(startLeft, targetLeft, fraction), AnimationUtils.lerp(startRight, targetRight, fraction));
                    }
                });
                animator.addListener(new AnimatorListenerAdapter() {
                    /* class huawei.android.widget.SubTabViewContainer.SlidingTabStrip.AnonymousClass2 */

                    @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
                    public void onAnimationEnd(Animator animator) {
                        SlidingTabStrip slidingTabStrip = SlidingTabStrip.this;
                        slidingTabStrip.mSelectedPosition = position;
                        slidingTabStrip.mSelectionOffset = 0.0f;
                    }
                });
                animator.start();
            }
        }

        @Override // android.view.View
        public void draw(Canvas canvas) {
            int i;
            Drawable drawable;
            super.draw(canvas);
            int marginBottom = 0;
            int i2 = this.mSelectedPosition;
            if (i2 != -1) {
                View view = getChildAt(i2);
                if (view instanceof TextView) {
                    marginBottom = ((TextView) view).getTotalPaddingBottom() - this.mSelectedIndicatorMargin;
                }
            }
            int i3 = this.mIndicatorLeft;
            if (i3 >= 0 && (i = this.mIndicatorRight) > i3 && (drawable = this.mUnderlineDrawable) != null) {
                drawable.setBounds(0, 0, i - i3, this.mSelectedIndicatorHeight);
                canvas.save();
                canvas.translate((float) this.mIndicatorLeft, (float) ((getHeight() - this.mSelectedIndicatorHeight) - marginBottom));
                this.mUnderlineDrawable.draw(canvas);
                canvas.restore();
            }
        }
    }

    /* access modifiers changed from: package-private */
    public int dpToPx(int dps) {
        return Math.round(getResources().getDisplayMetrics().density * ((float) dps));
    }
}
