package android.hardware.wifi.V1_0;

import android.os.HidlSupport;
import android.os.HwBlob;
import android.os.HwParcel;
import java.util.ArrayList;
import java.util.Objects;

public final class WifiDebugHostWakeReasonRxPacketDetails {
    public int rxBroadcastCnt;
    public int rxMulticastCnt;
    public int rxUnicastCnt;

    public final boolean equals(Object otherObject) {
        if (this == otherObject) {
            return true;
        }
        if (otherObject == null || otherObject.getClass() != WifiDebugHostWakeReasonRxPacketDetails.class) {
            return false;
        }
        WifiDebugHostWakeReasonRxPacketDetails other = (WifiDebugHostWakeReasonRxPacketDetails) otherObject;
        return this.rxUnicastCnt == other.rxUnicastCnt && this.rxMulticastCnt == other.rxMulticastCnt && this.rxBroadcastCnt == other.rxBroadcastCnt;
    }

    public final int hashCode() {
        return Objects.hash(new Object[]{Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.rxUnicastCnt))), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.rxMulticastCnt))), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.rxBroadcastCnt)))});
    }

    public final String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("{");
        builder.append(".rxUnicastCnt = ");
        builder.append(this.rxUnicastCnt);
        builder.append(", .rxMulticastCnt = ");
        builder.append(this.rxMulticastCnt);
        builder.append(", .rxBroadcastCnt = ");
        builder.append(this.rxBroadcastCnt);
        builder.append("}");
        return builder.toString();
    }

    public final void readFromParcel(HwParcel parcel) {
        readEmbeddedFromParcel(parcel, parcel.readBuffer(12), 0);
    }

    public static final ArrayList<WifiDebugHostWakeReasonRxPacketDetails> readVectorFromParcel(HwParcel parcel) {
        ArrayList<WifiDebugHostWakeReasonRxPacketDetails> _hidl_vec = new ArrayList();
        HwBlob _hidl_blob = parcel.readBuffer(16);
        int _hidl_vec_size = _hidl_blob.getInt32(8);
        HwBlob childBlob = parcel.readEmbeddedBuffer((long) (_hidl_vec_size * 12), _hidl_blob.handle(), 0, true);
        _hidl_vec.clear();
        for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            WifiDebugHostWakeReasonRxPacketDetails _hidl_vec_element = new WifiDebugHostWakeReasonRxPacketDetails();
            _hidl_vec_element.readEmbeddedFromParcel(parcel, childBlob, (long) (_hidl_index_0 * 12));
            _hidl_vec.add(_hidl_vec_element);
        }
        return _hidl_vec;
    }

    public final void readEmbeddedFromParcel(HwParcel parcel, HwBlob _hidl_blob, long _hidl_offset) {
        this.rxUnicastCnt = _hidl_blob.getInt32(0 + _hidl_offset);
        this.rxMulticastCnt = _hidl_blob.getInt32(4 + _hidl_offset);
        this.rxBroadcastCnt = _hidl_blob.getInt32(8 + _hidl_offset);
    }

    public final void writeToParcel(HwParcel parcel) {
        HwBlob _hidl_blob = new HwBlob(12);
        writeEmbeddedToBlob(_hidl_blob, 0);
        parcel.writeBuffer(_hidl_blob);
    }

    public static final void writeVectorToParcel(HwParcel parcel, ArrayList<WifiDebugHostWakeReasonRxPacketDetails> _hidl_vec) {
        HwBlob _hidl_blob = new HwBlob(16);
        int _hidl_vec_size = _hidl_vec.size();
        _hidl_blob.putInt32(8, _hidl_vec_size);
        _hidl_blob.putBool(12, false);
        HwBlob childBlob = new HwBlob(_hidl_vec_size * 12);
        for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            ((WifiDebugHostWakeReasonRxPacketDetails) _hidl_vec.get(_hidl_index_0)).writeEmbeddedToBlob(childBlob, (long) (_hidl_index_0 * 12));
        }
        _hidl_blob.putBlob(0, childBlob);
        parcel.writeBuffer(_hidl_blob);
    }

    public final void writeEmbeddedToBlob(HwBlob _hidl_blob, long _hidl_offset) {
        _hidl_blob.putInt32(0 + _hidl_offset, this.rxUnicastCnt);
        _hidl_blob.putInt32(4 + _hidl_offset, this.rxMulticastCnt);
        _hidl_blob.putInt32(8 + _hidl_offset, this.rxBroadcastCnt);
    }
}
