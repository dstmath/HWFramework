package com.android.internal.widget;

import android.content.Context;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.ViewGroup;
import android.view.ViewGroup.MarginLayoutParams;
import android.view.ViewTreeObserver;
import android.view.ViewTreeObserver.OnScrollChangedListener;
import android.widget.FrameLayout;
import android.widget.FrameLayout.LayoutParams;
import android.widget.ListView;
import java.util.ArrayList;

public class WatchListDecorLayout extends FrameLayout implements OnScrollChangedListener {
    private View mBottomPanel;
    private int mForegroundPaddingBottom = 0;
    private int mForegroundPaddingLeft = 0;
    private int mForegroundPaddingRight = 0;
    private int mForegroundPaddingTop = 0;
    private ListView mListView;
    private final ArrayList<View> mMatchParentChildren = new ArrayList(1);
    private ViewTreeObserver mObserver;
    private int mPendingScroll;
    private View mTopPanel;

    public WatchListDecorLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public WatchListDecorLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public WatchListDecorLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        this.mPendingScroll = 0;
        for (int i = 0; i < getChildCount(); i++) {
            View child = getChildAt(i);
            if (!(child instanceof ListView)) {
                int gravity = ((LayoutParams) child.getLayoutParams()).gravity & 112;
                if (gravity == 48 && this.mTopPanel == null) {
                    this.mTopPanel = child;
                } else if (gravity == 80 && this.mBottomPanel == null) {
                    this.mBottomPanel = child;
                }
            } else if (this.mListView != null) {
                throw new IllegalArgumentException("only one ListView child allowed");
            } else {
                this.mListView = (ListView) child;
                this.mListView.setNestedScrollingEnabled(true);
                this.mObserver = this.mListView.getViewTreeObserver();
                this.mObserver.addOnScrollChangedListener(this);
            }
        }
    }

    public void onDetachedFromWindow() {
        this.mListView = null;
        this.mBottomPanel = null;
        this.mTopPanel = null;
        if (this.mObserver != null) {
            if (this.mObserver.isAlive()) {
                this.mObserver.removeOnScrollChangedListener(this);
            }
            this.mObserver = null;
        }
    }

    private void applyMeasureToChild(View child, int widthMeasureSpec, int heightMeasureSpec) {
        int childWidthMeasureSpec;
        int childHeightMeasureSpec;
        MarginLayoutParams lp = (MarginLayoutParams) child.getLayoutParams();
        if (lp.width == -1) {
            childWidthMeasureSpec = MeasureSpec.makeMeasureSpec(Math.max(0, (((getMeasuredWidth() - getPaddingLeftWithForeground()) - getPaddingRightWithForeground()) - lp.leftMargin) - lp.rightMargin), 1073741824);
        } else {
            childWidthMeasureSpec = ViewGroup.getChildMeasureSpec(widthMeasureSpec, ((getPaddingLeftWithForeground() + getPaddingRightWithForeground()) + lp.leftMargin) + lp.rightMargin, lp.width);
        }
        if (lp.height == -1) {
            childHeightMeasureSpec = MeasureSpec.makeMeasureSpec(Math.max(0, (((getMeasuredHeight() - getPaddingTopWithForeground()) - getPaddingBottomWithForeground()) - lp.topMargin) - lp.bottomMargin), 1073741824);
        } else {
            childHeightMeasureSpec = ViewGroup.getChildMeasureSpec(heightMeasureSpec, ((getPaddingTopWithForeground() + getPaddingBottomWithForeground()) + lp.topMargin) + lp.bottomMargin, lp.height);
        }
        child.measure(childWidthMeasureSpec, childHeightMeasureSpec);
    }

    private int measureAndGetHeight(View child, int widthMeasureSpec, int heightMeasureSpec) {
        if (child != null) {
            if (child.getVisibility() != 8) {
                applyMeasureToChild(this.mBottomPanel, widthMeasureSpec, heightMeasureSpec);
                return child.getMeasuredHeight();
            } else if (getMeasureAllChildren()) {
                applyMeasureToChild(this.mBottomPanel, widthMeasureSpec, heightMeasureSpec);
            }
        }
        return 0;
    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int i;
        View child;
        int count = getChildCount();
        boolean measureMatchParentChildren = MeasureSpec.getMode(widthMeasureSpec) == 1073741824 ? MeasureSpec.getMode(heightMeasureSpec) != 1073741824 : true;
        this.mMatchParentChildren.clear();
        int maxHeight = 0;
        int maxWidth = 0;
        int childState = 0;
        for (i = 0; i < count; i++) {
            child = getChildAt(i);
            if (getMeasureAllChildren() || child.getVisibility() != 8) {
                measureChildWithMargins(child, widthMeasureSpec, 0, heightMeasureSpec, 0);
                LayoutParams lp = (LayoutParams) child.getLayoutParams();
                maxWidth = Math.max(maxWidth, (child.getMeasuredWidth() + lp.leftMargin) + lp.rightMargin);
                maxHeight = Math.max(maxHeight, (child.getMeasuredHeight() + lp.topMargin) + lp.bottomMargin);
                childState = View.combineMeasuredStates(childState, child.getMeasuredState());
                if (measureMatchParentChildren && (lp.width == -1 || lp.height == -1)) {
                    this.mMatchParentChildren.add(child);
                }
            }
        }
        maxWidth += getPaddingLeftWithForeground() + getPaddingRightWithForeground();
        maxHeight = Math.max(maxHeight + (getPaddingTopWithForeground() + getPaddingBottomWithForeground()), getSuggestedMinimumHeight());
        maxWidth = Math.max(maxWidth, getSuggestedMinimumWidth());
        Drawable drawable = getForeground();
        if (drawable != null) {
            maxHeight = Math.max(maxHeight, drawable.getMinimumHeight());
            maxWidth = Math.max(maxWidth, drawable.getMinimumWidth());
        }
        -wrap3(View.resolveSizeAndState(maxWidth, widthMeasureSpec, childState), View.resolveSizeAndState(maxHeight, heightMeasureSpec, childState << 16));
        if (this.mListView != null) {
            if (this.mPendingScroll != 0) {
                this.mListView.scrollListBy(this.mPendingScroll);
                this.mPendingScroll = 0;
            }
            int paddingTop = Math.max(this.mListView.getPaddingTop(), measureAndGetHeight(this.mTopPanel, widthMeasureSpec, heightMeasureSpec));
            int paddingBottom = Math.max(this.mListView.getPaddingBottom(), measureAndGetHeight(this.mBottomPanel, widthMeasureSpec, heightMeasureSpec));
            if (!(paddingTop == this.mListView.getPaddingTop() && paddingBottom == this.mListView.getPaddingBottom())) {
                this.mPendingScroll += this.mListView.getPaddingTop() - paddingTop;
                this.mListView.setPadding(this.mListView.getPaddingLeft(), paddingTop, this.mListView.getPaddingRight(), paddingBottom);
            }
        }
        count = this.mMatchParentChildren.size();
        if (count > 1) {
            for (i = 0; i < count; i++) {
                child = (View) this.mMatchParentChildren.get(i);
                if (this.mListView == null || !(child == this.mTopPanel || child == this.mBottomPanel)) {
                    applyMeasureToChild(child, widthMeasureSpec, heightMeasureSpec);
                }
            }
        }
    }

    public void setForegroundGravity(int foregroundGravity) {
        if (getForegroundGravity() != foregroundGravity) {
            super.setForegroundGravity(foregroundGravity);
            Drawable foreground = getForeground();
            if (getForegroundGravity() != 119 || foreground == null) {
                this.mForegroundPaddingLeft = 0;
                this.mForegroundPaddingTop = 0;
                this.mForegroundPaddingRight = 0;
                this.mForegroundPaddingBottom = 0;
                return;
            }
            Rect padding = new Rect();
            if (foreground.getPadding(padding)) {
                this.mForegroundPaddingLeft = padding.left;
                this.mForegroundPaddingTop = padding.top;
                this.mForegroundPaddingRight = padding.right;
                this.mForegroundPaddingBottom = padding.bottom;
            }
        }
    }

    private int getPaddingLeftWithForeground() {
        if (isForegroundInsidePadding()) {
            return Math.max(this.mPaddingLeft, this.mForegroundPaddingLeft);
        }
        return this.mPaddingLeft + this.mForegroundPaddingLeft;
    }

    private int getPaddingRightWithForeground() {
        if (isForegroundInsidePadding()) {
            return Math.max(this.mPaddingRight, this.mForegroundPaddingRight);
        }
        return this.mPaddingRight + this.mForegroundPaddingRight;
    }

    private int getPaddingTopWithForeground() {
        if (isForegroundInsidePadding()) {
            return Math.max(this.mPaddingTop, this.mForegroundPaddingTop);
        }
        return this.mPaddingTop + this.mForegroundPaddingTop;
    }

    private int getPaddingBottomWithForeground() {
        if (isForegroundInsidePadding()) {
            return Math.max(this.mPaddingBottom, this.mForegroundPaddingBottom);
        }
        return this.mPaddingBottom + this.mForegroundPaddingBottom;
    }

    public void onScrollChanged() {
        if (this.mListView != null) {
            if (this.mTopPanel != null) {
                if (this.mListView.getChildCount() <= 0) {
                    setScrolling(this.mTopPanel, 0.0f);
                } else if (this.mListView.getFirstVisiblePosition() == 0) {
                    setScrolling(this.mTopPanel, (this.mListView.getChildAt(0).getY() - ((float) this.mTopPanel.getHeight())) - ((float) this.mTopPanel.getTop()));
                } else {
                    setScrolling(this.mTopPanel, (float) (-this.mTopPanel.getHeight()));
                }
            }
            if (this.mBottomPanel != null) {
                if (this.mListView.getChildCount() <= 0) {
                    setScrolling(this.mBottomPanel, 0.0f);
                } else if (this.mListView.getLastVisiblePosition() >= this.mListView.getCount() - 1) {
                    View lastChild = this.mListView.getChildAt(this.mListView.getChildCount() - 1);
                    setScrolling(this.mBottomPanel, Math.max(0.0f, (lastChild.getY() + ((float) lastChild.getHeight())) - ((float) this.mBottomPanel.getTop())));
                } else {
                    setScrolling(this.mBottomPanel, (float) this.mBottomPanel.getHeight());
                }
            }
        }
    }

    private void setScrolling(View panel, float translationY) {
        if (panel.getTranslationY() != translationY) {
            panel.setTranslationY(translationY);
        }
    }
}
