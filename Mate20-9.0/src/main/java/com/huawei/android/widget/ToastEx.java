package com.huawei.android.widget;

import android.view.WindowManager;
import android.widget.Toast;

public class ToastEx {
    private static final WindowManager.LayoutParams DEFAULT_VALUE = new WindowManager.LayoutParams();

    public static WindowManager.LayoutParams getWindowParams(Toast toast) {
        if (toast == null) {
            return DEFAULT_VALUE;
        }
        return toast.getWindowParams();
    }
}
