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

    public interface MessagingChild {
        public static final int MEASURED_NORMAL = 0;
        public static final int MEASURED_SHORTENED = 1;
        public static final int MEASURED_TOO_SMALL = 2;

        int getConsumedLines();

        int getMeasuredType();

        void hideAnimated();

        boolean isHidingAnimated();

        void setMaxDisplayedLines(int i);

        int getExtraSpacing() {
            return 0;
        }
    }

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
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int count;
        int targetHeight = View.MeasureSpec.getSize(heightMeasureSpec);
        if (View.MeasureSpec.getMode(heightMeasureSpec) == 0) {
            targetHeight = Integer.MAX_VALUE;
        }
        int targetHeight2 = targetHeight;
        int measuredWidth = this.mPaddingLeft + this.mPaddingRight;
        int count2 = getChildCount();
        for (int i = 0; i < count2; i++) {
            ((LayoutParams) getChildAt(i).getLayoutParams()).hide = true;
        }
        int i2 = count2 - 1;
        int measuredWidth2 = measuredWidth;
        int totalHeight = this.mPaddingTop + this.mPaddingBottom;
        boolean first = true;
        int linesRemaining = this.mMaxDisplayedLines;
        while (true) {
            int i3 = i2;
            if (i3 < 0 || totalHeight >= targetHeight2) {
            } else {
                if (getChildAt(i3).getVisibility() != 8) {
                    View child = getChildAt(i3);
                    LayoutParams lp = (LayoutParams) getChildAt(i3).getLayoutParams();
                    MessagingChild messagingChild = null;
                    int spacing = this.mSpacing;
                    if (child instanceof MessagingChild) {
                        messagingChild = (MessagingChild) child;
                        messagingChild.setMaxDisplayedLines(linesRemaining);
                        spacing += messagingChild.getExtraSpacing();
                    }
                    MessagingChild messagingChild2 = messagingChild;
                    int spacing2 = first ? 0 : spacing;
                    MessagingChild messagingChild3 = messagingChild2;
                    LayoutParams lp2 = lp;
                    count = count2;
                    View child2 = child;
                    measureChildWithMargins(child, widthMeasureSpec, 0, heightMeasureSpec, ((totalHeight - this.mPaddingTop) - this.mPaddingBottom) + spacing2);
                    int newHeight = Math.max(totalHeight, totalHeight + child2.getMeasuredHeight() + lp2.topMargin + lp2.bottomMargin + spacing2);
                    first = false;
                    int measureType = 0;
                    if (messagingChild3 != null) {
                        measureType = messagingChild3.getMeasuredType();
                        linesRemaining -= messagingChild3.getConsumedLines();
                    }
                    boolean isShortened = measureType == 1;
                    boolean isTooSmall = measureType == 2;
                    if (newHeight > targetHeight2 || isTooSmall) {
                        break;
                    }
                    totalHeight = newHeight;
                    measuredWidth2 = Math.max(measuredWidth2, child2.getMeasuredWidth() + lp2.leftMargin + lp2.rightMargin + this.mPaddingLeft + this.mPaddingRight);
                    lp2.hide = false;
                    if (isShortened || linesRemaining <= 0) {
                        break;
                    }
                } else {
                    count = count2;
                }
                i2 = i3 - 1;
                count2 = count;
            }
        }
        setMeasuredDimension(resolveSize(Math.max(getSuggestedMinimumWidth(), measuredWidth2), widthMeasureSpec), Math.max(getSuggestedMinimumHeight(), totalHeight));
    }

    /* access modifiers changed from: protected */
    public void onLayout(boolean changed, int left, int top, int right, int bottom) {
        int paddingLeft;
        int childLeft;
        int paddingLeft2 = this.mPaddingLeft;
        int childRight = (right - left) - this.mPaddingRight;
        int layoutDirection = getLayoutDirection();
        int count = getChildCount();
        int childTop = this.mPaddingTop;
        boolean shown = isShown();
        boolean first = true;
        int childTop2 = childTop;
        int i = 0;
        while (i < count) {
            View child = getChildAt(i);
            if (child.getVisibility() == 8) {
                paddingLeft = paddingLeft2;
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
                int childLeft2 = childLeft;
                paddingLeft = paddingLeft2;
                if (lp.hide != 0) {
                    if (shown && lp.visibleBefore) {
                        child.layout(childLeft2, childTop2, childLeft2 + childWidth, lp.lastVisibleHeight + childTop2);
                        messagingChild.hideAnimated();
                    }
                    lp.visibleBefore = false;
                } else {
                    lp.visibleBefore = true;
                    lp.lastVisibleHeight = childHeight;
                    if (!first) {
                        childTop2 += this.mSpacing;
                    }
                    int childTop3 = childTop2 + lp.topMargin;
                    child.layout(childLeft2, childTop3, childLeft2 + childWidth, childTop3 + childHeight);
                    childTop2 = childTop3 + lp.bottomMargin + childHeight;
                    first = false;
                }
            }
            i++;
            paddingLeft2 = paddingLeft;
        }
    }

    /* access modifiers changed from: protected */
    public boolean drawChild(Canvas canvas, View child, long drawingTime) {
        if (!((LayoutParams) child.getLayoutParams()).hide || ((MessagingChild) child).isHidingAnimated()) {
            return super.drawChild(canvas, child, drawingTime);
        }
        return true;
    }

    public LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new LayoutParams(this.mContext, attrs);
    }

    /* access modifiers changed from: protected */
    public LayoutParams generateDefaultLayoutParams() {
        return new LayoutParams(-1, -2);
    }

    /* access modifiers changed from: protected */
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
}
