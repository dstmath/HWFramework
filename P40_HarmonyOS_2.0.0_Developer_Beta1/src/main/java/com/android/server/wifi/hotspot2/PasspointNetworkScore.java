package com.android.server.wifi.hotspot2;

import android.net.RssiCurve;
import com.android.internal.annotations.VisibleForTesting;
import com.android.server.wifi.ScanDetail;
import com.android.server.wifi.hotspot2.NetworkDetail;
import com.android.server.wifi.hotspot2.anqp.ANQPElement;
import com.android.server.wifi.hotspot2.anqp.Constants;
import com.android.server.wifi.hotspot2.anqp.HSWanMetricsElement;
import com.android.server.wifi.hotspot2.anqp.IPAddressTypeAvailabilityElement;
import java.util.HashMap;
import java.util.Map;

public class PasspointNetworkScore {
    @VisibleForTesting
    public static final int HOME_PROVIDER_AWARD = 100;
    @VisibleForTesting
    public static final int INTERNET_ACCESS_AWARD = 50;
    private static final Map<Integer, Integer> IPV4_SCORES = new HashMap();
    private static final Map<Integer, Integer> IPV6_SCORES = new HashMap();
    private static final Map<NetworkDetail.Ant, Integer> NETWORK_TYPE_SCORES = new HashMap();
    @VisibleForTesting
    public static final int PERSONAL_OR_EMERGENCY_NETWORK_AWARDS = 2;
    @VisibleForTesting
    public static final int PUBLIC_OR_PRIVATE_NETWORK_AWARDS = 4;
    @VisibleForTesting
    public static final int RESTRICTED_OR_UNKNOWN_IP_AWARDS = 1;
    @VisibleForTesting
    public static final RssiCurve RSSI_SCORE = new RssiCurve(-80, 20, new byte[]{-10, 0, 10, 20, 30, 40}, 20);
    @VisibleForTesting
    public static final int UNRESTRICTED_IP_AWARDS = 2;
    @VisibleForTesting
    public static final int WAN_PORT_DOWN_OR_CAPPED_PENALTY = 50;

    static {
        NETWORK_TYPE_SCORES.put(NetworkDetail.Ant.FreePublic, 4);
        NETWORK_TYPE_SCORES.put(NetworkDetail.Ant.ChargeablePublic, 4);
        NETWORK_TYPE_SCORES.put(NetworkDetail.Ant.PrivateWithGuest, 4);
        NETWORK_TYPE_SCORES.put(NetworkDetail.Ant.Private, 4);
        NETWORK_TYPE_SCORES.put(NetworkDetail.Ant.Personal, 2);
        NETWORK_TYPE_SCORES.put(NetworkDetail.Ant.EmergencyOnly, 2);
        NETWORK_TYPE_SCORES.put(NetworkDetail.Ant.Wildcard, 0);
        NETWORK_TYPE_SCORES.put(NetworkDetail.Ant.TestOrExperimental, 0);
        IPV4_SCORES.put(0, 0);
        IPV4_SCORES.put(2, 1);
        IPV4_SCORES.put(5, 1);
        IPV4_SCORES.put(6, 1);
        IPV4_SCORES.put(7, 1);
        IPV4_SCORES.put(1, 2);
        IPV4_SCORES.put(3, 2);
        IPV4_SCORES.put(4, 2);
        IPV6_SCORES.put(0, 0);
        IPV6_SCORES.put(2, 1);
        IPV6_SCORES.put(1, 2);
    }

    public static int calculateScore(boolean isHomeProvider, ScanDetail scanDetail, Map<Constants.ANQPElementType, ANQPElement> anqpElements, boolean isActiveNetwork) {
        NetworkDetail networkDetail = scanDetail.getNetworkDetail();
        int score = 0;
        if (isHomeProvider) {
            score = 0 + 100;
        }
        int score2 = score + ((networkDetail.isInternet() ? 1 : -1) * 50);
        Integer ndScore = NETWORK_TYPE_SCORES.get(networkDetail.getAnt());
        if (ndScore != null) {
            score2 += ndScore.intValue();
        }
        if (anqpElements != null) {
            HSWanMetricsElement wm = (HSWanMetricsElement) anqpElements.get(Constants.ANQPElementType.HSWANMetrics);
            if (wm != null && (wm.getStatus() != 1 || wm.isCapped())) {
                score2 -= 50;
            }
            IPAddressTypeAvailabilityElement ipa = (IPAddressTypeAvailabilityElement) anqpElements.get(Constants.ANQPElementType.ANQPIPAddrAvailability);
            if (ipa != null) {
                Integer v4Score = IPV4_SCORES.get(Integer.valueOf(ipa.getV4Availability()));
                Integer v6Score = IPV6_SCORES.get(Integer.valueOf(ipa.getV6Availability()));
                int i = 0;
                Integer v4Score2 = Integer.valueOf(v4Score != null ? v4Score.intValue() : 0);
                if (v6Score != null) {
                    i = v6Score.intValue();
                }
                score2 += v4Score2.intValue() + Integer.valueOf(i).intValue();
            }
        }
        return score2 + RSSI_SCORE.lookupScore(scanDetail.getScanResult().level, isActiveNetwork);
    }
}
