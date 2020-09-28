package com.android.internal.widget;

import android.annotation.UnsupportedAppUsage;
import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;

public class LinearLayoutWithDefaultTouchRecepient extends LinearLayout {
    private View mDefaultTouchRecepient;
    private final Rect mTempRect = new Rect();

    @UnsupportedAppUsage
    public LinearLayoutWithDefaultTouchRecepient(Context context) {
        super(context);
    }

    public LinearLayoutWithDefaultTouchRecepient(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @UnsupportedAppUsage
    public void setDefaultTouchRecepient(View defaultTouchRecepient) {
        this.mDefaultTouchRecepient = defaultTouchRecepient;
    }

    @Override // android.view.View, android.view.ViewGroup
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (this.mDefaultTouchRecepient == null) {
            return super.dispatchTouchEvent(ev);
        }
        if (super.dispatchTouchEvent(ev)) {
            return true;
        }
        this.mTempRect.set(0, 0, 0, 0);
        offsetRectIntoDescendantCoords(this.mDefaultTouchRecepient, this.mTempRect);
        ev.setLocation(ev.getX() + ((float) this.mTempRect.left), ev.getY() + ((float) this.mTempRect.top));
        return this.mDefaultTouchRecepient.dispatchTouchEvent(ev);
    }
}
