package android.hardware.wifi.V1_0;

import android.os.HidlSupport;
import android.os.HwBlob;
import android.os.HwParcel;
import java.util.ArrayList;
import java.util.Objects;

public final class WifiDebugHostWakeReasonStats {
    public ArrayList<Integer> cmdEventWakeCntPerType = new ArrayList<>();
    public ArrayList<Integer> driverFwLocalWakeCntPerType = new ArrayList<>();
    public WifiDebugHostWakeReasonRxIcmpPacketDetails rxIcmpPkWakeDetails = new WifiDebugHostWakeReasonRxIcmpPacketDetails();
    public WifiDebugHostWakeReasonRxMulticastPacketDetails rxMulticastPkWakeDetails = new WifiDebugHostWakeReasonRxMulticastPacketDetails();
    public WifiDebugHostWakeReasonRxPacketDetails rxPktWakeDetails = new WifiDebugHostWakeReasonRxPacketDetails();
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
        if (this.totalCmdEventWakeCnt == other.totalCmdEventWakeCnt && HidlSupport.deepEquals(this.cmdEventWakeCntPerType, other.cmdEventWakeCntPerType) && this.totalDriverFwLocalWakeCnt == other.totalDriverFwLocalWakeCnt && HidlSupport.deepEquals(this.driverFwLocalWakeCntPerType, other.driverFwLocalWakeCntPerType) && this.totalRxPacketWakeCnt == other.totalRxPacketWakeCnt && HidlSupport.deepEquals(this.rxPktWakeDetails, other.rxPktWakeDetails) && HidlSupport.deepEquals(this.rxMulticastPkWakeDetails, other.rxMulticastPkWakeDetails) && HidlSupport.deepEquals(this.rxIcmpPkWakeDetails, other.rxIcmpPkWakeDetails)) {
            return true;
        }
        return false;
    }

    public final int hashCode() {
        return Objects.hash(Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.totalCmdEventWakeCnt))), Integer.valueOf(HidlSupport.deepHashCode(this.cmdEventWakeCntPerType)), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.totalDriverFwLocalWakeCnt))), Integer.valueOf(HidlSupport.deepHashCode(this.driverFwLocalWakeCntPerType)), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.totalRxPacketWakeCnt))), Integer.valueOf(HidlSupport.deepHashCode(this.rxPktWakeDetails)), Integer.valueOf(HidlSupport.deepHashCode(this.rxMulticastPkWakeDetails)), Integer.valueOf(HidlSupport.deepHashCode(this.rxIcmpPkWakeDetails)));
    }

    public final String toString() {
        return "{.totalCmdEventWakeCnt = " + this.totalCmdEventWakeCnt + ", .cmdEventWakeCntPerType = " + this.cmdEventWakeCntPerType + ", .totalDriverFwLocalWakeCnt = " + this.totalDriverFwLocalWakeCnt + ", .driverFwLocalWakeCntPerType = " + this.driverFwLocalWakeCntPerType + ", .totalRxPacketWakeCnt = " + this.totalRxPacketWakeCnt + ", .rxPktWakeDetails = " + this.rxPktWakeDetails + ", .rxMulticastPkWakeDetails = " + this.rxMulticastPkWakeDetails + ", .rxIcmpPkWakeDetails = " + this.rxIcmpPkWakeDetails + "}";
    }

    public final void readFromParcel(HwParcel parcel) {
        readEmbeddedFromParcel(parcel, parcel.readBuffer(96), 0);
    }

    public static final ArrayList<WifiDebugHostWakeReasonStats> readVectorFromParcel(HwParcel parcel) {
        ArrayList<WifiDebugHostWakeReasonStats> _hidl_vec = new ArrayList<>();
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
        this.totalCmdEventWakeCnt = _hidl_blob.getInt32(_hidl_offset + 0);
        int _hidl_vec_size = _hidl_blob.getInt32(_hidl_offset + 8 + 8);
        HwBlob childBlob = parcel.readEmbeddedBuffer((long) (_hidl_vec_size * 4), _hidl_blob.handle(), _hidl_offset + 8 + 0, true);
        this.cmdEventWakeCntPerType.clear();
        for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            this.cmdEventWakeCntPerType.add(Integer.valueOf(childBlob.getInt32((long) (_hidl_index_0 * 4))));
        }
        this.totalDriverFwLocalWakeCnt = _hidl_blob.getInt32(_hidl_offset + 24);
        int _hidl_vec_size2 = _hidl_blob.getInt32(_hidl_offset + 32 + 8);
        HwBlob childBlob2 = parcel.readEmbeddedBuffer((long) (_hidl_vec_size2 * 4), _hidl_blob.handle(), _hidl_offset + 32 + 0, true);
        this.driverFwLocalWakeCntPerType.clear();
        for (int _hidl_index_02 = 0; _hidl_index_02 < _hidl_vec_size2; _hidl_index_02++) {
            this.driverFwLocalWakeCntPerType.add(Integer.valueOf(childBlob2.getInt32((long) (_hidl_index_02 * 4))));
        }
        this.totalRxPacketWakeCnt = _hidl_blob.getInt32(_hidl_offset + 48);
        this.rxPktWakeDetails.readEmbeddedFromParcel(parcel, _hidl_blob, _hidl_offset + 52);
        this.rxMulticastPkWakeDetails.readEmbeddedFromParcel(parcel, _hidl_blob, _hidl_offset + 64);
        this.rxIcmpPkWakeDetails.readEmbeddedFromParcel(parcel, _hidl_blob, _hidl_offset + 76);
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
            _hidl_vec.get(_hidl_index_0).writeEmbeddedToBlob(childBlob, (long) (_hidl_index_0 * 96));
        }
        _hidl_blob.putBlob(0, childBlob);
        parcel.writeBuffer(_hidl_blob);
    }

    public final void writeEmbeddedToBlob(HwBlob _hidl_blob, long _hidl_offset) {
        _hidl_blob.putInt32(_hidl_offset + 0, this.totalCmdEventWakeCnt);
        int _hidl_vec_size = this.cmdEventWakeCntPerType.size();
        _hidl_blob.putInt32(_hidl_offset + 8 + 8, _hidl_vec_size);
        _hidl_blob.putBool(_hidl_offset + 8 + 12, false);
        HwBlob childBlob = new HwBlob(_hidl_vec_size * 4);
        for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            childBlob.putInt32((long) (_hidl_index_0 * 4), this.cmdEventWakeCntPerType.get(_hidl_index_0).intValue());
        }
        _hidl_blob.putBlob(_hidl_offset + 8 + 0, childBlob);
        _hidl_blob.putInt32(_hidl_offset + 24, this.totalDriverFwLocalWakeCnt);
        int _hidl_vec_size2 = this.driverFwLocalWakeCntPerType.size();
        _hidl_blob.putInt32(_hidl_offset + 32 + 8, _hidl_vec_size2);
        _hidl_blob.putBool(_hidl_offset + 32 + 12, false);
        HwBlob childBlob2 = new HwBlob(_hidl_vec_size2 * 4);
        for (int _hidl_index_02 = 0; _hidl_index_02 < _hidl_vec_size2; _hidl_index_02++) {
            childBlob2.putInt32((long) (_hidl_index_02 * 4), this.driverFwLocalWakeCntPerType.get(_hidl_index_02).intValue());
        }
        _hidl_blob.putBlob(_hidl_offset + 32 + 0, childBlob2);
        _hidl_blob.putInt32(_hidl_offset + 48, this.totalRxPacketWakeCnt);
        this.rxPktWakeDetails.writeEmbeddedToBlob(_hidl_blob, _hidl_offset + 52);
        this.rxMulticastPkWakeDetails.writeEmbeddedToBlob(_hidl_blob, _hidl_offset + 64);
        this.rxIcmpPkWakeDetails.writeEmbeddedToBlob(_hidl_blob, _hidl_offset + 76);
    }
}
