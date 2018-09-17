package com.android.server.wifi.hotspot2;

import com.android.server.wifi.SIMAccessor;
import com.android.server.wifi.WifiKeyStore;
import com.android.server.wifi.hotspot2.PasspointConfigStoreData.DataSource;
import com.android.server.wifi.util.XmlUtil;
import java.io.IOException;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public class CustPasspointConfigStoreData extends PasspointConfigStoreData {
    CustPasspointConfigStoreData(WifiKeyStore keyStore, SIMAccessor simAccessor, DataSource dataSource) {
        super(keyStore, simAccessor, dataSource);
    }

    public void deserializeCustData(XmlPullParser in, int outerTagDepth) throws XmlPullParserException, IOException {
        String[] headerName = new String[1];
        while (XmlUtil.gotoNextSectionOrEnd(in, headerName, outerTagDepth)) {
            if (headerName[0].equals("ProviderList")) {
                PasspointManager.addProviders(deserializeProviderList(in, outerTagDepth + 1));
            } else {
                throw new XmlPullParserException("Unknown Passpoint user store data " + headerName[0]);
            }
        }
    }
}
