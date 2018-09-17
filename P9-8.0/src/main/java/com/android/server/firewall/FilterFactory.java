package com.android.server.firewall;

import java.io.IOException;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public abstract class FilterFactory {
    private final String mTag;

    public abstract Filter newFilter(XmlPullParser xmlPullParser) throws IOException, XmlPullParserException;

    protected FilterFactory(String tag) {
        if (tag == null) {
            throw new NullPointerException();
        }
        this.mTag = tag;
    }

    public String getTagName() {
        return this.mTag;
    }
}
