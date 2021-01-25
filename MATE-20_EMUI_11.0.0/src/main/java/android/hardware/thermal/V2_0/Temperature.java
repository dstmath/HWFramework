package android.hardware.thermal.V2_0;

import android.os.HidlSupport;
import android.os.HwBlob;
import android.os.HwParcel;
import java.util.ArrayList;
import java.util.Objects;

public final class Temperature {
    public String name = new String();
    public int throttlingStatus;
    public int type;
    public float value;

    public final boolean equals(Object otherObject) {
        if (this == otherObject) {
            return true;
        }
        if (otherObject == null || otherObject.getClass() != Temperature.class) {
            return false;
        }
        Temperature other = (Temperature) otherObject;
        if (this.type == other.type && HidlSupport.deepEquals(this.name, other.name) && this.value == other.value && this.throttlingStatus == other.throttlingStatus) {
            return true;
        }
        return false;
    }

    public final int hashCode() {
        return Objects.hash(Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.type))), Integer.valueOf(HidlSupport.deepHashCode(this.name)), Integer.valueOf(HidlSupport.deepHashCode(Float.valueOf(this.value))), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.throttlingStatus))));
    }

    public final String toString() {
        return "{.type = " + TemperatureType.toString(this.type) + ", .name = " + this.name + ", .value = " + this.value + ", .throttlingStatus = " + ThrottlingSeverity.toString(this.throttlingStatus) + "}";
    }

    public final void readFromParcel(HwParcel parcel) {
        readEmbeddedFromParcel(parcel, parcel.readBuffer(32), 0);
    }

    public static final ArrayList<Temperature> readVectorFromParcel(HwParcel parcel) {
        ArrayList<Temperature> _hidl_vec = new ArrayList<>();
        HwBlob _hidl_blob = parcel.readBuffer(16);
        int _hidl_vec_size = _hidl_blob.getInt32(8);
        HwBlob childBlob = parcel.readEmbeddedBuffer((long) (_hidl_vec_size * 32), _hidl_blob.handle(), 0, true);
        _hidl_vec.clear();
        for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            Temperature _hidl_vec_element = new Temperature();
            _hidl_vec_element.readEmbeddedFromParcel(parcel, childBlob, (long) (_hidl_index_0 * 32));
            _hidl_vec.add(_hidl_vec_element);
        }
        return _hidl_vec;
    }

    public final void readEmbeddedFromParcel(HwParcel parcel, HwBlob _hidl_blob, long _hidl_offset) {
        this.type = _hidl_blob.getInt32(_hidl_offset + 0);
        this.name = _hidl_blob.getString(_hidl_offset + 8);
        parcel.readEmbeddedBuffer((long) (this.name.getBytes().length + 1), _hidl_blob.handle(), _hidl_offset + 8 + 0, false);
        this.value = _hidl_blob.getFloat(_hidl_offset + 24);
        this.throttlingStatus = _hidl_blob.getInt32(_hidl_offset + 28);
    }

    public final void writeToParcel(HwParcel parcel) {
        HwBlob _hidl_blob = new HwBlob(32);
        writeEmbeddedToBlob(_hidl_blob, 0);
        parcel.writeBuffer(_hidl_blob);
    }

    public static final void writeVectorToParcel(HwParcel parcel, ArrayList<Temperature> _hidl_vec) {
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
        _hidl_blob.putInt32(0 + _hidl_offset, this.type);
        _hidl_blob.putString(8 + _hidl_offset, this.name);
        _hidl_blob.putFloat(24 + _hidl_offset, this.value);
        _hidl_blob.putInt32(28 + _hidl_offset, this.throttlingStatus);
    }
}
