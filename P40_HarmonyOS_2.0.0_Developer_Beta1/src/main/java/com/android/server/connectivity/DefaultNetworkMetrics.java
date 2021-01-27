package com.android.server.connectivity;

import android.net.LinkProperties;
import android.net.metrics.DefaultNetworkEvent;
import android.os.SystemClock;
import com.android.internal.annotations.GuardedBy;
import com.android.internal.util.BitUtils;
import com.android.internal.util.RingBuffer;
import com.android.server.connectivity.metrics.nano.IpConnectivityLogClass;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

public class DefaultNetworkMetrics {
    private static final int ROLLING_LOG_SIZE = 64;
    public final long creationTimeMs = SystemClock.elapsedRealtime();
    @GuardedBy({"this"})
    private DefaultNetworkEvent mCurrentDefaultNetwork;
    @GuardedBy({"this"})
    private final List<DefaultNetworkEvent> mEvents = new ArrayList();
    @GuardedBy({"this"})
    private final RingBuffer<DefaultNetworkEvent> mEventsLog = new RingBuffer<>(DefaultNetworkEvent.class, 64);
    @GuardedBy({"this"})
    private boolean mIsCurrentlyValid;
    @GuardedBy({"this"})
    private int mLastTransports;
    @GuardedBy({"this"})
    private long mLastValidationTimeMs;

    public DefaultNetworkMetrics() {
        newDefaultNetwork(this.creationTimeMs, null);
    }

    public synchronized void listEvents(PrintWriter pw) {
        pw.println("default network events:");
        long localTimeMs = System.currentTimeMillis();
        long timeMs = SystemClock.elapsedRealtime();
        for (DefaultNetworkEvent ev : (DefaultNetworkEvent[]) this.mEventsLog.toArray()) {
            printEvent(localTimeMs, pw, ev);
        }
        this.mCurrentDefaultNetwork.updateDuration(timeMs);
        if (this.mIsCurrentlyValid) {
            updateValidationTime(timeMs);
            this.mLastValidationTimeMs = timeMs;
        }
        printEvent(localTimeMs, pw, this.mCurrentDefaultNetwork);
    }

    public synchronized void listEventsAsProto(PrintWriter pw) {
        for (DefaultNetworkEvent ev : (DefaultNetworkEvent[]) this.mEventsLog.toArray()) {
            pw.print(IpConnectivityEventBuilder.toProto(ev));
        }
    }

    public synchronized void flushEvents(List<IpConnectivityLogClass.IpConnectivityEvent> out) {
        for (DefaultNetworkEvent ev : this.mEvents) {
            out.add(IpConnectivityEventBuilder.toProto(ev));
        }
        this.mEvents.clear();
    }

    public synchronized void logDefaultNetworkValidity(long timeMs, boolean isValid) {
        if (!isValid) {
            try {
                if (this.mIsCurrentlyValid) {
                    this.mIsCurrentlyValid = false;
                    updateValidationTime(timeMs);
                }
            } catch (Throwable th) {
                throw th;
            }
        }
        if (isValid && !this.mIsCurrentlyValid) {
            this.mIsCurrentlyValid = true;
            this.mLastValidationTimeMs = timeMs;
        }
    }

    private void updateValidationTime(long timeMs) {
        this.mCurrentDefaultNetwork.validatedMs += timeMs - this.mLastValidationTimeMs;
    }

    public synchronized void logDefaultNetworkEvent(long timeMs, NetworkAgentInfo newNai, NetworkAgentInfo oldNai) {
        logCurrentDefaultNetwork(timeMs, oldNai);
        newDefaultNetwork(timeMs, newNai);
    }

    private void logCurrentDefaultNetwork(long timeMs, NetworkAgentInfo oldNai) {
        if (this.mIsCurrentlyValid) {
            updateValidationTime(timeMs);
        }
        DefaultNetworkEvent ev = this.mCurrentDefaultNetwork;
        ev.updateDuration(timeMs);
        ev.previousTransports = this.mLastTransports;
        if (oldNai != null) {
            fillLinkInfo(ev, oldNai);
            ev.finalScore = oldNai.getCurrentScore();
        }
        if (ev.transports != 0) {
            this.mLastTransports = ev.transports;
        }
        this.mEvents.add(ev);
        this.mEventsLog.append(ev);
    }

    private void newDefaultNetwork(long timeMs, NetworkAgentInfo newNai) {
        DefaultNetworkEvent ev = new DefaultNetworkEvent(timeMs);
        ev.durationMs = timeMs;
        if (newNai != null) {
            fillLinkInfo(ev, newNai);
            ev.initialScore = newNai.getCurrentScore();
            if (newNai.lastValidated) {
                this.mIsCurrentlyValid = true;
                this.mLastValidationTimeMs = timeMs;
            }
        } else {
            this.mIsCurrentlyValid = false;
        }
        this.mCurrentDefaultNetwork = ev;
    }

    private static void fillLinkInfo(DefaultNetworkEvent ev, NetworkAgentInfo nai) {
        LinkProperties lp = nai.linkProperties;
        ev.netId = nai.network().netId;
        ev.transports = (int) (((long) ev.transports) | BitUtils.packBits(nai.networkCapabilities.getTransportTypes()));
        boolean z = true;
        ev.ipv4 |= lp.hasIpv4Address() && lp.hasIpv4DefaultRoute();
        boolean z2 = ev.ipv6;
        if (!lp.hasGlobalIpv6Address() || !lp.hasIpv6DefaultRoute()) {
            z = false;
        }
        ev.ipv6 = z2 | z;
    }

    private static void printEvent(long localTimeMs, PrintWriter pw, DefaultNetworkEvent ev) {
        long localCreationTimeMs = localTimeMs - ev.durationMs;
        pw.println(String.format("%tT.%tL: %s", Long.valueOf(localCreationTimeMs), Long.valueOf(localCreationTimeMs), ev));
    }
}
