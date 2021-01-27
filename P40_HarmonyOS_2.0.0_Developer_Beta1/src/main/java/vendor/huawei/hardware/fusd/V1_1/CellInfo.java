package vendor.huawei.hardware.fusd.V1_1;

import android.os.HidlSupport;
import android.os.HwBlob;
import android.os.HwParcel;
import java.util.ArrayList;
import java.util.Objects;

public final class CellInfo {
    public int cid;
    public short lac;
    public short mcc;
    public short mnc;
    public short rssi;

    public final boolean equals(Object otherObject) {
        if (this == otherObject) {
            return true;
        }
        if (otherObject == null || otherObject.getClass() != CellInfo.class) {
            return false;
        }
        CellInfo other = (CellInfo) otherObject;
        if (this.cid == other.cid && this.lac == other.lac && this.rssi == other.rssi && this.mcc == other.mcc && this.mnc == other.mnc) {
            return true;
        }
        return false;
    }

    public final int hashCode() {
        return Objects.hash(Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.cid))), Integer.valueOf(HidlSupport.deepHashCode(Short.valueOf(this.lac))), Integer.valueOf(HidlSupport.deepHashCode(Short.valueOf(this.rssi))), Integer.valueOf(HidlSupport.deepHashCode(Short.valueOf(this.mcc))), Integer.valueOf(HidlSupport.deepHashCode(Short.valueOf(this.mnc))));
    }

    public final String toString() {
        return "{.cid = " + this.cid + ", .lac = " + ((int) this.lac) + ", .rssi = " + ((int) this.rssi) + ", .mcc = " + ((int) this.mcc) + ", .mnc = " + ((int) this.mnc) + "}";
    }

    public final void readFromParcel(HwParcel parcel) {
        readEmbeddedFromParcel(parcel, parcel.readBuffer(12), 0);
    }

    public static final ArrayList<CellInfo> readVectorFromParcel(HwParcel parcel) {
        ArrayList<CellInfo> _hidl_vec = new ArrayList<>();
        HwBlob _hidl_blob = parcel.readBuffer(16);
        int _hidl_vec_size = _hidl_blob.getInt32(8);
        HwBlob childBlob = parcel.readEmbeddedBuffer((long) (_hidl_vec_size * 12), _hidl_blob.handle(), 0, true);
        _hidl_vec.clear();
        for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            CellInfo _hidl_vec_element = new CellInfo();
            _hidl_vec_element.readEmbeddedFromParcel(parcel, childBlob, (long) (_hidl_index_0 * 12));
            _hidl_vec.add(_hidl_vec_element);
        }
        return _hidl_vec;
    }

    public final void readEmbeddedFromParcel(HwParcel parcel, HwBlob _hidl_blob, long _hidl_offset) {
        this.cid = _hidl_blob.getInt32(0 + _hidl_offset);
        this.lac = _hidl_blob.getInt16(4 + _hidl_offset);
        this.rssi = _hidl_blob.getInt16(6 + _hidl_offset);
        this.mcc = _hidl_blob.getInt16(8 + _hidl_offset);
        this.mnc = _hidl_blob.getInt16(10 + _hidl_offset);
    }

    public final void writeToParcel(HwParcel parcel) {
        HwBlob _hidl_blob = new HwBlob(12);
        writeEmbeddedToBlob(_hidl_blob, 0);
        parcel.writeBuffer(_hidl_blob);
    }

    public static final void writeVectorToParcel(HwParcel parcel, ArrayList<CellInfo> _hidl_vec) {
        HwBlob _hidl_blob = new HwBlob(16);
        int _hidl_vec_size = _hidl_vec.size();
        _hidl_blob.putInt32(8, _hidl_vec_size);
        _hidl_blob.putBool(12, false);
        HwBlob childBlob = new HwBlob(_hidl_vec_size * 12);
        for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            _hidl_vec.get(_hidl_index_0).writeEmbeddedToBlob(childBlob, (long) (_hidl_index_0 * 12));
        }
        _hidl_blob.putBlob(0, childBlob);
        parcel.writeBuffer(_hidl_blob);
    }

    public final void writeEmbeddedToBlob(HwBlob _hidl_blob, long _hidl_offset) {
        _hidl_blob.putInt32(0 + _hidl_offset, this.cid);
        _hidl_blob.putInt16(4 + _hidl_offset, this.lac);
        _hidl_blob.putInt16(6 + _hidl_offset, this.rssi);
        _hidl_blob.putInt16(8 + _hidl_offset, this.mcc);
        _hidl_blob.putInt16(10 + _hidl_offset, this.mnc);
    }
}
