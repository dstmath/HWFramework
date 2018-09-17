package com.android.internal.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.RemotableViewMethod;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.ViewGroup;
import android.view.ViewGroup.MarginLayoutParams;
import android.widget.RemoteViews.RemoteView;
import com.android.internal.R;

@RemoteView
public class MessagingLinearLayout extends ViewGroup {
    private static final int NOT_MEASURED_BEFORE = -1;
    private int mContractedChildId;
    private int mIndentLines;
    private int mLastMeasuredWidth = -1;
    private int mMaxHeight;
    private int mSpacing;

    public static class LayoutParams extends MarginLayoutParams {
        boolean hide = false;

        public LayoutParams(Context c, AttributeSet attrs) {
            super(c, attrs);
        }

        public LayoutParams(int width, int height) {
            super(width, height);
        }
    }

    public MessagingLinearLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.MessagingLinearLayout, 0, 0);
        int N = a.getIndexCount();
        for (int i = 0; i < N; i++) {
            switch (a.getIndex(i)) {
                case 0:
                    this.mSpacing = a.getDimensionPixelSize(i, 0);
                    break;
                default:
                    break;
            }
        }
        a.recycle();
    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int i;
        int totalHeight;
        boolean first;
        View child;
        LayoutParams lp;
        ImageFloatingTextView textChild;
        int targetHeight = MeasureSpec.getSize(heightMeasureSpec);
        switch (MeasureSpec.getMode(heightMeasureSpec)) {
            case 0:
                targetHeight = Integer.MAX_VALUE;
                break;
        }
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        boolean recalculateVisibility = (this.mLastMeasuredWidth == -1 || getMeasuredHeight() != targetHeight) ? true : this.mLastMeasuredWidth != widthSize;
        int count = getChildCount();
        if (recalculateVisibility) {
            for (i = 0; i < count; i++) {
                ((LayoutParams) getChildAt(i).getLayoutParams()).hide = true;
            }
            totalHeight = this.mPaddingTop + this.mPaddingBottom;
            first = true;
            for (i = count - 1; i >= 0 && totalHeight < targetHeight; i--) {
                if (getChildAt(i).getVisibility() != 8) {
                    child = getChildAt(i);
                    lp = (LayoutParams) getChildAt(i).getLayoutParams();
                    textChild = null;
                    if (child instanceof ImageFloatingTextView) {
                        textChild = (ImageFloatingTextView) child;
                        textChild.setNumIndentLines(this.mIndentLines == 2 ? 3 : this.mIndentLines);
                    }
                    int spacing = first ? 0 : this.mSpacing;
                    measureChildWithMargins(child, widthMeasureSpec, 0, heightMeasureSpec, ((totalHeight - this.mPaddingTop) - this.mPaddingBottom) + spacing);
                    int childHeight = child.getMeasuredHeight();
                    int newHeight = Math.max(totalHeight, (((totalHeight + childHeight) + lp.topMargin) + lp.bottomMargin) + spacing);
                    first = false;
                    boolean measuredTooSmall = false;
                    if (textChild != null) {
                        measuredTooSmall = childHeight < (textChild.getLayoutHeight() + textChild.getPaddingTop()) + textChild.getPaddingBottom();
                    }
                    if (newHeight <= targetHeight && (measuredTooSmall ^ 1) != 0) {
                        totalHeight = newHeight;
                        lp.hide = false;
                    }
                }
            }
        }
        int measuredWidth = this.mPaddingLeft + this.mPaddingRight;
        int imageLines = this.mIndentLines;
        totalHeight = this.mPaddingTop + this.mPaddingBottom;
        first = true;
        for (i = 0; i < count; i++) {
            child = getChildAt(i);
            lp = (LayoutParams) child.getLayoutParams();
            if (!(child.getVisibility() == 8 || lp.hide)) {
                if (child instanceof ImageFloatingTextView) {
                    textChild = (ImageFloatingTextView) child;
                    if (imageLines == 2 && textChild.getLineCount() > 2) {
                        imageLines = 3;
                    }
                    if (textChild.setNumIndentLines(Math.max(0, imageLines)) || (recalculateVisibility ^ 1) != 0) {
                        child.measure(ViewGroup.getChildMeasureSpec(widthMeasureSpec, ((this.mPaddingLeft + this.mPaddingRight) + lp.leftMargin) + lp.rightMargin, lp.width), ViewGroup.getChildMeasureSpec(heightMeasureSpec, targetHeight - child.getMeasuredHeight(), lp.height));
                    }
                    imageLines -= textChild.getLineCount();
                }
                measuredWidth = Math.max(measuredWidth, (((child.getMeasuredWidth() + lp.leftMargin) + lp.rightMargin) + this.mPaddingLeft) + this.mPaddingRight);
                totalHeight = Math.max(totalHeight, (first ? 0 : this.mSpacing) + (lp.bottomMargin + ((child.getMeasuredHeight() + totalHeight) + lp.topMargin)));
                first = false;
            }
        }
        -wrap3(View.resolveSize(Math.max(getSuggestedMinimumWidth(), measuredWidth), widthMeasureSpec), View.resolveSize(Math.max(getSuggestedMinimumHeight(), totalHeight), heightMeasureSpec));
        this.mLastMeasuredWidth = widthSize;
    }

    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        int paddingLeft = this.mPaddingLeft;
        int childRight = (right - left) - this.mPaddingRight;
        int layoutDirection = getLayoutDirection();
        int count = getChildCount();
        int childTop = this.mPaddingTop;
        boolean first = true;
        for (int i = 0; i < count; i++) {
            View child = getChildAt(i);
            LayoutParams lp = (LayoutParams) child.getLayoutParams();
            if (!(child.getVisibility() == 8 || lp.hide)) {
                int childLeft;
                int childWidth = child.getMeasuredWidth();
                int childHeight = child.getMeasuredHeight();
                if (layoutDirection == 1) {
                    childLeft = (childRight - childWidth) - lp.rightMargin;
                } else {
                    childLeft = paddingLeft + lp.leftMargin;
                }
                if (!first) {
                    childTop += this.mSpacing;
                }
                childTop += lp.topMargin;
                child.layout(childLeft, childTop, childLeft + childWidth, childTop + childHeight);
                childTop += lp.bottomMargin + childHeight;
                first = false;
            }
        }
        this.mLastMeasuredWidth = -1;
    }

    protected boolean drawChild(Canvas canvas, View child, long drawingTime) {
        if (((LayoutParams) child.getLayoutParams()).hide) {
            return true;
        }
        return super.drawChild(canvas, child, drawingTime);
    }

    public LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new LayoutParams(this.mContext, attrs);
    }

    protected LayoutParams generateDefaultLayoutParams() {
        return new LayoutParams(-1, -2);
    }

    protected LayoutParams generateLayoutParams(android.view.ViewGroup.LayoutParams lp) {
        LayoutParams copy = new LayoutParams(lp.width, lp.height);
        if (lp instanceof MarginLayoutParams) {
            copy.copyMarginsFrom((MarginLayoutParams) lp);
        }
        return copy;
    }

    @RemotableViewMethod
    public void setNumIndentLines(int numberLines) {
        this.mIndentLines = numberLines;
    }

    @RemotableViewMethod
    public void setContractedChildId(int contractedChildId) {
        this.mContractedChildId = contractedChildId;
    }

    public int getContractedChildId() {
        return this.mContractedChildId;
    }
}
