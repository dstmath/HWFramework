package vendor.huawei.hardware.radio.V2_0;

import android.os.HidlSupport;
import android.os.HwBlob;
import android.os.HwParcel;
import java.util.ArrayList;
import java.util.Objects;

public final class IccIoResultEx {
    public int fileId;
    public boolean isValid;
    public String simResponse = new String();
    public int sw1;
    public int sw2;

    public final boolean equals(Object otherObject) {
        if (this == otherObject) {
            return true;
        }
        if (otherObject == null || otherObject.getClass() != IccIoResultEx.class) {
            return false;
        }
        IccIoResultEx other = (IccIoResultEx) otherObject;
        if (this.isValid == other.isValid && this.fileId == other.fileId && this.sw1 == other.sw1 && this.sw2 == other.sw2 && HidlSupport.deepEquals(this.simResponse, other.simResponse)) {
            return true;
        }
        return false;
    }

    public final int hashCode() {
        return Objects.hash(Integer.valueOf(HidlSupport.deepHashCode(Boolean.valueOf(this.isValid))), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.fileId))), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.sw1))), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.sw2))), Integer.valueOf(HidlSupport.deepHashCode(this.simResponse)));
    }

    public final String toString() {
        return "{" + ".isValid = " + this.isValid + ", .fileId = " + this.fileId + ", .sw1 = " + this.sw1 + ", .sw2 = " + this.sw2 + ", .simResponse = " + this.simResponse + "}";
    }

    public final void readFromParcel(HwParcel parcel) {
        readEmbeddedFromParcel(parcel, parcel.readBuffer(32), 0);
    }

    public static final ArrayList<IccIoResultEx> readVectorFromParcel(HwParcel parcel) {
        ArrayList<IccIoResultEx> _hidl_vec = new ArrayList<>();
        HwBlob _hidl_blob = parcel.readBuffer(16);
        int _hidl_vec_size = _hidl_blob.getInt32(8);
        HwBlob childBlob = parcel.readEmbeddedBuffer((long) (_hidl_vec_size * 32), _hidl_blob.handle(), 0, true);
        _hidl_vec.clear();
        for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            IccIoResultEx _hidl_vec_element = new IccIoResultEx();
            _hidl_vec_element.readEmbeddedFromParcel(parcel, childBlob, (long) (_hidl_index_0 * 32));
            _hidl_vec.add(_hidl_vec_element);
        }
        return _hidl_vec;
    }

    public final void readEmbeddedFromParcel(HwParcel parcel, HwBlob _hidl_blob, long _hidl_offset) {
        this.isValid = _hidl_blob.getBool(_hidl_offset + 0);
        this.fileId = _hidl_blob.getInt32(_hidl_offset + 4);
        this.sw1 = _hidl_blob.getInt32(_hidl_offset + 8);
        this.sw2 = _hidl_blob.getInt32(_hidl_offset + 12);
        this.simResponse = _hidl_blob.getString(_hidl_offset + 16);
        parcel.readEmbeddedBuffer((long) (this.simResponse.getBytes().length + 1), _hidl_blob.handle(), _hidl_offset + 16 + 0, false);
    }

    public final void writeToParcel(HwParcel parcel) {
        HwBlob _hidl_blob = new HwBlob(32);
        writeEmbeddedToBlob(_hidl_blob, 0);
        parcel.writeBuffer(_hidl_blob);
    }

    public static final void writeVectorToParcel(HwParcel parcel, ArrayList<IccIoResultEx> _hidl_vec) {
        HwBlob _hidl_blob = new HwBlob(16);
        int _hidl_vec_size = _hidl_vec.size();
        _hidl_blob.putInt32(8, _hidl_vec_size);
        _hidl_blob.putBool(12, false);
        HwBlob childBlob = new HwBlob(_hidl_vec_size * 32);
        for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            _hidl_vec.get(_hidl_index_0).writeEmbeddedToBlob(childBlob, (long) (_hidl_index_0 * 32));
        }
        _hidl_blob.putBlob(0, childBlob);
        parcel.writeBuffer(_hidl_blob);
    }

    public final void writeEmbeddedToBlob(HwBlob _hidl_blob, long _hidl_offset) {
        _hidl_blob.putBool(0 + _hidl_offset, this.isValid);
        _hidl_blob.putInt32(4 + _hidl_offset, this.fileId);
        _hidl_blob.putInt32(8 + _hidl_offset, this.sw1);
        _hidl_blob.putInt32(12 + _hidl_offset, this.sw2);
        _hidl_blob.putString(16 + _hidl_offset, this.simResponse);
    }
}
