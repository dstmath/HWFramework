package com.huawei.android.internal.util;

import com.android.internal.util.XmlUtils;
import org.xmlpull.v1.XmlPullParser;

public class XmlUtilsExt {
    public static int readIntAttribute(XmlPullParser in, String name, int defaultValue) {
        return XmlUtils.readIntAttribute(in, name, defaultValue);
    }

    public static String readStringAttribute(XmlPullParser in, String name) {
        return XmlUtils.readStringAttribute(in, name);
    }

    public static boolean readBooleanAttribute(XmlPullParser in, String name, boolean defaultValue) {
        return XmlUtils.readBooleanAttribute(in, name, defaultValue);
    }
}
