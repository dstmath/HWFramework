package com.android.server.wifi.hotspot2;

import com.android.server.wifi.hotspot2.anqp.ANQPElement;
import com.android.server.wifi.hotspot2.anqp.Constants.ANQPElementType;
import java.util.HashMap;
import java.util.Map;

public class AnqpEvent {
    private static final String TAG = "AnqpEvent";
    private static final Map<String, ANQPElementType> sWpsNames = new HashMap();
    private final long mBssid;
    private final Map<ANQPElementType, ANQPElement> mElements;

    static {
        sWpsNames.put("anqp_venue_name", ANQPElementType.ANQPVenueName);
        sWpsNames.put("anqp_roaming_consortium", ANQPElementType.ANQPRoamingConsortium);
        sWpsNames.put("anqp_ip_addr_type_availability", ANQPElementType.ANQPIPAddrAvailability);
        sWpsNames.put("anqp_nai_realm", ANQPElementType.ANQPNAIRealm);
        sWpsNames.put("anqp_3gpp", ANQPElementType.ANQP3GPPNetwork);
        sWpsNames.put("anqp_domain_name", ANQPElementType.ANQPDomName);
        sWpsNames.put("hs20_operator_friendly_name", ANQPElementType.HSFriendlyName);
        sWpsNames.put("hs20_wan_metrics", ANQPElementType.HSWANMetrics);
        sWpsNames.put("hs20_connection_capability", ANQPElementType.HSConnCapability);
        sWpsNames.put("hs20_osu_providers_list", ANQPElementType.HSOSUProviders);
    }

    public AnqpEvent(long bssid, Map<ANQPElementType, ANQPElement> elements) {
        this.mBssid = bssid;
        this.mElements = elements;
    }

    public long getBssid() {
        return this.mBssid;
    }

    public Map<ANQPElementType, ANQPElement> getElements() {
        return this.mElements;
    }
}
