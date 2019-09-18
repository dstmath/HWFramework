package android.net;

import android.bluetooth.BluetoothHidDevice;
import android.net.util.IpUtils;
import android.os.Parcel;
import android.os.Parcelable;
import android.system.OsConstants;
import android.util.Log;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class KeepalivePacketData implements Parcelable {
    public static final Parcelable.Creator<KeepalivePacketData> CREATOR = new Parcelable.Creator<KeepalivePacketData>() {
        public KeepalivePacketData createFromParcel(Parcel in) {
            return new KeepalivePacketData(in);
        }

        public KeepalivePacketData[] newArray(int size) {
            return new KeepalivePacketData[size];
        }
    };
    private static final int IPV4_HEADER_LENGTH = 20;
    private static final String TAG = "KeepalivePacketData";
    private static final int UDP_HEADER_LENGTH = 8;
    public final InetAddress dstAddress;
    public final int dstPort;
    private final byte[] mPacket;
    public final InetAddress srcAddress;
    public final int srcPort;

    public static class InvalidPacketException extends Exception {
        public final int error;

        public InvalidPacketException(int error2) {
            this.error = error2;
        }
    }

    protected KeepalivePacketData(InetAddress srcAddress2, int srcPort2, InetAddress dstAddress2, int dstPort2, byte[] data) throws InvalidPacketException {
        this.srcAddress = srcAddress2;
        this.dstAddress = dstAddress2;
        this.srcPort = srcPort2;
        this.dstPort = dstPort2;
        this.mPacket = data;
        if (srcAddress2 == null || dstAddress2 == null || !srcAddress2.getClass().getName().equals(dstAddress2.getClass().getName())) {
            Log.e(TAG, "Invalid or mismatched InetAddresses in KeepalivePacketData");
            throw new InvalidPacketException(-21);
        } else if (!IpUtils.isValidUdpOrTcpPort(srcPort2) || !IpUtils.isValidUdpOrTcpPort(dstPort2)) {
            Log.e(TAG, "Invalid ports in KeepalivePacketData");
            throw new InvalidPacketException(-22);
        }
    }

    public byte[] getPacket() {
        return (byte[]) this.mPacket.clone();
    }

    public static KeepalivePacketData nattKeepalivePacket(InetAddress srcAddress2, int srcPort2, InetAddress dstAddress2, int dstPort2) throws InvalidPacketException {
        if (!(srcAddress2 instanceof Inet4Address) || !(dstAddress2 instanceof Inet4Address)) {
            throw new InvalidPacketException(-21);
        } else if (dstPort2 == 4500) {
            ByteBuffer buf = ByteBuffer.allocate(29);
            buf.order(ByteOrder.BIG_ENDIAN);
            buf.putShort(17664);
            buf.putShort((short) 29);
            buf.putInt(0);
            buf.put(BluetoothHidDevice.SUBCLASS1_KEYBOARD);
            buf.put((byte) OsConstants.IPPROTO_UDP);
            int ipChecksumOffset = buf.position();
            buf.putShort(0);
            buf.put(srcAddress2.getAddress());
            buf.put(dstAddress2.getAddress());
            buf.putShort((short) srcPort2);
            buf.putShort((short) dstPort2);
            buf.putShort((short) (29 - 20));
            int udpChecksumOffset = buf.position();
            buf.putShort(0);
            buf.put((byte) -1);
            buf.putShort(ipChecksumOffset, IpUtils.ipChecksum(buf, 0));
            buf.putShort(udpChecksumOffset, IpUtils.udpChecksum(buf, 0, 20));
            KeepalivePacketData keepalivePacketData = new KeepalivePacketData(srcAddress2, srcPort2, dstAddress2, dstPort2, buf.array());
            return keepalivePacketData;
        } else {
            throw new InvalidPacketException(-22);
        }
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeString(this.srcAddress.getHostAddress());
        out.writeString(this.dstAddress.getHostAddress());
        out.writeInt(this.srcPort);
        out.writeInt(this.dstPort);
        out.writeByteArray(this.mPacket);
    }

    private KeepalivePacketData(Parcel in) {
        this.srcAddress = NetworkUtils.numericToInetAddress(in.readString());
        this.dstAddress = NetworkUtils.numericToInetAddress(in.readString());
        this.srcPort = in.readInt();
        this.dstPort = in.readInt();
        this.mPacket = in.createByteArray();
    }
}
