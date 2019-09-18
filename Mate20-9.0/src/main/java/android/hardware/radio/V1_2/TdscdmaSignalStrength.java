package android.hardware.radio.V1_2;

import android.os.HidlSupport;
import android.os.HwBlob;
import android.os.HwParcel;
import java.util.ArrayList;
import java.util.Objects;

public final class TdscdmaSignalStrength {
    public int bitErrorRate;
    public int rscp;
    public int signalStrength;

    public final boolean equals(Object otherObject) {
        if (this == otherObject) {
            return true;
        }
        if (otherObject == null || otherObject.getClass() != TdscdmaSignalStrength.class) {
            return false;
        }
        TdscdmaSignalStrength other = (TdscdmaSignalStrength) otherObject;
        if (this.signalStrength == other.signalStrength && this.bitErrorRate == other.bitErrorRate && this.rscp == other.rscp) {
            return true;
        }
        return false;
    }

    public final int hashCode() {
        return Objects.hash(new Object[]{Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.signalStrength))), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.bitErrorRate))), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.rscp)))});
    }

    public final String toString() {
        return "{" + ".signalStrength = " + this.signalStrength + ", .bitErrorRate = " + this.bitErrorRate + ", .rscp = " + this.rscp + "}";
    }

    public final void readFromParcel(HwParcel parcel) {
        readEmbeddedFromParcel(parcel, parcel.readBuffer(12), 0);
    }

    public static final ArrayList<TdscdmaSignalStrength> readVectorFromParcel(HwParcel parcel) {
        ArrayList<TdscdmaSignalStrength> _hidl_vec = new ArrayList<>();
        HwBlob _hidl_blob = parcel.readBuffer(16);
        int _hidl_vec_size = _hidl_blob.getInt32(8);
        HwBlob childBlob = parcel.readEmbeddedBuffer((long) (_hidl_vec_size * 12), _hidl_blob.handle(), 0, true);
        _hidl_vec.clear();
        for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            TdscdmaSignalStrength _hidl_vec_element = new TdscdmaSignalStrength();
            _hidl_vec_element.readEmbeddedFromParcel(parcel, childBlob, (long) (_hidl_index_0 * 12));
            _hidl_vec.add(_hidl_vec_element);
        }
        return _hidl_vec;
    }

    public final void readEmbeddedFromParcel(HwParcel parcel, HwBlob _hidl_blob, long _hidl_offset) {
        this.signalStrength = _hidl_blob.getInt32(0 + _hidl_offset);
        this.bitErrorRate = _hidl_blob.getInt32(4 + _hidl_offset);
        this.rscp = _hidl_blob.getInt32(8 + _hidl_offset);
    }

    public final void writeToParcel(HwParcel parcel) {
        HwBlob _hidl_blob = new HwBlob(12);
        writeEmbeddedToBlob(_hidl_blob, 0);
        parcel.writeBuffer(_hidl_blob);
    }

    public static final void writeVectorToParcel(HwParcel parcel, ArrayList<TdscdmaSignalStrength> _hidl_vec) {
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
        _hidl_blob.putInt32(0 + _hidl_offset, this.signalStrength);
        _hidl_blob.putInt32(4 + _hidl_offset, this.bitErrorRate);
        _hidl_blob.putInt32(8 + _hidl_offset, this.rscp);
    }
}
