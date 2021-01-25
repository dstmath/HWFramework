package com.android.server.appactcontrol;

import java.io.IOException;
import java.util.HashMap;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

interface IAppActScenes {
    boolean isNeedForbidAppAct(String str, String str2, HashMap<String, String> hashMap);

    void readXmlDataByScenes(int i, XmlPullParser xmlPullParser) throws XmlPullParserException, IOException;
}
