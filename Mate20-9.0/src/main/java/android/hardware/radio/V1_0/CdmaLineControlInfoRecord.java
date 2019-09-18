package android.hardware.radio.V1_0;

import android.os.HidlSupport;
import android.os.HwBlob;
import android.os.HwParcel;
import java.util.ArrayList;
import java.util.Objects;

public final class CdmaLineControlInfoRecord {
    public byte lineCtrlPolarityIncluded;
    public byte lineCtrlPowerDenial;
    public byte lineCtrlReverse;
    public byte lineCtrlToggle;

    public final boolean equals(Object otherObject) {
        if (this == otherObject) {
            return true;
        }
        if (otherObject == null || otherObject.getClass() != CdmaLineControlInfoRecord.class) {
            return false;
        }
        CdmaLineControlInfoRecord other = (CdmaLineControlInfoRecord) otherObject;
        if (this.lineCtrlPolarityIncluded == other.lineCtrlPolarityIncluded && this.lineCtrlToggle == other.lineCtrlToggle && this.lineCtrlReverse == other.lineCtrlReverse && this.lineCtrlPowerDenial == other.lineCtrlPowerDenial) {
            return true;
        }
        return false;
    }

    public final int hashCode() {
        return Objects.hash(new Object[]{Integer.valueOf(HidlSupport.deepHashCode(Byte.valueOf(this.lineCtrlPolarityIncluded))), Integer.valueOf(HidlSupport.deepHashCode(Byte.valueOf(this.lineCtrlToggle))), Integer.valueOf(HidlSupport.deepHashCode(Byte.valueOf(this.lineCtrlReverse))), Integer.valueOf(HidlSupport.deepHashCode(Byte.valueOf(this.lineCtrlPowerDenial)))});
    }

    public final String toString() {
        return "{" + ".lineCtrlPolarityIncluded = " + this.lineCtrlPolarityIncluded + ", .lineCtrlToggle = " + this.lineCtrlToggle + ", .lineCtrlReverse = " + this.lineCtrlReverse + ", .lineCtrlPowerDenial = " + this.lineCtrlPowerDenial + "}";
    }

    public final void readFromParcel(HwParcel parcel) {
        readEmbeddedFromParcel(parcel, parcel.readBuffer(4), 0);
    }

    public static final ArrayList<CdmaLineControlInfoRecord> readVectorFromParcel(HwParcel parcel) {
        ArrayList<CdmaLineControlInfoRecord> _hidl_vec = new ArrayList<>();
        HwBlob _hidl_blob = parcel.readBuffer(16);
        int _hidl_vec_size = _hidl_blob.getInt32(8);
        HwBlob childBlob = parcel.readEmbeddedBuffer((long) (_hidl_vec_size * 4), _hidl_blob.handle(), 0, true);
        _hidl_vec.clear();
        for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            CdmaLineControlInfoRecord _hidl_vec_element = new CdmaLineControlInfoRecord();
            _hidl_vec_element.readEmbeddedFromParcel(parcel, childBlob, (long) (_hidl_index_0 * 4));
            _hidl_vec.add(_hidl_vec_element);
        }
        return _hidl_vec;
    }

    public final void readEmbeddedFromParcel(HwParcel parcel, HwBlob _hidl_blob, long _hidl_offset) {
        this.lineCtrlPolarityIncluded = _hidl_blob.getInt8(0 + _hidl_offset);
        this.lineCtrlToggle = _hidl_blob.getInt8(1 + _hidl_offset);
        this.lineCtrlReverse = _hidl_blob.getInt8(2 + _hidl_offset);
        this.lineCtrlPowerDenial = _hidl_blob.getInt8(3 + _hidl_offset);
    }

    public final void writeToParcel(HwParcel parcel) {
        HwBlob _hidl_blob = new HwBlob(4);
        writeEmbeddedToBlob(_hidl_blob, 0);
        parcel.writeBuffer(_hidl_blob);
    }

    public static final void writeVectorToParcel(HwParcel parcel, ArrayList<CdmaLineControlInfoRecord> _hidl_vec) {
        HwBlob _hidl_blob = new HwBlob(16);
        int _hidl_vec_size = _hidl_vec.size();
        _hidl_blob.putInt32(8, _hidl_vec_size);
        _hidl_blob.putBool(12, false);
        HwBlob childBlob = new HwBlob(_hidl_vec_size * 4);
        for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            _hidl_vec.get(_hidl_index_0).writeEmbeddedToBlob(childBlob, (long) (_hidl_index_0 * 4));
        }
        _hidl_blob.putBlob(0, childBlob);
        parcel.writeBuffer(_hidl_blob);
    }

    public final void writeEmbeddedToBlob(HwBlob _hidl_blob, long _hidl_offset) {
        _hidl_blob.putInt8(0 + _hidl_offset, this.lineCtrlPolarityIncluded);
        _hidl_blob.putInt8(1 + _hidl_offset, this.lineCtrlToggle);
        _hidl_blob.putInt8(2 + _hidl_offset, this.lineCtrlReverse);
        _hidl_blob.putInt8(3 + _hidl_offset, this.lineCtrlPowerDenial);
    }
}
