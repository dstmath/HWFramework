package vendor.huawei.hardware.eid.V1_1;

import android.os.HidlSupport;
import android.os.HwBlob;
import android.os.HwParcel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;

public final class IMAGE_ZIP_CONTAINER_S {
    public byte[] hash = new byte[32];
    public int hash_len;
    public byte[] image = new byte[131072];
    public int image_len;

    public final boolean equals(Object otherObject) {
        if (this == otherObject) {
            return true;
        }
        if (otherObject == null || otherObject.getClass() != IMAGE_ZIP_CONTAINER_S.class) {
            return false;
        }
        IMAGE_ZIP_CONTAINER_S other = (IMAGE_ZIP_CONTAINER_S) otherObject;
        if (this.hash_len == other.hash_len && this.image_len == other.image_len && HidlSupport.deepEquals(this.hash, other.hash) && HidlSupport.deepEquals(this.image, other.image)) {
            return true;
        }
        return false;
    }

    public final int hashCode() {
        return Objects.hash(Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.hash_len))), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.image_len))), Integer.valueOf(HidlSupport.deepHashCode(this.hash)), Integer.valueOf(HidlSupport.deepHashCode(this.image)));
    }

    public final String toString() {
        return "{.hash_len = " + this.hash_len + ", .image_len = " + this.image_len + ", .hash = " + Arrays.toString(this.hash) + ", .image = " + Arrays.toString(this.image) + "}";
    }

    public final void readFromParcel(HwParcel parcel) {
        readEmbeddedFromParcel(parcel, parcel.readBuffer(131112), 0);
    }

    public static final ArrayList<IMAGE_ZIP_CONTAINER_S> readVectorFromParcel(HwParcel parcel) {
        ArrayList<IMAGE_ZIP_CONTAINER_S> _hidl_vec = new ArrayList<>();
        HwBlob _hidl_blob = parcel.readBuffer(16);
        int _hidl_vec_size = _hidl_blob.getInt32(8);
        HwBlob childBlob = parcel.readEmbeddedBuffer((long) (_hidl_vec_size * 131112), _hidl_blob.handle(), 0, true);
        _hidl_vec.clear();
        for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            IMAGE_ZIP_CONTAINER_S _hidl_vec_element = new IMAGE_ZIP_CONTAINER_S();
            _hidl_vec_element.readEmbeddedFromParcel(parcel, childBlob, (long) (_hidl_index_0 * 131112));
            _hidl_vec.add(_hidl_vec_element);
        }
        return _hidl_vec;
    }

    public final void readEmbeddedFromParcel(HwParcel parcel, HwBlob _hidl_blob, long _hidl_offset) {
        this.hash_len = _hidl_blob.getInt32(0 + _hidl_offset);
        this.image_len = _hidl_blob.getInt32(4 + _hidl_offset);
        _hidl_blob.copyToInt8Array(8 + _hidl_offset, this.hash, 32);
        _hidl_blob.copyToInt8Array(40 + _hidl_offset, this.image, 131072);
    }

    public final void writeToParcel(HwParcel parcel) {
        HwBlob _hidl_blob = new HwBlob(131112);
        writeEmbeddedToBlob(_hidl_blob, 0);
        parcel.writeBuffer(_hidl_blob);
    }

    public static final void writeVectorToParcel(HwParcel parcel, ArrayList<IMAGE_ZIP_CONTAINER_S> _hidl_vec) {
        HwBlob _hidl_blob = new HwBlob(16);
        int _hidl_vec_size = _hidl_vec.size();
        _hidl_blob.putInt32(8, _hidl_vec_size);
        _hidl_blob.putBool(12, false);
        HwBlob childBlob = new HwBlob(_hidl_vec_size * 131112);
        for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            _hidl_vec.get(_hidl_index_0).writeEmbeddedToBlob(childBlob, (long) (_hidl_index_0 * 131112));
        }
        _hidl_blob.putBlob(0, childBlob);
        parcel.writeBuffer(_hidl_blob);
    }

    public final void writeEmbeddedToBlob(HwBlob _hidl_blob, long _hidl_offset) {
        _hidl_blob.putInt32(0 + _hidl_offset, this.hash_len);
        _hidl_blob.putInt32(4 + _hidl_offset, this.image_len);
        long _hidl_array_offset_0 = 8 + _hidl_offset;
        byte[] _hidl_array_item_0 = this.hash;
        if (_hidl_array_item_0 == null || _hidl_array_item_0.length != 32) {
            throw new IllegalArgumentException("Array element is not of the expected length");
        }
        _hidl_blob.putInt8Array(_hidl_array_offset_0, _hidl_array_item_0);
        long _hidl_array_offset_02 = 40 + _hidl_offset;
        byte[] _hidl_array_item_02 = this.image;
        if (_hidl_array_item_02 == null || _hidl_array_item_02.length != 131072) {
            throw new IllegalArgumentException("Array element is not of the expected length");
        }
        _hidl_blob.putInt8Array(_hidl_array_offset_02, _hidl_array_item_02);
    }
}
