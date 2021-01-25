package com.android.internal.widget;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ViewAnimator;
import java.util.ArrayList;

public class DialogViewAnimator extends ViewAnimator {
    private final ArrayList<View> mMatchParentChildren = new ArrayList<>(1);

    public DialogViewAnimator(Context context) {
        super(context);
    }

    public DialogViewAnimator(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    /* access modifiers changed from: protected */
    @Override // android.widget.FrameLayout, android.view.View
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int maxHeight;
        int childWidthMeasureSpec;
        int childHeightMeasureSpec;
        boolean measureMatchParentChildren = (View.MeasureSpec.getMode(widthMeasureSpec) == 1073741824 && View.MeasureSpec.getMode(heightMeasureSpec) == 1073741824) ? false : true;
        int count = getChildCount();
        int maxHeight2 = 0;
        int maxWidth = 0;
        int childState = 0;
        int i = 0;
        while (true) {
            maxHeight = -1;
            if (i >= count) {
                break;
            }
            View child = getChildAt(i);
            if (getMeasureAllChildren() || child.getVisibility() != 8) {
                FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) child.getLayoutParams();
                boolean matchWidth = lp.width == -1;
                boolean matchHeight = lp.height == -1;
                if (measureMatchParentChildren && (matchWidth || matchHeight)) {
                    this.mMatchParentChildren.add(child);
                }
                measureChildWithMargins(child, widthMeasureSpec, 0, heightMeasureSpec, 0);
                int state = 0;
                if (measureMatchParentChildren && !matchWidth) {
                    maxWidth = Math.max(maxWidth, child.getMeasuredWidth() + lp.leftMargin + lp.rightMargin);
                    state = 0 | (child.getMeasuredWidthAndState() & -16777216);
                }
                if (!measureMatchParentChildren || matchHeight) {
                    maxHeight2 = maxHeight2;
                } else {
                    maxHeight2 = Math.max(maxHeight2, child.getMeasuredHeight() + lp.topMargin + lp.bottomMargin);
                    state |= (child.getMeasuredHeightAndState() >> 16) & -256;
                }
                childState = combineMeasuredStates(childState, state);
            }
            i++;
        }
        int maxWidth2 = maxWidth + getPaddingLeft() + getPaddingRight();
        int maxHeight3 = Math.max(maxHeight2 + getPaddingTop() + getPaddingBottom(), getSuggestedMinimumHeight());
        int maxWidth3 = Math.max(maxWidth2, getSuggestedMinimumWidth());
        Drawable drawable = getForeground();
        if (drawable != null) {
            maxHeight3 = Math.max(maxHeight3, drawable.getMinimumHeight());
            maxWidth3 = Math.max(maxWidth3, drawable.getMinimumWidth());
        }
        setMeasuredDimension(resolveSizeAndState(maxWidth3, widthMeasureSpec, childState), resolveSizeAndState(maxHeight3, heightMeasureSpec, childState << 16));
        int matchCount = this.mMatchParentChildren.size();
        int i2 = 0;
        while (i2 < matchCount) {
            View child2 = this.mMatchParentChildren.get(i2);
            ViewGroup.MarginLayoutParams lp2 = (ViewGroup.MarginLayoutParams) child2.getLayoutParams();
            if (lp2.width == maxHeight) {
                childWidthMeasureSpec = View.MeasureSpec.makeMeasureSpec((((getMeasuredWidth() - getPaddingLeft()) - getPaddingRight()) - lp2.leftMargin) - lp2.rightMargin, 1073741824);
            } else {
                childWidthMeasureSpec = getChildMeasureSpec(widthMeasureSpec, getPaddingLeft() + getPaddingRight() + lp2.leftMargin + lp2.rightMargin, lp2.width);
            }
            if (lp2.height == maxHeight) {
                childHeightMeasureSpec = View.MeasureSpec.makeMeasureSpec((((getMeasuredHeight() - getPaddingTop()) - getPaddingBottom()) - lp2.topMargin) - lp2.bottomMargin, 1073741824);
            } else {
                childHeightMeasureSpec = getChildMeasureSpec(heightMeasureSpec, getPaddingTop() + getPaddingBottom() + lp2.topMargin + lp2.bottomMargin, lp2.height);
            }
            child2.measure(childWidthMeasureSpec, childHeightMeasureSpec);
            i2++;
            maxHeight = -1;
        }
        this.mMatchParentChildren.clear();
    }
}
