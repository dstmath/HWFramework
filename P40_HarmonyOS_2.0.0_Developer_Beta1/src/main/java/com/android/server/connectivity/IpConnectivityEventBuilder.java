package com.android.server.connectivity;

import android.net.ConnectivityMetricsEvent;
import android.net.metrics.ApfProgramEvent;
import android.net.metrics.ApfStats;
import android.net.metrics.ConnectStats;
import android.net.metrics.DefaultNetworkEvent;
import android.net.metrics.DhcpClientEvent;
import android.net.metrics.DhcpErrorEvent;
import android.net.metrics.DnsEvent;
import android.net.metrics.IpManagerEvent;
import android.net.metrics.IpReachabilityEvent;
import android.net.metrics.NetworkEvent;
import android.net.metrics.RaEvent;
import android.net.metrics.ValidationProbeEvent;
import android.net.metrics.WakeupStats;
import android.os.Parcelable;
import android.util.SparseIntArray;
import com.android.server.connectivity.metrics.nano.IpConnectivityLogClass;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public final class IpConnectivityEventBuilder {
    private static final int[] IFNAME_LINKLAYERS = new int[7];
    private static final String[] IFNAME_PREFIXES = new String[7];
    private static final int KNOWN_PREFIX = 7;
    private static final int[] TRANSPORT_LINKLAYER_MAP = new int[8];

    private IpConnectivityEventBuilder() {
    }

    public static byte[] serialize(int dropped, List<IpConnectivityLogClass.IpConnectivityEvent> events) throws IOException {
        IpConnectivityLogClass.IpConnectivityLog log = new IpConnectivityLogClass.IpConnectivityLog();
        log.events = (IpConnectivityLogClass.IpConnectivityEvent[]) events.toArray(new IpConnectivityLogClass.IpConnectivityEvent[events.size()]);
        log.droppedEvents = dropped;
        if (log.events.length > 0 || dropped > 0) {
            log.version = 2;
        }
        return IpConnectivityLogClass.IpConnectivityLog.toByteArray(log);
    }

    public static List<IpConnectivityLogClass.IpConnectivityEvent> toProto(List<ConnectivityMetricsEvent> eventsIn) {
        ArrayList<IpConnectivityLogClass.IpConnectivityEvent> eventsOut = new ArrayList<>(eventsIn.size());
        for (ConnectivityMetricsEvent in : eventsIn) {
            IpConnectivityLogClass.IpConnectivityEvent out = toProto(in);
            if (out != null) {
                eventsOut.add(out);
            }
        }
        return eventsOut;
    }

    public static IpConnectivityLogClass.IpConnectivityEvent toProto(ConnectivityMetricsEvent ev) {
        IpConnectivityLogClass.IpConnectivityEvent out = buildEvent(ev.netId, ev.transports, ev.ifname);
        out.timeMs = ev.timestamp;
        if (!setEvent(out, ev.data)) {
            return null;
        }
        return out;
    }

    public static IpConnectivityLogClass.IpConnectivityEvent toProto(ConnectStats in) {
        IpConnectivityLogClass.ConnectStatistics stats = new IpConnectivityLogClass.ConnectStatistics();
        stats.connectCount = in.connectCount;
        stats.connectBlockingCount = in.connectBlockingCount;
        stats.ipv6AddrCount = in.ipv6ConnectCount;
        stats.latenciesMs = in.latencies.toArray();
        stats.errnosCounters = toPairArray(in.errnos);
        IpConnectivityLogClass.IpConnectivityEvent out = buildEvent(in.netId, in.transports, null);
        out.setConnectStatistics(stats);
        return out;
    }

    public static IpConnectivityLogClass.IpConnectivityEvent toProto(DnsEvent in) {
        IpConnectivityLogClass.DNSLookupBatch dnsLookupBatch = new IpConnectivityLogClass.DNSLookupBatch();
        in.resize(in.eventCount);
        dnsLookupBatch.eventTypes = bytesToInts(in.eventTypes);
        dnsLookupBatch.returnCodes = bytesToInts(in.returnCodes);
        dnsLookupBatch.latenciesMs = in.latenciesMs;
        IpConnectivityLogClass.IpConnectivityEvent out = buildEvent(in.netId, in.transports, null);
        out.setDnsLookupBatch(dnsLookupBatch);
        return out;
    }

    public static IpConnectivityLogClass.IpConnectivityEvent toProto(WakeupStats in) {
        IpConnectivityLogClass.WakeupStats wakeupStats = new IpConnectivityLogClass.WakeupStats();
        in.updateDuration();
        wakeupStats.durationSec = in.durationSec;
        wakeupStats.totalWakeups = in.totalWakeups;
        wakeupStats.rootWakeups = in.rootWakeups;
        wakeupStats.systemWakeups = in.systemWakeups;
        wakeupStats.nonApplicationWakeups = in.nonApplicationWakeups;
        wakeupStats.applicationWakeups = in.applicationWakeups;
        wakeupStats.noUidWakeups = in.noUidWakeups;
        wakeupStats.l2UnicastCount = in.l2UnicastCount;
        wakeupStats.l2MulticastCount = in.l2MulticastCount;
        wakeupStats.l2BroadcastCount = in.l2BroadcastCount;
        wakeupStats.ethertypeCounts = toPairArray(in.ethertypes);
        wakeupStats.ipNextHeaderCounts = toPairArray(in.ipNextHeaders);
        IpConnectivityLogClass.IpConnectivityEvent out = buildEvent(0, 0, in.iface);
        out.setWakeupStats(wakeupStats);
        return out;
    }

    public static IpConnectivityLogClass.IpConnectivityEvent toProto(DefaultNetworkEvent in) {
        IpConnectivityLogClass.DefaultNetworkEvent ev = new IpConnectivityLogClass.DefaultNetworkEvent();
        ev.finalScore = (long) in.finalScore;
        ev.initialScore = (long) in.initialScore;
        ev.ipSupport = ipSupportOf(in);
        ev.defaultNetworkDurationMs = in.durationMs;
        ev.validationDurationMs = in.validatedMs;
        ev.previousDefaultNetworkLinkLayer = transportsToLinkLayer((long) in.previousTransports);
        IpConnectivityLogClass.IpConnectivityEvent out = buildEvent(in.netId, (long) in.transports, null);
        if (in.transports == 0) {
            out.linkLayer = 5;
        }
        out.setDefaultNetworkEvent(ev);
        return out;
    }

    private static IpConnectivityLogClass.IpConnectivityEvent buildEvent(int netId, long transports, String ifname) {
        IpConnectivityLogClass.IpConnectivityEvent ev = new IpConnectivityLogClass.IpConnectivityEvent();
        ev.networkId = netId;
        ev.transports = transports;
        if (ifname != null) {
            ev.ifName = ifname;
        }
        inferLinkLayer(ev);
        return ev;
    }

    private static boolean setEvent(IpConnectivityLogClass.IpConnectivityEvent out, Parcelable in) {
        if (in instanceof DhcpErrorEvent) {
            setDhcpErrorEvent(out, (DhcpErrorEvent) in);
            return true;
        } else if (in instanceof DhcpClientEvent) {
            setDhcpClientEvent(out, (DhcpClientEvent) in);
            return true;
        } else if (in instanceof IpManagerEvent) {
            setIpManagerEvent(out, (IpManagerEvent) in);
            return true;
        } else if (in instanceof IpReachabilityEvent) {
            setIpReachabilityEvent(out, (IpReachabilityEvent) in);
            return true;
        } else if (in instanceof NetworkEvent) {
            setNetworkEvent(out, (NetworkEvent) in);
            return true;
        } else if (in instanceof ValidationProbeEvent) {
            setValidationProbeEvent(out, (ValidationProbeEvent) in);
            return true;
        } else if (in instanceof ApfProgramEvent) {
            setApfProgramEvent(out, (ApfProgramEvent) in);
            return true;
        } else if (in instanceof ApfStats) {
            setApfStats(out, (ApfStats) in);
            return true;
        } else if (!(in instanceof RaEvent)) {
            return false;
        } else {
            setRaEvent(out, (RaEvent) in);
            return true;
        }
    }

    private static void setDhcpErrorEvent(IpConnectivityLogClass.IpConnectivityEvent out, DhcpErrorEvent in) {
        IpConnectivityLogClass.DHCPEvent dhcpEvent = new IpConnectivityLogClass.DHCPEvent();
        dhcpEvent.setErrorCode(in.errorCode);
        out.setDhcpEvent(dhcpEvent);
    }

    private static void setDhcpClientEvent(IpConnectivityLogClass.IpConnectivityEvent out, DhcpClientEvent in) {
        IpConnectivityLogClass.DHCPEvent dhcpEvent = new IpConnectivityLogClass.DHCPEvent();
        dhcpEvent.setStateTransition(in.msg);
        dhcpEvent.durationMs = in.durationMs;
        out.setDhcpEvent(dhcpEvent);
    }

    private static void setIpManagerEvent(IpConnectivityLogClass.IpConnectivityEvent out, IpManagerEvent in) {
        IpConnectivityLogClass.IpProvisioningEvent ipProvisioningEvent = new IpConnectivityLogClass.IpProvisioningEvent();
        ipProvisioningEvent.eventType = in.eventType;
        ipProvisioningEvent.latencyMs = (int) in.durationMs;
        out.setIpProvisioningEvent(ipProvisioningEvent);
    }

    private static void setIpReachabilityEvent(IpConnectivityLogClass.IpConnectivityEvent out, IpReachabilityEvent in) {
        IpConnectivityLogClass.IpReachabilityEvent ipReachabilityEvent = new IpConnectivityLogClass.IpReachabilityEvent();
        ipReachabilityEvent.eventType = in.eventType;
        out.setIpReachabilityEvent(ipReachabilityEvent);
    }

    private static void setNetworkEvent(IpConnectivityLogClass.IpConnectivityEvent out, NetworkEvent in) {
        IpConnectivityLogClass.NetworkEvent networkEvent = new IpConnectivityLogClass.NetworkEvent();
        networkEvent.eventType = in.eventType;
        networkEvent.latencyMs = (int) in.durationMs;
        out.setNetworkEvent(networkEvent);
    }

    private static void setValidationProbeEvent(IpConnectivityLogClass.IpConnectivityEvent out, ValidationProbeEvent in) {
        IpConnectivityLogClass.ValidationProbeEvent validationProbeEvent = new IpConnectivityLogClass.ValidationProbeEvent();
        validationProbeEvent.latencyMs = (int) in.durationMs;
        validationProbeEvent.probeType = in.probeType;
        validationProbeEvent.probeResult = in.returnCode;
        out.setValidationProbeEvent(validationProbeEvent);
    }

    private static void setApfProgramEvent(IpConnectivityLogClass.IpConnectivityEvent out, ApfProgramEvent in) {
        IpConnectivityLogClass.ApfProgramEvent apfProgramEvent = new IpConnectivityLogClass.ApfProgramEvent();
        apfProgramEvent.lifetime = in.lifetime;
        apfProgramEvent.effectiveLifetime = in.actualLifetime;
        apfProgramEvent.filteredRas = in.filteredRas;
        apfProgramEvent.currentRas = in.currentRas;
        apfProgramEvent.programLength = in.programLength;
        if (isBitSet(in.flags, 0)) {
            apfProgramEvent.dropMulticast = true;
        }
        if (isBitSet(in.flags, 1)) {
            apfProgramEvent.hasIpv4Addr = true;
        }
        out.setApfProgramEvent(apfProgramEvent);
    }

    private static void setApfStats(IpConnectivityLogClass.IpConnectivityEvent out, ApfStats in) {
        IpConnectivityLogClass.ApfStatistics apfStatistics = new IpConnectivityLogClass.ApfStatistics();
        apfStatistics.durationMs = in.durationMs;
        apfStatistics.receivedRas = in.receivedRas;
        apfStatistics.matchingRas = in.matchingRas;
        apfStatistics.droppedRas = in.droppedRas;
        apfStatistics.zeroLifetimeRas = in.zeroLifetimeRas;
        apfStatistics.parseErrors = in.parseErrors;
        apfStatistics.programUpdates = in.programUpdates;
        apfStatistics.programUpdatesAll = in.programUpdatesAll;
        apfStatistics.programUpdatesAllowingMulticast = in.programUpdatesAllowingMulticast;
        apfStatistics.maxProgramSize = in.maxProgramSize;
        out.setApfStatistics(apfStatistics);
    }

    private static void setRaEvent(IpConnectivityLogClass.IpConnectivityEvent out, RaEvent in) {
        IpConnectivityLogClass.RaEvent raEvent = new IpConnectivityLogClass.RaEvent();
        raEvent.routerLifetime = in.routerLifetime;
        raEvent.prefixValidLifetime = in.prefixValidLifetime;
        raEvent.prefixPreferredLifetime = in.prefixPreferredLifetime;
        raEvent.routeInfoLifetime = in.routeInfoLifetime;
        raEvent.rdnssLifetime = in.rdnssLifetime;
        raEvent.dnsslLifetime = in.dnsslLifetime;
        out.setRaEvent(raEvent);
    }

    private static int[] bytesToInts(byte[] in) {
        int[] out = new int[in.length];
        for (int i = 0; i < in.length; i++) {
            out[i] = in[i] & 255;
        }
        return out;
    }

    private static IpConnectivityLogClass.Pair[] toPairArray(SparseIntArray counts) {
        int s = counts.size();
        IpConnectivityLogClass.Pair[] pairs = new IpConnectivityLogClass.Pair[s];
        for (int i = 0; i < s; i++) {
            IpConnectivityLogClass.Pair p = new IpConnectivityLogClass.Pair();
            p.key = counts.keyAt(i);
            p.value = counts.valueAt(i);
            pairs[i] = p;
        }
        return pairs;
    }

    private static int ipSupportOf(DefaultNetworkEvent in) {
        if (in.ipv4 && in.ipv6) {
            return 3;
        }
        if (in.ipv6) {
            return 2;
        }
        if (in.ipv4) {
            return 1;
        }
        return 0;
    }

    private static boolean isBitSet(int flags, int bit) {
        return ((1 << bit) & flags) != 0;
    }

    private static void inferLinkLayer(IpConnectivityLogClass.IpConnectivityEvent ev) {
        int linkLayer = 0;
        if (ev.transports != 0) {
            linkLayer = transportsToLinkLayer(ev.transports);
        } else if (ev.ifName != null) {
            linkLayer = ifnameToLinkLayer(ev.ifName);
        }
        if (linkLayer != 0) {
            ev.linkLayer = linkLayer;
            ev.ifName = "";
        }
    }

    private static int transportsToLinkLayer(long transports) {
        int bitCount = Long.bitCount(transports);
        if (bitCount == 0) {
            return 0;
        }
        if (bitCount != 1) {
            return 6;
        }
        return transportToLinkLayer(Long.numberOfTrailingZeros(transports));
    }

    private static int transportToLinkLayer(int transport) {
        if (transport < 0) {
            return 0;
        }
        int[] iArr = TRANSPORT_LINKLAYER_MAP;
        if (transport < iArr.length) {
            return iArr[transport];
        }
        return 0;
    }

    static {
        int[] iArr = TRANSPORT_LINKLAYER_MAP;
        iArr[0] = 2;
        iArr[1] = 4;
        iArr[2] = 1;
        iArr[3] = 3;
        iArr[4] = 0;
        iArr[5] = 8;
        iArr[6] = 9;
        String[] strArr = IFNAME_PREFIXES;
        strArr[0] = "rmnet";
        int[] iArr2 = IFNAME_LINKLAYERS;
        iArr2[0] = 2;
        strArr[1] = "wlan";
        iArr2[1] = 4;
        strArr[2] = "bt-pan";
        iArr2[2] = 1;
        strArr[3] = "p2p";
        iArr2[3] = 7;
        strArr[4] = "aware";
        iArr2[4] = 8;
        strArr[5] = "eth";
        iArr2[5] = 3;
        strArr[6] = "wpan";
        iArr2[6] = 9;
    }

    private static int ifnameToLinkLayer(String ifname) {
        for (int i = 0; i < 7; i++) {
            if (ifname.startsWith(IFNAME_PREFIXES[i])) {
                return IFNAME_LINKLAYERS[i];
            }
        }
        return 0;
    }
}
