package android.hardware.wifi.V1_0;

import android.os.HidlSupport;
import android.os.HwBlob;
import android.os.HwParcel;
import java.util.ArrayList;
import java.util.Objects;

public final class WifiDebugHostWakeReasonStats {
    public final ArrayList<Integer> cmdEventWakeCntPerType = new ArrayList();
    public final ArrayList<Integer> driverFwLocalWakeCntPerType = new ArrayList();
    public final WifiDebugHostWakeReasonRxIcmpPacketDetails rxIcmpPkWakeDetails = new WifiDebugHostWakeReasonRxIcmpPacketDetails();
    public final WifiDebugHostWakeReasonRxMulticastPacketDetails rxMulticastPkWakeDetails = new WifiDebugHostWakeReasonRxMulticastPacketDetails();
    public final WifiDebugHostWakeReasonRxPacketDetails rxPktWakeDetails = new WifiDebugHostWakeReasonRxPacketDetails();
    public int totalCmdEventWakeCnt;
    public int totalDriverFwLocalWakeCnt;
    public int totalRxPacketWakeCnt;

    public final boolean equals(Object otherObject) {
        if (this == otherObject) {
            return true;
        }
        if (otherObject == null || otherObject.getClass() != WifiDebugHostWakeReasonStats.class) {
            return false;
        }
        WifiDebugHostWakeReasonStats other = (WifiDebugHostWakeReasonStats) otherObject;
        return this.totalCmdEventWakeCnt == other.totalCmdEventWakeCnt && HidlSupport.deepEquals(this.cmdEventWakeCntPerType, other.cmdEventWakeCntPerType) && this.totalDriverFwLocalWakeCnt == other.totalDriverFwLocalWakeCnt && HidlSupport.deepEquals(this.driverFwLocalWakeCntPerType, other.driverFwLocalWakeCntPerType) && this.totalRxPacketWakeCnt == other.totalRxPacketWakeCnt && HidlSupport.deepEquals(this.rxPktWakeDetails, other.rxPktWakeDetails) && HidlSupport.deepEquals(this.rxMulticastPkWakeDetails, other.rxMulticastPkWakeDetails) && HidlSupport.deepEquals(this.rxIcmpPkWakeDetails, other.rxIcmpPkWakeDetails);
    }

    public final int hashCode() {
        return Objects.hash(new Object[]{Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.totalCmdEventWakeCnt))), Integer.valueOf(HidlSupport.deepHashCode(this.cmdEventWakeCntPerType)), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.totalDriverFwLocalWakeCnt))), Integer.valueOf(HidlSupport.deepHashCode(this.driverFwLocalWakeCntPerType)), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.totalRxPacketWakeCnt))), Integer.valueOf(HidlSupport.deepHashCode(this.rxPktWakeDetails)), Integer.valueOf(HidlSupport.deepHashCode(this.rxMulticastPkWakeDetails)), Integer.valueOf(HidlSupport.deepHashCode(this.rxIcmpPkWakeDetails))});
    }

    public final String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("{");
        builder.append(".totalCmdEventWakeCnt = ");
        builder.append(this.totalCmdEventWakeCnt);
        builder.append(", .cmdEventWakeCntPerType = ");
        builder.append(this.cmdEventWakeCntPerType);
        builder.append(", .totalDriverFwLocalWakeCnt = ");
        builder.append(this.totalDriverFwLocalWakeCnt);
        builder.append(", .driverFwLocalWakeCntPerType = ");
        builder.append(this.driverFwLocalWakeCntPerType);
        builder.append(", .totalRxPacketWakeCnt = ");
        builder.append(this.totalRxPacketWakeCnt);
        builder.append(", .rxPktWakeDetails = ");
        builder.append(this.rxPktWakeDetails);
        builder.append(", .rxMulticastPkWakeDetails = ");
        builder.append(this.rxMulticastPkWakeDetails);
        builder.append(", .rxIcmpPkWakeDetails = ");
        builder.append(this.rxIcmpPkWakeDetails);
        builder.append("}");
        return builder.toString();
    }

    public final void readFromParcel(HwParcel parcel) {
        readEmbeddedFromParcel(parcel, parcel.readBuffer(96), 0);
    }

    public static final ArrayList<WifiDebugHostWakeReasonStats> readVectorFromParcel(HwParcel parcel) {
        ArrayList<WifiDebugHostWakeReasonStats> _hidl_vec = new ArrayList();
        HwBlob _hidl_blob = parcel.readBuffer(16);
        int _hidl_vec_size = _hidl_blob.getInt32(8);
        HwBlob childBlob = parcel.readEmbeddedBuffer((long) (_hidl_vec_size * 96), _hidl_blob.handle(), 0, true);
        _hidl_vec.clear();
        for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            WifiDebugHostWakeReasonStats _hidl_vec_element = new WifiDebugHostWakeReasonStats();
            _hidl_vec_element.readEmbeddedFromParcel(parcel, childBlob, (long) (_hidl_index_0 * 96));
            _hidl_vec.add(_hidl_vec_element);
        }
        return _hidl_vec;
    }

    public final void readEmbeddedFromParcel(HwParcel parcel, HwBlob _hidl_blob, long _hidl_offset) {
        int _hidl_index_0;
        this.totalCmdEventWakeCnt = _hidl_blob.getInt32(0 + _hidl_offset);
        int _hidl_vec_size = _hidl_blob.getInt32((8 + _hidl_offset) + 8);
        HwBlob childBlob = parcel.readEmbeddedBuffer((long) (_hidl_vec_size * 4), _hidl_blob.handle(), (8 + _hidl_offset) + 0, true);
        this.cmdEventWakeCntPerType.clear();
        for (_hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            this.cmdEventWakeCntPerType.add(Integer.valueOf(childBlob.getInt32((long) (_hidl_index_0 * 4))));
        }
        this.totalDriverFwLocalWakeCnt = _hidl_blob.getInt32(24 + _hidl_offset);
        _hidl_vec_size = _hidl_blob.getInt32((32 + _hidl_offset) + 8);
        childBlob = parcel.readEmbeddedBuffer((long) (_hidl_vec_size * 4), _hidl_blob.handle(), (32 + _hidl_offset) + 0, true);
        this.driverFwLocalWakeCntPerType.clear();
        for (_hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            this.driverFwLocalWakeCntPerType.add(Integer.valueOf(childBlob.getInt32((long) (_hidl_index_0 * 4))));
        }
        this.totalRxPacketWakeCnt = _hidl_blob.getInt32(48 + _hidl_offset);
        this.rxPktWakeDetails.readEmbeddedFromParcel(parcel, _hidl_blob, 52 + _hidl_offset);
        this.rxMulticastPkWakeDetails.readEmbeddedFromParcel(parcel, _hidl_blob, 64 + _hidl_offset);
        this.rxIcmpPkWakeDetails.readEmbeddedFromParcel(parcel, _hidl_blob, 76 + _hidl_offset);
    }

    public final void writeToParcel(HwParcel parcel) {
        HwBlob _hidl_blob = new HwBlob(96);
        writeEmbeddedToBlob(_hidl_blob, 0);
        parcel.writeBuffer(_hidl_blob);
    }

    public static final void writeVectorToParcel(HwParcel parcel, ArrayList<WifiDebugHostWakeReasonStats> _hidl_vec) {
        HwBlob _hidl_blob = new HwBlob(16);
        int _hidl_vec_size = _hidl_vec.size();
        _hidl_blob.putInt32(8, _hidl_vec_size);
        _hidl_blob.putBool(12, false);
        HwBlob childBlob = new HwBlob(_hidl_vec_size * 96);
        for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            ((WifiDebugHostWakeReasonStats) _hidl_vec.get(_hidl_index_0)).writeEmbeddedToBlob(childBlob, (long) (_hidl_index_0 * 96));
        }
        _hidl_blob.putBlob(0, childBlob);
        parcel.writeBuffer(_hidl_blob);
    }

    public final void writeEmbeddedToBlob(HwBlob _hidl_blob, long _hidl_offset) {
        int _hidl_index_0;
        _hidl_blob.putInt32(0 + _hidl_offset, this.totalCmdEventWakeCnt);
        int _hidl_vec_size = this.cmdEventWakeCntPerType.size();
        _hidl_blob.putInt32((8 + _hidl_offset) + 8, _hidl_vec_size);
        _hidl_blob.putBool((8 + _hidl_offset) + 12, false);
        HwBlob childBlob = new HwBlob(_hidl_vec_size * 4);
        for (_hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            childBlob.putInt32((long) (_hidl_index_0 * 4), ((Integer) this.cmdEventWakeCntPerType.get(_hidl_index_0)).intValue());
        }
        _hidl_blob.putBlob((8 + _hidl_offset) + 0, childBlob);
        _hidl_blob.putInt32(24 + _hidl_offset, this.totalDriverFwLocalWakeCnt);
        _hidl_vec_size = this.driverFwLocalWakeCntPerType.size();
        _hidl_blob.putInt32((32 + _hidl_offset) + 8, _hidl_vec_size);
        _hidl_blob.putBool((32 + _hidl_offset) + 12, false);
        childBlob = new HwBlob(_hidl_vec_size * 4);
        for (_hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            childBlob.putInt32((long) (_hidl_index_0 * 4), ((Integer) this.driverFwLocalWakeCntPerType.get(_hidl_index_0)).intValue());
        }
        _hidl_blob.putBlob((32 + _hidl_offset) + 0, childBlob);
        _hidl_blob.putInt32(48 + _hidl_offset, this.totalRxPacketWakeCnt);
        this.rxPktWakeDetails.writeEmbeddedToBlob(_hidl_blob, 52 + _hidl_offset);
        this.rxMulticastPkWakeDetails.writeEmbeddedToBlob(_hidl_blob, 64 + _hidl_offset);
        this.rxIcmpPkWakeDetails.writeEmbeddedToBlob(_hidl_blob, 76 + _hidl_offset);
    }
}
