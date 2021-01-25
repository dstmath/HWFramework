package vendor.huawei.hardware.fusd.V1_2;

import android.os.HidlSupport;
import android.os.HwBlob;
import android.os.HwParcel;
import java.util.ArrayList;
import java.util.Objects;

public final class CurrentCell {
    public int bootTimeHigh;
    public int bootTimeLow;
    public int cellId;
    public int channelNum;
    public int lac;
    public short mcc;
    public short mnc;
    public short rat;
    public short rssi;

    public final boolean equals(Object otherObject) {
        if (this == otherObject) {
            return true;
        }
        if (otherObject == null || otherObject.getClass() != CurrentCell.class) {
            return false;
        }
        CurrentCell other = (CurrentCell) otherObject;
        if (this.cellId == other.cellId && this.lac == other.lac && this.mcc == other.mcc && this.mnc == other.mnc && this.rssi == other.rssi && this.rat == other.rat && this.channelNum == other.channelNum && this.bootTimeLow == other.bootTimeLow && this.bootTimeHigh == other.bootTimeHigh) {
            return true;
        }
        return false;
    }

    public final int hashCode() {
        return Objects.hash(Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.cellId))), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.lac))), Integer.valueOf(HidlSupport.deepHashCode(Short.valueOf(this.mcc))), Integer.valueOf(HidlSupport.deepHashCode(Short.valueOf(this.mnc))), Integer.valueOf(HidlSupport.deepHashCode(Short.valueOf(this.rssi))), Integer.valueOf(HidlSupport.deepHashCode(Short.valueOf(this.rat))), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.channelNum))), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.bootTimeLow))), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.bootTimeHigh))));
    }

    public final String toString() {
        return "{.cellId = " + this.cellId + ", .lac = " + this.lac + ", .mcc = " + ((int) this.mcc) + ", .mnc = " + ((int) this.mnc) + ", .rssi = " + ((int) this.rssi) + ", .rat = " + ((int) this.rat) + ", .channelNum = " + this.channelNum + ", .bootTimeLow = " + this.bootTimeLow + ", .bootTimeHigh = " + this.bootTimeHigh + "}";
    }

    public final void readFromParcel(HwParcel parcel) {
        readEmbeddedFromParcel(parcel, parcel.readBuffer(28), 0);
    }

    public static final ArrayList<CurrentCell> readVectorFromParcel(HwParcel parcel) {
        ArrayList<CurrentCell> _hidl_vec = new ArrayList<>();
        HwBlob _hidl_blob = parcel.readBuffer(16);
        int _hidl_vec_size = _hidl_blob.getInt32(8);
        HwBlob childBlob = parcel.readEmbeddedBuffer((long) (_hidl_vec_size * 28), _hidl_blob.handle(), 0, true);
        _hidl_vec.clear();
        for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            CurrentCell _hidl_vec_element = new CurrentCell();
            _hidl_vec_element.readEmbeddedFromParcel(parcel, childBlob, (long) (_hidl_index_0 * 28));
            _hidl_vec.add(_hidl_vec_element);
        }
        return _hidl_vec;
    }

    public final void readEmbeddedFromParcel(HwParcel parcel, HwBlob _hidl_blob, long _hidl_offset) {
        this.cellId = _hidl_blob.getInt32(0 + _hidl_offset);
        this.lac = _hidl_blob.getInt32(4 + _hidl_offset);
        this.mcc = _hidl_blob.getInt16(8 + _hidl_offset);
        this.mnc = _hidl_blob.getInt16(10 + _hidl_offset);
        this.rssi = _hidl_blob.getInt16(12 + _hidl_offset);
        this.rat = _hidl_blob.getInt16(14 + _hidl_offset);
        this.channelNum = _hidl_blob.getInt32(16 + _hidl_offset);
        this.bootTimeLow = _hidl_blob.getInt32(20 + _hidl_offset);
        this.bootTimeHigh = _hidl_blob.getInt32(24 + _hidl_offset);
    }

    public final void writeToParcel(HwParcel parcel) {
        HwBlob _hidl_blob = new HwBlob(28);
        writeEmbeddedToBlob(_hidl_blob, 0);
        parcel.writeBuffer(_hidl_blob);
    }

    public static final void writeVectorToParcel(HwParcel parcel, ArrayList<CurrentCell> _hidl_vec) {
        HwBlob _hidl_blob = new HwBlob(16);
        int _hidl_vec_size = _hidl_vec.size();
        _hidl_blob.putInt32(8, _hidl_vec_size);
        _hidl_blob.putBool(12, false);
        HwBlob childBlob = new HwBlob(_hidl_vec_size * 28);
        for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            _hidl_vec.get(_hidl_index_0).writeEmbeddedToBlob(childBlob, (long) (_hidl_index_0 * 28));
        }
        _hidl_blob.putBlob(0, childBlob);
        parcel.writeBuffer(_hidl_blob);
    }

    public final void writeEmbeddedToBlob(HwBlob _hidl_blob, long _hidl_offset) {
        _hidl_blob.putInt32(0 + _hidl_offset, this.cellId);
        _hidl_blob.putInt32(4 + _hidl_offset, this.lac);
        _hidl_blob.putInt16(8 + _hidl_offset, this.mcc);
        _hidl_blob.putInt16(10 + _hidl_offset, this.mnc);
        _hidl_blob.putInt16(12 + _hidl_offset, this.rssi);
        _hidl_blob.putInt16(14 + _hidl_offset, this.rat);
        _hidl_blob.putInt32(16 + _hidl_offset, this.channelNum);
        _hidl_blob.putInt32(20 + _hidl_offset, this.bootTimeLow);
        _hidl_blob.putInt32(24 + _hidl_offset, this.bootTimeHigh);
    }
}
