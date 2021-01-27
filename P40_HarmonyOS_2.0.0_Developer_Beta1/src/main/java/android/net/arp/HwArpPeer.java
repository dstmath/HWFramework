package android.net.arp;

import android.net.LinkAddress;
import android.net.LinkProperties;
import android.net.RouteInfo;
import android.os.SystemClock;
import android.util.wifi.HwHiLog;
import java.io.IOException;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.Iterator;

public class HwArpPeer {
    private static final int ARP_LENGTH = 28;
    private static final int ARP_REPLY = 2;
    private static final boolean DBG = false;
    private static final int ETHERNET_TYPE = 1;
    private static final int IPV4_LENGTH = 4;
    private static final int L2_BROADCAST_DEFAULT = 255;
    private static final int MAC_ADDRESS_LEN_PER_FREG = 3;
    private static final int MAC_ADDRESS_NUM_PER_FREG = 2;
    private static final int MAC_ADDR_LENGTH = 6;
    private static final int MAX_LENGTH = 1500;
    private static final int PROTOCOL_IP_INDEX = 8;
    private static final int RADIX_HEX = 16;
    private static final String TAG = "HwArpPeer";
    private String mInterfaceName;
    private final byte[] mL2Broadcast;
    private final InetAddress mMyAddr;
    private final byte[] mMyMac = new byte[6];
    private final InetAddress mPeer;
    private final RawSocket mSocket;

    public HwArpPeer(String interfaceName, InetAddress myAddr, String mac, InetAddress peer) throws SocketException {
        this.mInterfaceName = interfaceName;
        this.mMyAddr = myAddr;
        if (mac != null) {
            for (int i = 0; i < 6; i++) {
                try {
                    this.mMyMac[i] = (byte) Integer.parseInt(mac.substring(i * 3, (i * 3) + 2), 16);
                } catch (NumberFormatException e) {
                    HwHiLog.e(TAG, false, "HwArpPeer initiation failure", new Object[0]);
                }
            }
        }
        if ((myAddr instanceof Inet6Address) || (peer instanceof Inet6Address)) {
            throw new IllegalArgumentException("IPv6 unsupported");
        }
        this.mPeer = peer;
        this.mL2Broadcast = new byte[6];
        Arrays.fill(this.mL2Broadcast, (byte) -1);
        this.mSocket = new RawSocket(this.mInterfaceName, RawSocket.ETH_P_ARP);
    }

    public byte[] doArp(int timeoutMillis) {
        if (this.mMyAddr == null) {
            return null;
        }
        byte[] desiredIp = this.mPeer.getAddress();
        writeBuffer(desiredIp);
        HwHiLog.d(TAG, false, "doArp socket_write", new Object[0]);
        byte[] recvBuf = new byte[MAX_LENGTH];
        long timeout = SystemClock.elapsedRealtime() + ((long) timeoutMillis);
        while (SystemClock.elapsedRealtime() < timeout) {
            if (this.mSocket.read(recvBuf, 0, recvBuf.length, -1, (int) (timeout - SystemClock.elapsedRealtime())) >= 28 && recvBuf[0] == 0 && recvBuf[1] == 1 && recvBuf[2] == 8 && recvBuf[3] == 0 && recvBuf[4] == 6 && recvBuf[5] == 4 && recvBuf[6] == 0 && recvBuf[7] == 2 && recvBuf[14] == desiredIp[0] && recvBuf[15] == desiredIp[1] && recvBuf[16] == desiredIp[2] && recvBuf[17] == desiredIp[3]) {
                byte[] result = new byte[6];
                System.arraycopy(recvBuf, 8, result, 0, 6);
                return result;
            }
        }
        return null;
    }

    /* JADX INFO: Multiple debug info for r4v4 android.net.arp.HwArpPeer: [D('peer' android.net.arp.HwArpPeer), D('route' android.net.RouteInfo)] */
    /* JADX WARNING: Code restructure failed: missing block: B:28:0x0071, code lost:
        if (0 == 0) goto L_0x0074;
     */
    public static boolean doArp(String myMacAddress, LinkProperties linkProperties, int timeoutMillis, int numArpPings, int minArpResponses) {
        boolean isSuccess;
        String interfaceName = linkProperties.getInterfaceName();
        InetAddress inetAddress = null;
        InetAddress gateway = null;
        Iterator<LinkAddress> it = linkProperties.getLinkAddresses().iterator();
        if (it.hasNext()) {
            inetAddress = it.next().getAddress();
        }
        Iterator<RouteInfo> it2 = linkProperties.getRoutes().iterator();
        if (it2.hasNext()) {
            gateway = it2.next().getGateway();
        }
        HwArpPeer peer = null;
        boolean z = false;
        try {
            peer = new HwArpPeer(interfaceName, inetAddress, myMacAddress, gateway);
            int responses = 0;
            for (int i = 0; i < numArpPings; i++) {
                if (peer.doArp(timeoutMillis) != null) {
                    responses++;
                }
            }
            if (responses >= minArpResponses) {
                z = true;
            }
            isSuccess = z;
        } catch (SocketException e) {
            HwHiLog.e(TAG, false, "ARP test initiation failure", new Object[0]);
            isSuccess = true;
        } catch (IllegalArgumentException e2) {
            HwHiLog.e(TAG, false, "ARP failre", new Object[0]);
            isSuccess = true;
            if (0 != 0) {
            }
        } catch (Throwable th) {
            if (0 != 0) {
                peer.close();
            }
            throw th;
        }
        peer.close();
        return isSuccess;
    }

    public HwMultiGw getGateWayArpResponses(int timeoutMillis) {
        if (this.mMyAddr == null) {
            return null;
        }
        byte[] desiredIp = this.mPeer.getAddress();
        writeBuffer(desiredIp);
        byte[] recvBuf = new byte[MAX_LENGTH];
        HwMultiGw resultGw = new HwMultiGw();
        long startTime = SystemClock.elapsedRealtime();
        resultGw.setGwIpAddr(this.mPeer.getHostAddress());
        long timeout = SystemClock.elapsedRealtime() + ((long) timeoutMillis);
        byte[] result = new byte[6];
        while (SystemClock.elapsedRealtime() < timeout) {
            if (isLegalAddress(desiredIp, recvBuf, this.mSocket.read(recvBuf, 0, recvBuf.length, -1, (int) (timeout - SystemClock.elapsedRealtime())))) {
                resultGw.setArpRtt(SystemClock.elapsedRealtime() - startTime);
                System.arraycopy(recvBuf, 8, result, 0, 6);
                resultGw.setGwMacAddr(result);
            }
            result = result;
        }
        if (resultGw.getGwNum() > 0) {
            return resultGw;
        }
        return null;
    }

    private void writeBuffer(byte[] desiredIp) {
        ByteBuffer buf = ByteBuffer.allocate(MAX_LENGTH);
        buf.clear();
        buf.order(ByteOrder.BIG_ENDIAN);
        buf.putShort(1);
        buf.putShort(RawSocket.ETH_P_IP);
        buf.put((byte) 6);
        buf.put((byte) 4);
        buf.putShort(1);
        buf.put(this.mMyMac);
        buf.put(this.mMyAddr.getAddress());
        buf.put(new byte[6]);
        buf.put(desiredIp);
        buf.flip();
        this.mSocket.write(this.mL2Broadcast, buf.array(), 0, buf.limit());
    }

    private boolean isLegalAddress(byte[] desiredIp, byte[] recvBuf, int readLen) {
        return readLen >= 28 && recvBuf[0] == 0 && recvBuf[1] == 1 && recvBuf[2] == 8 && recvBuf[3] == 0 && recvBuf[4] == 6 && recvBuf[5] == 4 && recvBuf[6] == 0 && recvBuf[7] == 2 && recvBuf[14] == desiredIp[0] && recvBuf[15] == desiredIp[1] && recvBuf[16] == desiredIp[2] && recvBuf[17] == desiredIp[3];
    }

    public byte[] doGratuitousArp(int timeoutMillis) {
        ByteBuffer buf = ByteBuffer.allocate(MAX_LENGTH);
        byte[] desiredIp = this.mMyAddr.getAddress();
        buf.clear();
        buf.order(ByteOrder.BIG_ENDIAN);
        buf.putShort(1);
        buf.putShort(RawSocket.ETH_P_IP);
        buf.put((byte) 6);
        buf.put((byte) 4);
        buf.putShort(1);
        buf.put(this.mMyMac);
        buf.put(this.mMyAddr.getAddress());
        buf.put(new byte[6]);
        buf.put(desiredIp);
        buf.flip();
        this.mSocket.write(this.mL2Broadcast, buf.array(), 0, buf.limit());
        byte[] recvBuf = new byte[MAX_LENGTH];
        long timeout = SystemClock.elapsedRealtime() + ((long) timeoutMillis);
        while (SystemClock.elapsedRealtime() < timeout) {
            if (isLegalAddress(desiredIp, recvBuf, this.mSocket.read(recvBuf, 0, recvBuf.length, -1, (int) (timeout - SystemClock.elapsedRealtime())))) {
                byte[] result = new byte[6];
                System.arraycopy(recvBuf, 8, result, 0, 6);
                return result;
            }
        }
        return null;
    }

    public void close() {
        try {
            this.mSocket.close();
        } catch (IOException e) {
            HwHiLog.e(TAG, false, "Exception happens when close socket", new Object[0]);
        }
    }
}
