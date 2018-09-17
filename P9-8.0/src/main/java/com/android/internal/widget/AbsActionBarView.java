package com.android.internal.widget;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.TimeInterpolator;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.ContextThemeWrapper;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.ActionMenuPresenter;
import android.widget.ActionMenuView;
import com.android.internal.R;

public abstract class AbsActionBarView extends ViewGroup {
    private static final int FADE_DURATION = 200;
    private static final TimeInterpolator sAlphaInterpolator = new DecelerateInterpolator();
    protected ActionMenuPresenter mActionMenuPresenter;
    private boolean mAlwaysSplit;
    protected int mContentHeight;
    private boolean mEatingHover;
    private boolean mEatingTouch;
    protected ActionMenuView mMenuView;
    protected final Context mPopupContext;
    protected boolean mSplitActionBar;
    protected ViewGroup mSplitView;
    protected boolean mSplitWhenNarrow;
    protected final VisibilityAnimListener mVisAnimListener;
    protected Animator mVisibilityAnim;

    protected class VisibilityAnimListener implements AnimatorListener {
        private boolean mCanceled = false;
        int mFinalVisibility;

        protected VisibilityAnimListener() {
        }

        public VisibilityAnimListener withFinalVisibility(int visibility) {
            this.mFinalVisibility = visibility;
            return this;
        }

        public void onAnimationStart(Animator animation) {
            AbsActionBarView.this.setVisibility(0);
            AbsActionBarView.this.mVisibilityAnim = animation;
            this.mCanceled = false;
        }

        public void onAnimationEnd(Animator animation) {
            if (!this.mCanceled) {
                AbsActionBarView.this.mVisibilityAnim = null;
                AbsActionBarView.this.setVisibility(this.mFinalVisibility);
                if (!(AbsActionBarView.this.mSplitView == null || AbsActionBarView.this.mMenuView == null)) {
                    AbsActionBarView.this.mMenuView.setVisibility(this.mFinalVisibility);
                }
            }
        }

        public void onAnimationCancel(Animator animation) {
            this.mCanceled = true;
        }

        public void onAnimationRepeat(Animator animation) {
        }
    }

    public AbsActionBarView(Context context) {
        this(context, null);
    }

    public AbsActionBarView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AbsActionBarView(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public AbsActionBarView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        this.mVisAnimListener = new VisibilityAnimListener();
        this.mAlwaysSplit = false;
        TypedValue tv = new TypedValue();
        if (!context.getTheme().resolveAttribute(R.attr.actionBarPopupTheme, tv, true) || tv.resourceId == 0) {
            this.mPopupContext = context;
        } else {
            this.mPopupContext = new ContextThemeWrapper(context, tv.resourceId);
        }
    }

    public void setSplitActionBarAlways(boolean bAlwaysSplit) {
        this.mAlwaysSplit = bAlwaysSplit;
    }

    protected void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        TypedArray a = getContext().obtainStyledAttributes(null, R.styleable.ActionBar, R.attr.actionBarStyle, 0);
        setContentHeight(a.getLayoutDimension(4, 0));
        a.recycle();
        if (this.mSplitWhenNarrow) {
            setSplitToolbar(this.mAlwaysSplit ? true : getContext().getResources().getBoolean(R.bool.split_action_bar_is_narrow));
        }
        if (this.mActionMenuPresenter != null) {
            this.mActionMenuPresenter.onConfigurationChanged(newConfig);
        }
    }

    public boolean onTouchEvent(MotionEvent ev) {
        int action = ev.getActionMasked();
        if (action == 0) {
            this.mEatingTouch = false;
        }
        if (!this.mEatingTouch) {
            boolean handled = super.onTouchEvent(ev);
            if (action == 0 && (handled ^ 1) != 0) {
                this.mEatingTouch = true;
            }
        }
        if (action == 1 || action == 3) {
            this.mEatingTouch = false;
        }
        return true;
    }

    public boolean onHoverEvent(MotionEvent ev) {
        int action = ev.getActionMasked();
        if (action == 9) {
            this.mEatingHover = false;
        }
        if (!this.mEatingHover) {
            boolean handled = super.-wrap9(ev);
            if (action == 9 && (handled ^ 1) != 0) {
                this.mEatingHover = true;
            }
        }
        if (action == 10 || action == 3) {
            this.mEatingHover = false;
        }
        return true;
    }

    public void setSplitToolbar(boolean split) {
        this.mSplitActionBar = split;
    }

    public void setSplitWhenNarrow(boolean splitWhenNarrow) {
        this.mSplitWhenNarrow = splitWhenNarrow;
    }

    public void setContentHeight(int height) {
        this.mContentHeight = height;
        requestLayout();
    }

    public int getContentHeight() {
        return this.mContentHeight;
    }

    public void setSplitView(ViewGroup splitView) {
        this.mSplitView = splitView;
    }

    public int getAnimatedVisibility() {
        if (this.mVisibilityAnim != null) {
            return this.mVisAnimListener.mFinalVisibility;
        }
        return getVisibility();
    }

    public Animator setupAnimatorToVisibility(int visibility, long duration) {
        if (this.mVisibilityAnim != null) {
            this.mVisibilityAnim.cancel();
        }
        ObjectAnimator anim;
        AnimatorSet set;
        ObjectAnimator splitAnim;
        if (visibility == 0) {
            if (getVisibility() != 0) {
                setAlpha(0.0f);
                if (!(this.mSplitView == null || this.mMenuView == null)) {
                    this.mMenuView.setAlpha(0.0f);
                }
            }
            anim = ObjectAnimator.ofFloat(this, View.ALPHA, new float[]{1.0f});
            anim.setDuration(duration);
            anim.setInterpolator(sAlphaInterpolator);
            if (this.mSplitView == null || this.mMenuView == null) {
                anim.addListener(this.mVisAnimListener.withFinalVisibility(visibility));
                return anim;
            }
            set = new AnimatorSet();
            splitAnim = ObjectAnimator.ofFloat(this.mMenuView, View.ALPHA, new float[]{1.0f});
            splitAnim.setDuration(duration);
            set.addListener(this.mVisAnimListener.withFinalVisibility(visibility));
            set.play(anim).with(splitAnim);
            return set;
        }
        anim = ObjectAnimator.ofFloat(this, View.ALPHA, new float[]{0.0f});
        anim.setDuration(duration);
        anim.setInterpolator(sAlphaInterpolator);
        if (this.mSplitView == null || this.mMenuView == null) {
            anim.addListener(this.mVisAnimListener.withFinalVisibility(visibility));
            return anim;
        }
        set = new AnimatorSet();
        splitAnim = ObjectAnimator.ofFloat(this.mMenuView, View.ALPHA, new float[]{0.0f});
        splitAnim.setDuration(duration);
        set.addListener(this.mVisAnimListener.withFinalVisibility(visibility));
        set.play(anim).with(splitAnim);
        return set;
    }

    public void animateToVisibility(int visibility) {
        setupAnimatorToVisibility(visibility, 200).start();
    }

    public void setVisibility(int visibility) {
        if (visibility != getVisibility()) {
            if (this.mVisibilityAnim != null) {
                this.mVisibilityAnim.end();
            }
            super.setVisibility(visibility);
        }
    }

    public boolean showOverflowMenu() {
        if (this.mActionMenuPresenter != null) {
            return this.mActionMenuPresenter.showOverflowMenu();
        }
        return false;
    }

    public void postShowOverflowMenu() {
        post(new Runnable() {
            public void run() {
                AbsActionBarView.this.showOverflowMenu();
            }
        });
    }

    public void showOverflowMenuPending() {
        if (this.mActionMenuPresenter != null) {
            this.mActionMenuPresenter.showOverflowMenuPending();
        }
    }

    public void postShowOverflowMenuPending() {
        post(new Runnable() {
            public void run() {
                AbsActionBarView.this.showOverflowMenuPending();
            }
        });
    }

    public boolean hideOverflowMenu() {
        if (this.mActionMenuPresenter != null) {
            return this.mActionMenuPresenter.hideOverflowMenu();
        }
        return false;
    }

    public boolean isOverflowMenuShowing() {
        if (this.mActionMenuPresenter != null) {
            return this.mActionMenuPresenter.isOverflowMenuShowing();
        }
        return false;
    }

    public boolean isOverflowMenuShowPending() {
        if (this.mActionMenuPresenter != null) {
            return this.mActionMenuPresenter.isOverflowMenuShowPending();
        }
        return false;
    }

    public boolean isOverflowReserved() {
        return this.mActionMenuPresenter != null ? this.mActionMenuPresenter.isOverflowReserved() : false;
    }

    public boolean canShowOverflowMenu() {
        return isOverflowReserved() && getVisibility() == 0;
    }

    public void dismissPopupMenus() {
        if (this.mActionMenuPresenter != null) {
            this.mActionMenuPresenter.dismissPopupMenus();
        }
    }

    protected int measureChildView(View child, int availableWidth, int childSpecHeight, int spacing) {
        child.measure(MeasureSpec.makeMeasureSpec(availableWidth, Integer.MIN_VALUE), childSpecHeight);
        return Math.max(0, (availableWidth - child.getMeasuredWidth()) - spacing);
    }

    protected static int next(int x, int val, boolean isRtl) {
        return isRtl ? x - val : x + val;
    }

    protected int positionChild(View child, int x, int y, int contentHeight, boolean reverse) {
        int childWidth = child.getMeasuredWidth();
        int childHeight = child.getMeasuredHeight();
        int childTop = y + ((contentHeight - childHeight) / 2);
        if (reverse) {
            child.layout(x - childWidth, childTop, x, childTop + childHeight);
        } else {
            child.layout(x, childTop, x + childWidth, childTop + childHeight);
        }
        return reverse ? -childWidth : childWidth;
    }
}
