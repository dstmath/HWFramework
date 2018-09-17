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
import huawei.cust.HwCfgFilePolicy;

@RemoteView
public class MessagingLinearLayout extends ViewGroup {
    private int mContractedChildId;
    private int mIndentLines;
    private int mMaxHeight;
    private int mSpacing;

    public static class LayoutParams extends MarginLayoutParams {
        boolean hide;

        public LayoutParams(Context c, AttributeSet attrs) {
            super(c, attrs);
            this.hide = false;
        }

        public LayoutParams(int width, int height) {
            super(width, height);
            this.hide = false;
        }
    }

    public MessagingLinearLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.MessagingLinearLayout, 0, 0);
        int N = a.getIndexCount();
        for (int i = 0; i < N; i++) {
            switch (a.getIndex(i)) {
                case HwCfgFilePolicy.GLOBAL /*0*/:
                    this.mSpacing = a.getDimensionPixelSize(i, 0);
                    break;
                case HwCfgFilePolicy.EMUI /*1*/:
                    this.mMaxHeight = a.getDimensionPixelSize(i, 0);
                    break;
                default:
                    break;
            }
        }
        if (this.mMaxHeight <= 0) {
            throw new IllegalStateException("MessagingLinearLayout: Must specify positive maxHeight");
        }
        a.recycle();
    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int i;
        View child;
        switch (MeasureSpec.getMode(heightMeasureSpec)) {
            case RtlSpacingHelper.UNDEFINED /*-2147483648*/:
                heightMeasureSpec = MeasureSpec.makeMeasureSpec(Math.min(this.mMaxHeight, MeasureSpec.getSize(heightMeasureSpec)), RtlSpacingHelper.UNDEFINED);
                break;
            case HwCfgFilePolicy.GLOBAL /*0*/:
                heightMeasureSpec = MeasureSpec.makeMeasureSpec(this.mMaxHeight, RtlSpacingHelper.UNDEFINED);
                break;
        }
        int targetHeight = MeasureSpec.getSize(heightMeasureSpec);
        int count = getChildCount();
        for (i = 0; i < count; i++) {
            ((LayoutParams) getChildAt(i).getLayoutParams()).hide = true;
        }
        int totalHeight = this.mPaddingTop + this.mPaddingBottom;
        boolean first = true;
        for (i = count - 1; i >= 0 && totalHeight < targetHeight; i--) {
            LayoutParams lp;
            if (getChildAt(i).getVisibility() != 8) {
                int i2;
                child = getChildAt(i);
                lp = (LayoutParams) getChildAt(i).getLayoutParams();
                if (child instanceof ImageFloatingTextView) {
                    ((ImageFloatingTextView) child).setNumIndentLines(this.mIndentLines);
                }
                measureChildWithMargins(child, widthMeasureSpec, 0, heightMeasureSpec, 0);
                int measuredHeight = lp.bottomMargin + ((totalHeight + child.getMeasuredHeight()) + lp.topMargin);
                if (first) {
                    i2 = 0;
                } else {
                    i2 = this.mSpacing;
                }
                int newHeight = Math.max(totalHeight, i2 + measuredHeight);
                first = false;
                if (newHeight <= targetHeight) {
                    totalHeight = newHeight;
                    lp.hide = false;
                }
            }
        }
        int measuredWidth = this.mPaddingLeft + this.mPaddingRight;
        int imageLines = this.mIndentLines;
        for (i = 0; i < count; i++) {
            child = getChildAt(i);
            lp = (LayoutParams) child.getLayoutParams();
            if (!(child.getVisibility() == 8 || lp.hide)) {
                if (child instanceof ImageFloatingTextView) {
                    ImageFloatingTextView textChild = (ImageFloatingTextView) child;
                    if (imageLines == 2 && textChild.getLineCount() > 2) {
                        imageLines = 3;
                    }
                    if (textChild.setNumIndentLines(Math.max(0, imageLines))) {
                        measureChildWithMargins(child, widthMeasureSpec, 0, heightMeasureSpec, 0);
                    }
                    imageLines -= textChild.getLineCount();
                }
                measuredWidth = Math.max(measuredWidth, (((child.getMeasuredWidth() + lp.leftMargin) + lp.rightMargin) + this.mPaddingLeft) + this.mPaddingRight);
            }
        }
        setMeasuredDimension(View.resolveSize(Math.max(getSuggestedMinimumWidth(), measuredWidth), widthMeasureSpec), View.resolveSize(Math.max(getSuggestedMinimumHeight(), totalHeight), heightMeasureSpec));
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
