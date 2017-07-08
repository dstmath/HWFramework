package huawei.com.android.internal.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.FrameLayout;

public class HwFragmentFrameLayout extends FrameLayout {
    private HwFragmentFrameLayoutCallback mFragmentFrameLayoutCallback;
    private boolean mSelectContainerByTouch;

    public interface HwFragmentFrameLayoutCallback {
        void setSelectedFrameLayout(int i);
    }

    public HwFragmentFrameLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.mSelectContainerByTouch = false;
    }

    public HwFragmentFrameLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mSelectContainerByTouch = false;
    }

    public HwFragmentFrameLayout(Context context) {
        super(context);
        this.mSelectContainerByTouch = false;
    }

    protected void setFragmentFrameLayoutCallback(HwFragmentFrameLayoutCallback callback) {
        this.mFragmentFrameLayoutCallback = callback;
    }

    protected void setSelectContainerByTouch(boolean enabled) {
        this.mSelectContainerByTouch = enabled;
    }

    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (this.mSelectContainerByTouch && (ev.getAction() & PduHeaders.STORE_STATUS_ERROR_END) == 0 && this.mFragmentFrameLayoutCallback != null) {
            this.mFragmentFrameLayoutCallback.setSelectedFrameLayout(getId());
        }
        return super.dispatchTouchEvent(ev);
    }
}
