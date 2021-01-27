package com.huawei.org.kxml2.io;

import com.android.org.kxml2.io.KXmlParser;
import com.huawei.annotation.HwSystemApi;
import java.io.IOException;
import org.xmlpull.v1.XmlPullParser;

@HwSystemApi
public class KXmlParserExt {
    public static void closeKXmlParser(XmlPullParser parser) throws IOException {
        if (parser != null && (parser instanceof KXmlParser)) {
            ((KXmlParser) parser).close();
        }
    }
}
