package com.android.internal.policy;

import android.content.Context;
import android.hwcontrol.HwWidgetFactory;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;

public class HwPhoneLayoutInflater extends PhoneLayoutInflater {
    private static final String TAG = "HwPhoneLayoutInflater";
    private static final String[] sAndroidClassList = new String[]{"LinearLayout", "RelativeLayout", "FrameLayout", "ViewStub", "View", "ScrollView", "TextView", "RadioButton", "EditText", "ImageView", "Space"};
    private static final String[] sHwClassPrefixList = new String[]{"huawei.android.view.", "huawei.android.widget."};

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

    protected View onCreateView(String name, AttributeSet attrs) throws ClassNotFoundException {
        if (HwWidgetFactory.isHwTheme(getContext())) {
            if (isAndoridClass(name)) {
                return super.onCreateView(name, attrs);
            }
            String[] strArr = sHwClassPrefixList;
            int i = 0;
            int length = strArr.length;
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
        return super.onCreateView(name, attrs);
    }

    public LayoutInflater cloneInContext(Context newContext) {
        return new HwPhoneLayoutInflater(this, newContext);
    }
}
