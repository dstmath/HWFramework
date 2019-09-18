package android.net.arp;

import android.net.LinkAddress;
import android.net.LinkProperties;
import android.net.RouteInfo;
import android.os.SystemClock;
import android.util.Log;
import java.io.IOException;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.Iterator;
import libcore.net.RawSocket;

public class HWArpPeer {
    private static final int ARP_LENGTH = 28;
    private static final boolean DBG = false;
    private static final int ETHERNET_TYPE = 1;
    private static final int IPV4_LENGTH = 4;
    private static final int MAC_ADDR_LENGTH = 6;
    private static final int MAX_LENGTH = 1500;
    private static final String TAG = "HWArpPeer";
    private final byte[] L2_BROADCAST;
    private String mInterfaceName;
    private final InetAddress mMyAddr;
    private final byte[] mMyMac = new byte[6];
    private final InetAddress mPeer;
    private final RawSocket mSocket;

    public HWArpPeer(String interfaceName, InetAddress myAddr, String mac, InetAddress peer) throws SocketException {
        this.mInterfaceName = interfaceName;
        this.mMyAddr = myAddr;
        if (mac != null) {
            for (int i = 0; i < 6; i++) {
                this.mMyMac[i] = (byte) Integer.parseInt(mac.substring(i * 3, (i * 3) + 2), 16);
            }
        }
        if ((myAddr instanceof Inet6Address) != 0 || (peer instanceof Inet6Address)) {
            throw new IllegalArgumentException("IPv6 unsupported");
        }
        this.mPeer = peer;
        this.L2_BROADCAST = new byte[6];
        Arrays.fill(this.L2_BROADCAST, (byte) -1);
        this.mSocket = new RawSocket(this.mInterfaceName, 2054);
    }

    public byte[] doArp(int timeoutMillis) {
        char c;
        byte b;
        if (this.mMyAddr == null) {
            return null;
        }
        ByteBuffer buf = ByteBuffer.allocate(MAX_LENGTH);
        byte[] desiredIp = this.mPeer.getAddress();
        long timeout = SystemClock.elapsedRealtime() + ((long) timeoutMillis);
        buf.clear();
        buf.order(ByteOrder.BIG_ENDIAN);
        byte b2 = 1;
        buf.putShort(1);
        buf.putShort(2048);
        buf.put((byte) 6);
        byte b3 = 4;
        buf.put((byte) 4);
        buf.putShort(1);
        buf.put(this.mMyMac);
        buf.put(this.mMyAddr.getAddress());
        buf.put(new byte[6]);
        buf.put(desiredIp);
        buf.flip();
        char c2 = 0;
        this.mSocket.write(this.L2_BROADCAST, buf.array(), 0, buf.limit());
        byte[] recvBuf = new byte[MAX_LENGTH];
        while (SystemClock.elapsedRealtime() < timeout) {
            if (this.mSocket.read(recvBuf, 0, recvBuf.length, -1, (int) (timeout - SystemClock.elapsedRealtime())) < 28 || recvBuf[c2] != 0 || recvBuf[b2] != b2) {
                b = b2;
                c = c2;
            } else if (recvBuf[2] == 8 && recvBuf[3] == 0 && recvBuf[b3] == 6 && recvBuf[5] == b3 && recvBuf[6] == 0 && recvBuf[7] == 2 && recvBuf[14] == desiredIp[c2]) {
                b = 1;
                if (recvBuf[15] == desiredIp[1] && recvBuf[16] == desiredIp[2] && recvBuf[17] == desiredIp[3]) {
                    byte[] result = new byte[6];
                    System.arraycopy(recvBuf, 8, result, 0, 6);
                    return result;
                }
                c = 0;
            } else {
                c = c2;
                b = 1;
            }
            b2 = b;
            c2 = c;
            b3 = 4;
        }
        return null;
    }

    public HWMultiGW getGateWayARPResponses(int timeoutMillis) {
        boolean z;
        char c;
        HWArpPeer hWArpPeer = this;
        if (hWArpPeer.mMyAddr == null) {
            return null;
        }
        ByteBuffer buf = ByteBuffer.allocate(MAX_LENGTH);
        byte[] desiredIp = hWArpPeer.mPeer.getAddress();
        long timeout = SystemClock.elapsedRealtime() + ((long) timeoutMillis);
        buf.clear();
        buf.order(ByteOrder.BIG_ENDIAN);
        buf.putShort(1);
        buf.putShort(2048);
        buf.put((byte) 6);
        buf.put((byte) 4);
        buf.putShort(1);
        buf.put(hWArpPeer.mMyMac);
        buf.put(hWArpPeer.mMyAddr.getAddress());
        buf.put(new byte[6]);
        buf.put(desiredIp);
        buf.flip();
        hWArpPeer.mSocket.write(hWArpPeer.L2_BROADCAST, buf.array(), 0, buf.limit());
        byte[] recvBuf = new byte[MAX_LENGTH];
        HWMultiGW resultGW = new HWMultiGW();
        byte[] result = new byte[6];
        long lStart = SystemClock.elapsedRealtime();
        resultGW.setGWIPAddr(hWArpPeer.mPeer.getHostAddress());
        while (SystemClock.elapsedRealtime() < timeout) {
            long duration = timeout - SystemClock.elapsedRealtime();
            if (hWArpPeer.mSocket.read(recvBuf, 0, recvBuf.length, -1, (int) duration) >= 28 && recvBuf[0] == 0 && recvBuf[1] == 1 && recvBuf[2] == 8 && recvBuf[3] == 0) {
                if (recvBuf[4] != 6) {
                    c = 6;
                    z = false;
                    char c2 = c;
                    boolean z2 = z;
                    hWArpPeer = this;
                } else if (recvBuf[5] == 4 && recvBuf[6] == 0 && recvBuf[7] == 2 && recvBuf[14] == desiredIp[0] && recvBuf[15] == desiredIp[1] && recvBuf[16] == desiredIp[2] && recvBuf[17] == desiredIp[3]) {
                    long lEnd = SystemClock.elapsedRealtime();
                    long j = duration;
                    resultGW.setArpRTT(lEnd - lStart);
                    c = 6;
                    z = false;
                    System.arraycopy(recvBuf, 8, result, 0, 6);
                    resultGW.setGWMACAddr(result);
                    long j2 = lEnd;
                    char c22 = c;
                    boolean z22 = z;
                    hWArpPeer = this;
                }
            }
            c = 6;
            z = false;
            char c222 = c;
            boolean z222 = z;
            hWArpPeer = this;
        }
        if (resultGW.getGWNum() > 0) {
            return resultGW;
        }
        return null;
    }

    public byte[] doGratuitousArp(int timeoutMillis) {
        char c;
        byte b;
        ByteBuffer buf = ByteBuffer.allocate(MAX_LENGTH);
        byte[] desiredIp = this.mMyAddr.getAddress();
        long timeout = SystemClock.elapsedRealtime() + ((long) timeoutMillis);
        buf.clear();
        buf.order(ByteOrder.BIG_ENDIAN);
        byte b2 = 1;
        buf.putShort(1);
        buf.putShort(2048);
        buf.put((byte) 6);
        byte b3 = 4;
        buf.put((byte) 4);
        buf.putShort(1);
        buf.put(this.mMyMac);
        buf.put(this.mMyAddr.getAddress());
        buf.put(new byte[6]);
        buf.put(desiredIp);
        buf.flip();
        char c2 = 0;
        this.mSocket.write(this.L2_BROADCAST, buf.array(), 0, buf.limit());
        byte[] recvBuf = new byte[MAX_LENGTH];
        while (SystemClock.elapsedRealtime() < timeout) {
            if (this.mSocket.read(recvBuf, 0, recvBuf.length, -1, (int) (timeout - SystemClock.elapsedRealtime())) < 28 || recvBuf[c2] != 0 || recvBuf[b2] != b2) {
                b = b2;
                c = c2;
            } else if (recvBuf[2] == 8 && recvBuf[3] == 0 && recvBuf[b3] == 6 && recvBuf[5] == b3 && recvBuf[6] == 0 && recvBuf[7] == 2 && recvBuf[14] == desiredIp[c2]) {
                b = 1;
                if (recvBuf[15] == desiredIp[1] && recvBuf[16] == desiredIp[2] && recvBuf[17] == desiredIp[3]) {
                    byte[] result = new byte[6];
                    System.arraycopy(recvBuf, 8, result, 0, 6);
                    return result;
                }
                c = 0;
            } else {
                c = c2;
                b = 1;
            }
            b2 = b;
            c2 = c;
            b3 = 4;
        }
        return null;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:28:0x008f, code lost:
        if (r3 == null) goto L_0x0093;
     */
    public static boolean doArp(String myMacAddress, LinkProperties linkProperties, int timeoutMillis, int numArpPings, int minArpResponses) {
        boolean success;
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
        HWArpPeer peer = null;
        try {
            peer = new HWArpPeer(interfaceName, inetAddress, myMacAddress, gateway);
            boolean z = false;
            int responses = 0;
            for (int i = 0; i < numArpPings; i++) {
                if (peer.doArp(timeoutMillis) != null) {
                    responses++;
                }
            }
            if (responses >= minArpResponses) {
                z = true;
            }
            success = z;
        } catch (SocketException se) {
            Log.e(TAG, "ARP test initiation failure: " + se);
            success = true;
        } catch (Exception ae) {
            Log.e(TAG, "ARP failre: " + ae);
            success = true;
            if (peer != null) {
            }
        } catch (Throwable th) {
            if (peer != null) {
                peer.close();
            }
            throw th;
        }
        peer.close();
        return success;
    }

    public void close() {
        try {
            this.mSocket.close();
        } catch (IOException e) {
        }
    }
}
