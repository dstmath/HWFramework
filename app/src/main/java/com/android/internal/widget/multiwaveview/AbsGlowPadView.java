package com.android.internal.widget.multiwaveview;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;

public class AbsGlowPadView extends View {
    protected Drawable mPointDrawable;

    public AbsGlowPadView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mPointDrawable = null;
    }

    public Drawable getPointDrawable() {
        return this.mPointDrawable;
    }
}
