package com.android.server.wifi.hotspot2;

import com.android.server.wifi.SIMAccessor;
import com.android.server.wifi.WifiKeyStore;
import com.android.server.wifi.hotspot2.PasspointConfigUserStoreData;
import com.android.server.wifi.util.XmlUtil;
import java.io.IOException;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public class CustPasspointConfigStoreData extends PasspointConfigUserStoreData {
    CustPasspointConfigStoreData(WifiKeyStore keyStore, SIMAccessor simAccessor, PasspointConfigUserStoreData.DataSource dataSource) {
        super(keyStore, simAccessor, dataSource);
    }

    public void deserializeCustData(XmlPullParser in, int outerTagDepth) throws XmlPullParserException, IOException {
        String[] headerName = new String[1];
        while (XmlUtil.gotoNextSectionOrEnd(in, headerName, outerTagDepth)) {
            String str = headerName[0];
            char c = 65535;
            if (str.hashCode() == -254992817 && str.equals("ProviderList")) {
                c = 0;
            }
            if (c == 0) {
                PasspointManager.addProviders(deserializeProviderList(in, outerTagDepth + 1));
            } else {
                throw new XmlPullParserException("Unknown Passpoint user store data " + headerName[0]);
            }
        }
    }
}
