package android.net.util;

import android.net.NattKeepalivePacketData;
import android.net.NattKeepalivePacketDataParcelable;

public final class KeepalivePacketDataUtil {
    public static NattKeepalivePacketDataParcelable toStableParcelable(NattKeepalivePacketData pkt) {
        NattKeepalivePacketDataParcelable parcel = new NattKeepalivePacketDataParcelable();
        parcel.srcAddress = pkt.srcAddress.getAddress();
        parcel.srcPort = pkt.srcPort;
        parcel.dstAddress = pkt.dstAddress.getAddress();
        parcel.dstPort = pkt.dstPort;
        return parcel;
    }
}
