package com.android.internal.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.RemotableViewMethod;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RemoteViews;
import com.android.internal.R;

@RemoteViews.RemoteView
public class MessagingLinearLayout extends ViewGroup {
    private int mMaxDisplayedLines = Integer.MAX_VALUE;
    private MessagingLayout mMessagingLayout;
    private int mSpacing;

    public MessagingLinearLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.MessagingLinearLayout, 0, 0);
        int N = a.getIndexCount();
        for (int i = 0; i < N; i++) {
            if (a.getIndex(i) == 0) {
                this.mSpacing = a.getDimensionPixelSize(i, 0);
            }
        }
        a.recycle();
    }

    /* access modifiers changed from: protected */
    @Override // android.view.View
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        MessagingChild messagingChild;
        int targetHeight = View.MeasureSpec.getMode(heightMeasureSpec) != 0 ? View.MeasureSpec.getSize(heightMeasureSpec) : Integer.MAX_VALUE;
        int measuredWidth = this.mPaddingLeft + this.mPaddingRight;
        int count = getChildCount();
        for (int i = 0; i < count; i++) {
            ((LayoutParams) getChildAt(i).getLayoutParams()).hide = true;
        }
        int measuredWidth2 = measuredWidth;
        int totalHeight = this.mPaddingTop + this.mPaddingBottom;
        boolean first = true;
        int linesRemaining = this.mMaxDisplayedLines;
        for (int i2 = count - 1; i2 >= 0 && totalHeight < targetHeight; i2--) {
            if (getChildAt(i2).getVisibility() != 8) {
                View child = getChildAt(i2);
                LayoutParams lp = (LayoutParams) getChildAt(i2).getLayoutParams();
                int spacing = this.mSpacing;
                if (child instanceof MessagingChild) {
                    MessagingChild messagingChild2 = (MessagingChild) child;
                    messagingChild2.setMaxDisplayedLines(linesRemaining);
                    spacing += messagingChild2.getExtraSpacing();
                    messagingChild = messagingChild2;
                } else {
                    messagingChild = null;
                }
                int spacing2 = first ? 0 : spacing;
                measureChildWithMargins(child, widthMeasureSpec, 0, heightMeasureSpec, ((totalHeight - this.mPaddingTop) - this.mPaddingBottom) + spacing2);
                int newHeight = Math.max(totalHeight, totalHeight + child.getMeasuredHeight() + lp.topMargin + lp.bottomMargin + spacing2);
                int measureType = 0;
                if (messagingChild != null) {
                    measureType = messagingChild.getMeasuredType();
                    linesRemaining -= messagingChild.getConsumedLines();
                }
                boolean isTooSmall = measureType == 2 && !first;
                boolean isShortened = measureType == 1 || (measureType == 2 && first);
                if (newHeight > targetHeight || isTooSmall) {
                    break;
                }
                totalHeight = newHeight;
                measuredWidth2 = Math.max(measuredWidth2, child.getMeasuredWidth() + lp.leftMargin + lp.rightMargin + this.mPaddingLeft + this.mPaddingRight);
                lp.hide = false;
                if (isShortened || linesRemaining <= 0) {
                    break;
                }
                first = false;
            }
        }
        setMeasuredDimension(resolveSize(Math.max(getSuggestedMinimumWidth(), measuredWidth2), widthMeasureSpec), Math.max(getSuggestedMinimumHeight(), totalHeight));
    }

    /* access modifiers changed from: protected */
    @Override // android.view.ViewGroup, android.view.View
    public void onLayout(boolean changed, int left, int top, int right, int bottom) {
        int width;
        int paddingLeft;
        int childLeft;
        int paddingLeft2 = this.mPaddingLeft;
        int width2 = right - left;
        int childRight = width2 - this.mPaddingRight;
        int layoutDirection = getLayoutDirection();
        int count = getChildCount();
        int childTop = this.mPaddingTop;
        boolean first = true;
        boolean shown = isShown();
        int i = 0;
        while (i < count) {
            View child = getChildAt(i);
            if (child.getVisibility() == 8) {
                paddingLeft = paddingLeft2;
                width = width2;
            } else {
                LayoutParams lp = (LayoutParams) child.getLayoutParams();
                MessagingChild messagingChild = (MessagingChild) child;
                int childWidth = child.getMeasuredWidth();
                int childHeight = child.getMeasuredHeight();
                if (layoutDirection == 1) {
                    childLeft = (childRight - childWidth) - lp.rightMargin;
                } else {
                    childLeft = paddingLeft2 + lp.leftMargin;
                }
                paddingLeft = paddingLeft2;
                if (lp.hide) {
                    if (!shown || !lp.visibleBefore) {
                        width = width2;
                    } else {
                        width = width2;
                        child.layout(childLeft, childTop, childLeft + childWidth, lp.lastVisibleHeight + childTop);
                        messagingChild.hideAnimated();
                    }
                    lp.visibleBefore = false;
                } else {
                    width = width2;
                    lp.visibleBefore = true;
                    lp.lastVisibleHeight = childHeight;
                    if (!first) {
                        childTop += this.mSpacing;
                    }
                    int childTop2 = childTop + lp.topMargin;
                    child.layout(childLeft, childTop2, childLeft + childWidth, childTop2 + childHeight);
                    childTop = childTop2 + lp.bottomMargin + childHeight;
                    first = false;
                }
            }
            i++;
            paddingLeft2 = paddingLeft;
            width2 = width;
        }
    }

    /* access modifiers changed from: protected */
    @Override // android.view.ViewGroup
    public boolean drawChild(Canvas canvas, View child, long drawingTime) {
        if (!((LayoutParams) child.getLayoutParams()).hide || ((MessagingChild) child).isHidingAnimated()) {
            return super.drawChild(canvas, child, drawingTime);
        }
        return true;
    }

    @Override // android.view.ViewGroup
    public LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new LayoutParams(this.mContext, attrs);
    }

    /* access modifiers changed from: protected */
    @Override // android.view.ViewGroup
    public LayoutParams generateDefaultLayoutParams() {
        return new LayoutParams(-1, -2);
    }

    /* access modifiers changed from: protected */
    @Override // android.view.ViewGroup
    public LayoutParams generateLayoutParams(ViewGroup.LayoutParams lp) {
        LayoutParams copy = new LayoutParams(lp.width, lp.height);
        if (lp instanceof ViewGroup.MarginLayoutParams) {
            copy.copyMarginsFrom((ViewGroup.MarginLayoutParams) lp);
        }
        return copy;
    }

    public static boolean isGone(View view) {
        if (view.getVisibility() == 8) {
            return true;
        }
        ViewGroup.LayoutParams lp = view.getLayoutParams();
        if (!(lp instanceof LayoutParams) || !((LayoutParams) lp).hide) {
            return false;
        }
        return true;
    }

    @RemotableViewMethod
    public void setMaxDisplayedLines(int numberLines) {
        this.mMaxDisplayedLines = numberLines;
    }

    public void setMessagingLayout(MessagingLayout layout) {
        this.mMessagingLayout = layout;
    }

    public MessagingLayout getMessagingLayout() {
        return this.mMessagingLayout;
    }

    public interface MessagingChild {
        public static final int MEASURED_NORMAL = 0;
        public static final int MEASURED_SHORTENED = 1;
        public static final int MEASURED_TOO_SMALL = 2;

        int getConsumedLines();

        int getMeasuredType();

        void hideAnimated();

        boolean isHidingAnimated();

        void setMaxDisplayedLines(int i);

        default int getExtraSpacing() {
            return 0;
        }
    }

    public static class LayoutParams extends ViewGroup.MarginLayoutParams {
        public boolean hide = false;
        public int lastVisibleHeight;
        public boolean visibleBefore = false;

        public LayoutParams(Context c, AttributeSet attrs) {
            super(c, attrs);
        }

        public LayoutParams(int width, int height) {
            super(width, height);
        }
    }
}
