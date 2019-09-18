package com.android.internal.policy;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;

public class PhoneLayoutInflater extends LayoutInflater {
    private static final String[] sClassPrefixList = {"android.widget.", "android.webkit.", "android.app."};

    public PhoneLayoutInflater(Context context) {
        super(context);
    }

    protected PhoneLayoutInflater(LayoutInflater original, Context newContext) {
        super(original, newContext);
    }

    /* access modifiers changed from: protected */
    public View onCreateView(String name, AttributeSet attrs) throws ClassNotFoundException {
        if (name.equals("View") || name.equals("ViewStub")) {
            return super.onCreateView(name, attrs);
        }
        String[] strArr = sClassPrefixList;
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
            }
        }
        return super.onCreateView(name, attrs);
    }

    public LayoutInflater cloneInContext(Context newContext) {
        return new PhoneLayoutInflater(this, newContext);
    }
}
