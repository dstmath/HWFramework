package vendor.huawei.hardware.eid.V1_0;

import android.os.HidlSupport;
import android.os.HwBlob;
import android.os.HwParcel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;

public final class SEC_IMAGE_S {
    public byte[] deSkey = new byte[2048];
    public int deSkeyLen;
    public byte[] image = new byte[163840];
    public int len;

    public final boolean equals(Object otherObject) {
        if (this == otherObject) {
            return true;
        }
        if (otherObject == null || otherObject.getClass() != SEC_IMAGE_S.class) {
            return false;
        }
        SEC_IMAGE_S other = (SEC_IMAGE_S) otherObject;
        if (this.len == other.len && this.deSkeyLen == other.deSkeyLen && HidlSupport.deepEquals(this.image, other.image) && HidlSupport.deepEquals(this.deSkey, other.deSkey)) {
            return true;
        }
        return false;
    }

    public final int hashCode() {
        return Objects.hash(Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.len))), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.deSkeyLen))), Integer.valueOf(HidlSupport.deepHashCode(this.image)), Integer.valueOf(HidlSupport.deepHashCode(this.deSkey)));
    }

    public final String toString() {
        return "{.len = " + this.len + ", .deSkeyLen = " + this.deSkeyLen + ", .image = " + Arrays.toString(this.image) + ", .deSkey = " + Arrays.toString(this.deSkey) + "}";
    }

    public final void readFromParcel(HwParcel parcel) {
        readEmbeddedFromParcel(parcel, parcel.readBuffer(165896), 0);
    }

    public static final ArrayList<SEC_IMAGE_S> readVectorFromParcel(HwParcel parcel) {
        ArrayList<SEC_IMAGE_S> _hidl_vec = new ArrayList<>();
        HwBlob _hidl_blob = parcel.readBuffer(16);
        int _hidl_vec_size = _hidl_blob.getInt32(8);
        HwBlob childBlob = parcel.readEmbeddedBuffer((long) (_hidl_vec_size * 165896), _hidl_blob.handle(), 0, true);
        _hidl_vec.clear();
        for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            SEC_IMAGE_S _hidl_vec_element = new SEC_IMAGE_S();
            _hidl_vec_element.readEmbeddedFromParcel(parcel, childBlob, (long) (_hidl_index_0 * 165896));
            _hidl_vec.add(_hidl_vec_element);
        }
        return _hidl_vec;
    }

    public final void readEmbeddedFromParcel(HwParcel parcel, HwBlob _hidl_blob, long _hidl_offset) {
        this.len = _hidl_blob.getInt32(0 + _hidl_offset);
        this.deSkeyLen = _hidl_blob.getInt32(4 + _hidl_offset);
        _hidl_blob.copyToInt8Array(8 + _hidl_offset, this.image, 163840);
        _hidl_blob.copyToInt8Array(163848 + _hidl_offset, this.deSkey, 2048);
    }

    public final void writeToParcel(HwParcel parcel) {
        HwBlob _hidl_blob = new HwBlob(165896);
        writeEmbeddedToBlob(_hidl_blob, 0);
        parcel.writeBuffer(_hidl_blob);
    }

    public static final void writeVectorToParcel(HwParcel parcel, ArrayList<SEC_IMAGE_S> _hidl_vec) {
        HwBlob _hidl_blob = new HwBlob(16);
        int _hidl_vec_size = _hidl_vec.size();
        _hidl_blob.putInt32(8, _hidl_vec_size);
        _hidl_blob.putBool(12, false);
        HwBlob childBlob = new HwBlob(_hidl_vec_size * 165896);
        for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            _hidl_vec.get(_hidl_index_0).writeEmbeddedToBlob(childBlob, (long) (_hidl_index_0 * 165896));
        }
        _hidl_blob.putBlob(0, childBlob);
        parcel.writeBuffer(_hidl_blob);
    }

    public final void writeEmbeddedToBlob(HwBlob _hidl_blob, long _hidl_offset) {
        _hidl_blob.putInt32(0 + _hidl_offset, this.len);
        _hidl_blob.putInt32(4 + _hidl_offset, this.deSkeyLen);
        long _hidl_array_offset_0 = 8 + _hidl_offset;
        byte[] _hidl_array_item_0 = this.image;
        if (_hidl_array_item_0 == null || _hidl_array_item_0.length != 163840) {
            throw new IllegalArgumentException("Array element is not of the expected length");
        }
        _hidl_blob.putInt8Array(_hidl_array_offset_0, _hidl_array_item_0);
        long _hidl_array_offset_02 = 163848 + _hidl_offset;
        byte[] _hidl_array_item_02 = this.deSkey;
        if (_hidl_array_item_02 == null || _hidl_array_item_02.length != 2048) {
            throw new IllegalArgumentException("Array element is not of the expected length");
        }
        _hidl_blob.putInt8Array(_hidl_array_offset_02, _hidl_array_item_02);
    }
}
