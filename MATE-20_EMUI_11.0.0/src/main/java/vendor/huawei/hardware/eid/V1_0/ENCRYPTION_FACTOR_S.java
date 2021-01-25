package vendor.huawei.hardware.eid.V1_0;

import android.os.HidlSupport;
import android.os.HwBlob;
import android.os.HwParcel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;

public final class ENCRYPTION_FACTOR_S {
    public byte[] certificate = new byte[8192];
    public int certificateLen;
    public int encryptionMethod;
    public int splitTimes;

    public final boolean equals(Object otherObject) {
        if (this == otherObject) {
            return true;
        }
        if (otherObject == null || otherObject.getClass() != ENCRYPTION_FACTOR_S.class) {
            return false;
        }
        ENCRYPTION_FACTOR_S other = (ENCRYPTION_FACTOR_S) otherObject;
        if (this.encryptionMethod == other.encryptionMethod && this.certificateLen == other.certificateLen && this.splitTimes == other.splitTimes && HidlSupport.deepEquals(this.certificate, other.certificate)) {
            return true;
        }
        return false;
    }

    public final int hashCode() {
        return Objects.hash(Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.encryptionMethod))), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.certificateLen))), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.splitTimes))), Integer.valueOf(HidlSupport.deepHashCode(this.certificate)));
    }

    public final String toString() {
        return "{.encryptionMethod = " + this.encryptionMethod + ", .certificateLen = " + this.certificateLen + ", .splitTimes = " + this.splitTimes + ", .certificate = " + Arrays.toString(this.certificate) + "}";
    }

    public final void readFromParcel(HwParcel parcel) {
        readEmbeddedFromParcel(parcel, parcel.readBuffer(8204), 0);
    }

    public static final ArrayList<ENCRYPTION_FACTOR_S> readVectorFromParcel(HwParcel parcel) {
        ArrayList<ENCRYPTION_FACTOR_S> _hidl_vec = new ArrayList<>();
        HwBlob _hidl_blob = parcel.readBuffer(16);
        int _hidl_vec_size = _hidl_blob.getInt32(8);
        HwBlob childBlob = parcel.readEmbeddedBuffer((long) (_hidl_vec_size * 8204), _hidl_blob.handle(), 0, true);
        _hidl_vec.clear();
        for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            ENCRYPTION_FACTOR_S _hidl_vec_element = new ENCRYPTION_FACTOR_S();
            _hidl_vec_element.readEmbeddedFromParcel(parcel, childBlob, (long) (_hidl_index_0 * 8204));
            _hidl_vec.add(_hidl_vec_element);
        }
        return _hidl_vec;
    }

    public final void readEmbeddedFromParcel(HwParcel parcel, HwBlob _hidl_blob, long _hidl_offset) {
        this.encryptionMethod = _hidl_blob.getInt32(0 + _hidl_offset);
        this.certificateLen = _hidl_blob.getInt32(4 + _hidl_offset);
        this.splitTimes = _hidl_blob.getInt32(8 + _hidl_offset);
        _hidl_blob.copyToInt8Array(12 + _hidl_offset, this.certificate, 8192);
    }

    public final void writeToParcel(HwParcel parcel) {
        HwBlob _hidl_blob = new HwBlob(8204);
        writeEmbeddedToBlob(_hidl_blob, 0);
        parcel.writeBuffer(_hidl_blob);
    }

    public static final void writeVectorToParcel(HwParcel parcel, ArrayList<ENCRYPTION_FACTOR_S> _hidl_vec) {
        HwBlob _hidl_blob = new HwBlob(16);
        int _hidl_vec_size = _hidl_vec.size();
        _hidl_blob.putInt32(8, _hidl_vec_size);
        _hidl_blob.putBool(12, false);
        HwBlob childBlob = new HwBlob(_hidl_vec_size * 8204);
        for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            _hidl_vec.get(_hidl_index_0).writeEmbeddedToBlob(childBlob, (long) (_hidl_index_0 * 8204));
        }
        _hidl_blob.putBlob(0, childBlob);
        parcel.writeBuffer(_hidl_blob);
    }

    public final void writeEmbeddedToBlob(HwBlob _hidl_blob, long _hidl_offset) {
        _hidl_blob.putInt32(0 + _hidl_offset, this.encryptionMethod);
        _hidl_blob.putInt32(4 + _hidl_offset, this.certificateLen);
        _hidl_blob.putInt32(8 + _hidl_offset, this.splitTimes);
        long _hidl_array_offset_0 = 12 + _hidl_offset;
        byte[] _hidl_array_item_0 = this.certificate;
        if (_hidl_array_item_0 == null || _hidl_array_item_0.length != 8192) {
            throw new IllegalArgumentException("Array element is not of the expected length");
        }
        _hidl_blob.putInt8Array(_hidl_array_offset_0, _hidl_array_item_0);
    }
}
