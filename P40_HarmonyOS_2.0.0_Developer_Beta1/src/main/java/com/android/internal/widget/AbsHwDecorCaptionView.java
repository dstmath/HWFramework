package com.android.internal.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.KeyEvent;

public abstract class AbsHwDecorCaptionView extends DecorCaptionView {
    public AbsHwDecorCaptionView(Context context) {
        super(context);
    }

    public AbsHwDecorCaptionView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public AbsHwDecorCaptionView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public void updateShade(boolean isLight) {
    }

    public boolean processKeyEvent(KeyEvent event) {
        return false;
    }
}
