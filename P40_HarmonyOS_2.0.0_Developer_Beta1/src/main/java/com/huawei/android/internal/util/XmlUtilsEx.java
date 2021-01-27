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

    @HwSystemApi
    public static final void skipCurrentTag(XmlPullParser parser) throws XmlPullParserException, IOException {
        XmlUtils.skipCurrentTag(parser);
    }

    @HwSystemApi
    public static String readStringAttribute(XmlPullParser in, String name) {
        return XmlUtils.readStringAttribute(in, name);
    }

    @HwSystemApi
    public static void writeStringAttribute(FastXmlSerializerEx out, String name, CharSequence value) throws IOException {
        XmlUtils.writeStringAttribute(out.getFastXmlSerializer(), name, value);
    }
}
