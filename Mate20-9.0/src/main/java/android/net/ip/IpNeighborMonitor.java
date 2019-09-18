package android.net.ip;

import android.net.MacAddress;
import android.net.netlink.NetlinkConstants;
import android.net.netlink.NetlinkErrorMessage;
import android.net.netlink.NetlinkMessage;
import android.net.netlink.NetlinkSocket;
import android.net.netlink.RtNetlinkNeighborMessage;
import android.net.netlink.StructNdMsg;
import android.net.util.PacketReader;
import android.net.util.SharedLog;
import android.os.Handler;
import android.os.SystemClock;
import android.system.ErrnoException;
import android.system.NetlinkSocketAddress;
import android.system.Os;
import android.system.OsConstants;
import android.util.Log;
import com.android.internal.util.BitUtils;
import java.io.FileDescriptor;
import java.net.InetAddress;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.StringJoiner;
import libcore.io.IoUtils;

public class IpNeighborMonitor extends PacketReader {
    private static final boolean DBG = false;
    private static final String TAG = IpNeighborMonitor.class.getSimpleName();
    private static final boolean VDBG = false;
    private final NeighborEventConsumer mConsumer;
    private final SharedLog mLog;

    public static class NeighborEvent {
        final long elapsedMs;
        final int ifindex;
        final InetAddress ip;
        final MacAddress macAddr;
        final short msgType;
        final short nudState;

        public NeighborEvent(long elapsedMs2, short msgType2, int ifindex2, InetAddress ip2, short nudState2, MacAddress macAddr2) {
            this.elapsedMs = elapsedMs2;
            this.msgType = msgType2;
            this.ifindex = ifindex2;
            this.ip = ip2;
            this.nudState = nudState2;
            this.macAddr = macAddr2;
        }

        /* access modifiers changed from: package-private */
        public boolean isConnected() {
            return this.msgType != 29 && StructNdMsg.isNudStateConnected(this.nudState);
        }

        /* access modifiers changed from: package-private */
        public boolean isValid() {
            return this.msgType != 29 && StructNdMsg.isNudStateValid(this.nudState);
        }

        public String toString() {
            StringJoiner j = new StringJoiner(",", "NeighborEvent{", "}");
            StringJoiner add = j.add("@" + this.elapsedMs).add(NetlinkConstants.stringForNlMsgType(this.msgType));
            StringJoiner add2 = add.add("if=" + this.ifindex).add(this.ip.getHostAddress()).add(StructNdMsg.stringForNudState(this.nudState));
            return add2.add("[" + this.macAddr + "]").toString();
        }
    }

    public interface NeighborEventConsumer {
        void accept(NeighborEvent neighborEvent);
    }

    public static int startKernelNeighborProbe(int ifIndex, InetAddress ip) {
        String msgSnippet = "probing ip=" + ip.getHostAddress() + "%" + ifIndex;
        try {
            NetlinkSocket.sendOneShotKernelMessage(OsConstants.NETLINK_ROUTE, RtNetlinkNeighborMessage.newNewNeighborMessage(1, ip, 16, ifIndex, null));
            return 0;
        } catch (ErrnoException e) {
            Log.e(TAG, "Error " + msgSnippet + ": " + e);
            return -e.errno;
        }
    }

    public IpNeighborMonitor(Handler h, SharedLog log, NeighborEventConsumer cb) {
        super(h, 8192);
        this.mLog = log.forSubComponent(TAG);
        this.mConsumer = cb != null ? cb : $$Lambda$IpNeighborMonitor$4TdKAwtCtq9Ri1cSdW1mKm0JycM.INSTANCE;
    }

    static /* synthetic */ void lambda$new$0(NeighborEvent event) {
    }

    /* access modifiers changed from: protected */
    public FileDescriptor createFd() {
        FileDescriptor fd = null;
        try {
            fd = NetlinkSocket.forProto(OsConstants.NETLINK_ROUTE);
            Os.bind(fd, new NetlinkSocketAddress(0, OsConstants.RTMGRP_NEIGH));
            Os.connect(fd, new NetlinkSocketAddress(0, 0));
            return fd;
        } catch (ErrnoException | SocketException e) {
            logError("Failed to create rtnetlink socket", e);
            IoUtils.closeQuietly(fd);
            return null;
        }
    }

    /* access modifiers changed from: protected */
    public void handlePacket(byte[] recvbuf, int length) {
        long whenMs = SystemClock.elapsedRealtime();
        ByteBuffer byteBuffer = ByteBuffer.wrap(recvbuf, 0, length);
        byteBuffer.order(ByteOrder.nativeOrder());
        parseNetlinkMessageBuffer(byteBuffer, whenMs);
    }

    private void parseNetlinkMessageBuffer(ByteBuffer byteBuffer, long whenMs) {
        while (byteBuffer.remaining() > 0) {
            int position = byteBuffer.position();
            NetlinkMessage nlMsg = NetlinkMessage.parse(byteBuffer);
            if (nlMsg == null || nlMsg.getHeader() == null) {
                byteBuffer.position(position);
                SharedLog sharedLog = this.mLog;
                sharedLog.e("unparsable netlink msg: " + NetlinkConstants.hexify(byteBuffer));
                return;
            }
            int srcPortId = nlMsg.getHeader().nlmsg_pid;
            if (srcPortId != 0) {
                SharedLog sharedLog2 = this.mLog;
                sharedLog2.e("non-kernel source portId: " + BitUtils.uint32(srcPortId));
                return;
            } else if (nlMsg instanceof NetlinkErrorMessage) {
                SharedLog sharedLog3 = this.mLog;
                sharedLog3.e("netlink error: " + nlMsg);
            } else if (!(nlMsg instanceof RtNetlinkNeighborMessage)) {
                SharedLog sharedLog4 = this.mLog;
                sharedLog4.i("non-rtnetlink neighbor msg: " + nlMsg);
            } else {
                evaluateRtNetlinkNeighborMessage((RtNetlinkNeighborMessage) nlMsg, whenMs);
            }
        }
    }

    private void evaluateRtNetlinkNeighborMessage(RtNetlinkNeighborMessage neighMsg, long whenMs) {
        short nudState;
        short msgType = neighMsg.getHeader().nlmsg_type;
        StructNdMsg ndMsg = neighMsg.getNdHeader();
        if (ndMsg == null) {
            this.mLog.e("RtNetlinkNeighborMessage without ND message header!");
            return;
        }
        int ifindex = ndMsg.ndm_ifindex;
        InetAddress destination = neighMsg.getDestination();
        if (msgType == 29) {
            nudState = 0;
        } else {
            nudState = ndMsg.ndm_state;
        }
        NeighborEvent event = new NeighborEvent(whenMs, msgType, ifindex, destination, nudState, getMacAddress(neighMsg.getLinkLayerAddress()));
        this.mConsumer.accept(event);
    }

    private static MacAddress getMacAddress(byte[] linkLayerAddress) {
        if (linkLayerAddress != null) {
            try {
                return MacAddress.fromBytes(linkLayerAddress);
            } catch (IllegalArgumentException e) {
                String str = TAG;
                Log.e(str, "Failed to parse link-layer address: " + NetlinkConstants.hexify(linkLayerAddress));
            }
        }
        return null;
    }
}
