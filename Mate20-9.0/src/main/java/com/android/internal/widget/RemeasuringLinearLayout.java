package com.android.internal.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RemoteViews;

@RemoteViews.RemoteView
public class RemeasuringLinearLayout extends LinearLayout {
    public RemeasuringLinearLayout(Context context) {
        super(context);
    }

    public RemeasuringLinearLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public RemeasuringLinearLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public RemeasuringLinearLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    /* access modifiers changed from: protected */
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int count = getChildCount();
        int height = 0;
        for (int i = 0; i < count; i++) {
            View child = getChildAt(i);
            if (!(child == null || child.getVisibility() == 8)) {
                LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) child.getLayoutParams();
                height = Math.max(height, child.getMeasuredHeight() + height + lp.topMargin + lp.bottomMargin);
            }
        }
        setMeasuredDimension(getMeasuredWidth(), height);
    }
}
