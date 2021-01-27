package com.huawei.server.fingerprint;

import android.content.Context;
import android.graphics.Canvas;
import android.text.DynamicLayout;
import android.text.Layout;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.widget.TextView;
import java.util.Locale;

public class HintText extends TextView {
    private static final float MSPACINGMULTFORMY = 1.18f;
    private static final float MSPACINGMULTFORSI = 1.08f;
    private DynamicLayout mDynamicLayout;
    private TextPaint tp;

    public HintText(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    /* access modifiers changed from: protected */
    @Override // android.view.View
    public void onSizeChanged(int width, int height, int oldw, int oldh) {
        super.onSizeChanged(width, height, oldw, oldh);
        initView();
    }

    private void initView() {
        this.tp = new TextPaint(1);
        this.tp.setTextSize(getTextSize());
        this.tp.setColor(getCurrentTextColor());
    }

    /* access modifiers changed from: protected */
    @Override // android.widget.TextView, android.view.View
    public void onDraw(Canvas canvas) {
        float spacingMult = 1.0f;
        boolean isIncludepad = false;
        Locale loc = Locale.getDefault();
        if ("my".equals(loc.getLanguage()) && "MM".equals(loc.getCountry())) {
            spacingMult = MSPACINGMULTFORMY;
            isIncludepad = true;
        } else if ("si".equals(loc.getLanguage())) {
            spacingMult = MSPACINGMULTFORSI;
            isIncludepad = true;
        }
        this.mDynamicLayout = new DynamicLayout(getText(), this.tp, getWidth(), Layout.Alignment.ALIGN_CENTER, spacingMult, 0.0f, isIncludepad);
        this.mDynamicLayout.draw(canvas);
    }
}
