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
        TypedArray result;
        if (context == null || attrs == null) {
            return null;
        }
        if (styleableName != 0) {
            result = null;
        } else {
            result = context.obtainStyledAttributes(attrs, R.styleable.ContactsDataKind);
        }
        return result;
    }

    public static String getString(TypedArray typedArray, int styleableAttrName) {
        String result;
        if (typedArray == null) {
            return DEFAULT_STRING;
        }
        switch (styleableAttrName) {
            case 0:
                result = typedArray.getString(1);
                break;
            case 1:
                result = typedArray.getString(2);
                break;
            case 2:
                result = typedArray.getString(3);
                break;
            case 3:
                result = typedArray.getString(5);
                break;
            default:
                result = DEFAULT_STRING;
                break;
        }
        return result;
    }

    public static String getNonResourceString(TypedArray typedArray, int styleableAttrName) {
        String result;
        if (typedArray == null) {
            return DEFAULT_STRING;
        }
        if (styleableAttrName != 3) {
            result = DEFAULT_STRING;
        } else {
            result = typedArray.getNonResourceString(5);
        }
        return result;
    }

    public static int getResourceId(TypedArray typedArray, int styleableAttrName, int defaultValue) {
        int result;
        if (typedArray == null) {
            return defaultValue;
        }
        if (styleableAttrName != 3) {
            result = defaultValue;
        } else {
            result = typedArray.getResourceId(5, defaultValue);
        }
        return result;
    }
}
