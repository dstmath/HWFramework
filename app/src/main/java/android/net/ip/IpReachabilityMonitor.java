package android.net.ip;

import android.content.Context;
import android.net.LinkProperties;
import android.net.LinkProperties.ProvisioningChange;
import android.net.RouteInfo;
import android.net.metrics.IpReachabilityEvent;
import android.net.netlink.NetlinkConstants;
import android.net.netlink.NetlinkErrorMessage;
import android.net.netlink.NetlinkMessage;
import android.net.netlink.NetlinkSocket;
import android.net.netlink.RtNetlinkNeighborMessage;
import android.net.netlink.StructNdMsg;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.os.SystemClock;
import android.system.ErrnoException;
import android.system.NetlinkSocketAddress;
import android.system.OsConstants;
import android.util.Log;
import com.android.internal.annotations.GuardedBy;
import java.io.InterruptedIOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class IpReachabilityMonitor {
    private static final boolean DBG = false;
    private static final String TAG = "IpReachabilityMonitor";
    private static final boolean VDBG = false;
    private final Callback mCallback;
    private final int mInterfaceIndex;
    private final String mInterfaceName;
    @GuardedBy("mLock")
    private Map<InetAddress, Short> mIpWatchList;
    @GuardedBy("mLock")
    private int mIpWatchListVersion;
    @GuardedBy("mLock")
    private LinkProperties mLinkProperties;
    private final Object mLock;
    private final NetlinkSocketObserver mNetlinkSocketObserver;
    private final Thread mObserverThread;
    @GuardedBy("mLock")
    private boolean mRunning;
    private final WakeLock mWakeLock;

    public interface Callback {
        void notifyLost(InetAddress inetAddress, String str);
    }

    private final class NetlinkSocketObserver implements Runnable {
        private NetlinkSocket mSocket;

        private NetlinkSocketObserver() {
        }

        public void run() {
            synchronized (IpReachabilityMonitor.this.mLock) {
                IpReachabilityMonitor.this.mRunning = true;
            }
            try {
                setupNetlinkSocket();
            } catch (Exception e) {
                Log.e(IpReachabilityMonitor.TAG, "Failed to suitably initialize a netlink socket", e);
                synchronized (IpReachabilityMonitor.this.mLock) {
                }
                IpReachabilityMonitor.this.mRunning = IpReachabilityMonitor.DBG;
            }
            while (IpReachabilityMonitor.this.stillRunning()) {
                try {
                    ByteBuffer byteBuffer = recvKernelReply();
                    long whenMs = SystemClock.elapsedRealtime();
                    if (byteBuffer != null) {
                        parseNetlinkMessageBuffer(byteBuffer, whenMs);
                    }
                } catch (ErrnoException e2) {
                    if (IpReachabilityMonitor.this.stillRunning()) {
                        Log.w(IpReachabilityMonitor.TAG, "ErrnoException: ", e2);
                    }
                }
            }
            clearNetlinkSocket();
            synchronized (IpReachabilityMonitor.this.mLock) {
                IpReachabilityMonitor.this.mRunning = IpReachabilityMonitor.DBG;
            }
        }

        private void clearNetlinkSocket() {
            if (this.mSocket != null) {
                this.mSocket.close();
            }
        }

        private void setupNetlinkSocket() throws ErrnoException, SocketException {
            clearNetlinkSocket();
            this.mSocket = new NetlinkSocket(OsConstants.NETLINK_ROUTE);
            this.mSocket.bind(new NetlinkSocketAddress(0, OsConstants.RTMGRP_NEIGH));
        }

        private ByteBuffer recvKernelReply() throws ErrnoException {
            try {
                return this.mSocket.recvMessage(0);
            } catch (InterruptedIOException e) {
                return null;
            } catch (ErrnoException e2) {
                if (e2.errno != OsConstants.EAGAIN) {
                    throw e2;
                }
                return null;
            }
        }

        private void parseNetlinkMessageBuffer(ByteBuffer byteBuffer, long whenMs) {
            while (byteBuffer.remaining() > 0) {
                int position = byteBuffer.position();
                NetlinkMessage nlMsg = NetlinkMessage.parse(byteBuffer);
                if (nlMsg == null || nlMsg.getHeader() == null) {
                    byteBuffer.position(position);
                    Log.e(IpReachabilityMonitor.TAG, "unparsable netlink msg: " + NetlinkConstants.hexify(byteBuffer));
                    return;
                }
                int srcPortId = nlMsg.getHeader().nlmsg_pid;
                if (srcPortId != 0) {
                    Log.e(IpReachabilityMonitor.TAG, "non-kernel source portId: " + ((long) (srcPortId & -1)));
                    return;
                } else if (nlMsg instanceof NetlinkErrorMessage) {
                    Log.e(IpReachabilityMonitor.TAG, "netlink error: " + nlMsg);
                } else if (nlMsg instanceof RtNetlinkNeighborMessage) {
                    evaluateRtNetlinkNeighborMessage((RtNetlinkNeighborMessage) nlMsg, whenMs);
                }
            }
        }

        private void evaluateRtNetlinkNeighborMessage(RtNetlinkNeighborMessage neighMsg, long whenMs) {
            StructNdMsg ndMsg = neighMsg.getNdHeader();
            if (ndMsg != null && ndMsg.ndm_ifindex == IpReachabilityMonitor.this.mInterfaceIndex) {
                InetAddress destination = neighMsg.getDestination();
                if (IpReachabilityMonitor.this.isWatching(destination)) {
                    short msgType = neighMsg.getHeader().nlmsg_type;
                    short nudState = ndMsg.ndm_state;
                    String eventMsg = "NeighborEvent{elapsedMs=" + whenMs + ", " + destination.getHostAddress() + ", " + "[" + NetlinkConstants.hexify(neighMsg.getLinkLayerAddress()) + "], " + NetlinkConstants.stringForNlMsgType(msgType) + ", " + StructNdMsg.stringForNudState(nudState) + "}";
                    synchronized (IpReachabilityMonitor.this.mLock) {
                        if (IpReachabilityMonitor.this.mIpWatchList.containsKey(destination)) {
                            short s;
                            if (msgType == (short) 29) {
                                s = (short) 0;
                            } else {
                                s = nudState;
                            }
                            IpReachabilityMonitor.this.mIpWatchList.put(destination, Short.valueOf(s));
                        }
                    }
                    if (nudState == (short) 32) {
                        Log.w(IpReachabilityMonitor.TAG, "ALERT: " + eventMsg);
                        IpReachabilityMonitor.this.handleNeighborLost(eventMsg);
                    }
                }
            }
        }
    }

    private static int probeNeighbor(int ifIndex, InetAddress ip) {
        Throwable th;
        Throwable th2;
        String msgSnippet = "probing ip=" + ip.getHostAddress() + "%" + ifIndex;
        byte[] msg = RtNetlinkNeighborMessage.newNewNeighborMessage(1, ip, (short) 16, ifIndex, null);
        int errno = -OsConstants.EPROTO;
        Throwable th3 = null;
        NetlinkSocket nlSocket;
        try {
            nlSocket = new NetlinkSocket(OsConstants.NETLINK_ROUTE);
            try {
                String errmsg;
                nlSocket.connectToKernel();
                nlSocket.sendMessage(msg, 0, msg.length, 300);
                ByteBuffer bytes = nlSocket.recvMessage(300);
                NetlinkMessage response = NetlinkMessage.parse(bytes);
                if (response != null && (response instanceof NetlinkErrorMessage)) {
                    if (((NetlinkErrorMessage) response).getNlMsgError() != null) {
                        errno = ((NetlinkErrorMessage) response).getNlMsgError().error;
                        if (errno != 0) {
                            Log.e(TAG, "Error " + msgSnippet + ", errmsg=" + response.toString());
                        }
                        if (nlSocket != null) {
                            try {
                                nlSocket.close();
                            } catch (Throwable th4) {
                                th3 = th4;
                            }
                        }
                        if (th3 != null) {
                            return errno;
                        }
                        try {
                            throw th3;
                        } catch (ErrnoException e) {
                            Log.e(TAG, "Error " + msgSnippet, e);
                            return -e.errno;
                        } catch (InterruptedIOException e2) {
                            Log.e(TAG, "Error " + msgSnippet, e2);
                            return -OsConstants.ETIMEDOUT;
                        } catch (SocketException e3) {
                            Log.e(TAG, "Error " + msgSnippet, e3);
                            return -OsConstants.EIO;
                        }
                    }
                }
                if (response == null) {
                    bytes.position(0);
                    errmsg = "raw bytes: " + NetlinkConstants.hexify(bytes);
                } else {
                    errmsg = response.toString();
                }
                Log.e(TAG, "Error " + msgSnippet + ", errmsg=" + errmsg);
                if (nlSocket != null) {
                    nlSocket.close();
                }
                if (th3 != null) {
                    return errno;
                }
                throw th3;
            } catch (Throwable th5) {
                th = th5;
                th2 = null;
                if (nlSocket != null) {
                    try {
                        nlSocket.close();
                    } catch (Throwable th6) {
                        if (th2 == null) {
                            th2 = th6;
                        } else if (th2 != th6) {
                            th2.addSuppressed(th6);
                        }
                    }
                }
                if (th2 == null) {
                    throw th2;
                } else {
                    throw th;
                }
            }
        } catch (Throwable th7) {
            th = th7;
            th2 = null;
            nlSocket = null;
            if (nlSocket != null) {
                nlSocket.close();
            }
            if (th2 == null) {
                throw th;
            } else {
                throw th2;
            }
        }
    }

    public IpReachabilityMonitor(Context context, String ifName, Callback callback) throws IllegalArgumentException {
        this.mLock = new Object();
        this.mLinkProperties = new LinkProperties();
        this.mIpWatchList = new HashMap();
        this.mInterfaceName = ifName;
        try {
            this.mInterfaceIndex = NetworkInterface.getByName(ifName).getIndex();
            this.mWakeLock = ((PowerManager) context.getSystemService("power")).newWakeLock(1, "IpReachabilityMonitor." + this.mInterfaceName);
            this.mCallback = callback;
            this.mNetlinkSocketObserver = new NetlinkSocketObserver();
            this.mObserverThread = new Thread(this.mNetlinkSocketObserver);
            this.mObserverThread.start();
        } catch (Exception e) {
            throw new IllegalArgumentException("invalid interface '" + ifName + "': ", e);
        }
    }

    public void stop() {
        synchronized (this.mLock) {
            this.mRunning = DBG;
        }
        clearLinkProperties();
        this.mNetlinkSocketObserver.clearNetlinkSocket();
    }

    private String describeWatchList() {
        String delimiter = ", ";
        StringBuilder sb = new StringBuilder();
        synchronized (this.mLock) {
            sb.append("iface{").append(this.mInterfaceName).append("/").append(this.mInterfaceIndex).append("}, ");
            sb.append("v{").append(this.mIpWatchListVersion).append("}, ");
            sb.append("ntable=[");
            boolean firstTime = true;
            for (Entry<InetAddress, Short> entry : this.mIpWatchList.entrySet()) {
                if (firstTime) {
                    firstTime = DBG;
                } else {
                    sb.append(", ");
                }
                sb.append(((InetAddress) entry.getKey()).getHostAddress()).append("/").append(StructNdMsg.stringForNudState(((Short) entry.getValue()).shortValue()));
            }
            sb.append("]");
        }
        return sb.toString();
    }

    private boolean isWatching(InetAddress ip) {
        boolean containsKey;
        synchronized (this.mLock) {
            containsKey = this.mRunning ? this.mIpWatchList.containsKey(ip) : DBG;
        }
        return containsKey;
    }

    private boolean stillRunning() {
        boolean z;
        synchronized (this.mLock) {
            z = this.mRunning;
        }
        return z;
    }

    private static boolean isOnLink(List<RouteInfo> routes, InetAddress ip) {
        for (RouteInfo route : routes) {
            if (!route.hasGateway() && route.matches(ip)) {
                return true;
            }
        }
        return DBG;
    }

    private short getNeighborStateLocked(InetAddress ip) {
        if (this.mIpWatchList.containsKey(ip)) {
            return ((Short) this.mIpWatchList.get(ip)).shortValue();
        }
        return (short) 0;
    }

    public void updateLinkProperties(LinkProperties lp) {
        if (this.mInterfaceName.equals(lp.getInterfaceName())) {
            synchronized (this.mLock) {
                this.mLinkProperties = new LinkProperties(lp);
                Map<InetAddress, Short> newIpWatchList = new HashMap();
                List<RouteInfo> routes = this.mLinkProperties.getRoutes();
                for (RouteInfo route : routes) {
                    if (route.hasGateway()) {
                        InetAddress gw = route.getGateway();
                        if (isOnLink(routes, gw)) {
                            newIpWatchList.put(gw, Short.valueOf(getNeighborStateLocked(gw)));
                        }
                    }
                }
                for (InetAddress nameserver : lp.getDnsServers()) {
                    if (isOnLink(routes, nameserver)) {
                        newIpWatchList.put(nameserver, Short.valueOf(getNeighborStateLocked(nameserver)));
                    }
                }
                this.mIpWatchList = newIpWatchList;
                this.mIpWatchListVersion++;
            }
            return;
        }
        Log.wtf(TAG, "requested LinkProperties interface '" + lp.getInterfaceName() + "' does not match: " + this.mInterfaceName);
    }

    public void clearLinkProperties() {
        synchronized (this.mLock) {
            this.mLinkProperties.clear();
            this.mIpWatchList.clear();
            this.mIpWatchListVersion++;
        }
    }

    private void handleNeighborLost(String msg) {
        InetAddress ip = null;
        synchronized (this.mLock) {
            LinkProperties whatIfLp = new LinkProperties(this.mLinkProperties);
            for (Entry<InetAddress, Short> entry : this.mIpWatchList.entrySet()) {
                if (((Short) entry.getValue()).shortValue() == (short) 32) {
                    ip = (InetAddress) entry.getKey();
                    for (RouteInfo route : this.mLinkProperties.getRoutes()) {
                        if (ip.equals(route.getGateway())) {
                            whatIfLp.removeRoute(route);
                        }
                    }
                    whatIfLp.removeDnsServer(ip);
                }
            }
            ProvisioningChange delta = LinkProperties.compareProvisioning(this.mLinkProperties, whatIfLp);
        }
        if (delta == ProvisioningChange.LOST_PROVISIONING) {
            IpReachabilityEvent.logProvisioningLost(this.mInterfaceName);
            String logMsg = "FAILURE: LOST_PROVISIONING, " + msg;
            Log.w(TAG, logMsg);
            if (this.mCallback != null) {
                this.mCallback.notifyLost(ip, logMsg);
                return;
            }
            return;
        }
        IpReachabilityEvent.logNudFailed(this.mInterfaceName);
    }

    public void probeAll() {
        Set<InetAddress> ipProbeList = new HashSet();
        synchronized (this.mLock) {
            ipProbeList.addAll(this.mIpWatchList.keySet());
        }
        if (!ipProbeList.isEmpty() && stillRunning()) {
            this.mWakeLock.acquire(getProbeWakeLockDuration());
        }
        for (InetAddress target : ipProbeList) {
            if (stillRunning()) {
                IpReachabilityEvent.logProbeEvent(this.mInterfaceName, probeNeighbor(this.mInterfaceIndex, target));
            } else {
                return;
            }
        }
    }

    private long getProbeWakeLockDuration() {
        return 3500;
    }
}
