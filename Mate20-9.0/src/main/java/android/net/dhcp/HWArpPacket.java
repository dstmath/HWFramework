package android.net.dhcp;

import android.net.netlink.StructNlMsgHdr;
import android.os.SystemClock;
import android.util.Log;
import java.io.IOException;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;
import libcore.net.RawSocket;

public class HWArpPacket {
    private static final int ARP_LENGTH = 28;
    private static final int ETHERNET_TYPE = 1;
    private static final int IPV4_LENGTH = 4;
    private static final int MAC_ADDR_LENGTH = 6;
    private static final int MAX_LENGTH = 1500;
    private static final String TAG = "HWArpPacket";
    private final byte[] L2_BROADCAST;
    private String mInterfaceName;
    private InetAddress mMyAddr = null;
    private final byte[] mMyMac = new byte[6];
    private final RawSocket mSocket;

    public HWArpPacket(String interfaceName, InetAddress myAddr, String mac) throws SocketException {
        this.mInterfaceName = interfaceName;
        if (mac != null) {
            for (int i = 0; i < 6; i++) {
                this.mMyMac[i] = (byte) Integer.parseInt(mac.substring(i * 3, (i * 3) + 2), 16);
            }
        }
        if ((myAddr instanceof Inet6Address) == 0) {
            this.L2_BROADCAST = new byte[6];
            Arrays.fill(this.L2_BROADCAST, (byte) -1);
            this.mSocket = new RawSocket(this.mInterfaceName, 2054);
            this.mMyAddr = myAddr;
            return;
        }
        throw new IllegalArgumentException("IPv6 unsupported");
    }

    public boolean doArp(int timeoutMillis, Inet4Address requestedAddress, boolean fillSenderIp) {
        ByteBuffer buf = ByteBuffer.allocate(1500);
        byte[] desiredIp = requestedAddress.getAddress();
        long timeout = SystemClock.elapsedRealtime() + ((long) timeoutMillis);
        buf.clear();
        buf.order(ByteOrder.BIG_ENDIAN);
        byte b = 1;
        buf.putShort(1);
        buf.putShort(StructNlMsgHdr.NLM_F_APPEND);
        buf.put((byte) 6);
        byte b2 = 4;
        buf.put((byte) 4);
        buf.putShort(1);
        buf.put(this.mMyMac);
        buf.put((!fillSenderIp || this.mMyAddr == null) ? new byte[4] : this.mMyAddr.getAddress());
        buf.put(new byte[6]);
        buf.put(desiredIp);
        buf.flip();
        this.mSocket.write(this.L2_BROADCAST, buf.array(), 0, buf.limit());
        byte[] recvBuf = new byte[1500];
        while (SystemClock.elapsedRealtime() < timeout) {
            if (this.mSocket.read(recvBuf, 0, recvBuf.length, -1, (int) (timeout - SystemClock.elapsedRealtime())) >= 28 && recvBuf[0] == 0 && recvBuf[b] == b) {
                if (recvBuf[2] == 8 && recvBuf[3] == 0 && recvBuf[b2] == 6 && recvBuf[5] == b2 && recvBuf[6] == 0 && recvBuf[7] == 2 && recvBuf[14] == desiredIp[0] && recvBuf[15] == desiredIp[1] && recvBuf[16] == desiredIp[2] && recvBuf[17] == desiredIp[3] && !isMyMac(this.mMyMac, recvBuf)) {
                    Log.d(TAG, "doArp() return true ");
                    return true;
                }
                b = 1;
            }
            b2 = 4;
        }
        Log.d(TAG, "doArp() return false ");
        return false;
    }

    public void close() {
        try {
            this.mSocket.close();
        } catch (IOException e) {
        }
    }

    public boolean isMyMac(byte[] myMac, byte[] recvBuf) {
        if (recvBuf[8] == myMac[0] && recvBuf[9] == myMac[1] && recvBuf[10] == myMac[2] && recvBuf[11] == myMac[3] && recvBuf[12] == myMac[4] && recvBuf[13] == myMac[5]) {
            Log.d(TAG, "isMyMac return true");
            return true;
        }
        Log.d(TAG, "isMyMac return false");
        return false;
    }
}
