package com.android.internal.widget;

import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RemoteViews.RemoteView;

@RemoteView
public class NotificationExpandButton extends ImageView {
    private View mLabeledBy;

    public NotificationExpandButton(Context context) {
        super(context);
    }

    public NotificationExpandButton(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public NotificationExpandButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public NotificationExpandButton(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public void getBoundsOnScreen(Rect outRect, boolean clipToParent) {
        super.getBoundsOnScreen(outRect, clipToParent);
        extendRectToMinTouchSize(outRect);
    }

    private void extendRectToMinTouchSize(Rect rect) {
        int touchTargetSize = (int) (getResources().getDisplayMetrics().density * 48.0f);
        rect.left = rect.centerX() - (touchTargetSize / 2);
        rect.right = rect.left + touchTargetSize;
        rect.top = rect.centerY() - (touchTargetSize / 2);
        rect.bottom = rect.top + touchTargetSize;
    }

    public void onInitializeAccessibilityNodeInfo(AccessibilityNodeInfo info) {
        super.onInitializeAccessibilityNodeInfo(info);
        info.setClassName(Button.class.getName());
        if (this.mLabeledBy != null) {
            info.setLabeledBy(this.mLabeledBy);
        }
    }

    public void setLabeledBy(View labeledBy) {
        this.mLabeledBy = labeledBy;
    }
}
