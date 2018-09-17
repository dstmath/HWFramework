package com.android.server.wifi.hotspot2;

import com.android.server.wifi.ScanDetail;
import com.android.server.wifi.anqp.ANQPElement;
import com.android.server.wifi.anqp.Constants.ANQPElementType;
import com.android.server.wifi.anqp.HSConnectionCapabilityElement;
import com.android.server.wifi.anqp.HSConnectionCapabilityElement.ProtoStatus;
import com.android.server.wifi.anqp.HSConnectionCapabilityElement.ProtocolTuple;
import com.android.server.wifi.anqp.HSWanMetricsElement;
import com.android.server.wifi.anqp.HSWanMetricsElement.LinkStatus;
import com.android.server.wifi.anqp.IPAddressTypeAvailabilityElement;
import com.android.server.wifi.anqp.IPAddressTypeAvailabilityElement.IPv4Availability;
import com.android.server.wifi.anqp.IPAddressTypeAvailabilityElement.IPv6Availability;
import com.android.server.wifi.hotspot2.NetworkDetail.Ant;
import com.android.server.wifi.hotspot2.NetworkDetail.HSRelease;
import com.android.server.wifi.hotspot2.pps.HomeSP;
import java.util.Map;

public class PasspointMatchInfo implements Comparable<PasspointMatchInfo> {
    private static final int IPPROTO_ESP = 50;
    private static final int IPPROTO_ICMP = 1;
    private static final int IPPROTO_TCP = 6;
    private static final int IPPROTO_UDP = 17;
    private static final Map<Ant, Integer> sAntScores = null;
    private static final Map<IPv4Availability, Integer> sIP4Scores = null;
    private static final Map<IPv6Availability, Integer> sIP6Scores = null;
    private static final Map<Integer, Map<Integer, Integer>> sPortScores = null;
    private final HomeSP mHomeSP;
    private final PasspointMatch mPasspointMatch;
    private final ScanDetail mScanDetail;
    private final int mScore;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.wifi.hotspot2.PasspointMatchInfo.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.wifi.hotspot2.PasspointMatchInfo.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.wifi.hotspot2.PasspointMatchInfo.<clinit>():void");
    }

    public PasspointMatchInfo(PasspointMatch passpointMatch, ScanDetail scanDetail, HomeSP homeSP) {
        int score;
        this.mPasspointMatch = passpointMatch;
        this.mScanDetail = scanDetail;
        this.mHomeSP = homeSP;
        if (passpointMatch == PasspointMatch.HomeProvider) {
            score = 100;
        } else if (passpointMatch == PasspointMatch.RoamingProvider) {
            score = 0;
        } else {
            score = -1000;
        }
        if (getNetworkDetail().getHSRelease() != null) {
            score += getNetworkDetail().getHSRelease() != HSRelease.Unknown ? IPPROTO_ESP : 0;
        }
        if (getNetworkDetail().hasInterworking()) {
            score += getNetworkDetail().isInternet() ? 20 : -20;
        }
        score += ((Math.max(200 - getNetworkDetail().getStationCount(), 0) * (255 - getNetworkDetail().getChannelUtilization())) * getNetworkDetail().getCapacity()) >>> 26;
        if (getNetworkDetail().hasInterworking()) {
            score += ((Integer) sAntScores.get(getNetworkDetail().getAnt())).intValue();
        }
        Map<ANQPElementType, ANQPElement> anqp = getNetworkDetail().getANQPElements();
        if (anqp != null) {
            HSWanMetricsElement wm = (HSWanMetricsElement) anqp.get(ANQPElementType.HSWANMetrics);
            if (wm != null) {
                if (wm.getStatus() != LinkStatus.Up || wm.isCapped()) {
                    score -= 1000;
                } else {
                    score = (int) (((long) score) + (Math.min(((wm.getDlSpeed() * ((long) (255 - wm.getDlLoad()))) * 8) + ((wm.getUlSpeed() * ((long) (255 - wm.getUlLoad()))) * 2), 255000000) >>> 23));
                }
            }
            IPAddressTypeAvailabilityElement ipa = (IPAddressTypeAvailabilityElement) anqp.get(ANQPElementType.ANQPIPAddrAvailability);
            if (ipa != null) {
                Integer as14 = (Integer) sIP4Scores.get(ipa.getV4Availability());
                Integer as16 = (Integer) sIP6Scores.get(ipa.getV6Availability());
                score += (Integer.valueOf(as14 != null ? as14.intValue() : IPPROTO_ICMP).intValue() * 2) + Integer.valueOf(as16 != null ? as16.intValue() : IPPROTO_ICMP).intValue();
            }
            HSConnectionCapabilityElement cce = (HSConnectionCapabilityElement) anqp.get(ANQPElementType.HSConnCapability);
            if (cce != null) {
                score = Math.min(Math.max(protoScore(cce) >> 3, -10), 10);
            }
        }
        this.mScore = score;
    }

    public PasspointMatch getPasspointMatch() {
        return this.mPasspointMatch;
    }

    public ScanDetail getScanDetail() {
        return this.mScanDetail;
    }

    public NetworkDetail getNetworkDetail() {
        return this.mScanDetail.getNetworkDetail();
    }

    public HomeSP getHomeSP() {
        return this.mHomeSP;
    }

    public int getScore() {
        return this.mScore;
    }

    public int compareTo(PasspointMatchInfo that) {
        return getScore() - that.getScore();
    }

    private static int protoScore(HSConnectionCapabilityElement cce) {
        int score = 0;
        for (ProtocolTuple tuple : cce.getStatusList()) {
            int sign = tuple.getStatus() == ProtoStatus.Open ? IPPROTO_ICMP : -1;
            int elementScore = IPPROTO_ICMP;
            if (tuple.getProtocol() == IPPROTO_ICMP) {
                elementScore = IPPROTO_ICMP;
            } else if (tuple.getProtocol() == IPPROTO_ESP) {
                elementScore = 5;
            } else {
                Map<Integer, Integer> protoMap = (Map) sPortScores.get(Integer.valueOf(tuple.getProtocol()));
                if (protoMap != null) {
                    Integer portScore = (Integer) protoMap.get(Integer.valueOf(tuple.getPort()));
                    elementScore = portScore != null ? portScore.intValue() : 0;
                }
            }
            score += elementScore * sign;
        }
        return score;
    }

    public boolean equals(Object thatObject) {
        boolean z = false;
        if (this == thatObject) {
            return true;
        }
        if (thatObject == null || getClass() != thatObject.getClass()) {
            return false;
        }
        PasspointMatchInfo that = (PasspointMatchInfo) thatObject;
        if (getNetworkDetail().equals(that.getNetworkDetail()) && getHomeSP().equals(that.getHomeSP())) {
            z = getPasspointMatch().equals(that.getPasspointMatch());
        }
        return z;
    }

    public int hashCode() {
        int result;
        int i = 0;
        if (this.mPasspointMatch != null) {
            result = this.mPasspointMatch.hashCode();
        } else {
            result = 0;
        }
        int hashCode = ((result * 31) + getNetworkDetail().hashCode()) * 31;
        if (this.mHomeSP != null) {
            i = this.mHomeSP.hashCode();
        }
        return hashCode + i;
    }

    public String toString() {
        return "PasspointMatchInfo{, mPasspointMatch=" + this.mPasspointMatch + ", mNetworkInfo=" + getNetworkDetail().getSSID() + ", mHomeSP=" + this.mHomeSP.getFQDN() + '}';
    }
}
