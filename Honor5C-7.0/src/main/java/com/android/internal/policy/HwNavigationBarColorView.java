package com.android.internal.policy;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.view.View;
import huawei.com.android.internal.widget.HwFragmentMenuItemView;

public class HwNavigationBarColorView extends View {
    private static final String TAG = "HwNavigationBarColorView";
    private static final int mLineHeightPx = 2;
    private int mLineColor;
    private int mOrientation;
    private Paint mPaint;

    public HwNavigationBarColorView(Context context) {
        super(context);
        initDrawLinePaint(context);
        initOrientation(context);
    }

    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (this.mOrientation == 1) {
            canvas.drawLine(0.0f, HwFragmentMenuItemView.ALPHA_NORMAL, (float) getWidth(), HwFragmentMenuItemView.ALPHA_NORMAL, this.mPaint);
        } else if (this.mOrientation == mLineHeightPx) {
            canvas.drawLine(HwFragmentMenuItemView.ALPHA_NORMAL, 0.0f, HwFragmentMenuItemView.ALPHA_NORMAL, (float) getHeight(), this.mPaint);
        }
    }

    protected void onConfigurationChanged(Configuration newConfig) {
        this.mOrientation = newConfig.orientation;
    }

    private void initDrawLinePaint(Context context) {
        this.mLineColor = context.getResources().getColor(33882231);
        this.mPaint = new Paint();
        this.mPaint.setStrokeWidth(2.0f);
        this.mPaint.setColor(this.mLineColor);
    }

    private void initOrientation(Context context) {
        this.mOrientation = context.getResources().getConfiguration().orientation;
    }
}
