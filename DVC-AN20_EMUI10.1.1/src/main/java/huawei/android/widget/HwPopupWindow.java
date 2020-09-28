package huawei.android.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.PopupWindow;

public class HwPopupWindow extends PopupWindow {
    public HwPopupWindow(Context context) {
        this(context, (AttributeSet) null);
    }

    public HwPopupWindow(Context context, AttributeSet attrs) {
        this(context, attrs, 16842870);
    }

    public HwPopupWindow(Context context, AttributeSet attrs, int defStyle) {
        this(context, attrs, defStyle, 0);
    }

    public HwPopupWindow(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public HwPopupWindow() {
        this((View) null, 0, 0);
    }

    public HwPopupWindow(View contentView) {
        this(contentView, 0, 0);
    }

    public HwPopupWindow(int width, int height) {
        this((View) null, width, height);
    }

    public HwPopupWindow(View contentView, int width, int height) {
        this(contentView, width, height, false);
    }

    public HwPopupWindow(View contentView, int width, int height, boolean isFocusable) {
        super(contentView, width, height, isFocusable);
    }
}
