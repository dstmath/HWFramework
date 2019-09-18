package com.android.internal.policy;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.SystemProperties;
import android.view.View;

public class HwNavigationBarColorView extends View {
    private static final int ROTATION = (SystemProperties.getInt("ro.panel.hw_orientation", 0) / 90);
    private static final boolean SHOW_DCM_NAVBAR_AT_BOTTOM = SystemProperties.getBoolean("ro.config.dcm_landscreen_navbar", false);
    private static final String TAG = "HwNavigationBarColorView";
    private static final int mLineHeightPx = 1;
    private int mLineColor;
    private int mOrientation;
    private Paint mPaint;

    public HwNavigationBarColorView(Context context) {
        super(context);
        initDrawLinePaint(context);
        initOrientation(context);
    }

    /* access modifiers changed from: protected */
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (this.mOrientation != 1 && this.mOrientation == 2 && !isNavBarAtBottom()) {
            canvas.drawLine(0.0f, 0.0f, 0.0f, (float) getHeight(), this.mPaint);
        }
    }

    /* access modifiers changed from: protected */
    public void onConfigurationChanged(Configuration newConfig) {
        this.mOrientation = newConfig.orientation;
    }

    private void initDrawLinePaint(Context context) {
        this.mLineColor = context.getResources().getColor(33882234);
        this.mPaint = new Paint();
        this.mPaint.setStrokeWidth(1.0f);
        this.mPaint.setColor(this.mLineColor);
    }

    private void initOrientation(Context context) {
        this.mOrientation = context.getResources().getConfiguration().orientation;
    }

    private boolean isNavBarAtBottom() {
        boolean z = false;
        if (ROTATION == 0 || ROTATION == 2) {
            return SHOW_DCM_NAVBAR_AT_BOTTOM;
        }
        if (this.mOrientation == 2) {
            z = true;
        }
        return z;
    }
}
