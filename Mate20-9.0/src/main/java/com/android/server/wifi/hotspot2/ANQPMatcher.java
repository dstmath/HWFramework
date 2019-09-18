package com.android.server.wifi.hotspot2;

import com.android.server.wifi.IMSIParameter;
import com.android.server.wifi.hotspot2.anqp.CellularNetwork;
import com.android.server.wifi.hotspot2.anqp.DomainNameElement;
import com.android.server.wifi.hotspot2.anqp.NAIRealmData;
import com.android.server.wifi.hotspot2.anqp.NAIRealmElement;
import com.android.server.wifi.hotspot2.anqp.RoamingConsortiumElement;
import com.android.server.wifi.hotspot2.anqp.ThreeGPPNetworkElement;
import com.android.server.wifi.hotspot2.anqp.eap.AuthParam;
import com.android.server.wifi.hotspot2.anqp.eap.EAPMethod;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class ANQPMatcher {
    public static boolean matchDomainName(DomainNameElement element, String fqdn, IMSIParameter imsiParam, List<String> simImsiList) {
        if (element == null) {
            return false;
        }
        for (String domain : element.getDomains()) {
            if (DomainMatcher.arg2SubdomainOfArg1(fqdn, domain)) {
                return true;
            }
            if (matchMccMnc(Utils.getMccMnc(Utils.splitDomain(domain)), imsiParam, simImsiList)) {
                return true;
            }
        }
        return false;
    }

    public static boolean matchRoamingConsortium(RoamingConsortiumElement element, long[] providerOIs) {
        if (element == null || providerOIs == null) {
            return false;
        }
        List<Long> rcOIs = element.getOIs();
        for (long oi : providerOIs) {
            if (rcOIs.contains(Long.valueOf(oi))) {
                return true;
            }
        }
        return false;
    }

    public static int matchNAIRealm(NAIRealmElement element, String realm, int eapMethodID, AuthParam authParam) {
        if (element == null || element.getRealmDataList().isEmpty()) {
            return 0;
        }
        int bestMatch = -1;
        for (NAIRealmData realmData : element.getRealmDataList()) {
            int match = matchNAIRealmData(realmData, realm, eapMethodID, authParam);
            if (match > bestMatch) {
                bestMatch = match;
                if (bestMatch == 7) {
                    break;
                }
            }
        }
        return bestMatch;
    }

    public static boolean matchThreeGPPNetwork(ThreeGPPNetworkElement element, IMSIParameter imsiParam, List<String> simImsiList) {
        if (element == null) {
            return false;
        }
        for (CellularNetwork network : element.getNetworks()) {
            if (matchCellularNetwork(network, imsiParam, simImsiList)) {
                return true;
            }
        }
        return false;
    }

    private static int matchNAIRealmData(NAIRealmData realmData, String realm, int eapMethodID, AuthParam authParam) {
        int realmMatch = -1;
        Iterator<String> it = realmData.getRealms().iterator();
        while (true) {
            if (it.hasNext()) {
                if (DomainMatcher.arg2SubdomainOfArg1(realm, it.next())) {
                    realmMatch = 4;
                    break;
                }
            } else {
                break;
            }
        }
        if (realmData.getEAPMethods().isEmpty()) {
            return realmMatch;
        }
        int eapMethodMatch = -1;
        for (EAPMethod eapMethod : realmData.getEAPMethods()) {
            eapMethodMatch = matchEAPMethod(eapMethod, eapMethodID, authParam);
            if (eapMethodMatch != -1) {
                break;
            }
        }
        if (eapMethodMatch == -1) {
            return -1;
        }
        if (realmMatch == -1) {
            return eapMethodMatch;
        }
        return realmMatch | eapMethodMatch;
    }

    private static int matchEAPMethod(EAPMethod method, int eapMethodID, AuthParam authParam) {
        if (method.getEAPMethodID() != eapMethodID) {
            return -1;
        }
        if (authParam == null) {
            return 2;
        }
        Set<AuthParam> paramSet = method.getAuthParams().get(Integer.valueOf(authParam.getAuthTypeID()));
        if (paramSet == null || !paramSet.contains(authParam)) {
            return -1;
        }
        return 3;
    }

    private static boolean matchCellularNetwork(CellularNetwork network, IMSIParameter imsiParam, List<String> simImsiList) {
        for (String plmn : network.getPlmns()) {
            if (matchMccMnc(plmn, imsiParam, simImsiList)) {
                return true;
            }
        }
        return false;
    }

    private static boolean matchMccMnc(String mccMnc, IMSIParameter imsiParam, List<String> simImsiList) {
        if (imsiParam == null || simImsiList == null || !imsiParam.matchesMccMnc(mccMnc)) {
            return false;
        }
        for (String imsi : simImsiList) {
            if (imsi.startsWith(mccMnc)) {
                return true;
            }
        }
        return false;
    }
}
