package android.hardware.radio.V1_0;

import android.os.HidlSupport;
import android.os.HwBlob;
import android.os.HwParcel;
import java.util.ArrayList;
import java.util.Objects;

public final class SuppSvcNotification {
    public int code;
    public int index;
    public boolean isMT;
    public String number = new String();
    public int type;

    public final boolean equals(Object otherObject) {
        if (this == otherObject) {
            return true;
        }
        if (otherObject == null || otherObject.getClass() != SuppSvcNotification.class) {
            return false;
        }
        SuppSvcNotification other = (SuppSvcNotification) otherObject;
        if (this.isMT == other.isMT && this.code == other.code && this.index == other.index && this.type == other.type && HidlSupport.deepEquals(this.number, other.number)) {
            return true;
        }
        return false;
    }

    public final int hashCode() {
        return Objects.hash(new Object[]{Integer.valueOf(HidlSupport.deepHashCode(Boolean.valueOf(this.isMT))), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.code))), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.index))), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.type))), Integer.valueOf(HidlSupport.deepHashCode(this.number))});
    }

    public final String toString() {
        return "{" + ".isMT = " + this.isMT + ", .code = " + this.code + ", .index = " + this.index + ", .type = " + this.type + ", .number = " + this.number + "}";
    }

    public final void readFromParcel(HwParcel parcel) {
        readEmbeddedFromParcel(parcel, parcel.readBuffer(32), 0);
    }

    public static final ArrayList<SuppSvcNotification> readVectorFromParcel(HwParcel parcel) {
        ArrayList<SuppSvcNotification> _hidl_vec = new ArrayList<>();
        HwBlob _hidl_blob = parcel.readBuffer(16);
        int _hidl_vec_size = _hidl_blob.getInt32(8);
        HwBlob childBlob = parcel.readEmbeddedBuffer((long) (_hidl_vec_size * 32), _hidl_blob.handle(), 0, true);
        _hidl_vec.clear();
        for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            SuppSvcNotification _hidl_vec_element = new SuppSvcNotification();
            _hidl_vec_element.readEmbeddedFromParcel(parcel, childBlob, (long) (_hidl_index_0 * 32));
            _hidl_vec.add(_hidl_vec_element);
        }
        return _hidl_vec;
    }

    public final void readEmbeddedFromParcel(HwParcel parcel, HwBlob _hidl_blob, long _hidl_offset) {
        HwBlob hwBlob = _hidl_blob;
        this.isMT = hwBlob.getBool(_hidl_offset + 0);
        this.code = hwBlob.getInt32(_hidl_offset + 4);
        this.index = hwBlob.getInt32(_hidl_offset + 8);
        this.type = hwBlob.getInt32(_hidl_offset + 12);
        this.number = hwBlob.getString(_hidl_offset + 16);
        parcel.readEmbeddedBuffer((long) (this.number.getBytes().length + 1), _hidl_blob.handle(), _hidl_offset + 16 + 0, false);
    }

    public final void writeToParcel(HwParcel parcel) {
        HwBlob _hidl_blob = new HwBlob(32);
        writeEmbeddedToBlob(_hidl_blob, 0);
        parcel.writeBuffer(_hidl_blob);
    }

    public static final void writeVectorToParcel(HwParcel parcel, ArrayList<SuppSvcNotification> _hidl_vec) {
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
        _hidl_blob.putBool(0 + _hidl_offset, this.isMT);
        _hidl_blob.putInt32(4 + _hidl_offset, this.code);
        _hidl_blob.putInt32(8 + _hidl_offset, this.index);
        _hidl_blob.putInt32(12 + _hidl_offset, this.type);
        _hidl_blob.putString(16 + _hidl_offset, this.number);
    }
}
