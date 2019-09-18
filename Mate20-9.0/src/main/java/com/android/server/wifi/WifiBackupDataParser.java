package com.android.server.wifi;

import android.net.wifi.WifiConfiguration;
import java.io.IOException;
import java.util.List;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

interface WifiBackupDataParser {
    List<WifiConfiguration> parseNetworkConfigurationsFromXml(XmlPullParser xmlPullParser, int i, int i2) throws XmlPullParserException, IOException;
}
