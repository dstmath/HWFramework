package com.android.internal.widget;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.hwcontrol.HwWidgetFactory;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

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

    /* access modifiers changed from: protected */
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (!tryOnMeasure(widthMeasureSpec, heightMeasureSpec)) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        }
    }

    private boolean tryOnMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int childHeightSpec;
        int i = widthMeasureSpec;
        int i2 = heightMeasureSpec;
        int count = getChildCount();
        View middlePanel = null;
        View buttonPanel = null;
        View topPanel = null;
        for (int i3 = 0; i3 < count; i3++) {
            View child = getChildAt(i3);
            if (child.getVisibility() != 8) {
                int id = child.getId();
                if (id == 16908786) {
                    buttonPanel = child;
                } else if (id == 16908832 || id == 16908839) {
                    if (middlePanel != null) {
                        return false;
                    }
                    middlePanel = child;
                } else if (id != 16909457) {
                    return false;
                } else {
                    topPanel = child;
                }
            }
        }
        int heightMode = View.MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = View.MeasureSpec.getSize(heightMeasureSpec);
        int widthMode = View.MeasureSpec.getMode(widthMeasureSpec);
        int childState = 0;
        int usedHeight = getPaddingTop() + getPaddingBottom();
        if (topPanel != null) {
            topPanel.measure(i, 0);
            usedHeight += topPanel.getMeasuredHeight();
            childState = combineMeasuredStates(0, topPanel.getMeasuredState());
        }
        int buttonHeight = 0;
        int buttonWantsHeight = 0;
        if (buttonPanel != null) {
            buttonPanel.measure(i, 0);
            buttonHeight = resolveMinimumHeight(buttonPanel);
            if (HwWidgetFactory.isHwTheme(this.mContext)) {
                buttonHeight = buttonPanel.getMeasuredHeight();
            }
            buttonWantsHeight = buttonPanel.getMeasuredHeight() - buttonHeight;
            usedHeight += buttonHeight;
            childState = combineMeasuredStates(childState, buttonPanel.getMeasuredState());
        }
        int middleHeight = 0;
        if (middlePanel != null) {
            if (heightMode == 0) {
                childHeightSpec = 0;
                View view = topPanel;
            } else {
                View view2 = topPanel;
                childHeightSpec = View.MeasureSpec.makeMeasureSpec(Math.max(0, heightSize - usedHeight), heightMode);
            }
            middlePanel.measure(i, childHeightSpec);
            middleHeight = middlePanel.getMeasuredHeight();
            usedHeight += middleHeight;
            childState = combineMeasuredStates(childState, middlePanel.getMeasuredState());
        }
        int remainingHeight = heightSize - usedHeight;
        if (buttonPanel != null) {
            int usedHeight2 = usedHeight - buttonHeight;
            int heightToGive = Math.min(remainingHeight, buttonWantsHeight);
            if (heightToGive > 0) {
                remainingHeight -= heightToGive;
                buttonHeight += heightToGive;
            }
            buttonPanel.measure(i, View.MeasureSpec.makeMeasureSpec(buttonHeight, 1073741824));
            usedHeight = usedHeight2 + buttonPanel.getMeasuredHeight();
            childState = combineMeasuredStates(childState, buttonPanel.getMeasuredState());
            remainingHeight = remainingHeight;
        }
        if (middlePanel == null || remainingHeight <= 0) {
        } else {
            int heightToGive2 = remainingHeight;
            middlePanel.measure(i, View.MeasureSpec.makeMeasureSpec(middleHeight + heightToGive2, heightMode));
            usedHeight = (usedHeight - middleHeight) + middlePanel.getMeasuredHeight();
            int i4 = heightMode;
            childState = combineMeasuredStates(childState, middlePanel.getMeasuredState());
            remainingHeight -= heightToGive2;
        }
        int maxWidth = 0;
        int i5 = 0;
        while (i5 < count) {
            int remainingHeight2 = remainingHeight;
            View child2 = getChildAt(i5);
            View buttonPanel2 = buttonPanel;
            View middlePanel2 = middlePanel;
            if (child2.getVisibility() != 8) {
                maxWidth = Math.max(maxWidth, child2.getMeasuredWidth());
            }
            i5++;
            remainingHeight = remainingHeight2;
            buttonPanel = buttonPanel2;
            middlePanel = middlePanel2;
        }
        View view3 = buttonPanel;
        View view4 = middlePanel;
        setMeasuredDimension(resolveSizeAndState(maxWidth + getPaddingLeft() + getPaddingRight(), i, childState), resolveSizeAndState(usedHeight, i2, 0));
        if (widthMode != 1073741824) {
            forceUniformWidth(count, i2);
        }
        return true;
    }

    private void forceUniformWidth(int count, int heightMeasureSpec) {
        int uniformMeasureSpec = View.MeasureSpec.makeMeasureSpec(getMeasuredWidth(), 1073741824);
        for (int i = 0; i < count; i++) {
            View child = getChildAt(i);
            if (child.getVisibility() != 8) {
                LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) child.getLayoutParams();
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

    /* access modifiers changed from: protected */
    public void onLayout(boolean changed, int left, int top, int right, int bottom) {
        int childTop;
        int count;
        int i;
        int majorGravity;
        Drawable dividerDrawable;
        int childLeft;
        AlertDialogLayout alertDialogLayout = this;
        int paddingLeft = alertDialogLayout.mPaddingLeft;
        int width = right - left;
        int childRight = width - alertDialogLayout.mPaddingRight;
        int childSpace = (width - paddingLeft) - alertDialogLayout.mPaddingRight;
        int totalLength = getMeasuredHeight();
        int count2 = getChildCount();
        int gravity = getGravity();
        int majorGravity2 = gravity & 112;
        int minorGravity = gravity & 8388615;
        if (majorGravity2 == 16) {
            childTop = alertDialogLayout.mPaddingTop + (((bottom - top) - totalLength) / 2);
        } else if (majorGravity2 != 80) {
            childTop = alertDialogLayout.mPaddingTop;
        } else {
            childTop = ((alertDialogLayout.mPaddingTop + bottom) - top) - totalLength;
        }
        Drawable dividerDrawable2 = getDividerDrawable();
        int i2 = 0;
        int dividerHeight = dividerDrawable2 == null ? 0 : dividerDrawable2.getIntrinsicHeight();
        while (i2 < count2) {
            View child = alertDialogLayout.getChildAt(i2);
            if (child != null) {
                dividerDrawable = dividerDrawable2;
                majorGravity = majorGravity2;
                if (child.getVisibility() != 8) {
                    int childWidth = child.getMeasuredWidth();
                    int childHeight = child.getMeasuredHeight();
                    LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) child.getLayoutParams();
                    int layoutGravity = lp.gravity;
                    if (layoutGravity < 0) {
                        layoutGravity = minorGravity;
                    }
                    View child2 = child;
                    int absoluteGravity = Gravity.getAbsoluteGravity(layoutGravity, getLayoutDirection()) & 7;
                    int i3 = layoutGravity;
                    if (absoluteGravity != 1) {
                        childLeft = absoluteGravity != 5 ? lp.leftMargin + paddingLeft : (childRight - childWidth) - lp.rightMargin;
                    } else {
                        childLeft = ((((childSpace - childWidth) / 2) + paddingLeft) + lp.leftMargin) - lp.rightMargin;
                    }
                    if (alertDialogLayout.hasDividerBeforeChildAt(i2)) {
                        childTop += dividerHeight;
                    }
                    int childTop2 = childTop + lp.topMargin;
                    i = i2;
                    count = count2;
                    alertDialogLayout.setChildFrame(child2, childLeft, childTop2, childWidth, childHeight);
                    childTop = childTop2 + childHeight + lp.bottomMargin;
                } else {
                    i = i2;
                    count = count2;
                }
            } else {
                i = i2;
                dividerDrawable = dividerDrawable2;
                majorGravity = majorGravity2;
                count = count2;
            }
            i2 = i + 1;
            dividerDrawable2 = dividerDrawable;
            majorGravity2 = majorGravity;
            count2 = count;
            alertDialogLayout = this;
        }
        int i4 = majorGravity2;
        int i5 = count2;
    }

    private void setChildFrame(View child, int left, int top, int width, int height) {
        child.layout(left, top, left + width, top + height);
    }
}
