package com.android.internal.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.MeasureSpec;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import com.android.internal.R;

public class ButtonBarLayout extends LinearLayout {
    private static final int ALLOW_STACKING_MIN_HEIGHT_DP = 320;
    private static final int PEEK_BUTTON_DP = 16;
    private boolean mAllowStacking;
    private int mLastWidthSize = -1;
    private int mMinimumHeight = 0;

    public ButtonBarLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        boolean allowStackingDefault = context.getResources().getConfiguration().screenHeightDp >= 320;
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.ButtonBarLayout);
        this.mAllowStacking = ta.getBoolean(0, allowStackingDefault);
        ta.recycle();
    }

    public void setAllowStacking(boolean allowStacking) {
        if (this.mAllowStacking != allowStacking) {
            this.mAllowStacking = allowStacking;
            if (!this.mAllowStacking && getOrientation() == 1) {
                setStacked(false);
            }
            requestLayout();
        }
    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int initialWidthMeasureSpec;
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        if (this.mAllowStacking) {
            if (widthSize > this.mLastWidthSize && isStacked()) {
                setStacked(false);
            }
            this.mLastWidthSize = widthSize;
        }
        boolean needsRemeasure = false;
        if (isStacked() || MeasureSpec.getMode(widthMeasureSpec) != 1073741824) {
            initialWidthMeasureSpec = widthMeasureSpec;
        } else {
            initialWidthMeasureSpec = MeasureSpec.makeMeasureSpec(widthSize, Integer.MIN_VALUE);
            needsRemeasure = true;
        }
        super.onMeasure(initialWidthMeasureSpec, heightMeasureSpec);
        if (this.mAllowStacking && (isStacked() ^ 1) != 0 && (getMeasuredWidthAndState() & -16777216) == 16777216) {
            setStacked(true);
            needsRemeasure = true;
        }
        if (needsRemeasure) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        }
        int minHeight = 0;
        int firstVisible = getNextVisibleChildIndex(0);
        if (firstVisible >= 0) {
            View firstButton = getChildAt(firstVisible);
            LayoutParams firstParams = (LayoutParams) firstButton.getLayoutParams();
            minHeight = (((getPaddingTop() + firstButton.getMeasuredHeight()) + firstParams.topMargin) + firstParams.bottomMargin) + 0;
            if (isStacked()) {
                int secondVisible = getNextVisibleChildIndex(firstVisible + 1);
                if (secondVisible >= 0) {
                    minHeight = (int) (((float) minHeight) + (((float) getChildAt(secondVisible).getPaddingTop()) + (getResources().getDisplayMetrics().density * 16.0f)));
                }
            } else {
                minHeight += getPaddingBottom();
            }
        }
        if (getMinimumHeight() != minHeight) {
            setMinimumHeight(minHeight);
        }
    }

    private int getNextVisibleChildIndex(int index) {
        int count = getChildCount();
        for (int i = index; i < count; i++) {
            if (getChildAt(i).getVisibility() == 0) {
                return i;
            }
        }
        return -1;
    }

    public int getMinimumHeight() {
        return Math.max(this.mMinimumHeight, super.getMinimumHeight());
    }

    private void setStacked(boolean stacked) {
        int i = 0;
        if (stacked) {
            i = 1;
        }
        setOrientation(i);
        setGravity(stacked ? 5 : 80);
        View spacer = findViewById(R.id.spacer);
        if (spacer != null) {
            spacer.setVisibility(stacked ? 8 : 4);
        }
        for (int i2 = getChildCount() - 2; i2 >= 0; i2--) {
            bringChildToFront(getChildAt(i2));
        }
    }

    private boolean isStacked() {
        return getOrientation() == 1;
    }
}
