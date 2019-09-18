package com.android.internal.policy;

import android.content.Context;
import android.hwcontrol.HwWidgetFactory;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;

public class HwPhoneLayoutInflater extends PhoneLayoutInflater {
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
            String[] strArr = sHwClassPrefixList;
            int length = strArr.length;
            int i = 0;
            while (i < length) {
                try {
                    View view = createView(name, strArr[i], attrs);
                    if (view != null) {
                        return view;
                    }
                    i++;
                } catch (ClassNotFoundException e) {
                    Log.w(TAG, "onCreateView : ClassNotFoundException, In this case we want to let the base class take a crack at it");
                }
            }
        }
        return HwPhoneLayoutInflater.super.onCreateView(name, attrs);
    }

    /* JADX WARNING: type inference failed for: r0v0, types: [com.android.internal.policy.HwPhoneLayoutInflater, android.view.LayoutInflater] */
    public LayoutInflater cloneInContext(Context newContext) {
        return new HwPhoneLayoutInflater(this, newContext);
    }
}
