package huawei.com.android.internal.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.FrameLayout;

public class HwFragmentFrameLayout extends FrameLayout {
    private HwFragmentFrameLayoutCallback mFragmentFrameLayoutCallback;
    private boolean mIsSelectedContainerByTouch = false;

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

    /* access modifiers changed from: protected */
    public void setFragmentFrameLayoutCallback(HwFragmentFrameLayoutCallback callback) {
        this.mFragmentFrameLayoutCallback = callback;
    }

    /* access modifiers changed from: protected */
    public void setSelectContainerByTouch(boolean isEnabled) {
        this.mIsSelectedContainerByTouch = isEnabled;
    }

    public boolean dispatchTouchEvent(MotionEvent event) {
        HwFragmentFrameLayoutCallback hwFragmentFrameLayoutCallback;
        if (this.mIsSelectedContainerByTouch && (event.getAction() & 255) == 0 && (hwFragmentFrameLayoutCallback = this.mFragmentFrameLayoutCallback) != null) {
            hwFragmentFrameLayoutCallback.setSelectedFrameLayout(getId());
        }
        return super.dispatchTouchEvent(event);
    }
}
