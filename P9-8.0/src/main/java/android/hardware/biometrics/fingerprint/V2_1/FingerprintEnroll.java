package android.hardware.biometrics.fingerprint.V2_1;

import android.os.HidlSupport;
import android.os.HwBlob;
import android.os.HwParcel;
import java.util.ArrayList;
import java.util.Objects;

public final class FingerprintEnroll {
    public final FingerprintFingerId finger = new FingerprintFingerId();
    public long msg;
    public int samplesRemaining;

    public final boolean equals(Object otherObject) {
        if (this == otherObject) {
            return true;
        }
        if (otherObject == null || otherObject.getClass() != FingerprintEnroll.class) {
            return false;
        }
        FingerprintEnroll other = (FingerprintEnroll) otherObject;
        return HidlSupport.deepEquals(this.finger, other.finger) && this.samplesRemaining == other.samplesRemaining && this.msg == other.msg;
    }

    public final int hashCode() {
        return Objects.hash(new Object[]{Integer.valueOf(HidlSupport.deepHashCode(this.finger)), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.samplesRemaining))), Integer.valueOf(HidlSupport.deepHashCode(Long.valueOf(this.msg)))});
    }

    public final String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("{");
        builder.append(".finger = ");
        builder.append(this.finger);
        builder.append(", .samplesRemaining = ");
        builder.append(this.samplesRemaining);
        builder.append(", .msg = ");
        builder.append(this.msg);
        builder.append("}");
        return builder.toString();
    }

    public final void readFromParcel(HwParcel parcel) {
        readEmbeddedFromParcel(parcel, parcel.readBuffer(24), 0);
    }

    public static final ArrayList<FingerprintEnroll> readVectorFromParcel(HwParcel parcel) {
        ArrayList<FingerprintEnroll> _hidl_vec = new ArrayList();
        HwBlob _hidl_blob = parcel.readBuffer(16);
        int _hidl_vec_size = _hidl_blob.getInt32(8);
        HwBlob childBlob = parcel.readEmbeddedBuffer((long) (_hidl_vec_size * 24), _hidl_blob.handle(), 0, true);
        _hidl_vec.clear();
        for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            FingerprintEnroll _hidl_vec_element = new FingerprintEnroll();
            _hidl_vec_element.readEmbeddedFromParcel(parcel, childBlob, (long) (_hidl_index_0 * 24));
            _hidl_vec.add(_hidl_vec_element);
        }
        return _hidl_vec;
    }

    public final void readEmbeddedFromParcel(HwParcel parcel, HwBlob _hidl_blob, long _hidl_offset) {
        this.finger.readEmbeddedFromParcel(parcel, _hidl_blob, 0 + _hidl_offset);
        this.samplesRemaining = _hidl_blob.getInt32(8 + _hidl_offset);
        this.msg = _hidl_blob.getInt64(16 + _hidl_offset);
    }

    public final void writeToParcel(HwParcel parcel) {
        HwBlob _hidl_blob = new HwBlob(24);
        writeEmbeddedToBlob(_hidl_blob, 0);
        parcel.writeBuffer(_hidl_blob);
    }

    public static final void writeVectorToParcel(HwParcel parcel, ArrayList<FingerprintEnroll> _hidl_vec) {
        HwBlob _hidl_blob = new HwBlob(16);
        int _hidl_vec_size = _hidl_vec.size();
        _hidl_blob.putInt32(8, _hidl_vec_size);
        _hidl_blob.putBool(12, false);
        HwBlob childBlob = new HwBlob(_hidl_vec_size * 24);
        for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            ((FingerprintEnroll) _hidl_vec.get(_hidl_index_0)).writeEmbeddedToBlob(childBlob, (long) (_hidl_index_0 * 24));
        }
        _hidl_blob.putBlob(0, childBlob);
        parcel.writeBuffer(_hidl_blob);
    }

    public final void writeEmbeddedToBlob(HwBlob _hidl_blob, long _hidl_offset) {
        this.finger.writeEmbeddedToBlob(_hidl_blob, 0 + _hidl_offset);
        _hidl_blob.putInt32(8 + _hidl_offset, this.samplesRemaining);
        _hidl_blob.putInt64(16 + _hidl_offset, this.msg);
    }
}
