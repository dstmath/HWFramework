package android.net;

import android.net.SocketKeepalive;
import android.net.util.IpUtils;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;
import java.net.InetAddress;

public class KeepalivePacketData implements Parcelable {
    public static final Parcelable.Creator<KeepalivePacketData> CREATOR = new Parcelable.Creator<KeepalivePacketData>() {
        /* class android.net.KeepalivePacketData.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public KeepalivePacketData createFromParcel(Parcel in) {
            return new KeepalivePacketData(in);
        }

        @Override // android.os.Parcelable.Creator
        public KeepalivePacketData[] newArray(int size) {
            return new KeepalivePacketData[size];
        }
    };
    protected static final int IPV4_HEADER_LENGTH = 20;
    private static final String TAG = "KeepalivePacketData";
    protected static final int UDP_HEADER_LENGTH = 8;
    public final InetAddress dstAddress;
    public final int dstPort;
    private final byte[] mPacket;
    public final InetAddress srcAddress;
    public final int srcPort;

    protected KeepalivePacketData(InetAddress srcAddress2, int srcPort2, InetAddress dstAddress2, int dstPort2, byte[] data) throws SocketKeepalive.InvalidPacketException {
        this.srcAddress = srcAddress2;
        this.dstAddress = dstAddress2;
        this.srcPort = srcPort2;
        this.dstPort = dstPort2;
        this.mPacket = data;
        if (srcAddress2 == null || dstAddress2 == null || !srcAddress2.getClass().getName().equals(dstAddress2.getClass().getName())) {
            Log.e(TAG, "Invalid or mismatched InetAddresses in KeepalivePacketData");
            throw new SocketKeepalive.InvalidPacketException(-21);
        } else if (!IpUtils.isValidUdpOrTcpPort(srcPort2) || !IpUtils.isValidUdpOrTcpPort(dstPort2)) {
            Log.e(TAG, "Invalid ports in KeepalivePacketData");
            throw new SocketKeepalive.InvalidPacketException(-22);
        }
    }

    public byte[] getPacket() {
        return (byte[]) this.mPacket.clone();
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel out, int flags) {
        out.writeString(this.srcAddress.getHostAddress());
        out.writeString(this.dstAddress.getHostAddress());
        out.writeInt(this.srcPort);
        out.writeInt(this.dstPort);
        out.writeByteArray(this.mPacket);
    }

    protected KeepalivePacketData(Parcel in) {
        this.srcAddress = NetworkUtils.numericToInetAddress(in.readString());
        this.dstAddress = NetworkUtils.numericToInetAddress(in.readString());
        this.srcPort = in.readInt();
        this.dstPort = in.readInt();
        this.mPacket = in.createByteArray();
    }
}
