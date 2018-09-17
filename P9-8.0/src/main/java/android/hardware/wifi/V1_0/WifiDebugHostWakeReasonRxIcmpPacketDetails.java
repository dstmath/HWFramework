package android.hardware.wifi.V1_0;

import android.os.HidlSupport;
import android.os.HwBlob;
import android.os.HwParcel;
import java.util.ArrayList;
import java.util.Objects;

public final class WifiDebugHostWakeReasonRxIcmpPacketDetails {
    public int icmp6Na;
    public int icmp6Ns;
    public int icmp6Pkt;
    public int icmp6Ra;
    public int icmpPkt;

    public final boolean equals(Object otherObject) {
        if (this == otherObject) {
            return true;
        }
        if (otherObject == null || otherObject.getClass() != WifiDebugHostWakeReasonRxIcmpPacketDetails.class) {
            return false;
        }
        WifiDebugHostWakeReasonRxIcmpPacketDetails other = (WifiDebugHostWakeReasonRxIcmpPacketDetails) otherObject;
        return this.icmpPkt == other.icmpPkt && this.icmp6Pkt == other.icmp6Pkt && this.icmp6Ra == other.icmp6Ra && this.icmp6Na == other.icmp6Na && this.icmp6Ns == other.icmp6Ns;
    }

    public final int hashCode() {
        return Objects.hash(new Object[]{Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.icmpPkt))), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.icmp6Pkt))), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.icmp6Ra))), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.icmp6Na))), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.icmp6Ns)))});
    }

    public final String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("{");
        builder.append(".icmpPkt = ");
        builder.append(this.icmpPkt);
        builder.append(", .icmp6Pkt = ");
        builder.append(this.icmp6Pkt);
        builder.append(", .icmp6Ra = ");
        builder.append(this.icmp6Ra);
        builder.append(", .icmp6Na = ");
        builder.append(this.icmp6Na);
        builder.append(", .icmp6Ns = ");
        builder.append(this.icmp6Ns);
        builder.append("}");
        return builder.toString();
    }

    public final void readFromParcel(HwParcel parcel) {
        readEmbeddedFromParcel(parcel, parcel.readBuffer(20), 0);
    }

    public static final ArrayList<WifiDebugHostWakeReasonRxIcmpPacketDetails> readVectorFromParcel(HwParcel parcel) {
        ArrayList<WifiDebugHostWakeReasonRxIcmpPacketDetails> _hidl_vec = new ArrayList();
        HwBlob _hidl_blob = parcel.readBuffer(16);
        int _hidl_vec_size = _hidl_blob.getInt32(8);
        HwBlob childBlob = parcel.readEmbeddedBuffer((long) (_hidl_vec_size * 20), _hidl_blob.handle(), 0, true);
        _hidl_vec.clear();
        for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            WifiDebugHostWakeReasonRxIcmpPacketDetails _hidl_vec_element = new WifiDebugHostWakeReasonRxIcmpPacketDetails();
            _hidl_vec_element.readEmbeddedFromParcel(parcel, childBlob, (long) (_hidl_index_0 * 20));
            _hidl_vec.add(_hidl_vec_element);
        }
        return _hidl_vec;
    }

    public final void readEmbeddedFromParcel(HwParcel parcel, HwBlob _hidl_blob, long _hidl_offset) {
        this.icmpPkt = _hidl_blob.getInt32(0 + _hidl_offset);
        this.icmp6Pkt = _hidl_blob.getInt32(4 + _hidl_offset);
        this.icmp6Ra = _hidl_blob.getInt32(8 + _hidl_offset);
        this.icmp6Na = _hidl_blob.getInt32(12 + _hidl_offset);
        this.icmp6Ns = _hidl_blob.getInt32(16 + _hidl_offset);
    }

    public final void writeToParcel(HwParcel parcel) {
        HwBlob _hidl_blob = new HwBlob(20);
        writeEmbeddedToBlob(_hidl_blob, 0);
        parcel.writeBuffer(_hidl_blob);
    }

    public static final void writeVectorToParcel(HwParcel parcel, ArrayList<WifiDebugHostWakeReasonRxIcmpPacketDetails> _hidl_vec) {
        HwBlob _hidl_blob = new HwBlob(16);
        int _hidl_vec_size = _hidl_vec.size();
        _hidl_blob.putInt32(8, _hidl_vec_size);
        _hidl_blob.putBool(12, false);
        HwBlob childBlob = new HwBlob(_hidl_vec_size * 20);
        for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            ((WifiDebugHostWakeReasonRxIcmpPacketDetails) _hidl_vec.get(_hidl_index_0)).writeEmbeddedToBlob(childBlob, (long) (_hidl_index_0 * 20));
        }
        _hidl_blob.putBlob(0, childBlob);
        parcel.writeBuffer(_hidl_blob);
    }

    public final void writeEmbeddedToBlob(HwBlob _hidl_blob, long _hidl_offset) {
        _hidl_blob.putInt32(0 + _hidl_offset, this.icmpPkt);
        _hidl_blob.putInt32(4 + _hidl_offset, this.icmp6Pkt);
        _hidl_blob.putInt32(8 + _hidl_offset, this.icmp6Ra);
        _hidl_blob.putInt32(12 + _hidl_offset, this.icmp6Na);
        _hidl_blob.putInt32(16 + _hidl_offset, this.icmp6Ns);
    }
}
