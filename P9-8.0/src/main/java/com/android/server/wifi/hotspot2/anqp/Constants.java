package com.android.server.wifi.hotspot2.anqp;

import java.util.Collection;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class Constants {
    public static final int ANQP_3GPP_NETWORK = 264;
    public static final int ANQP_DOM_NAME = 268;
    public static final int ANQP_IP_ADDR_AVAILABILITY = 262;
    public static final int ANQP_NAI_REALM = 263;
    public static final int ANQP_QUERY_LIST = 256;
    public static final int ANQP_ROAMING_CONSORTIUM = 261;
    public static final int ANQP_VENDOR_SPEC = 56797;
    public static final int ANQP_VENUE_NAME = 258;
    public static final int BYTES_IN_EUI48 = 6;
    public static final int BYTES_IN_INT = 4;
    public static final int BYTES_IN_SHORT = 2;
    public static final int BYTE_MASK = 255;
    public static final int HS20_FRAME_PREFIX = 278556496;
    public static final int HS20_PREFIX = 295333712;
    public static final int HS_CONN_CAPABILITY = 5;
    public static final int HS_FRIENDLY_NAME = 3;
    public static final int HS_ICON_REQUEST = 10;
    public static final int HS_NAI_HOME_REALM_QUERY = 6;
    public static final int HS_OSU_PROVIDERS = 8;
    public static final int HS_QUERY_LIST = 1;
    public static final int HS_WAN_METRICS = 4;
    public static final long INT_MASK = 4294967295L;
    public static final int LANG_CODE_LENGTH = 3;
    public static final long MILLIS_IN_A_SEC = 1000;
    public static final int NIBBLE_MASK = 15;
    public static final int SHORT_MASK = 65535;
    public static final int UTF8_INDICATOR = 1;
    public static final int VENUE_INFO_LENGTH = 2;
    private static final Map<Integer, ANQPElementType> sAnqpMap = new HashMap();
    private static final Map<Integer, ANQPElementType> sHs20Map = new HashMap();
    private static final Map<ANQPElementType, Integer> sRevAnqpmap = new EnumMap(ANQPElementType.class);
    private static final Map<ANQPElementType, Integer> sRevHs20map = new EnumMap(ANQPElementType.class);

    public enum ANQPElementType {
        ANQPQueryList,
        ANQPVenueName,
        ANQPRoamingConsortium,
        ANQPIPAddrAvailability,
        ANQPNAIRealm,
        ANQP3GPPNetwork,
        ANQPDomName,
        ANQPVendorSpec,
        HSQueryList,
        HSFriendlyName,
        HSWANMetrics,
        HSConnCapability,
        HSNAIHomeRealmQuery,
        HSOSUProviders,
        HSIconRequest
    }

    static {
        sAnqpMap.put(Integer.valueOf(256), ANQPElementType.ANQPQueryList);
        sAnqpMap.put(Integer.valueOf(ANQP_VENUE_NAME), ANQPElementType.ANQPVenueName);
        sAnqpMap.put(Integer.valueOf(ANQP_ROAMING_CONSORTIUM), ANQPElementType.ANQPRoamingConsortium);
        sAnqpMap.put(Integer.valueOf(ANQP_IP_ADDR_AVAILABILITY), ANQPElementType.ANQPIPAddrAvailability);
        sAnqpMap.put(Integer.valueOf(ANQP_NAI_REALM), ANQPElementType.ANQPNAIRealm);
        sAnqpMap.put(Integer.valueOf(ANQP_3GPP_NETWORK), ANQPElementType.ANQP3GPPNetwork);
        sAnqpMap.put(Integer.valueOf(ANQP_DOM_NAME), ANQPElementType.ANQPDomName);
        sAnqpMap.put(Integer.valueOf(ANQP_VENDOR_SPEC), ANQPElementType.ANQPVendorSpec);
        sHs20Map.put(Integer.valueOf(1), ANQPElementType.HSQueryList);
        sHs20Map.put(Integer.valueOf(3), ANQPElementType.HSFriendlyName);
        sHs20Map.put(Integer.valueOf(4), ANQPElementType.HSWANMetrics);
        sHs20Map.put(Integer.valueOf(5), ANQPElementType.HSConnCapability);
        sHs20Map.put(Integer.valueOf(6), ANQPElementType.HSNAIHomeRealmQuery);
        sHs20Map.put(Integer.valueOf(8), ANQPElementType.HSOSUProviders);
        sHs20Map.put(Integer.valueOf(10), ANQPElementType.HSIconRequest);
        for (Entry<Integer, ANQPElementType> entry : sAnqpMap.entrySet()) {
            sRevAnqpmap.put((ANQPElementType) entry.getValue(), (Integer) entry.getKey());
        }
        for (Entry<Integer, ANQPElementType> entry2 : sHs20Map.entrySet()) {
            sRevHs20map.put((ANQPElementType) entry2.getValue(), (Integer) entry2.getKey());
        }
    }

    public static ANQPElementType mapANQPElement(int id) {
        return (ANQPElementType) sAnqpMap.get(Integer.valueOf(id));
    }

    public static ANQPElementType mapHS20Element(int id) {
        return (ANQPElementType) sHs20Map.get(Integer.valueOf(id));
    }

    public static Integer getANQPElementID(ANQPElementType elementType) {
        return (Integer) sRevAnqpmap.get(elementType);
    }

    public static Integer getHS20ElementID(ANQPElementType elementType) {
        return (Integer) sRevHs20map.get(elementType);
    }

    public static boolean hasBaseANQPElements(Collection<ANQPElementType> elements) {
        if (elements == null) {
            return false;
        }
        for (ANQPElementType element : elements) {
            if (sRevAnqpmap.containsKey(element)) {
                return true;
            }
        }
        return false;
    }

    public static boolean hasR2Elements(List<ANQPElementType> elements) {
        return elements.contains(ANQPElementType.HSOSUProviders);
    }
}
