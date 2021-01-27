package android.net.dhcp;

import android.net.arp.RawSocket;
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

public class HwArpPacket {
    private static final int ARP_LENGTH = 28;
    private static final int ARP_REPLY = 2;
    private static final byte BROADCAST_ADDRESS_BYTE_DATA = -1;
    private static final int ETHERNET_TYPE = 1;
    private static final int IPV4_LENGTH = 4;
    private static final int MAC_ADDRESS_STR_TO_BYTE_END_INDEX = 16;
    private static final int MAC_ADDRESS_STR_TO_BYTE_NUM = 2;
    private static final int MAC_ADDRESS_STR_TO_BYTE_STEP = 3;
    private static final int MAC_ADDR_LENGTH = 6;
    private static final int MAX_LENGTH = 1500;
    private static final int PROTOCOL_IP = 8;
    private static final String TAG = "HwArpPacket";
    private String mInterfaceName;
    private final byte[] mL2Broadcast;
    private InetAddress mMyAddr = null;
    private final byte[] mMyMac = new byte[6];
    private final RawSocket mSocket;

    public HwArpPacket(String interfaceName, InetAddress myAddr, String mac) throws SocketException, IllegalArgumentException {
        this.mInterfaceName = interfaceName;
        if (mac != null) {
            for (int i = 0; i < 6; i++) {
                try {
                    this.mMyMac[i] = (byte) Integer.parseInt(mac.substring(i * 3, (i * 3) + 2), 16);
                } catch (NumberFormatException e) {
                    Log.e(TAG, "HwArpPacket initiation failure");
                }
            }
        }
        if (!(myAddr instanceof Inet6Address)) {
            this.mL2Broadcast = new byte[6];
            Arrays.fill(this.mL2Broadcast, (byte) BROADCAST_ADDRESS_BYTE_DATA);
            this.mSocket = new RawSocket(this.mInterfaceName, RawSocket.ETH_P_ARP);
            this.mMyAddr = myAddr;
            return;
        }
        throw new IllegalArgumentException("IPv6 unsupported");
    }

    public boolean doArp(int timeoutMillis, Inet4Address requestedAddress, boolean isFillSenderIp) {
        char c;
        InetAddress inetAddress;
        ByteBuffer buf = ByteBuffer.allocate(MAX_LENGTH);
        byte[] desiredIp = requestedAddress.getAddress();
        buf.clear();
        buf.order(ByteOrder.BIG_ENDIAN);
        buf.putShort(1);
        buf.putShort(RawSocket.ETH_P_IP);
        buf.put((byte) 6);
        buf.put((byte) 4);
        buf.putShort(1);
        buf.put(this.mMyMac);
        buf.put((!isFillSenderIp || (inetAddress = this.mMyAddr) == null) ? new byte[4] : inetAddress.getAddress());
        buf.put(new byte[6]);
        buf.put(desiredIp);
        buf.flip();
        this.mSocket.write(this.mL2Broadcast, buf.array(), 0, buf.limit());
        byte[] recvBuf = new byte[MAX_LENGTH];
        long timeout = SystemClock.elapsedRealtime() + ((long) timeoutMillis);
        while (SystemClock.elapsedRealtime() < timeout) {
            if (this.mSocket.read(recvBuf, 0, recvBuf.length, -1, (int) (timeout - SystemClock.elapsedRealtime())) >= 28 && recvBuf[0] == 0 && recvBuf[1] == 1 && recvBuf[2] == 8 && recvBuf[3] == 0) {
                c = 6;
                if (recvBuf[4] == 6 && recvBuf[5] == 4 && recvBuf[6] == 0 && recvBuf[7] == 2 && recvBuf[14] == desiredIp[0] && recvBuf[15] == desiredIp[1] && recvBuf[16] == desiredIp[2] && recvBuf[17] == desiredIp[3] && !isMyMac(this.mMyMac, recvBuf)) {
                    Log.d(TAG, "doArp() return true");
                    return true;
                }
            } else {
                c = 6;
            }
        }
        Log.d(TAG, "doArp() return false");
        return false;
    }

    public void close() {
        try {
            this.mSocket.close();
        } catch (IOException e) {
            Log.d(TAG, "IOException");
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
