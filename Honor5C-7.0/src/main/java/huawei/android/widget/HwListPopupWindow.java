package huawei.android.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ListPopupWindow;

public class HwListPopupWindow extends ListPopupWindow {
    public HwListPopupWindow(Context context) {
        this(context, null, 16843519, 0);
    }

    public HwListPopupWindow(Context context, AttributeSet attrs) {
        this(context, attrs, 16843519, 0);
    }

    public HwListPopupWindow(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public HwListPopupWindow(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public void setHwFullDim() {
    }

    public boolean isHwFullDim() {
        return false;
    }
}
