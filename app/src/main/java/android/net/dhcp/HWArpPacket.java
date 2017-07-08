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
    private final byte[] mMyMac;
    private final RawSocket mSocket;

    public HWArpPacket(String interfaceName, InetAddress myAddr, String mac) throws SocketException {
        this.mMyMac = new byte[MAC_ADDR_LENGTH];
        this.mInterfaceName = interfaceName;
        if (mac != null) {
            for (int i = 0; i < MAC_ADDR_LENGTH; i += ETHERNET_TYPE) {
                this.mMyMac[i] = (byte) Integer.parseInt(mac.substring(i * 3, (i * 3) + 2), 16);
            }
        }
        if (myAddr instanceof Inet6Address) {
            throw new IllegalArgumentException("IPv6 unsupported");
        }
        this.L2_BROADCAST = new byte[MAC_ADDR_LENGTH];
        Arrays.fill(this.L2_BROADCAST, (byte) -1);
        this.mSocket = new RawSocket(this.mInterfaceName, (short) 2054);
    }

    public boolean doArp(int timeoutMillis, Inet4Address requestedAddress) {
        ByteBuffer buf = ByteBuffer.allocate(MAX_LENGTH);
        byte[] desiredIp = requestedAddress.getAddress();
        long timeout = SystemClock.elapsedRealtime() + ((long) timeoutMillis);
        buf.clear();
        buf.order(ByteOrder.BIG_ENDIAN);
        buf.putShort((short) 1);
        buf.putShort(StructNlMsgHdr.NLM_F_APPEND);
        buf.put((byte) 6);
        buf.put((byte) 4);
        buf.putShort((short) 1);
        buf.put(this.mMyMac);
        buf.put(new byte[IPV4_LENGTH]);
        buf.put(new byte[MAC_ADDR_LENGTH]);
        buf.put(desiredIp);
        buf.flip();
        this.mSocket.write(this.L2_BROADCAST, buf.array(), 0, buf.limit());
        byte[] recvBuf = new byte[MAX_LENGTH];
        while (SystemClock.elapsedRealtime() < timeout) {
            if (this.mSocket.read(recvBuf, 0, recvBuf.length, -1, (int) (timeout - SystemClock.elapsedRealtime())) >= ARP_LENGTH && recvBuf[0] == null && recvBuf[ETHERNET_TYPE] == ETHERNET_TYPE && recvBuf[2] == 8 && recvBuf[3] == null && recvBuf[IPV4_LENGTH] == MAC_ADDR_LENGTH && recvBuf[5] == IPV4_LENGTH && recvBuf[MAC_ADDR_LENGTH] == null && recvBuf[7] == 2 && recvBuf[14] == desiredIp[0] && recvBuf[15] == desiredIp[ETHERNET_TYPE] && recvBuf[16] == desiredIp[2] && recvBuf[17] == desiredIp[3]) {
                Log.d(TAG, "doArp() return true ");
                return true;
            }
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
}
