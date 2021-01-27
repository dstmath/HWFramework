package vendor.huawei.hardware.radio.V2_1;

import android.hardware.radio.V1_4.CellIdentityNr;
import android.hardware.radio.V1_4.DataRegStateResult;
import android.os.HidlSupport;
import android.os.HwBlob;
import android.os.HwParcel;
import java.util.ArrayList;
import java.util.Objects;

public final class HwDataRegStateResult_2_1 {
    public DataRegStateResult base = new DataRegStateResult();
    public CellIdentityNr cellIdentityNr = new CellIdentityNr();
    public int nsaState;

    public final boolean equals(Object otherObject) {
        if (this == otherObject) {
            return true;
        }
        if (otherObject == null || otherObject.getClass() != HwDataRegStateResult_2_1.class) {
            return false;
        }
        HwDataRegStateResult_2_1 other = (HwDataRegStateResult_2_1) otherObject;
        if (HidlSupport.deepEquals(this.base, other.base) && HidlSupport.deepEquals(this.cellIdentityNr, other.cellIdentityNr) && this.nsaState == other.nsaState) {
            return true;
        }
        return false;
    }

    public final int hashCode() {
        return Objects.hash(Integer.valueOf(HidlSupport.deepHashCode(this.base)), Integer.valueOf(HidlSupport.deepHashCode(this.cellIdentityNr)), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.nsaState))));
    }

    public final String toString() {
        return "{.base = " + this.base + ", .cellIdentityNr = " + this.cellIdentityNr + ", .nsaState = " + this.nsaState + "}";
    }

    public final void readFromParcel(HwParcel parcel) {
        readEmbeddedFromParcel(parcel, parcel.readBuffer(208), 0);
    }

    public static final ArrayList<HwDataRegStateResult_2_1> readVectorFromParcel(HwParcel parcel) {
        ArrayList<HwDataRegStateResult_2_1> _hidl_vec = new ArrayList<>();
        HwBlob _hidl_blob = parcel.readBuffer(16);
        int _hidl_vec_size = _hidl_blob.getInt32(8);
        HwBlob childBlob = parcel.readEmbeddedBuffer((long) (_hidl_vec_size * 208), _hidl_blob.handle(), 0, true);
        _hidl_vec.clear();
        for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            HwDataRegStateResult_2_1 _hidl_vec_element = new HwDataRegStateResult_2_1();
            _hidl_vec_element.readEmbeddedFromParcel(parcel, childBlob, (long) (_hidl_index_0 * 208));
            _hidl_vec.add(_hidl_vec_element);
        }
        return _hidl_vec;
    }

    public final void readEmbeddedFromParcel(HwParcel parcel, HwBlob _hidl_blob, long _hidl_offset) {
        this.base.readEmbeddedFromParcel(parcel, _hidl_blob, 0 + _hidl_offset);
        this.cellIdentityNr.readEmbeddedFromParcel(parcel, _hidl_blob, 112 + _hidl_offset);
        this.nsaState = _hidl_blob.getInt32(200 + _hidl_offset);
    }

    public final void writeToParcel(HwParcel parcel) {
        HwBlob _hidl_blob = new HwBlob(208);
        writeEmbeddedToBlob(_hidl_blob, 0);
        parcel.writeBuffer(_hidl_blob);
    }

    public static final void writeVectorToParcel(HwParcel parcel, ArrayList<HwDataRegStateResult_2_1> _hidl_vec) {
        HwBlob _hidl_blob = new HwBlob(16);
        int _hidl_vec_size = _hidl_vec.size();
        _hidl_blob.putInt32(8, _hidl_vec_size);
        _hidl_blob.putBool(12, false);
        HwBlob childBlob = new HwBlob(_hidl_vec_size * 208);
        for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            _hidl_vec.get(_hidl_index_0).writeEmbeddedToBlob(childBlob, (long) (_hidl_index_0 * 208));
        }
        _hidl_blob.putBlob(0, childBlob);
        parcel.writeBuffer(_hidl_blob);
    }

    public final void writeEmbeddedToBlob(HwBlob _hidl_blob, long _hidl_offset) {
        this.base.writeEmbeddedToBlob(_hidl_blob, 0 + _hidl_offset);
        this.cellIdentityNr.writeEmbeddedToBlob(_hidl_blob, 112 + _hidl_offset);
        _hidl_blob.putInt32(200 + _hidl_offset, this.nsaState);
    }
}
