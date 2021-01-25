package com.android.server.wifi.hotspot2;

import com.android.server.wifi.hotspot2.anqp.ANQPElement;
import com.android.server.wifi.hotspot2.anqp.Constants;
import java.util.HashMap;
import java.util.Map;

public class AnqpEvent {
    private static final String TAG = "AnqpEvent";
    private static final Map<String, Constants.ANQPElementType> sWpsNames = new HashMap();
    private final long mBssid;
    private final Map<Constants.ANQPElementType, ANQPElement> mElements;

    static {
        sWpsNames.put("anqp_venue_name", Constants.ANQPElementType.ANQPVenueName);
        sWpsNames.put("anqp_roaming_consortium", Constants.ANQPElementType.ANQPRoamingConsortium);
        sWpsNames.put("anqp_ip_addr_type_availability", Constants.ANQPElementType.ANQPIPAddrAvailability);
        sWpsNames.put("anqp_nai_realm", Constants.ANQPElementType.ANQPNAIRealm);
        sWpsNames.put("anqp_3gpp", Constants.ANQPElementType.ANQP3GPPNetwork);
        sWpsNames.put("anqp_domain_name", Constants.ANQPElementType.ANQPDomName);
        sWpsNames.put("hs20_operator_friendly_name", Constants.ANQPElementType.HSFriendlyName);
        sWpsNames.put("hs20_wan_metrics", Constants.ANQPElementType.HSWANMetrics);
        sWpsNames.put("hs20_connection_capability", Constants.ANQPElementType.HSConnCapability);
        sWpsNames.put("hs20_osu_providers_list", Constants.ANQPElementType.HSOSUProviders);
    }

    public AnqpEvent(long bssid, Map<Constants.ANQPElementType, ANQPElement> elements) {
        this.mBssid = bssid;
        this.mElements = elements;
    }

    public long getBssid() {
        return this.mBssid;
    }

    public Map<Constants.ANQPElementType, ANQPElement> getElements() {
        return this.mElements;
    }
}
