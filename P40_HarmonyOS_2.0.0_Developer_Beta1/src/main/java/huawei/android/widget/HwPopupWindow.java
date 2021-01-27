package huawei.android.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.PopupWindow;
import com.huawei.android.view.ViewEx;
import com.huawei.android.view.WindowManagerEx;

public class HwPopupWindow extends PopupWindow {
    private int mBlurStyle;
    private int mCornerSize;
    private boolean mIsBlurEnabled;

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
        this.mIsBlurEnabled = false;
        this.mCornerSize = 0;
        this.mBlurStyle = 2;
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
        this.mIsBlurEnabled = false;
        this.mCornerSize = 0;
        this.mBlurStyle = 2;
    }

    public void setBlurEnabled(boolean isBlurEnabled) {
        this.mIsBlurEnabled = isBlurEnabled;
    }

    public void setBlurEnabled(boolean isBlurEnabled, int cornerSize) {
        this.mIsBlurEnabled = isBlurEnabled;
        this.mCornerSize = cornerSize;
    }

    public void setBlurStyle(int blurStyle) {
        this.mBlurStyle = blurStyle;
    }

    /* access modifiers changed from: protected */
    public void preInvokePopup(FrameLayout decorView, WindowManager.LayoutParams layoutParams) {
        if (decorView != null && layoutParams != null && WindowManagerEx.getBlurFeatureEnabled() && this.mIsBlurEnabled) {
            WindowManagerEx.LayoutParamsEx layoutParamsEx = new WindowManagerEx.LayoutParamsEx(layoutParams);
            layoutParamsEx.addHwFlags(33554432);
            layoutParamsEx.setBlurStyle(this.mBlurStyle);
            ViewEx.setBlurEnabled(decorView, true);
            int i = this.mCornerSize;
            ViewEx.setBlurCornerRadius(decorView, i, i);
        }
    }
}
