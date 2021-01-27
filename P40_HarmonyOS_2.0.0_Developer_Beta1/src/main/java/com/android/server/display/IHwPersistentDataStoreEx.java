package com.android.server.display;

import java.io.IOException;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlSerializer;

public interface IHwPersistentDataStoreEx {
    void addHdcpSupportedDevice(String str);

    void addUibcExceptionDevice(String str);

    boolean isHdcpSupported(String str);

    boolean isUibcException(String str);

    void loadWifiDisplayExtendAttribute(XmlPullParser xmlPullParser, String str);

    void saveWifiDisplayExtendAttribute(XmlSerializer xmlSerializer, String str) throws IOException;
}
