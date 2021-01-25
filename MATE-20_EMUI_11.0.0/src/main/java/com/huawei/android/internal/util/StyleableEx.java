package com.huawei.android.internal.util;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import com.android.internal.R;

public class StyleableEx {
    public static final String DEFAULT_STRING = null;
    public static final int STYLEABLE_ATTR_NAME_CONTACTSDATAKIND_ALLCONTACTSNAME = 3;
    public static final int STYLEABLE_ATTR_NAME_CONTACTSDATAKIND_DETAILCOLUMN = 2;
    public static final int STYLEABLE_ATTR_NAME_CONTACTSDATAKIND_MIMETYPE = 0;
    public static final int STYLEABLE_ATTR_NAME_CONTACTSDATAKIND_SUMMARYCOLUMN = 1;
    public static final int STYLEABLE_NAME_CONTACTSDATAKIND = 0;

    public static TypedArray getTypedArray(Context context, AttributeSet attrs, int styleableName) {
        if (context == null || attrs == null || styleableName != 0) {
            return null;
        }
        return context.obtainStyledAttributes(attrs, R.styleable.ContactsDataKind);
    }

    public static String getString(TypedArray typedArray, int styleableAttrName) {
        if (typedArray == null) {
            return DEFAULT_STRING;
        }
        if (styleableAttrName == 0) {
            return typedArray.getString(1);
        }
        if (styleableAttrName == 1) {
            return typedArray.getString(2);
        }
        if (styleableAttrName == 2) {
            return typedArray.getString(3);
        }
        if (styleableAttrName != 3) {
            return DEFAULT_STRING;
        }
        return typedArray.getString(5);
    }

    public static String getNonResourceString(TypedArray typedArray, int styleableAttrName) {
        if (typedArray == null) {
            return DEFAULT_STRING;
        }
        if (styleableAttrName != 3) {
            return DEFAULT_STRING;
        }
        return typedArray.getNonResourceString(5);
    }

    public static int getResourceId(TypedArray typedArray, int styleableAttrName, int defaultValue) {
        if (typedArray == null) {
            return defaultValue;
        }
        if (styleableAttrName != 3) {
            return defaultValue;
        }
        return typedArray.getResourceId(5, defaultValue);
    }
}
