package android.net.ip;

import android.net.NetworkUtils;
import android.net.util.BlockingSocketReader;
import android.net.util.ConnectivityPacketSummary;
import android.os.Handler;
import android.system.Os;
import android.system.OsConstants;
import android.system.PacketSocketAddress;
import android.util.LocalLog;
import android.util.Log;
import java.io.FileDescriptor;
import java.net.NetworkInterface;
import libcore.util.HexEncoding;

public class ConnectivityPacketTracker {
    private static final boolean DBG = false;
    private static final String MARK_START = "--- START ---";
    private static final String MARK_STOP = "--- STOP ---";
    private static final String TAG = ConnectivityPacketTracker.class.getSimpleName();
    private final Handler mHandler;
    private final LocalLog mLog;
    private final BlockingSocketReader mPacketListener;
    private final String mTag;

    private final class PacketListener extends BlockingSocketReader {
        private final byte[] mHwAddr;
        private final int mIfIndex;

        PacketListener(int ifindex, byte[] hwaddr, int mtu) {
            super(mtu);
            this.mIfIndex = ifindex;
            this.mHwAddr = hwaddr;
        }

        /* JADX WARNING: Removed duplicated region for block: B:4:0x001d A:{Splitter: B:1:0x0001, ExcHandler: android.system.ErrnoException (r0_0 'e' java.lang.Exception), PHI: r1 } */
        /* JADX WARNING: Missing block: B:4:0x001d, code:
            r0 = move-exception;
     */
        /* JADX WARNING: Missing block: B:5:0x001e, code:
            logError("Failed to create packet tracking socket: ", r0);
            android.net.util.BlockingSocketReader.closeSocket(r1);
     */
        /* JADX WARNING: Missing block: B:6:0x0028, code:
            return null;
     */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        protected FileDescriptor createSocket() {
            FileDescriptor s = null;
            try {
                s = Os.socket(OsConstants.AF_PACKET, OsConstants.SOCK_RAW, 0);
                NetworkUtils.attachControlPacketFilter(s, OsConstants.ARPHRD_ETHER);
                Os.bind(s, new PacketSocketAddress((short) OsConstants.ETH_P_ALL, this.mIfIndex));
                return s;
            } catch (Exception e) {
            }
        }

        protected void handlePacket(byte[] recvbuf, int length) {
            String summary = ConnectivityPacketSummary.summarize(this.mHwAddr, recvbuf, length);
            if (summary != null) {
                addLogEntry(summary + "\n[" + new String(HexEncoding.encode(recvbuf, 0, length)) + "]");
            }
        }

        protected void logError(String msg, Exception e) {
            Log.e(ConnectivityPacketTracker.this.mTag, msg, e);
            addLogEntry(msg + e);
        }

        private void addLogEntry(String entry) {
            ConnectivityPacketTracker.this.mHandler.post(new -$Lambda$yDnD85pUPxzgrjWolXWWPPki110(this, entry));
        }

        /* synthetic */ void lambda$-android_net_ip_ConnectivityPacketTracker$PacketListener_4824(String entry) {
            ConnectivityPacketTracker.this.mLog.log(entry);
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:5:0x0040 A:{Splitter: B:1:0x0003, ExcHandler: java.lang.NullPointerException (r0_0 'e' java.lang.Exception)} */
    /* JADX WARNING: Missing block: B:5:0x0040, code:
            r0 = move-exception;
     */
    /* JADX WARNING: Missing block: B:7:0x0049, code:
            throw new java.lang.IllegalArgumentException("bad network interface", r0);
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public ConnectivityPacketTracker(NetworkInterface netif, LocalLog log) {
        try {
            String ifname = netif.getName();
            int ifindex = netif.getIndex();
            byte[] hwaddr = netif.getHardwareAddress();
            int mtu = netif.getMTU();
            this.mTag = TAG + "." + ifname;
            this.mHandler = new Handler();
            this.mLog = log;
            this.mPacketListener = new PacketListener(ifindex, hwaddr, mtu);
        } catch (Exception e) {
        }
    }

    public void start() {
        this.mLog.log(MARK_START);
        this.mPacketListener.start();
    }

    public void stop() {
        this.mPacketListener.stop();
        this.mLog.log(MARK_STOP);
    }
}
