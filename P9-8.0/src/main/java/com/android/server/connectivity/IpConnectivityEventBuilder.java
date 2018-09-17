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
import android.os.Parcelable;
import android.util.SparseArray;
import android.util.SparseIntArray;
import com.android.server.connectivity.metrics.nano.IpConnectivityLogClass;
import com.android.server.connectivity.metrics.nano.IpConnectivityLogClass.ApfStatistics;
import com.android.server.connectivity.metrics.nano.IpConnectivityLogClass.ConnectStatistics;
import com.android.server.connectivity.metrics.nano.IpConnectivityLogClass.DHCPEvent;
import com.android.server.connectivity.metrics.nano.IpConnectivityLogClass.DNSLookupBatch;
import com.android.server.connectivity.metrics.nano.IpConnectivityLogClass.IpConnectivityEvent;
import com.android.server.connectivity.metrics.nano.IpConnectivityLogClass.IpConnectivityLog;
import com.android.server.connectivity.metrics.nano.IpConnectivityLogClass.IpProvisioningEvent;
import com.android.server.connectivity.metrics.nano.IpConnectivityLogClass.NetworkId;
import com.android.server.connectivity.metrics.nano.IpConnectivityLogClass.Pair;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public final class IpConnectivityEventBuilder {
    private static final SparseArray<String> IFNAME_LINKLAYER_MAP = new SparseArray();
    private static final int[] TRANSPORT_LINKLAYER_MAP = new int[6];

    private IpConnectivityEventBuilder() {
    }

    public static byte[] serialize(int dropped, List<IpConnectivityEvent> events) throws IOException {
        IpConnectivityLog log = new IpConnectivityLog();
        log.events = (IpConnectivityEvent[]) events.toArray(new IpConnectivityEvent[events.size()]);
        log.droppedEvents = dropped;
        if (log.events.length > 0 || dropped > 0) {
            log.version = 2;
        }
        return IpConnectivityLog.toByteArray(log);
    }

    public static List<IpConnectivityEvent> toProto(List<ConnectivityMetricsEvent> eventsIn) {
        ArrayList<IpConnectivityEvent> eventsOut = new ArrayList(eventsIn.size());
        for (ConnectivityMetricsEvent in : eventsIn) {
            IpConnectivityEvent out = toProto(in);
            if (out != null) {
                eventsOut.add(out);
            }
        }
        return eventsOut;
    }

    public static IpConnectivityEvent toProto(ConnectivityMetricsEvent ev) {
        IpConnectivityEvent out = buildEvent(ev.netId, ev.transports, ev.ifname);
        out.timeMs = ev.timestamp;
        if (setEvent(out, ev.data)) {
            return out;
        }
        return null;
    }

    public static IpConnectivityEvent toProto(ConnectStats in) {
        ConnectStatistics stats = new ConnectStatistics();
        stats.connectCount = in.connectCount;
        stats.connectBlockingCount = in.connectBlockingCount;
        stats.ipv6AddrCount = in.ipv6ConnectCount;
        stats.latenciesMs = in.latencies.toArray();
        stats.errnosCounters = toPairArray(in.errnos);
        IpConnectivityEvent out = buildEvent(in.netId, in.transports, null);
        out.setConnectStatistics(stats);
        return out;
    }

    public static IpConnectivityEvent toProto(DnsEvent in) {
        DNSLookupBatch dnsLookupBatch = new DNSLookupBatch();
        in.resize(in.eventCount);
        dnsLookupBatch.eventTypes = bytesToInts(in.eventTypes);
        dnsLookupBatch.returnCodes = bytesToInts(in.returnCodes);
        dnsLookupBatch.latenciesMs = in.latenciesMs;
        IpConnectivityEvent out = buildEvent(in.netId, in.transports, null);
        out.setDnsLookupBatch(dnsLookupBatch);
        return out;
    }

    private static IpConnectivityEvent buildEvent(int netId, long transports, String ifname) {
        IpConnectivityEvent ev = new IpConnectivityEvent();
        ev.networkId = netId;
        ev.transports = transports;
        if (ifname != null) {
            ev.ifName = ifname;
        }
        inferLinkLayer(ev);
        return ev;
    }

    private static boolean setEvent(IpConnectivityEvent out, Parcelable in) {
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
        } else if (in instanceof DefaultNetworkEvent) {
            setDefaultNetworkEvent(out, (DefaultNetworkEvent) in);
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

    private static void setDhcpErrorEvent(IpConnectivityEvent out, DhcpErrorEvent in) {
        DHCPEvent dhcpEvent = new DHCPEvent();
        dhcpEvent.setErrorCode(in.errorCode);
        out.setDhcpEvent(dhcpEvent);
    }

    private static void setDhcpClientEvent(IpConnectivityEvent out, DhcpClientEvent in) {
        DHCPEvent dhcpEvent = new DHCPEvent();
        dhcpEvent.setStateTransition(in.msg);
        dhcpEvent.durationMs = in.durationMs;
        out.setDhcpEvent(dhcpEvent);
    }

    private static void setIpManagerEvent(IpConnectivityEvent out, IpManagerEvent in) {
        IpProvisioningEvent ipProvisioningEvent = new IpProvisioningEvent();
        ipProvisioningEvent.eventType = in.eventType;
        ipProvisioningEvent.latencyMs = (int) in.durationMs;
        out.setIpProvisioningEvent(ipProvisioningEvent);
    }

    private static void setIpReachabilityEvent(IpConnectivityEvent out, IpReachabilityEvent in) {
        IpConnectivityLogClass.IpReachabilityEvent ipReachabilityEvent = new IpConnectivityLogClass.IpReachabilityEvent();
        ipReachabilityEvent.eventType = in.eventType;
        out.setIpReachabilityEvent(ipReachabilityEvent);
    }

    private static void setDefaultNetworkEvent(IpConnectivityEvent out, DefaultNetworkEvent in) {
        IpConnectivityLogClass.DefaultNetworkEvent defaultNetworkEvent = new IpConnectivityLogClass.DefaultNetworkEvent();
        defaultNetworkEvent.networkId = netIdOf(in.netId);
        defaultNetworkEvent.previousNetworkId = netIdOf(in.prevNetId);
        defaultNetworkEvent.transportTypes = in.transportTypes;
        defaultNetworkEvent.previousNetworkIpSupport = ipSupportOf(in);
        out.setDefaultNetworkEvent(defaultNetworkEvent);
    }

    private static void setNetworkEvent(IpConnectivityEvent out, NetworkEvent in) {
        IpConnectivityLogClass.NetworkEvent networkEvent = new IpConnectivityLogClass.NetworkEvent();
        networkEvent.networkId = netIdOf(in.netId);
        networkEvent.eventType = in.eventType;
        networkEvent.latencyMs = (int) in.durationMs;
        out.setNetworkEvent(networkEvent);
    }

    private static void setValidationProbeEvent(IpConnectivityEvent out, ValidationProbeEvent in) {
        IpConnectivityLogClass.ValidationProbeEvent validationProbeEvent = new IpConnectivityLogClass.ValidationProbeEvent();
        validationProbeEvent.latencyMs = (int) in.durationMs;
        validationProbeEvent.probeType = in.probeType;
        validationProbeEvent.probeResult = in.returnCode;
        out.setValidationProbeEvent(validationProbeEvent);
    }

    private static void setApfProgramEvent(IpConnectivityEvent out, ApfProgramEvent in) {
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

    private static void setApfStats(IpConnectivityEvent out, ApfStats in) {
        ApfStatistics apfStatistics = new ApfStatistics();
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

    private static void setRaEvent(IpConnectivityEvent out, RaEvent in) {
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

    private static Pair[] toPairArray(SparseIntArray counts) {
        int s = counts.size();
        Pair[] pairs = new Pair[s];
        for (int i = 0; i < s; i++) {
            Pair p = new Pair();
            p.key = counts.keyAt(i);
            p.value = counts.valueAt(i);
            pairs[i] = p;
        }
        return pairs;
    }

    private static NetworkId netIdOf(int netid) {
        NetworkId ni = new NetworkId();
        ni.networkId = netid;
        return ni;
    }

    private static int ipSupportOf(DefaultNetworkEvent in) {
        if (in.prevIPv4 && in.prevIPv6) {
            return 3;
        }
        if (in.prevIPv6) {
            return 2;
        }
        if (in.prevIPv4) {
            return 1;
        }
        return 0;
    }

    private static boolean isBitSet(int flags, int bit) {
        return ((1 << bit) & flags) != 0;
    }

    private static void inferLinkLayer(IpConnectivityEvent ev) {
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
        switch (Long.bitCount(transports)) {
            case 0:
                return 0;
            case 1:
                return transportToLinkLayer(Long.numberOfTrailingZeros(transports));
            default:
                return 6;
        }
    }

    private static int transportToLinkLayer(int transport) {
        if (transport < 0 || transport >= TRANSPORT_LINKLAYER_MAP.length) {
            return 0;
        }
        return TRANSPORT_LINKLAYER_MAP[transport];
    }

    static {
        TRANSPORT_LINKLAYER_MAP[0] = 2;
        TRANSPORT_LINKLAYER_MAP[1] = 4;
        TRANSPORT_LINKLAYER_MAP[2] = 1;
        TRANSPORT_LINKLAYER_MAP[3] = 3;
        TRANSPORT_LINKLAYER_MAP[4] = 0;
        TRANSPORT_LINKLAYER_MAP[5] = 0;
        IFNAME_LINKLAYER_MAP.put(2, "rmnet");
        IFNAME_LINKLAYER_MAP.put(4, "wlan");
        IFNAME_LINKLAYER_MAP.put(1, "bt-pan");
        IFNAME_LINKLAYER_MAP.put(3, "usb");
    }

    private static int ifnameToLinkLayer(String ifname) {
        for (int i = 0; i < IFNAME_LINKLAYER_MAP.size(); i++) {
            if (ifname.startsWith((String) IFNAME_LINKLAYER_MAP.valueAt(i))) {
                return IFNAME_LINKLAYER_MAP.keyAt(i);
            }
        }
        return 0;
    }
}
