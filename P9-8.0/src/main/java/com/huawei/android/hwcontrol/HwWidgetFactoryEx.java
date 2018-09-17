package com.huawei.android.hwcontrol;

import android.content.Context;
import android.hwcontrol.HwWidgetFactory;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Toast;

public class HwWidgetFactoryEx {

    public static class HwToast {
        private android.hwcontrol.HwWidgetFactory.HwToast mHwToast;

        /* synthetic */ HwToast(android.hwcontrol.HwWidgetFactory.HwToast hwToast, HwToast -this1) {
            this(hwToast);
        }

        private HwToast(android.hwcontrol.HwWidgetFactory.HwToast hwToast) {
            this.mHwToast = hwToast;
        }

        public View layoutInflate(Context context) {
            return this.mHwToast.layoutInflate(context);
        }
    }

    public static HwToast getHwToast(Context context, Toast view, AttributeSet attrs) {
        android.hwcontrol.HwWidgetFactory.HwToast hwToast = HwWidgetFactory.getHwToast(context, view, attrs);
        if (hwToast != null) {
            return new HwToast(hwToast, null);
        }
        return null;
    }
}
