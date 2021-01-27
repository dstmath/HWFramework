package com.android.server.autofill.ui;

import android.content.Context;
import android.graphics.Point;
import android.util.AttributeSet;
import android.util.Slog;
import android.util.TypedValue;
import android.widget.ScrollView;
import com.android.server.autofill.Helper;

public class CustomScrollView extends ScrollView {
    private static final String TAG = "CustomScrollView";
    private int mHeight = -1;
    private int mWidth = -1;

    public CustomScrollView(Context context) {
        super(context);
    }

    public CustomScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CustomScrollView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public CustomScrollView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    /* access modifiers changed from: protected */
    @Override // android.widget.ScrollView, android.widget.FrameLayout, android.view.View
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        if (getChildCount() == 0) {
            Slog.e(TAG, "no children");
            return;
        }
        calculateDimensions();
        setMeasuredDimension(this.mWidth, this.mHeight);
    }

    private void calculateDimensions() {
        if (this.mWidth == -1) {
            TypedValue typedValue = new TypedValue();
            Point point = new Point();
            Context context = getContext();
            context.getDisplay().getSize(point);
            context.getTheme().resolveAttribute(17956885, typedValue, true);
            int childHeight = getChildAt(0).getMeasuredHeight();
            int maxHeight = (int) typedValue.getFraction((float) point.y, (float) point.y);
            this.mWidth = point.x;
            this.mHeight = Math.min(childHeight, maxHeight);
            if (Helper.sDebug) {
                Slog.d(TAG, "calculateDimensions(): maxHeight=" + maxHeight + ", childHeight=" + childHeight + ", w=" + this.mWidth + ", h=" + this.mHeight);
            }
        }
    }
}
