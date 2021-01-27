package com.android.server.firewall;

import java.io.IOException;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public abstract class FilterFactory {
    private final String mTag;

    public abstract Filter newFilter(XmlPullParser xmlPullParser) throws IOException, XmlPullParserException;

    protected FilterFactory(String tag) {
        if (tag != null) {
            this.mTag = tag;
            return;
        }
        throw new NullPointerException();
    }

    public String getTagName() {
        return this.mTag;
    }
}
