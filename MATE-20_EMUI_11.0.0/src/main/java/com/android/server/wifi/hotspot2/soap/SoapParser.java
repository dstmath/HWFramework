package com.android.server.wifi.hotspot2.soap;

import org.ksoap2.serialization.SoapObject;

public class SoapParser {
    /* JADX WARNING: Removed duplicated region for block: B:12:0x002b  */
    /* JADX WARNING: Removed duplicated region for block: B:15:0x0034  */
    public static SppResponseMessage getResponse(SoapObject response) {
        char c;
        String name = response.getName();
        int hashCode = name.hashCode();
        if (hashCode != 1679330383) {
            if (hashCode == 1786714445 && name.equals("sppPostDevDataResponse")) {
                c = 0;
                if (c == 0) {
                    return PostDevDataResponse.createInstance(response);
                }
                if (c != 1) {
                    return null;
                }
                return ExchangeCompleteMessage.createInstance(response);
            }
        } else if (name.equals("sppExchangeComplete")) {
            c = 1;
            if (c == 0) {
            }
        }
        c = 65535;
        if (c == 0) {
        }
    }
}
