package com.android.server.wifi.hotspot2;

import android.net.RssiCurve;
import com.android.server.wifi.ScanDetail;
import com.android.server.wifi.hotspot2.NetworkDetail.Ant;
import com.android.server.wifi.hotspot2.anqp.ANQPElement;
import com.android.server.wifi.hotspot2.anqp.Constants.ANQPElementType;
import com.android.server.wifi.hotspot2.anqp.HSWanMetricsElement;
import com.android.server.wifi.hotspot2.anqp.IPAddressTypeAvailabilityElement;
import java.util.HashMap;
import java.util.Map;

public class PasspointNetworkScore {
    public static final int HOME_PROVIDER_AWARD = 100;
    public static final int INTERNET_ACCESS_AWARD = 50;
    private static final Map<Integer, Integer> IPV4_SCORES = new HashMap();
    private static final Map<Integer, Integer> IPV6_SCORES = new HashMap();
    private static final Map<Ant, Integer> NETWORK_TYPE_SCORES = new HashMap();
    public static final int PERSONAL_OR_EMERGENCY_NETWORK_AWARDS = 2;
    public static final int PUBLIC_OR_PRIVATE_NETWORK_AWARDS = 4;
    public static final int RESTRICTED_OR_UNKNOWN_IP_AWARDS = 1;
    public static final RssiCurve RSSI_SCORE = new RssiCurve(-80, 20, new byte[]{(byte) -10, (byte) 0, (byte) 10, (byte) 20, (byte) 30, (byte) 40}, 20);
    public static final int UNRESTRICTED_IP_AWARDS = 2;
    public static final int WAN_PORT_DOWN_OR_CAPPED_PENALTY = 50;

    static {
        NETWORK_TYPE_SCORES.put(Ant.FreePublic, Integer.valueOf(4));
        NETWORK_TYPE_SCORES.put(Ant.ChargeablePublic, Integer.valueOf(4));
        NETWORK_TYPE_SCORES.put(Ant.PrivateWithGuest, Integer.valueOf(4));
        NETWORK_TYPE_SCORES.put(Ant.Private, Integer.valueOf(4));
        NETWORK_TYPE_SCORES.put(Ant.Personal, Integer.valueOf(2));
        NETWORK_TYPE_SCORES.put(Ant.EmergencyOnly, Integer.valueOf(2));
        NETWORK_TYPE_SCORES.put(Ant.Wildcard, Integer.valueOf(0));
        NETWORK_TYPE_SCORES.put(Ant.TestOrExperimental, Integer.valueOf(0));
        IPV4_SCORES.put(Integer.valueOf(0), Integer.valueOf(0));
        IPV4_SCORES.put(Integer.valueOf(2), Integer.valueOf(1));
        IPV4_SCORES.put(Integer.valueOf(5), Integer.valueOf(1));
        IPV4_SCORES.put(Integer.valueOf(6), Integer.valueOf(1));
        IPV4_SCORES.put(Integer.valueOf(7), Integer.valueOf(1));
        IPV4_SCORES.put(Integer.valueOf(1), Integer.valueOf(2));
        IPV4_SCORES.put(Integer.valueOf(3), Integer.valueOf(2));
        IPV4_SCORES.put(Integer.valueOf(4), Integer.valueOf(2));
        IPV6_SCORES.put(Integer.valueOf(0), Integer.valueOf(0));
        IPV6_SCORES.put(Integer.valueOf(2), Integer.valueOf(1));
        IPV6_SCORES.put(Integer.valueOf(1), Integer.valueOf(2));
    }

    public static int calculateScore(boolean isHomeProvider, ScanDetail scanDetail, Map<ANQPElementType, ANQPElement> anqpElements, boolean isActiveNetwork) {
        int i = 0;
        NetworkDetail networkDetail = scanDetail.getNetworkDetail();
        int score = 0;
        if (isHomeProvider) {
            score = 100;
        }
        score = (score + ((networkDetail.isInternet() ? 1 : -1) * 50)) + ((Integer) NETWORK_TYPE_SCORES.get(networkDetail.getAnt())).intValue();
        if (anqpElements != null) {
            HSWanMetricsElement wm = (HSWanMetricsElement) anqpElements.get(ANQPElementType.HSWANMetrics);
            if (wm != null && (wm.getStatus() != 1 || wm.isCapped())) {
                score -= 50;
            }
            IPAddressTypeAvailabilityElement ipa = (IPAddressTypeAvailabilityElement) anqpElements.get(ANQPElementType.ANQPIPAddrAvailability);
            if (ipa != null) {
                int intValue;
                Integer v4Score = (Integer) IPV4_SCORES.get(Integer.valueOf(ipa.getV4Availability()));
                Integer v6Score = (Integer) IPV6_SCORES.get(Integer.valueOf(ipa.getV6Availability()));
                if (v4Score != null) {
                    intValue = v4Score.intValue();
                } else {
                    intValue = 0;
                }
                v4Score = Integer.valueOf(intValue);
                if (v6Score != null) {
                    i = v6Score.intValue();
                }
                score += v4Score.intValue() + Integer.valueOf(i).intValue();
            }
        }
        return score + RSSI_SCORE.lookupScore(scanDetail.getScanResult().level, isActiveNetwork);
    }
}
