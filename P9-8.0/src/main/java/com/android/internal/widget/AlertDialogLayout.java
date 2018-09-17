package com.android.internal.widget;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import com.android.internal.R;

public class AlertDialogLayout extends LinearLayout {
    public AlertDialogLayout(Context context) {
        super(context);
    }

    public AlertDialogLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public AlertDialogLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public AlertDialogLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (!tryOnMeasure(widthMeasureSpec, heightMeasureSpec)) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        }
    }

    private boolean tryOnMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int i;
        View child;
        int heightToGive;
        View topPanel = null;
        View buttonPanel = null;
        View middlePanel = null;
        int count = getChildCount();
        for (i = 0; i < count; i++) {
            child = getChildAt(i);
            if (child.getVisibility() != 8) {
                switch (child.getId()) {
                    case R.id.buttonPanel /*16908768*/:
                        buttonPanel = child;
                        break;
                    case R.id.contentPanel /*16908810*/:
                    case R.id.customPanel /*16908817*/:
                        if (middlePanel == null) {
                            middlePanel = child;
                            break;
                        }
                        return false;
                    case R.id.topPanel /*16909400*/:
                        topPanel = child;
                        break;
                    default:
                        return false;
                }
            }
        }
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int childState = 0;
        int usedHeight = getPaddingTop() + getPaddingBottom();
        if (topPanel != null) {
            topPanel.measure(widthMeasureSpec, 0);
            usedHeight += topPanel.getMeasuredHeight();
            childState = View.combineMeasuredStates(0, topPanel.getMeasuredState());
        }
        int buttonHeight = 0;
        int buttonWantsHeight = 0;
        if (buttonPanel != null) {
            buttonPanel.measure(widthMeasureSpec, 0);
            buttonHeight = resolveMinimumHeight(buttonPanel);
            buttonWantsHeight = buttonPanel.getMeasuredHeight() - buttonHeight;
            usedHeight += buttonHeight;
            childState = View.combineMeasuredStates(childState, buttonPanel.getMeasuredState());
        }
        int middleHeight = 0;
        if (middlePanel != null) {
            int childHeightSpec;
            if (heightMode == 0) {
                childHeightSpec = 0;
            } else {
                childHeightSpec = MeasureSpec.makeMeasureSpec(Math.max(0, heightSize - usedHeight), heightMode);
            }
            middlePanel.measure(widthMeasureSpec, childHeightSpec);
            middleHeight = middlePanel.getMeasuredHeight();
            usedHeight += middleHeight;
            childState = View.combineMeasuredStates(childState, middlePanel.getMeasuredState());
        }
        int remainingHeight = heightSize - usedHeight;
        if (buttonPanel != null) {
            usedHeight -= buttonHeight;
            heightToGive = Math.min(remainingHeight, buttonWantsHeight);
            if (heightToGive > 0) {
                remainingHeight -= heightToGive;
                buttonHeight += heightToGive;
            }
            buttonPanel.measure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(buttonHeight, 1073741824));
            usedHeight += buttonPanel.getMeasuredHeight();
            childState = View.combineMeasuredStates(childState, buttonPanel.getMeasuredState());
        }
        if (middlePanel != null && remainingHeight > 0) {
            usedHeight -= middleHeight;
            heightToGive = remainingHeight;
            remainingHeight -= remainingHeight;
            middlePanel.measure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(middleHeight + heightToGive, heightMode));
            usedHeight += middlePanel.getMeasuredHeight();
            childState = View.combineMeasuredStates(childState, middlePanel.getMeasuredState());
        }
        int maxWidth = 0;
        for (i = 0; i < count; i++) {
            child = getChildAt(i);
            if (child.getVisibility() != 8) {
                maxWidth = Math.max(maxWidth, child.getMeasuredWidth());
            }
        }
        -wrap6(View.resolveSizeAndState(maxWidth + (getPaddingLeft() + getPaddingRight()), widthMeasureSpec, childState), View.resolveSizeAndState(usedHeight, heightMeasureSpec, 0));
        if (widthMode != 1073741824) {
            forceUniformWidth(count, heightMeasureSpec);
        }
        return true;
    }

    private void forceUniformWidth(int count, int heightMeasureSpec) {
        int uniformMeasureSpec = MeasureSpec.makeMeasureSpec(getMeasuredWidth(), 1073741824);
        for (int i = 0; i < count; i++) {
            View child = getChildAt(i);
            if (child.getVisibility() != 8) {
                LayoutParams lp = (LayoutParams) child.getLayoutParams();
                if (lp.width == -1) {
                    int oldHeight = lp.height;
                    lp.height = child.getMeasuredHeight();
                    measureChildWithMargins(child, uniformMeasureSpec, 0, heightMeasureSpec, 0);
                    lp.height = oldHeight;
                }
            }
        }
    }

    private int resolveMinimumHeight(View v) {
        int minHeight = v.getMinimumHeight();
        if (minHeight > 0) {
            return minHeight;
        }
        if (v instanceof ViewGroup) {
            ViewGroup vg = (ViewGroup) v;
            if (vg.getChildCount() == 1) {
                return resolveMinimumHeight(vg.getChildAt(0));
            }
        }
        return 0;
    }

    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        int childTop;
        int paddingLeft = this.mPaddingLeft;
        int width = right - left;
        int childRight = width - this.mPaddingRight;
        int childSpace = (width - paddingLeft) - this.mPaddingRight;
        int totalLength = getMeasuredHeight();
        int count = getChildCount();
        int gravity = getGravity();
        int minorGravity = gravity & Gravity.RELATIVE_HORIZONTAL_GRAVITY_MASK;
        switch (gravity & 112) {
            case 16:
                childTop = this.mPaddingTop + (((bottom - top) - totalLength) / 2);
                break;
            case 80:
                childTop = ((this.mPaddingTop + bottom) - top) - totalLength;
                break;
            default:
                childTop = this.mPaddingTop;
                break;
        }
        Drawable dividerDrawable = getDividerDrawable();
        int dividerHeight = dividerDrawable == null ? 0 : dividerDrawable.getIntrinsicHeight();
        for (int i = 0; i < count; i++) {
            View child = getChildAt(i);
            if (!(child == null || child.getVisibility() == 8)) {
                int childLeft;
                int childWidth = child.getMeasuredWidth();
                int childHeight = child.getMeasuredHeight();
                LayoutParams lp = (LayoutParams) child.getLayoutParams();
                int layoutGravity = lp.gravity;
                if (layoutGravity < 0) {
                    layoutGravity = minorGravity;
                }
                switch (Gravity.getAbsoluteGravity(layoutGravity, getLayoutDirection()) & 7) {
                    case 1:
                        childLeft = ((((childSpace - childWidth) / 2) + paddingLeft) + lp.leftMargin) - lp.rightMargin;
                        break;
                    case 5:
                        childLeft = (childRight - childWidth) - lp.rightMargin;
                        break;
                    default:
                        childLeft = paddingLeft + lp.leftMargin;
                        break;
                }
                if (hasDividerBeforeChildAt(i)) {
                    childTop += dividerHeight;
                }
                childTop += lp.topMargin;
                setChildFrame(child, childLeft, childTop, childWidth, childHeight);
                childTop += lp.bottomMargin + childHeight;
            }
        }
    }

    private void setChildFrame(View child, int left, int top, int width, int height) {
        child.layout(left, top, left + width, top + height);
    }
}
