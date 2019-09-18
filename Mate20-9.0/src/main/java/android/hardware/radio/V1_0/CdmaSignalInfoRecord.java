package android.hardware.radio.V1_0;

import android.os.HidlSupport;
import android.os.HwBlob;
import android.os.HwParcel;
import java.util.ArrayList;
import java.util.Objects;

public final class CdmaSignalInfoRecord {
    public byte alertPitch;
    public boolean isPresent;
    public byte signal;
    public byte signalType;

    public final boolean equals(Object otherObject) {
        if (this == otherObject) {
            return true;
        }
        if (otherObject == null || otherObject.getClass() != CdmaSignalInfoRecord.class) {
            return false;
        }
        CdmaSignalInfoRecord other = (CdmaSignalInfoRecord) otherObject;
        if (this.isPresent == other.isPresent && this.signalType == other.signalType && this.alertPitch == other.alertPitch && this.signal == other.signal) {
            return true;
        }
        return false;
    }

    public final int hashCode() {
        return Objects.hash(new Object[]{Integer.valueOf(HidlSupport.deepHashCode(Boolean.valueOf(this.isPresent))), Integer.valueOf(HidlSupport.deepHashCode(Byte.valueOf(this.signalType))), Integer.valueOf(HidlSupport.deepHashCode(Byte.valueOf(this.alertPitch))), Integer.valueOf(HidlSupport.deepHashCode(Byte.valueOf(this.signal)))});
    }

    public final String toString() {
        return "{" + ".isPresent = " + this.isPresent + ", .signalType = " + this.signalType + ", .alertPitch = " + this.alertPitch + ", .signal = " + this.signal + "}";
    }

    public final void readFromParcel(HwParcel parcel) {
        readEmbeddedFromParcel(parcel, parcel.readBuffer(4), 0);
    }

    public static final ArrayList<CdmaSignalInfoRecord> readVectorFromParcel(HwParcel parcel) {
        ArrayList<CdmaSignalInfoRecord> _hidl_vec = new ArrayList<>();
        HwBlob _hidl_blob = parcel.readBuffer(16);
        int _hidl_vec_size = _hidl_blob.getInt32(8);
        HwBlob childBlob = parcel.readEmbeddedBuffer((long) (_hidl_vec_size * 4), _hidl_blob.handle(), 0, true);
        _hidl_vec.clear();
        for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            CdmaSignalInfoRecord _hidl_vec_element = new CdmaSignalInfoRecord();
            _hidl_vec_element.readEmbeddedFromParcel(parcel, childBlob, (long) (_hidl_index_0 * 4));
            _hidl_vec.add(_hidl_vec_element);
        }
        return _hidl_vec;
    }

    public final void readEmbeddedFromParcel(HwParcel parcel, HwBlob _hidl_blob, long _hidl_offset) {
        this.isPresent = _hidl_blob.getBool(0 + _hidl_offset);
        this.signalType = _hidl_blob.getInt8(1 + _hidl_offset);
        this.alertPitch = _hidl_blob.getInt8(2 + _hidl_offset);
        this.signal = _hidl_blob.getInt8(3 + _hidl_offset);
    }

    public final void writeToParcel(HwParcel parcel) {
        HwBlob _hidl_blob = new HwBlob(4);
        writeEmbeddedToBlob(_hidl_blob, 0);
        parcel.writeBuffer(_hidl_blob);
    }

    public static final void writeVectorToParcel(HwParcel parcel, ArrayList<CdmaSignalInfoRecord> _hidl_vec) {
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
        _hidl_blob.putBool(0 + _hidl_offset, this.isPresent);
        _hidl_blob.putInt8(1 + _hidl_offset, this.signalType);
        _hidl_blob.putInt8(2 + _hidl_offset, this.alertPitch);
        _hidl_blob.putInt8(3 + _hidl_offset, this.signal);
    }
}
