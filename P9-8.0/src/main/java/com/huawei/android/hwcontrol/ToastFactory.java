package com.huawei.android.hwcontrol;

import android.content.Context;
import android.content.res.Resources;
import android.hwcontrol.HwWidgetFactory.HwToast;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager.LayoutParams;
import android.widget.Toast;
import huawei.android.utils.HwRTBlurUtils;

public class ToastFactory implements HwToast {
    public static final String STYLE_EMUI_TOAST = "androidhwext:style/Animation.Emui.Toast";
    private static final String TAG = "ToastFactory";

    public ToastFactory(Context context, Toast view, AttributeSet attrs) {
        if (!(context == null || view == null)) {
            LayoutParams lp = view.getWindowParams();
            Resources res = context.getResources();
            if (res != null) {
                lp.windowAnimations = res.getIdentifier(STYLE_EMUI_TOAST, null, null);
            }
        }
        updateBlurStatus(context, view, attrs);
    }

    public void initialToast(Context context, AttributeSet attrs) {
    }

    public View layoutInflate(Context context) {
        return ((LayoutInflater) context.getSystemService("layout_inflater")).inflate(34013206, null);
    }

    private void updateBlurStatus(Context context, Toast toast, AttributeSet set) {
        if (toast != null) {
            LayoutParams lp = toast.getWindowParams();
            HwRTBlurUtils.updateBlurStatus(lp, HwRTBlurUtils.obtainBlurStyle(context, set, 33619992, 33951767, "HwToast-" + (lp != null ? lp.getTitle().toString() : "")));
        }
    }
}
