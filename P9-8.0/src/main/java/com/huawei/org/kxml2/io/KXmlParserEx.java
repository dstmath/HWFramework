package com.huawei.org.kxml2.io;

import android.util.Log;
import java.io.IOException;
import org.kxml2.io.KXmlParser;
import org.xmlpull.v1.XmlPullParser;

public class KXmlParserEx {
    XmlPullParser mParser;

    public KXmlParserEx(XmlPullParser parser) {
        this.mParser = parser;
    }

    public void close() {
        if (this.mParser instanceof KXmlParser) {
            try {
                ((KXmlParser) this.mParser).close();
            } catch (IOException e) {
                Log.e("KXmlParserEx", "loadMmsSettings: fail close the parser");
            }
        }
    }
}
