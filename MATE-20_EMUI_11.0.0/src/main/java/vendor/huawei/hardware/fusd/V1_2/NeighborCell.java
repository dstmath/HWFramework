package vendor.huawei.hardware.fusd.V1_2;

import android.os.HidlSupport;
import android.os.HwBlob;
import android.os.HwParcel;
import java.util.ArrayList;
import java.util.Objects;

public final class NeighborCell {
    public int channelNum;
    public short mcc;
    public short mnc;
    public int physicalId;
    public short rat;
    public short rssi;

    public final boolean equals(Object otherObject) {
        if (this == otherObject) {
            return true;
        }
        if (otherObject == null || otherObject.getClass() != NeighborCell.class) {
            return false;
        }
        NeighborCell other = (NeighborCell) otherObject;
        if (this.physicalId == other.physicalId && this.mcc == other.mcc && this.mnc == other.mnc && this.rssi == other.rssi && this.rat == other.rat && this.channelNum == other.channelNum) {
            return true;
        }
        return false;
    }

    public final int hashCode() {
        return Objects.hash(Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.physicalId))), Integer.valueOf(HidlSupport.deepHashCode(Short.valueOf(this.mcc))), Integer.valueOf(HidlSupport.deepHashCode(Short.valueOf(this.mnc))), Integer.valueOf(HidlSupport.deepHashCode(Short.valueOf(this.rssi))), Integer.valueOf(HidlSupport.deepHashCode(Short.valueOf(this.rat))), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.channelNum))));
    }

    public final String toString() {
        return "{.physicalId = " + this.physicalId + ", .mcc = " + ((int) this.mcc) + ", .mnc = " + ((int) this.mnc) + ", .rssi = " + ((int) this.rssi) + ", .rat = " + ((int) this.rat) + ", .channelNum = " + this.channelNum + "}";
    }

    public final void readFromParcel(HwParcel parcel) {
        readEmbeddedFromParcel(parcel, parcel.readBuffer(16), 0);
    }

    public static final ArrayList<NeighborCell> readVectorFromParcel(HwParcel parcel) {
        ArrayList<NeighborCell> _hidl_vec = new ArrayList<>();
        HwBlob _hidl_blob = parcel.readBuffer(16);
        int _hidl_vec_size = _hidl_blob.getInt32(8);
        HwBlob childBlob = parcel.readEmbeddedBuffer((long) (_hidl_vec_size * 16), _hidl_blob.handle(), 0, true);
        _hidl_vec.clear();
        for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            NeighborCell _hidl_vec_element = new NeighborCell();
            _hidl_vec_element.readEmbeddedFromParcel(parcel, childBlob, (long) (_hidl_index_0 * 16));
            _hidl_vec.add(_hidl_vec_element);
        }
        return _hidl_vec;
    }

    public final void readEmbeddedFromParcel(HwParcel parcel, HwBlob _hidl_blob, long _hidl_offset) {
        this.physicalId = _hidl_blob.getInt32(0 + _hidl_offset);
        this.mcc = _hidl_blob.getInt16(4 + _hidl_offset);
        this.mnc = _hidl_blob.getInt16(6 + _hidl_offset);
        this.rssi = _hidl_blob.getInt16(8 + _hidl_offset);
        this.rat = _hidl_blob.getInt16(10 + _hidl_offset);
        this.channelNum = _hidl_blob.getInt32(12 + _hidl_offset);
    }

    public final void writeToParcel(HwParcel parcel) {
        HwBlob _hidl_blob = new HwBlob(16);
        writeEmbeddedToBlob(_hidl_blob, 0);
        parcel.writeBuffer(_hidl_blob);
    }

    public static final void writeVectorToParcel(HwParcel parcel, ArrayList<NeighborCell> _hidl_vec) {
        HwBlob _hidl_blob = new HwBlob(16);
        int _hidl_vec_size = _hidl_vec.size();
        _hidl_blob.putInt32(8, _hidl_vec_size);
        _hidl_blob.putBool(12, false);
        HwBlob childBlob = new HwBlob(_hidl_vec_size * 16);
        for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            _hidl_vec.get(_hidl_index_0).writeEmbeddedToBlob(childBlob, (long) (_hidl_index_0 * 16));
        }
        _hidl_blob.putBlob(0, childBlob);
        parcel.writeBuffer(_hidl_blob);
    }

    public final void writeEmbeddedToBlob(HwBlob _hidl_blob, long _hidl_offset) {
        _hidl_blob.putInt32(0 + _hidl_offset, this.physicalId);
        _hidl_blob.putInt16(4 + _hidl_offset, this.mcc);
        _hidl_blob.putInt16(6 + _hidl_offset, this.mnc);
        _hidl_blob.putInt16(8 + _hidl_offset, this.rssi);
        _hidl_blob.putInt16(10 + _hidl_offset, this.rat);
        _hidl_blob.putInt32(12 + _hidl_offset, this.channelNum);
    }
}
