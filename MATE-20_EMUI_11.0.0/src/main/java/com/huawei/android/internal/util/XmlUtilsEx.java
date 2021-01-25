package com.huawei.android.internal.util;

import com.android.internal.util.XmlUtils;
import com.huawei.annotation.HwSystemApi;
import java.io.IOException;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public class XmlUtilsEx {
    public static final int convertValueToInt(CharSequence charSeq, int defaultValue) {
        return XmlUtils.convertValueToInt(charSeq, defaultValue);
    }

    @HwSystemApi
    public static final void beginDocument(XmlPullParser parser, String firstElementName) throws XmlPullParserException, IOException {
        XmlUtils.beginDocument(parser, firstElementName);
    }

    @HwSystemApi
    public static final void nextElement(XmlPullParser parser) throws XmlPullParserException, IOException {
        XmlUtils.nextElement(parser);
    }
}
