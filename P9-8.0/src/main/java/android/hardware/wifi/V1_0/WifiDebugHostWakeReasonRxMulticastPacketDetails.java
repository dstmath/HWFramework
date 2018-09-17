package android.hardware.wifi.V1_0;

import android.os.HidlSupport;
import android.os.HwBlob;
import android.os.HwParcel;
import java.util.ArrayList;
import java.util.Objects;

public final class WifiDebugHostWakeReasonRxMulticastPacketDetails {
    public int ipv4RxMulticastAddrCnt;
    public int ipv6RxMulticastAddrCnt;
    public int otherRxMulticastAddrCnt;

    public final boolean equals(Object otherObject) {
        if (this == otherObject) {
            return true;
        }
        if (otherObject == null || otherObject.getClass() != WifiDebugHostWakeReasonRxMulticastPacketDetails.class) {
            return false;
        }
        WifiDebugHostWakeReasonRxMulticastPacketDetails other = (WifiDebugHostWakeReasonRxMulticastPacketDetails) otherObject;
        return this.ipv4RxMulticastAddrCnt == other.ipv4RxMulticastAddrCnt && this.ipv6RxMulticastAddrCnt == other.ipv6RxMulticastAddrCnt && this.otherRxMulticastAddrCnt == other.otherRxMulticastAddrCnt;
    }

    public final int hashCode() {
        return Objects.hash(new Object[]{Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.ipv4RxMulticastAddrCnt))), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.ipv6RxMulticastAddrCnt))), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.otherRxMulticastAddrCnt)))});
    }

    public final String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("{");
        builder.append(".ipv4RxMulticastAddrCnt = ");
        builder.append(this.ipv4RxMulticastAddrCnt);
        builder.append(", .ipv6RxMulticastAddrCnt = ");
        builder.append(this.ipv6RxMulticastAddrCnt);
        builder.append(", .otherRxMulticastAddrCnt = ");
        builder.append(this.otherRxMulticastAddrCnt);
        builder.append("}");
        return builder.toString();
    }

    public final void readFromParcel(HwParcel parcel) {
        readEmbeddedFromParcel(parcel, parcel.readBuffer(12), 0);
    }

    public static final ArrayList<WifiDebugHostWakeReasonRxMulticastPacketDetails> readVectorFromParcel(HwParcel parcel) {
        ArrayList<WifiDebugHostWakeReasonRxMulticastPacketDetails> _hidl_vec = new ArrayList();
        HwBlob _hidl_blob = parcel.readBuffer(16);
        int _hidl_vec_size = _hidl_blob.getInt32(8);
        HwBlob childBlob = parcel.readEmbeddedBuffer((long) (_hidl_vec_size * 12), _hidl_blob.handle(), 0, true);
        _hidl_vec.clear();
        for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            WifiDebugHostWakeReasonRxMulticastPacketDetails _hidl_vec_element = new WifiDebugHostWakeReasonRxMulticastPacketDetails();
            _hidl_vec_element.readEmbeddedFromParcel(parcel, childBlob, (long) (_hidl_index_0 * 12));
            _hidl_vec.add(_hidl_vec_element);
        }
        return _hidl_vec;
    }

    public final void readEmbeddedFromParcel(HwParcel parcel, HwBlob _hidl_blob, long _hidl_offset) {
        this.ipv4RxMulticastAddrCnt = _hidl_blob.getInt32(0 + _hidl_offset);
        this.ipv6RxMulticastAddrCnt = _hidl_blob.getInt32(4 + _hidl_offset);
        this.otherRxMulticastAddrCnt = _hidl_blob.getInt32(8 + _hidl_offset);
    }

    public final void writeToParcel(HwParcel parcel) {
        HwBlob _hidl_blob = new HwBlob(12);
        writeEmbeddedToBlob(_hidl_blob, 0);
        parcel.writeBuffer(_hidl_blob);
    }

    public static final void writeVectorToParcel(HwParcel parcel, ArrayList<WifiDebugHostWakeReasonRxMulticastPacketDetails> _hidl_vec) {
        HwBlob _hidl_blob = new HwBlob(16);
        int _hidl_vec_size = _hidl_vec.size();
        _hidl_blob.putInt32(8, _hidl_vec_size);
        _hidl_blob.putBool(12, false);
        HwBlob childBlob = new HwBlob(_hidl_vec_size * 12);
        for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            ((WifiDebugHostWakeReasonRxMulticastPacketDetails) _hidl_vec.get(_hidl_index_0)).writeEmbeddedToBlob(childBlob, (long) (_hidl_index_0 * 12));
        }
        _hidl_blob.putBlob(0, childBlob);
        parcel.writeBuffer(_hidl_blob);
    }

    public final void writeEmbeddedToBlob(HwBlob _hidl_blob, long _hidl_offset) {
        _hidl_blob.putInt32(0 + _hidl_offset, this.ipv4RxMulticastAddrCnt);
        _hidl_blob.putInt32(4 + _hidl_offset, this.ipv6RxMulticastAddrCnt);
        _hidl_blob.putInt32(8 + _hidl_offset, this.otherRxMulticastAddrCnt);
    }
}
