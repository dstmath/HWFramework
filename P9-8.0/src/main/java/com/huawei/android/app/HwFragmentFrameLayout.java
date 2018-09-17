package com.huawei.android.app;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.FrameLayout;

public class HwFragmentFrameLayout extends FrameLayout {
    private HwFragmentFrameLayoutCallback mFragmentFrameLayoutCallback;
    private boolean mSelectContainerByTouch = false;

    public interface HwFragmentFrameLayoutCallback {
        void setSelectedFrameLayout(int i);
    }

    public HwFragmentFrameLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public HwFragmentFrameLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public HwFragmentFrameLayout(Context context) {
        super(context);
    }

    protected void setFragmentFrameLayoutCallback(HwFragmentFrameLayoutCallback callback) {
        this.mFragmentFrameLayoutCallback = callback;
    }

    protected void setSelectContainerByTouch(boolean enabled) {
        this.mSelectContainerByTouch = enabled;
    }

    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (this.mSelectContainerByTouch && (ev.getAction() & 255) == 0 && this.mFragmentFrameLayoutCallback != null) {
            this.mFragmentFrameLayoutCallback.setSelectedFrameLayout(getId());
        }
        return super.dispatchTouchEvent(ev);
    }
}
