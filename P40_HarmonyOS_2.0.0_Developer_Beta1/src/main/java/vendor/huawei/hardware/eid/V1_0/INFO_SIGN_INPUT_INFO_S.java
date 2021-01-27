package vendor.huawei.hardware.eid.V1_0;

import android.os.HidlSupport;
import android.os.HwBlob;
import android.os.HwParcel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;

public final class INFO_SIGN_INPUT_INFO_S {
    public int encryptionMethod;
    public byte[] info = new byte[2048];
    public int infoLen;
    public int splitTimes;

    public final boolean equals(Object otherObject) {
        if (this == otherObject) {
            return true;
        }
        if (otherObject == null || otherObject.getClass() != INFO_SIGN_INPUT_INFO_S.class) {
            return false;
        }
        INFO_SIGN_INPUT_INFO_S other = (INFO_SIGN_INPUT_INFO_S) otherObject;
        if (this.encryptionMethod == other.encryptionMethod && this.infoLen == other.infoLen && this.splitTimes == other.splitTimes && HidlSupport.deepEquals(this.info, other.info)) {
            return true;
        }
        return false;
    }

    public final int hashCode() {
        return Objects.hash(Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.encryptionMethod))), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.infoLen))), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.splitTimes))), Integer.valueOf(HidlSupport.deepHashCode(this.info)));
    }

    public final String toString() {
        return "{.encryptionMethod = " + this.encryptionMethod + ", .infoLen = " + this.infoLen + ", .splitTimes = " + this.splitTimes + ", .info = " + Arrays.toString(this.info) + "}";
    }

    public final void readFromParcel(HwParcel parcel) {
        readEmbeddedFromParcel(parcel, parcel.readBuffer(2060), 0);
    }

    public static final ArrayList<INFO_SIGN_INPUT_INFO_S> readVectorFromParcel(HwParcel parcel) {
        ArrayList<INFO_SIGN_INPUT_INFO_S> _hidl_vec = new ArrayList<>();
        HwBlob _hidl_blob = parcel.readBuffer(16);
        int _hidl_vec_size = _hidl_blob.getInt32(8);
        HwBlob childBlob = parcel.readEmbeddedBuffer((long) (_hidl_vec_size * 2060), _hidl_blob.handle(), 0, true);
        _hidl_vec.clear();
        for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            INFO_SIGN_INPUT_INFO_S _hidl_vec_element = new INFO_SIGN_INPUT_INFO_S();
            _hidl_vec_element.readEmbeddedFromParcel(parcel, childBlob, (long) (_hidl_index_0 * 2060));
            _hidl_vec.add(_hidl_vec_element);
        }
        return _hidl_vec;
    }

    public final void readEmbeddedFromParcel(HwParcel parcel, HwBlob _hidl_blob, long _hidl_offset) {
        this.encryptionMethod = _hidl_blob.getInt32(0 + _hidl_offset);
        this.infoLen = _hidl_blob.getInt32(4 + _hidl_offset);
        this.splitTimes = _hidl_blob.getInt32(8 + _hidl_offset);
        _hidl_blob.copyToInt8Array(12 + _hidl_offset, this.info, 2048);
    }

    public final void writeToParcel(HwParcel parcel) {
        HwBlob _hidl_blob = new HwBlob(2060);
        writeEmbeddedToBlob(_hidl_blob, 0);
        parcel.writeBuffer(_hidl_blob);
    }

    public static final void writeVectorToParcel(HwParcel parcel, ArrayList<INFO_SIGN_INPUT_INFO_S> _hidl_vec) {
        HwBlob _hidl_blob = new HwBlob(16);
        int _hidl_vec_size = _hidl_vec.size();
        _hidl_blob.putInt32(8, _hidl_vec_size);
        _hidl_blob.putBool(12, false);
        HwBlob childBlob = new HwBlob(_hidl_vec_size * 2060);
        for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            _hidl_vec.get(_hidl_index_0).writeEmbeddedToBlob(childBlob, (long) (_hidl_index_0 * 2060));
        }
        _hidl_blob.putBlob(0, childBlob);
        parcel.writeBuffer(_hidl_blob);
    }

    public final void writeEmbeddedToBlob(HwBlob _hidl_blob, long _hidl_offset) {
        _hidl_blob.putInt32(0 + _hidl_offset, this.encryptionMethod);
        _hidl_blob.putInt32(4 + _hidl_offset, this.infoLen);
        _hidl_blob.putInt32(8 + _hidl_offset, this.splitTimes);
        long _hidl_array_offset_0 = 12 + _hidl_offset;
        byte[] _hidl_array_item_0 = this.info;
        if (_hidl_array_item_0 == null || _hidl_array_item_0.length != 2048) {
            throw new IllegalArgumentException("Array element is not of the expected length");
        }
        _hidl_blob.putInt8Array(_hidl_array_offset_0, _hidl_array_item_0);
    }
}
