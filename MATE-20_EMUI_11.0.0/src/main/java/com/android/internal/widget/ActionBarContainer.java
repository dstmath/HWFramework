package com.android.internal.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Outline;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.ActionMode;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import com.android.internal.R;

public class ActionBarContainer extends FrameLayout {
    private View mActionBarView;
    private View mActionContextView;
    private Drawable mBackground;
    protected boolean mForcedPrimaryBackground;
    protected boolean mForcedSplitBackground;
    protected boolean mForcedStackedBackground;
    private int mHeight;
    private boolean mIsSplit;
    private boolean mIsStacked;
    private boolean mIsTransitioning;
    private Drawable mSplitBackground;
    private Drawable mStackedBackground;
    private View mTabContainer;

    public ActionBarContainer(Context context) {
        this(context, null);
    }

    public ActionBarContainer(Context context, AttributeSet attrs) {
        super(context, attrs);
        setBackground(new ActionBarBackgroundDrawable());
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.ActionBar);
        this.mBackground = a.getDrawable(2);
        this.mStackedBackground = a.getDrawable(18);
        this.mHeight = a.getDimensionPixelSize(4, -1);
        boolean z = true;
        if (getId() == 16909433) {
            this.mIsSplit = true;
            this.mSplitBackground = a.getDrawable(19);
        }
        a.recycle();
        if (this.mIsSplit) {
            if (this.mSplitBackground != null) {
                z = false;
            }
        } else if (!(this.mBackground == null && this.mStackedBackground == null)) {
            z = false;
        }
        setWillNotDraw(z);
    }

    @Override // android.view.View
    public void onFinishInflate() {
        super.onFinishInflate();
        this.mActionBarView = findViewById(R.id.action_bar);
        this.mActionContextView = findViewById(R.id.action_context_bar);
    }

    public void setPrimaryBackground(Drawable bg) {
        Drawable drawable = this.mBackground;
        if (drawable != null) {
            drawable.setCallback(null);
            unscheduleDrawable(this.mBackground);
        }
        this.mBackground = bg;
        if (bg != null) {
            bg.setCallback(this);
            View view = this.mActionBarView;
            if (view != null) {
                this.mBackground.setBounds(view.getLeft(), this.mActionBarView.getTop(), this.mActionBarView.getRight(), this.mActionBarView.getBottom());
            }
        }
        boolean z = true;
        if (this.mIsSplit) {
            if (this.mSplitBackground != null) {
                z = false;
            }
        } else if (!(this.mBackground == null && this.mStackedBackground == null)) {
            z = false;
        }
        setWillNotDraw(z);
        invalidate();
    }

    public void setStackedBackground(Drawable bg) {
        Drawable drawable;
        View view;
        Drawable drawable2 = this.mStackedBackground;
        if (drawable2 != null) {
            drawable2.setCallback(null);
            unscheduleDrawable(this.mStackedBackground);
        }
        this.mStackedBackground = bg;
        if (bg != null) {
            bg.setCallback(this);
            if (!(!this.mIsStacked || (drawable = this.mStackedBackground) == null || (view = this.mTabContainer) == null)) {
                drawable.setBounds(view.getLeft(), this.mTabContainer.getTop(), this.mTabContainer.getRight(), this.mTabContainer.getBottom());
            }
        }
        boolean z = true;
        if (this.mIsSplit) {
            if (this.mSplitBackground != null) {
                z = false;
            }
        } else if (!(this.mBackground == null && this.mStackedBackground == null)) {
            z = false;
        }
        setWillNotDraw(z);
        invalidate();
    }

    public void setSplitBackground(Drawable bg) {
        Drawable drawable;
        Drawable drawable2 = this.mSplitBackground;
        if (drawable2 != null) {
            drawable2.setCallback(null);
            unscheduleDrawable(this.mSplitBackground);
        }
        this.mSplitBackground = bg;
        boolean z = false;
        if (bg != null) {
            bg.setCallback(this);
            if (this.mIsSplit && (drawable = this.mSplitBackground) != null) {
                drawable.setBounds(0, 0, getMeasuredWidth(), getMeasuredHeight());
            }
        }
        if (this.mIsSplit) {
            if (this.mSplitBackground == null) {
                z = true;
            }
        } else if (this.mBackground == null && this.mStackedBackground == null) {
            z = true;
        }
        setWillNotDraw(z);
        invalidate();
    }

    @Override // android.view.View
    public void setVisibility(int visibility) {
        super.setVisibility(visibility);
        boolean isVisible = visibility == 0;
        Drawable drawable = this.mBackground;
        if (drawable != null) {
            drawable.setVisible(isVisible, false);
        }
        Drawable drawable2 = this.mStackedBackground;
        if (drawable2 != null) {
            drawable2.setVisible(isVisible, false);
        }
        Drawable drawable3 = this.mSplitBackground;
        if (drawable3 != null) {
            drawable3.setVisible(isVisible, false);
        }
    }

    /* access modifiers changed from: protected */
    @Override // android.view.View
    public boolean verifyDrawable(Drawable who) {
        return (who == this.mBackground && !this.mIsSplit) || (who == this.mStackedBackground && this.mIsStacked) || ((who == this.mSplitBackground && this.mIsSplit) || super.verifyDrawable(who));
    }

    /* access modifiers changed from: protected */
    @Override // android.view.ViewGroup, android.view.View
    public void drawableStateChanged() {
        super.drawableStateChanged();
        int[] state = getDrawableState();
        boolean changed = false;
        Drawable background = this.mBackground;
        if (background != null && background.isStateful()) {
            changed = false | background.setState(state);
        }
        Drawable stackedBackground = this.mStackedBackground;
        if (stackedBackground != null && stackedBackground.isStateful()) {
            changed |= stackedBackground.setState(state);
        }
        Drawable splitBackground = this.mSplitBackground;
        if (splitBackground != null && splitBackground.isStateful()) {
            changed |= splitBackground.setState(state);
        }
        if (changed) {
            invalidate();
        }
    }

    @Override // android.view.ViewGroup, android.view.View
    public void jumpDrawablesToCurrentState() {
        super.jumpDrawablesToCurrentState();
        Drawable drawable = this.mBackground;
        if (drawable != null) {
            drawable.jumpToCurrentState();
        }
        Drawable drawable2 = this.mStackedBackground;
        if (drawable2 != null) {
            drawable2.jumpToCurrentState();
        }
        Drawable drawable3 = this.mSplitBackground;
        if (drawable3 != null) {
            drawable3.jumpToCurrentState();
        }
    }

    @Override // android.view.View
    public void onResolveDrawables(int layoutDirection) {
        super.onResolveDrawables(layoutDirection);
        Drawable drawable = this.mBackground;
        if (drawable != null) {
            drawable.setLayoutDirection(layoutDirection);
        }
        Drawable drawable2 = this.mStackedBackground;
        if (drawable2 != null) {
            drawable2.setLayoutDirection(layoutDirection);
        }
        Drawable drawable3 = this.mSplitBackground;
        if (drawable3 != null) {
            drawable3.setLayoutDirection(layoutDirection);
        }
    }

    public void setTransitioning(boolean isTransitioning) {
        int i;
        this.mIsTransitioning = isTransitioning;
        if (isTransitioning) {
            i = 393216;
        } else {
            i = 262144;
        }
        setDescendantFocusability(i);
    }

    @Override // android.view.ViewGroup
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return this.mIsTransitioning || super.onInterceptTouchEvent(ev);
    }

    @Override // android.view.View
    public boolean onTouchEvent(MotionEvent ev) {
        super.onTouchEvent(ev);
        return true;
    }

    @Override // android.view.View
    public boolean onHoverEvent(MotionEvent ev) {
        super.onHoverEvent(ev);
        return true;
    }

    public void setTabContainer(ScrollingTabContainerView tabView) {
        View view = this.mTabContainer;
        if (view != null) {
            removeView(view);
        }
        this.mTabContainer = tabView;
        if (tabView != null) {
            addView(tabView);
            ViewGroup.LayoutParams lp = tabView.getLayoutParams();
            lp.width = -1;
            lp.height = -2;
            tabView.setAllowCollapse(false);
        }
    }

    public void setSplitViewLocation(int start, int end) {
    }

    public View getTabContainer() {
        return this.mTabContainer;
    }

    @Override // android.view.ViewGroup, android.view.ViewParent
    public ActionMode startActionModeForChild(View child, ActionMode.Callback callback, int type) {
        if (type != 0) {
            return super.startActionModeForChild(child, callback, type);
        }
        return null;
    }

    /* access modifiers changed from: private */
    public static boolean isCollapsed(View view) {
        return view == null || view.getVisibility() == 8 || view.getMeasuredHeight() == 0;
    }

    private int getMeasuredHeightWithMargins(View view) {
        FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) view.getLayoutParams();
        return view.getMeasuredHeight() + lp.topMargin + lp.bottomMargin;
    }

    @Override // android.widget.FrameLayout, android.view.View
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        View view;
        int i;
        int i2;
        if (this.mActionBarView == null && View.MeasureSpec.getMode(heightMeasureSpec) == Integer.MIN_VALUE && (i2 = this.mHeight) >= 0) {
            heightMeasureSpec = View.MeasureSpec.makeMeasureSpec(Math.min(i2, View.MeasureSpec.getSize(heightMeasureSpec)), Integer.MIN_VALUE);
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        if (!(this.mActionBarView == null || (view = this.mTabContainer) == null || view.getVisibility() == 8)) {
            int nonTabMaxHeight = 0;
            int childCount = getChildCount();
            for (int i3 = 0; i3 < childCount; i3++) {
                View child = getChildAt(i3);
                if (child != this.mTabContainer) {
                    if (isCollapsed(child)) {
                        i = 0;
                    } else {
                        i = getMeasuredHeightWithMargins(child);
                    }
                    nonTabMaxHeight = Math.max(nonTabMaxHeight, i);
                }
            }
            setMeasuredDimension(getMeasuredWidth(), Math.min(getMeasuredHeightWithMargins(this.mTabContainer) + nonTabMaxHeight, View.MeasureSpec.getMode(heightMeasureSpec) == Integer.MIN_VALUE ? View.MeasureSpec.getSize(heightMeasureSpec) : Integer.MAX_VALUE));
        }
    }

    @Override // android.widget.FrameLayout, android.view.ViewGroup, android.view.View
    public void onLayout(boolean changed, int l, int t, int r, int b) {
        Drawable drawable;
        super.onLayout(changed, l, t, r, b);
        View tabContainer = this.mTabContainer;
        boolean hasTabs = (tabContainer == null || tabContainer.getVisibility() == 8) ? false : true;
        if (!(tabContainer == null || tabContainer.getVisibility() == 8)) {
            int containerHeight = getMeasuredHeight();
            FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) tabContainer.getLayoutParams();
            tabContainer.layout(l, (containerHeight - tabContainer.getMeasuredHeight()) - lp.bottomMargin, r, containerHeight - lp.bottomMargin);
        }
        boolean needsInvalidate = false;
        if (this.mIsSplit) {
            Drawable drawable2 = this.mSplitBackground;
            if (drawable2 != null) {
                drawable2.setBounds(0, 0, getMeasuredWidth(), getMeasuredHeight());
                needsInvalidate = true;
            }
        } else {
            if (this.mBackground != null) {
                if (this.mActionBarView.getVisibility() == 0) {
                    this.mBackground.setBounds(this.mActionBarView.getLeft(), this.mActionBarView.getTop(), this.mActionBarView.getRight(), this.mActionBarView.getBottom());
                } else {
                    View view = this.mActionContextView;
                    if (view == null || view.getVisibility() != 0) {
                        this.mBackground.setBounds(0, 0, 0, 0);
                    } else {
                        this.mBackground.setBounds(this.mActionContextView.getLeft(), this.mActionContextView.getTop(), this.mActionContextView.getRight(), this.mActionContextView.getBottom());
                    }
                }
                needsInvalidate = true;
            }
            this.mIsStacked = hasTabs;
            if (hasTabs && (drawable = this.mStackedBackground) != null) {
                drawable.setBounds(tabContainer.getLeft(), tabContainer.getTop(), tabContainer.getRight(), tabContainer.getBottom());
                needsInvalidate = true;
            }
        }
        if (needsInvalidate) {
            invalidate();
        }
    }

    private class ActionBarBackgroundDrawable extends Drawable {
        private ActionBarBackgroundDrawable() {
        }

        @Override // android.graphics.drawable.Drawable
        public void draw(Canvas canvas) {
            if (!ActionBarContainer.this.mIsSplit) {
                if (ActionBarContainer.this.mBackground != null) {
                    ActionBarContainer.this.mBackground.draw(canvas);
                }
                if (ActionBarContainer.this.mStackedBackground != null && ActionBarContainer.this.mIsStacked) {
                    ActionBarContainer.this.mStackedBackground.draw(canvas);
                }
            } else if (ActionBarContainer.this.mSplitBackground != null) {
                ActionBarContainer.this.mSplitBackground.draw(canvas);
            }
        }

        @Override // android.graphics.drawable.Drawable
        public void getOutline(Outline outline) {
            if (ActionBarContainer.this.mIsSplit) {
                if (ActionBarContainer.this.mSplitBackground != null) {
                    ActionBarContainer.this.mSplitBackground.getOutline(outline);
                }
            } else if (ActionBarContainer.this.mBackground != null) {
                ActionBarContainer.this.mBackground.getOutline(outline);
            }
        }

        @Override // android.graphics.drawable.Drawable
        public void setAlpha(int alpha) {
        }

        @Override // android.graphics.drawable.Drawable
        public void setColorFilter(ColorFilter colorFilter) {
        }

        @Override // android.graphics.drawable.Drawable
        public int getOpacity() {
            if (ActionBarContainer.this.mIsSplit) {
                if (ActionBarContainer.this.mSplitBackground != null && ActionBarContainer.this.mSplitBackground.getOpacity() == -1) {
                    return -1;
                }
            } else if ((!ActionBarContainer.this.mIsStacked || (ActionBarContainer.this.mStackedBackground != null && ActionBarContainer.this.mStackedBackground.getOpacity() == -1)) && !ActionBarContainer.isCollapsed(ActionBarContainer.this.mActionBarView) && ActionBarContainer.this.mBackground != null && ActionBarContainer.this.mBackground.getOpacity() == -1) {
                return -1;
            }
            return 0;
        }
    }

    /* access modifiers changed from: protected */
    public void setHeight(int h) {
        this.mHeight = h;
    }

    /* access modifiers changed from: protected */
    public View getActionBarView() {
        return this.mActionBarView;
    }

    /* access modifiers changed from: protected */
    public Drawable getPrimaryBackground() {
        return this.mBackground;
    }

    /* access modifiers changed from: protected */
    public Drawable getStackedBackground() {
        return this.mStackedBackground;
    }

    /* access modifiers changed from: protected */
    public Drawable getSplitBackground() {
        return this.mSplitBackground;
    }

    public void setForcedPrimaryBackground(boolean forced) {
        this.mForcedPrimaryBackground = forced;
    }

    public void setForcedStackedBackground(boolean forced) {
        this.mForcedStackedBackground = forced;
    }

    public void setForcedSplitBackground(boolean forced) {
        this.mForcedSplitBackground = forced;
    }

    @Deprecated
    public void setAnimationEnable(boolean enableAnim) {
    }

    @Deprecated
    public boolean isAnimationEnabled() {
        return false;
    }
}
