package com.android.internal.policy;

import android.content.Context;
import android.hwcontrol.HwWidgetFactory;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;

public class HwPhoneLayoutInflater extends PhoneLayoutInflater {
    private static final boolean DEBUG = false;
    private static final String TAG = "HwPhoneLayoutInflater";
    private static final String[] sAndroidClassList = {"LinearLayout", "RelativeLayout", "FrameLayout", "ViewStub", "View", "TextView", "RadioButton", "Space"};
    private static final String[] sHwClassPrefixList = {"huawei.android.view.", "huawei.android.widget."};

    private static boolean isAndoridClass(String name) {
        for (String clazz : sAndroidClassList) {
            if (name.equals(clazz)) {
                return true;
            }
        }
        return false;
    }

    public HwPhoneLayoutInflater(Context context) {
        super(context);
    }

    protected HwPhoneLayoutInflater(LayoutInflater original, Context newContext) {
        super(original, newContext);
    }

    /* access modifiers changed from: protected */
    public View onCreateView(String name, AttributeSet attrs) throws ClassNotFoundException {
        if (HwWidgetFactory.isHwTheme(getContext())) {
            if (isAndoridClass(name)) {
                return HwPhoneLayoutInflater.super.onCreateView(name, attrs);
            }
            for (String prefix : sHwClassPrefixList) {
                try {
                    View view = createView(name, prefix, attrs);
                    if (view != null) {
                        return view;
                    }
                } catch (ClassNotFoundException e) {
                }
            }
        }
        return HwPhoneLayoutInflater.super.onCreateView(name, attrs);
    }

    /* JADX DEBUG: Multi-variable search result rejected for r1v0, resolved type: com.android.internal.policy.HwPhoneLayoutInflater */
    /* JADX WARN: Multi-variable type inference failed */
    /* JADX WARN: Type inference failed for: r0v0, types: [com.android.internal.policy.HwPhoneLayoutInflater, android.view.LayoutInflater] */
    /* JADX WARNING: Unknown variable types count: 1 */
    public LayoutInflater cloneInContext(Context newContext) {
        return new HwPhoneLayoutInflater(this, newContext);
    }
}
