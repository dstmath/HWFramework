package android.net;

import android.net.SocketKeepalive;
import android.net.util.IpUtils;
import android.os.Parcel;
import android.os.Parcelable;
import android.system.OsConstants;
import com.android.server.pm.DumpState;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Objects;

public class TcpKeepalivePacketData extends KeepalivePacketData implements Parcelable {
    public static final Parcelable.Creator<TcpKeepalivePacketData> CREATOR = new Parcelable.Creator<TcpKeepalivePacketData>() {
        /* class android.net.TcpKeepalivePacketData.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public TcpKeepalivePacketData createFromParcel(Parcel in) {
            return new TcpKeepalivePacketData(in);
        }

        @Override // android.os.Parcelable.Creator
        public TcpKeepalivePacketData[] newArray(int size) {
            return new TcpKeepalivePacketData[size];
        }
    };
    private static final int IPV4_HEADER_LENGTH = 20;
    private static final int IPV6_HEADER_LENGTH = 40;
    private static final String TAG = "TcpKeepalivePacketData";
    private static final int TCP_HEADER_LENGTH = 20;
    public final int ipTos;
    public final int ipTtl;
    public final int tcpAck;
    public final int tcpSeq;
    public final int tcpWnd;
    public final int tcpWndScale;

    private TcpKeepalivePacketData(TcpKeepalivePacketDataParcelable tcpDetails, byte[] data) throws SocketKeepalive.InvalidPacketException, UnknownHostException {
        super(InetAddress.getByAddress(tcpDetails.srcAddress), tcpDetails.srcPort, InetAddress.getByAddress(tcpDetails.dstAddress), tcpDetails.dstPort, data);
        this.tcpSeq = tcpDetails.seq;
        this.tcpAck = tcpDetails.ack;
        this.tcpWnd = tcpDetails.rcvWnd;
        this.tcpWndScale = tcpDetails.rcvWndScale;
        this.ipTos = tcpDetails.tos;
        this.ipTtl = tcpDetails.ttl;
    }

    public static TcpKeepalivePacketData tcpKeepalivePacket(TcpKeepalivePacketDataParcelable tcpDetails) throws SocketKeepalive.InvalidPacketException {
        try {
            if (tcpDetails.srcAddress != null && tcpDetails.dstAddress != null && tcpDetails.srcAddress.length == 4 && tcpDetails.dstAddress.length == 4) {
                return new TcpKeepalivePacketData(tcpDetails, buildV4Packet(tcpDetails));
            }
            throw new SocketKeepalive.InvalidPacketException(-21);
        } catch (UnknownHostException e) {
            throw new SocketKeepalive.InvalidPacketException(-21);
        }
    }

    private static byte[] buildV4Packet(TcpKeepalivePacketDataParcelable tcpDetails) {
        ByteBuffer buf = ByteBuffer.allocate(40);
        buf.order(ByteOrder.BIG_ENDIAN);
        buf.put((byte) 69);
        buf.put((byte) tcpDetails.tos);
        buf.putShort(40);
        buf.putInt(DumpState.DUMP_KEYSETS);
        buf.put((byte) tcpDetails.ttl);
        buf.put((byte) OsConstants.IPPROTO_TCP);
        int ipChecksumOffset = buf.position();
        buf.putShort(0);
        buf.put(tcpDetails.srcAddress);
        buf.put(tcpDetails.dstAddress);
        buf.putShort((short) tcpDetails.srcPort);
        buf.putShort((short) tcpDetails.dstPort);
        buf.putInt(tcpDetails.seq);
        buf.putInt(tcpDetails.ack);
        buf.putShort(20496);
        buf.putShort((short) (tcpDetails.rcvWnd >> tcpDetails.rcvWndScale));
        int tcpChecksumOffset = buf.position();
        buf.putShort(0);
        buf.putShort(0);
        buf.putShort(ipChecksumOffset, IpUtils.ipChecksum(buf, 0));
        buf.putShort(tcpChecksumOffset, IpUtils.tcpChecksum(buf, 0, 20, 20));
        return buf.array();
    }

    @Override // java.lang.Object
    public boolean equals(Object o) {
        if (!(o instanceof TcpKeepalivePacketData)) {
            return false;
        }
        TcpKeepalivePacketData other = (TcpKeepalivePacketData) o;
        if (this.srcAddress.equals(other.srcAddress) && this.dstAddress.equals(other.dstAddress) && this.srcPort == other.srcPort && this.dstPort == other.dstPort && this.tcpAck == other.tcpAck && this.tcpSeq == other.tcpSeq && this.tcpWnd == other.tcpWnd && this.tcpWndScale == other.tcpWndScale && this.ipTos == other.ipTos && this.ipTtl == other.ipTtl) {
            return true;
        }
        return false;
    }

    @Override // java.lang.Object
    public int hashCode() {
        return Objects.hash(this.srcAddress, this.dstAddress, Integer.valueOf(this.srcPort), Integer.valueOf(this.dstPort), Integer.valueOf(this.tcpAck), Integer.valueOf(this.tcpSeq), Integer.valueOf(this.tcpWnd), Integer.valueOf(this.tcpWndScale), Integer.valueOf(this.ipTos), Integer.valueOf(this.ipTtl));
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel out, int flags) {
        TcpKeepalivePacketData.super.writeToParcel(out, flags);
        out.writeInt(this.tcpSeq);
        out.writeInt(this.tcpAck);
        out.writeInt(this.tcpWnd);
        out.writeInt(this.tcpWndScale);
        out.writeInt(this.ipTos);
        out.writeInt(this.ipTtl);
    }

    private TcpKeepalivePacketData(Parcel in) {
        super(in);
        this.tcpSeq = in.readInt();
        this.tcpAck = in.readInt();
        this.tcpWnd = in.readInt();
        this.tcpWndScale = in.readInt();
        this.ipTos = in.readInt();
        this.ipTtl = in.readInt();
    }

    public TcpKeepalivePacketDataParcelable toStableParcelable() {
        TcpKeepalivePacketDataParcelable parcel = new TcpKeepalivePacketDataParcelable();
        parcel.srcAddress = this.srcAddress.getAddress();
        parcel.srcPort = this.srcPort;
        parcel.dstAddress = this.dstAddress.getAddress();
        parcel.dstPort = this.dstPort;
        parcel.seq = this.tcpSeq;
        parcel.ack = this.tcpAck;
        parcel.rcvWnd = this.tcpWnd;
        parcel.rcvWndScale = this.tcpWndScale;
        parcel.tos = this.ipTos;
        parcel.ttl = this.ipTtl;
        return parcel;
    }

    @Override // java.lang.Object
    public String toString() {
        return "saddr: " + this.srcAddress + " daddr: " + this.dstAddress + " sport: " + this.srcPort + " dport: " + this.dstPort + " seq: " + this.tcpSeq + " ack: " + this.tcpAck + " wnd: " + this.tcpWnd + " wndScale: " + this.tcpWndScale + " tos: " + this.ipTos + " ttl: " + this.ipTtl;
    }
}
