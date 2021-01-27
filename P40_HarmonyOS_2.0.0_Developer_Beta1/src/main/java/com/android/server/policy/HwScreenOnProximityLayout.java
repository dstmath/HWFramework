package com.android.server.policy;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.FrameLayout;

public class HwScreenOnProximityLayout extends FrameLayout {
    private EventListener mEventListener;

    public interface EventListener {
        void onDownEvent(MotionEvent motionEvent);
    }

    public HwScreenOnProximityLayout(Context context) {
        super(context, null);
    }

    public HwScreenOnProximityLayout(Context context, AttributeSet attrs) {
        super(context, attrs, 0);
    }

    @Override // android.view.ViewGroup
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (this.mEventListener != null && ev.getAction() == 0) {
            this.mEventListener.onDownEvent(ev);
        }
        return super.onInterceptTouchEvent(ev);
    }

    public void setEventListener(EventListener listener) {
        this.mEventListener = listener;
    }
}
